package com.kiwizitos.collection.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiwizitos.collection.data.model.ComicDetails
import com.kiwizitos.collection.domain.usecase.GetComicDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class EditionViewModel @Inject constructor(
    private val getComicDetailsUseCase: GetComicDetailsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<ComicDetails>>(UiState.Idle)
    val state: StateFlow<UiState<ComicDetails>> = _state.asStateFlow()

    fun load(url: String) {
        if (_state.value is UiState.Loading) return
        viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value = getComicDetailsUseCase(url).fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(friendlyMessage(it), it) }
            )
        }
    }

    private fun friendlyMessage(t: Throwable) = when (t) {
        is UnknownHostException -> "Sem conexão com a internet"
        is SocketTimeoutException -> "Tempo de conexão esgotado. Tente novamente."
        is IOException -> "Erro de conexão. Verifique sua internet."
        else -> t.message ?: "Erro desconhecido. Tente novamente."
    }
}


