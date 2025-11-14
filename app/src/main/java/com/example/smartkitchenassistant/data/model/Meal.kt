package com.example.smartkitchenassistant.data.model

data class MealResponse(
    val meals: List<Meal>?
)

data class Meal(
    val idMeal: String,
    val strMeal: String,
    val strCategory: String?,
    val strArea: String?,
    val strMealThumb: String?,
    val strInstructions: String?
)