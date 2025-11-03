package com.example.smartkitchenassistant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.example.smartkitchenassistant.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onClickLogout: () -> Unit = {}) {
    val auth = Firebase.auth
    val user = auth.currentUser

    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf(
        BottomNavItem("Buscar recetas", Icons.Default.Search),
        BottomNavItem("Agregar ingredientes", Icons.Default.Favorite),
        BottomNavItem("Mi despensa", Icons.AutoMirrored.Filled.List),
        BottomNavItem("Recomendaciones", Icons.Default.Star),
        BottomNavItem("Mi perfil", Icons.Default.Person)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = user?.email ?: "invitado",
                        fontSize = 18.sp
                    )
                },
                actions = {
                    IconButton(onClick = {
                        auth.signOut()
                        onClickLogout()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Color(0xFFF2EAD3),
                    titleContentColor = Color.Black
                )
            )
        },
        containerColor = Color(0xFFF9F5F0),
        bottomBar = {
            NavigationBar(containerColor = Color(0xFFF2EAD3)) {
                tabs.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title, fontSize = 12.sp) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Color(0xFFF9F5F0))
        ) {
            when(selectedTab) {
                0 -> BuscarRecetasScreen()
                1 -> AgregarIngredientesScreen()
                2 -> MiDespensaScreen()
                3 -> RecomendacionesScreen()
                4 -> MiPerfilScreen()
            }
        }
    }
}

data class BottomNavItem(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
