package com.example.smartkitchenassistant.screens.despensa

import androidx.lifecycle.ViewModel
import com.example.smartkitchenassistant.data.model.Ingrediente
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class DespensaViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid

    private val _ingredientes = MutableStateFlow<List<Ingrediente>>(emptyList())
    val ingredientes = _ingredientes.asStateFlow()

    init {
        cargarIngredientes()
    }

    private fun cargarIngredientes() {
        if (uid == null) return

        db.collection("usuarios")
            .document(uid)
            .collection("despensa")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val lista = snapshot.documents.map { doc ->
                        Ingrediente(
                            id = doc.id,
                            nombre = doc.getString("nombre") ?: "",
                            cantidad = doc.getString("cantidad") ?: ""
                        )
                    }
                    _ingredientes.value = lista
                }
            }
    }

    fun agregarIngrediente(nombre: String, cantidad: String) {
        if (uid == null) return

        val data = mapOf(
            "nombre" to nombre,
            "cantidad" to cantidad
        )

        db.collection("usuarios")
            .document(uid)
            .collection("despensa")
            .add(data)
    }

    fun eliminarIngrediente(ingrediente: Ingrediente) {
        if (uid == null) return

        db.collection("usuarios")
            .document(uid)
            .collection("despensa")
            .document(ingrediente.id)
            .delete()
    }

    fun editarIngrediente(ingrediente: Ingrediente, nuevoNombre: String, nuevaCantidad: String) {
        if (uid == null) return

        val data = mapOf(
            "nombre" to nuevoNombre,
            "cantidad" to nuevaCantidad
        )

        db.collection("usuarios")
            .document(uid)
            .collection("despensa")
            .document(ingrediente.id)
            .update(data)
    }
}
