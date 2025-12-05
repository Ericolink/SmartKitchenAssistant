package com.example.smartkitchenassistant.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartkitchenassistant.data.FavoritosRepository
import com.example.smartkitchenassistant.data.model.Meal
import com.example.smartkitchenassistant.data.network.RetrofitInstance
import com.example.smartkitchenassistant.ml.RecomendadorTFLite
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class RecomendacionesViewModel(app: Application) : AndroidViewModel(app) {

    // ============================================================
    // FIREBASE (privado porque la UI no debe acceder directamente)
    // ============================================================
    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid
    private val favRepo = FavoritosRepository()

    // ============================================================
    // TFLITE
    // ============================================================
    private val model = RecomendadorTFLite(app)

    // ============================================================
    // STATEFLOW PARA UI
    // ============================================================
    private val _recomendaciones = MutableStateFlow<List<Meal>>(emptyList())
    val recomendaciones = _recomendaciones.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando = _cargando.asStateFlow()

    // Datos del usuario ya normalizados
    private var ingredientesUsuario = listOf<String>()
    private var favoritosUsuario = listOf<String>()
    private var historialBusquedas = listOf<String>()

    init {
        cargarDatos()
    }

    // ============================================================
    // MÉTODOS PÚBLICOS PARA OTRAS IA / MÓDULOS
    // ============================================================
    fun obtenerIngredientesParaIA(): List<String> = ingredientesUsuario
    fun obtenerFavoritosParaIA(): List<String> = favoritosUsuario
    fun obtenerHistorialParaIA(): List<String> = historialBusquedas

    // ============================================================
    // BOTÓN DE REFRESCAR
    // ============================================================
    fun refrescarRecomendaciones() {
        cargarDatos()
    }

    // ============================================================
    // NORMALIZADOR DE TEXTO
    // ============================================================
    private fun normalizar(texto: String): String {
        return texto
            .lowercase()
            .replace("""\([^)]*\)""".toRegex(), "")
            .replace("""\d""".toRegex(), "")
            .replace("kg", "")
            .replace("cup", "")
            .replace("tablespoon", "")
            .replace("teaspoon", "")
            .replace("breast", "")
            .replace("piece", "")
            .replace("pieces", "")
            .replace(",", "")
            .trim()
    }

    // ============================================================
    // DESCARGA TODAS LAS RECETAS A–Z
    // ============================================================
    private suspend fun cargarTodasLasRecetas(): List<Meal> {
        val letras = ('a'..'z')
        val lista = mutableListOf<Meal>()

        for (letra in letras) {
            try {
                val resp = RetrofitInstance.api.searchMeals(letra.toString())
                resp.meals?.let { lista.addAll(it) }
            } catch (_: Exception) {}
        }

        return lista
    }

    // ============================================================
    // CARGA DESDE FIREBASE Y GENERA RECOMENDACIONES
    // ============================================================
    private fun cargarDatos() {
        if (uid == null) return

        viewModelScope.launch {

            _cargando.value = true

            ingredientesUsuario = cargarIngredientes().map { normalizar(it) }
            favoritosUsuario = favRepo.obtenerFavoritos().map { it.nombre.lowercase() }
            historialBusquedas = cargarHistorial()

            val recetas = cargarTodasLasRecetas()

            val procesadas = recetas.map { receta ->

                val ingredientesReceta = receta.getIngredientList()
                    .map { normalizar(it) }
                    .filter { it.isNotBlank() }

                // --- Features IA ---
                val matchPct = calcularMatch(ingredientesUsuario, ingredientesReceta)
                val missingCount = calcularFaltantes(ingredientesUsuario, ingredientesReceta)
                val ingredientOverlapRatio = matchPct
                val ingredientCountScore = 1f - (ingredientesReceta.size / 20f)

                val categoryMatch =
                    if (favoritosUsuario.any { receta.strCategory?.lowercase()?.contains(it) ?: false }) 1f else 0f

                val areaMatch =
                    if (historialBusquedas.any { receta.strArea?.lowercase()?.contains(it) ?: false }) 1f else 0f

                val favoriteNameSimilarity =
                    favoritosUsuario.count { receta.strMeal.lowercase().contains(it) }
                        .coerceAtMost(1)
                        .toFloat()

                val searchSimilarity = calcularSimilitudHistorial(receta.strMeal)

                val ingredientVectorScore =
                    (matchPct * 0.7f + ingredientCountScore * 0.3f)

                val pastInteractionScore =
                    (favoriteNameSimilarity * 0.5f + searchSimilarity * 0.5f)

                // --- IA (10 FEATURES) ---
                val score = model.predecir(
                    matchPct,
                    missingCount,
                    ingredientOverlapRatio,
                    ingredientCountScore,
                    categoryMatch,
                    areaMatch,
                    favoriteNameSimilarity,
                    searchSimilarity,
                    ingredientVectorScore,
                    pastInteractionScore
                )

                receta to score
            }
                .sortedByDescending { it.second }
                .map { it.first }
                .take(10)

            _recomendaciones.value = procesadas
            _cargando.value = false
        }
    }

    // ============================================================
    // FIREBASE HELPERS
    // ============================================================
    private suspend fun cargarIngredientes(): List<String> {
        val snapshot = db.collection("usuarios")
            .document(uid!!)
            .collection("despensa")
            .get()
            .await()

        return snapshot.documents.map { it.getString("nombre") ?: "" }
    }

    private suspend fun cargarHistorial(): List<String> {
        val snapshot = db.collection("usuarios")
            .document(uid!!)
            .collection("historialBusquedas")
            .get()
            .await()

        return snapshot.documents.map { it.getString("texto") ?: "" }
    }

    // ============================================================
    // FEATURE CALCULATIONS
    // ============================================================
    private fun calcularMatch(userIng: List<String>, recIng: List<String>): Float {
        if (recIng.isEmpty()) return 0f
        val inter = recIng.count { userIng.contains(it) }
        return (inter.toFloat() / recIng.size).coerceIn(0f, 1f)
    }

    private fun calcularFaltantes(userIng: List<String>, recIng: List<String>): Float {
        return recIng.count { !userIng.contains(it) }.toFloat()
    }

    private fun calcularSimilitudHistorial(nombre: String): Float {
        val n = nombre.lowercase()
        if (historialBusquedas.isEmpty()) return 0f

        val count = historialBusquedas.count { h ->
            h.length >= 2 && (n.contains(h) || h.contains(n.take(3)))
        }
        return count.coerceAtMost(1).toFloat()
    }
}
