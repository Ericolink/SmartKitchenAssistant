package com.example.smartkitchenassistant.data

import com.example.smartkitchenassistant.screens.FavoritoUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FavoritosRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun userCollection() = db
        .collection("usuarios")
        .document(auth.currentUser!!.uid)
        .collection("favoritos")

    suspend fun agregarFavorito(fav: FavoritoUI) {
        userCollection()
            .document(fav.id)
            .set(fav)
            .await()
    }

    suspend fun eliminarFavorito(id: String) {
        userCollection()
            .document(id)
            .delete()
            .await()
    }

    suspend fun obtenerFavoritos(): List<FavoritoUI> {
        return userCollection()
            .get()
            .await()
            .toObjects(FavoritoUI::class.java)
    }
}
