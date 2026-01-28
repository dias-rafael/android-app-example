package com.example.android.data.repository

import com.example.android.data.local.UserDao
import com.example.android.data.local.toDomain
import com.example.android.data.local.toEntity
import com.example.android.data.remote.ApiService
import com.example.android.data.remote.toDomain
import com.example.android.domain.model.User
import com.example.android.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao
) : UserRepository {

    override fun getUsers(): Flow<List<User>> {
        return userDao.getAllUsers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun refreshUsers() {
        val remoteUsers = apiService.getUsers()
        userDao.insertUsers(remoteUsers.map { it.toDomain().toEntity() })
    }
}