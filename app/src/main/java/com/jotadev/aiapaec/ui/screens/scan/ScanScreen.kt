package com.jotadev.aiapaec.ui.screens.scan

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.YuvImage
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.Surface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.navigation.NavController
import com.google.gson.Gson
import com.jotadev.aiapaec.navigation.NavigationRoutes
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(navController: NavController) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Escanear",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Regresar",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        // Variables de cámara y permisos
        val context = LocalContext.current
        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
        var hasPermission by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            )
        }
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasPermission = granted
        }

        var previewView by remember { mutableStateOf<PreviewView?>(null) }
        val mainExecutor = ContextCompat.getMainExecutor(context)
        var markers by remember { mutableStateOf(listOf(false, false, false, false)) }
        var stableCount by remember { mutableStateOf(0) }
        var capturing by remember { mutableStateOf(false) }
        val client = remember { OkHttpClient() }
        val gson = remember { Gson() }
        var cornersNorm by remember { mutableStateOf<List<Pair<Float, Float>>>(emptyList()) }
        var boxesNorm by remember { mutableStateOf<List<List<Float>>>(emptyList()) }
        var cornersCount by remember { mutableIntStateOf(0) }
        var frameW by remember { mutableIntStateOf(0) }
        var frameH by remember { mutableIntStateOf(0) }
        var roiNorm by remember { mutableStateOf<List<Float>?>(null) }
        val currentRotation = LocalView.current.display?.rotation ?: 0
        val imageCapture = remember(currentRotation) {
            ImageCapture.Builder()
                .setTargetRotation(Surface.ROTATION_0)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setJpegQuality(75)
                .build()
        }

        LaunchedEffect(Unit) {
            if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
        }

        LaunchedEffect(hasPermission, previewView) {
            if (hasPermission && previewView != null) {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build()
                    preview.setSurfaceProvider(previewView!!.surfaceProvider)
                    val selector = CameraSelector.DEFAULT_BACK_CAMERA
                    val analysis = ImageAnalysis.Builder()
                        .setTargetRotation(Surface.ROTATION_0)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                        .build()
                    var busy = false
                    var lastTs = 0L
                    analysis.setAnalyzer(mainExecutor) { image ->
                        val now = System.currentTimeMillis()
                        if (!busy && now - lastTs > 45) {
                            busy = true
                            lastTs = now
                            Thread {
                                val res = detectMarkersRemote(client, image, roiNorm)
                                image.close()
                                if (res != null) {
                                    androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot {
                                        frameW = res.w
                                        frameH = res.h
                                        val ordered = reorderCornersAndBoxes(res.corners, res.boxes)
                                        val oc = ordered.first
                                        val ob = ordered.second
                                        cornersNorm = oc.map { p ->
                                            Pair(p[0] / res.w.toFloat(), p[1] / res.h.toFloat())
                                        }
                                        boxesNorm = ob.map { b ->
                                            listOf(
                                                b[0] / res.w.toFloat(),
                                                b[1] / res.h.toFloat(),
                                                b[2] / res.w.toFloat(),
                                                b[3] / res.h.toFloat()
                                            )
                                        }
                                        cornersCount = res.count
                                        markers = res.markers.toList()
                                    }
                                }
                                busy = false
                            }.start()
                        } else {
                            image.close()
                        }
                    }
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, analysis, imageCapture)
                }, ContextCompat.getMainExecutor(context))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        previewView = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            ScanningOverlay(
                primaryInstruction = "Alinear cuadrados en visores",
                secondaryInstruction = "Mantén la hoja estable para el escaneo",
                markers = markers,
                cornersNorm = cornersNorm,
                boxesNorm = boxesNorm,
                cornersCount = cornersCount,
                frameW = frameW,
                frameH = frameH,
                onRoiComputed = { r -> roiNorm = r.toList() },
                onMarkersComputed = { inside ->
                    if (inside.all { it }) {
                        stableCount++
                        if (!capturing && stableCount >= 1) {
                            capturing = true
                            stableCount = 0
                            captureAndProcess(imageCapture, context, client, gson, navController) {
                                capturing = false
                            }
                        }
                    } else {
                        stableCount = 0
                    }
                }
            )

            // Botón de captura manual
            IconButton(
                onClick = {
                    captureAndProcess(imageCapture, context, client, gson, navController) {}
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Capturar",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun ScanningOverlay(
    primaryInstruction: String,
    secondaryInstruction: String? = null,
    markers: List<Boolean> = listOf(false, false, false, false),
    cornersNorm: List<Pair<Float, Float>> = emptyList(),
    boxesNorm: List<List<Float>> = emptyList(),
    cornersCount: Int = 0,
    frameW: Int = 0,
    frameH: Int = 0,
    onRoiComputed: (FloatArray) -> Unit = {},
    onMarkersComputed: (BooleanArray) -> Unit = {}
) {
    val cornerSize = 90.dp
    val cornerPadding = 0.dp
    val bottomOffset = 290.dp
    val density = LocalDensity.current

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val boxWpx = with(density) { maxWidth.toPx() }
        val boxHpx = with(density) { maxHeight.toPx() }
        val bottomOffsetPx = with(density) { bottomOffset.toPx() }
        val cornerPadPx = with(density) { cornerPadding.toPx() }
        val cornerSizePx = with(density) { cornerSize.toPx() }

        // DEFINIR LAS ÁREAS DE LOS CUADRADOS (donde será TRANSPARENTE)
        val transparentAreas = listOf(
            androidx.compose.ui.geometry.Rect(
                left = cornerPadPx,
                top = 0f,
                right = cornerPadPx + cornerSizePx,
                bottom = cornerSizePx
            ),
            androidx.compose.ui.geometry.Rect(
                left = boxWpx - cornerPadPx - cornerSizePx,
                top = 0f,
                right = boxWpx - cornerPadPx,
                bottom = cornerSizePx
            ),
            androidx.compose.ui.geometry.Rect(
                left = cornerPadPx,
                top = boxHpx - bottomOffsetPx - cornerSizePx,
                right = cornerPadPx + cornerSizePx,
                bottom = boxHpx - bottomOffsetPx
            ),
            androidx.compose.ui.geometry.Rect(
                left = boxWpx - cornerPadPx - cornerSizePx,
                top = boxHpx - bottomOffsetPx - cornerSizePx,
                right = boxWpx - cornerPadPx,
                bottom = boxHpx - bottomOffsetPx
            )
        )

        // CAPA OSCURA SEMI-TRANSPARENTE con agujeros en los cuadrados
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Crear path para el fondo oscuro con agujeros
            val path = android.graphics.Path().apply {
                fillType = android.graphics.Path.FillType.EVEN_ODD

                // Agregar rectángulo completo (fondo oscuro)
                addRect(0f, 0f, boxWpx, boxHpx, android.graphics.Path.Direction.CW)

                // Agregar los cuadrados como "agujeros" transparentes
                transparentAreas.forEach { rect ->
                    addRect(
                        rect.left,
                        rect.top,
                        rect.right,
                        rect.bottom,
                        android.graphics.Path.Direction.CW
                    )
                }
            }

            // Dibujar SOLO el fondo oscuro (los cuadrados quedan transparentes automáticamente)
            drawPath(
                path = path.asComposePath(),
                color = Color.Black.copy(alpha = 0.4f), // AJUSTA TRANSPARENCIA AQUÍ: 0.3f = poco oscuro, 0.6f = más oscuro
                blendMode = BlendMode.SrcOver
            )
        }

        // GEOMETRÍA DE CUADROS DE ESQUINA Y ESCALA DE FRAME (NECESARIO PARA CÁLCULOS)
        val topLeft = androidx.compose.ui.geometry.Rect(
            left = cornerPadPx,
            top = 0f,
            right = cornerPadPx + cornerSizePx,
            bottom = cornerSizePx
        )
        val topRight = androidx.compose.ui.geometry.Rect(
            left = boxWpx - cornerPadPx - cornerSizePx,
            top = 0f,
            right = boxWpx - cornerPadPx,
            bottom = cornerSizePx
        )
        val bottomLeft = androidx.compose.ui.geometry.Rect(
            left = cornerPadPx,
            top = boxHpx - bottomOffsetPx - cornerSizePx,
            right = cornerPadPx + cornerSizePx,
            bottom = boxHpx - bottomOffsetPx
        )
        val bottomRight = androidx.compose.ui.geometry.Rect(
            left = boxWpx - cornerPadPx - cornerSizePx,
            top = boxHpx - bottomOffsetPx - cornerSizePx,
            right = boxWpx - cornerPadPx,
            bottom = boxHpx - bottomOffsetPx
        )

        val hasDims = frameW > 0 && frameH > 0
        val scale = if (hasDims) max(boxWpx / frameW.toFloat(), boxHpx / frameH.toFloat()) else 1f
        val scaledW = if (hasDims) frameW * scale else boxWpx
        val scaledH = if (hasDims) frameH * scale else boxHpx
        val offsetX = (boxWpx - scaledW) / 2f
        val offsetY = (boxHpx - scaledH) / 2f

        // ROI NORMALIZADA PARA BACKEND BASADA EN VISORES DE ESQUINA
        if (hasDims) {
            val minLeft = minOf(topLeft.left, topRight.left, bottomLeft.left, bottomRight.left)
            val minTop = minOf(topLeft.top, topRight.top, bottomLeft.top, bottomRight.top)
            val maxRight = maxOf(topLeft.right, topRight.right, bottomLeft.right, bottomRight.right)
            val maxBottom = maxOf(topLeft.bottom, topRight.bottom, bottomLeft.bottom, bottomRight.bottom)
            val rx = ((minLeft - offsetX) / scaledW).coerceIn(0f, 1f)
            val ry = ((minTop - offsetY) / scaledH).coerceIn(0f, 1f)
            val rw = ((maxRight - minLeft) / scaledW).coerceIn(0f, 1f)
            val rh = ((maxBottom - minTop) / scaledH).coerceIn(0f, 1f)
            // AMPLIAR ROI CON MARGEN PARA MAYOR SENSIBILIDAD
            val pad = 0.10f
            val rxPad = (rx - pad).coerceIn(0f, 1f)
            val ryPad = (ry - pad).coerceIn(0f, 1f)
            val rwPad = (rw + pad * 2f).coerceIn(0f, 1f)
            val rhPad = (rh + pad * 2f).coerceIn(0f, 1f)
            onRoiComputed(floatArrayOf(rxPad, ryPad, rwPad, rhPad))
        }

        val inside = BooleanArray(4) { false }
        val insideBoxes = BooleanArray(4) { false }
        val insideCorners = BooleanArray(4) { false }
        val boxColor = Color(0xFF00C853)

        // Dibujar solo marcadores dentro de los recuadros UI (filtro estricto)
        Canvas(modifier = Modifier.fillMaxSize()) {
            boxesNorm.forEachIndexed { idx, b ->
                val px = offsetX + b[0] * scaledW
                val py = offsetY + b[1] * scaledH
                val pw = b[2] * scaledW
                val ph = b[3] * scaledH
                val cx = px + pw * 0.5f
                val cy = py + ph * 0.5f
                val pad = 8f
                val contained = when (idx) {
                    0 -> (cx >= (topLeft.left - pad) && cy >= (topLeft.top - pad) && cx <= (topLeft.right + pad) && cy <= (topLeft.bottom + pad))
                    1 -> (cx >= (topRight.left - pad) && cy >= (topRight.top - pad) && cx <= (topRight.right + pad) && cy <= (topRight.bottom + pad))
                    2 -> (cx >= (bottomRight.left - pad) && cy >= (bottomRight.top - pad) && cx <= (bottomRight.right + pad) && cy <= (bottomRight.bottom + pad))
                    3 -> (cx >= (bottomLeft.left - pad) && cy >= (bottomLeft.top - pad) && cx <= (bottomLeft.right + pad) && cy <= (bottomLeft.bottom + pad))
                    else -> false
                }
                when (idx) {
                    0 -> insideBoxes[0] = contained
                    1 -> insideBoxes[1] = contained
                    2 -> insideBoxes[2] = contained
                    3 -> insideBoxes[3] = contained
                }
                if (contained) {
                    drawRect(
                        color = boxColor,
                        topLeft = androidx.compose.ui.geometry.Offset(px, py),
                        size = androidx.compose.ui.geometry.Size(pw, ph),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                    )
                }
            }
            cornersNorm.forEachIndexed { idx, p ->
                val cx = offsetX + p.first * scaledW
                val cy = offsetY + p.second * scaledH
                val pad = 4f
                val containedCorner = when (idx) {
                    0 -> (cx >= (topLeft.left - pad) && cy >= (topLeft.top - pad) && cx <= (topLeft.right + pad) && cy <= (topLeft.bottom + pad))
                    1 -> (cx >= (topRight.left - pad) && cy >= (topRight.top - pad) && cx <= (topRight.right + pad) && cy <= (topRight.bottom + pad))
                    2 -> (cx >= (bottomRight.left - pad) && cy >= (bottomRight.top - pad) && cx <= (bottomRight.right + pad) && cy <= (bottomRight.bottom + pad))
                    3 -> (cx >= (bottomLeft.left - pad) && cy >= (bottomLeft.top - pad) && cx <= (bottomLeft.right + pad) && cy <= (bottomLeft.bottom + pad))
                    else -> false
                }
                when (idx) {
                    0 -> insideCorners[0] = containedCorner
                    1 -> insideCorners[1] = containedCorner
                    2 -> insideCorners[2] = containedCorner
                    3 -> insideCorners[3] = containedCorner
                }
                if (containedCorner) {
                    drawCircle(
                        color = boxColor,
                        radius = 6f,
                        center = androidx.compose.ui.geometry.Offset(cx, cy),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                    )
                }
            }
        }

        LaunchedEffect(boxesNorm, cornersNorm, frameW, frameH) {
            for (i in 0 until 4) {
                // ESTRICTO: solo considerar dentro si cualquiera de las dos pruebas en UI es verdadera
                val inBox = insideBoxes.getOrNull(i) ?: false
                val inCorner = insideCorners.getOrNull(i) ?: false
                inside[i] = inBox || inCorner
            }
            onMarkersComputed(inside)
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = primaryInstruction,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (secondaryInstruction != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = secondaryInstruction,
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$cornersCount / 4",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ... (el resto de las funciones se mantienen IGUAL: captureAndProcess, detectCornersRemote, cropRemote, etc.)

private fun captureAndProcess(
    imageCapture: ImageCapture,
    context: android.content.Context,
    client: OkHttpClient,
    gson: Gson,
    navController: NavController,
    onDone: () -> Unit
) {
    val output = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "scan_${System.currentTimeMillis()}.jpg")
    val outOptions = ImageCapture.OutputFileOptions.Builder(output).build()
    imageCapture.takePicture(outOptions, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
        override fun onError(exception: ImageCaptureException) {
            onDone()
        }
        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            Thread {
                val detectCorners = detectCornersRemote(client, output)
                if (detectCorners != null) {
                    val outFile = cropRemote(client, gson, output, detectCorners)
                    Handler(Looper.getMainLooper()).post {
                        onDone()
                        if (outFile != null) {
                            val encoded = Uri.encode(outFile.absolutePath)
                            navController.navigate(NavigationRoutes.cropPreview(encoded))
                        }
                    }
                } else {
                    Handler(Looper.getMainLooper()).post { onDone() }
                }
            }.start()
        }
    })
}

private fun detectCornersRemote(
    client: OkHttpClient,
    file: File
): List<List<Float>>? {
    val url = "http://192.168.18.224:5000/scan/detect-corners"
    val body = MultipartBody.Builder().setType(MultipartBody.FORM)
        .addFormDataPart("file", file.name, file.asRequestBody("image/jpeg".toMediaType()))
        .build()
    val req = Request.Builder().url(url).post(body).build()
    client.newCall(req).execute().use { resp ->
        if (!resp.isSuccessful) return null
        val text = resp.body?.string() ?: return null
        val json = JSONObject(text)
        val arr = json.getJSONArray("corners")
        val out = ArrayList<List<Float>>(4)
        for (i in 0 until arr.length()) {
            val p = arr.getJSONArray(i)
            out.add(listOf(p.getDouble(0).toFloat(), p.getDouble(1).toFloat()))
        }
        return out
    }
}

private fun cropRemote(
    client: OkHttpClient,
    gson: Gson,
    file: File,
    corners: List<List<Float>>
): File? {
    val url = "http://192.168.18.224:5000/scan/crop"
    val cornersJson = gson.toJson(corners)
    val body = MultipartBody.Builder().setType(MultipartBody.FORM)
        .addFormDataPart("file", file.name, file.asRequestBody("image/jpeg".toMediaType()))
        .addFormDataPart("corners", cornersJson)
        .addFormDataPart("width", "1200")
        .addFormDataPart("height", "1800")
        .build()
    val req = Request.Builder().url(url).post(body).build()
    client.newCall(req).execute().use { resp ->
        if (!resp.isSuccessful) return null
        val png = resp.body?.bytes() ?: return null
        val out = File(file.parentFile, file.nameWithoutExtension + "_crop.png")
        FileOutputStream(out).use { it.write(png) }
        return out
    }
}

private fun yuv420ToJpeg(image: ImageProxy, quality: Int = 70): ByteArray {
    val yPlane = image.planes[0].buffer
    val uPlane = image.planes[1].buffer
    val vPlane = image.planes[2].buffer
    val ySize = yPlane.remaining()
    val uSize = uPlane.remaining()
    val vSize = vPlane.remaining()
    val nv21 = ByteArray(ySize + uSize + vSize)
    yPlane.get(nv21, 0, ySize)
    val pixelStride = image.planes[2].pixelStride
    val rowStride = image.planes[2].rowStride
    val w = image.width
    val h = image.height
    var offset = ySize
    val vBuffer = ByteArray(vSize)
    val uBuffer = ByteArray(uSize)
    vPlane.get(vBuffer)
    uPlane.get(uBuffer)
    var i = 0
    while (i < vSize && i < uSize) {
        nv21[offset++] = vBuffer[i]
        nv21[offset++] = uBuffer[i]
        i += pixelStride
    }
    val yuv = YuvImage(nv21, ImageFormat.NV21, w, h, null)
    val out = java.io.ByteArrayOutputStream()
    yuv.compressToJpeg(android.graphics.Rect(0, 0, w, h), quality, out)
    val bytes = out.toByteArray()
    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    val rotation = image.imageInfo.rotationDegrees
    if (rotation == 0) {
        // SIN ROTACIÓN: DEVOLVER JPEG DIRECTO PARA MAXIMIZAR VELOCIDAD
        return bytes
    }
    val matrix = android.graphics.Matrix()
    matrix.postRotate(rotation.toFloat())
    val rotated = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
    val maxSide = max(rotated.width, rotated.height)
    val targetMax = 960
    val finalBitmap = if (maxSide > targetMax) {
        val scale = targetMax.toFloat() / maxSide.toFloat()
        val newW = (rotated.width * scale).toInt()
        val newH = (rotated.height * scale).toInt()
        Bitmap.createScaledBitmap(rotated, newW, newH, true)
    } else rotated
    val out2 = java.io.ByteArrayOutputStream()
    finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out2)
    return out2.toByteArray()
}

private data class DetectionResult(
    val markers: BooleanArray,
    val corners: List<List<Float>>,
    val boxes: List<List<Float>>,
    val count: Int,
    val w: Int,
    val h: Int
)

private fun detectMarkersRemote(client: OkHttpClient, image: ImageProxy, roiNorm: List<Float>?): DetectionResult? {
    val url = "http://192.168.18.224:5000/scan/detect-corners"
    val jpeg = yuv420ToJpeg(image, 70)
    val bodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
        .addFormDataPart("file", "frame.jpg", jpeg.toRequestBody("image/jpeg".toMediaType()))
    if (roiNorm != null && roiNorm.size == 4) {
        val roiStr = "[${roiNorm[0]},${roiNorm[1]},${roiNorm[2]},${roiNorm[3]}]"
        bodyBuilder.addFormDataPart("roi", roiStr)
    }
    val body = bodyBuilder.build()
    val req = Request.Builder().url(url).post(body).build()
    client.newCall(req).execute().use { resp ->
        if (!resp.isSuccessful) return null
        val text = resp.body?.string() ?: return null
        val json = JSONObject(text)
        val cornersJson = json.optJSONArray("corners") ?: return null
        val markersJson = json.optJSONArray("markers")
        val boxesJson = json.optJSONArray("boxes")
        val countJson = json.optInt("count", cornersJson.length())
        val imageSizeJson = json.optJSONObject("image_size")
        val corners = ArrayList<List<Float>>(4)
        for (i in 0 until cornersJson.length()) {
            val c = cornersJson.getJSONArray(i)
            corners.add(listOf(c.getDouble(0).toFloat(), c.getDouble(1).toFloat()))
        }
        val boxes = ArrayList<List<Float>>()
        if (boxesJson != null) {
            for (i in 0 until boxesJson.length()) {
                val b = boxesJson.getJSONArray(i)
                boxes.add(listOf(b.getDouble(0).toFloat(), b.getDouble(1).toFloat(), b.getDouble(2).toFloat(), b.getDouble(3).toFloat()))
            }
        }
        val markers = if (markersJson != null && markersJson.length() == 4) {
            BooleanArray(4) { i -> markersJson.getBoolean(i) }
        } else BooleanArray(4) { false }
        val w = imageSizeJson?.optInt("w", image.width) ?: image.width
        val h = imageSizeJson?.optInt("h", image.height) ?: image.height
        return DetectionResult(markers, corners, boxes, countJson, w, h)
    }
}

private fun reorderCornersAndBoxes(
    corners: List<List<Float>>,
    boxes: List<List<Float>>
): Pair<List<List<Float>>, List<List<Float>>> {
    if (corners.size != 4) return Pair(corners, boxes)
    val idxByY = (0..3).sortedBy { corners[it][1] }
    val top = idxByY.take(2).sortedBy { corners[it][0] }
    val bottom = idxByY.takeLast(2).sortedBy { corners[it][0] }
    val order = listOf(top[0], top[1], bottom[1], bottom[0])
    val orderedCorners = order.map { corners[it] }
    val centers = boxes.map { listOf(it[0] + it[2] * 0.5f, it[1] + it[3] * 0.5f) }
    val used = BooleanArray(boxes.size) { false }
    val orderedBoxes = ArrayList<List<Float>>(4)
    for (i in 0 until 4) {
        val cx = orderedCorners[i][0]
        val cy = orderedCorners[i][1]
        var best = -1
        var bestDist = Float.MAX_VALUE
        for (j in boxes.indices) {
            if (used[j]) continue
            val dx = centers[j][0] - cx
            val dy = centers[j][1] - cy
            val d = dx * dx + dy * dy
            if (d < bestDist) {
                bestDist = d
                best = j
            }
        }
        if (best >= 0) {
            used[best] = true
            orderedBoxes.add(boxes[best])
        } else {
            orderedBoxes.add(listOf(0f, 0f, 0f, 0f))
        }
    }
    return Pair(orderedCorners, orderedBoxes)
}