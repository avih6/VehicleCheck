package com.avih6.vehiclecheck.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Serializable
data class GovApiResponse<T>(
    val success: Boolean,
    val result: GovResult<T>? = null
)

@Serializable
data class GovResult<T>(
    val records: List<T> = emptyList(),
    val total: Int = 0
)

@Serializable
data class VehicleRecord(
    @SerialName("_id") val id: Long? = null,
    @SerialName("mispar_rechev") val licensePlate: Long? = null,
    @SerialName("tozeret_nm") val make: String? = null,
    @SerialName("tozeret_cd") val makeCode: Long? = null,
    @SerialName("kinuy_mishari") val model: String? = null,
    @SerialName("degem_nm") val modelCode: String? = null,
    @SerialName("degem_cd") val modelCd: Long? = null,
    @SerialName("sug_degem") val modelType: String? = null,
    @SerialName("ramat_gimur") val trimLevel: String? = null,
    @SerialName("shnat_yitzur") val year: Int? = null,
    @SerialName("moed_aliya_lakvish") val onRoadDate: String? = null,
    @SerialName("mivchan_acharon_dt") val lastTestDate: String? = null,
    @SerialName("tokef_dt") val testExpiryDate: String? = null,
    @SerialName("baalut") val ownership: String? = null,
    @SerialName("tzeva_rechev") val color: String? = null,
    @SerialName("tzeva_cd") val colorCode: Int? = null,
    @SerialName("sug_delek_nm") val fuelType: String? = null,
    @SerialName("degem_manoa") val engineModel: String? = null,
    @SerialName("zmig_kidmi") val frontTire: String? = null,
    @SerialName("zmig_ahori") val rearTire: String? = null,
    @SerialName("ramat_eivzur_betihuty") val safetyRating: Int? = null,
    @SerialName("kvutzat_zihum") val emissionGroup: Int? = null,
    @SerialName("misgeret") val vin: String? = null,
    @SerialName("horaat_rishum") val registrationDirective: Long? = null
)

@Serializable
data class VehicleTechnicalSpecRecord(
    @SerialName("_id") val id: Long? = null,
    @SerialName("tozeret_cd") val makeCode: Long? = null,
    @SerialName("degem_cd") val modelCode: Long? = null,
    @SerialName("shnat_yitzur") val year: Int? = null,
    @SerialName("koah_sus") val horsepower: Int? = null,
    @SerialName("nefah_manoa") val engineDisplacement: Int? = null,
    @SerialName("automatic_ind") val isAutomatic: Int? = null,
    @SerialName("hanaa_nm") val driveType: String? = null,
    @SerialName("merkav") val bodyType: String? = null,
    @SerialName("mispar_moshavim") val seats: Int? = null,
    @SerialName("mispar_dlatot") val doors: Int? = null,
    @SerialName("mispar_kariot_avir") val airbags: Int? = null,
    @SerialName("mispar_halonot_hashmal") val electricWindows: Int? = null,
    @SerialName("mishkal_kolel") val totalWeight: Int? = null,
    @SerialName("kosher_grira_im_blamim") val towingCapacityWithBrakes: Int? = null,
    @SerialName("kosher_grira_bli_blamim") val towingCapacityWithoutBrakes: Int? = null,
    @SerialName("madad_yarok") val greenIndex: Double? = null,
    @SerialName("kvutzat_zihum") val emissionGroup: Int? = null,
    @SerialName("hege_koah_ind") val powerSteering: Int? = null,
    @SerialName("matzlemat_reverse_ind") val reverseCamera: Int? = null,
    @SerialName("galgaley_sagsoget_kala_ind") val alloyWheels: Int? = null,
    @SerialName("halon_bagg_ind") val sunroof: Int? = null,
    @SerialName("maarechet_ezer_labalam_ind") val brakeAssist: Int? = null,
    @SerialName("bakarat_shyut_adaptivit_ind") val adaptiveCruise: Int? = null,
    @SerialName("zihuy_holchey_regel_ind") val pedestrianDetection: Int? = null,
    @SerialName("zihuy_tamrurey_tnua_ind") val trafficSignDetection: Int? = null,
    @SerialName("zihuy_beshetah_nistar_ind") val blindSpotDetection: Int? = null,
    @SerialName("teura_automatit_benesiya_kadima_ind") val autoHeadlights: Int? = null,
    @SerialName("bakarat_stiya_menativ_ind") val laneDepartureWarning: Int? = null,
    @SerialName("nitur_merhak_milfanim_ind") val forwardCollisionWarning: Int? = null,
    @SerialName("hayshaney_lahatz_avir_batzmigim_ind") val tpms: Int? = null,
    @SerialName("shlita_automatit_beorot_gvohim_ind") val autoHighBeam: Int? = null
)

@Serializable
data class VehicleExtraHistoryRecord(
    @SerialName("_id") val id: Long? = null,
    @SerialName("mispar_rechev") val licensePlate: Long? = null,
    @SerialName("mispar_manoa") val engineNumber: String? = null,
    @SerialName("kilometer_test_aharon") val lastTestMileage: Long? = null,
    @SerialName("shinui_mivne_ind") val structuralChange: Int? = null,
    @SerialName("gapam_ind") val lpgInstalled: Int? = null,
    @SerialName("shnui_zeva_ind") val colorChange: Int? = null,
    @SerialName("shinui_zmig_ind") val tireChange: Int? = null,
    @SerialName("rishum_rishon_dt") val firstRegistrationDate: String? = null,
    @SerialName("mkoriut_nm") val originality: String? = null
)

@Serializable
data class DisabledPermitRecord(
    @SerialName("_id") val id: Long? = null,
    @SerialName("MISPAR RECHEV") val licensePlate: Long? = null,
    @SerialName("TAARICH HAFAKAT TAG") val issueDate: Long? = null,
    @SerialName("SUG TAV") val permitType: Long? = null
)

sealed interface TestStatus {
    data class Valid(val daysLeft: Long) : TestStatus
    data class ExpiringSoon(val daysLeft: Long) : TestStatus
    data class Expired(val daysPassed: Long) : TestStatus
    object Unknown : TestStatus
}

sealed interface SearchState {
    object Idle : SearchState
    object Loading : SearchState
    data class Success(
        val vehicle: VehicleRecord,
        val techSpec: VehicleTechnicalSpecRecord?,
        val extraHistory: VehicleExtraHistoryRecord?,
        val formattedPlate: String,
        val testStatus: TestStatus,
        val hasDisabledPermit: Boolean,
        val permitIssueDate: Long?
    ) : SearchState
    data class NotFound(val plate: String) : SearchState
    data class Error(val message: String) : SearchState
}

object VehicleUtils {
    fun parseTestStatus(testExpiryDateStr: String?): TestStatus {
        if (testExpiryDateStr.isNullOrBlank()) return TestStatus.Unknown
        return try {
            val expiryDate = LocalDate.parse(testExpiryDateStr.trim().take(10), DateTimeFormatter.ISO_LOCAL_DATE)
            val today = LocalDate.now()
            val daysDiff = ChronoUnit.DAYS.between(today, expiryDate)
            when {
                daysDiff < 0 -> TestStatus.Expired(Math.abs(daysDiff))
                daysDiff <= 30 -> TestStatus.ExpiringSoon(daysDiff)
                else -> TestStatus.Valid(daysDiff)
            }
        } catch (e: Exception) {
            TestStatus.Unknown
        }
    }

    fun formatPlate(raw: String): String {
        val clean = raw.filter { it.isDigit() }
        return when (clean.length) {
            7 -> "${clean.substring(0, 2)}-${clean.substring(2, 5)}-${clean.substring(5, 7)}"
            8 -> "${clean.substring(0, 3)}-${clean.substring(3, 5)}-${clean.substring(5, 8)}"
            else -> raw
        }
    }

    fun formatPermitDate(dateLong: Long?): String {
        if (dateLong == null || dateLong == 0L) return ""
        val s = dateLong.toString()
        return if (s.length == 8) {
            "${s.substring(6, 8)}/${s.substring(4, 6)}/${s.substring(0, 4)}"
        } else {
            s
        }
    }
}