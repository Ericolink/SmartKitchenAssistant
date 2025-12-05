package com.example.smartkitchenassistant.screens.despensa

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.smartkitchenassistant.data.model.Ingrediente

@Composable
fun DespensaScreen(viewModel: DespensaViewModel = viewModel()) {

    val primario = Color(0xFFF9F5F0)
    val secundario = Color(0xFFF2EAD3)
    val naranja = Color(0xFFF4991A)
    val verde = Color(0xFF344F1F)

    var nuevoIngrediente by remember { mutableStateOf(TextFieldValue("")) }
    var nuevaCantidad by remember { mutableStateOf(TextFieldValue("")) }

    var ingredienteAEditar by remember { mutableStateOf<Ingrediente?>(null) }
    var textoEditado by remember { mutableStateOf("") }
    var cantidadEditada by remember { mutableStateOf("") }

    val ingredientes by viewModel.ingredientes.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(primario)
            .padding(16.dp)
    ) {

        Text(
            text = "My Pantry",
            style = MaterialTheme.typography.titleLarge,
            fontSize = 26.sp,
            color = verde
        )

        Spacer(modifier = Modifier.height(18.dp))

        OutlinedTextField(
            value = nuevoIngrediente,
            onValueChange = { nuevoIngrediente = it },
            label = { Text("Ingredient", color = verde) },
            placeholder = { Text("Example: Tomato") },
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Add", tint = verde)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = naranja,
                unfocusedBorderColor = verde,
                cursorColor = naranja
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = nuevaCantidad,
            onValueChange = { nuevaCantidad = it },
            label = { Text("Quantity", color = verde) },
            placeholder = { Text("Example: 2 kg, 3 pieces...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = naranja,
                unfocusedBorderColor = verde,
                cursorColor = naranja
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (nuevoIngrediente.text.isNotBlank() && nuevaCantidad.text.isNotBlank()) {
                    viewModel.agregarIngrediente(
                        nuevoIngrediente.text.trim(),
                        nuevaCantidad.text.trim()
                    )
                    nuevoIngrediente = TextFieldValue("")
                    nuevaCantidad = TextFieldValue("")
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = naranja,
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Ingredient")
        }

        Spacer(modifier = Modifier.height(22.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(ingredientes) { ingrediente ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(secundario)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(naranja.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ingrediente.nombre.take(1).uppercase(),
                            color = naranja,
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ingrediente.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            color = verde
                        )
                        Text(
                            text = ingrediente.cantidad,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }

                    IconButton(
                        onClick = {
                            ingredienteAEditar = ingrediente
                            textoEditado = ingrediente.nombre
                            cantidadEditada = ingrediente.cantidad
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit ingredient",
                            tint = verde
                        )
                    }

                    IconButton(onClick = { viewModel.eliminarIngrediente(ingrediente) }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red
                        )
                    }
                }
            }
        }

        if (ingredientes.isEmpty()) {
            Text(
                text = "Your pantry is empty",
                color = verde,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 20.dp)
            )
        }
    }

    if (ingredienteAEditar != null) {
        AlertDialog(
            onDismissRequest = { ingredienteAEditar = null },
            title = { Text("Edit Ingredient") },
            text = {
                Column {
                    TextField(
                        value = textoEditado,
                        onValueChange = { textoEditado = it },
                        placeholder = { Text("New name") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = cantidadEditada,
                        onValueChange = { cantidadEditada = it },
                        placeholder = { Text("Quantity") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (textoEditado.isNotBlank() && cantidadEditada.isNotBlank()) {
                        viewModel.editarIngrediente(
                            ingredienteAEditar!!,
                            textoEditado.trim(),
                            cantidadEditada.trim()
                        )
                    }
                    ingredienteAEditar = null
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { ingredienteAEditar = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
