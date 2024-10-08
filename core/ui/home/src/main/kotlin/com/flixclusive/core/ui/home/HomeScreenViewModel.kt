package com.flixclusive.core.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flixclusive.core.datastore.AppSettingsManager
import com.flixclusive.core.util.common.resource.Resource
import com.flixclusive.data.util.InternetMonitor
import com.flixclusive.data.watch_history.WatchHistoryRepository
import com.flixclusive.domain.home.HomeItemsProviderUseCase
import com.flixclusive.model.database.WatchHistoryItem
import com.flixclusive.model.database.util.getNextEpisodeToWatch
import com.flixclusive.model.tmdb.Film
import com.flixclusive.model.tmdb.category.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


private fun filterWatchedFilms(watchHistoryItem: WatchHistoryItem): Boolean {
    val isTvShow = watchHistoryItem.seasons != null

    var isFinished = true
    if (watchHistoryItem.episodesWatched.isEmpty()) {
        isFinished = false
    } else if(isTvShow) {
        val (nextEpisodeToWatch, _) = getNextEpisodeToWatch(watchHistoryItem)
        if(nextEpisodeToWatch != null)
            isFinished = false
    } else {
        isFinished = watchHistoryItem.episodesWatched.last().isFinished
    }

    return isFinished
}

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val homeItemsProviderUseCase: HomeItemsProviderUseCase,
    appSettingsManager: AppSettingsManager,
    internetMonitor: InternetMonitor,
    watchHistoryRepository: WatchHistoryRepository,
) : ViewModel() {
    var itemsSize by mutableIntStateOf(0) // For TV
        private set

    private val paginationJobs = mutableMapOf<Int, Job?>()

    val state = homeItemsProviderUseCase.state
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = homeItemsProviderUseCase.state.value
        )

    val appSettings = appSettingsManager.appSettings
        .data
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = appSettingsManager.localAppSettings
        )

    private val connectionObserver = internetMonitor
        .isOnline
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val continueWatchingList = watchHistoryRepository
        .getAllItemsInFlow()
        .map { items ->
            items.filterNot(::filterWatchedFilms)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = runBlocking {
                watchHistoryRepository.getAllItemsInFlow()
                    .first()
                    .filterNot(::filterWatchedFilms)
            }
        )

    init {
        viewModelScope.launch {
            val initializationStatus = homeItemsProviderUseCase.state.map {
                it.status
            }.distinctUntilChanged()

            connectionObserver
                .combine(initializationStatus) { isConnected, status ->
                    isConnected to status
                }
                .onEach { (isConnected, status) ->
                    if (isConnected && status is Resource.Failure || status is Resource.Loading) {
                        initialize()
                    } else if(status is Resource.Success) {
                        onPaginateCategories()
                    }
                }
                .collect()
        }
    }

    fun initialize() {
        homeItemsProviderUseCase()
    }

    suspend fun loadFocusedFilm(film: Film) {
        homeItemsProviderUseCase.getFocusedFilm(film)
    }

    fun onPaginateCategories() {
        viewModelScope.launch {
            itemsSize += homeItemsProviderUseCase.state.value.categories.size
        }
    }

    fun onPaginateFilms(
        category: Category,
        page: Int,
        index: Int
    ) {
        homeItemsProviderUseCase.state.value.run {
            if(paginationJobs[index]?.isActive == true)
                return

            paginationJobs[index] = viewModelScope.launch {
                homeItemsProviderUseCase.getCategoryItems(
                    category = category,
                    index = index,
                    page = page
                )
            }
        }
    }
}