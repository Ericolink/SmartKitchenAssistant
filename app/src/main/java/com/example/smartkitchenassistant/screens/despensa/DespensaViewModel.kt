package com.example.smartkitchenassistant.screens.despensa

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class DespensaViewModel : ViewModel() {

    private val _ingredientes = MutableStateFlow<List<String>>(emptyList())
    val ingredientes = _ingredientes.asStateFlow()

    fun agregarIngrediente(nombre: String) {
        _ingredientes.value = _ingredientes.value + nombre
    }

    fun eliminarIngrediente(nombre: String) {
        _ingredientes.value = _ingredientes.value - nombre
    }

    fun editarIngrediente(viejo: String, nuevo: String) {
        _ingredientes.value = _ingredientes.value.map {
            if (it == viejo) nuevo else it
        }
    }

}
