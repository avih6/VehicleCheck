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
data class GovCountResponse(
    val success: Boolean = false,
    val result: GovCountResult? = null
)

@Serializable
data class GovCountResult(
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
data class PersonalImportRecord(
    @SerialName("_id") val id: Long? = null,
    @SerialName("mispar_rechev") val licensePlate: Long? = null,
    @SerialName("shilda") val vin: String? = null,
    @SerialName("tozeret_cd") val makeCode: Long? = null,
    @SerialName("tozeret_nm") val make: String? = null,
    @SerialName("sug_rechev_nm") val vehicleType: String? = null,
    @SerialName("degem_nm") val model: String? = null,
    @SerialName("mishkal_kolel") val totalWeight: Int? = null,
    @SerialName("shnat_yitzur") val year: Int? = null,
    @SerialName("nefach_manoa") val engineDisplacement: Int? = null,
    @SerialName("tozeret_eretz_nm") val countryOfOrigin: String? = null,
    @SerialName("degem_manoa") val engineModel: String? = null,
    @SerialName("mivchan_acharon_dt") val lastTestDate: String? = null,
    @SerialName("tokef_dt") val testExpiryDate: String? = null,
    @SerialName("sug_yevu") val importType: String? = null,
    @SerialName("moed_aliya_lakvish") val onRoadDate: String? = null,
    @SerialName("sug_delek_nm") val fuelType: String? = null
) {
    fun toVehicleRecord(): VehicleRecord {
        return VehicleRecord(
            id = id,
            licensePlate = licensePlate,
            make = make,
            makeCode = makeCode,
            model = model,
            modelCode = model,
            modelCd = null,
            modelType = if (!importType.isNullOrBlank()) "יבוא אישי ($importType)" else "יבוא אישי",
            trimLevel = null,
            year = year,
            onRoadDate = onRoadDate,
            lastTestDate = lastTestDate,
            testExpiryDate = testExpiryDate,
            ownership = "פרטי",
            color = null,
            colorCode = null,
            fuelType = fuelType,
            engineModel = engineModel,
            frontTire = null,
            rearTire = null,
            safetyRating = null,
            emissionGroup = null,
            vin = vin,
            registrationDirective = null
        )
    }
}

@Serializable
data class DeregisteredVehicleRecord(
    @SerialName("_id") val id: Long? = null,
    @SerialName("mispar_rechev") val licensePlateRaw: String? = null,
    @SerialName("tozeret_cd") val makeCodeRaw: String? = null,
    @SerialName("tozeret_nm") val make: String? = null,
    @SerialName("degem_cd") val modelCodeRaw: String? = null,
    @SerialName("degem_nm") val modelCode: String? = null,
    @SerialName("sug_rechev_nm") val vehicleType: String? = null,
    @SerialName("moed_aliya_lakvish") val onRoadDate: String? = null,
    @SerialName("bitul_dt") val cancellationDate: String? = null,
    @SerialName("misgeret") val vin: String? = null,
    @SerialName("degem_manoa") val engineModel: String? = null,
    @SerialName("mispar_manoa") val engineNumber: String? = null,
    @SerialName("mishkal_kolel") val totalWeightRaw: String? = null,
    @SerialName("ramat_gimur") val trimLevel: String? = null,
    @SerialName("shnat_yitzur") val yearRaw: String? = null,
    @SerialName("baalut") val ownership: String? = null,
    @SerialName("tzeva_rechev") val color: String? = null,
    @SerialName("zmig_kidmi") val frontTire: String? = null,
    @SerialName("zmig_ahori") val rearTire: String? = null,
    @SerialName("sug_delek_nm") val fuelType: String? = null,
    @SerialName("horaat_rishum") val registrationDirectiveRaw: String? = null,
    @SerialName("kinuy_mishari") val model: String? = null
) {
    fun toVehicleRecord(): VehicleRecord {
        val plate = licensePlateRaw?.filter { it.isDigit() }?.toLongOrNull()
        val year = yearRaw?.filter { it.isDigit() }?.toIntOrNull()
        val makeCd = makeCodeRaw?.filter { it.isDigit() }?.toLongOrNull()
        val modelCd = modelCodeRaw?.filter { it.isDigit() }?.toLongOrNull()
        val directive = registrationDirectiveRaw?.filter { it.isDigit() }?.toLongOrNull()

        return VehicleRecord(
            id = id,
            licensePlate = plate,
            make = make,
            makeCode = makeCd,
            model = if (!model.isNullOrBlank()) model else modelCode,
            modelCode = modelCode,
            modelCd = modelCd,
            modelType = vehicleType,
            trimLevel = trimLevel,
            year = year,
            onRoadDate = onRoadDate,
            lastTestDate = null,
            testExpiryDate = null,
            ownership = ownership,
            color = color,
            colorCode = null,
            fuelType = fuelType,
            engineModel = engineModel,
            frontTire = frontTire,
            rearTire = rearTire,
            safetyRating = null,
            emissionGroup = null,
            vin = vin,
            registrationDirective = directive
        )
    }
}

@Serializable
data class VehicleRecallRestrictionRecord(
    @SerialName("_id") val id: Long? = null,
    @SerialName("MISPAR_RECHEV") val licensePlate: Long? = null,
    @SerialName("RECALL_ID") val recallId: Long? = null,
    @SerialName("SUG_RECALL") val recallType: String? = null,
    @SerialName("SUG_TAKALA") val faultType: String? = null,
    @SerialName("TEUR_TAKALA") val faultDescription: String? = null,
    @SerialName("TAARICH_PTICHA") val openDate: String? = null
)

@Serializable
data class RecallDetailRecord(
    @SerialName("_id") val id: Long? = null,
    @SerialName("RECALL_ID") val recallId: Long? = null,
    @SerialName("TOZAR_TEUR") val makeName: String? = null,
    @SerialName("DEGEM") val model: String? = null,
    @SerialName("SUG_RECALL") val recallType: String? = null,
    @SerialName("SUG_TAKALA") val faultType: String? = null,
    @SerialName("TEUR_TAKALA") val faultDescription: String? = null,
    @SerialName("OFEN_TIKUN") val repairMethod: String? = null,
    @SerialName("YEVUAN_TEUR") val importerName: String? = null,
    @SerialName("TELEPHONE") val telephone: String? = null,
    @SerialName("WEBSITE") val website: String? = null
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

data class ModelYearCount(
    val year: Int,
    val activeCount: Int,
    val inactiveCount: Int
) {
    val totalCount: Int get() = activeCount + inactiveCount
    val activePercentage: Float get() = if (totalCount > 0) (activeCount.toFloat() / totalCount) * 100f else 100f
}

data class ModelStatistics(
    val totalActive: Int,
    val totalInactive: Int,
    val breakdownByYear: List<ModelYearCount> = emptyList()
) {
    val totalVehicles: Int get() = totalActive + totalInactive
    val activePercentage: Float get() = if (totalVehicles > 0) (totalActive.toFloat() / totalVehicles) * 100f else 100f
}

sealed interface TestStatus {
    data class Valid(val daysLeft: Long) : TestStatus
    data class ExpiringSoon(val daysLeft: Long) : TestStatus
    data class Expired(val daysPassed: Long) : TestStatus
    data class OffRoad(val offRoadDate: String) : TestStatus
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
        val isOffRoad: Boolean = false,
        val offRoadDate: String? = null,
        val stats: ModelStatistics = ModelStatistics(0, 0),
        val recalls: List<VehicleRecallRestrictionRecord> = emptyList(),
        val recallDetail: RecallDetailRecord? = null
    ) : SearchState
    data class NotFound(val plate: String) : SearchState
    data class Error(val message: String) : SearchState
}

object VehicleUtils {
    fun parseTestStatus(testExpiryDateStr: String?, isOffRoad: Boolean = false, offRoadDate: String? = null): TestStatus {
        if (isOffRoad) {
            return TestStatus.OffRoad(offRoadDate ?: "בוטל")
        }
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
            5 -> "${clean.substring(0, 2)}-${clean.substring(2, 5)}"
            6 -> "${clean.substring(0, 3)}-${clean.substring(3, 6)}"
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

    fun formatDate(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        val clean = raw.trim().take(10)
        return try {
            val d = LocalDate.parse(clean, DateTimeFormatter.ISO_LOCAL_DATE)
            d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } catch (e: Exception) {
            clean
        }
    }

    fun getBrandLogoUrl(hebrewMake: String?): String {
        val m = hebrewMake.orEmpty().lowercase()
        return when {
            m.contains("סובארו") || m.contains("subaru") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c9/Subaru_emblem.svg/512px-Subaru_emblem.svg.png"
            m.contains("טויוטה") || m.contains("toyota") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e7/Toyota.svg/512px-Toyota.svg.png"
            m.contains("יונדאי") || m.contains("hyundai") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/4/44/Hyundai_Motor_Company_logo.svg/512px-Hyundai_Motor_Company_logo.svg.png"
            m.contains("מאזדה") || m.contains("mazda") -> "https://upload.wikimedia.org/wikipedia/en/thumb/f/f6/Mazda_Logo.svg/512px-Mazda_Logo.svg.png"
            m.contains("קיה") || m.contains("kia") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/4/47/KIA_logo2.svg/512px-KIA_logo2.svg.png"
            m.contains("סקודה") || m.contains("skoda") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4c/Škoda_Auto_logo_%282023%29.svg/512px-Škoda_Auto_logo_%282023%29.svg.png"
            m.contains("מרצדס") || m.contains("mercedes") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/9/90/Mercedes-Logo.svg/512px-Mercedes-Logo.svg.png"
            m.contains("ב.מ.וו") || m.contains("bmw") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/4/44/BMW.svg/512px-BMW.svg.png"
            m.contains("אאודי") || m.contains("audi") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/9/92/Audi-Logo_2016.svg/512px-Audi-Logo_2016.svg.png"
            m.contains("פולקסווגן") || m.contains("volkswagen") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6d/Volkswagen_logo_2019.svg/512px-Volkswagen_logo_2019.svg.png"
            m.contains("דייהטסו") || m.contains("daihatsu") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/Daihatsu_logo.svg/512px-Daihatsu_logo.svg.png"
            m.contains("ניסאן") || m.contains("nissan") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8c/Nissan_2020_logo.svg/512px-Nissan_2020_logo.svg.png"
            m.contains("טסלה") || m.contains("tesla") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e8/Tesla_logo.png/512px-Tesla_logo.png"
            m.contains("רנו") || m.contains("renault") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b7/Renault_2021_Logo.svg/512px-Renault_2021_Logo.svg.png"
            m.contains("פיג'ו") || m.contains("peugeot") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f6/Peugeot_2021_Logo.svg/512px-Peugeot_2021_Logo.svg.png"
            m.contains("סיטרואן") || m.contains("citroen") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9a/Citroën_2022.svg/512px-Citroën_2022.svg.png"
            m.contains("הונדה") || m.contains("honda") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/7/7b/Honda_Logo.svg/512px-Honda_Logo.svg.png"
            m.contains("סוזוקי") || m.contains("suzuki") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/1/12/Suzuki_logo_2019.svg/512px-Suzuki_logo_2019.svg.png"
            m.contains("סיאט") || m.contains("seat") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/4/49/SEAT_Logo_from_2017.svg/512px-SEAT_Logo_from_2017.svg.png"
            m.contains("וולוו") || m.contains("volvo") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/2/29/Volvo-Iron-Mark-Black.svg/512px-Volvo-Iron-Mark-Black.svg.png"
            m.contains("מיצובישי") || m.contains("mitsubishi") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5a/Mitsubishi_logo.svg/512px-Mitsubishi_logo.svg.png"
            m.contains("בי ואי די") || m.contains("byd") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/9/9f/BYD_Auto_2022_logo.svg/512px-BYD_Auto_2022_logo.svg.png"
            m.contains("אם ג'י") || m.contains("mg") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a2/MG_Motor_logo_%282021%29.svg/512px-MG_Motor_logo_%282021%29.svg.png"
            m.contains("קופרה") || m.contains("cupra") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/9/90/Cupra_Logo.svg/512px-Cupra_Logo.svg.png"
            m.contains("ג'יפ") || m.contains("jeep") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/1/17/Jeep_logo.svg/512px-Jeep_logo.svg.png"
            m.contains("פורשה") || m.contains("porsche") -> "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8c/Porsche_Wappen.svg/512px-Porsche_Wappen.svg.png"
            else -> "https://raw.githubusercontent.com/filippofilip95/car-logos-dataset/master/logos/optimized/car.png"
        }
    }

    fun getEnglishMakeAndModel(hebrewMake: String?, model: String?): Pair<String, String> {
        val m = hebrewMake.orEmpty().lowercase()
        val makeEn = when {
            m.contains("סובארו") || m.contains("subaru") -> "subaru"
            m.contains("דייהטסו") || m.contains("daihatsu") -> "daihatsu"
            m.contains("טויוטה") || m.contains("toyota") -> "toyota"
            m.contains("יונדאי") || m.contains("hyundai") -> "hyundai"
            m.contains("קיה") || m.contains("kia") -> "kia"
            m.contains("מאזדה") || m.contains("mazda") -> "mazda"
            m.contains("סקודה") || m.contains("skoda") -> "skoda"
            m.contains("מרצדס") || m.contains("mercedes") -> "mercedes-benz"
            m.contains("ב.מ.וו") || m.contains("bmw") -> "bmw"
            m.contains("אאודי") || m.contains("audi") -> "audi"
            m.contains("פולקסווגן") || m.contains("volkswagen") -> "volkswagen"
            m.contains("רנו") || m.contains("renault") -> "renault"
            m.contains("פיג'ו") || m.contains("peugeot") -> "peugeot"
            m.contains("סיטרואן") || m.contains("citroen") -> "citroen"
            m.contains("ניסאן") || m.contains("nissan") -> "nissan"
            m.contains("הונדה") || m.contains("honda") -> "honda"
            m.contains("שברולט") || m.contains("chevrolet") -> "chevrolet"
            m.contains("פורד") || m.contains("ford") -> "ford"
            m.contains("סוזוקי") || m.contains("suzuki") -> "suzuki"
            m.contains("סיאט") || m.contains("seat") -> "seat"
            m.contains("וולוו") || m.contains("volvo") -> "volvo"
            m.contains("מיצובישי") || m.contains("mitsubishi") -> "mitsubishi"
            m.contains("טסלה") || m.contains("tesla") -> "tesla"
            m.contains("בי ואי די") || m.contains("byd") -> "byd"
            m.contains("ג'ילי") || m.contains("geely") -> "geely"
            m.contains("אם ג'י") || m.contains("mg") -> "mg"
            m.contains("קופרה") || m.contains("cupra") -> "cupra"
            m.contains("ג'יפ") || m.contains("jeep") -> "jeep"
            m.contains("לנד רובר") || m.contains("land rover") -> "land-rover"
            m.contains("פורשה") || m.contains("porsche") -> "porsche"
            else -> m.filter { it.isLetter() }
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