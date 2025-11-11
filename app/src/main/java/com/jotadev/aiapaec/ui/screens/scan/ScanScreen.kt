package com.jotadev.aiapaec.ui.screens.scan

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
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
import androidx.camera.core.Preview as CameraPreview
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
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.tooling.preview.Preview
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

private const val BASE_URL = "http://10.0.2.2:5000"

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
        var stableCount by remember { mutableIntStateOf(0) }
        var capturing by remember { mutableStateOf(false) }
        val client = remember { OkHttpClient() }
        val gson = remember { Gson() }
    var cornersNorm by remember { mutableStateOf<List<Pair<Float, Float>>>(emptyList()) }
    var circlesNorm by remember { mutableStateOf<List<List<Float>>>(emptyList()) }
    var sheetType by remember { mutableStateOf(CornerDetector.SheetType.S20) }
        var boxesNorm by remember { mutableStateOf<List<List<Float>>>(emptyList()) }
        var cornersCount by remember { mutableIntStateOf(0) }
        var frameW by remember { mutableIntStateOf(0) }
        var frameH by remember { mutableIntStateOf(0) }
        var roiNorm by remember { mutableStateOf<List<Float>?>(null) }
        var roiLocked by remember { mutableStateOf(false) }
        // ESTADO PREVIO DE ESQUINAS PARA MEDIR DERIVA ENTRE FRAMES
        var prevCornersNorm by remember { mutableStateOf<List<Pair<Float, Float>>>(emptyList()) }
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
                    val preview = CameraPreview.Builder().build()
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
                val res = detectMarkersLocal(image, roiNorm, sheetType)
                                image.close()
                                if (res != null) {
                                    androidx.compose.runtime.snapshots.Snapshot.withMutableSnapshot {
                                        frameW = res.w
                                        frameH = res.h
                                        val ordered = reorderCornersAndBoxes(res.corners, res.boxes)
                                        val oc = ordered.first
                                        val ob = ordered.second
                                        // HOLD-LAST: si falta alguna esquina/caja, mantener la última válida para evitar parpadeo
                                        val nextCorners = if (oc.size == 4) {
                                            oc.map { p -> Pair(p[0] / res.w.toFloat(), p[1] / res.h.toFloat()) }
                                        } else if (cornersNorm.size == 4) {
                                            cornersNorm.toList()
                                        } else {
                                            oc.map { p -> Pair(p[0] / res.w.toFloat(), p[1] / res.h.toFloat()) }
                                        }
                                        val prevCorners = cornersNorm
                                        cornersNorm = if (prevCorners.size == nextCorners.size && nextCorners.size == 4) {
                                            val th = 0.005f // DEADBand ~0.5% del tamaño normalizado
                                            listOf(
                                                run {
                                                    val dx = nextCorners[0].first - prevCorners[0].first
                                                    val dy = nextCorners[0].second - prevCorners[0].second
                                                    val d = kotlin.math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
                                                    if (d <= th) prevCorners[0] else Pair(prevCorners[0].first * 0.7f + nextCorners[0].first * 0.3f, prevCorners[0].second * 0.7f + nextCorners[0].second * 0.3f)
                                                },
                                                run {
                                                    val dx = nextCorners[1].first - prevCorners[1].first
                                                    val dy = nextCorners[1].second - prevCorners[1].second
                                                    val d = kotlin.math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
                                                    if (d <= th) prevCorners[1] else Pair(prevCorners[1].first * 0.7f + nextCorners[1].first * 0.3f, prevCorners[1].second * 0.7f + nextCorners[1].second * 0.3f)
                                                },
                                                run {
                                                    val dx = nextCorners[2].first - prevCorners[2].first
                                                    val dy = nextCorners[2].second - prevCorners[2].second
                                                    val d = kotlin.math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
                                                    if (d <= th) prevCorners[2] else Pair(prevCorners[2].first * 0.7f + nextCorners[2].first * 0.3f, prevCorners[2].second * 0.7f + nextCorners[2].second * 0.3f)
                                                },
                                                run {
                                                    val dx = nextCorners[3].first - prevCorners[3].first
                                                    val dy = nextCorners[3].second - prevCorners[3].second
                                                    val d = kotlin.math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
                                                    if (d <= th) prevCorners[3] else Pair(prevCorners[3].first * 0.7f + nextCorners[3].first * 0.3f, prevCorners[3].second * 0.7f + nextCorners[3].second * 0.3f)
                                                }
                                            )
                                        } else nextCorners
                                        boxesNorm = if (ob.size == 4) {
                                            ob.map { b ->
                                                listOf(
                                                    b[0] / res.w.toFloat(),
                                                    b[1] / res.h.toFloat(),
                                                    b[2] / res.w.toFloat(),
                                                    b[3] / res.h.toFloat()
                                                )
                                            }
                                        } else if (boxesNorm.size == 4) {
                                            boxesNorm.toList()
                                        } else {
                                            ob.map { b ->
                                                listOf(
                                                    b[0] / res.w.toFloat(),
                                                    b[1] / res.h.toFloat(),
                                                    b[2] / res.w.toFloat(),
                                                    b[3] / res.h.toFloat()
                                                )
                                            }
                                        }
                                        circlesNorm = res.circles.map { c ->
                                            listOf(
                                                c[0] / res.w.toFloat(),
                                                c[1] / res.h.toFloat(),
                                                c[2] / res.w.toFloat()
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

            var hysteresis by remember { mutableStateOf(IntArray(4) { 0 }) }
            ScanningOverlay(
                primaryInstruction = "Alinear cuadrados en visores",
                secondaryInstruction = "Mantén la hoja estable para el escaneo",
                markers = markers,
                cornersNorm = cornersNorm,
                boxesNorm = boxesNorm,
                circlesNorm = circlesNorm,
                cornersCount = cornersCount,
                frameW = frameW,
                frameH = frameH,
                onRoiComputed = { r -> roiNorm = r.toList() },
                onMarkersComputed = { inside ->
                    // HISTÉRESIS: evita parpadeo cuando un marcador cae 1-2 frames
                    val adj = inside.copyOf()
                    for (i in 0 until 4) {
                        if (inside[i]) hysteresis[i] = 3 else if (hysteresis[i] > 0) { hysteresis[i]--; adj[i] = true }
                    }
                    // VALIDACIÓN DE CARTILLA: requiere 4 esquinas y geometría consistente
                    val hasFourCorners = cornersCount == 4 && cornersNorm.size == 4
                    val validSheet = if (hasFourCorners && frameW > 0 && frameH > 0) {
                        fun dist(a: Pair<Float, Float>, b: Pair<Float, Float>): Double {
                            val ax = a.first * frameW; val ay = a.second * frameH
                            val bx = b.first * frameW; val by = b.second * frameH
                            val dx = (ax - bx).toDouble(); val dy = (ay - by).toDouble()
                            return kotlin.math.hypot(dx, dy)
                        }
                        val tl = cornersNorm[0]; val tr = cornersNorm[1]; val br = cornersNorm[2]; val bl = cornersNorm[3]
                        val wAvg = ((dist(tl, tr) + dist(bl, br)) / 2.0)
                        val hAvg = ((dist(tl, bl) + dist(tr, br)) / 2.0)
                        val ratio = if (wAvg > 0.0) hAvg / wAvg else 0.0
                        val area = wAvg * hAvg
                        val minArea = (frameW.toDouble() * frameH.toDouble()) * 0.07
                        val ratioOk = ratio >= 0.95 && ratio <= 2.05 // hoja vertical más tolerante
                        val areaOk = area >= minArea
                        // ORTOGONALIDAD: ángulo cercano a 90° entre lados adyacentes
                        val vTopX = (tr.first - tl.first) * frameW; val vTopY = (tr.second - tl.second) * frameH
                        val vLeftX = (bl.first - tl.first) * frameW; val vLeftY = (bl.second - tl.second) * frameH
                        val dot = (vTopX * vLeftX + vTopY * vLeftY)
                        val nTop = kotlin.math.hypot(vTopX.toDouble(), vTopY.toDouble())
                        val nLeft = kotlin.math.hypot(vLeftX.toDouble(), vLeftY.toDouble())
                        val cosA = if (nTop > 0.0 && nLeft > 0.0) kotlin.math.abs(dot) / (nTop * nLeft) else 1.0
                        val orthoOk = cosA <= 0.15
                        // PARALELISMO: longitudes opuestas similares
                        val wDiff = kotlin.math.abs(dist(tl, tr) - dist(bl, br))
                        val hDiff = kotlin.math.abs(dist(tl, bl) - dist(tr, br))
                        val parallelOk = (wDiff <= wAvg * 0.08) && (hDiff <= hAvg * 0.08)
                        ratioOk && areaOk && orthoOk && parallelOk
                    } else false

                    val allInside = adj.all { it }
                    // ALINEACIÓN: cada esquina cerca del centro del recuadro correspondiente
                    val alignedOk = if (hasFourCorners && boxesNorm.size == 4 && frameW > 0 && frameH > 0) {
                        val s = kotlin.math.min(frameW, frameH).toDouble()
                        // UMBRAL DE ALINEACIÓN MÁS PERMISIVO para dibujar antes
                        val th = s * 0.016 // ~1.6% del tamaño
                        var worst = 0.0
                        for (i in 0 until 4) {
                            val b = boxesNorm[i]
                            val bcX = (b[0] + b[2] * 0.5f) * frameW
                            val bcY = (b[1] + b[3] * 0.5f) * frameH
                            val cX = cornersNorm[i].first * frameW
                            val cY = cornersNorm[i].second * frameH
                            val d = kotlin.math.hypot((bcX - cX).toDouble(), (bcY - cY).toDouble())
                            if (d > worst) worst = d
                        }
                        worst <= th
                    } else false
                    // DERIVA: movimiento mínimo entre frames consecutivos para asegurar estabilidad
                    val driftOk = if (prevCornersNorm.size == 4 && cornersNorm.size == 4 && frameW > 0 && frameH > 0) {
                        var maxDrift = 0.0
                        for (i in 0 until 4) {
                            val px = prevCornersNorm[i].first * frameW
                            val py = prevCornersNorm[i].second * frameH
                            val cx = cornersNorm[i].first * frameW
                            val cy = cornersNorm[i].second * frameH
                            val d = kotlin.math.hypot((cx - px).toDouble(), (cy - py).toDouble())
                            if (d > maxDrift) maxDrift = d
                        }
                        val s = kotlin.math.min(frameW, frameH).toDouble()
                        // DRIFT un poco más permisivo para mantener estabilidad visual
                        val driftTh = s * 0.008 // ~0.8% del tamaño
                        maxDrift <= driftTh
                    } else true
                    // ACTUALIZAR ESQUINAS PREVIAS TRAS CÁLCULO
                    prevCornersNorm = cornersNorm.toList()
                    // ROI LOCK: cuando está alineado y válido, congelar ROI alrededor de la hoja
                    if (hasFourCorners && validSheet && alignedOk && frameW > 0 && frameH > 0) {
                        val xs = floatArrayOf(cornersNorm[0].first, cornersNorm[1].first, cornersNorm[2].first, cornersNorm[3].first)
                        val ys = floatArrayOf(cornersNorm[0].second, cornersNorm[1].second, cornersNorm[2].second, cornersNorm[3].second)
                        val minX = xs.minOrNull() ?: 0f
                        val maxX = xs.maxOrNull() ?: 1f
                        val minY = ys.minOrNull() ?: 0f
                        val maxY = ys.maxOrNull() ?: 1f
                        val padX = 0.06f
                        val padY = 0.06f
                        val rx = (minX - padX).coerceAtLeast(0f)
                        val ry = (minY - padY).coerceAtLeast(0f)
                        val rw = (maxX - minX + 2f * padX).coerceAtMost(1f - rx)
                        val rh = (maxY - minY + 2f * padY).coerceAtMost(1f - ry)
                        val minW = 0.30f
                        val minH = 0.50f
                        val finalW = rw.coerceAtLeast(minW)
                        val finalH = rh.coerceAtLeast(minH)
                        roiNorm = listOf(rx, ry, finalW, finalH)
                        roiLocked = true
                    } else if (!allInside || !validSheet || !alignedOk) {
                        roiLocked = false
                        roiNorm = null
                    }
                    if (allInside && validSheet && alignedOk) {
                        if (driftOk) stableCount++ else stableCount = 0
                        // DISPARO RÁPIDO PERO ESTABLE: 5 frames
                        if (!capturing && stableCount >= 5) {
                            capturing = true
                            stableCount = 0
                            captureAndProcessLocal(imageCapture, context, navController, cornersNorm, sheetType) {
                                capturing = false
                                roiLocked = false
                                roiNorm = null
                            }
                        }
                    } else {
                        stableCount = 0
                    }
                }
            )

            // Botón de captura manual
            // Selector de modo de cartilla (20/50)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 110.dp)
                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val active = MaterialTheme.colorScheme.primary
                val inactive = Color.White
                TextButton(onClick = { sheetType = CornerDetector.SheetType.S20 }) {
                    Text(text = "20", color = if (sheetType == CornerDetector.SheetType.S20) active else inactive)
                }
                TextButton(onClick = { sheetType = CornerDetector.SheetType.S50 }) {
                    Text(text = "50", color = if (sheetType == CornerDetector.SheetType.S50) active else inactive)
                }
            }

            IconButton(
                onClick = {
                    captureAndProcessLocal(imageCapture, context, navController, cornersNorm, sheetType) {}
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
    circlesNorm: List<List<Float>> = emptyList(),
    cornersCount: Int = 0,
    frameW: Int = 0,
    frameH: Int = 0,
    onRoiComputed: (FloatArray) -> Unit = {},
    onMarkersComputed: (BooleanArray) -> Unit = {}
) {
    val cornerSize = 90.dp
    val cornerPadding = 0.dp
    val bottomOffset = 280.dp
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
                val pad = 12f
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
                val pad = 10f
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
            circlesNorm.forEach { c ->
                val cx = offsetX + c[0] * scaledW
                val cy = offsetY + c[1] * scaledH
                val r = c[2] * scaledW
                drawCircle(
                    color = Color(0xFF00B8D4),
                    radius = r,
                    center = androidx.compose.ui.geometry.Offset(cx, cy),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                )
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

private fun captureAndProcessLocal(
    imageCapture: ImageCapture,
    context: android.content.Context,
    navController: NavController,
    lastCornersNorm: List<Pair<Float, Float>>,
    type: CornerDetector.SheetType,
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
                val raw = BitmapFactory.decodeFile(output.absolutePath)
                val bmp = if (raw != null) applyExifOrientation(raw, output.absolutePath) else null
                var useCorners: List<FloatArray>? = null
                if (bmp != null) {
                    // DETECTAR SIEMPRE EN LA IMAGEN GUARDADA (EVITA DESALINEOS ENTRE FRAME Y CAPTURA)
                    val (corners, _) = CornerDetector.detectarEsquinasYCuadrados(bmp, type)
                    useCorners = if (corners.list.size == 4) corners.list else null
                    // SI FALLA LA DETECCIÓN, USAR ÚLTIMAS ESQUINAS NORMALIZADAS COMO PISTA
                    if (useCorners == null && lastCornersNorm.size == 4) {
                        val wBmp = bmp.width.toFloat(); val hBmp = bmp.height.toFloat()
                        useCorners = listOf(
                            floatArrayOf(lastCornersNorm[0].first * wBmp, lastCornersNorm[0].second * hBmp),
                            floatArrayOf(lastCornersNorm[1].first * wBmp, lastCornersNorm[1].second * hBmp),
                            floatArrayOf(lastCornersNorm[2].first * wBmp, lastCornersNorm[2].second * hBmp),
                            floatArrayOf(lastCornersNorm[3].first * wBmp, lastCornersNorm[3].second * hBmp)
                        )
                    }
                    val outFile = if (useCorners != null && useCorners.size == 4) {
                        val cropped = CornerDetector.recortarPorEsquinasAutoSize(bmp, useCorners!!, maxDim = 2200)
                        val outPng = File(output.parentFile, output.nameWithoutExtension + "_crop.png")
                        FileOutputStream(outPng).use { cropped.compress(Bitmap.CompressFormat.PNG, 100, it) }
                        outPng
                    } else null
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

private fun applyExifOrientation(src: Bitmap, path: String): Bitmap {
    val exif = ExifInterface(path)
    val ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    val m = Matrix()
    when (ori) {
        ExifInterface.ORIENTATION_ROTATE_90 -> m.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> m.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> m.postRotate(270f)
        else -> {}
    }
    return Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, true)
}

@Preview(showBackground = true)
@Composable
private fun ScanningOverlayPreview() {
    val corners = listOf(
        Pair(0.08f, 0.06f), // TL
        Pair(0.92f, 0.06f), // TR
        Pair(0.92f, 0.75f), // BR (sobre el área visible superior de la UI)
        Pair(0.08f, 0.75f)  // BL
    )
    val boxes = listOf(
        listOf(0.06f, 0.03f, 0.10f, 0.10f), // TL
        listOf(0.84f, 0.03f, 0.10f, 0.10f), // TR
        listOf(0.84f, 0.72f, 0.10f, 0.10f), // BR
        listOf(0.06f, 0.72f, 0.10f, 0.10f)  // BL
    )
    ScanningOverlay(
        primaryInstruction = "Alinear cuadrados en visores",
        secondaryInstruction = "Mantén la hoja estable",
        markers = listOf(true, true, true, true),
        cornersNorm = corners,
        boxesNorm = boxes,
        cornersCount = 4,
        frameW = 1080,
        frameH = 1920,
        onRoiComputed = {},
        onMarkersComputed = {}
    )
}

// REMOTO ELIMINADO: detectCornersRemote

// REMOTO ELIMINADO: cropRemote

private fun yuv420ToJpeg(image: ImageProxy, quality: Int = 70): ByteArray {
    val w = image.width
    val h = image.height
    val yPlane = image.planes[0]
    val uPlane = image.planes[1]
    val vPlane = image.planes[2]

    val yRow = yPlane.rowStride
    val yPix = yPlane.pixelStride
    val uRow = uPlane.rowStride
    val uPix = uPlane.pixelStride
    val vRow = vPlane.rowStride
    val vPix = vPlane.pixelStride

    val yBuf = yPlane.buffer
    val uBuf = uPlane.buffer
    val vBuf = vPlane.buffer
    yBuf.rewind(); uBuf.rewind(); vBuf.rewind()

    val nv21 = ByteArray(w * h + (w * h) / 2)
    var offset = 0

    var r = 0
    while (r < h) {
        var c = 0
        val yBase = r * yRow
        while (c < w) {
            nv21[offset++] = yBuf.get(yBase + c * yPix)
            c++
        }
        r++
    }

    r = 0
    while (r < h / 2) {
        var c = 0
        val uBase = r * uRow
        val vBase = r * vRow
        while (c < w / 2) {
            nv21[offset++] = vBuf.get(vBase + c * vPix)
            nv21[offset++] = uBuf.get(uBase + c * uPix)
            c++
        }
        r++
    }

    val yuv = YuvImage(nv21, ImageFormat.NV21, w, h, null)
    val out = java.io.ByteArrayOutputStream()
    yuv.compressToJpeg(android.graphics.Rect(0, 0, w, h), quality, out)
    val bytes = out.toByteArray()

    val rotation = image.imageInfo.rotationDegrees
    if (rotation == 0) return bytes

    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    val matrix = Matrix()
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
    val circles: List<List<Float>>,
    val count: Int,
    val w: Int,
    val h: Int
)

private fun detectMarkersLocal(image: ImageProxy, roiNorm: List<Float>?, type: CornerDetector.SheetType): DetectionResult? {
    val jpeg = kotlin.runCatching { yuv420ToJpeg(image, 70) }.getOrNull() ?: return null
    val bmp = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size) ?: return null
    val W = bmp.width
    val H = bmp.height
    var work = bmp
    var offsetX = 0
    var offsetY = 0
    if (roiNorm != null && roiNorm.size == 4) {
        val x = (roiNorm[0] * W).toInt()
        val y = (roiNorm[1] * H).toInt()
        val w = (roiNorm[2] * W).toInt()
        val h = (roiNorm[3] * H).toInt()
        offsetX = x; offsetY = y
        work = Bitmap.createBitmap(bmp, x.coerceAtLeast(0), y.coerceAtLeast(0), w.coerceAtLeast(1).coerceAtMost(W - x), h.coerceAtLeast(1).coerceAtMost(H - y))
    }
    val det = kotlin.runCatching { CornerDetector.detectarEsquinasYCuadrados(work, type) }.getOrNull() ?: return null
    val cornersRes = det.first
    val boxesRes = det.second
    val corners = cornersRes.list.map { listOf(it[0] + offsetX, it[1] + offsetY) }
    val boxes = boxesRes.list.map { listOf((it[0] + offsetX).toFloat(), (it[1] + offsetY).toFloat(), it[2].toFloat(), it[3].toFloat()) }
    // CÍRCULOS NO EN TIEMPO REAL: se detectan tras la captura/recorte
    val circles = emptyList<List<Float>>()
    val s = minOf(W, H)
    val size = max(24, (s * 0.06).toInt())
    val pad = max(32, (s * 0.08).toInt())
    val rects = listOf(
        intArrayOf(pad, pad, size, size),
        intArrayOf(W - pad - size, pad, size, size),
        intArrayOf(W - pad - size, H - pad - size, size, size),
        intArrayOf(pad, H - pad - size, size, size)
    )
    val cornersFull = ArrayList<List<Float>>(4)
    val n = corners.size.coerceIn(0, 4)
    for (i in 0 until n) cornersFull.add(corners[i])
    while (cornersFull.size < 4) cornersFull.add(listOf(0f, 0f))
    val mk = BooleanArray(4)
    for (i in 0 until 4) {
        val cx = cornersFull[i][0].toInt()
        val cy = cornersFull[i][1].toInt()
        val r = rects[i]
        mk[i] = (cx >= r[0] && cx <= r[0] + r[2] && cy >= r[1] && cy <= r[1] + r[3])
    }
    val count = cornersRes.list.size
    return DetectionResult(mk, corners.map { it.map { v -> v.toFloat() } }, boxes, circles, count, W, H)
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