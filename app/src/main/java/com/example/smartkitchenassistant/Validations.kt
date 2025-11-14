package com.example.smartkitchenassistant

import android.R
import android.util.Patterns
import org.intellij.lang.annotations.Pattern

//returnar un true si es valido y un false si no es valido
//tambien retorne una cadena que diga que pasa si no es valido

fun validateEmail(email: String): Pair<Boolean, String>{
    return when{
        email.isEmpty() -> Pair(false, "El correo es requerido.")
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> Pair(false, "El correo es invalido")
        !email.endsWith("@gmail.com")-> Pair(false, "Ese email no es corporativo.")
        else -> Pair (true, "")
    }
}

fun validatePassword(password:String): Pair<Boolean, String>{
    return  when{
        password.isEmpty() -> Pair(false, "La contraseña es requerida.")
        password.length < 8 -> Pair(false, "La contraseña debe tener al menos 8 caracteres")
        !password.any{it.isDigit()} -> Pair(false, "La contraseña debe tener al menos un número")
        else -> Pair(true, "")
    }
}

fun validateName(name: String): Pair<Boolean, String>{
    return when{
        name.isEmpty() -> Pair(false, "El nombre es requerido.")
        name.length < 3 -> Pair(false, "El nombre debe tener al menos 3 caracteres.")
        else -> Pair(true, "")
    }
}

fun validateConfirmPassword(password: String, confirmPassword: String): Pair<Boolean, String>{
    return when{
        confirmPassword.isEmpty() -> Pair(false, "La contraseña es requerida.")
        confirmPassword != password -> Pair(false, "Las contraseñas no coinciden.")
        else -> Pair(true, "")
    }
}