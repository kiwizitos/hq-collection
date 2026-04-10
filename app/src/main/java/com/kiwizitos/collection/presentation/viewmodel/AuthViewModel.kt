package com.kiwizitos.collection.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kiwizitos.collection.data.repository.AuthRepository
import com.kiwizitos.collection.domain.usecase.SignInUseCase
import com.kiwizitos.collection.domain.usecase.SignOutUseCase
import com.kiwizitos.collection.domain.usecase.SignUpUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.UnknownHostException

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val signInUseCase:  SignInUseCase,
    private val signUpUseCase:  SignUpUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val authState: StateFlow<UiState<Unit>> = _authState.asStateFlow()

    val isLoggedIn: Boolean get() = authRepository.isLoggedIn()

    fun currentUserEmail(): String? = authRepository.currentUserEmail()
    fun currentUserId():    String? = authRepository.currentUserId()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            _authState.value = signInUseCase(email, password).fold(
                onSuccess = { UiState.Success(Unit) },
                onFailure = { UiState.Error(friendlyMessage(it), it) }
            )
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            _authState.value = signUpUseCase(email, password).fold(
                onSuccess = { UiState.Success(Unit) },
                onFailure = { UiState.Error(friendlyMessage(it), it) }
            )
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            signOutUseCase().fold(
                onSuccess = { _authState.value = UiState.Idle },
                onFailure = { _authState.value = UiState.Error(friendlyMessage(it), it) }
            )
        }
    }

    /** Reseta o estado para [UiState.Idle] (ex: ao fechar a tela de erro). */
    fun resetState() {
        _authState.value = UiState.Idle
    }

    private fun friendlyMessage(t: Throwable) = when (t) {
        is UnknownHostException        -> "Sem conexão com a internet"
        is IOException                 -> "Erro de conexão. Verifique sua internet."
        is IllegalArgumentException    -> t.message ?: "Dados inválidos"
        else                           -> t.message ?: "Erro desconhecido. Tente novamente."
    }
}

class AuthViewModelFactory(
    private val authRepository: AuthRepository,
    private val galleryRepository: com.kiwizitos.collection.data.repository.GalleryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AuthViewModel(
            authRepository = authRepository,
            signInUseCase  = SignInUseCase(authRepository),
            signUpUseCase  = SignUpUseCase(authRepository),
            signOutUseCase = SignOutUseCase(authRepository, galleryRepository)
        ) as T
}

