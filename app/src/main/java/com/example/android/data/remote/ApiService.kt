package com.example.android.data.remote

import retrofit2.http.GET

interface ApiService {
    @GET("users")
    suspend fun getUsers(): List<UserDto>

    companion object {
        const val BASE_URL = "https://jsonplaceholder.typicode.com/"
    }
}