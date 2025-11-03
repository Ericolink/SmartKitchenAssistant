package com.example.smartkitchenassistant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onClickLogout: () -> Unit = {}) {
    val auth = Firebase.auth
    val user = auth.currentUser

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Kitchen Assistant") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Activar asistente de voz */ }) {
                Text("🎤")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Saludo
            item {
                Column {
                    Text(
                        text = "¡Hola, ${user?.email ?: "invitado"}! 👋",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "¿Qué te gustaría hacer hoy?",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Secciones principales
            item {
                HomeCard("Ver recetas") { /* Navegar a recetas */ }
            }
            item {
                HomeCard("Dispositivos conectados") { /* Navegar a dispositivos */ }
            }
            item {
                HomeCard("Temporizador de cocina") { /* Navegar a temporizador */ }
            }
            item {
                HomeCard("Consejos del día") { /* Navegar a consejos */ }
            }

            // Botón cerrar sesión
            item {
                Button(
                    onClick = {
                        auth.signOut()
                        onClickLogout()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text("Cerrar sesión")
                }
            }
        }
    }
}

@Composable
fun HomeCard(title: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
