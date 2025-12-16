package com.jotadev.aiapaec.ui.screens.scan

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import android.view.Surface
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.navigation.NavController
import com.jotadev.aiapaec.data.api.NetworkConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt
import androidx.camera.core.Preview as CameraPreview
import com.jotadev.aiapaec.data.storage.TokenStorage

// PORCENTAJE DE RECORTE INFERIOR SOBRE LA ROI (0.0 a 0.5)
private const val ROI_BOTTOM_TRIM: Float = 0.20f

// HOST BACKEND EN DISPOSITIVO FÍSICO (AJUSTA SI CAMBIA LA IP)
//

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanUploadScreen(navController: NavController, examId: String, studentId: Int, numQuestions: Int) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        val context = androidx.compose.ui.platform.LocalContext.current
        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
        var hasPermission by remember { mutableStateOf(false) }
        var previewView by remember { mutableStateOf<PreviewView?>(null) }
        var statusText by remember { mutableStateOf("") }
        var roi by remember { mutableStateOf<FloatArray?>(null) }

        val permissionLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                hasPermission = granted
            }

        val imageCapture = remember {
            ImageCapture.Builder()
                .setTargetRotation(Surface.ROTATION_0)
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setJpegQuality(85)
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
                    val preview = CameraPreview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .build()
                    preview.setSurfaceProvider(previewView!!.surfaceProvider)
                    val selector = CameraSelector.DEFAULT_BACK_CAMERA
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            selector,
                            preview,
                            imageCapture
                        )
                    } catch (_: Exception) {
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        previewView = this
                    }
                }
            )

            // OVERLAY OSCURO CON VISORES DE ESQUINA IGUAL QUE ScanScreen
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

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val path = android.graphics.Path().apply {
                        fillType = android.graphics.Path.FillType.EVEN_ODD
                        addRect(0f, 0f, boxWpx, boxHpx, android.graphics.Path.Direction.CW)
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
                    drawPath(
                        path = path.asComposePath(),
                        color = Color.Black.copy(alpha = 0.4f),
                        blendMode = BlendMode.SrcOver
                    )
                }

                // ROI NORMALIZADA BASADA EN LOS VISORES DE ESQUINA
                val minLeft = listOf(
                    transparentAreas[0].left,
                    transparentAreas[1].left,
                    transparentAreas[2].left,
                    transparentAreas[3].left
                ).minOrNull() ?: 0f
                val minTop = listOf(
                    transparentAreas[0].top,
                    transparentAreas[1].top,
                    transparentAreas[2].top,
                    transparentAreas[3].top
                ).minOrNull() ?: 0f
                val maxRight = listOf(
                    transparentAreas[0].right,
                    transparentAreas[1].right,
                    transparentAreas[2].right,
                    transparentAreas[3].right
                ).maxOrNull() ?: boxWpx
                val maxBottom = listOf(
                    transparentAreas[0].bottom,
                    transparentAreas[1].bottom,
                    transparentAreas[2].bottom,
                    transparentAreas[3].bottom
                ).maxOrNull() ?: boxHpx
                val rx = (minLeft / boxWpx).coerceIn(0f, 1f)
                val ry = (minTop / boxHpx).coerceIn(0f, 1f)
                val rw = ((maxRight - minLeft) / boxWpx).coerceIn(0f, 1f)
                val rh = ((maxBottom - minTop) / boxHpx).coerceIn(0f, 1f)
                val pad = 0.10f
                val rxPad = (rx - pad).coerceIn(0f, 1f)
                val ryPad = (ry - pad).coerceIn(0f, 1f)
                val rwPad = (rw + pad * 2f).coerceIn(0f, 1f)
                val rhPad = (rh + pad * 2f).coerceIn(0f, 1f)
                roi = floatArrayOf(rxPad, ryPad, rwPad, rhPad)
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CaptureInstructionsCard(
                    modifier = Modifier.fillMaxWidth()
                )

                FloatingActionButton(
                    onClick = {
                        if (!hasPermission) return@FloatingActionButton
                        statusText = "Capturando..."
                        val out = createTempFile()
                        val outputOptions = ImageCapture.OutputFileOptions.Builder(out).build()
                        imageCapture.takePicture(
                            outputOptions,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onError(exception: ImageCaptureException) {
                                    statusText = "Error de captura"
                                }

                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                    statusText = "Enviando..."
                                    val sendFile = centerCropForPreview(out, previewView!!, roi)
                                    Thread {
                                        val result = uploadAndProcess(sendFile, roi, examId.toIntOrNull(), studentId, numQuestions)
                                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                                            if (result != null) {
                                                statusText = "Procesado"
                                                val runId = result.first
                                                val overlayUrl = result.second
                                                val tipo = result.third
                                                val encOverlay = android.net.Uri.encode(overlayUrl)
                                                navController.navigate(
                                                    com.jotadev.aiapaec.navigation.NavigationRoutes.scanResult(
                                                        runId,
                                                        encOverlay,
                                                        tipo,
                                                        examId.toIntOrNull() ?: 0,
                                                        studentId
                                                    )
                                                )
                                            } else {
                                                statusText = "Fallo al procesar"
                                            }
                                        }
                                    }.start()
                                }
                            })
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = "Capturar"
                    )
                }
            }

            if (statusText.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(8.dp)
                        .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(text = statusText, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
private fun CaptureInstructionsCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Recomendaciones para mejorar la captura
            Text(
                text = "✅ Iluminación: Evita sombras, usa luz uniforme.",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "✅ Perspectiva: Mantén la cámara paralela al documento.",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "✅ Enfoque: Asegúrate de que el texto esté nítido.",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "✅ Encuadre: Coloca el documento dentro de los visores.",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun createTempFile(): File {
    val dir = File("/sdcard/Pictures")
    if (!dir.exists()) dir.mkdirs()
    return File(dir, "upload_${System.currentTimeMillis()}.jpg")
}

private fun centerCropForPreview(file: File, previewView: PreviewView, roi: FloatArray?): File {
    val vw = previewView.width
    val vh = previewView.height
    val bmp = decodeBitmapWithExif(file, vw, vh)
    val imgW = bmp.width
    val imgH = bmp.height
    val scale = maxOf(vw.toFloat() / imgW.toFloat(), vh.toFloat() / imgH.toFloat())
    val cropW = (vw / scale).roundToInt().coerceAtMost(imgW)
    val cropH = (vh / scale).roundToInt().coerceAtMost(imgH)
    val left = ((imgW - cropW) / 2f).roundToInt()
    val top = ((imgH - cropH) / 2f).roundToInt()
    val cropped = Bitmap.createBitmap(bmp, left, top, cropW, cropH)

    var finalBmp = cropped
    if (roi != null && roi.size == 4) {
        val rx = (roi[0] * cropW).roundToInt().coerceIn(0, cropW - 1)
        val ry = (roi[1] * cropH).roundToInt().coerceIn(0, cropH - 1)
        val rw = (roi[2] * cropW).roundToInt().coerceIn(1, cropW - rx)
        val rh = (roi[3] * cropH).roundToInt().coerceIn(1, cropH - ry)
        val trimPx = (ROI_BOTTOM_TRIM * rh).roundToInt().coerceIn(0, rh - 1)
        val rhAdj = (rh - trimPx).coerceAtLeast(1)
        finalBmp = Bitmap.createBitmap(cropped, rx, ry, rw, rhAdj)
    }

    val out = File(
        file.parentFile ?: File("/sdcard/Pictures"),
        "upload_cropped_${System.currentTimeMillis()}.jpg"
    )
    FileOutputStream(out).use { fos ->
        finalBmp.compress(Bitmap.CompressFormat.JPEG, 85, fos)
    }
    return out
}

private fun decodeBitmapWithExif(file: File, reqW: Int, reqH: Int): Bitmap {
    val opts = BitmapFactory.Options()
    opts.inJustDecodeBounds = true
    BitmapFactory.decodeFile(file.absolutePath, opts)
    var inSample = 1
    val (w, h) = opts.outWidth to opts.outHeight
    if (reqW > 0 && reqH > 0) {
        var halfW = w / 2
        var halfH = h / 2
        while (halfW / inSample >= reqW && halfH / inSample >= reqH) {
            inSample *= 2
        }
    }
    val opts2 = BitmapFactory.Options()
    opts2.inSampleSize = inSample
    val src = BitmapFactory.decodeFile(file.absolutePath, opts2)
    val exif = ExifInterface(file.absolutePath)
    val orientation =
        exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.postRotate(90f); matrix.postScale(1f, -1f)
        }

        ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.postRotate(270f); matrix.postScale(1f, -1f)
        }

        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
        else -> {}
    }
    return if (!matrix.isIdentity) Bitmap.createBitmap(
        src,
        0,
        0,
        src.width,
        src.height,
        matrix,
        true
    ) else src
}

private fun uploadForCorners(file: File, roi: FloatArray?): Boolean {
    val url = "${NetworkConfig.baseRoot}/api/scan/upload"
    val client = OkHttpClient.Builder().build()
    val media = "image/jpeg".toMediaType()
    val body = file.asRequestBody(media)
    val part = MultipartBody.Part.createFormData("file", file.name, body)
    val builder = MultipartBody.Builder().setType(MultipartBody.FORM).addPart(part)
    if (roi != null && roi.size == 4) {
        val roiJson = "[" + roi.joinToString(",") { it.toString() } + "]"
        builder.addFormDataPart("roi", roiJson)
    }
    val form = builder.build()
    return try {
        val token = TokenStorage.getToken()
        val reqBuilder = Request.Builder().url(url).post(form)
        if (!token.isNullOrBlank()) {
            reqBuilder.addHeader("Authorization", "Bearer $token")
        }
        val req = reqBuilder.build()
        client.newCall(req).execute().use { resp ->
            val code = resp.code
            val msg = resp.message
            val txt = resp.body?.string()
            Log.d("ScanUpload", "HTTP $code $msg; body=${txt ?: "<null>"}")
            if (code != 200 || txt == null) return@use false
            val json = JSONObject(txt)
            json.optBoolean("ok", false)
        }
    } catch (e: Exception) {
        Log.e("ScanUpload", "Upload error", e)
        false
    }
}

private fun uploadAndProcess(file: File, roi: FloatArray?, quizId: Int? = null, studentId: Int? = null, numPreguntas: Int? = null): Triple<String, String, Int>? {
    val client = OkHttpClient.Builder().build()
    val media = "image/jpeg".toMediaType()
    val body = file.asRequestBody(media)
    val part = MultipartBody.Part.createFormData("file", file.name, body)
    val builder = MultipartBody.Builder().setType(MultipartBody.FORM).addPart(part)
    if (roi != null && roi.size == 4) {
        val roiJson = "[" + roi.joinToString(",") { it.toString() } + "]"
        builder.addFormDataPart("roi", roiJson)
    }
    val token = TokenStorage.getToken()
    val uploadBuilder = Request.Builder().url("${NetworkConfig.baseRoot}/api/scan/upload").post(builder.build())
    if (!token.isNullOrBlank()) {
        uploadBuilder.addHeader("Authorization", "Bearer $token")
    }
    val uploadReq = uploadBuilder.build()
    client.newCall(uploadReq).execute().use { up ->
        val txt = up.body?.string() ?: return null
        val j = runCatching { JSONObject(txt) }.getOrNull() ?: return null
        val runId = j.optString("run_id", "")
        if (runId.isEmpty()) return null
        val payloadObj = JSONObject().put("run_id", runId)
        if (quizId != null) payloadObj.put("quiz_id", quizId)
        if (studentId != null) payloadObj.put("student_id", studentId)
        if (numPreguntas != null) payloadObj.put("num_preguntas", numPreguntas)
        payloadObj.put("preview", true)
        val payload = payloadObj.toString()
        val procBuilder = Request.Builder().url("${NetworkConfig.baseRoot}/api/scan/process-omr")
            .post(payload.toRequestBody("application/json".toMediaType()))
        if (!token.isNullOrBlank()) {
            procBuilder.addHeader("Authorization", "Bearer $token")
        }
        val procReq = procBuilder.build()
        client.newCall(procReq).execute().use { pr ->
            val ptxt = pr.body?.string() ?: return null
            val pj = runCatching { JSONObject(ptxt) }.getOrNull() ?: return null
            val overlay = pj.optString("overlay_url", "")
            val tipo = pj.optInt("tipo", 0)
            if (overlay.isEmpty()) return null
            return Triple(runId, overlay, tipo)
        }
    }
}
