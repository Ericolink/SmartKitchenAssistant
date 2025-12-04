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
import com.example.smartkitchenassistant.data.FavoritosRepository
import com.example.smartkitchenassistant.screens.FavoritoUI
import com.example.smartkitchenassistant.screens.enviarRecetaATV
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun BuscarRecetasScreen(viewModel: BuscarRecetasViewModel = viewModel()) {

    val Primario = Color(0xFFF9F5F0)
    val Secundario = Color(0xFFF2EAD3)
    val naranja = Color(0xFFF4991A)
    val verde = Color(0xFF344F1F)

    var query by remember { mutableStateOf(TextFieldValue("")) }
    val meals by viewModel.meals.collectAsState()

    val scope = rememberCoroutineScope()
    val repo = FavoritosRepository()

    val uid = FirebaseAuth.getInstance().currentUser?.uid

    // Lista de favoritos SOLO para la UI (los marcados localmente)
    val favoritosUI = remember { mutableStateListOf<String>() }

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
            placeholder = { Text("Ejemplo: pasta, pollo, pizza...") },
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = verde)
            },
            modifier = Modifier.fillMaxWidth(),
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

                // --------------------------------------------------------------
                // TARJETA DEL RESULTADO DE BÚSQUEDA (igual a FavoritosScreen)
                // --------------------------------------------------------------
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
                        contentDescription = null,
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(meal.strMeal, style = MaterialTheme.typography.titleMedium, color = verde)
                        Text(meal.strCategory ?: "", color = naranja)
                    }

                    // ----------------------------------------------------------
                    // BOTÓN ENVIAR A TV — usando tu método oficial
                    // ----------------------------------------------------------
                    IconButton(onClick = {

                        if (uid == null) return@IconButton

                        scope.launch {
                            enviarRecetaATV(
                                fav = FavoritoUI(
                                    id = meal.idMeal,
                                    nombre = meal.strMeal,
                                    categoria = meal.strCategory ?: "",
                                    imagen = meal.strMealThumb ?: ""
                                ),
                                uid = uid
                            )
                        }

                    }) {
                        Icon(Icons.Default.PlayArrow, "Enviar a TV", tint = naranja)
                    }

                    // ----------------------------------------------------------
                    // BOTÓN FAVORITO — mismlógica que FavoritosScreen
                    // ----------------------------------------------------------
                    val esFavorito = favoritosUI.contains(meal.idMeal)

                    IconButton(
                        onClick = {
                            if (esFavorito) {
                                favoritosUI.remove(meal.idMeal)
                            } else {
                                favoritosUI.add(meal.idMeal)

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
                            imageVector = if (esFavorito)
                                Icons.Default.Favorite
                            else
                                Icons.Default.FavoriteBorder,
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
