package com.avih6.vehiclecheck

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.avih6.vehiclecheck.data.*
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = HistoryRepository(database.vehicleDao())

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    val searchHistory: StateFlow<List<VehicleHistoryEntity>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<VehicleHistoryEntity>> = repository.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _nativeAd = MutableStateFlow<NativeAd?>(null)
    val nativeAd: StateFlow<NativeAd?> = _nativeAd.asStateFlow()

    private var isAdLoading = false

    fun onQueryChange(newQuery: String) {
        val filtered = newQuery.filter { it.isDigit() }.take(8)
        _query.value = filtered
        if (filtered.isEmpty()) {
            _searchState.value = SearchState.Idle
        }
    }

    fun search() {
        val plate = _query.value.trim()
        if (plate.length !in 7..8) {
            _searchState.value = SearchState.Error("מספר הרכב חייב להכיל 7 או 8 ספרות")
            return
        }
        performSearch(plate)
    }

    fun searchPlateDirect(plate: String) {
        val clean = plate.filter { it.isDigit() }.take(8)
        _query.value = clean
        if (clean.length in 7..8) {
            performSearch(clean)
        }
    }

    private fun performSearch(plateStr: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _searchState.value = SearchState.Loading
            try {
                val plateNum = plateStr.toLongOrNull() ?: 0L
                val filterJson = "{\"mispar_rechev\": $plateNum}"
                val permitFilterJson = "{\"MISPAR RECHEV\": $plateNum}"

                // 1. Primary private vehicle dataset
                val primaryDeferred = async {
                    try {
                        NetworkClient.apiService.getPrivateVehicle(filters = filterJson)
                    } catch (e: Exception) { null }
                }

                // 2. Extra History & Mileage dataset
                val extraDeferred = async {
                    try {
                        NetworkClient.apiService.getExtraHistory(filters = filterJson)
                    } catch (e: Exception) { null }
                }

                // 3. Cross-check disabled permit dataset
                val permitDeferred = async {
                    try {
                        NetworkClient.apiService.getDisabledPermit(filters = permitFilterJson)
                    } catch (e: Exception) { null }
                }

                var vehicle = primaryDeferred.await()?.result?.records?.firstOrNull()

                // Fallback 1: Query search if exact filter returned empty
                if (vehicle == null) {
                    try {
                        val queryRes = NetworkClient.apiService.searchVehicleByQuery(query = plateStr)
                        vehicle = queryRes.result?.records?.firstOrNull { it.licensePlate == plateNum }
                    } catch (e: Exception) { null }
                }

                // Fallback 2: Heavy vehicles (Trucks/Buses)
                if (vehicle == null) {
                    try {
                        val heavyRes = NetworkClient.apiService.getHeavyVehicle(filters = filterJson)
                        vehicle = heavyRes.result?.records?.firstOrNull()
                    } catch (e: Exception) { null }
                }

                // Fallback 3: Two-Wheelers (Motorcycles/Scooters)
                if (vehicle == null) {
                    try {
                        val twoWheelerRes = NetworkClient.apiService.getTwoWheeler(filters = filterJson)
                        vehicle = twoWheelerRes.result?.records?.firstOrNull()
                    } catch (e: Exception) { null }
                }

                val extraHistory = extraDeferred.await()?.result?.records?.firstOrNull()
                val hasPermit = (permitDeferred.await()?.result?.records?.isNotEmpty() == true)

                if (vehicle != null) {
                    val formatted = VehicleUtils.formatPlate(plateStr)
                    val testStatus = VehicleUtils.parseTestStatus(vehicle.testExpiryDate)
                    
                    // Save to history
                    repository.saveSearch(plateStr, vehicle, testStatus)

                    _searchState.value = SearchState.Success(
                        vehicle = vehicle,
                        extraHistory = extraHistory,
                        formattedPlate = formatted,
                        testStatus = testStatus,
                        hasDisabledPermit = hasPermit
                    )
                } else {
                    _searchState.value = SearchState.NotFound(plateStr)
                }
            } catch (e: Exception) {
                if (e is IOException || e is SocketTimeoutException || e is UnknownHostException) {
                    _searchState.value = SearchState.Error("שגיאת תקשורת. בדוק את החיבור לאינטרנט ונסה שוב.")
                } else {
                    _searchState.value = SearchState.Error(e.localizedMessage ?: "אירעה שגיאה בחיפוש")
                }
            }
        }
    }

    fun toggleFavorite(id: Long, currentStatus: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleFavorite(id, currentStatus)
        }
    }

    fun toggleFavoriteCurrentResult(plate: String, isFavorite: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleFavoriteByPlate(plate, isFavorite)
        }
    }

    fun deleteHistoryEntry(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearHistory()
        }
    }

    fun loadNativeAd(context: Context) {
        if (isAdLoading || _nativeAd.value != null) return
        isAdLoading = true

        val adLoader = AdLoader.Builder(context, "ca-app-pub-3940256099942544/2247696110")
            .forNativeAd { ad: NativeAd ->
                _nativeAd.value?.destroy()
                _nativeAd.value = ad
                isAdLoading = false
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    isAdLoading = false
                }
            })
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    override fun onCleared() {
        super.onCleared()
        _nativeAd.value?.destroy()
    }
}