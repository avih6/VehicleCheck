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

    private val prefs = application.getSharedPreferences("vehicle_check_prefs", Context.MODE_PRIVATE)
    private val database = AppDatabase.getDatabase(application)
    private val repository = HistoryRepository(database.vehicleDao())

    private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "system") ?: "system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _dynamicColors = MutableStateFlow(prefs.getBoolean("dynamic_colors", true))
    val dynamicColors: StateFlow<Boolean> = _dynamicColors.asStateFlow()

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode).apply()
    }

    fun setDynamicColors(enabled: Boolean) {
        _dynamicColors.value = enabled
        prefs.edit().putBoolean("dynamic_colors", enabled).apply()
    }

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
                val plateLong = plateStr.toLongOrNull() ?: 0L
                val filtersStr = "{\"mispar_rechev\":$plateLong}"
                val paddedPlate = if (plateStr.length == 7) "0$plateStr" else plateStr

                // 1. Check Active Private Vehicle
                var activeVehicle: VehicleRecord? = null
                try {
                    val resp = NetworkClient.apiService.getPrivateVehicle(filters = filtersStr)
                    activeVehicle = resp.result?.records?.firstOrNull()
                } catch (e: Exception) {
                    try {
                        val fallback = NetworkClient.apiService.searchVehicleByQuery(query = plateStr)
                        activeVehicle = fallback.result?.records?.firstOrNull { it.licensePlate == plateLong }
                    } catch (e2: Exception) {}
                }

                // Check Heavy vehicle & Two-wheelers fallback
                if (activeVehicle == null) {
                    try {
                        val heavy = NetworkClient.apiService.getHeavyVehicle(filters = filtersStr)
                        activeVehicle = heavy.result?.records?.firstOrNull()
                    } catch (e: Exception) {}
                }
                if (activeVehicle == null) {
                    try {
                        val bike = NetworkClient.apiService.getTwoWheeler(filters = filtersStr)
                        activeVehicle = bike.result?.records?.firstOrNull()
                    } catch (e: Exception) {}
                }

                var isOffRoad = false
                var offRoadDateFormatted: String? = null
                var finalVehicle: VehicleRecord? = activeVehicle

                // 2. If not found in active, search deregistered / cancelled datasets!
                if (finalVehicle == null) {
                    // Try 2010-2016
                    try {
                        val resp2010 = NetworkClient.apiService.getDeregisteredVehicle2010(filters = "{\"mispar_rechev\":\"$paddedPlate\"}")
                        val match = resp2010.result?.records?.firstOrNull() ?: run {
                            NetworkClient.apiService.getDeregisteredVehicle2010(filters = "{\"mispar_rechev\":\"$plateStr\"}").result?.records?.firstOrNull()
                        }
                        if (match != null) {
                            finalVehicle = match.toVehicleRecord()
                            isOffRoad = true
                            offRoadDateFormatted = VehicleUtils.formatDate(match.cancellationDate)
                        }
                    } catch (e: Exception) {}

                    // Try 2017+
                    if (finalVehicle == null) {
                        try {
                            val resp2017 = NetworkClient.apiService.getDeregisteredVehicle2017(filters = "{\"mispar_rechev\":\"$paddedPlate\"}")
                            val match = resp2017.result?.records?.firstOrNull() ?: run {
                                NetworkClient.apiService.getDeregisteredVehicle2017(filters = "{\"mispar_rechev\":$plateLong}").result?.records?.firstOrNull()
                            }
                            if (match != null) {
                                finalVehicle = match.toVehicleRecord()
                                isOffRoad = true
                                offRoadDateFormatted = VehicleUtils.formatDate(match.cancellationDate)
                            }
                        } catch (e: Exception) {}
                    }

                    // Try 2000-2009
                    if (finalVehicle == null) {
                        try {
                            val resp2000 = NetworkClient.apiService.getDeregisteredVehicle2000(filters = "{\"mispar_rechev\":\"$paddedPlate\"}")
                            val match = resp2000.result?.records?.firstOrNull() ?: run {
                                NetworkClient.apiService.getDeregisteredVehicle2000(filters = "{\"mispar_rechev\":\"$plateStr\"}").result?.records?.firstOrNull()
                            }
                            if (match != null) {
                                finalVehicle = match.toVehicleRecord()
                                isOffRoad = true
                                offRoadDateFormatted = VehicleUtils.formatDate(match.cancellationDate)
                            }
                        } catch (e: Exception) {}
                    }
                }

                if (finalVehicle == null) {
                    _searchState.value = SearchState.NotFound(plateStr)
                    return@launch
                }

                val vehicle = finalVehicle

                // 3. Fetch Extra History & Disabled Permit in parallel
                val extraHistoryDeferred = async {
                    try {
                        val resp = NetworkClient.apiService.getExtraHistory(filters = filtersStr)
                        resp.result?.records?.firstOrNull()
                    } catch (e: Exception) { null }
                }

                val permitDeferred = async {
                    try {
                        val permitFilters = "{\"MISPAR RECHEV\":$plateLong}"
                        val resp = NetworkClient.apiService.getDisabledPermit(filters = permitFilters)
                        resp.result?.records?.firstOrNull()
                    } catch (e: Exception) { null }
                }

                val techSpecDeferred = async {
                    try {
                        val makeCd = vehicle.makeCode
                        val modelCd = vehicle.modelCd
                        val year = vehicle.year
                        if (makeCd != null && modelCd != null) {
                            val techFilter = if (year != null) {
                                "{\"tozeret_cd\":$makeCd,\"degem_cd\":$modelCd,\"shnat_yitzur\":$year}"
                            } else {
                                "{\"tozeret_cd\":$makeCd,\"degem_cd\":$modelCd}"
                            }
                            val resp = NetworkClient.apiService.getModelTechnicalSpec(filters = techFilter)
                            resp.result?.records?.firstOrNull() ?: run {
                                val broadFilter = "{\"tozeret_cd\":$makeCd,\"degem_cd\":$modelCd}"
                                NetworkClient.apiService.getModelTechnicalSpec(filters = broadFilter).result?.records?.firstOrNull()
                            }
                        } else null
                    } catch (e: Exception) { null }
                }

                val importerDeferred = async {
                    try {
                        val makeCd = vehicle.makeCode
                        val modelCd = vehicle.modelCd
                        val year = vehicle.year
                        if (makeCd != null && modelCd != null) {
                            val impFilter = if (year != null) {
                                "{\"tozeret_cd\":$makeCd,\"degem_cd\":$modelCd,\"shnat_yitzur\":$year}"
                            } else {
                                "{\"tozeret_cd\":$makeCd,\"degem_cd\":$modelCd}"
                            }
                            val resp = NetworkClient.apiService.getImporterPrice(filters = impFilter)
                            resp.result?.records?.firstOrNull() ?: run {
                                val broadFilter = "{\"tozeret_cd\":$makeCd,\"degem_cd\":$modelCd}"
                                NetworkClient.apiService.getImporterPrice(filters = broadFilter).result?.records?.firstOrNull()
                            }
                        } else null
                    } catch (e: Exception) { null }
                }

                // Model Statistics Deferred
                val statsDeferred = async {
                    try {
                        val makeCd = vehicle.makeCode
                        val modelCd = vehicle.modelCd
                        val year = vehicle.year
                        if (makeCd != null && modelCd != null) {
                            // Total Active for model across all years
                            val activeTotalFilter = "{\"tozeret_cd\":$makeCd,\"degem_cd\":$modelCd}"
                            val activeResp = NetworkClient.apiService.getSameModelActiveCount(filters = activeTotalFilter)
                            val totalActive = activeResp.result?.total ?: 0

                            // Specific Year Active
                            val activeYearFilter = if (year != null) "{\"tozeret_cd\":$makeCd,\"degem_cd\":$modelCd,\"shnat_yitzur\":$year}" else activeTotalFilter
                            val activeYearCount = if (year != null) NetworkClient.apiService.getSameModelActiveCount(filters = activeYearFilter).result?.total ?: 0 else totalActive

                            // Inactive count (from 2010+ and 2017+)
                            val inactFilter = if (year != null) "{\"tozeret_cd\":$makeCd,\"degem_cd\":$modelCd,\"shnat_yitzur\":$year}" else "{\"tozeret_cd\":$makeCd,\"degem_cd\":$modelCd}"
                            val inactResp = NetworkClient.apiService.getDeregisteredCount("851ecab1-0622-4dbe-a6c7-f950cf82abf9", inactFilter)
                            val inactCount2017 = inactResp.result?.total ?: 0

                            val inactResp2 = NetworkClient.apiService.getDeregisteredCount("4e6b9724-4c1e-43f0-909a-154d4cc4e046", inactFilter)
                            val inactCount2010 = inactResp2.result?.total ?: 0

                            val totalInactive = inactCount2017 + inactCount2010

                            val currentYear = year ?: 2022
                            val breakdown = mutableListOf<ModelYearCount>()
                            breakdown.add(ModelYearCount(currentYear, activeYearCount, totalInactive))
                            if (totalActive > activeYearCount) {
                                breakdown.add(ModelYearCount(currentYear + 1, totalActive - activeYearCount, 0))
                            }

                            ModelStatistics(
                                totalActive = if (totalActive > 0) totalActive else if (isOffRoad) 0 else 1,
                                totalInactive = if (totalInactive > 0) totalInactive else if (isOffRoad) 1 else 2,
                                breakdownByYear = breakdown
                            )
                        } else {
                            ModelStatistics(if (isOffRoad) 0 else 1, if (isOffRoad) 1 else 0)
                        }
                    } catch (e: Exception) {
                        ModelStatistics(if (isOffRoad) 0 else 1, if (isOffRoad) 1 else 0)
                    }
                }

                val extraHistory = extraHistoryDeferred.await()
                val permitRecord = permitDeferred.await()
                val techSpec = techSpecDeferred.await()
                val importerInfo = importerDeferred.await()
                val stats = statsDeferred.await()

                val formattedPlate = VehicleUtils.formatPlate(plateStr)
                val testStatus = VehicleUtils.parseTestStatus(vehicle.testExpiryDate, isOffRoad, offRoadDateFormatted)
                val hasDisabledPermit = permitRecord != null

                // Save to Room DB
                repository.saveSearch(
                    plate = plateStr,
                    record = vehicle,
                    testStatus = testStatus
                )

                _searchState.value = SearchState.Success(
                    vehicle = vehicle,
                    techSpec = techSpec,
                    importerInfo = importerInfo,
                    extraHistory = extraHistory,
                    formattedPlate = formattedPlate,
                    testStatus = testStatus,
                    hasDisabledPermit = hasDisabledPermit,
                    permitIssueDate = permitRecord?.issueDate,
                    isOffRoad = isOffRoad,
                    offRoadDate = offRoadDateFormatted,
                    stats = stats
                )

            } catch (e: Exception) {
                val errorMsg = when (e) {
                    is UnknownHostException -> "אין חיבור לאינטרנט. אנא בדוק את החיבור ונסה שוב."
                    is SocketTimeoutException -> "השרת אינו מגיב (פסק זמן). נסה שוב בעוד מספר רגעים."
                    is IOException -> "שגיאת תקשורת בטעינת נתוני הרכב."
                    else -> "אירעה שגיאה בבדיקת מספר הרכב: ${e.localizedMessage ?: "לא ידוע"}"
                }
                _searchState.value = SearchState.Error(errorMsg)
            }
        }
    }

    fun toggleFavorite(id: Long, currentStatus: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleFavorite(id, currentStatus)
        }
    }

    fun toggleFavoriteByPlate(plate: String, isFavorite: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleFavoriteByPlate(plate, isFavorite)
        }
    }

    fun toggleFavoriteCurrentResult(plate: String, currentFavStatus: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleFavoriteByPlate(plate, !currentFavStatus)
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

        val adUnitId = if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/2247696110" else "ca-app-pub-6647546375254792/3189297317"

        val adLoader = AdLoader.Builder(context, adUnitId)
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