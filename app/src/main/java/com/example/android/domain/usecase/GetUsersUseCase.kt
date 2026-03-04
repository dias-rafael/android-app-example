package com.example.android.domain.usecase

import com.example.android.domain.model.User
import com.example.android.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface GetUsersUseCase {
    operator fun invoke(): Flow<List<User>>
    suspend fun refresh()
}

class GetUsersUseCaseImpl @Inject constructor(
    private val repository: UserRepository
) : GetUsersUseCase {
    override operator fun invoke(): Flow<List<User>> = repository.getUsers()
    override suspend fun refresh() = repository.refreshUsers()
}