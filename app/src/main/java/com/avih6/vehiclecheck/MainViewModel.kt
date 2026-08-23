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

                // 1. Fetch Main Vehicle Record
                val vehicleDeferred = async {
                    try {
                        val resp = NetworkClient.apiService.getPrivateVehicle(filters = filtersStr)
                        resp.result?.records?.firstOrNull()
                    } catch (e: Exception) {
                        try {
                            val fallback = NetworkClient.apiService.searchVehicleByQuery(query = plateStr)
                            fallback.result?.records?.firstOrNull { it.licensePlate == plateLong }
                        } catch (e2: Exception) {
                            null
                        }
                    }
                }

                // 2. Fetch Extra Mileage & History Record
                val extraHistoryDeferred = async {
                    try {
                        val resp = NetworkClient.apiService.getExtraHistory(filters = filtersStr)
                        resp.result?.records?.firstOrNull()
                    } catch (e: Exception) {
                        null
                    }
                }

                // 3. Cross-Check Disabled Permit
                val permitDeferred = async {
                    try {
                        val permitFilters = "{\"MISPAR RECHEV\":$plateLong}"
                        val resp = NetworkClient.apiService.getDisabledPermit(filters = permitFilters)
                        resp.result?.records?.firstOrNull()
                    } catch (e: Exception) {
                        null
                    }
                }

                val vehicle = vehicleDeferred.await()

                if (vehicle == null) {
                    _searchState.value = SearchState.NotFound(plateStr)
                    return@launch
                }

                // 4. Fetch Technical Specs, Importer Price, and Active Count in parallel
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
                    } catch (e: Exception) {
                        null
                    }
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
                    } catch (e: Exception) {
                        null
                    }
                }

                val countDeferred = async {
                    try {
                        val makeCd = vehicle.makeCode
                        val modelCd = vehicle.modelCd
                        val year = vehicle.year
                        if (makeCd != null && modelCd != null) {
                            val countFilter = if (year != null) {
                                "{\"tozeret_cd\":$makeCd,\"degem_cd\":$modelCd,\"shnat_yitzur\":$year}"
                            } else {
                                "{\"tozeret_cd\":$makeCd,\"degem_cd\":$modelCd}"
                            }
                            val resp = NetworkClient.apiService.getSameModelActiveCount(filters = countFilter)
                            resp.result?.total ?: 0
                        } else 0
                    } catch (e: Exception) {
                        0
                    }
                }

                val extraHistory = extraHistoryDeferred.await()
                val permitRecord = permitDeferred.await()
                val techSpec = techSpecDeferred.await()
                val importerInfo = importerDeferred.await()
                val activeCount = countDeferred.await()

                val formattedPlate = VehicleUtils.formatPlate(plateStr)
                val testStatus = VehicleUtils.parseTestStatus(vehicle.testExpiryDate)
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
                    sameModelActiveCount = activeCount
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