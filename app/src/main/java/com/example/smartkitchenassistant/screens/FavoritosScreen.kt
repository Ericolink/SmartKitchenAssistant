package com.example.smartkitchenassistant.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

@Composable
fun FavoritosScreen() {

    // Colores
    val primario = Color(0xFFF9F5F0)
    val secundario = Color(0xFFF2EAD3)
    val naranja = Color(0xFFF4991A)
    val verde = Color(0xFF344F1F)

    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    var favoritos by remember { mutableStateOf<List<FavoritoUI>>(emptyList()) }
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    // Cargar favoritos
    LaunchedEffect(Unit) {
        if (uid != null) {
            db.collection("usuarios")
                .document(uid)
                .collection("favoritos")
                .addSnapshotListener { snap, _ ->
                    favoritos = snap?.documents?.map { doc ->
                        FavoritoUI(
                            id = doc.id,
                            nombre = doc.getString("nombre") ?: "",
                            categoria = doc.getString("categoria") ?: "",
                            imagen = doc.getString("imagen") ?: "",
                            instrucciones = doc.getString("instrucciones") ?: "",
                            ingredientes = doc.get("ingredientes") as? List<String> ?: emptyList()
                        )
                    } ?: emptyList()
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(primario)
            .padding(16.dp)
    ) {

        Text(
            text = "Recetas favoritas",
            style = MaterialTheme.typography.titleLarge,
            fontSize = 26.sp,
            color = verde
        )

        Spacer(Modifier.height(20.dp))

        if (favoritos.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aún no tienes recetas favoritas", fontSize = 20.sp, color = verde)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {

                items(favoritos) { fav ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(secundario)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Image(
                            painter = rememberAsyncImagePainter(fav.imagen),
                            contentDescription = null,
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(fav.nombre, style = MaterialTheme.typography.titleMedium, color = verde)
                            Text(fav.categoria, color = naranja)
                        }

                        // --------------------------------------------------------------
                        // BOTÓN ENVIAR A TV
                        // --------------------------------------------------------------
                        IconButton(onClick = {
                            if (uid == null) return@IconButton

                            scope.launch {
                                enviarRecetaATV(fav, uid)
                            }

                        }) {
                            Icon(Icons.Default.PlayArrow, "Enviar a TV", tint = naranja)
                        }

                        // Eliminar favorito
                        IconButton(onClick = {
                            if (uid != null) {
                                db.collection("usuarios")
                                    .document(uid)
                                    .collection("favoritos")
                                    .document(fav.id)
                                    .delete()
                            }
                        }) {
                            Icon(Icons.Default.Favorite, "Eliminar", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

// --------------------------------------------------------------
// METODO PRINCIPAL PARA ENVIAR RECETA A LA TV
// --------------------------------------------------------------
suspend fun enviarRecetaATV(fav: FavoritoUI, uid: String) {

    val db = FirebaseFirestore.getInstance()

    var instrucciones = fav.instrucciones
    var ingredientes = fav.ingredientes

    // Si están vacíos → obtener desde TheMealDB usando el idMeal (fav.id)
    if (instrucciones.isBlank() || ingredientes.isEmpty()) {
        Log.d("TV_SEND", "Buscando datos completos desde TheMealDB… id=${fav.id}")

        try {
            val fetched = fetchMealDetailsFromTheMealDB(fav.id)
            if (instrucciones.isBlank()) instrucciones = fetched.first
            if (ingredientes.isEmpty()) ingredientes = fetched.second
        } catch (e: Exception) {
            Log.e("TV_SEND", "Error obteniendo detalles: ${e.message}")
        }
    }

    val pasos = instrucciones
        .replace("\r", "\n")
        .split("\n")
        .map { it.trim() }
        .filter { it.isNotBlank() }

    val finalSteps = if (pasos.isEmpty()) listOf("No hay pasos disponibles") else pasos
    val finalIngredients =
        if (ingredientes.isEmpty()) listOf("No hay ingredientes disponibles") else ingredientes

    val receta = hashMapOf(
        "title" to fav.nombre,
        "category" to fav.categoria,
        "image" to fav.imagen,
        "ingredients" to finalIngredients,
        "steps" to finalSteps,
        "updatedAt" to System.currentTimeMillis()
    )

    db.collection("usuarios")
        .document(uid)
        .collection("recetas")
        .document("actual")
        .set(receta)

    Log.d("TV_SEND", "Receta enviada correctamente a la TV ✔")
}

// --------------------------------------------------------------
// FUNCIÓN PARA OBTENER INSTRUCCIONES E INGREDIENTES DESDE THEMEALDB
// --------------------------------------------------------------
suspend fun fetchMealDetailsFromTheMealDB(idMeal: String): Pair<String, List<String>> {

    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://www.themealdb.com/api/json/v1/1/lookup.php?i=$idMeal")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Respuesta HTTP ${response.code}")

            val jsonText = response.body?.string() ?: throw Exception("Cuerpo vacío")
            val jsonObj = JSONObject(jsonText)

            val meals = jsonObj.optJSONArray("meals") ?: return@withContext Pair("", emptyList())
            val meal = meals.getJSONObject(0)

            val instru = meal.optString("strInstructions", "")

            // Ingredientes 1..20
            val ingredientesList = mutableListOf<String>()
            for (i in 1..20) {
                val ing = meal.optString("strIngredient$i", "").trim()
                val measure = meal.optString("strMeasure$i", "").trim()

                if (ing.isNotBlank() && ing.lowercase() != "null") {
                    val item = if (measure.isNotBlank() && measure.lowercase() != "null")
                        "$measure $ing"
                    else ing
                    ingredientesList.add(item)
                }
            }

            Pair(instru, ingredientesList)
        }
    }
}

// --------------------------------------------------------------
// MODELO
// --------------------------------------------------------------
data class FavoritoUI(
    val id: String = "",
    val nombre: String = "",
    val categoria: String = "",
    val imagen: String = "",
    val instrucciones: String = "",
    val ingredientes: List<String> = emptyList()
)
