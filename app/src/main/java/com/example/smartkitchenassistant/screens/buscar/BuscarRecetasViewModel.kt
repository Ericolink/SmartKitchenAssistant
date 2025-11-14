package com.example.smartkitchenassistant.screens.buscar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartkitchenassistant.data.model.Meal
import com.example.smartkitchenassistant.data.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BuscarRecetasViewModel : ViewModel() {

    private val _meals = MutableStateFlow<List<Meal>>(emptyList())
    val meals = _meals.asStateFlow()

    fun buscarRecetas(query: String) {
        val texto = query.trim()
        if (texto.isEmpty()) {
            _meals.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                // usa: search.php?s=NombreComida
                val response = RetrofitInstance.api.searchMeals(texto)
                val lista = response.meals ?: emptyList()
                _meals.value = lista
            } catch (e: Exception) {
                Log.e("BuscarVM", "Error buscando receta por nombre", e)
                _meals.value = emptyList()
            }
        }
    }
}
