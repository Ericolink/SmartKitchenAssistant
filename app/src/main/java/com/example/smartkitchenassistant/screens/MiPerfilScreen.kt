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

    // 👉 Estados del perfil
    var nombreUsuario by remember { mutableStateOf("") }
    var nombreCompleto by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf(user?.email ?: "") }
    var telefono by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fotoPerfilUrl by remember { mutableStateOf<String?>(null) }
    var mensajeGuardado by remember { mutableStateOf("") }
    var modoEdicion by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }

    // 👉 Launcher para seleccionar imagen
    val launcherGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            cargando = true
            subirImagenCloudinary(uri) { url ->
                if (url != null) {
                    fotoPerfilUrl = url

                    // 👉 Guardamos AUTOMÁTICAMENTE en Firestore
                    user?.uid?.let { uid ->
                        db.collection("usuarios").document(uid)
                            .update("fotoPerfilUrl", url)
                            .addOnSuccessListener {
                                mensajeGuardado = "Foto actualizada ✔"
                            }
                    }
                } else {
                    mensajeGuardado = "Error al subir la imagen"
                }
                cargando = false
            }
        }
    }

    // 👉 Cargar datos al entrar
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
        // 👉 FOTO DE PERFIL
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
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("FOTO", fontSize = 16.sp, color = Color.DarkGray)
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // 👉 BOTÓN SUBIR FOTO
            OutlinedButton(
                onClick = { launcherGaleria.launch("image/*") }
            ) {
                Text("Subir foto")
            }

            // 👉 BOTÓN ACTUALIZAR FOTO
            OutlinedButton(
                onClick = { launcherGaleria.launch("image/*") }
            ) {
                Text("Actualizar")
            }

            // 👉 BOTÓN ELIMINAR FOTO
            OutlinedButton(
                onClick = {
                    fotoPerfilUrl = null

                    user?.uid?.let { uid ->
                        db.collection("usuarios")
                            .document(uid)
                            .update("fotoPerfilUrl", null)
                        mensajeGuardado = "Foto eliminada ✔"
                    }
                }
            ) {
                Text("Eliminar")
            }
        }

        if (cargando) {
            Spacer(modifier = Modifier.height(10.dp))
            CircularProgressIndicator()
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 👉 TARJETA PROFESIONAL DE PERFIL
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Información del Perfil",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(15.dp))

                if (!modoEdicion) {
                    // MODO LECTURA
                    PerfilDatoItem("Nombre de usuario", nombreUsuario)
                    PerfilDatoItem("Nombre completo", nombreCompleto)
                    PerfilDatoItem("Correo", correo)
                    PerfilDatoItem("Teléfono", telefono)
                    PerfilDatoItem("Descripción", descripcion)
                } else {
                    // MODO EDICIÓN
                    OutlinedTextField(
                        value = nombreUsuario,
                        onValueChange = { nombreUsuario = it },
                        label = { Text("Nombre de usuario") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = nombreCompleto,
                        onValueChange = { nombreCompleto = it },
                        label = { Text("Nombre completo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = correo,
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Correo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción breve") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 👉 BOTÓNES
        if (!modoEdicion) {
            Button(
                onClick = { modoEdicion = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Editar datos")
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
                                mensajeGuardado = "Perfil actualizado correctamente ✔"
                                modoEdicion = false
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar cambios")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { modoEdicion = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
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
            valor.ifEmpty { "Sin información" },
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

// 👉 FUNCIÓN PARA SUBIR IMAGEN A CLOUDINARY
fun subirImagenCloudinary(
    uri: Uri,
    onResult: (String?) -> Unit
) {
    val requestId = MediaManager.get().upload(uri)
        .unsigned("perfil_preset") // tu preset de Cloudinary
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
