package com.example.android.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.android.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val email: String
)

fun UserEntity.toDomain() = User(id, name, email)
fun User.toEntity() = UserEntity(id, name, email)