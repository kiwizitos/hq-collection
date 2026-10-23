package com.kiwizitos.collection.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kiwizitos.collection.data.model.ComicDetails
import com.kiwizitos.collection.data.remote.GuiaFailureMessages
import com.kiwizitos.collection.domain.usecase.GetComicDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditionViewModel @Inject constructor(
    private val getComicDetailsUseCase: GetComicDetailsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<ComicDetails>>(UiState.Idle)
    val state: StateFlow<UiState<ComicDetails>> = _state.asStateFlow()
    private var loadJob: Job? = null
    private var currentUrl: String? = null

    fun load(url: String) {
        if (currentUrl == url && loadJob?.isActive == true) return
        loadJob?.cancel()
        currentUrl = url
        _state.value = UiState.Loading
        loadJob = viewModelScope.launch {
            val result = getComicDetailsUseCase(url)
            coroutineContext.ensureActive()
            _state.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = {
                    if (it is CancellationException) throw it
                    UiState.Error(GuiaFailureMessages.message(it), it)
                }
            )
        }
    }
}

