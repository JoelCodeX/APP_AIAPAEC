package com.jotadev.aiapaec.ui.screens.scan

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
 private const val BASE_ROOT: String = "http://192.168.1.8:5000"

data class AnswerItem(val q: Int, val r: Int, val letra: String)

@Composable
fun ScanResultScreen(navController: NavController, runId: String, overlayUrl: String, tipoInit: Int) {
    var overlay by remember { mutableStateOf<Bitmap?>(null) }
    var answers by remember { mutableStateOf(listOf<AnswerItem>()) }
    var tipoSel by remember { mutableStateOf(tipoInit) }
    val client = remember { OkHttpClient.Builder().build() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(runId, overlayUrl) {
        withContext(Dispatchers.IO) {
            val ansUrl = "$BASE_ROOT/scan/results/answers/$runId"
            client.newCall(Request.Builder().url(ansUrl).get().build()).execute().use { resp ->
                val body = resp.body?.string() ?: "{}"
                val json = JSONObject(body)
                val arr: JSONArray = json.optJSONArray("answers") ?: JSONArray()
                val tmp = mutableListOf<AnswerItem>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    tmp.add(AnswerItem(o.optInt("q"), o.optInt("r"), o.optString("letra")))
                }
                answers = tmp
            }
            client.newCall(Request.Builder().url(overlayUrl).get().build()).execute().use { resp ->
                val bytes = resp.body?.bytes()
                if (bytes != null) overlay = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Resultados", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                if (tipoSel != 20) {
                    tipoSel = 20
                    scope.launch {
                        val res = reprocess(client, runId, 20)
                        overlay = res.first
                        answers = res.second
                    }
                }
            }, enabled = tipoSel != 20) { Text("Cartilla 20") }
            Button(onClick = {
                if (tipoSel != 50) {
                    tipoSel = 50
                    scope.launch {
                        val res = reprocess(client, runId, 50)
                        overlay = res.first
                        answers = res.second
                    }
                }
            }, enabled = tipoSel != 50) { Text("Cartilla 50") }
        }
        if (overlay != null) {
            Image(bitmap = overlay!!.asImageBitmap(), contentDescription = "Overlay", modifier = Modifier.fillMaxWidth().height(360.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(answers) { a ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Text(text = "Q${a.q}", modifier = Modifier.width(60.dp))
                    Text(text = "R${a.r}", modifier = Modifier.width(60.dp))
                    Text(text = a.letra)
                }
            }
        }
    }
}

private suspend fun reprocess(client: OkHttpClient, runId: String, tipo: Int): Pair<Bitmap?, List<AnswerItem>> {
    return withContext(Dispatchers.IO) {
        val endpoint = if (tipo == 20) "$BASE_ROOT/scan/process-20" else "$BASE_ROOT/scan/process-50"
        val payload = JSONObject().put("run_id", runId).toString()
        val req = Request.Builder().url(endpoint).post(payload.toRequestBody("application/json".toMediaType())).build()
        client.newCall(req).execute().use { resp ->
            val body = resp.body?.string() ?: "{}"
            val json = JSONObject(body)
            val overlayUrl = json.optString("overlay_url", "")
            val arr: JSONArray = json.optJSONArray("answers") ?: JSONArray()
            val tmp = mutableListOf<AnswerItem>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                tmp.add(AnswerItem(o.optInt("q"), o.optInt("r"), o.optString("letra")))
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
