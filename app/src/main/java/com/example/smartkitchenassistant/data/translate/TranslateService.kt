package com.example.smartkitchenassistant.data.translate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object TranslateService {

    suspend fun translateToSpanish(text: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val apiUrl = URL("https://libretranslate.de/translate")
                val connection = apiUrl.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonBody = """
                {
                    "q": "$text",
                    "source": "en",
                    "target": "es",
                    "format": "text"
                }
                """.trimIndent()

                connection.outputStream.use { os ->
                    os.write(jsonBody.toByteArray())
                }

                val response = connection.inputStream.bufferedReader().readText()
                val json = JSONObject(response)

                json.getString("translatedText")

            } catch (e: Exception) {
                e.printStackTrace()
                text  // fallback: regresa el texto original
            }
        }
    }
}
