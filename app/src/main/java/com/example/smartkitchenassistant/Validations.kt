package com.example.smartkitchenassistant

import android.R
import android.util.Patterns
import org.intellij.lang.annotations.Pattern

fun validateEmail(email: String): Pair<Boolean, String>{
    return when{
        email.isEmpty() -> Pair(false, "Email is required.")
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> Pair(false, "The email is invalid.")
        !email.endsWith("@gmail.com")-> Pair(false, "That email is not corporate.")
        else -> Pair (true, "")
    }
}

fun validatePassword(password:String): Pair<Boolean, String>{
    return  when{
        password.isEmpty() -> Pair(false, "Password is required.")
        password.length < 8 -> Pair(false, "Password must be at least 8 characters long.")
        !password.any{it.isDigit()} -> Pair(false, "Password must contain at least one number.")
        else -> Pair(true, "")
    }
}

fun validateName(name: String): Pair<Boolean, String>{
    return when{
        name.isEmpty() -> Pair(false, "Name is required.")
        name.length < 3 -> Pair(false, "Name must have at least 3 characters.")
        else -> Pair(true, "")
    }
}

fun validateConfirmPassword(password: String, confirmPassword: String): Pair<Boolean, String>{
    return when{
        confirmPassword.isEmpty() -> Pair(false, "Password confirmation is required.")
        confirmPassword != password -> Pair(false, "Passwords do not match.")
        else -> Pair(true, "")
    }
}
