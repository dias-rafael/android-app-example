package com.example.android.presentation

import com.example.android.domain.model.User
import com.example.android.domain.usecase.GetUsersUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class UserViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `init observes users and refreshes once`() = runTest {
        val user = User(id = 1, name = "Ada", email = "ada@example.com")
        val useCase = FakeGetUsersUseCase(initialUsers = listOf(user))

        val viewModel = UserViewModel(useCase)

        assertEquals(listOf(user), viewModel.uiState.value.users)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(1, useCase.refreshCalls)
    }

    @Test
    fun `refresh event refreshes users`() = runTest {
        val refreshedUser = User(id = 2, name = "Grace", email = "grace@example.com")
        val useCase = FakeGetUsersUseCase()
        useCase.onRefresh = { useCase.emit(listOf(refreshedUser)) }
        val viewModel = UserViewModel(useCase)

        viewModel.onEvent(UserEvent.Refresh)

        assertEquals(2, useCase.refreshCalls)
        assertEquals(listOf(refreshedUser), viewModel.uiState.value.users)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `refresh failure exposes error and stops loading`() = runTest {
        val useCase = FakeGetUsersUseCase()
        useCase.refreshFailure = IllegalStateException("service unavailable")

        val viewModel = UserViewModel(useCase)

        assertEquals("service unavailable", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `user stream failure exposes error and stops loading`() = runTest {
        val useCase = FailingUsersUseCase(IllegalStateException("database unavailable"))

        val viewModel = UserViewModel(useCase)

        assertEquals("database unavailable", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(useCase.refreshCalled)
    }

    private class FakeGetUsersUseCase(
        initialUsers: List<User> = emptyList()
    ) : GetUsersUseCase {
        private val users = MutableStateFlow(initialUsers)
        var refreshCalls = 0
        var refreshFailure: RuntimeException? = null
        var onRefresh: suspend () -> Unit = {}

        override fun invoke(): Flow<List<User>> = users

        override suspend fun refresh() {
            refreshCalls++
            refreshFailure?.let { throw it }
            onRefresh()
        }

        fun emit(newUsers: List<User>) {
            users.value = newUsers
        }
    }

    private class FailingUsersUseCase(
        private val failure: RuntimeException
    ) : GetUsersUseCase {
        var refreshCalled = false

        override fun invoke(): Flow<List<User>> = kotlinx.coroutines.flow.flow {
            throw failure
        }

        override suspend fun refresh() {
            refreshCalled = true
        }
    }
}
