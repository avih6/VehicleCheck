package com.avih6.vehiclecheck.data

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GovApiService {
    // 1. Private & Commercial Active Vehicles
    @GET("api/3/action/datastore_search")
    suspend fun getPrivateVehicle(
        @Query("resource_id") resourceId: String = "053cea08-09bc-40ec-8f7a-156f0677aff3",
        @Query("filters") filters: String,
        @Query("limit") limit: Int = 1
    ): GovApiResponse<VehicleRecord>

    // 2. Search by query fallback
    @GET("api/3/action/datastore_search")
    suspend fun searchVehicleByQuery(
        @Query("resource_id") resourceId: String = "053cea08-09bc-40ec-8f7a-156f0677aff3",
        @Query("q") query: String,
        @Query("limit") limit: Int = 5
    ): GovApiResponse<VehicleRecord>

    // 3. Technical Specs & Active Safety Model Registry
    @GET("api/3/action/datastore_search")
    suspend fun getModelTechnicalSpec(
        @Query("resource_id") resourceId: String = "142afde2-6228-49f9-8a29-9b6c3a0cbe40",
        @Query("filters") filters: String,
        @Query("limit") limit: Int = 1
    ): GovApiResponse<VehicleTechnicalSpecRecord>

    // 4. Extra History & Mileage (מאגר קילומטראז' ומקוריות)
    @GET("api/3/action/datastore_search")
    suspend fun getExtraHistory(
        @Query("resource_id") resourceId: String = "56063a99-8a3e-4ff4-912e-5966c0279bad",
        @Query("filters") filters: String,
        @Query("limit") limit: Int = 1
    ): GovApiResponse<VehicleExtraHistoryRecord>

    // 5. Heavy Vehicles / Trucks / Buses
    @GET("api/3/action/datastore_search")
    suspend fun getHeavyVehicle(
        @Query("resource_id") resourceId: String = "cd3acc5c-03c3-4c89-9c54-d40f93c0d790",
        @Query("filters") filters: String,
        @Query("limit") limit: Int = 1
    ): GovApiResponse<VehicleRecord>

    // 6. Two-Wheelers / Motorcycles
    @GET("api/3/action/datastore_search")
    suspend fun getTwoWheeler(
        @Query("resource_id") resourceId: String = "bf9df4e2-d90d-4c0a-a400-19e15af8e95f",
        @Query("filters") filters: String,
        @Query("limit") limit: Int = 1
    ): GovApiResponse<VehicleRecord>

    // 7. Cross-Check Disabled Permit
    @GET("api/3/action/datastore_search")
    suspend fun getDisabledPermit(
        @Query("resource_id") resourceId: String = "c8b9f9c8-4612-4068-934f-d4acd2e3c06e",
        @Query("filters") filters: String,
        @Query("limit") limit: Int = 1
    ): GovApiResponse<DisabledPermitRecord>

    // 8. Importer Name & Price
    @GET("api/3/action/datastore_search")
    suspend fun getImporterPrice(
        @Query("resource_id") resourceId: String = "39f455bf-6db0-4926-859d-017f34eacbcb",
        @Query("filters") filters: String,
        @Query("limit") limit: Int = 1
    ): GovApiResponse<VehicleImporterPriceRecord>

    // 9. Count active vehicles with same make and model (Using GovCountResponse)
    @GET("api/3/action/datastore_search")
    suspend fun getSameModelActiveCount(
        @Query("resource_id") resourceId: String = "053cea08-09bc-40ec-8f7a-156f0677aff3",
        @Query("filters") filters: String,
        @Query("limit") limit: Int = 0
    ): GovCountResponse

    // 10. Deregistered / Cancelled / Off-Road Vehicles (2010-2016)
    @GET("api/3/action/datastore_search")
    suspend fun getDeregisteredVehicle2010(
        @Query("resource_id") resourceId: String = "4e6b9724-4c1e-43f0-909a-154d4cc4e046",
        @Query("filters") filters: String,
        @Query("limit") limit: Int = 1
    ): GovApiResponse<DeregisteredVehicleRecord>

    // 11. Deregistered / Cancelled / Off-Road Vehicles (2017+)
    @GET("api/3/action/datastore_search")
    suspend fun getDeregisteredVehicle2017(
        @Query("resource_id") resourceId: String = "851ecab1-0622-4dbe-a6c7-f950cf82abf9",
        @Query("filters") filters: String,
        @Query("limit") limit: Int = 1
    ): GovApiResponse<DeregisteredVehicleRecord>

    // 12. Deregistered / Cancelled / Off-Road Vehicles (2000-2009)
    @GET("api/3/action/datastore_search")
    suspend fun getDeregisteredVehicle2000(
        @Query("resource_id") resourceId: String = "ec8cbc34-72e1-4b69-9c48-22821ba0bd6c",
        @Query("filters") filters: String,
        @Query("limit") limit: Int = 1
    ): GovApiResponse<DeregisteredVehicleRecord>

    // 12b. Deregistered / Cancelled / Off-Road Vehicles Master Registry (מאגר רכבים שנגרעו)
    @GET("api/3/action/datastore_search")
    suspend fun getDeregisteredMaster(
        @Query("resource_id") resourceId: String = "f6efe89a-373d-4501-8471-96815686be2b",
        @Query("filters") filters: String,
        @Query("limit") limit: Int = 1
    ): GovApiResponse<DeregisteredVehicleRecord>

    // 12c. Heavy Engineering Equipment (צמ"ה - כלי ציוד מכני הנדסי)
    @GET("api/3/action/datastore_search")
    suspend fun getEngineeringEquipment(
        @Query("resource_id") resourceId: String = "58da1b8b-e85d-4a18-8f81-7917e3cd3d04",
        @Query("filters") filters: String,
        @Query("limit") limit: Int = 1
    ): GovApiResponse<VehicleRecord>

    // 13. Count inactive vehicles of model (Using GovCountResponse)
    @GET("api/3/action/datastore_search")
    suspend fun getDeregisteredCount(
        @Query("resource_id") resourceId: String,
        @Query("filters") filters: String,
        @Query("limit") limit: Int = 0
    ): GovCountResponse

    // 14. Recall Restrictions per vehicle (הגבלות ריקול פתוחות ברכב)
    @GET("api/3/action/datastore_search")
    suspend fun getRecallRestrictions(
        @Query("resource_id") resourceId: String = "36bf1404-0be4-49d2-82dc-2f1ead4a8b93",
        @Query("filters") filters: String,
        @Query("limit") limit: Int = 5
    ): GovApiResponse<VehicleRecallRestrictionRecord>

    // 15. Recall Full Details & Importer info (פרטי ריקול והוראות תיקון)
    @GET("api/3/action/datastore_search")
    suspend fun getRecallDetails(
        @Query("resource_id") resourceId: String = "2c33523f-87aa-44ec-a736-edbb0a82975e",
        @Query("filters") filters: String,
        @Query("limit") limit: Int = 1
    ): GovApiResponse<RecallDetailRecord>

    // 15b. All Recalls Feed with Filtering & Sorting (מאגר כל הריקולים הרשמיים)
    @GET("api/3/action/datastore_search")
    suspend fun getAllRecalls(
        @Query("resource_id") resourceId: String = "2c33523f-87aa-44ec-a736-edbb0a82975e",
        @Query("q") query: String? = null,
        @Query("filters") filters: String? = null,
        @Query("sort") sort: String = "_id desc",
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 5000
    ): GovApiResponse<RecallDetailRecord>

    // 16. Personal Import Vehicles (יבוא אישי)
    @GET("api/3/action/datastore_search")
    suspend fun getPersonalImportVehicle(
        @Query("resource_id") resourceId: String = "03adc637-b6fe-402b-9937-7c3d3afc9140",
        @Query("filters") filters: String,
        @Query("limit") limit: Int = 1
    ): GovApiResponse<PersonalImportRecord>

    // 17. Public Transport Vehicles (מוניות, רכב סיור, אוטובוסים)
    @GET("api/3/action/datastore_search")
    suspend fun getPublicVehicle(
        @Query("resource_id") resourceId: String = "cf29862d-ca25-4691-84f6-1be60dcb4a1e",
        @Query("filters") filters: String,
        @Query("limit") limit: Int = 1
    ): GovApiResponse<VehicleRecord>
}

object NetworkClient {
    private const val BASE_URL = "https://data.gov.il/"

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val apiService: GovApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GovApiService::class.java)
    }
}