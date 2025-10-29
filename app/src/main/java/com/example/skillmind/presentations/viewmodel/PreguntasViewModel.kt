package com.example.skillmind.presentations.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skillmind.data.QuestionsRepository
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject


data class MCQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int? = null
)

class PreguntasViewModel : ViewModel() {


    val isLoading = mutableStateOf(false)
    val lastError = mutableStateOf<String?>(null)
    val mcQuestionsList = mutableStateListOf<MCQuestion>()

    private val repo = QuestionsRepository()

    fun clearState() {
        mcQuestionsList.clear()
        isLoading.value = false
        lastError.value = null
    }


    fun generateQuestionsByTopic(subject: String, topic: String, count: Int) {
        viewModelScope.launch {
            isLoading.value = true
            lastError.value = null
            try {
                val rawJson = repo.generateRawFromTopic(subject, topic, count)
                processJson(rawJson)
            } catch (e: Exception) {
                lastError.value = e.message ?: "Error desconocido al generar preguntas."
            } finally {
                isLoading.value = false
            }
        }
    }


    fun generateQuestionsFromImage(uri: Uri, count: Int, context: Context) {
        viewModelScope.launch {
            isLoading.value = true
            lastError.value = null
            try {
                val rawJson = repo.generateRawFromImage(uri, count, context)
                processJson(rawJson)
            } catch (e: Exception) {
                lastError.value = e.message ?: "Error al generar preguntas desde imagen."
            } finally {
                isLoading.value = false
            }
        }
    }


    fun generateQuestionsFromPdf(context: Context, uri: Uri, count: Int) {
        viewModelScope.launch {
            isLoading.value = true
            lastError.value = null
            try {
                val rawJson = repo.generateRawFromPdf(context, uri, count)
                processJson(rawJson)
            } catch (e: Exception) {
                lastError.value = e.message ?: "Error al procesar el PDF."
            } finally {
                isLoading.value = false
            }
        }
    }

    private fun processJson(rawJson: String) {
        if (rawJson.isBlank()) {
            lastError.value = "La API respondió sin preguntas."
            mcQuestionsList.clear()
        } else {
            try {
                val parsedQuestions = parseJsonToMCQuestions(rawJson)
                if (parsedQuestions.isEmpty()) {
                    lastError.value = "No fue posible parsear preguntas del JSON."
                    mcQuestionsList.clear()
                } else {
                    mcQuestionsList.clear()
                    mcQuestionsList.addAll(parsedQuestions)
                }
            } catch(e: JSONException) {
                lastError.value = "La respuesta de la API no es un JSON válido."
                Log.e("ViewModel", "JSON Parsing Error: ${e.message}")
                Log.e("ViewModel", "Raw Response: $rawJson")
                mcQuestionsList.clear()
            }
        }
    }
}

fun parseJsonToMCQuestions(jsonString: String): List<MCQuestion> {
    val questions = mutableListOf<MCQuestion>()
    val cleanJsonString = jsonString.removePrefix("```json").removeSuffix("```").trim()

    val jsonObject = JSONObject(cleanJsonString)
    val preguntasArray = jsonObject.getJSONArray("preguntas")

    for (i in 0 until preguntasArray.length()) {
        val preguntaObject = preguntasArray.getJSONObject(i)

        val pregunta = preguntaObject.getString("pregunta")
        val opcionesArray = preguntaObject.getJSONArray("opciones")
        val respuestaCorrecta = preguntaObject.getString("respuesta_correcta")

        val opciones = List(opcionesArray.length()) { j -> opcionesArray.getString(j) }

        val correctIndex = opciones.indexOfFirst { it.equals(respuestaCorrecta, ignoreCase = true) }

        if (pregunta.isNotEmpty() && opciones.size == 3 && correctIndex != -1) {
            questions.add(MCQuestion(pregunta, opciones, correctIndex))
        }
    }
    return questions
}
