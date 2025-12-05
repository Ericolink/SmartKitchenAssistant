package com.example.smartkitchenassistant

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    private val db = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // 🔥 Si no hay usuario → Ir a login
        if (currentUser == null) {
            startActivity(Intent(this, TVLoginActivity::class.java))
            finish()
            return
        }

        val uid = currentUser.uid

        // Estado REAL de Compose: se actualizará cuando Firestore cambie
        val recipeState = mutableStateOf<Recipe?>(null)

        // Listener Firestore 🔥
        listener = db.collection("usuarios")
            .document(uid)
            .collection("recetas")
            .document("actual")
            .addSnapshotListener { snapshot, _ ->
                recipeState.value =
                    if (snapshot != null && snapshot.exists())
                        snapshot.toObject(Recipe::class.java)
                    else
                        null
            }

        setContent {
            val context = LocalContext.current

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                // Contenido principal
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    TVRecipeScreen(recipeState.value)
                }

                Spacer(Modifier.height(16.dp))

                // Botón cerrar sesión
                Button(
                    onClick = {
                        auth.signOut()
                        context.startActivity(Intent(context, TVLoginActivity::class.java))
                        (context as Activity).finish()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .focusable()
                ) {
                    Text("Cerrar sesión")
                }
            }
        }
    }

    override fun onDestroy() {
        listener?.remove()
        super.onDestroy()
    }

    @Composable
    fun TVRecipeScreen(recipe: Recipe?) {

        // Caso 1: aún no hay receta en Firestore
        if (recipe == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Esperando receta…",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.focusable()
                )
            }
            return
        }

        // Caso 2: sí hay receta → usamos LazyColumn navegable
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            item {
                Text(
                    "SmartKitchenAssistant - TV",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.focusable()
                )
            }

            // Imagen
            item {
                AsyncImage(
                    model = recipe.image,
                    contentDescription = recipe.title,
                    modifier = Modifier
                        .height(260.dp)
                        .fillMaxWidth()
                        .focusable()
                )
            }

            // Título + categoría
            item {
                Column(Modifier.focusable()) {
                    Text(
                        recipe.title,
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Text(
                        "Categoría: ${recipe.category}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            // Ingredientes título
            item {
                Text(
                    "Ingredientes",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.focusable()
                )
            }

            // Ingredientes lista
            items(recipe.ingredients) { ing ->
                Text(
                    "• $ing",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .focusable()
                )
            }

            // Pasos título
            item {
                Text(
                    "Pasos",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.focusable()
                )
            }

            // Pasos lista
            items(recipe.steps.size) { index ->
                Text(
                    "${index + 1}. ${recipe.steps[index]}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .focusable()
                )
            }
        }
    }
}
