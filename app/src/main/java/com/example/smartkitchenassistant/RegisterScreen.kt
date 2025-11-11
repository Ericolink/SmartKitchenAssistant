package com.example.smartkitchenassistant

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.handleCoroutineException
import kotlin.math.sign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen (onClickBack : ()-> Unit = {}, onSuccessfulRegister : ()-> Unit = {}) {

    val auth = Firebase.auth
    val activity = LocalView.current.context as Activity
    val context = LocalContext.current

    //ESTADOS DE LOS IMPUT

    var inputName by remember {
        mutableStateOf("")
    }
    var inputEmail by remember {
        mutableStateOf("")
    }
    var inputPassword by remember {
        mutableStateOf("")
    }
    var inputPasswordConfirmation by remember {
        mutableStateOf("")
    }

    var nameError by remember {
        mutableStateOf("")
    }
    var emailError by remember {
        mutableStateOf("")
    }
    var passwordError by remember {
        mutableStateOf("")
    }
    var passwordConfirmationError by remember {
        mutableStateOf("")
    }

    var registerError by remember {
        mutableStateOf("")
    }

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()
    val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            handleGoogleRegister(account, auth, onSuccessfulRegister, { err ->
                registerError = err
            })
        } catch (e: Exception) {
            Log.d("RegisterScreen", "GoogleSignIn fallo: ${e.localizedMessage}")
            registerError = "Fallo el registro con Google."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                },
                navigationIcon = {
                    IconButton(onClick = onClickBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "icon register"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally )
        {
            // Logo de la app
            Image(
                painter = painterResource(id = R.drawable.logo1),
                contentDescription = "Register Image",
                modifier = Modifier.size(200.dp)
            )

            Text(
                text = "Registro",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color(0xFFFF9800) // Naranja
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo: Nombre
            OutlinedTextField(
                value = inputName,
                onValueChange = { inputName = it },
                label = { Text("Nombre") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Name Icon"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    if (nameError.isNotEmpty()){
                        Text(
                            text = nameError,
                            color = Color.Red
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo: Correo
            OutlinedTextField(
                value = inputEmail,
                onValueChange = { inputEmail = it },
                label = { Text("Email") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email Icon"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    if (emailError.isNotEmpty()){
                        Text(
                            text = emailError,
                            color = Color.Red
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo: Contraseña
            OutlinedTextField(
                value = inputPassword,
                onValueChange = { inputPassword = it },
                label = { Text("Contraseña") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password Icon"
                    )
                },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    if (passwordError.isNotEmpty()){
                        Text(
                            text = passwordError,
                            color = Color.Red
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo: Confirmar Contraseña
            OutlinedTextField(
                value = inputPasswordConfirmation,
                onValueChange = { inputPasswordConfirmation = it },
                label = { Text("Confirmar Contraseña") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password Confirmation Icon"
                    )
                },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    if (passwordConfirmationError.isNotEmpty()){
                        Text(
                            text = passwordConfirmationError,
                            color = Color.Red
                        )
                    }
                }
            )

            if (registerError.isNotEmpty()){
                Text(registerError, color = Color.Red)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botón de Registro
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

                    if (isValidName && isValidEmail && isValidPassword && isValidConfirmPassword){
                        /*auth.createUserWithEmailAndPassword(inputEmail, inputPassword).
                                addOnCompleteListener(activity) { task ->
                                    if (task.isSuccessful){
                                        onSuccessfulRegister()
                                    }else{
                                        registerError = when(task.isSuccessful){
                                            is FirebaseAuthInvalidCredentialsException -> "Correo invalido"
                                            is FirebaseAuthUserCollisionException -> "Correo ya registrado"
                                            else -> "Error al registrarse"
                                        }
                                    }
                                }*/
                        registerError = "Por favor verifica tu correo con Google para completar el registro."
                        val signInIntent = googleSignInClient.signInIntent
                        googleLauncher.launch(signInIntent)
                    }else{
                        registerError = "Hubo un error en el register"
                    }

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(text = "Registro")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val signInIntent = googleSignInClient.signInIntent
                        googleLauncher.launch(signInIntent)
                    }
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                Image(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Registrar con Google",
                    modifier = Modifier
                        .padding(10.dp)
                        .size(40.dp)
                )
                Text(text = "Registrarse con Google", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
private fun handleGoogleRegister(
    account: GoogleSignInAccount?,
    auth: FirebaseAuth,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    if (account == null) {
        onError("No se obtuvo la cuenta de Google.")
        return
    }
    try {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        // Intentamos iniciar sesión con la credencial: si es nuevo usuario, Firebase lo crea automáticamente.
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("RegisterScreen", "Registro con Google exitoso: ${account.email}")
                    onSuccess()
                } else {
                    Log.d("RegisterScreen", "Fallo auth con credencial Google: ${task.exception?.localizedMessage}")
                    onError("Error al registrarse con Google.")
                }
            }
            .addOnFailureListener {
                Log.d("RegisterScreen", "Exception en registro Google: ${it.localizedMessage}")
                onError("Fallo el registro con Google.")
            }
    } catch (ex: Exception) {
        Log.d("RegisterScreen", "Excepcion: ${ex.localizedMessage}")
        onError("Excepción al registrar con Google.")
    }
}