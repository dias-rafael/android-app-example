package com.example.android.presentation

import com.example.android.domain.model.User

data class UserUiState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val error: String? = null
)

sealed class UserEvent {
    object Refresh : UserEvent()
    data class ShowMessage(val message: String) : UserEvent()
}