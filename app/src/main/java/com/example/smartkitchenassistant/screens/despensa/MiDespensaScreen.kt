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

@Composable
fun DespensaScreen(viewModel: DespensaViewModel = viewModel()) {

    val primario = Color(0xFFF9F5F0)
    val secundario = Color(0xFFF2EAD3)
    val naranja = Color(0xFFF4991A)
    val verde = Color(0xFF344F1F)

    var nuevoIngrediente by remember { mutableStateOf(TextFieldValue("")) }
    var ingredienteAEditar by remember { mutableStateOf<String?>(null) }
    var textoEditado by remember { mutableStateOf("") }

    val ingredientes by viewModel.ingredientes.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(primario)
            .padding(16.dp)
    ) {

        Text(
            text = "Mi despensa",
            style = MaterialTheme.typography.titleLarge,
            fontSize = 26.sp,
            color = verde
        )

        Spacer(modifier = Modifier.height(18.dp))

        OutlinedTextField(
            value = nuevoIngrediente,
            onValueChange = { nuevoIngrediente = it },
            label = { Text("Agregar ingrediente...", color = verde) },
            placeholder = { Text("Ejemplo: tomate") },
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Agregar", tint = verde)
            },
            modifier = Modifier
                .fillMaxWidth()
                .background(secundario),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = naranja,
                unfocusedBorderColor = verde,
                cursorColor = naranja
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (nuevoIngrediente.text.isNotBlank()) {
                    viewModel.agregarIngrediente(nuevoIngrediente.text.trim())
                    nuevoIngrediente = TextFieldValue("")
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = naranja,
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Agregar ingrediente")
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
                            text = ingrediente.take(1).uppercase(),
                            color = naranja,
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = ingrediente,
                        style = MaterialTheme.typography.titleMedium,
                        color = verde,
                        modifier = Modifier.weight(1f)
                    )

                    // Botón editar (lápiz)
                    IconButton(
                        onClick = {
                            ingredienteAEditar = ingrediente
                            textoEditado = ingrediente
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar ingrediente",
                            tint = verde
                        )
                    }

                    // Botón eliminar
                    IconButton(onClick = { viewModel.eliminarIngrediente(ingrediente) }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color.Red
                        )
                    }
                }
            }
        }

        if (ingredientes.isEmpty()) {
            Text(
                text = "Tu despensa está vacía",
                color = verde,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 20.dp)
            )
        }
    }

    // Diálogo para editar ingrediente
    if (ingredienteAEditar != null) {
        AlertDialog(
            onDismissRequest = { ingredienteAEditar = null },
            title = { Text("Editar ingrediente") },
            text = {
                TextField(
                    value = textoEditado,
                    onValueChange = { textoEditado = it },
                    placeholder = { Text("Nuevo nombre") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (textoEditado.isNotBlank()) {
                        viewModel.editarIngrediente(
                            ingredienteAEditar!!,
                            textoEditado.trim()
                        )
                    }
                    ingredienteAEditar = null
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { ingredienteAEditar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
