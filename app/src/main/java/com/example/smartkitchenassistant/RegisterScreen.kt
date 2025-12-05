package com.example.smartkitchenassistant

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

val Primario = Color(0xFFF9F5F0)
val Secundario = Color(0xFFF2EAD3)
val naranja = Color(0xFFF4991A)
val verde = Color(0xFF344F1F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onClickBack: () -> Unit = {},
    onSuccessfulRegister: () -> Unit = {}
) {

    val auth = Firebase.auth
    val activity = LocalView.current.context as Activity
    val db = Firebase.firestore

    var inputName by remember { mutableStateOf("") }
    var inputEmail by remember { mutableStateOf("") }
    var inputPassword by remember { mutableStateOf("") }
    var inputPasswordConfirmation by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var passwordConfirmationError by remember { mutableStateOf("") }

    var registerError by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onClickBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "back",
                            tint = verde
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primario
                )
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Primario)
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(10.dp))

            Image(
                painter = painterResource(id = R.drawable.logo1),
                contentDescription = "logo",
                modifier = Modifier.size(160.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Register",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = verde
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(Secundario),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {

                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    ModernInput(
                        label = "Username",
                        value = inputName,
                        onValueChange = { inputName = it },
                        icon = Icons.Default.Person,
                        error = nameError
                    )

                    ModernInput(
                        label = "Email",
                        value = inputEmail,
                        onValueChange = { inputEmail = it },
                        icon = Icons.Default.Email,
                        error = emailError
                    )

                    ModernInput(
                        label = "Password",
                        value = inputPassword,
                        onValueChange = { inputPassword = it },
                        icon = Icons.Default.Lock,
                        isPassword = true,
                        error = passwordError
                    )

                    ModernInput(
                        label = "Confirm password",
                        value = inputPasswordConfirmation,
                        onValueChange = { inputPasswordConfirmation = it },
                        icon = Icons.Default.Lock,
                        isPassword = true,
                        error = passwordConfirmationError
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (registerError.isNotEmpty())
                Text(registerError, color = Color.Red, fontSize = 14.sp)

            if (successMessage.isNotEmpty())
                Text(successMessage, color = verde, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {

                    val isValidName = validateName(inputName).first
                    val isValidEmail = validateEmail(inputEmail).first
                    val isValidPassword = validatePassword(inputPassword).first
                    val isValidConfirmPassword = validateConfirmPassword(inputPassword, inputPasswordConfirmation).first

                    nameError = validateName(inputName).second
                    emailError = validateEmail(inputEmail).second
                    passwordError = validatePassword(inputPassword).second
                    passwordConfirmationError = validateConfirmPassword(inputPassword, inputPasswordConfirmation).second

                    if (isValidName && isValidEmail && isValidPassword && isValidConfirmPassword) {
                        auth.createUserWithEmailAndPassword(inputEmail, inputPassword)
                            .addOnCompleteListener(activity) { task ->
                                if (task.isSuccessful) {

                                    val user = auth.currentUser
                                    val uid = user?.uid

                                    if (uid != null) {
                                        val userData = hashMapOf(
                                            "nombreUsuario" to inputName,
                                            "correo" to inputEmail,
                                            "fechaRegistro" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                        )
                                        db.collection("usuarios")
                                            .document(uid)
                                            .set(userData)
                                    }

                                    user?.sendEmailVerification()
                                    successMessage = "Registration successful. Check your email to verify your account."
                                    auth.signOut()

                                } else {
                                    registerError = when (task.exception) {
                                        is FirebaseAuthInvalidCredentialsException -> "Invalid email"
                                        is FirebaseAuthUserCollisionException -> "Email already registered"
                                        else -> "Registration error"
                                    }
                                }
                            }

                    } else {
                        registerError = "Fill in all fields correctly."
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(naranja),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Sign up",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false,
    error: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = verde
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        supportingText = {
            if (error.isNotEmpty()) Text(error, color = Color.Red)
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = naranja,
            unfocusedBorderColor = verde.copy(alpha = 0.5f),
            focusedLabelColor = naranja,
            unfocusedLabelColor = verde
        )
    )
}
