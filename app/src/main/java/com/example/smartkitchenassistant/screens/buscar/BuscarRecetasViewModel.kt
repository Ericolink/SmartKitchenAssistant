package com.example.smartkitchenassistant.screens.buscar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URLEncoder
import java.net.URL

data class Meal(
    val strMeal: String,
    val strMealThumb: String,
    val strCategory: String?
)

class BuscarRecetasViewModel : ViewModel() {

    private val _meals = MutableStateFlow<List<Meal>>(emptyList())
    val meals = _meals.asStateFlow()

    // Diccionario simple ES -> EN
    private val diccionarioIngredientes = mapOf(
        "pollo" to "chicken",
        "carne" to "beef",
        "res" to "beef",
        "cerdo" to "pork",
        "puerco" to "pork",
        "pescado" to "fish",
        "atun" to "tuna",
        "atún" to "tuna",
        "huevo" to "egg",
        "huevos" to "eggs",
        "leche" to "milk",
        "harina" to "flour",
        "chile" to "chili",
        "chiles" to "chili",
        "cebolla" to "onion",
        "ajo" to "garlic",
        "tomate" to "tomato",
        "papas" to "potato",
        "papa" to "potato",
        "arroz" to "rice",
        "pasta" to "pasta",
        "queso" to "cheese",
        "mantequilla" to "butter",
        "sal" to "salt",
        "azucar" to "sugar",
        "azúcar" to "sugar"
    )

    private fun traducirIngrediente(palabra: String): String {
        val lower = palabra.lowercase()
        return diccionarioIngredientes[lower] ?: palabra
    }

    fun buscarRecetas(query: String) {
        val raw = query.trim()
        if (raw.isEmpty()) {
            _meals.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                // 1. traducir (local)
                val translated = traducirIngrediente(raw)
                Log.d("BuscarVM", "Traducido '$raw' -> '$translated'")

                val encoded = URLEncoder.encode(translated, "UTF-8")
                val urlStr = "https://www.themealdb.com/api/json/v1/1/filter.php?i=$encoded"

                Log.d("BuscarVM", "Request URL: $urlStr")

                val responseText = withContext(Dispatchers.IO) {
                    URL(urlStr).readText()
                }

                Log.d("BuscarVM", "Response: ${responseText.take(500)}")

                val json = JSONObject(responseText)
                val mealsArray = json.optJSONArray("meals")

                if (mealsArray != null) {
                    val lista = mutableListOf<Meal>()
                    for (i in 0 until mealsArray.length()) {
                        val item = mealsArray.getJSONObject(i)
                        lista.add(
                            Meal(
                                strMeal = item.optString("strMeal", ""),
                                strMealThumb = item.optString("strMealThumb", ""),
                                strCategory = null
                            )
                        )
                    }
                    _meals.value = lista
                } else {
                    _meals.value = emptyList() // sin resultados
                }

            } catch (e: Exception) {
                Log.e("BuscarVM", "Error buscando recetas", e)
                _meals.value = emptyList()
            }
        }
    }
}
