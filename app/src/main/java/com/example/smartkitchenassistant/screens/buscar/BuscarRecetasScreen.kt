package com.example.smartkitchenassistant.screens.buscar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.smartkitchenassistant.screens.buscar.BuscarRecetasViewModel
import kotlinx.coroutines.launch
import com.example.smartkitchenassistant.data.FavoritosRepository
import com.example.smartkitchenassistant.screens.FavoritoUI   // ← IMPORTANTE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun BuscarRecetasScreen(viewModel: BuscarRecetasViewModel = viewModel()) {

    val Primario = Color(0xFFF9F5F0)
    val Secundario = Color(0xFFF2EAD3)
    val naranja = Color(0xFFF4991A)
    val verde = Color(0xFF344F1F)

    var query by remember { mutableStateOf(TextFieldValue("")) }
    val meals by viewModel.meals.collectAsState()

    // Estado local (solo para la UI)
    val favoritos = remember { mutableStateListOf<String>() }

    // --- NUEVO ---
    val repo = FavoritosRepository()
    val scope = rememberCoroutineScope()

    val uid = FirebaseAuth.getInstance().currentUser?.uid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primario)
            .padding(16.dp)
    ) {

        Text(
            text = "Buscar recetas",
            style = MaterialTheme.typography.titleLarge,
            fontSize = 26.sp,
            color = verde
        )

        Spacer(modifier = Modifier.height(18.dp))

        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                if (it.text.isNotEmpty()) viewModel.buscarRecetas(it.text)
            },
            label = { Text("Buscar receta...", color = verde) },
            placeholder = { Text("Ejemplo: pizza") },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = verde
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .background(Secundario),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = naranja,
                unfocusedBorderColor = verde,
                cursorColor = naranja
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(meals) { meal ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Secundario)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Image(
                        painter = rememberAsyncImagePainter(meal.strMealThumb),
                        contentDescription = meal.strMeal,
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = meal.strMeal,
                            style = MaterialTheme.typography.titleMedium,
                            color = verde
                        )
                        Text(
                            text = meal.strCategory ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = naranja
                        )
                    }

                    //Enviar a la TV

                    IconButton(onClick = {

                        if (uid == null) return@IconButton

                        val receta = hashMapOf(
                            "title" to meal.strMeal,
                            "category" to (meal.strCategory ?: "Sin categoría"),
                            "image" to (meal.strMealThumb ?: ""),
                            "ingredients" to listOf("Cargando ingredientes..."),
                            "steps" to listOf("Cargando pasos...")
                        )

                        FirebaseFirestore.getInstance()
                            .collection("usuarios")
                            .document(uid)
                            .collection("recetas")
                            .document("actual")
                            .set(receta)
                    }) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Enviar a TV",
                            tint = naranja
                        )
                    }

                    // -------------------- FAVORITO --------------------
                    val esFavorito = favoritos.contains(meal.idMeal)

                    IconButton(
                        onClick = {

                            if (esFavorito) {
                                favoritos.remove(meal.idMeal)
                            } else {
                                favoritos.add(meal.idMeal)

                                // ---- GUARDAR EN FIREBASE ----
                                scope.launch {
                                    repo.agregarFavorito(
                                        FavoritoUI(
                                            id = meal.idMeal,
                                            nombre = meal.strMeal,
                                            categoria = meal.strCategory ?: "Sin categoría",
                                            imagen = meal.strMealThumb ?: ""
                                        )
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (esFavorito) Color.Red else verde
                        )
                    }
                }
            }
        }

        if (meals.isEmpty() && query.text.isNotEmpty()) {
            Text(
                text = "No se encontraron recetas para \"${query.text}\"",
                color = verde,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 20.dp)
            )
        }
    }
}
