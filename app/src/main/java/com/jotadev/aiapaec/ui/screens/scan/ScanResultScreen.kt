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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
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

data class AnswerItem(
    val q: Int,
    val r: Int,
    val letra: String,
    val correcta: String,
    val estado: String,
    val puntaje: Double
)

@Composable
fun ScanResultScreen(
    navController: NavController,
    runId: String,
    overlayUrl: String,
    tipoInit: Int,
    quizId: Int,
    studentId: Int
) {
    var overlay by remember { mutableStateOf<Bitmap?>(null) }
    var answers by remember { mutableStateOf(listOf<AnswerItem>()) }
    var tipoSel by remember { mutableStateOf(tipoInit) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Respuestas, 1: Imagen escaneada
    var studentName by remember {
        mutableStateOf(
            navController.previousBackStackEntry?.savedStateHandle?.get<String>(
                "student_name"
            ) ?: "Estudiante"
        )
    }
    val client = remember { OkHttpClient.Builder().build() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isSaving by remember { mutableStateOf(false) }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val saveRequest by (savedStateHandle?.getStateFlow("scan_save_request", false)
        ?: MutableStateFlow(false)).collectAsState()

    LaunchedEffect(saveRequest) {
        if (saveRequest) {
            if (!isSaving) {
                isSaving = true
                Toast.makeText(context, "Guardando...", Toast.LENGTH_SHORT).show()
                val result = saveScan(client, runId, quizId, studentId)
                if (result.first) {
                    Toast.makeText(context, "Guardado exitosamente", Toast.LENGTH_SHORT).show()
                    navController.popBackStack(
                        NavigationRoutes.applyExam(quizId.toString()),
                        inclusive = false
                    )
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
            try {
                val ansUrl = "${NetworkConfig.baseRoot}/api/scan/results/answers/$runId"
                // Forzar URL correcta usando NetworkConfig para evitar problemas si el backend devuelve una URL antigua o incorrecta
                val finalOverlayUrl = "${NetworkConfig.baseRoot}/api/scan/results/overlay/$runId"

                try {
                    client.newCall(Request.Builder().url(ansUrl).get().build()).execute().use { resp ->
                        if (resp.isSuccessful) {
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
                                tmp.add(
                                    AnswerItem(
                                        o.optInt("q"),
                                        o.optInt("r"),
                                        o.optString("letra"),
                                        o.optString("correcta"), // Leer respuesta correcta
                                        o.optString("estado", "P"), // C, X, P
                                        o.optDouble("puntaje", 0.0)
                                    )
                                )
                            }
                            answers = tmp
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                try {
                    client.newCall(Request.Builder().url(finalOverlayUrl).get().build()).execute()
                        .use { resp ->
                            if (resp.isSuccessful) {
                                val bytes = resp.body?.bytes()
                                if (bytes != null) overlay = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            }
                        }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
        TabItem(
            label = "Respuestas",
            selected = selectedTab == 0,
            onClick = { onSelect(0) },
            modifier = Modifier.weight(1f)
        )
        TabItem(
            label = "Imagen escaneada",
            selected = selectedTab == 1,
            onClick = { onSelect(1) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TabItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
    
    // Lógica de Puntos: Base 100
    // Correcta: 100 / total
    // Incorrecta: -1
    // Blanco: +1
    val ptsPerCorrect = 100.0 / total
    
    // Contadores
    val correctCount = answers.count { it.estado == "C" }
    val incorrectCount = answers.count { it.estado == "X" }
    val blankCount = answers.count { it.estado != "C" && it.estado != "X" }
    
    // Cálculo de Puntaje Total
    val rawScore = answers.sumOf { item ->
        when (item.estado) {
            "C" -> ptsPerCorrect
            "X" -> -1.0
            else -> 1.0 // Blanco
        }
    }
    // Asegurar que no sea negativo visualmente si no se desea, pero la lógica dice sumar.
    // El porcentaje para la dona: Score / 100 (Max posible)
    val percent = (rawScore / 100.0).coerceIn(0.0, 1.0).toFloat()
    
    // Nota en escala vigesimal (0-20)
    val nota20 = (rawScore / 100.0 * 20.0).coerceIn(0.0, 20.0)

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Estudiante",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = studentName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = "Puntaje",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "${String.format("%.2f", rawScore)} / 100",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Nota ${String.format("%.2f", nota20)}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(), 
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CounterBadge(count = correctCount, icon = Icons.Filled.CheckCircle, color = Color(0xFF2E7D32))
                    CounterBadge(count = incorrectCount, icon = Icons.Filled.Cancel, color = Color(0xFFC62828))
                    CounterBadge(count = blankCount, icon = Icons.Filled.Warning, color = Color(0xFFFF8F00))
                }
            }
            DonutPercentage(percent = percent, modifier = Modifier.size(80.dp))
        }
    }
}

@Composable
private fun CounterBadge(count: Int, icon: ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon, 
            contentDescription = null, 
            tint = color, 
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = count.toString(), 
            style = MaterialTheme.typography.bodyMedium, 
            fontWeight = FontWeight.Bold,
            color = color
        )
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
    
    // Cálculo de Puntos por Correcta para mostrar
    val total = answers.size.takeIf { it > 0 } ?: 1
    val ptsPerCorrect = 100.0 / total

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBg)
                .padding(vertical = 10.dp, horizontal = 8.dp)
        ) {
            Text(
                text = "N°",
                modifier = Modifier.weight(0.8f),
                fontWeight = FontWeight.SemiBold,
                color = headerTextColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Correcta",
                modifier = Modifier.weight(1.5f),
                fontWeight = FontWeight.SemiBold,
                color = headerTextColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Marcada",
                modifier = Modifier.weight(1.5f),
                fontWeight = FontWeight.SemiBold,
                color = headerTextColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Puntaje",
                modifier = Modifier.weight(1.2f),
                fontWeight = FontWeight.SemiBold,
                color = headerTextColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Estado",
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.SemiBold,
                color = headerTextColor,
                textAlign = TextAlign.Center
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(answers) { a ->
                // Determinar Puntos y Color de Fila
                val (ptsStr, rowColor) = when (a.estado) {
                    "C" -> Pair("+${String.format("%.2f", ptsPerCorrect)}", Color(0xFFE8F5E9)) // Verde suave
                    "X" -> Pair("-1.00", Color(0xFFFFEBEE)) // Rojo suave
                    else -> Pair("+1.00", Color(0xFFFFFDE7)) // Amarillo suave (Blanco)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(rowColor)
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = a.q.toString(),
                        modifier = Modifier.weight(0.8f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = a.correcta.ifEmpty { "-" },
                        modifier = Modifier.weight(1.5f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = a.letra.ifEmpty { "-" },
                        modifier = Modifier.weight(1.5f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = ptsStr,
                        modifier = Modifier.weight(1.2f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = if (ptsStr.startsWith("-")) Color.Red else Color.Unspecified
                    )
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        when (a.estado) {
                            "C" -> Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Correcta",
                                tint = Color(0xFF2E7D32)
                            )

                            "X" -> Icon(
                                imageVector = Icons.Filled.Cancel,
                                contentDescription = "Incorrecta",
                                tint = Color(0xFFC62828)
                            )

                            else -> Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "En Blanco",
                                tint = Color(0xFFFF8F00)
                            )
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            }
            item { HorizontalDivider(color = MaterialTheme.colorScheme.outline) }
        }
    }
}

private suspend fun reprocess(
    client: OkHttpClient,
    runId: String,
    tipo: Int
): Pair<Bitmap?, List<AnswerItem>> {
    return withContext(Dispatchers.IO) {
        try {
            val endpoint = "${NetworkConfig.baseRoot}/api/scan/process-omr"
            val payload = JSONObject().put("run_id", runId).put("num_preguntas", tipo).toString()
            val req = Request.Builder().url(endpoint)
                .post(payload.toRequestBody("application/json".toMediaType())).build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null to emptyList()
                val body = resp.body?.string() ?: "{}"
                val json = JSONObject(body)
                val overlayUrl = json.optString("overlay_url", "")
                val arr: JSONArray = json.optJSONArray("answers") ?: JSONArray()
                val tmp = mutableListOf<AnswerItem>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    tmp.add(
                        AnswerItem(
                            o.optInt("q"),
                            o.optInt("r"),
                            o.optString("letra"),
                            o.optString("correcta"),
                            o.optString("estado", "P"),
                            o.optDouble("puntaje", 0.0)
                        )
                    )
                }
                var bmp: Bitmap? = null
                if (overlayUrl.isNotEmpty()) {
                    try {
                        client.newCall(Request.Builder().url(overlayUrl).get().build()).execute()
                            .use { r2 ->
                                if (r2.isSuccessful) {
                                    val bytes = r2.body?.bytes()
                                    if (bytes != null) bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                }
                            }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                bmp to tmp
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null to emptyList()
        }
    }
}

private suspend fun saveScan(
    client: OkHttpClient,
    runId: String,
    quizId: Int,
    studentId: Int
): Pair<Boolean, String> {
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
