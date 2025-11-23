package com.example.smartkitchenassistant

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.auth.FirebaseAuth

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
                    if (snapshot != null && snapshot.exists()) snapshot.toObject(Recipe::class.java)
                    else null
            }

        setContent {
            val context = LocalContext.current

            Column {
                TVRecipeScreen(recipeState.value)

                Spacer(Modifier.height(20.dp))

                Button(onClick = {
                    auth.signOut()
                    context.startActivity(Intent(context, TVLoginActivity::class.java))
                    (context as Activity).finish()
                }) {
                    Text("Cerrar sesión")
                }
            }
        }

        fun onDestroy() {
            listener?.remove()
            super.onDestroy()
        }
    }

    @Composable
    fun TVRecipeScreen(recipe: Recipe?) {

        Column(
            Modifier
                .fillMaxSize()
                .padding(40.dp)
        ) {

            Text("SmartKitchenAssistant - TV", style = MaterialTheme.typography.headlineLarge)

            Spacer(Modifier.height(30.dp))

            if (recipe == null) {
                Text("Esperando receta…", style = MaterialTheme.typography.headlineMedium)
                return
            }

            // Imagen
            AsyncImage(
                model = recipe.image,
                contentDescription = recipe.title,
                modifier = Modifier
                    .height(260.dp)
                    .fillMaxWidth()
            )

            Spacer(Modifier.height(25.dp))

            Text(recipe.title, style = MaterialTheme.typography.headlineLarge)
            Text("Categoría: ${recipe.category}", style = MaterialTheme.typography.headlineMedium)

            Spacer(Modifier.height(25.dp))
            Text("Ingredientes", style = MaterialTheme.typography.titleLarge)

            recipe.ingredients.forEach { ing ->
                Text("• $ing", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(Modifier.height(25.dp))

            Text("Pasos", style = MaterialTheme.typography.titleLarge)
            recipe.steps.forEachIndexed { i, step ->
                Text("${i + 1}. $step", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
