package com.apptest.feature.appdetail.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.apptest.core.common.onFailure
import com.apptest.core.common.onSuccess
import com.apptest.core.navigation.AppDestination
import com.apptest.feature.appdetail.domain.usecase.GetAppDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Loads detail by `appId` from route. Exposes a one-shot [openPlayStoreEvents] Channel
 * so the side-effect (launching Play Store via Intent) stays out of composition.
 */
@HiltViewModel
class AppDetailViewModel @Inject constructor(
    private val getDetail: GetAppDetailUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val args = savedStateHandle.toRoute<AppDestination.AppDetail>()

    private val _state = MutableStateFlow<AppDetailUiState>(AppDetailUiState.Loading)
    val state: StateFlow<AppDetailUiState> = _state.asStateFlow()

    private val _openPlayStore = Channel<String>(Channel.BUFFERED)
    val openPlayStoreEvents = _openPlayStore.receiveAsFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = AppDetailUiState.Loading
            getDetail(args.appId)
                .onSuccess { _state.value = AppDetailUiState.Loaded(it) }
                .onFailure { _state.value = AppDetailUiState.Error(it) }
        }
    }

    /**
     * V1: fires an [openPlayStoreEvents] event with the App's `playOptInUrl`. Real flow
     * (TestRequest creation + heartbeat scheduling) lands with APT-V1-R-040.
     */
    fun onJoinClicked() {
        val loaded = _state.value as? AppDetailUiState.Loaded ?: return
        _state.update { loaded.copy(joinInProgress = true) }
        viewModelScope.launch {
            _openPlayStore.send(loaded.data.playOptInUrl)
            _state.update { loaded.copy(joinInProgress = false) }
        }
    }
}
