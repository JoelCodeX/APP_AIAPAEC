package com.jotadev.aiapaec.ui.screens.scan

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

object CornerDetector {
    enum class SheetType { S20, S50 }
    data class Boxes(val list: List<IntArray>)
    data class Corners(val list: List<FloatArray>)
    // CÍRCULOS DETECTADOS (cx, cy, r)
    data class Circles(val list: List<FloatArray>)

    fun detectarEsquinasYCuadrados(src: Bitmap, type: SheetType = SheetType.S20): Pair<Corners, Boxes> {
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
        // MEJORAR CONTRASTE (CLAHE para iluminación desigual)
        val eq = Mat(); val clahe = Imgproc.createCLAHE(2.0, Size(8.0, 8.0)); clahe.apply(gray, eq)
        // Pipeline según tipo de cartilla
        val thr = Mat()
        val dl = Mat()
        if (type == SheetType.S50) {
            val blur = Mat(); Imgproc.GaussianBlur(eq, blur, Size(5.0, 5.0), 0.0)
            Imgproc.threshold(blur, thr, 0.0, 255.0, Imgproc.THRESH_BINARY_INV or Imgproc.THRESH_OTSU)
            val kernel2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(2.0, 2.0))
            val cl2 = Mat(); Imgproc.morphologyEx(thr, cl2, Imgproc.MORPH_CLOSE, kernel2)
            Imgproc.dilate(cl2, dl, kernel2)
        } else {
            val blur = Mat(); Imgproc.GaussianBlur(eq, blur, Size(5.0, 5.0), 0.0)
            // COMBINAR OTSU + ADAPTATIVO para robustez en condiciones de luz variables
            val thrOtsu = Mat(); Imgproc.threshold(blur, thrOtsu, 0.0, 255.0, Imgproc.THRESH_BINARY_INV or Imgproc.THRESH_OTSU)
            val thrAdapt = Mat(); Imgproc.adaptiveThreshold(blur, thrAdapt, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 31, 2.0)
            Core.bitwise_or(thrOtsu, thrAdapt, thr)
            thrOtsu.release(); thrAdapt.release()
            val kernel3 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(3.0, 3.0))
            val er = Mat(); Imgproc.erode(thr, er, kernel3)
            val cl = Mat(); Imgproc.morphologyEx(er, cl, Imgproc.MORPH_CLOSE, kernel3)
            Imgproc.dilate(cl, dl, kernel3)
        }
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
            // EPS MÁS PERMISIVO para recuperar polígonos de 4 vértices
            val eps = if (type == SheetType.S50) 0.012 else 0.014
            Imgproc.approxPolyDP(c2f, ap, eps * p, true)
            approxList.add(ap)
            if (ap.rows() == 4) indices.add(idx)
        }
        // Construir rectángulos filtrados y centros precisos usando momentos del contorno
        val rectsAll = mutableListOf<IntArray>()
        val areas = mutableListOf<Double>()
        val aspects = mutableListOf<Double>()
        val centersAll = mutableListOf<Point>()
        for (i in indices) {
            val ap = approxList[i]
            val apPts = ap.toArray()
            val rect = Imgproc.boundingRect(MatOfPoint(*apPts))
            val rectArr = intArrayOf(rect.x, rect.y, rect.width, rect.height)
            val area = Imgproc.contourArea(ap)
            val aspect = if (rect.height > 0) rect.width.toDouble() / rect.height.toDouble() else 0.0
            // Centro por momentos del contorno original (más estable que rectángulo)
            val m = Imgproc.moments(contours[i])
            val cx = if (m.m00 != 0.0) m.m10 / m.m00 else (rect.x + rect.width / 2.0)
            val cy = if (m.m00 != 0.0) m.m01 / m.m00 else (rect.y + rect.height / 2.0)
            rectsAll.add(rectArr)
            areas.add(area)
            aspects.add(aspect)
            centersAll.add(Point(cx, cy))
        }
        // UMBRALES por tipo
        // ÁREA MÍNIMA MÁS BAJA para sensibilidad inmediata
        val areaMin = imgArea * (if (type == SheetType.S50) 0.0000008 else 0.000002)
        val areaMax = imgArea * 0.05
        val rectsF = mutableListOf<IntArray>()
        val centersF = mutableListOf<Point>()
        for (i in rectsAll.indices) {
            // FILTRO POR RELLENO: área del contorno vs área del rectángulo
            val rectArea = rectsAll[i][2].toDouble() * rectsAll[i][3].toDouble()
            val fillRatio = if (rectArea > 0.0) areas[i] / rectArea else 0.0
            // FILLO MÍNIMO MÁS PERMISIVO y aspecto más amplio
            val fillMin = if (type == SheetType.S50) 0.30 else 0.40
            val ok = areas[i] >= areaMin && areas[i] <= areaMax && aspects[i] >= 0.45 && aspects[i] <= 1.95 && fillRatio >= fillMin
            if (ok) {
                rectsF.add(rectsAll[i])
                centersF.add(centersAll[i])
            }
        }
        // SELECCIÓN POR CUADRANTES usando centros refinados
        val candidates = centersF.mapIndexed { idx, p -> p to rectsF[idx] }
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
        val orderCenters = listOf(centersF[tlIdx!!], centersF[trIdx!!], centersF[brIdx!!], centersF[blIdx!!])
        val pts = orderCenters.map { p ->
            floatArrayOf((p.x * inv).toFloat(), (p.y * inv).toFloat())
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

    // DETECTAR CÍRCULOS TIPO S20 EN TIEMPO REAL
    fun detectarCirculosS20(src: Bitmap): Circles {
        val img = Mat(); Utils.bitmapToMat(src, img)
        val h = img.rows(); val w = img.cols()
        val target = 2200.0
        val scale = target / maxOf(h, w).toDouble()
        val resized = Mat(); Imgproc.resize(img, resized, Size(w * scale, h * scale), 0.0, 0.0, Imgproc.INTER_AREA)
        val gray = Mat(); Imgproc.cvtColor(resized, gray, Imgproc.COLOR_RGBA2GRAY)
        val eq = Mat(); val clahe = Imgproc.createCLAHE(2.0, Size(8.0, 8.0)); clahe.apply(gray, eq)
        val blur = Mat(); Imgproc.GaussianBlur(eq, blur, Size(5.0, 5.0), 0.0)

        val circles = Mat()
        // HOUGH CIRCLES: parámetros adaptados a burbujas impresas
        // dp=1.2, minDist proporcional, thresholds moderados para estabilidad
        val minDist = (resized.rows() / 36.0)
        Imgproc.HoughCircles(
            blur,
            circles,
            Imgproc.HOUGH_GRADIENT,
            1.2,
            minDist,
            120.0,
            22.0,
            12,
            40
        )
        val inv = 1.0 / scale
        val out = ArrayList<FloatArray>()
        val cols = circles.cols()
        val rows = circles.rows()
        val n = if (cols > 1 && rows == 1) cols else rows
        for (i in 0 until n) {
            val data = if (rows == 1) circles.get(0, i) else circles.get(i, 0)
            if (data != null && data.size >= 3) {
                val cx = (data[0] * inv).toFloat()
                val cy = (data[1] * inv).toFloat()
                val r = (data[2] * inv).toFloat()
                out.add(floatArrayOf(cx, cy, r))
            }
        }

        // Filtrar y normalizar a exactamente 100 si hay más
        if (out.size > 100) {
            // Separar por mitad vertical aproximada y tomar 50 por bloque
            val midY = (h / 2.0f)
            val top = out.filter { it[1] < midY }.sortedBy { it[1] }.take(50)
            val bottom = out.filter { it[1] >= midY }.sortedBy { it[1] }.take(50)
            val merged = ArrayList<FloatArray>(100); merged.addAll(top); merged.addAll(bottom)
            return Circles(merged)
        }
        // Si faltan, devolver los encontrados (overlay informativo)
        return Circles(out)
    }
}