package com.example.android.data.remote

import com.example.android.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Test

class UserDtoTest {

    @Test
    fun `toDomain maps all fields`() {
        val dto = UserDto(id = 3, name = "Linus Torvalds", email = "linus@example.com")

        val user = dto.toDomain()

        assertEquals(User(id = 3, name = "Linus Torvalds", email = "linus@example.com"), user)
    }
}
