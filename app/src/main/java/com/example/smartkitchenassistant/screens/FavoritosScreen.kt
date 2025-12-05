package com.example.smartkitchenassistant.screens

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
import com.example.smartkitchenassistant.data.FavoritosRepository
import com.example.smartkitchenassistant.data.network.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun FavoritosScreen() {

    // Paleta de colores
    val primario = Color(0xFFF9F5F0)
    val secundario = Color(0xFFF2EAD3)
    val naranja = Color(0xFFF4991A)
    val verde = Color(0xFF344F1F)

    val repo = FavoritosRepository()
    val scope = rememberCoroutineScope()

    // Lista que viene de Firebase
    var favoritos by remember { mutableStateOf<List<FavoritoUI>>(emptyList()) }

    val uid = FirebaseAuth.getInstance().currentUser?.uid

    // Cargar datos al entrar
    LaunchedEffect(Unit) {
        favoritos = repo.obtenerFavoritos()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(primario)
            .padding(16.dp)
    ) {

        // Título
        Text(
            text = "Recetas favoritas",
            style = MaterialTheme.typography.titleLarge,
            fontSize = 26.sp,
            color = verde
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (favoritos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no tienes recetas favoritas",
                    fontSize = 18.sp,
                    color = verde
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(favoritos) { fav ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(secundario)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // Imagen
                        Image(
                            painter = rememberAsyncImagePainter(fav.imagen),
                            contentDescription = fav.nombre,
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Info
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = fav.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                color = verde
                            )
                            Text(
                                text = fav.categoria,
                                style = MaterialTheme.typography.bodySmall,
                                color = naranja
                            )
                        }

                        // Botón reproducir
                        IconButton(onClick = {
                            if (uid == null) return@IconButton

                            scope.launch {
                                // Descarga la receta completa desde TheMealDB
                                val resp = RetrofitInstance.api.getMealById(fav.id)
                                val meal = resp.meals?.firstOrNull()

                                if (meal != null) {

                                    val receta = hashMapOf(
                                        "title" to meal.strMeal,
                                        "category" to (meal.strCategory ?: "Sin categoría"),
                                        "image" to (meal.strMealThumb ?: ""),
                                        "ingredients" to meal.getIngredientList(),
                                        "steps" to meal.getStepsList()
                                    )

                                    // Enviar a la TV
                                    FirebaseFirestore.getInstance()
                                        .collection("usuarios")
                                        .document(uid)
                                        .collection("recetas")
                                        .document("actual")
                                        .set(receta)
                                }
                            }
                        }) {
                            Icon(Icons.Default.PlayArrow, "Enviar a TV", tint = naranja)
                        }

                        // Botón eliminar favorito
                        IconButton(
                            onClick = {
                                scope.launch {
                                    repo.eliminarFavorito(fav.id)
                                    favoritos = favoritos.filterNot { it.id == fav.id }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Eliminar de favoritos",
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
        }
    }
}

data class FavoritoUI(
    val id: String = "",
    val nombre: String = "",
    val categoria: String = "",
    val imagen: String = ""
)
