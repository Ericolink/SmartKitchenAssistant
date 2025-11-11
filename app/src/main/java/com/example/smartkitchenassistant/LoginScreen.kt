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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.ActivityNavigatorExtras
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.play.integrity.internal.ac
import com.google.firebase.auth.GoogleAuthProvider
import kotlin.contracts.contract

@Composable
fun loginScreen(onClickRegister : ()-> Unit = {}, onSuccessfulLogin : ()-> Unit = {}){

    val auth = Firebase.auth
    val activity = LocalView.current.context as Activity
    val context = LocalContext.current

    //ESTADOS
    var inputEmail by remember {
        mutableStateOf("")
    }
    var inputPassword by remember {
        mutableStateOf("")
    }
    var loginError by remember {
        mutableStateOf("")
    }
    var emailError by remember {
        mutableStateOf("")
    }
    var passwordError by remember {
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
            if (account != null && account.email != null) {
                auth.fetchSignInMethodsForEmail(account.email!!)
                    .addOnCompleteListener { fetchTask ->
                        if (fetchTask.isSuccessful) {
                            val signInMethods = fetchTask.result?.signInMethods
                            if (signInMethods == null || signInMethods.isEmpty()) {
                                loginError = "Debes registrarte primero antes de iniciar sesion con Google."
                                googleSignInClient.signOut()
                            } else {
                                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                                auth.signInWithCredential(credential)
                                    .addOnCompleteListener(activity) { task ->
                                        if (task.isSuccessful) {
                                            onSuccessfulLogin()
                                        } else {
                                            loginError = "Error al inicar sesion con Google"
                                        }
                                    }
                                    .addOnFailureListener {
                                        loginError = "Fallo el autenticar con Google"
                                    }
                            }
                        } else {
                            loginError = "Error al verificar el correo en Firebase."
                        }
                    }
            } else {
                loginError = "No se obtuvo el correo de Google."
            }
        } catch (ex: Exception) {
            Log.d("LoginScreen", "GoogleSignIn fallo: ${ex.localizedMessage}")
            loginError = "Error en Google Sign-In"
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo1),
                contentDescription = "Login Image",
                modifier = Modifier.size(200.dp)
            )
            Text(text = "Bienvenido", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Inicia sesion en tu cuenta")

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = inputEmail,
                onValueChange = { inputEmail = it },
                label = {
                    Text(text = "Correo Electronico")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email Icon"
                    )
                },
                supportingText = {
                    if (emailError.isNotEmpty()){
                        Text(
                            text = emailError,
                            color = Color.Red
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrect = false,
                    keyboardType = KeyboardType.Email
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = inputPassword,
                onValueChange = { inputPassword = it },
                label = {
                    Text(text = "Contraseña")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password Icon"
                    )
                },
                supportingText = {
                    if (passwordError.isNotEmpty()){
                        Text(
                            text = passwordError,
                            color = Color.Red
                        )
                    }
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrect = false,
                    keyboardType = KeyboardType.Password
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (loginError.isNotEmpty()){
                Text(
                    loginError,
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(bottom = 8.dp)
                )
            }

            Button(onClick = {

                val isValidEmail: Boolean = validateEmail(inputEmail).first
                val isValidPassword = validatePassword(inputPassword).first

                emailError = validateEmail(inputEmail).second
                passwordError = validatePassword(inputPassword).second

                if (isValidEmail && isValidPassword){
                    auth.signInWithEmailAndPassword(inputEmail, inputPassword)
                        .addOnCompleteListener(activity) { task ->
                            if (task.isSuccessful) {
                                onSuccessfulLogin()
                            } else {
                                loginError = when(task.exception){
                                    is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrecta"
                                    is FirebaseAuthInvalidUserException -> "No existe una cuenta con este correo"
                                    else -> "Error al iniciar sesión. Intenta de nuevo"
                                }
                            }
                        }
                }else {

                }

            }) {
                Text(text = "Iniciar Sesion")
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onClickRegister) {
                Text("¿No tienes una cuenta? Registrate")
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { }) {
                Text(text = "Olvide mi contraseña", modifier = Modifier.clickable {

                })
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .clickable {
                        val signInIntent = googleSignInClient.signInIntent
                        googleLauncher.launch(signInIntent)
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            )
            {
                Image(
                    painter = painterResource(id = R.drawable.ic_google) ,
                    contentDescription = "Logo con Google" ,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(40.dp)
                )
                Text(text = "Login con Google",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold)
            }
        }
    }

}