package com.example.android.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userDao = database.userDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertUsers_persistsUsers() = runBlocking {
        val users = listOf(
            UserEntity(id = 1, name = "Ada", email = "ada@example.com"),
            UserEntity(id = 2, name = "Grace", email = "grace@example.com")
        )

        userDao.insertUsers(users)

        assertEquals(users, userDao.getAllUsers().first())
    }

    @Test
    fun insertUsers_replacesUserWithSameId() = runBlocking {
        userDao.insertUsers(listOf(UserEntity(id = 1, name = "Old", email = "old@example.com")))

        userDao.insertUsers(listOf(UserEntity(id = 1, name = "New", email = "new@example.com")))

        assertEquals(
            listOf(UserEntity(id = 1, name = "New", email = "new@example.com")),
            userDao.getAllUsers().first()
        )
    }
}
