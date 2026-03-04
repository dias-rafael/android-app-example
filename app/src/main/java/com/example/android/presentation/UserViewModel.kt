package com.example.android.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.domain.usecase.GetUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val getUsersUseCase: com.example.android.domain.usecase.GetUsersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    private val disposables = CompositeDisposable()

    init {
        observeUsers()
        refresh()
    }

    private fun observeUsers() {
        getUsersUseCase()
            .onEach { users ->
                _uiState.update { it.copy(users = users, isLoading = false) }
            }
            .catch { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: UserEvent) {
        when (event) {
            is UserEvent.Refresh -> refresh()
            is UserEvent.ShowMessage -> {
                // Exemplo de como lidar com eventos pontuais
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                getUsersUseCase.refresh()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear() // Exemplo de limpeza do RxJava
    }
}