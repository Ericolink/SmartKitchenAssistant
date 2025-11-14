package com.example.smartkitchenassistant.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartkitchenassistant.R

data class RecetaRecomendada(
    val id: String = "",
    val titulo: String = "",
    val categoria: String = "",
    val imagen: Int,
)

@Composable
fun RecomendacionesScreen() {

    // 🔥 Ejemplo de recetas de demostración (solo vista)
    val recetasDemo = listOf(
        RecetaRecomendada(
            id = "1",
            titulo = "Tacos de pollo especiado",
            categoria = "Mexican",
            imagen = R.drawable.food1
        ),
        RecetaRecomendada(
            id = "2",
            titulo = "Sopa cremosa de vegetales",
            categoria = "Vegetarian",
            imagen = R.drawable.food2
        ),
        RecetaRecomendada(
            id = "3",
            titulo = "Pasta al pesto con queso",
            categoria = "Italian",
            imagen = R.drawable.food3
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 50.dp)
    ) {

        // 🔵 TÍTULO
        Text(
            text = "Recomendaciones",
            style = MaterialTheme.typography.headlineMedium.copy(
                color = Color(0xFF33691E),
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(start = 20.dp, bottom = 20.dp)
        )

        if (recetasDemo.isEmpty()) {
            // 🟡 Estado cuando no hay recomendaciones aún
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no hay recomendaciones.\nCuando registres tus ingredientes, te sugeriremos recetas.",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        } else {

            // 🟢 LISTA DE RECOMENDACIONES
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(recetasDemo) { receta ->
                    TarjetaRecomendacion(receta)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 🎨 TARJETA DE RECOMENDACIÓN
// ---------------------------------------------------------------------------

@Composable
fun TarjetaRecomendacion(receta: RecetaRecomendada) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { /* TODO: acción futura */ },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F1DC)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {

            // 🟤 Imagen de receta
            Image(
                painter = painterResource(id = receta.imagen),
                contentDescription = receta.titulo,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(95.dp)
                    .background(Color.LightGray, RoundedCornerShape(15.dp))
                    .padding(3.dp)
            )

            Spacer(modifier = Modifier.width(15.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                // Nombre receta
                Text(
                    text = receta.titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )

                // Categoría
                Text(
                    text = receta.categoria,
                    fontSize = 13.sp,
                    color = Color(0xFFB57F30),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Botón tipo "ver receta"
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Ver receta",
                tint = Color(0xFFFF9800),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
