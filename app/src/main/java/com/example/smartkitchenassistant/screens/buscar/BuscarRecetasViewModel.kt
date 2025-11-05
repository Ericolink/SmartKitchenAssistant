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
import java.net.URL
import java.net.URLEncoder

data class Meal(
    val strMeal: String,
    val strMealThumb: String,
    val strCategory: String?
)

class BuscarRecetasViewModel : ViewModel() {
    private val _meals = MutableStateFlow<List<Meal>>(emptyList())
    val meals = _meals.asStateFlow()

    fun buscarRecetas(query: String) {
        val raw = query.trim()
        if (raw.isEmpty()) {
            _meals.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                // encode the query (spaces, accents, etc.)
                val encoded = URLEncoder.encode(raw, "UTF-8")

                // NOTE: use filter.php?i= for ingredient search, or search.php?s= for name search
                val urlStr = "https://www.themealdb.com/api/json/v1/1/filter.php?i=$encoded"

                Log.d("BuscarVM", "Request URL: $urlStr")

                val responseText = withContext(Dispatchers.IO) {
                    URL(urlStr).readText()
                }

                Log.d("BuscarVM", "Response: $responseText")

                val json = JSONObject(responseText)
                val mealsArray = json.optJSONArray("meals")

                if (mealsArray != null) {
                    val lista = mutableListOf<Meal>()
                    for (i in 0 until mealsArray.length()) {
                        val item = mealsArray.getJSONObject(i)
                        val name = item.optString("strMeal", "")
                        val thumb = item.optString("strMealThumb", "")
                        lista.add(Meal(strMeal = name, strMealThumb = thumb, strCategory = null))
                    }
                    _meals.value = lista
                } else {
                    // API returned {"meals":null} -> no results
                    _meals.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("BuscarVM", "Error buscarRecetas", e)
                _meals.value = emptyList()
            }
        }
    }
}
