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
    @SerialName("kinuy_mishari") val commercialName: String? = null,
    @SerialName("tozeret_eretz_nm") val countryOfOrigin: String? = null,
    @SerialName("tozeret_nm") val makeName: String? = null,
    @SerialName("ramat_gimur") val trimLevel: String? = null,
    @SerialName("koah_sus") val horsepower: Int? = null,
    @SerialName("nefah_manoa") val engineDisplacement: Int? = null,
    @SerialName("automatic_ind") val isAutomatic: Int? = null,
    @SerialName("hanaa_nm") val driveType: String? = null,
    @SerialName("technologiat_hanaa_nm") val powertrainTech: String? = null,
    @SerialName("merkav") val bodyType: String? = null,
    @SerialName("mispar_moshavim") val seats: Int? = null,
    @SerialName("mispar_dlatot") val doors: Int? = null,
    @SerialName("mispar_kariot_avir") val airbags: Int? = null,
    @SerialName("mispar_halonot_hashmal") val electricWindows: Int? = null,
    @SerialName("mishkal_kolel") val totalWeight: Int? = null,
    @SerialName("gova") val height: Int? = null,
    @SerialName("kosher_grira_im_blamim") val towingCapacityWithBrakes: Int? = null,
    @SerialName("kosher_grira_bli_blamim") val towingCapacityWithoutBrakes: Int? = null,
    @SerialName("argaz_ind") val cargoBox: Int? = null,
    @SerialName("mazgan_ind") val airConditioning: Int? = null,
    @SerialName("abs_ind") val abs: Int? = null,
    @SerialName("hege_koah_ind") val powerSteering: Int? = null,
    @SerialName("halon_bagg_ind") val sunroof: Int? = null,
    @SerialName("galgaley_sagsoget_kala_ind") val alloyWheels: Int? = null,
    @SerialName("bakarat_yatzivut_ind") val stabilityControl: Int? = null,
    @SerialName("sug_tkina_nm") val standardType: String? = null,
    @SerialName("sug_mamir_nm") val catalystType: String? = null,
    @SerialName("kvuzat_agra_cd") val feeGroup: Int? = null,
    @SerialName("madad_yarok") val greenIndex: Double? = null,
    @SerialName("kvutzat_zihum") val emissionGroup: Int? = null,
    @SerialName("nikud_betihut") val safetyScore: Double? = null,
    @SerialName("ramat_eivzur_betihuty") val safetyEquipmentLevel: Int? = null,
    
    // Active Safety Checklist
    @SerialName("bakarat_stiya_menativ_ind") val laneDepartureWarning: Int? = null,
    @SerialName("bakarat_stiya_activ_s") val activeLaneDeparture: Int? = null,
    @SerialName("bakarat_shyut_adaptivit_ind") val adaptiveCruise: Int? = null,
    @SerialName("maarechet_ezer_labalam_ind") val brakeAssist: Int? = null,
    @SerialName("blima_otomatit_nesia_leahor") val reverseAutoBraking: Int? = null,
    @SerialName("blimat_hirum_lifnei_holhei_regel_ofanaim") val pedestrianBicycleEmergencyBrake: Int? = null,
    @SerialName("teura_automatit_benesiya_kadima_ind") val autoHeadlights: Int? = null,
    @SerialName("shlita_automatit_beorot_gvohim_ind") val autoHighBeam: Int? = null,
    @SerialName("bakarat_mehirut_isa") val intelligentSpeedAssist: Int? = null,
    @SerialName("nitur_merhak_milfanim_ind") val forwardCollisionWarning: Int? = null,
    @SerialName("hayshaney_lahatz_avir_batzmigim_ind") val tpms: Int? = null,
    @SerialName("hayshaney_hagorot_ind") val seatbeltSensors: Int? = null,
    @SerialName("matzlemat_reverse_ind") val reverseCamera: Int? = null,
    @SerialName("alco_lock") val alcoholLockReady: Int? = null,
    @SerialName("zihuy_matzav_hitkarvut_mesukenet_ind") val dangerousApproachDetection: Int? = null,
    @SerialName("zihuy_holchey_regel_ind") val pedestrianDetection: Int? = null,
    @SerialName("zihuy_tamrurey_tnua_ind") val trafficSignDetection: Int? = null,
    @SerialName("zihuy_beshetah_nistar_ind") val blindSpotDetection: Int? = null,
    @SerialName("hitnagshut_cad_shetah_met") val sideCollisionPrevention: Int? = null,
    @SerialName("zihuy_rechev_do_galgali") val twoWheelerDetection: Int? = null,

    // Environmental Emissions breakdown
    @SerialName("kamut_CO_city") val cityCO: Double? = null,
    @SerialName("kamut_CO2_city") val cityCO2: Double? = null,
    @SerialName("kamut_NOX_city") val cityNOX: Double? = null,
    @SerialName("kamut_HC_city") val cityHC: Double? = null,
    @SerialName("kamut_PM10_city") val cityPM10: Double? = null,
    @SerialName("kamut_CO_hway") val hwayCO: Double? = null,
    @SerialName("kamut_CO2_hway") val hwayCO2: Double? = null,
    @SerialName("kamut_NOX_hway") val hwayNOX: Double? = null,
    @SerialName("kamut_HC_hway") val hwayHC: Double? = null,
    @SerialName("kamut_PM10_hway") val hwayPM10: Double? = null,
    @SerialName("CO_WLTP") val wltpCO: Double? = null,
    @SerialName("CO2_WLTP") val wltpCO2: Double? = null,
    @SerialName("NOX_WLTP") val wltpNOX: Double? = null,
    @SerialName("HC_WLTP") val wltpHC: Double? = null,
    @SerialName("PM_WLTP") val wltpPM: Double? = null
)

@Serializable
data class VehicleImporterPriceRecord(
    @SerialName("_id") val id: Long? = null,
    @SerialName("shem_yevuan") val importerName: String? = null,
    @SerialName("mehir") val importerPrice: Long? = null,
    @SerialName("tozeret_cd") val makeCode: Long? = null,
    @SerialName("degem_cd") val modelCode: Long? = null,
    @SerialName("shnat_yitzur") val year: Int? = null,
    @SerialName("kinuy_mishari") val commercialName: String? = null
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
        val importerInfo: VehicleImporterPriceRecord?,
        val extraHistory: VehicleExtraHistoryRecord?,
        val formattedPlate: String,
        val testStatus: TestStatus,
        val hasDisabledPermit: Boolean,
        val permitIssueDate: Long?,
        val sameModelActiveCount: Int = 0
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

    fun getEnglishMakeAndModel(hebrewMake: String?, model: String?): Pair<String, String> {
        val m = hebrewMake.orEmpty()
        val makeEn = when {
            m.contains("סובארו") || m.contains("SUBARU", ignoreCase = true) -> "subaru"
            m.contains("טויוטה") || m.contains("TOYOTA", ignoreCase = true) -> "toyota"
            m.contains("יונדאי") || m.contains("HYUNDAI", ignoreCase = true) -> "hyundai"
            m.contains("קיה") || m.contains("KIA", ignoreCase = true) -> "kia"
            m.contains("מאזדה") || m.contains("MAZDA", ignoreCase = true) -> "mazda"
            m.contains("סקודה") || m.contains("SKODA", ignoreCase = true) -> "skoda"
            m.contains("מרצדס") || m.contains("MERCEDES", ignoreCase = true) -> "mercedes-benz"
            m.contains("ב.מ.וו") || m.contains("BMW", ignoreCase = true) -> "bmw"
            m.contains("אאודי") || m.contains("AUDI", ignoreCase = true) -> "audi"
            m.contains("פולקסווגן") || m.contains("VOLKSWAGEN", ignoreCase = true) -> "volkswagen"
            m.contains("רנו") || m.contains("RENAULT", ignoreCase = true) -> "renault"
            m.contains("פיג'ו") || m.contains("PEUGEOT", ignoreCase = true) -> "peugeot"
            m.contains("סיטרואן") || m.contains("CITROEN", ignoreCase = true) -> "citroen"
            m.contains("ניסאן") || m.contains("NISSAN", ignoreCase = true) -> "nissan"
            m.contains("הונדה") || m.contains("HONDA", ignoreCase = true) -> "honda"
            m.contains("שברולט") || m.contains("CHEVROLET", ignoreCase = true) -> "chevrolet"
            m.contains("פורד") || m.contains("FORD", ignoreCase = true) -> "ford"
            m.contains("סוזוקי") || m.contains("SUZUKI", ignoreCase = true) -> "suzuki"
            m.contains("סיאט") || m.contains("SEAT", ignoreCase = true) -> "seat"
            m.contains("וולוו") || m.contains("VOLVO", ignoreCase = true) -> "volvo"
            m.contains("מיצובישי") || m.contains("MITSUBISHI", ignoreCase = true) -> "mitsubishi"
            m.contains("טסלה") || m.contains("TESLA", ignoreCase = true) -> "tesla"
            m.contains("בי ואי די") || m.contains("BYD", ignoreCase = true) -> "byd"
            m.contains("ג'ילי") || m.contains("GEELY", ignoreCase = true) -> "geely"
            m.contains("אם ג'י") || m.contains("MG", ignoreCase = true) -> "mg"
            m.contains("קופרה") || m.contains("CUPRA", ignoreCase = true) -> "cupra"
            m.contains("ג'יפ") || m.contains("JEEP", ignoreCase = true) -> "jeep"
            m.contains("לנד רובר") || m.contains("LAND ROVER", ignoreCase = true) -> "land-rover"
            m.contains("פורשה") || m.contains("PORSCHE", ignoreCase = true) -> "porsche"
            else -> m.filter { it.isLetter() }.lowercase()
        }

        val mod = model.orEmpty().lowercase()
        val modelClean = mod
            .replace("4x4", "")
            .replace("awd", "")
            .replace("2x4", "")
            .replace("hybrid", "")
            .trim()
            .split(" ", "-", "_")
            .firstOrNull { it.isNotBlank() } ?: "car"

        return Pair(makeEn, modelClean)
    }

    fun calculateAnnualLicensingFee(feeGroup: Int?, year: Int?): Long {
        val group = feeGroup ?: 4
        val currentYear = LocalDate.now().year
        val age = if (year != null) (currentYear - year).coerceAtLeast(0) else 2
        return when (group) {
            1 -> if (age <= 3) 890L else if (age <= 6) 780L else 680L
            2 -> if (age <= 3) 1240L else if (age <= 6) 1050L else 910L
            3 -> if (age <= 3) 1580L else if (age <= 6) 1320L else 1140L
            4 -> if (age <= 3) 1998L else if (age <= 6) 1680L else 1420L
            5 -> if (age <= 3) 2490L else if (age <= 6) 2080L else 1760L
            6 -> if (age <= 3) 3250L else if (age <= 6) 2710L else 2280L
            7 -> if (age <= 3) 4590L else if (age <= 6) 3810L else 3190L
            else -> 1998L
        }
    }
}