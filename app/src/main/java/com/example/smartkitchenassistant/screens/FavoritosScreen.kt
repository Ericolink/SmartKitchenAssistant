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

@Composable
fun FavoritosScreen() {

    // Paleta de colores
    val primario = Color(0xFFF9F5F0)
    val secundario = Color(0xFFF2EAD3)
    val naranja = Color(0xFFF4991A)
    val verde = Color(0xFF344F1F)

    // Datos de prueba (más adelante conectamos Firebase)
    var favoritos by remember {
        mutableStateOf(
            listOf(
                FavoritoUI(
                    id = "52772",
                    nombre = "Teriyaki Chicken Casserole",
                    categoria = "Japanese",
                    imagen = "https://www.themealdb.com/images/media/meals/wvpsxx1468256321.jpg"
                ),
                FavoritoUI(
                    id = "52959",
                    nombre = "Baked salmon with fennel & tomatoes",
                    categoria = "British",
                    imagen = "https://www.themealdb.com/images/media/meals/1548772327.jpg"
                ),
                FavoritoUI(
                    id = "52802",
                    nombre = "Fish pie",
                    categoria = "British",
                    imagen = "https://www.themealdb.com/images/media/meals/ysxwuq1487323065.jpg"
                ),
                FavoritoUI(
                    id = "52844",
                    nombre = "Lasagne",
                    categoria = "Italian",
                    imagen = "https://www.themealdb.com/images/media/meals/wtsvxx1511296896.jpg"
                ),
                FavoritoUI(
                    id = "53013",
                    nombre = "Apam balik",
                    categoria = "Malaysian",
                    imagen = "https://www.themealdb.com/images/media/meals/adxcbq1619787919.jpg"
                ),
                FavoritoUI(
                    id = "52893",
                    nombre = "Bigos (Hunters Stew)",
                    categoria = "Polish",
                    imagen = "https://www.themealdb.com/images/media/meals/rlwcc51598734603.jpg"
                ),
                FavoritoUI(
                    id = "52944",
                    nombre = "Escovitch Fish",
                    categoria = "Jamaican",
                    imagen = "https://www.themealdb.com/images/media/meals/1520084413.jpg"
                ),
                FavoritoUI(
                    id = "52819",
                    nombre = "Cajun spiced fish tacos",
                    categoria = "Mexican",
                    imagen = "https://www.themealdb.com/images/media/meals/uvuyxu1503067369.jpg"
                ),
                FavoritoUI(
                    id = "52931",
                    nombre = "Sugar Pie",
                    categoria = "Canadian",
                    imagen = "https://www.themealdb.com/images/media/meals/yrstur1511816605.jpg"
                ),
                FavoritoUI(
                    id = "52773",
                    nombre = "Honey Teriyaki Salmon",
                    categoria = "Japanese",
                    imagen = "https://www.themealdb.com/images/media/meals/xxyupu1468262513.jpg"
                )
            )
        )
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
                        IconButton(
                            onClick = {}
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Reproducir receta",
                                tint = naranja
                            )
                        }

                        // Botón quitar favorito
                        IconButton(
                            onClick = {
                                favoritos = favoritos.filterNot { it.id == fav.id }
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
    val id: String,
    val nombre: String,
    val categoria: String,
    val imagen: String
)
