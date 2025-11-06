package com.jotadev.aiapaec.ui.screens.scan

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

object CornerDetector {
    data class Boxes(val list: List<IntArray>)
    data class Corners(val list: List<FloatArray>)

    fun detectarEsquinasYCuadrados(src: Bitmap): Pair<Corners, Boxes> {
        val img = Mat()
        Utils.bitmapToMat(src, img)
        val h = img.rows()
        val w = img.cols()
        val target = 2200.0
        val scale = target / maxOf(h, w).toDouble()
        val resized = Mat()
        Imgproc.resize(img, resized, Size(w * scale, h * scale), 0.0, 0.0, Imgproc.INTER_AREA)
        val gray = Mat()
        // CONVERTIR CORRECTAMENTE DESDE RGBA (bitmapToMat produce 8UC4)
        Imgproc.cvtColor(resized, gray, Imgproc.COLOR_RGBA2GRAY)
        // MEJORAR CONTRASTE PARA CUADRADOS PEQUEÑOS
        val eq = Mat(); Imgproc.equalizeHist(gray, eq)
        val blur = Mat()
        Imgproc.GaussianBlur(eq, blur, Size(5.0, 5.0), 0.0)
        val thr = Mat()
        // RESTAURAR OTSU PARA UNA SEGMENTACIÓN MÁS ESTABLE EN FONDOS BLANCOS
        Imgproc.threshold(blur, thr, 0.0, 255.0, Imgproc.THRESH_BINARY_INV or Imgproc.THRESH_OTSU)
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(3.0, 3.0))
        val er = Mat(); Imgproc.erode(thr, er, kernel)
        val cl = Mat(); Imgproc.morphologyEx(er, cl, Imgproc.MORPH_CLOSE, kernel)
        val dl = Mat(); Imgproc.dilate(cl, dl, kernel)
        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(dl, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)
        val imgArea = (dl.rows() * dl.cols()).toDouble()
        val indices = mutableListOf<Int>()
        val approxList = mutableListOf<MatOfPoint2f>()
        for ((idx, c) in contours.withIndex()) {
            val c2f = MatOfPoint2f(*c.toArray())
            val p = Imgproc.arcLength(c2f, true)
            val ap = MatOfPoint2f()
            Imgproc.approxPolyDP(c2f, ap, 0.015 * p, true)
            approxList.add(ap)
            if (ap.rows() == 4) indices.add(idx)
        }
        val rectsAll = mutableListOf<IntArray>()
        val areas = mutableListOf<Double>()
        for (i in indices) {
            val ap = approxList[i]
            val apPts = ap.toArray()
            val rect = Imgproc.boundingRect(MatOfPoint(*apPts))
            rectsAll.add(intArrayOf(rect.x, rect.y, rect.width, rect.height))
            areas.add(Imgproc.contourArea(ap))
        }
        val rectsArr = rectsAll.toTypedArray()
        val aspects = rectsArr.map { it[2].toDouble() / it[3].toDouble() }
        // UMBRALES MÁS PERMISIVOS PARA CUADRADOS PEQUEÑOS (PLANTILLA 50)
        val areaMin = imgArea * 0.000002
        val areaMax = imgArea * 0.05
        val rectsF = mutableListOf<IntArray>()
        for (i in rectsArr.indices) {
            // FILTRO POR RELLENO: área del contorno vs área del rectángulo
            val rectArea = rectsArr[i][2].toDouble() * rectsArr[i][3].toDouble()
            val fillRatio = if (rectArea > 0.0) areas[i] / rectArea else 0.0
            val ok = areas[i] >= areaMin && areas[i] <= areaMax && aspects[i] >= 0.5 && aspects[i] <= 1.85 && fillRatio >= 0.55
            if (ok) rectsF.add(rectsArr[i])
        }
        // SELECCIÓN POR CUADRANTES: ELEGIR EL MÁS CERCANO A CADA ESQUINA
        val candidates = rectsF.map { r ->
            val cx = r[0] + r[2] / 2.0
            val cy = r[1] + r[3] / 2.0
            Point(cx, cy) to r
        }
        fun pickNearest(target: Point, taken: MutableSet<Int>): Int? {
            var bestIdx: Int? = null
            var bestDist = Double.MAX_VALUE
            for ((i, cand) in candidates.withIndex()) {
                if (taken.contains(i)) continue
                val dx = cand.first.x - target.x
                val dy = cand.first.y - target.y
                val d = dx * dx + dy * dy
                if (d < bestDist) { bestDist = d; bestIdx = i }
            }
            return bestIdx
        }
        val taken = mutableSetOf<Int>()
        val tlIdx = pickNearest(Point(0.0, 0.0), taken)?.also { taken.add(it) }
        val trIdx = pickNearest(Point(dl.cols().toDouble(), 0.0), taken)?.also { taken.add(it) }
        val brIdx = pickNearest(Point(dl.cols().toDouble(), dl.rows().toDouble()), taken)?.also { taken.add(it) }
        val blIdx = pickNearest(Point(0.0, dl.rows().toDouble()), taken)?.also { taken.add(it) }
        val picked = listOfNotNull(tlIdx, trIdx, brIdx, blIdx)
        val inv = 1.0 / scale
        if (picked.size < 4) return Corners(emptyList()) to Boxes(emptyList())
        val orderRects = listOf(rectsF[tlIdx!!], rectsF[trIdx!!], rectsF[brIdx!!], rectsF[blIdx!!])
        val pts = orderRects.map { r ->
            val cx = r[0] + r[2] / 2.0
            val cy = r[1] + r[3] / 2.0
            floatArrayOf((cx * inv).toFloat(), (cy * inv).toFloat())
        }
        val boxes = orderRects.map { r -> intArrayOf((r[0] * inv).toInt(), (r[1] * inv).toInt(), (r[2] * inv).toInt(), (r[3] * inv).toInt()) }
        return Corners(pts) to Boxes(boxes)
    }

    fun recortarPorEsquinas(src: Bitmap, esquinas: List<FloatArray>, ancho: Int, alto: Int): Bitmap {
        val img = Mat(); Utils.bitmapToMat(src, img)
        val dst = Mat(4, 1, CvType.CV_32FC2)
        val srcPts = Mat(4, 1, CvType.CV_32FC2)
        val s0 = org.opencv.core.Point(esquinas[0][0].toDouble(), esquinas[0][1].toDouble())
        val s1 = org.opencv.core.Point(esquinas[1][0].toDouble(), esquinas[1][1].toDouble())
        val s2 = org.opencv.core.Point(esquinas[2][0].toDouble(), esquinas[2][1].toDouble())
        val s3 = org.opencv.core.Point(esquinas[3][0].toDouble(), esquinas[3][1].toDouble())
        srcPts.put(0, 0, floatArrayOf(s0.x.toFloat(), s0.y.toFloat()))
        srcPts.put(1, 0, floatArrayOf(s1.x.toFloat(), s1.y.toFloat()))
        srcPts.put(2, 0, floatArrayOf(s2.x.toFloat(), s2.y.toFloat()))
        srcPts.put(3, 0, floatArrayOf(s3.x.toFloat(), s3.y.toFloat()))
        dst.put(0, 0, floatArrayOf(0f, 0f))
        dst.put(1, 0, floatArrayOf((ancho - 1).toFloat(), 0f))
        dst.put(2, 0, floatArrayOf((ancho - 1).toFloat(), (alto - 1).toFloat()))
        dst.put(3, 0, floatArrayOf(0f, (alto - 1).toFloat()))
        val M = Imgproc.getPerspectiveTransform(srcPts, dst)
        val out = Mat()
        Imgproc.warpPerspective(img, out, M, Size(ancho.toDouble(), alto.toDouble()))
        val bmp = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(out, bmp)
        return bmp
    }

    // RECORTE CON DIMENSIÓN ADAPTATIVA BASADA EN DISTANCIAS ENTRE ESQUINAS
    fun recortarPorEsquinasAutoSize(src: Bitmap, esquinas: List<FloatArray>, maxDim: Int = 2000): Bitmap {
        fun dist(a: FloatArray, b: FloatArray): Double {
            val dx = (a[0] - b[0]).toDouble(); val dy = (a[1] - b[1]).toDouble()
            return Math.hypot(dx, dy)
        }
        val wTop = dist(esquinas[0], esquinas[1])
        val wBottom = dist(esquinas[3], esquinas[2])
        val hLeft = dist(esquinas[0], esquinas[3])
        val hRight = dist(esquinas[1], esquinas[2])
        val wAvg = ((wTop + wBottom) / 2.0).toInt().coerceAtLeast(600)
        val hAvg = ((hLeft + hRight) / 2.0).toInt().coerceAtLeast(800)
        val scale = (maxDim.toDouble() / maxOf(wAvg, hAvg).toDouble()).coerceAtMost(1.5)
        val outW = (wAvg * scale).toInt()
        val outH = (hAvg * scale).toInt()
        return recortarPorEsquinas(src, esquinas, outW, outH)
    }
}