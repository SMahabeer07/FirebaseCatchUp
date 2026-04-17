package com.fake.firebasebasics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthViewModel(private val repo: AuthRepository = AuthRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUIState(isAuthenticated = repo.currentUser != null))
    val uiState = _uiState.asStateFlow()

    private val authListener = FirebaseAuth.AuthStateListener { auth ->
        _uiState.value = _uiState.value.copy(isAuthenticated = auth.currentUser != null)
    }

    init {
        FirebaseAuth.getInstance().addAuthStateListener(authListener)
    }

    fun onEmailChange(v: String) { _uiState.value = _uiState.value.copy(email = v) }
    fun onPasswordChange(v: String) { _uiState.value = _uiState.value.copy(password = v) }

    fun login() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val res = repo.login(_uiState.value.email, _uiState.value.password)
            if (res.isFailure) _uiState.value = _uiState.value.copy(isLoading = false, error = res.exceptionOrNull()?.message)
        }
    }

    fun register() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val res = repo.register(_uiState.value.email, _uiState.value.password)
            if (res.isFailure) _uiState.value = _uiState.value.copy(isLoading = false, error = res.exceptionOrNull()?.message)
        }
    }

    fun logout() = repo.logout()

    override fun onCleared() {
        super.onCleared()
        FirebaseAuth.getInstance().removeAuthStateListener(authListener)
    }
}