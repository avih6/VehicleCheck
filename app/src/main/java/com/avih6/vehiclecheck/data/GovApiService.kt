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
    // 1. Private & Commercial Vehicles (up to 3.5t)
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
        @Query("resource_id") resourceId: String = "cd3acc5c-03c3-4c42-ac1a-d7240f2e022f",
        @Query("filters") filters: String,
        @Query("limit") limit: Int = 1
    ): GovApiResponse<VehicleRecord>

    // 6. Two-Wheelers / Motorcycles
    @GET("api/3/action/datastore_search")
    suspend fun getTwoWheeler(
        @Query("resource_id") resourceId: String = "bf9df4e2-d90d-4c0a-a40b-5426e6f6630f",
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