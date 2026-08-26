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

    private val _dbVehicleCount = MutableStateFlow<Int?>(null)
    val dbVehicleCount: StateFlow<Int?> = _dbVehicleCount.asStateFlow()

    private val _dbLastUpdated = MutableStateFlow<String?>(null)
    val dbLastUpdated: StateFlow<String?> = _dbLastUpdated.asStateFlow()

    private val _nativeAd = MutableStateFlow<NativeAd?>(null)
    val nativeAd: StateFlow<NativeAd?> = _nativeAd.asStateFlow()

    private var isAdLoading = false

    init {
        fetchDatabaseStats()
    }

    private fun fetchDatabaseStats() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val resp = NetworkClient.apiService.getTotalActiveVehicles()
                if (resp.result != null && resp.result.total > 0) {
                    _dbVehicleCount.value = resp.result.total
                }
            } catch (e: Exception) {}

            try {
                val metaResp = NetworkClient.apiService.getResourceMetadata()
                val lastMod = metaResp.result?.lastModified ?: metaResp.result?.metadataModified
                if (!lastMod.isNullOrBlank()) {
                    _dbLastUpdated.value = VehicleUtils.formatDateTime(lastMod)
                }
            } catch (e: Exception) {}
        }
    }

    fun onQueryChange(newQuery: String) {
        val filtered = newQuery.filter { it.isDigit() }.take(8)
        _query.value = filtered
        if (filtered.isEmpty()) {
            _searchState.value = SearchState.Idle
        }
    }

    fun resetSearchState() {
        _searchState.value = SearchState.Idle
    }

    fun search() {
        val plate = _query.value.trim()
        if (plate.length !in 5..8) {
            _searchState.value = SearchState.Error("מספר הרכב חייב להכיל בין 5 ל-8 ספרות")
            return
        }
        performSearch(plate)
    }

    fun searchPlateDirect(plate: String) {
        val clean = plate.filter { it.isDigit() }.take(8)
        _query.value = clean
        if (clean.length in 5..8) {
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

                // Check Personal Import
                if (activeVehicle == null) {
                    try {
                        val impResp = NetworkClient.apiService.getPersonalImportVehicle(filters = filtersStr)
                        activeVehicle = impResp.result?.records?.firstOrNull()?.toVehicleRecord()
                    } catch (e: Exception) {}
                }

                // Check Public Vehicle (Taxis/Buses)
                if (activeVehicle == null) {
                    try {
                        val pub = NetworkClient.apiService.getPublicVehicle(filters = filtersStr)
                        activeVehicle = pub.result?.records?.firstOrNull() ?: run {
                            NetworkClient.apiService.getPublicVehicle(filters = "{\"mispar_rechev\":\"$plateStr\"}").result?.records?.firstOrNull()
                        } ?: run {
                            NetworkClient.apiService.getPublicVehicle(filters = "{\"mispar_rechev\":\"$paddedPlate\"}").result?.records?.firstOrNull()
                        }
                    } catch (e: Exception) {}
                }

                // Check Heavy vehicle (includes Trucks & Buses)
                if (activeVehicle == null) {
                    try {
                        val heavy = NetworkClient.apiService.getHeavyVehicle(filters = filtersStr)
                        activeVehicle = heavy.result?.records?.firstOrNull() ?: run {
                            NetworkClient.apiService.getHeavyVehicle(filters = "{\"mispar_rechev\":\"$plateStr\"}").result?.records?.firstOrNull()
                        } ?: run {
                            NetworkClient.apiService.getHeavyVehicle(filters = "{\"mispar_rechev\":\"$paddedPlate\"}").result?.records?.firstOrNull()
                        }
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
                var equipmentRecord: EngineeringEquipmentRecord? = null

                // Always check Heavy Engineering Equipment (צמ"ה) in parallel
                try {
                    val respZama = NetworkClient.apiService.getEngineeringEquipment(filters = "{\"mispar_tzama\":$plateLong}")
                    equipmentRecord = respZama.result?.records?.firstOrNull() ?: run {
                        NetworkClient.apiService.getEngineeringEquipment(filters = "{\"mispar_tzama\":\"$plateStr\"}").result?.records?.firstOrNull()
                    }
                } catch (e: Exception) {}

                // 2. If not found in active, search deregistered / cancelled & vintage datasets!
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

                    // Try Master Deregistered Dataset
                    if (finalVehicle == null) {
                        try {
                            val respMaster = NetworkClient.apiService.getDeregisteredMaster(filters = "{\"mispar_rechev\":$plateLong}")
                            val match = respMaster.result?.records?.firstOrNull() ?: run {
                                NetworkClient.apiService.getDeregisteredMaster(filters = "{\"mispar_rechev\":\"$plateStr\"}").result?.records?.firstOrNull()
                            }
                            if (match != null) {
                                finalVehicle = match.toVehicleRecord()
                                isOffRoad = true
                                offRoadDateFormatted = VehicleUtils.formatDate(match.cancellationDate)
                            }
                        } catch (e: Exception) {}
                    }

                    // Try Vintage & Inactive Pre-2000 Dataset (e.g. 1950s Chevrolet etc.)
                    if (finalVehicle == null) {
                        try {
                            val respVintage = NetworkClient.apiService.getVintageDeregistered(filters = "{\"mispar_rechev\":$plateLong}")
                            val match = respVintage.result?.records?.firstOrNull() ?: run {
                                NetworkClient.apiService.getVintageDeregistered(filters = "{\"mispar_rechev\":\"$plateStr\"}").result?.records?.firstOrNull()
                            }
                            if (match != null) {
                                finalVehicle = match.toVehicleRecord()
                                isOffRoad = true
                                offRoadDateFormatted = "רכב היסטורי / נגרע"
                            }
                        } catch (e: Exception) {}
                    }
                }

                var isEngineering = false
                var activeEq: EngineeringEquipmentRecord? = null
                var altEq: EngineeringEquipmentRecord? = null

                if (finalVehicle == null) {
                    if (equipmentRecord != null) {
                        finalVehicle = equipmentRecord.toVehicleRecord()
                        isOffRoad = false
                        isEngineering = true
                        activeEq = equipmentRecord
                    } else {
                        // Save to history so user can easily recheck anytime
                        repository.saveNotFoundSearch(plateStr)
                        _searchState.value = SearchState.NotFound(plateStr)
                        return@launch
                    }
                } else {
                    if (equipmentRecord != null) {
                        altEq = equipmentRecord
                    }
                }

                val vehicle = finalVehicle

                // 3. Parallel Fetch: Recalls, Extra History, Disabled Permit, Specs, Pricing & Stats
                val recallsDeferred = async {
                    try {
                        val recallFilter = "{\"MISPAR_RECHEV\":$plateLong}"
                        val resp = NetworkClient.apiService.getRecallRestrictions(filters = recallFilter)
                        resp.result?.records ?: emptyList()
                    } catch (e: Exception) { emptyList<VehicleRecallRestrictionRecord>() }
                }

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

                val statsDeferred = async {
                    try {
                        val makeCd = vehicle.makeCode
                        val modelCd = vehicle.modelCd
                        val year = vehicle.year ?: 2022
                        val modelName = vehicle.model

                        var totalActive = 0
                        var activeYearCount = 0
                        var prevYearCount = 0
                        var nextYearCount = 0

                        // 1. Query by exact makeCode + modelCd
                        if (makeCd != null && modelCd != null && modelCd > 0) {
                            try {
                                val activeResp = NetworkClient.apiService.getSameModelActiveCount(filters = "{\"tozeret_cd\":$makeCd,\"degem_cd\":$modelCd}")
                                totalActive = activeResp.result?.total ?: 0

                                if (totalActive > 0) {
                                    activeYearCount = NetworkClient.apiService.getSameModelActiveCount(filters = "{\"tozeret_cd\":$makeCd,\"degem_cd\":$modelCd,\"shnat_yitzur\":$year}").result?.total ?: 0
                                    prevYearCount = NetworkClient.apiService.getSameModelActiveCount(filters = "{\"tozeret_cd\":$makeCd,\"degem_cd\":$modelCd,\"shnat_yitzur\":${year - 1}}").result?.total ?: 0
                                    nextYearCount = NetworkClient.apiService.getSameModelActiveCount(filters = "{\"tozeret_cd\":$makeCd,\"degem_cd\":$modelCd,\"shnat_yitzur\":${year + 1}}").result?.total ?: 0
                                }
                            } catch (e: Exception) {}
                        }

                        // 2. Fallback: Query by makeCode + kinuy_mishari
                        if (totalActive <= 1 && makeCd != null && !modelName.isNullOrBlank()) {
                            try {
                                val kinuyResp = NetworkClient.apiService.getSameModelActiveCount(filters = "{\"tozeret_cd\":$makeCd,\"kinuy_mishari\":\"${modelName.trim()}\"}")
                                val kinuyTotal = kinuyResp.result?.total ?: 0
                                if (kinuyTotal > 0) {
                                    totalActive = kinuyTotal
                                    activeYearCount = NetworkClient.apiService.getSameModelActiveCount(filters = "{\"tozeret_cd\":$makeCd,\"kinuy_mishari\":\"${modelName.trim()}\",\"shnat_yitzur\":$year}").result?.total ?: 0
                                }
                            } catch (e: Exception) {}
                        }

                        // Inactive count from deregistered datasets (2017+ and 2010+)
                        var inactCount2017 = 0
                        var inactCount2010 = 0
                        if (makeCd != null && modelCd != null && modelCd > 0) {
                            try {
                                inactCount2017 = NetworkClient.apiService.getDeregisteredCount("851ecab1-0622-4dbe-a6c7-f950cf82abf9", "{\"tozeret_cd\":$makeCd,\"degem_cd\":$modelCd}").result?.total ?: 0
                                inactCount2010 = NetworkClient.apiService.getDeregisteredCount("4e6b9724-4c1e-43f0-909a-154d4cc4e046", "{\"tozeret_cd\":$makeCd,\"degem_cd\":$modelCd}").result?.total ?: 0
                            } catch (e: Exception) {}
                        }

                        val totalInactive = (inactCount2017 + inactCount2010).coerceAtLeast(if (isOffRoad) 1 else 0)
                        val realTotalActive = if (totalActive > 0) totalActive else if (isOffRoad) 0 else 1

                        val breakdown = mutableListOf<ModelYearCount>()
                        if (nextYearCount > 0) {
                            breakdown.add(ModelYearCount(year + 1, nextYearCount, 0))
                        }
                        breakdown.add(ModelYearCount(year, if (activeYearCount > 0) activeYearCount else realTotalActive, totalInactive))
                        if (prevYearCount > 0) {
                            breakdown.add(ModelYearCount(year - 1, prevYearCount, 0))
                        }

                        ModelStatistics(
                            totalActive = realTotalActive,
                            totalInactive = totalInactive,
                            breakdownByYear = breakdown
                        )
                    } catch (e: Exception) {
                        ModelStatistics(if (isOffRoad) 0 else 1, if (isOffRoad) 1 else 0)
                    }
                }

                val recalls = recallsDeferred.await()
                var recallDetail: RecallDetailRecord? = null
                val firstRecallId = recalls.firstOrNull()?.recallId
                if (firstRecallId != null) {
                    try {
                        val detailResp = NetworkClient.apiService.getRecallDetails(filters = "{\"RECALL_ID\":$firstRecallId}")
                        recallDetail = detailResp.result?.records?.firstOrNull()
                    } catch (e: Exception) {}
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
                    stats = stats,
                    recalls = recalls,
                    recallDetail = recallDetail,
                    isEngineeringEquipment = isEngineering,
                    equipmentDetails = activeEq,
                    alternateEquipment = altEq,
                    alternateVehicle = null
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

    fun toggleEquipmentView() {
        val curr = _searchState.value as? SearchState.Success ?: return
        if (curr.isEngineeringEquipment) {
            val altVeh = curr.alternateVehicle ?: return
            val altEq = curr.equipmentDetails
            val testStatus = VehicleUtils.parseTestStatus(altVeh.testExpiryDate, curr.isOffRoad, curr.offRoadDate)
            _searchState.value = curr.copy(
                vehicle = altVeh,
                testStatus = testStatus,
                isEngineeringEquipment = false,
                equipmentDetails = null,
                alternateEquipment = altEq,
                alternateVehicle = null
            )
        } else {
            val altEq = curr.alternateEquipment ?: return
            val altVeh = curr.vehicle
            val eqVehicle = altEq.toVehicleRecord()
            val testStatus = VehicleUtils.parseTestStatus(altEq.expirationDate, false, null)
            _searchState.value = curr.copy(
                vehicle = eqVehicle,
                testStatus = testStatus,
                isEngineeringEquipment = true,
                equipmentDetails = altEq,
                alternateEquipment = null,
                alternateVehicle = altVeh
            )
        }
    }

    fun toggleFavorite(id: Long, currentStatus: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleFavorite(id, currentStatus)
        }
    }

    fun toggleFavoriteByPlate(plate: String, isFavorite: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleFavoriteByPlate(plate, !isFavorite)
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