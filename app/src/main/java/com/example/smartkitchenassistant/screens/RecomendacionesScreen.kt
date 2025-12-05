package com.example.smartkitchenassistant.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.smartkitchenassistant.data.FavoritosRepository
import com.example.smartkitchenassistant.data.model.Meal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

// ============================================================
//                    PANTALLA COMPLETA
// ============================================================

@Composable
fun RecomendacionesScreen(
    viewModel: RecomendacionesViewModel = viewModel()
) {
    val recetas by viewModel.recomendaciones.collectAsState()
    val cargando by viewModel.cargando.collectAsState()

    val repo = FavoritosRepository()
    val scope = rememberCoroutineScope()

    // Lista de favoritos desde Firebase
    var favoritos by remember { mutableStateOf<List<FavoritoUI>>(emptyList()) }

    // Cargar favoritos al entrar o actualizar ViewModel
    LaunchedEffect(Unit) {
        favoritos = repo.obtenerFavoritos()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // ------------------ TÍTULO Y REFRESH ------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recomendaciones",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF33691E)
            )

            IconButton(
                onClick = {
                    viewModel.refrescarRecomendaciones()
                    scope.launch { favoritos = repo.obtenerFavoritos() } // sincroniza favoritos
                },
                enabled = !cargando
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Refrescar",
                    tint = if (cargando) Color.Gray else Color(0xFF33691E),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ------------------ LOADING ------------------
        if (cargando) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = Color(0xFF33691E))
                Spacer(modifier = Modifier.height(10.dp))
                Text("Actualizando recomendaciones...", color = Color.Gray)
            }
            return
        }

        // ------------------ LISTADO ------------------
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(recetas) { meal ->

                TarjetaRecomendacionMeal(
                    meal = meal,
                    favoritos = favoritos,
                    onAgregarFavorito = { fav ->
                        scope.launch {
                            repo.agregarFavorito(fav)
                            favoritos = repo.obtenerFavoritos() // actualizar pantalla
                        }
                    }
                )
            }
        }
    }
}

/* =======================================================================
   TARJETA INDIVIDUAL DE RECOMENDACIÓN
   ======================================================================= */

@Composable
fun TarjetaRecomendacionMeal(
    meal: Meal,
    favoritos: List<FavoritoUI>,
    onAgregarFavorito: (FavoritoUI) -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val firestore = FirebaseFirestore.getInstance()

    val esFavorito = favoritos.any { it.id == meal.idMeal }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F1DC)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {

            // ------------------ IMAGEN ------------------
            Image(
                painter = rememberAsyncImagePainter(meal.strMealThumb),
                contentDescription = meal.strMeal,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(95.dp)
                    .background(Color.LightGray, RoundedCornerShape(15.dp))
                    .padding(3.dp)
            )

            Spacer(modifier = Modifier.width(15.dp))

            // ------------------ INFORMACIÓN ------------------
            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = meal.strMeal,
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                Text(
                    text = meal.strCategory ?: "Sin categoría",
                    fontSize = 13.sp,
                    color = Color(0xFFB57F30),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // ------------------ ENVIAR A TV ------------------
            IconButton(onClick = {

                val receta = hashMapOf(
                    "title" to meal.strMeal,
                    "category" to (meal.strCategory ?: "Sin categoría"),
                    "image" to (meal.strMealThumb ?: ""),
                    "ingredients" to meal.getIngredientList(),
                    "steps" to meal.getStepsList()
                )

                firestore.collection("usuarios")
                    .document(uid)
                    .collection("recetas")
                    .document("actual")
                    .set(receta)

            }) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Enviar a TV",
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(32.dp)
                )
            }

            // ------------------ FAVORITO ------------------
            IconButton(
                onClick = {
                    if (!esFavorito) {
                        onAgregarFavorito(
                            FavoritoUI(
                                id = meal.idMeal,
                                nombre = meal.strMeal,
                                categoria = meal.strCategory ?: "Sin categoría",
                                imagen = meal.strMealThumb ?: ""
                            )
                        )
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Favorito",
                    tint = if (esFavorito) Color.Red else Color.Gray
                )
            }
        }
    }
}
