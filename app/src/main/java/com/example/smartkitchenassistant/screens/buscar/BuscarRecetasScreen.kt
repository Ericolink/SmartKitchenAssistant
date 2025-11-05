package com.example.smartkitchenassistant.screens.buscar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter

@Composable
fun BuscarRecetasScreen(viewModel: BuscarRecetasViewModel = viewModel()) {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    val meals by viewModel.meals.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text(
            text = "Buscar recetas",
            style = MaterialTheme.typography.titleLarge,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 🔹 Input de búsqueda mejorado para Material3
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                if (it.text.isNotEmpty()) {
                    viewModel.buscarRecetas(it.text)
                }
            },
            label = { Text("Buscar receta...") },
            placeholder = { Text("Ejemplo: pollo, arroz, pasta...") },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF5F5F5)),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = Color.Gray
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4CAF50),
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color(0xFF4CAF50)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Lista de recetas
        meals.forEach { meal ->
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                Image(
                    painter = rememberAsyncImagePainter(meal.strMealThumb),
                    contentDescription = meal.strMeal,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(meal.strMeal, style = MaterialTheme.typography.titleMedium)
                    Text(meal.strCategory ?: "", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // 🔹 Mensaje si no se encuentran recetas
        if (meals.isEmpty() && query.text.isNotEmpty()) {
            Text(
                text = "No se encontraron recetas para \"${query.text}\"",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
