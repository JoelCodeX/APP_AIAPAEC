package com.jotadev.aiapaec.ui.screens.scan

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import okhttp3.OkHttpClient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropPreviewScreen(navController: NavController, filePath: String) {
    val overlayPath = remember { mutableStateOf<String?>(null) }
    val circlesNorm = remember { mutableStateOf<List<List<Float>>>(emptyList()) }
    val client = remember { OkHttpClient() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(text = "Previsualización", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
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
    ) { inner ->
    LaunchedEffect(filePath) {
        overlayPath.value = filePath
        val bmp = BitmapFactory.decodeFile(filePath)
        if (bmp != null) {
            val circles = CornerDetector.detectarCirculosS20(bmp)
            val w = bmp.width.toFloat(); val h = bmp.height.toFloat()
            circlesNorm.value = circles.list.map { c ->
                listOf(c[0] / w, c[1] / h, c[2] / w)
            }
        }
    }

        val bmp = BitmapFactory.decodeFile(overlayPath.value ?: filePath)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            if (bmp != null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Imagen recortada",
                        modifier = Modifier.fillMaxSize()
                    )
                    GridOverlay()
                    CirclesOverlay(circlesNorm = circlesNorm.value)
                }
            } else {
                Text(
                    text = "No se pudo cargar la imagen",
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun GridOverlay(lines: Int = 8, color: Color = Color.White.copy(alpha = 0.35f)) {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val stepX = w / lines
        val stepY = h / lines
        for (i in 1 until lines) {
            // VERTICAL
            drawLine(
                color = color,
                start = androidx.compose.ui.geometry.Offset(stepX * i, 0f),
                end = androidx.compose.ui.geometry.Offset(stepX * i, h),
                strokeWidth = 1.5f
            )
            // HORIZONTAL
            drawLine(
                color = color,
                start = androidx.compose.ui.geometry.Offset(0f, stepY * i),
                end = androidx.compose.ui.geometry.Offset(w, stepY * i),
                strokeWidth = 1.5f
            )
        }
        // MARCO
        drawRect(
            color = color.copy(alpha = 0.6f),
            style = Stroke(width = 2.2f),
            topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
            size = androidx.compose.ui.geometry.Size(w, h)
        )
    }
}

@Composable
private fun CirclesOverlay(circlesNorm: List<List<Float>>, color: Color = Color(0xFF00B8D4)) {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        circlesNorm.forEach { c ->
            val cx = c[0] * w
            val cy = c[1] * h
            val r = c[2] * w
            drawCircle(
                color = color,
                radius = r,
                center = androidx.compose.ui.geometry.Offset(cx, cy),
                style = Stroke(width = 2f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CropPreviewGridPreview() {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF101010))) {
        GridOverlay()
    }
}
