package com.example.smartkitchenassistant

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
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

class TVMainActivity : ComponentActivity() {

    // Instancia principal de Firestore
    private val db = FirebaseFirestore.getInstance()

    // Guarda el listener para poder quitarlo cuando la pantalla se destruya
    private var listener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // Si no hay usuario logueado, mandamos a la pantalla de login
        if (currentUser == null) {
            startActivity(Intent(this, TVLoginActivity::class.java))
            finish()
            return
        }

        val uid = currentUser.uid

        // Estado que almacenará la receta recibida desde Firestore
        val recipeState = mutableStateOf<Recipe?>(null)

        // Listener en tiempo real a Firestore para recibir la receta "actual"
        listener = db.collection("usuarios")
            .document(uid)
            .collection("recetas")
            .document("actual")
            .addSnapshotListener { snapshot, _ ->
                // Cuando cambia el documento, actualiza la receta en el estado
                recipeState.value =
                    if (snapshot != null && snapshot.exists())
                        snapshot.toObject(Recipe::class.java)
                    else
                        null
            }

        setContent {

            // Paleta de colores usada por la UI
            val Fondo = Color(0xFFF9F5F0)
            val Acento = Color(0xFFF4991A)
            val TextoOscuro = Color(0xFF344F1F)

            val context = LocalContext.current

            MaterialTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Fondo), // Fondo general, sin bordes ni padding exterior
                    verticalArrangement = Arrangement.SpaceBetween
                ) {

                    // Contenedor que ocupa la mayor parte de la pantalla
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        // Se dibuja la receta si existe, o el mensaje "Esperando receta"
                        TVRecipeScreenStyled(
                            recipe = recipeState.value,
                            Acento = Acento,
                            TextoOscuro = TextoOscuro
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Botón para cerrar sesión en la TV
                    Button(
                        onClick = {
                            auth.signOut()
                            context.startActivity(Intent(context, TVLoginActivity::class.java))
                            (context as Activity).finish()
                        },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .focusable() // Para navegación con control remoto
                    ) {
                        Text("Cerrar sesión", color = TextoOscuro)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        // Se elimina el listener cuando la activity se destruya
        listener?.remove()
        super.onDestroy()
    }
}

// UI de la pantalla de receta en TV
@Composable
fun TVRecipeScreenStyled(
    recipe: Recipe?,
    Acento: Color,
    TextoOscuro: Color
) {

    // Si aún no hay receta cargada, mostramos un mensaje centrado
    if (recipe == null) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Esperando receta…",
                style = MaterialTheme.typography.headlineLarge,
                color = TextoOscuro,
                modifier = Modifier.focusable()
            )
        }
        return
    }

    // Lista desplazable con toda la información de la receta
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        // Título superior de la app en pantalla
        item {
            Text(
                "Smart Display - TV",
                style = MaterialTheme.typography.headlineLarge,
                color = Acento,
                modifier = Modifier.focusable()
            )
        }

        // Imagen del platillo recibida desde Firebase
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

        // Nombre del platillo y categoría
        item {
            Column(Modifier.focusable()) {
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

        // Título de ingredientes
        item {
            Text(
                "Ingredientes",
                style = MaterialTheme.typography.titleLarge,
                color = TextoOscuro,
                modifier = Modifier.focusable()
            )
        }

        // Lista de ingredientes
        items(recipe.ingredients) { ing ->
            Text(
                "• $ing",
                style = MaterialTheme.typography.bodyLarge,
                color = TextoOscuro,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .focusable()
            )
        }

        // Título de pasos
        item {
            Text(
                "Pasos",
                style = MaterialTheme.typography.titleLarge,
                color = TextoOscuro,
                modifier = Modifier.focusable()
            )
        }

        // Lista de pasos numerados
        items(recipe.steps.size) { index ->
            Text(
                "${index + 1}. ${recipe.steps[index]}",
                style = MaterialTheme.typography.bodyLarge,
                color = TextoOscuro,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .focusable()
            )
        }
    }
}
