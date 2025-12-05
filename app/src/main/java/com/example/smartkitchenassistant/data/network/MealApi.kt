package com.example.smartkitchenassistant.data.network

import com.example.smartkitchenassistant.data.model.MealResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MealApi {
    @GET("search.php")
    suspend fun searchMeals(
        @Query("s") query: String
    ): MealResponse

    @GET("lookup.php")
    suspend fun getMealById(
        @Query("i") id: String
    ): MealResponse

}