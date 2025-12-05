package com.example.smartkitchenassistant.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiPerfilScreen() {
    val auth = Firebase.auth
    val user = auth.currentUser
    val db = FirebaseFirestore.getInstance()

    var nombreUsuario by remember { mutableStateOf("") }
    var nombreCompleto by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf(user?.email ?: "") }
    var telefono by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fotoPerfilUrl by remember { mutableStateOf<String?>(null) }
    var mensajeGuardado by remember { mutableStateOf("") }
    var modoEdicion by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }

    val launcherGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            cargando = true
            subirImagenCloudinary(uri) { url ->
                if (url != null) {
                    fotoPerfilUrl = url

                    user?.uid?.let { uid ->
                        db.collection("usuarios").document(uid)
                            .update("fotoPerfilUrl", url)
                            .addOnSuccessListener {
                                mensajeGuardado = "Photo updated ✔"
                            }
                    }
                } else {
                    mensajeGuardado = "Error uploading image"
                }
                cargando = false
            }
        }
    }

    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            db.collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        nombreUsuario = doc.getString("nombreUsuario") ?: ""
                        nombreCompleto = doc.getString("nombreCompleto") ?: ""
                        telefono = doc.getString("telefono") ?: ""
                        descripcion = doc.getString("descripcion") ?: ""
                        fotoPerfilUrl = doc.getString("fotoPerfilUrl")
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            if (fotoPerfilUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(fotoPerfilUrl),
                    contentDescription = "Profile photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("PHOTO", fontSize = 16.sp, color = Color.DarkGray)
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            OutlinedButton(onClick = { launcherGaleria.launch("image/*") }) {
                Text("Upload")
            }

            OutlinedButton(onClick = { launcherGaleria.launch("image/*") }) {
                Text("Update")
            }

            OutlinedButton(
                onClick = {
                    fotoPerfilUrl = null

                    user?.uid?.let { uid ->
                        db.collection("usuarios")
                            .document(uid)
                            .update("fotoPerfilUrl", null)
                        mensajeGuardado = "Photo deleted ✔"
                    }
                }
            ) {
                Text("Delete")
            }
        }

        if (cargando) {
            Spacer(modifier = Modifier.height(10.dp))
            CircularProgressIndicator()
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Profile Information",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(15.dp))

                if (!modoEdicion) {
                    PerfilDatoItem("Username", nombreUsuario)
                    PerfilDatoItem("Full name", nombreCompleto)
                    PerfilDatoItem("Email", correo)
                    PerfilDatoItem("Phone", telefono)
                    PerfilDatoItem("Description", descripcion)
                } else {
                    OutlinedTextField(
                        value = nombreUsuario,
                        onValueChange = { nombreUsuario = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = nombreCompleto,
                        onValueChange = { nombreCompleto = it },
                        label = { Text("Full name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = correo,
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Phone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Short description") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (!modoEdicion) {
            Button(
                onClick = { modoEdicion = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit profile")
            }
        } else {
            Button(
                onClick = {
                    user?.uid?.let { uid ->
                        val datosActualizados = mapOf(
                            "nombreUsuario" to nombreUsuario,
                            "nombreCompleto" to nombreCompleto,
                            "telefono" to telefono,
                            "descripcion" to descripcion,
                            "fotoPerfilUrl" to fotoPerfilUrl
                        )
                        db.collection("usuarios").document(uid)
                            .update(datosActualizados)
                            .addOnSuccessListener {
                                mensajeGuardado = "Profile updated successfully ✔"
                                modoEdicion = false
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save changes")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { modoEdicion = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }

        Spacer(modifier = Modifier.height(15.dp))
        if (mensajeGuardado.isNotEmpty()) {
            Text(
                mensajeGuardado,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PerfilDatoItem(titulo: String, valor: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(titulo, fontSize = 13.sp, color = Color.Gray)
        Text(
            valor.ifEmpty { "No information" },
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF222222)
        )
        Divider(
            modifier = Modifier.padding(top = 10.dp),
            thickness = 0.7.dp,
            color = Color.LightGray
        )
    }
}

fun subirImagenCloudinary(
    uri: Uri,
    onResult: (String?) -> Unit
) {
    val requestId = MediaManager.get().upload(uri)
        .unsigned("perfil_preset")
        .callback(object : UploadCallback {
            override fun onStart(requestId: String?) {}
            override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
            override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                val url = resultData?.get("secure_url") as? String
                onResult(url)
            }
            override fun onError(requestId: String?, error: ErrorInfo?) {
                onResult(null)
            }
            override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
        })
        .dispatch()
}
