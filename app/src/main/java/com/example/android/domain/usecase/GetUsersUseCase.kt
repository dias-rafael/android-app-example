package com.example.android.domain.usecase

import com.example.android.domain.model.User
import com.example.android.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUsersUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(): Flow<List<User>> = repository.getUsers()
    
    suspend fun refresh() = repository.refreshUsers()
}