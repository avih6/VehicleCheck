package com.avih6.vehiclecheck.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object FlexibleLongSerializer : KSerializer<Long?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FlexibleLong", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Long?) {
        if (value != null) encoder.encodeLong(value) else encoder.encodeNull()
    }
    override fun deserialize(decoder: Decoder): Long? {
        val jsonDecoder = decoder as? JsonDecoder ?: return null
        val element = jsonDecoder.decodeJsonElement()
        if (element is JsonNull) return null
        val str = element.jsonPrimitive.content.trim()
        return str.filter { it.isDigit() || it == '-' }.toLongOrNull()
    }
}

object FlexibleIntSerializer : KSerializer<Int?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FlexibleInt", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Int?) {
        if (value != null) encoder.encodeInt(value) else encoder.encodeNull()
    }
    override fun deserialize(decoder: Decoder): Int? {
        val jsonDecoder = decoder as? JsonDecoder ?: return null
        val element = jsonDecoder.decodeJsonElement()
        if (element is JsonNull) return null
        val str = element.jsonPrimitive.content.trim()
        return str.filter { it.isDigit() || it == '-' }.toIntOrNull()
    }
}

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
data class GovResourceShowResponse(
    val success: Boolean = false,
    val result: GovResourceMetadata? = null
)

@Serializable
data class GovResourceMetadata(
    val id: String? = null,
    @SerialName("last_modified") val lastModified: String? = null,
    @SerialName("metadata_modified") val metadataModified: String? = null
)

@Serializable
data class VehicleRecord(
    @SerialName("_id") val id: Long? = null,
    @Serializable(with = FlexibleLongSerializer::class) @SerialName("mispar_rechev") val licensePlate: Long? = null,
    @SerialName("tozeret_nm") val make: String? = null,
    @Serializable(with = FlexibleLongSerializer::class) @SerialName("tozeret_cd") val makeCode: Long? = null,
    @SerialName("kinuy_mishari") val model: String? = null,
    @SerialName("degem_nm") val modelCode: String? = null,
    @Serializable(with = FlexibleLongSerializer::class) @SerialName("degem_cd") val modelCd: Long? = null,
    @SerialName("sug_degem") val modelType: String? = null,
    @SerialName("ramat_gimur") val trimLevel: String? = null,
    @Serializable(with = FlexibleIntSerializer::class) @SerialName("shnat_yitzur") val year: Int? = null,
    @SerialName("moed_aliya_lakvish") val onRoadDate: String? = null,
    @SerialName("mivchan_acharon_dt") val lastTestDate: String? = null,
    @SerialName("tokef_dt") val testExpiryDate: String? = null,
    @SerialName("baalut") val ownership: String? = null,
    @SerialName("tzeva_rechev") val color: String? = null,
    @Serializable(with = FlexibleIntSerializer::class) @SerialName("tzeva_cd") val colorCode: Int? = null,
    @SerialName("sug_delek_nm") val fuelType: String? = null,
    @SerialName("degem_manoa") val engineModel: String? = null,
    @SerialName("zmig_kidmi") val frontTire: String? = null,
    @SerialName("zmig_ahori") val rearTire: String? = null,
    @Serializable(with = FlexibleIntSerializer::class) @SerialName("ramat_eivzur_betihuty") val safetyRating: Int? = null,
    @Serializable(with = FlexibleIntSerializer::class) @SerialName("kvutzat_zihum") val emissionGroup: Int? = null,
    @SerialName("misgeret") val vin: String? = null,
    @Serializable(with = FlexibleLongSerializer::class) @SerialName("horaat_rishum") val registrationDirective: Long? = null
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
    @SerialName("sug_degem") val sugDegem: String? = null,
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
    @SerialName("kinuy_mishari") val model: String? = null,
    @SerialName("mivchan_acharon_dt") val lastTestDate: String? = null,
    @SerialName("mivchan_aharon_dt") val lastTestDateAlt: String? = null,
    @SerialName("tokef_dt") val testExpiryDate: String? = null
) {
    fun toVehicleRecord(): VehicleRecord {
        val plate = licensePlateRaw?.filter { it.isDigit() }?.toLongOrNull()
        val year = yearRaw?.filter { it.isDigit() }?.toIntOrNull()
        val makeCd = makeCodeRaw?.filter { it.isDigit() }?.toLongOrNull()
        val modelCd = modelCodeRaw?.filter { it.isDigit() }?.toLongOrNull()
        val directive = registrationDirectiveRaw?.filter { it.isDigit() }?.toLongOrNull()
        val effectiveLastTest = lastTestDate ?: lastTestDateAlt
        val effectiveExpiry = testExpiryDate ?: cancellationDate

        return VehicleRecord(
            id = id,
            licensePlate = plate,
            make = make,
            makeCode = makeCd,
            model = if (!model.isNullOrBlank()) model else modelCode,
            modelCode = modelCode,
            modelCd = modelCd,
            modelType = if (!sugDegem.isNullOrBlank()) sugDegem else vehicleType,
            trimLevel = trimLevel,
            year = year,
            onRoadDate = onRoadDate,
            lastTestDate = effectiveLastTest,
            testExpiryDate = effectiveExpiry,
            ownership = if (!ownership.isNullOrBlank()) ownership else "פרטי",
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
data class EngineeringEquipmentRecord(
    @SerialName("_id") val id: Long? = null,
    @SerialName("mispar_tzama") val licensePlate: Long? = null,
    @SerialName("mispar_shilda") val vin: String? = null,
    @SerialName("shilda_totzar_cd") val makeCode: Long? = null,
    @SerialName("shilda_totzar_en_nm") val makeName: String? = null,
    @SerialName("degem_nm") val modelName: String? = null,
    @SerialName("shnat_yitzur") val year: Int? = null,
    @SerialName("sug_tzama_nm") val vehicleType: String? = null,
    @SerialName("hanaa_nm") val driveType: String? = null,
    @SerialName("rishum_date") val registrationDate: String? = null,
    @SerialName("koah_sus") val horsepower: Int? = null,
    @SerialName("mishkal_ton") val weightTon: Double? = null,
    @SerialName("mishkal_kolel_ton") val totalWeightTon: Double? = null,
    @SerialName("tokef_date") val expirationDate: String? = null,
    @SerialName("kosher_harama_ton") val liftingCapacityTon: Double? = null,
    @SerialName("hagbala_nm_1") val restriction1: String? = null
) {
    fun toVehicleRecord(): VehicleRecord {
        return VehicleRecord(
            id = id,
            licensePlate = licensePlate ?: 0L,
            make = makeName ?: "ציוד הנדסי",
            model = modelName ?: vehicleType ?: "צמ\"ה",
            year = year ?: 0,
            onRoadDate = registrationDate,
            testExpiryDate = expirationDate,
            ownership = "ציוד מכני הנדסי (צמ\"ה)",
            fuelType = "דיזל / מנוע תעשייתי",
            color = "צהוב / תעשייתי",
            vin = vin
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
    @SerialName("TOZAR_CD") val makeCode: Long? = null,
    @SerialName("TOZAR_TEUR") val makeName: String? = null,
    @SerialName("DEGEM") val model: String? = null,
    @SerialName("SHNAT_RECALL") val recallYear: Int? = null,
    @SerialName("BUILD_BEGIN_A") val buildStart: String? = null,
    @SerialName("BUILD_END_A") val buildEnd: String? = null,
    @SerialName("SUG_RECALL") val recallType: String? = null,
    @SerialName("SUG_TAKALA") val faultType: String? = null,
    @SerialName("TEUR_TAKALA") val faultDescription: String? = null,
    @SerialName("OFEN_TIKUN") val repairMethod: String? = null,
    @SerialName("TKINA_EU") val euStandard: String? = null,
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
    @SerialName("co_wltp") val wltpCO: Double? = null,
    @SerialName("co2_wltp") val wltpCO2: Double? = null,
    @SerialName("nox_wltp") val wltpNOX: Double? = null,
    @SerialName("hc_wltp") val wltpHC: Double? = null,
    @SerialName("pm_wltp") val wltpPM: Double? = null
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
        val recallDetail: RecallDetailRecord? = null,
        val isEngineeringEquipment: Boolean = false,
        val equipmentDetails: EngineeringEquipmentRecord? = null,
        val alternateEquipment: EngineeringEquipmentRecord? = null,
        val alternateVehicle: VehicleRecord? = null
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
            try {
                val parts = clean.split("-")
                if (parts.size == 2) {
                    val year = parts[0].toIntOrNull()
                    val month = parts[1].toIntOrNull()
                    if (year != null && month != null && month in 1..12) {
                        return "%02d/%d".format(month, year)
                    }
                }
            } catch (ignored: Exception) {}
            clean
        }
    }

    fun formatDateTime(isoStr: String?): String {
        if (isoStr.isNullOrBlank()) return ""
        return try {
            val clean = isoStr.trim().take(19)
            val ldt = java.time.LocalDateTime.parse(clean)
            val zdt = ldt.atZone(java.time.ZoneId.of("UTC")).withZoneSameInstant(java.time.ZoneId.of("Asia/Jerusalem"))
            zdt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy • HH:mm"))
        } catch (e: Exception) {
            try {
                val d = LocalDate.parse(isoStr.trim().take(10), DateTimeFormatter.ISO_LOCAL_DATE)
                d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            } catch (e2: Exception) {
                isoStr
            }
        }
    }

    fun parseLocalDate(raw: String?): LocalDate? {
        if (raw.isNullOrBlank()) return null
        val clean = raw.trim().take(10)
        return try {
            LocalDate.parse(clean, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (_: Exception) {
            try {
                LocalDate.parse(clean, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            } catch (_: Exception) {
                try {
                    val parts = clean.split("-")
                    if (parts.size == 2) {
                        val y = parts[0].toIntOrNull()
                        val m = parts[1].toIntOrNull()
                        if (y != null && m != null && m in 1..12) {
                            LocalDate.of(y, m, 1)
                        } else null
                    } else null
                } catch (_: Exception) {
                    null
                }
            }
        }
    }

    fun calculateDateDifferenceHebrew(dateStr: String?): String? {
        val targetDate = parseLocalDate(dateStr) ?: return null
        val today = LocalDate.now()
        
        val isPast = targetDate.isBefore(today)
        val start = if (isPast) targetDate else today
        val end = if (isPast) today else targetDate
        val period = java.time.Period.between(start, end)

        val years = period.years
        val months = period.months
        val days = period.days

        val parts = mutableListOf<String>()
        if (years > 0) {
            parts.add(if (years == 1) "שנה אחת" else if (years == 2) "שנתיים" else "$years שנים")
        }
        if (months > 0) {
            parts.add(if (months == 1) "חודש אחד" else if (months == 2) "חודשיים" else "$months חודשים")
        }
        if (days > 0 && years == 0) {
            parts.add(if (days == 1) "יום אחד" else if (days == 2) "יומיים" else "$days ימים")
        } else if (parts.isEmpty()) {
            parts.add(if (days <= 1) "היום" else "$days ימים")
        }

        val formattedDiff = parts.joinToString(if (parts.size > 2) ", " else " ו-")
        return if (isPast) {
            "איחור של $formattedDiff (עברו)"
        } else {
            "נותרו עוד $formattedDiff"
        }
    }

    fun getEstimatedLastTestDate(testExpiryDateStr: String?, lastTestDateStr: String?): String? {
        if (!lastTestDateStr.isNullOrBlank()) return lastTestDateStr
        val expiry = parseLocalDate(testExpiryDateStr) ?: return null
        val estimatedLast = expiry.minusYears(1)
        return estimatedLast.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    fun calculateAnnualLicensingFee(feeGroup: Int, year: Int?): Int {
        val currentYear = LocalDate.now().year
        val vehicleAge = if (year != null && year > 1900) (currentYear - year).coerceAtLeast(0) else 0

        val baseFee = when (feeGroup) {
            1 -> 1235
            2 -> 1420
            3 -> 1636
            4 -> 2060
            5 -> 2340
            6 -> 2750
            7 -> 3350
            else -> 1800
        }

        return when {
            vehicleAge >= 10 -> (baseFee * 0.65).toInt()
            vehicleAge >= 4 -> (baseFee * 0.85).toInt()
            else -> baseFee
        }
    }

    fun getBrandLogoUrl(hebrewMake: String?): String {
        val slug = getBrandSlug(hebrewMake)
        return "https://raw.githubusercontent.com/filippofilip95/car-logos-dataset/master/logos/optimized/$slug.png"
    }

    fun extractEnglishName(make: String?): String? {
        if (make.isNullOrBlank()) return null
        val regex = Regex("[A-Za-z0-9]+")
        val matches = regex.findAll(make).map { it.value }.toList()
        val filterWords = setOf("usa", "uk", "ltd", "gmbh", "co", "corp", "inc", "motor", "motors", "auto", "car")
        val cleanMatches = matches.filter { it.lowercase() !in filterWords }
        if (cleanMatches.isNotEmpty()) {
            return cleanMatches.joinToString("-").lowercase()
        }
        return null
    }

    fun getBrandSlug(hebrewMake: String?): String {
        val m = hebrewMake.orEmpty().lowercase()
        val predefined = when {
            m.contains("סובארו") || m.contains("subaru") -> "subaru"
            m.contains("טויוטה") || m.contains("toyota") -> "toyota"
            m.contains("יונדאי") || m.contains("hyundai") -> "hyundai"
            m.contains("קיה") || m.contains("kia") -> "kia"
            m.contains("מאזדה") || m.contains("mazda") -> "mazda"
            m.contains("סקודה") || m.contains("skoda") -> "skoda"
            m.contains("מרצדס") || m.contains("mercedes") -> "mercedes-benz"
            m.contains("ב.מ.וו") || m.contains("במוו") || m.contains("bmw") || m.contains("b.m.w") -> "bmw"
            m.contains("אאודי") || m.contains("אודי") || m.contains("audi") -> "audi"
            m.contains("פולקסווגן") || m.contains("פולקסוואגן") || m.contains("volkswagen") || m.contains("vw") -> "volkswagen"
            m.contains("דייהטסו") || m.contains("דייהטס'ו") || m.contains("daihatsu") -> "daihatsu"
            m.contains("ניסאן") || m.contains("nissan") -> "nissan"
            m.contains("טסלה") || m.contains("tesla") -> "tesla"
            m.contains("רנו") || m.contains("renault") -> "renault"
            m.contains("פיג'ו") || m.contains("פיגו") || m.contains("peugeot") -> "peugeot"
            m.contains("סיטרואן") || m.contains("citroen") -> "citroen"
            m.contains("הונדה") || m.contains("honda") -> "honda"
            m.contains("סוזוקי") || m.contains("suzuki") -> "suzuki"
            m.contains("סיאט") || m.contains("seat") -> "seat"
            m.contains("וולוו") || m.contains("volvo") -> "volvo"
            m.contains("מיצובישי") || m.contains("mitsubishi") -> "mitsubishi"
            m.contains("בי ואי די") || m.contains("בי.ואי.די") || m.contains("byd") || m.contains("b.y.d") -> "byd"
            m.contains("אם ג'י") || m.contains("אם.ג'י") || m.contains("mg") || m.contains("m.g") -> "mg"
            m.contains("פורד") || m.contains("ford") -> "ford"
            m.contains("שברולט") || m.contains("chevrolet") || m.contains("chevy") -> "chevrolet"
            m.contains("לנד רובר") || m.contains("לנדרובר") || m.contains("land rover") || m.contains("land-rover") -> "land-rover"
            m.contains("ג'ילי") || m.contains("geely") || m.contains("ג.י.ל.י") -> "geely"
            m.contains("קופרה") || m.contains("cupra") -> "cupra"
            m.contains("ג'יפ") || m.contains("גיפ") || m.contains("jeep") -> "jeep"
            m.contains("פורשה") || m.contains("porsche") -> "porsche"
            m.contains("אלפא") || m.contains("alfa") -> "alfa-romeo"
            m.contains("פיאט") || m.contains("fiat") -> "fiat"
            m.contains("אופל") || m.contains("opel") -> "opel"
            m.contains("אינפיניטי") || m.contains("infiniti") -> "infiniti"
            m.contains("לקסוס") || m.contains("lexus") -> "lexus"
            m.contains("יגואר") || m.contains("jaguar") -> "jaguar"
            m.contains("מיני") || m.contains("mini") -> "mini"
            m.contains("דאצ'יה") || m.contains("דאציה") || m.contains("dacia") -> "dacia"
            m.contains("קאדילאק") || m.contains("cadillac") -> "cadillac"
            m.contains("קרייזלר") || m.contains("chrysler") -> "chrysler"
            m.contains("דודג'") || m.contains("דודג") || m.contains("dodge") -> "dodge"
            m.contains("ראם") || m.contains("ram") -> "ram"
            m.contains("סמארט") || m.contains("smart") -> "smart"
            m.contains("סאנגיונג") || m.contains("סאנג יונג") || m.contains("ssangyong") || m.contains("kgm") || m.contains("קגמ") -> "ssangyong"
            m.contains("איסוזו") || m.contains("isuzu") -> "isuzu"
            m.contains("אברט") || m.contains("abarth") -> "abarth"
            m.contains("אסטון") || m.contains("aston") -> "aston-martin"
            m.contains("בנטלי") || m.contains("bentley") -> "bentley"
            m.contains("פרארי") || m.contains("ferrari") -> "ferrari"
            m.contains("למבורגיני") || m.contains("lamborghini") -> "lamborghini"
            m.contains("מזראטי") || m.contains("maserati") -> "maserati"
            m.contains("רולס") || m.contains("rolls") -> "rolls-royce"
            m.contains("מקלארן") || m.contains("mclaren") -> "mclaren"
            m.contains("זיקר") || m.contains("זיקיר") || m.contains("zeekr") -> "zeekr"
            m.contains("אקספנג") || m.contains("xpeng") -> "xpeng"
            m.contains("צ'רי") || m.contains("צרי") || m.contains("chery") -> "chery"
            m.contains("איוויז") || m.contains("איווייז") || m.contains("aiways") -> "aiways"
            m.contains("ליפמוטור") || m.contains("ליפ מוטור") || m.contains("leapmotor") || m.contains("leap") -> "leapmotor"
            m.contains("סרס") || m.contains("seres") || m.contains("dfsk") -> "seres"
            m.contains("וויה") || m.contains("ויה") || m.contains("voyah") -> "voyah"
            m.contains("פולסטאר") || m.contains("פול סטאר") || m.contains("polestar") -> "polestar"
            m.contains("לינק") || m.contains("lynk") -> "lynk-co"
            m.contains("הונגצ'י") || m.contains("הונג צ'י") || m.contains("hongqi") -> "hongqi"
            m.contains("ג'נסיס") || m.contains("גנסיס") || m.contains("genesis") -> "genesis"
            m.contains("אורה") || m.contains("ora") || m.contains("gwm") || m.contains("גרייט וול") -> "ora"
            m.contains("סקייוול") || m.contains("סקיי וול") || m.contains("skywell") -> "skywell"
            m.contains("דונגפנג") || m.contains("dongfeng") -> "dongfeng"
            m.contains("ג'קו") || m.contains("ג'אקו") || m.contains("גקו") || m.contains("jaecoo") -> "jaecoo"
            m.contains("אומודה") || m.contains("omoda") -> "omoda"
            m.contains("מקסוס") || m.contains("maxus") -> "maxus"
            m.contains("נטה") || m.contains("נטא") || m.contains("neta") -> "neta"
            m.contains("גאק") || m.contains("ג'אק") || m.contains("gac") -> "gac"
            m.contains("צ'אנגאן") || m.contains("צ'נגאן") || m.contains("changan") -> "changan"
            m.contains("פורתינג") || m.contains("פורת'ינג") || m.contains("forthing") -> "forthing"
            m.contains("ויי") || m.contains("וויי") || m.contains("wey") -> "wey"
            m.contains("פאריזון") || m.contains("farizon") -> "farizon"
            m.contains("לוסיד") || m.contains("lucid") -> "lucid"
            m.contains("ריביאן") || m.contains("rivian") -> "rivian"
            m.contains("אלפין") || m.contains("alpine") -> "alpine"
            m.contains("לוטוס") || m.contains("lotus") -> "lotus"
            m.contains("לנצ'יה") || m.contains("לנציה") || m.contains("lancia") -> "lancia"
            m.contains("סאאב") || m.contains("saab") -> "saab"
            m.contains("רובר") || m.contains("rover") -> "rover"
            m.contains("ביואיק") || m.contains("buick") -> "buick"
            m.contains("ג'י אם סי") || m.contains("ג'י.אם.סי") || m.contains("ג'מאס") || m.contains("gmc") -> "gmc"
            m.contains("לינקולן") || m.contains("lincoln") -> "lincoln"
            m.contains("פונטיאק") || m.contains("pontiac") -> "pontiac"
            m.contains("אולדסמוביל") || m.contains("oldsmobile") -> "oldsmobile"
            m.contains("בוגאטי") || m.contains("bugatti") -> "bugatti"
            m.contains("פגאני") || m.contains("pagani") -> "pagani"
            m.contains("קוניגסג") || m.contains("koenigsegg") -> "koenigsegg"
            m.contains("קומטסו") || m.contains("קומטס'ו") || m.contains("komatsu") -> "komatsu"
            m.contains("קטרפילר") || m.contains("קטרפילאר") || m.contains("caterpillar") || m.contains("cat") -> "caterpillar"
            m.contains("ג'י סי בי") || m.contains("jcb") -> "jcb"
            m.contains("בובקט") || m.contains("bobcat") -> "bobcat"
            m.contains("ג'ון דיר") || m.contains("john deere") -> "john-deere"
            m.contains("סקניה") || m.contains("scania") -> "scania"
            m.contains("מאן") || m.contains("man") -> "man"
            m.contains("דאף") || m.contains("daf") -> "daf"
            m.contains("איווקו") || m.contains("איבקו") || m.contains("iveco") -> "iveco"
            m.contains("מאק") || m.contains("mack") -> "mack"
            m.contains("גולדן דרגון") || m.contains("golden dragon") -> "golden-dragon"
            m.contains("יוטונג") || m.contains("yutong") -> "yutong"
            m.contains("הייגר") || m.contains("היגר") || m.contains("higer") -> "higer"
            m.contains("קינג לונג") || m.contains("king long") -> "king-long"
            m.contains("אנקאי") || m.contains("ankai") -> "ankai"
            m.contains("פוטון") || m.contains("foton") -> "foton"
            m.contains("ימאהה") || m.contains("yamaha") -> "yamaha"
            m.contains("קוואסאקי") || m.contains("קאוואסאקי") || m.contains("kawasaki") -> "kawasaki"
            m.contains("הארלי") || m.contains("harley") -> "harley-davidson"
            m.contains("דוקאטי") || m.contains("ducati") -> "ducati"
            m.contains("קיי טי אם") || m.contains("ק.ט.מ") || m.contains("ktm") -> "ktm"
            m.contains("סאן יאנג") || m.contains("סאניאנג") || m.contains("סים") || m.contains("sym") -> "sym"
            m.contains("קימקו") || m.contains("kymco") -> "kymco"
            m.contains("פיאג'ו") || m.contains("פיאגיו") || m.contains("piaggio") -> "piaggio"
            m.contains("וספה") || m.contains("vespa") -> "vespa"
            m.contains("אפריליה") || m.contains("aprilia") -> "aprilia"
            m.contains("מוטו גוצי") || m.contains("moto guzzi") -> "moto-guzzi"
            m.contains("טריומף") || m.contains("triumph") -> "triumph"
            m.contains("רויאל אנפילד") || m.contains("royal enfield") -> "royal-enfield"
            m.contains("הוסקוורנה") || m.contains("husqvarna") -> "husqvarna"
            m.contains("בנלי") || m.contains("בנאלי") || m.contains("benelli") -> "benelli"
            m.contains("סי אף מוטו") || m.contains("cfmoto") -> "cfmoto"
            m.contains("ווג") || m.contains("voge") -> "voge"
            m.contains("קיו ג'יי") || m.contains("qjmotor") -> "qjmotor"
            else -> null
        }
        if (predefined != null) return predefined

        val extracted = extractEnglishName(hebrewMake)
        if (!extracted.isNullOrBlank()) return extracted

        return hebrewMake.orEmpty()
            .replace(Regex("[^א-תA-Za-z0-9\\s]"), "")
            .trim()
            .replace(" ", "-")
            .lowercase()
            .ifBlank { "car" }
    }

    fun getEnglishMakeAndModel(hebrewMake: String?, model: String?): Pair<String, String> {
        val makeEn = getBrandSlug(hebrewMake)
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

    fun getCountryIsoCode(countryName: String?): String? {
        val c = countryName.orEmpty().trim().lowercase()
        return when {
            c.contains("גרמניה") || c.contains("germany") -> "de"
            c.contains("יפן") || c.contains("japan") -> "jp"
            c.contains("ארה\"ב") || c.contains("ארצות הברית") || c.contains("ארהב") || c.contains("usa") || c.contains("united states") -> "us"
            c.contains("צרפת") || c.contains("france") -> "fr"
            c.contains("קוריאה") || c.contains("korea") -> "kr"
            c.contains("שוודיה") || c.contains("שבדיה") || c.contains("sweden") -> "se"
            c.contains("בריטניה") || c.contains("אנגליה") || c.contains("הממלכה המאוחדת") || c.contains("uk") || c.contains("england") -> "gb"
            c.contains("איטליה") || c.contains("italy") -> "it"
            c.contains("צ'כיה") || c.contains("צכיה") || c.contains("czech") -> "cz"
            c.contains("ספרד") || c.contains("spain") -> "es"
            c.contains("סין") || c.contains("china") -> "cn"
            c.contains("רומניה") || c.contains("romania") -> "ro"
            c.contains("הודו") || c.contains("india") -> "in"
            c.contains("טורקיה") || c.contains("תורכיה") || c.contains("turkey") -> "tr"
            c.contains("ישראל") || c.contains("israel") -> "il"
            c.contains("סלובקיה") || c.contains("slovakia") -> "sk"
            c.contains("הונגריה") || c.contains("hungary") -> "hu"
            c.contains("פולין") || c.contains("poland") -> "pl"
            c.contains("מקסיקו") || c.contains("mexico") -> "mx"
            c.contains("קנדה") || c.contains("canada") -> "ca"
            c.contains("אוסטריה") || c.contains("austria") -> "at"
            c.contains("בלגיה") || c.contains("belgium") -> "be"
            c.contains("הולנד") || c.contains("netherlands") -> "nl"
            c.contains("תאילנד") || c.contains("thailand") -> "th"
            c.contains("דרום אפריקה") || c.contains("south africa") -> "za"
            else -> null
        }
    }

    fun getCountryFlagUrl(countryName: String?): String? {
        val iso = getCountryIsoCode(countryName) ?: return null
        return "https://flagcdn.com/w80/$iso.png"
    }

    fun convertSpokenHebrewToDigits(text: String): String {
        val cleanText = text.trim().lowercase()
        val wordToDigit = listOf(
            "אפס" to "0",
            "אחת" to "1",
            "אחד" to "1",
            "שתיים" to "2",
            "שתים" to "2",
            "שני" to "2",
            "שנים" to "2",
            "שלושה" to "3",
            "שלוש" to "3",
            "שלש" to "3",
            "ארבעה" to "4",
            "ארבע" to "4",
            "חמישה" to "5",
            "חמש" to "5",
            "שישה" to "6",
            "שש" to "6",
            "שבעה" to "7",
            "שבע" to "7",
            "שמונה" to "8",
            "שמנה" to "8",
            "תשעה" to "9",
            "תשע" to "9",
            "zero" to "0",
            "one" to "1",
            "two" to "2",
            "three" to "3",
            "four" to "4",
            "five" to "5",
            "six" to "6",
            "seven" to "7",
            "eight" to "8",
            "nine" to "9"
        )
        val words = cleanText.split(Regex("[\\s\\-\\.,]+"))
        val mappedWords = words.map { word ->
            wordToDigit.firstOrNull { it.first == word }?.second ?: word
        }
        return mappedWords.joinToString("")
    }

    fun getColorVisual(colorName: String?): Pair<Long, Long?> {
        val c = colorName.orEmpty().trim().lowercase()
        return when {
            // Black
            c.contains("שחור") -> Pair(0xFF121212L, null)
            
            // Silver
            c.contains("כסוף") || c.contains("כסף") -> Pair(0xFFDCDCDCL, 0xFFB0B0B0L)
            
            // Graphite / Dark Grey / Mouse Grey
            c.contains("גרפיט") || c.contains("עכבר") || c.contains("אפור כהה") -> Pair(0xFF4E5154L, null)
            
            // Light Grey
            c.contains("אפור בהיר") -> Pair(0xFFD3D3D3L, 0xFFB0B0B0L)
            
            // Generic Grey
            c.contains("אפור") -> Pair(0xFF808080L, null)
            
            // Gold
            c.contains("זהב") -> Pair(0xFFFFD700L, 0xFFDAA520L)
            
            // Copper / Bronze
            c.contains("נחושת") -> Pair(0xFFB87333L, null)
            c.contains("ברונזה") -> Pair(0xFFCD7F32L, null)
            
            // Mustard / Yellow
            c.contains("חרדל") -> Pair(0xFFE1AD01L, null)
            c.contains("צהוב") -> Pair(0xFFFFEB3BL, 0xFFFBC02DL)
            
            // Orange
            c.contains("כתום") || c.contains("אורנג") -> Pair(0xFFFF9800L, null)
            
            // Burgundy / Wine / Dark Red
            c.contains("בורדו") || c.contains("יין") -> Pair(0xFF800020L, null)
            c.contains("אדום כהה") -> Pair(0xFFB71C1CL, null)
            c.contains("אדום בהיר") -> Pair(0xFFEF5350L, null)
            c.contains("אדום") -> Pair(0xFFE53935L, null)
            
            // Eggplant / Dark Purple
            c.contains("חציל") -> Pair(0xFF301934L, null)
            c.contains("סגול") -> Pair(0xFF7E57C2L, null)
            
            // Navy / Dark Blue
            c.contains("נייבי") || c.contains("כחול כהה") -> Pair(0xFF1A237EL, null)
            c.contains("כחול בהיר") -> Pair(0xFF90CAF9L, 0xFF64B5F6L)
            c.contains("תכלת") -> Pair(0xFF4FC3F7L, null)
            c.contains("טורקיז") -> Pair(0xFF00BEC4L, null)
            c.contains("כחול") -> Pair(0xFF1E88E5L, null)
            
            // Bottle Green / Dark Green
            c.contains("בקבוק") || c.contains("ירוק כהה") -> Pair(0xFF1B5E20L, null)
            c.contains("זית") -> Pair(0xFF556B2FL, null)
            c.contains("ירוק בהיר") || c.contains("ליים") -> Pair(0xFF8BC34AL, null)
            c.contains("ירוק") -> Pair(0xFF4CAF50L, null)
            
            // Dark Brown
            c.contains("חום כהה") -> Pair(0xFF3E2723L, null)
            c.contains("חום בהיר") -> Pair(0xFF8D6E63L, null)
            c.contains("חום") -> Pair(0xFF5D4037L, null)
            
            // Champagne
            c.contains("שמפניה") -> Pair(0xFFF0E68CL, 0xFFD2B48CL)
            
            // Beige / Cream
            c.contains("בז'") || c.contains("בז") || c.contains("קרם") -> Pair(0xFFF5F5DCL, 0xFFD3D3D3L)
            
            // Off-White / Ivory
            c.contains("שנהב") || c.contains("אוף וויט") || c.contains("אוף-וויט") || c.contains("אופוייט") -> Pair(0xFFFAF9F6L, 0xFFD3D3D3L)
            
            // Pink
            c.contains("ורוד כהה") || c.contains("פוקסיה") -> Pair(0xFFD81B60L, null)
            c.contains("ורוד") -> Pair(0xFFF48FB1L, null)
            
            // White / Pearl
            c.contains("לבן") || c.contains("פנינה") -> Pair(0xFFFFFFFFL, 0xFFBDBDBDL)
            
            // Default Grey
            else -> Pair(0xFF9E9E9EL, null)
        }
    }
}