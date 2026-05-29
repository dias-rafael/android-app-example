package com.example.android.data.repository

import com.example.android.data.local.UserDao
import com.example.android.data.local.UserEntity
import com.example.android.data.remote.ApiService
import com.example.android.data.remote.UserDto
import com.example.android.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class UserRepositoryImplTest {

    private val apiService = FakeApiService()
    private val userDao = FakeUserDao()
    private val repository = UserRepositoryImpl(apiService, userDao)

    @Test
    fun `getUsers emits cached users mapped to domain`() = runTest {
        userDao.insertUsers(
            listOf(
                UserEntity(id = 1, name = "Ada", email = "ada@example.com"),
                UserEntity(id = 2, name = "Grace", email = "grace@example.com")
            )
        )

        val users = repository.getUsers().first()

        assertEquals(
            listOf(
                User(id = 1, name = "Ada", email = "ada@example.com"),
                User(id = 2, name = "Grace", email = "grace@example.com")
            ),
            users
        )
    }

    @Test
    fun `refreshUsers fetches remote users and replaces cache by id`() = runTest {
        userDao.insertUsers(listOf(UserEntity(id = 1, name = "Old", email = "old@example.com")))
        apiService.users = listOf(
            UserDto(id = 1, name = "New", email = "new@example.com"),
            UserDto(id = 2, name = "Second", email = "second@example.com")
        )

        repository.refreshUsers()

        assertEquals(
            listOf(
                User(id = 1, name = "New", email = "new@example.com"),
                User(id = 2, name = "Second", email = "second@example.com")
            ),
            repository.getUsers().first()
        )
    }

    @Test
    fun `refreshUsers does not update cache when api fails`() = runTest {
        userDao.insertUsers(listOf(UserEntity(id = 1, name = "Cached", email = "cached@example.com")))
        apiService.failure = IllegalStateException("network unavailable")

        try {
            repository.refreshUsers()
        } catch (_: IllegalStateException) {
        }

        assertEquals(
            listOf(User(id = 1, name = "Cached", email = "cached@example.com")),
            repository.getUsers().first()
        )
    }

    private class FakeApiService : ApiService {
        var users: List<UserDto> = emptyList()
        var failure: RuntimeException? = null

        override suspend fun getUsers(): List<UserDto> {
            failure?.let { throw it }
            return users
        }
    }

    private class FakeUserDao : UserDao {
        private val users = MutableStateFlow<List<UserEntity>>(emptyList())

        override fun getAllUsers(): Flow<List<UserEntity>> = users

        override suspend fun insertUsers(users: List<UserEntity>) {
            val byId = this.users.value.associateBy { it.id }.toMutableMap()
            users.forEach { byId[it.id] = it }
            this.users.value = byId.values.sortedBy { it.id }
        }
    }
}
