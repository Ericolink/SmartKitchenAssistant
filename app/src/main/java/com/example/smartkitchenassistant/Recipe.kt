package com.example.smartkitchenassistant

data class Recipe(
    val title: String = "",
    val category: String = "",
    val image: String = "",
    val ingredients: List<String> = emptyList(),
    val steps: List<String> = emptyList()
)
