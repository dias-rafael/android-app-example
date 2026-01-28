package com.example.android.domain.repository

import com.example.android.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUsers(): Flow<List<User>>
    suspend fun refreshUsers()
}