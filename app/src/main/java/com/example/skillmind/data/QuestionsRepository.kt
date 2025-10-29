package com.example.skillmind.data

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.skillmind.BuildConfig
import com.example.skillmind.data.remote.ApiService
import com.example.skillmind.data.remote.RetrofitClient
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream

class QuestionsRepository {

    private val api: ApiService = RetrofitClient.apiService
    private val currentApiKey = BuildConfig.GEMINI_API_KEY

    private suspend fun readPdfContent(context: Context, uri: Uri): String {
        return withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val stringBuilder = StringBuilder()
                    val reader = PdfReader(inputStream)
                    val numPages = reader.numberOfPages
                    for (i in 1..numPages) {
                        stringBuilder.append(PdfTextExtractor.getTextFromPage(reader, i)).append("\n")
                    }
                    reader.close()
                    stringBuilder.toString()
                } ?: throw IOException("Content resolver returned null stream")
            } catch (e: Exception) {
                Log.e("QuestionsRepository", "Error reading PDF: ${e.message}", e)
                ""
            }
        }
    }

    private fun uriToBase64(uri: Uri, context: Context): String {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        inputStream?.close() ?: throw IOException("Could not open InputStream for URI.")
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    // --- Base Prompt --- 
    private fun getBaseJsonPrompt(count: Int, topic: String, source: String?): String {
        val sourceText = if (source != null) " basándote en $source" else ""
        return """
        Genera $count preguntas de opción múltiple sobre el tema "$topic"$sourceText.
        IMPORTANTE: Toda la salida debe ser rigurosamente en idioma español.
        
        La respuesta DEBE ser un único string JSON válido, sin formato de código (markdown), comentarios, ni texto introductorio.
        El JSON debe ser un objeto que contenga una sola clave "preguntas", cuyo valor sea un array de objetos.
        Cada objeto en el array debe tener TRES claves:
        1. "pregunta": (String) El texto de la pregunta.
        2. "opciones": (Array de Strings) Una lista con exactamente 3 opciones de respuesta.
        3. "respuesta_correcta": (String) El texto exacto de la opción que es correcta.

        Ejemplo de formato de salida requerido:
        {
          "preguntas": [
            {
              "pregunta": "¿Cuál es la capital de Francia?",
              "opciones": [
                "Londres",
                "París",
                "Berlín"
              ],
              "respuesta_correcta": "París"
            }
          ]
        }
        """.trimIndent()
    }

    suspend fun generateRawFromTopic(subject: String, topic: String, count: Int): String {
        val source = if (subject == "Otros Temas") null else "la materia $subject"
        val prompt = getBaseJsonPrompt(count, topic, source)
        val body = mapOf("contents" to listOf(mapOf("parts" to listOf(mapOf("text" to prompt)))))
        val response = api.generate(apiKey = currentApiKey, body = body)
        return response.candidates?.flatMap { it.content?.parts.orEmpty() }?.mapNotNull { it.text }?.joinToString("") ?: ""
    }

    suspend fun generateRawFromImage(uri: Uri, count: Int, context: Context): String {
        return withContext(Dispatchers.IO) {
            val base64Image = uriToBase64(uri, context)
            val prompt = getBaseJsonPrompt(count, "el contenido de la imagen", "la siguiente imagen")
            val body = mapOf(
                "contents" to listOf(
                    mapOf("parts" to listOf(
                        mapOf("text" to prompt),
                        mapOf("inlineData" to mapOf("mimeType" to "image/jpeg", "data" to base64Image))
                    ))
                )
            )
            val response = api.generate(apiKey = currentApiKey, body = body)
            return@withContext response.candidates?.flatMap { it.content?.parts.orEmpty() }?.mapNotNull { it.text }?.joinToString("") ?: ""
        }
    }

    suspend fun generateRawFromPdf(context: Context, uri: Uri, count: Int): String {
        val pdfText = readPdfContent(context, uri)
        if (pdfText.isBlank()) return ""
        
        val prompt = getBaseJsonPrompt(count, "el texto proporcionado", "el siguiente texto extraído de un PDF") + "\n\nTexto del PDF:\n$pdfText"
        val body = mapOf("contents" to listOf(mapOf("parts" to listOf(mapOf("text" to prompt)))))
        val response = api.generate(apiKey = currentApiKey, body = body)
        return response.candidates?.flatMap { it.content?.parts.orEmpty() }?.mapNotNull { it.text }?.joinToString("") ?: ""
    }
}