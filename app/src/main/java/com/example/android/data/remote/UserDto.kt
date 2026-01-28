package com.example.android.data.remote

import com.example.android.domain.model.User
import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String
)

fun UserDto.toDomain() = User(id, name, email)