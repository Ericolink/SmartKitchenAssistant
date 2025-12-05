package com.example.smartkitchenassistant

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TVMainActivity : ComponentActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // Si no hay usuario → Ir al login
        if (currentUser == null) {
            startActivity(Intent(this, TVLoginActivity::class.java))
            finish()
            return
        }

        val uid = currentUser.uid
        val recipeState = mutableStateOf<Recipe?>(null)

        //  Listener Firestore
        listener = db.collection("usuarios")
            .document(uid)
            .collection("recetas")
            .document("actual")
            .addSnapshotListener { snapshot, _ ->
                recipeState.value = snapshot?.toObject(Recipe::class.java)
            }

        setContent {

            val Fondo = Color(0xFFF9F5F0)
            val Acento = Color(0xFFF4991A)
            val TextoOscuro = Color(0xFF344F1F)
            val context = LocalContext.current

            MaterialTheme {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Fondo),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {

                    // Pantalla con scroll
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        TVRecipeScreenStyled(
                            recipe = recipeState.value,
                            Acento = Acento,
                            TextoOscuro = TextoOscuro
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    //  BOTÓN CERRAR SESIÓN — con highlight de foco
                    val logoutSource = remember { MutableInteractionSource() }
                    val logoutFocused by logoutSource.collectIsFocusedAsState()

                    Button(
                        onClick = {
                            auth.signOut()
                            context.startActivity(Intent(context, TVLoginActivity::class.java))
                            (context as Activity).finish()
                        },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .focusable(interactionSource = logoutSource),
                        colors = ButtonDefaults.colors(
                            containerColor = if (logoutFocused) Color(0xFFCC8A00) else Acento,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Sign Out")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        listener?.remove()
        super.onDestroy()
    }
}

// TIMER CON FOCUS VISUAL
@Composable
fun PasoTimer(minutes: Int, TextoOscuro: Color, Acento: Color) {

    var remaining by remember { mutableStateOf(minutes * 60) }
    var running by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    //  Control de foco para el botón
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()

    Column(modifier = Modifier.padding(start = 16.dp, top = 8.dp)) {

        Button(
            onClick = {
                if (!running) {
                    running = true
                    scope.launch {
                        while (remaining > 0) {
                            delay(1000)
                            remaining--
                        }
                        running = false
                    }
                }
            },
            modifier = Modifier
                .width(320.dp)
                .focusable(interactionSource = interaction),
            colors = ButtonDefaults.colors(
                containerColor = if (focused) Color(0xFFCC8A00) else Acento,
                contentColor = Color.White
            )
        ) {
            Text(
                text = if (!running) "Start timer ($minutes min)" else "Timer running",
                style = MaterialTheme.typography.titleLarge
            )
        }

        if (running || remaining != minutes * 60) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Time remaining: ${remaining / 60}m ${remaining % 60}s",
                style = MaterialTheme.typography.bodyLarge,
                color = Acento
            )
        }
    }
}


//  PANTALLA PRINCIPAL — SCROLL + FOCUS + HIGHLIGHT
@Composable
fun TVRecipeScreenStyled(
    recipe: Recipe?,
    Acento: Color,
    TextoOscuro: Color
) {

    if (recipe == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Waiting for recipe…",
                style = MaterialTheme.typography.headlineLarge,
                color = TextoOscuro
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        // Título
        item {
            FocusableText(
                text = "SmartKitchenAssistant - TV",
                style = MaterialTheme.typography.headlineLarge,
                normalColor = Acento,
                focusedColor = Color(0xFFCC8A00)
            )
        }

        // Imagen
        item {
            AsyncImage(
                model = recipe.image,
                contentDescription = recipe.title,
                modifier = Modifier
                    .height(300.dp)
                    .fillMaxWidth()
                    .focusable()
            )
        }

        // Título + categoría
        item {
            FocusableColumn {
                Text(
                    recipe.title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextoOscuro
                )
                Text(
                    "Category: ${recipe.category}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Acento
                )
            }
        }

        // Ingredientes
        item {
            FocusableText(
                text = "Ingredients",
                style = MaterialTheme.typography.titleLarge,
                normalColor = TextoOscuro
            )
        }

        items(recipe.ingredients) { ing ->
            FocusableText(
                text = "• $ing",
                style = MaterialTheme.typography.bodyLarge,
                normalColor = TextoOscuro
            )
        }

        // Pasos
        item {
            FocusableText(
                text = "Steps",
                style = MaterialTheme.typography.titleLarge,
                normalColor = TextoOscuro
            )
        }

        items(recipe.steps.size) { index ->
            val paso = recipe.steps[index]

            val regex = Regex("(\\d{1,3})\\s*(min|mins|minutes|minutos)")
            val minutesFound = regex.find(paso)?.groupValues?.get(1)?.toIntOrNull()

            Column(modifier = Modifier.padding(start = 16.dp)) {

                FocusableText(
                    text = "${index + 1}. $paso",
                    style = MaterialTheme.typography.bodyLarge,
                    normalColor = TextoOscuro
                )

                if (minutesFound != null) {
                    PasoTimer(
                        minutes = minutesFound,
                        TextoOscuro = TextoOscuro,
                        Acento = Acento
                    )
                }
            }
        }
    }
}

// COMPONENTE PARA TEXTOS CON EFECTO DE FOCO

@Composable
fun FocusableText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    normalColor: Color,
    focusedColor: Color = Color(0xFF000000)
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()

    Text(
        text = text,
        style = style,
        color = if (focused) focusedColor else normalColor,
        modifier = Modifier.focusable(interactionSource = interaction)
    )
}

// COMPONENTE PARA COLUMNAS FOCUSABLE
@Composable
fun FocusableColumn(content: @Composable () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()

    Column(
        modifier = Modifier
            .focusable(interactionSource = interaction)
            .background(if (focused) Color(0x33CC8A00) else Color.Transparent)
            .padding(4.dp)
    ) {
        content()
    }
}
