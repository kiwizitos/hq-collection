package com.kiwizitos.collection.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kiwizitos.collection.data.model.ComicDetails
import com.kiwizitos.collection.data.remote.NetworkModule
import com.kiwizitos.collection.data.repository.GuiaQuadrinhosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class EditionViewModel(
    private val repository: GuiaQuadrinhosRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<ComicDetails>>(UiState.Idle)
    val state: StateFlow<UiState<ComicDetails>> = _state.asStateFlow()

    fun load(url: String) {
        if (_state.value is UiState.Loading) return
        viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value = repository.getComicDetails(url).fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(friendlyMessage(it), it) }
            )
        }
    }

    private fun friendlyMessage(t: Throwable) = when (t) {
        is UnknownHostException   -> "Sem conexão com a internet"
        is SocketTimeoutException -> "Tempo de conexão esgotado. Tente novamente."
        is IOException            -> "Erro de conexão. Verifique sua internet."
        else                      -> t.message ?: "Erro desconhecido. Tente novamente."
    }
}

class EditionViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        EditionViewModel(
            GuiaQuadrinhosRepository(NetworkModule.service)
        ) as T
}

