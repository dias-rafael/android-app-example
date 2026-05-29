package com.example.android.data.local

import com.example.android.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Test

class UserEntityTest {

    @Test
    fun `toDomain maps all fields`() {
        val entity = UserEntity(id = 7, name = "Ada Lovelace", email = "ada@example.com")

        val user = entity.toDomain()

        assertEquals(User(id = 7, name = "Ada Lovelace", email = "ada@example.com"), user)
    }

    @Test
    fun `toEntity maps all fields`() {
        val user = User(id = 9, name = "Grace Hopper", email = "grace@example.com")

        val entity = user.toEntity()

        assertEquals(UserEntity(id = 9, name = "Grace Hopper", email = "grace@example.com"), entity)
    }
}
