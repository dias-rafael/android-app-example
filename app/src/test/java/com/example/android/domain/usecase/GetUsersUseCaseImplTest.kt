package com.example.android.domain.usecase

import com.example.android.domain.model.User
import com.example.android.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class GetUsersUseCaseImplTest {

    private val usersFlow = MutableStateFlow<List<User>>(emptyList())
    private val repository = FakeUserRepository(usersFlow)
    private val useCase = GetUsersUseCaseImpl(repository)

    @Test
    fun `invoke returns repository user stream`() {
        val result = useCase()

        assertSame(usersFlow, result)
    }

    @Test
    fun `refresh delegates to repository`() = runTest {
        useCase.refresh()

        assertTrue(repository.refreshCalled)
    }

    private class FakeUserRepository(
        private val users: Flow<List<User>>
    ) : UserRepository {
        var refreshCalled = false

        override fun getUsers(): Flow<List<User>> = users

        override suspend fun refreshUsers() {
            refreshCalled = true
        }
    }
}
