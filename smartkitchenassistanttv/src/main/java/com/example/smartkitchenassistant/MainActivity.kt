package com.example.smartkitchenassistant

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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

        if (currentUser == null) {
            startActivity(Intent(this, TVLoginActivity::class.java))
            finish()
            return
        }

        val uid = currentUser.uid
        val recipeState = mutableStateOf<Recipe?>(null)

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

                    Button(
                        onClick = {
                            auth.signOut()
                            context.startActivity(Intent(context, TVLoginActivity::class.java))
                            (context as Activity).finish()
                        },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Text("Cerrar sesión", color = TextoOscuro)
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

// -----------------------------
// TEMPORIZADOR FUNCIONAL POR PASO
// -----------------------------
@Composable
fun PasoTimer(minutes: Int, TextoOscuro: Color, Acento: Color) {
    var remaining by remember { mutableStateOf(minutes * 60) }
    var running by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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
            modifier = Modifier.width(300.dp), // ancho fijo, no ocupa toda la pantalla
            colors = androidx.tv.material3.ButtonDefaults.colors(
                containerColor = Acento,   // Fondo naranja
                contentColor = Color.White  // Texto blanco
            )
        ) {
            Text(
                text = if (!running) "Iniciar temporizador ($minutes min)" else "Temporizador en curso",
                style = MaterialTheme.typography.titleLarge
            )
        }

        if (running || remaining != minutes * 60) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Tiempo restante: ${remaining / 60}m ${remaining % 60}s",
                style = MaterialTheme.typography.bodyLarge,
                color = Acento
            )
        }
    }
}


// -----------------------------
// PANTALLA PRINCIPAL DE LA RECETA
// -----------------------------
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
                "Esperando receta…",
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

        item {
            Text(
                "SmartKitchenAssistant - TV",
                style = MaterialTheme.typography.headlineLarge,
                color = Acento
            )
        }

        item {
            AsyncImage(
                model = recipe.image,
                contentDescription = recipe.title,
                modifier = Modifier
                    .height(300.dp)
                    .fillMaxWidth()
            )
        }

        item {
            Column {
                Text(
                    recipe.title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextoOscuro
                )
                Text(
                    "Categoría: ${recipe.category}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Acento
                )
            }
        }

        item {
            Text(
                "Ingredientes",
                style = MaterialTheme.typography.titleLarge,
                color = TextoOscuro
            )
        }

        items(recipe.ingredients) { ing ->
            Text(
                "• $ing",
                style = MaterialTheme.typography.bodyLarge,
                color = TextoOscuro,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        item {
            Text(
                "Pasos",
                style = MaterialTheme.typography.titleLarge,
                color = TextoOscuro
            )
        }

        // -----------------------------
        // PASOS + TEMPORIZADOR
        // -----------------------------
        items(recipe.steps.size) { index ->
            val paso = recipe.steps[index]
            val timeRegex = Regex("(\\d{1,3})\\s*(min|mins|minutes|minutos)")
            val match = timeRegex.find(paso)
            val minutosDetectados = match?.groupValues?.get(1)?.toIntOrNull()

            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    "${index + 1}. $paso",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextoOscuro
                )

                if (minutosDetectados != null) {
                    PasoTimer(
                        minutes = minutosDetectados,
                        TextoOscuro = TextoOscuro,
                        Acento = Acento
                    )
                }
            }
        }
    }
}
