package com.example.smartkitchenassistant

import BuscarRecetasScreen
import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.example.smartkitchenassistant.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onClickLogout: () -> Unit = {}) {
    val auth = Firebase.auth
    val user = auth.currentUser

    var userName by remember {
        mutableStateOf<String?>(null)
    }

    var selectedTab by remember { mutableIntStateOf(0) }

    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            db.collection("usuarios")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userName = document.getString("nombre")
                    }
                }
        }
    }

    val tabs = listOf(
        BottomNavItem("Buscar", Icons.Default.Search),
        BottomNavItem("Favoritas", Icons.Default.Favorite),
        BottomNavItem("Despensa", Icons.AutoMirrored.Filled.List),
        BottomNavItem("Recomendaciones", Icons.Default.Star),
        BottomNavItem("Perfil", Icons.Default.Person)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = userName ?: user?.email ?: "invitado",
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
                            contentDescription = "Cerrar sesión",
                            tint = Color(0xFF344F1F)
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

        // 🔹 BARRA INFERIOR SOLO CON ICONOS (SIN EFECTO MORADO)
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                        clip = false
                    ),
                containerColor = Color(0xFFF2EAD3),
                tonalElevation = 10.dp
            ) {
                tabs.forEachIndexed { index, item ->
                    val selected = selectedTab == index

                    NavigationBarItem(
                        icon = {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selected) Color(0xFFF4991A).copy(alpha = 0.15f)
                                        else Color.Transparent
                                    )
                                    // 🔸 Sin ripple ni sombra morada
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) { selectedTab = index }
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = if (selected) Color(0xFFF4991A) else Color(0xFF344F1F),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        },
                        selected = selected,
                        onClick = { selectedTab = index },
                        label = null,
                        alwaysShowLabel = false,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent,
                            selectedIconColor = Color(0xFFF4991A),
                            unselectedIconColor = Color(0xFF344F1F)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF9F5F0))
        ) {
            when (selectedTab) {
                0 -> BuscarRecetasScreen()
                1 -> FavoritosScreen()
                2 -> MiDespensaScreen()
                3 -> RecomendacionesScreen()
                4 -> MiPerfilScreen()
            }
        }
    }
}

data class BottomNavItem(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
