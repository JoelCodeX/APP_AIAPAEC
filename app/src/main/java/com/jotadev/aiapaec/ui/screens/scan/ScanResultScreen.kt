package com.jotadev.aiapaec.ui.screens.scan

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jotadev.aiapaec.data.api.NetworkConfig
import com.jotadev.aiapaec.data.storage.TokenStorage
import com.jotadev.aiapaec.navigation.NavigationRoutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

data class AnswerItem(val q: Int, val r: Int, val letra: String, val correcta: String, val estado: String, val puntaje: Double)

@Composable
fun ScanResultScreen(navController: NavController, runId: String, overlayUrl: String, tipoInit: Int, quizId: Int, studentId: Int) {
    var overlay by remember { mutableStateOf<Bitmap?>(null) }
    var answers by remember { mutableStateOf(listOf<AnswerItem>()) }
    var tipoSel by remember { mutableStateOf(tipoInit) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Respuestas, 1: Imagen escaneada
    var studentName by remember { mutableStateOf(navController.previousBackStackEntry?.savedStateHandle?.get<String>("student_name") ?: "Estudiante") }
    val client = remember { OkHttpClient.Builder().build() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isSaving by remember { mutableStateOf(false) }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val saveRequest by (savedStateHandle?.getStateFlow("scan_save_request", false) ?: MutableStateFlow(false)).collectAsState()

    LaunchedEffect(saveRequest) {
        if (saveRequest) {
            if (!isSaving) {
                isSaving = true
                Toast.makeText(context, "Guardando...", Toast.LENGTH_SHORT).show()
                val result = saveScan(client, runId, quizId, studentId)
                if (result.first) {
                    Toast.makeText(context, "Guardado exitosamente", Toast.LENGTH_SHORT).show()
                    navController.popBackStack(NavigationRoutes.applyExam(quizId.toString()), inclusive = false)
                } else {
                    Toast.makeText(context, "Error: ${result.second}", Toast.LENGTH_LONG).show()
                    isSaving = false
                    savedStateHandle?.set("scan_save_request", false)
                }
            }
        }
    }

    LaunchedEffect(runId, overlayUrl) {
        withContext(Dispatchers.IO) {
            val ansUrl = "${NetworkConfig.baseRoot}/api/scan/results/answers/$runId"
            // Forzar URL correcta usando NetworkConfig para evitar problemas si el backend devuelve una URL antigua o incorrecta
            val finalOverlayUrl = "${NetworkConfig.baseRoot}/api/scan/results/overlay/$runId"
            
            client.newCall(Request.Builder().url(ansUrl).get().build()).execute().use { resp ->
                val body = resp.body?.string() ?: "{}"
                val json = JSONObject(body)
                
                // Actualizar nombre del estudiante si viene en el JSON
                if (json.has("student_name")) {
                    studentName = json.getString("student_name")
                }
                
                val arr: JSONArray = json.optJSONArray("answers") ?: JSONArray()
                val tmp = mutableListOf<AnswerItem>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    tmp.add(AnswerItem(
                        o.optInt("q"), 
                        o.optInt("r"), 
                        o.optString("letra"),
                        o.optString("correcta"), // Leer respuesta correcta
                        o.optString("estado", "P"), // C, X, P
                        o.optDouble("puntaje", 0.0)
                    ))
                }
                answers = tmp
            }
            client.newCall(Request.Builder().url(finalOverlayUrl).get().build()).execute().use { resp ->
                val bytes = resp.body?.bytes()
                if (bytes != null) overlay = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            ResultHeaderCard(
            studentName = studentName,
            tipoSel = tipoSel,
            answers = answers
            )
        }

        // Eliminado: botones de Cartilla 20/50

        ResultTabs(selectedTab = selectedTab, onSelect = { selectedTab = it })

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> AnswersTable(answers = answers, tipoSel = tipoSel)
                1 -> {
                    if (overlay != null) {
                        Image(
                            bitmap = overlay!!.asImageBitmap(),
                            contentDescription = "Imagen escaneada",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Sin imagen disponible")
                        }
                    }
                }
            }
            if (isSaving) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun ResultTabs(selectedTab: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TabItem(label = "Respuestas", selected = selectedTab == 0, onClick = { onSelect(0) }, modifier = Modifier.weight(1f))
        TabItem(label = "Imagen escaneada", selected = selectedTab == 1, onClick = { onSelect(1) }, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TabItem(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.size(4.dp))
        Divider(
            thickness = 4.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        )
    }
}

@Composable
private fun ResultHeaderCard(studentName: String, tipoSel: Int, answers: List<AnswerItem>) {
    val total = answers.size.takeIf { it > 0 } ?: 1
    val correct = answers.count { it.r > 0 }
    val percent = (correct.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.Person, contentDescription = "Estudiante", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = studentName, style = MaterialTheme.typography.bodyLarge)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.PushPin, contentDescription = "Escala", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = "Calificar en escala de ${if (tipoSel == 20) 20 else 50}", style = MaterialTheme.typography.bodyMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val maxPts = if (tipoSel == 20) 20 else 50
                    val pts = (percent * maxPts).roundToInt()
                    Text(text = "$pts / $maxPts p.", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                }
            }
            DonutPercentage(percent = percent, modifier = Modifier.size(72.dp))
        }
    }
}

@Composable
private fun DonutPercentage(percent: Float, modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = primaryColor.copy(alpha = 0.2f)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 10.dp.toPx()
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = percent * 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Text(text = "${(percent * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun AnswersTable(answers: List<AnswerItem>, tipoSel: Int) {
    val headerBg = MaterialTheme.colorScheme.secondary
    val headerTextColor = MaterialTheme.colorScheme.onSecondary
    Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBg)
                    .padding(vertical = 10.dp, horizontal = 8.dp)
            ) {
                Text(text = "N°", modifier = Modifier.weight(0.8f), fontWeight = FontWeight.SemiBold, color = headerTextColor, textAlign = TextAlign.Center)
                Text(text = "Correcta", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.SemiBold, color = headerTextColor, textAlign = TextAlign.Center)
                Text(text = "Marcada", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.SemiBold, color = headerTextColor, textAlign = TextAlign.Center)
                Text(text = "Puntaje", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, color = headerTextColor, textAlign = TextAlign.Center)
                Text(text = "Estado", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, color = headerTextColor, textAlign = TextAlign.Center)
            }
            Divider(color = MaterialTheme.colorScheme.outline)
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(answers) { a ->
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = a.q.toString(), modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                        Text(text = a.correcta.ifEmpty { "-" }, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center) // Mostrar correcta
                        Text(text = a.letra, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
                        Text(text = String.format("%.2f", a.puntaje), modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            when (a.estado) {
                                "C" -> Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = "Correcta", tint = Color(0xFF2E7D32))
                                "X" -> Icon(imageVector = Icons.Filled.Cancel, contentDescription = "Incorrecta", tint = Color(0xFFC62828))
                                else -> Icon(imageVector = Icons.Filled.Warning, contentDescription = "No marcada", tint = Color(0xFFFF8F00))
                            }
                        }
                    }
                    Divider(color = MaterialTheme.colorScheme.outline)
                }
                item { Divider(color = MaterialTheme.colorScheme.outline) }
            }
    }
}

private suspend fun reprocess(client: OkHttpClient, runId: String, tipo: Int): Pair<Bitmap?, List<AnswerItem>> {
    return withContext(Dispatchers.IO) {
        val endpoint = "${NetworkConfig.baseRoot}/api/scan/process-omr"
        val payload = JSONObject().put("run_id", runId).put("num_preguntas", tipo).toString()
        val req = Request.Builder().url(endpoint).post(payload.toRequestBody("application/json".toMediaType())).build()
        client.newCall(req).execute().use { resp ->
            val body = resp.body?.string() ?: "{}"
            val json = JSONObject(body)
            val overlayUrl = json.optString("overlay_url", "")
            val arr: JSONArray = json.optJSONArray("answers") ?: JSONArray()
            val tmp = mutableListOf<AnswerItem>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                tmp.add(AnswerItem(
                    o.optInt("q"), 
                    o.optInt("r"), 
                    o.optString("letra"),
                    o.optString("correcta"),
                    o.optString("estado", "P"),
                    o.optDouble("puntaje", 0.0)
                ))
            }
            var bmp: Bitmap? = null
            if (overlayUrl.isNotEmpty()) {
                client.newCall(Request.Builder().url(overlayUrl).get().build()).execute().use { r2 ->
                    val bytes = r2.body?.bytes()
                    if (bytes != null) bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }
            }
            bmp to tmp
        }
    }
}

private suspend fun saveScan(client: OkHttpClient, runId: String, quizId: Int, studentId: Int): Pair<Boolean, String> {
    return withContext(Dispatchers.IO) {
        try {
            val url = "${NetworkConfig.baseRoot}/api/scan/save-scan"
            val json = JSONObject().apply {
                put("run_id", runId)
                put("quiz_id", quizId)
                put("student_id", studentId)
            }
            val body = json.toString().toRequestBody("application/json".toMediaType())
            val requestBuilder = Request.Builder().url(url).post(body)
            val token = TokenStorage.getToken()
            if (!token.isNullOrBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
            client.newCall(requestBuilder.build()).execute().use { response ->
                if (response.isSuccessful) {
                    true to "OK"
                } else {
                    val msg = response.body?.string() ?: "Error desconocido"
                    false to "Error ${response.code}: $msg"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false to "Excepción: ${e.message}"
        }
    }
}
