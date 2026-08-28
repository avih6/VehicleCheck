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
    @SerialName("shilda") val vinAlt: String? = null,
    @Serializable(with = FlexibleLongSerializer::class) @SerialName("horaat_rishum") val registrationDirective: Long? = null,
    @Serializable(with = FlexibleIntSerializer::class) @SerialName("nefach_manoa") val engineDisplacement: Int? = null,
    @Serializable(with = FlexibleIntSerializer::class) @SerialName("koah_sus") val horsepower: Int? = null,
    @Serializable(with = FlexibleIntSerializer::class) @SerialName("mishkal_kolel") val totalWeight: Int? = null,
    @Serializable(with = FlexibleIntSerializer::class) @SerialName("mishkal_azmi") val curbWeight: Int? = null,
    @Serializable(with = FlexibleIntSerializer::class) @SerialName("mishkal_mitan") val cargoWeight: Int? = null,
    @Serializable(with = FlexibleIntSerializer::class) @SerialName("mishkal_murshe_ligror") val towingCapacity: Int? = null,
    @SerialName("hanaa_nm") val driveType: String? = null,
    @Serializable(with = FlexibleIntSerializer::class) @SerialName("mispar_moshavim") val seats: Int? = null,
    @Serializable(with = FlexibleIntSerializer::class) @SerialName("mispar_leyad_nahag") val seatsNextToDriver: Int? = null,
    @SerialName("sug_rechev_nm") val vehicleCategory: String? = null,
    @SerialName("sug_tkina_nm") val standardType: String? = null,
    @SerialName("tozeret_eretz_nm") val countryOfOrigin: String? = null,
    @Serializable(with = FlexibleIntSerializer::class) @SerialName("kvutzat_agrah_cd") val feeGroupCd: Int? = null,
    @SerialName("sug_argaz_nm") val cargoBoxType: String? = null,
    @Serializable(with = FlexibleIntSerializer::class) @SerialName("argaz_ind") val cargoBoxInd: Int? = null,
    @SerialName("sug_merkev_nm") val bodyTypeName: String? = null,
    @SerialName("mispar_shilda") val vinHeavy: String? = null,
    @SerialName("tkina_EU") val standardTypeHeavy: String? = null,
    @SerialName("kvutzat_sug_rechev") val vehicleCategoryHeavy: String? = null,
    @Serializable(with = FlexibleIntSerializer::class) @SerialName("mispar_mekomot") val seatsHeavy: Int? = null,
    @Serializable(with = FlexibleIntSerializer::class) @SerialName("mispar_mekomot_leyd_nahag") val seatsNextToDriverHeavy: Int? = null,
    @Serializable(with = FlexibleIntSerializer::class) @SerialName("mishkal_mitan_harama") val cargoWeightHeavy: Int? = null,
    @SerialName("grira_nm") val towingCapacityHeavy: String? = null
) {
    val effectiveVin: String? get() = if (!vin.isNullOrBlank()) vin else if (!vinAlt.isNullOrBlank()) vinAlt else vinHeavy
    val effectiveStandardType: String? get() = if (!standardType.isNullOrBlank()) standardType else standardTypeHeavy
    val effectiveVehicleCategory: String? get() = if (!vehicleCategory.isNullOrBlank()) vehicleCategory else vehicleCategoryHeavy
    val effectiveSeats: Int? get() = seats ?: seatsHeavy
    val effectiveSeatsNextToDriver: Int? get() = seatsNextToDriver ?: seatsNextToDriverHeavy
    val effectiveCargoWeight: Int? get() = cargoWeight ?: cargoWeightHeavy
}

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

data class ModelStatisticsDetail(
    val makeHe: String,
    val makeEn: String,
    val modelName: String,
    val commercialName: String? = null,
    val vehicleType: String? = null,
    val classification: String,
    val totalActive: Int,
    val totalInactive: Int,
    val survivalRate: Float,
    val safetyScore: Double? = null,
    val fuelTypes: List<String> = emptyList(),
    val enginePowerHp: Int? = null,
    val transmission: String? = null,
    val yearDistribution: List<ModelYearCount> = emptyList()
)

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

    fun formatTimeAgo(dateStr: String?): String? {
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
        if (years > 0) parts.add(if (years == 1) "שנה אחת" else if (years == 2) "שנתיים" else "$years שנים")
        if (months > 0) parts.add(if (months == 1) "חודש אחד" else if (months == 2) "חודשיים" else "$months חודשים")
        if (days > 0 && years == 0) parts.add(if (days == 1) "יום אחד" else if (days == 2) "יומיים" else "$days ימים")
        else if (parts.isEmpty()) parts.add(if (days <= 1) "היום" else "$days ימים")

        val formattedDiff = parts.joinToString(if (parts.size > 2) ", " else " ו-")
        return if (isPast) "לפני $formattedDiff" else "בעוד $formattedDiff"
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

    fun formatCountry(country: String?): String {
        if (country.isNullOrBlank()) return "אין מידע"
        val clean = country.trim()
        return when {
            clean.contains("ארהב") || clean.contains("ארצות הברית") || clean.equals("USA", ignoreCase = true) -> "ארה\"ב"
            clean.contains("בריטניה") || clean.contains("אנגליה") || clean.equals("UK", ignoreCase = true) -> "בריטניה"
            clean.contains("גרמניה") || clean.contains("גרמנ") -> "גרמניה"
            clean.contains("יפן") -> "יפן"
            clean.contains("קוריאה") -> "דרום קוריאה"
            clean.contains("סין") -> "סין"
            clean.contains("צרפת") -> "צרפת"
            clean.contains("איטליה") -> "איטליה"
            clean.contains("ספרד") -> "ספרד"
            clean.contains("צ'כיה") || clean.contains("צכיה") -> "צ'כיה"
            clean.contains("שוודיה") || clean.contains("שבדיה") -> "שוודיה"
            clean.contains("טורקיה") || clean.contains("תורכיה") -> "טורקיה"
            clean.contains("מקסיקו") -> "מקסיקו"
            clean.contains("קנדה") -> "קנדה"
            clean.contains("הונגריה") -> "הונגריה"
            clean.contains("הודו") -> "הודו"
            clean.contains("רומניה") -> "רומניה"
            clean.contains("פולין") -> "פולין"
            clean.contains("בלגיה") -> "בלגיה"
            clean.contains("תאילנד") -> "תאילנד"
            clean.contains("ברזיל") -> "ברזיל"
            clean.contains("סלובקיה") -> "סלובקיה"
            clean.contains("אוסטריה") -> "אוסטריה"
            clean.contains("פורטוגל") -> "פורטוגל"
            clean.contains("הולנד") -> "הולנד"
            clean.contains("דרום אפריקה") -> "דרום אפריקה"
            else -> clean
        }
    }

    fun formatMake(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        var str = raw.trim()
        val countrySuffixes = listOf(
            " גרמנ", " גרמניה", " יפן", " צרפת", " איטליה", " איטלי",
            " ארה\"ב", " ארהב\"", " ארהב", " שוודיה", " שוודי",
            " בריטניה", " אנגליה", " סין", " הודו", " טורקיה", " תורכיה",
            " דרום קוריאה", " קוריאה", " צ'כיה", " ספרד", " רומניה",
            " הונגריה", " פולין", " מקסיקו", " קנדה", " תאילנד", " אוסטריה", " בלגיה"
        )
        for (suffix in countrySuffixes) {
            if (str.endsWith(suffix, ignoreCase = true)) {
                str = str.removeSuffix(suffix).trim()
            }
        }
        str = str.replace("ארהב\"", "ארה\"ב")
        str = str.replace("ארהב", "ארה\"ב")
        return str
    }

    fun getBrandLogoUrls(hebrewMake: String?): List<String> {
        val slug = getBrandSlug(hebrewMake)
        if (slug == "car" || slug == "trailer") return emptyList()

        val list = mutableListOf<String>()

        // 1. Direct high-res vectors and verified fallbacks for specialized/new brands
        when (slug) {
            "jaecoo" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Jaecoo_logo.svg/500px-Jaecoo_logo.svg.png")
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/Chery_logo.svg/500px-Chery_logo.svg.png")
                list.add("https://cdn.jsdelivr.net/gh/filippofilip95/car-logos-dataset@master/logos/optimized/chery.png")
            }
            "omoda" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/e/e8/Omoda_logo.svg/500px-Omoda_logo.svg.png")
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/Chery_logo.svg/500px-Chery_logo.svg.png")
                list.add("https://cdn.jsdelivr.net/gh/filippofilip95/car-logos-dataset@master/logos/optimized/chery.png")
            }
            "zeekr" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/3/36/Zeekr_logo.svg/500px-Zeekr_logo.svg.png")
                list.add("https://cdn.jsdelivr.net/gh/filippofilip95/car-logos-dataset@master/logos/optimized/zeekr.png")
            }
            "xpeng" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/0/07/XPeng_logo.svg/500px-XPeng_logo.svg.png")
                list.add("https://cdn.jsdelivr.net/gh/filippofilip95/car-logos-dataset@master/logos/optimized/xpeng.png")
            }
            "byd" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/8/8e/BYD_Auto_2022_logo.svg/500px-BYD_Auto_2022_logo.svg.png")
                list.add("https://cdn.jsdelivr.net/gh/filippofilip95/car-logos-dataset@master/logos/optimized/byd.png")
            }
            "chery" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/c/cf/Chery_logo.svg/500px-Chery_logo.svg.png")
                list.add("https://cdn.jsdelivr.net/gh/filippofilip95/car-logos-dataset@master/logos/optimized/chery.png")
            }
            "leapmotor" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/Leapmotor_logo.svg/500px-Leapmotor_logo.svg.png")
            }
            "voyah" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/2/27/Voyah_logo.svg/500px-Voyah_logo.svg.png")
            }
            "seres" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/a/a2/SERES_Logo.svg/500px-SERES_Logo.svg.png")
            }
            "hongqi" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/1/15/Hongqi_logo.svg/500px-Hongqi_logo.svg.png")
            }
            "ora" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/6/63/ORA_logo.svg/500px-ORA_logo.svg.png")
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/Great_Wall_Motors_logo.svg/500px-Great_Wall_Motors_logo.svg.png")
            }
            "wey" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/e/e8/WEY_logo.svg/500px-WEY_logo.svg.png")
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/Great_Wall_Motors_logo.svg/500px-Great_Wall_Motors_logo.svg.png")
            }
            "cupra" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/6/6f/Cupra_Logo.svg/500px-Cupra_Logo.svg.png")
                list.add("https://cdn.jsdelivr.net/gh/filippofilip95/car-logos-dataset@master/logos/optimized/cupra.png")
            }
            "lynk-co" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/e/e4/Lynk_%26_Co_logo.svg/500px-Lynk_%26_Co_logo.svg.png")
            }
            "polestar" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/0/05/Polestar_logo.svg/500px-Polestar_logo.svg.png")
                list.add("https://cdn.jsdelivr.net/gh/filippofilip95/car-logos-dataset@master/logos/optimized/polestar.png")
            }
            "smart" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/3/30/Smart_logo_2022.svg/500px-Smart_logo_2022.svg.png")
                list.add("https://cdn.jsdelivr.net/gh/filippofilip95/car-logos-dataset@master/logos/optimized/smart.png")
            }
            "aiways" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/4/4e/Aiways_Logo_2019.svg/500px-Aiways_Logo_2019.svg.png")
            }
            "forthing" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/7/77/Forthing_logo.svg/500px-Forthing_logo.svg.png")
            }
            "skywell" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/7/70/Skywell_logo.svg/500px-Skywell_logo.svg.png")
            }
        }

        // 2. High-speed jsDelivr CDN & GitHub raw
        list.add("https://cdn.jsdelivr.net/gh/filippofilip95/car-logos-dataset@master/logos/optimized/$slug.png")
        list.add("https://raw.githubusercontent.com/filippofilip95/car-logos-dataset/master/logos/optimized/$slug.png")
        list.add("https://cdn.jsdelivr.net/gh/filippofilip95/car-logos-dataset@master/logos/thumb/$slug.png")
        
        val cleanSlug = slug.replace("-", "")
        if (cleanSlug != slug) {
            list.add("https://cdn.jsdelivr.net/gh/filippofilip95/car-logos-dataset@master/logos/optimized/$cleanSlug.png")
        }

        return list.distinct()
    }

    fun getBrandLogoUrl(hebrewMake: String?): String {
        return getBrandLogoUrls(hebrewMake).firstOrNull() 
            ?: "https://cdn.jsdelivr.net/gh/filippofilip95/car-logos-dataset@master/logos/optimized/car.png"
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
            m.contains("בי ואי די") || m.contains("בי.ואי.די") || m.contains("בי וי די") || m.contains("בי.וויי.די") || m.contains("ביוואידי") || m.contains("byd") || m.contains("b.y.d") -> "byd"
            m.contains("מ.ג") || m.contains("מ.ג.") || m.contains("מ ג") || m.contains("אם ג'י") || m.contains("אם.ג'י") || m.contains("אי אם ג'י") || m.contains("mg") || m.contains("m.g") -> "mg"
            m.contains("ב.מ.וו") || m.contains("במוו") || m.contains("ב מ וו") || m.contains("bmw") || m.contains("b.m.w") -> "bmw"
            m.contains("מ.א.ן") || m.contains("מ א ן") || m.contains("מאן") || m.contains("man") -> "man"
            m.contains("ג.מ.ס") || m.contains("ג'י אם סי") || m.contains("ג'י.אם.סי") || m.contains("ג'מאס") || m.contains("gmc") -> "gmc"
            m.contains("פורד") || m.contains("ford") -> "ford"
            m.contains("שברולט") || m.contains("chevrolet") || m.contains("chevy") -> "chevrolet"
            m.contains("לנד רובר") || m.contains("לנדרובר") || m.contains("land rover") || m.contains("land-rover") -> "land-rover"
            m.contains("ג'ילי") || m.contains("geely") || m.contains("גילי") || m.contains("גיאומטרי") || m.contains("ג'יאומטרי") || m.contains("ג.י.ל.י") -> "geely"
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
            m.contains("צ'רי") || m.contains("צרי") || m.contains("שרי") || m.contains("chery") -> "chery"
            m.contains("איוויז") || m.contains("איווייז") || m.contains("aiways") -> "aiways"
            m.contains("ליפמוטור") || m.contains("ליפ מוטור") || m.contains("leapmotor") || m.contains("leap") -> "leapmotor"
            m.contains("סרס") || m.contains("סירס") || m.contains("seres") || m.contains("dfsk") -> "seres"
            m.contains("וויה") || m.contains("ויה") || m.contains("voyah") -> "voyah"
            m.contains("פולסטאר") || m.contains("פול סטאר") || m.contains("polestar") -> "polestar"
            m.contains("לינק") || m.contains("lynk") -> "lynk-co"
            m.contains("הונגצ'י") || m.contains("הונג צ'י") || m.contains("hongqi") -> "hongqi"
            m.contains("ג'נסיס") || m.contains("גנסיס") || m.contains("genesis") -> "genesis"
            m.contains("אורה") || m.contains("ora") || m.contains("gwm") || m.contains("גרייט וול") -> "ora"
            m.contains("סקייוול") || m.contains("סקיי וול") || m.contains("skywell") -> "skywell"
            m.contains("דונגפנג") || m.contains("dongfeng") -> "dongfeng"
            m.contains("ג'קו") || m.contains("ג'אקו") || m.contains("גקו") || m.contains("גאקו") || m.contains("ג'ייקו") || m.contains("גייקו") || m.contains("jaecoo") -> "jaecoo"
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
            m.contains("נצר סירני") || m.contains("סירני") || m.contains("גרור") || m.contains("נגרר") || m.contains("נתמך") || m.contains("מריצה") -> "trailer"
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

    fun getEnglishMakeAndModel(
        hebrewMake: String?,
        model: String?,
        trimLevel: String? = null,
        category: String? = null
    ): Pair<String, String> {
        val makeEn = getBrandSlug(hebrewMake)
        val mod = model.orEmpty().lowercase()
        val trim = trimLevel.orEmpty().lowercase()
        val cat = category.orEmpty().lowercase()

        // Check if model is a generic country placeholder from old MOT data (e.g. "גרמנ", "גרמניה", "יפן", etc.)
        val isCountryPlaceholder = listOf("גרמנ", "גרמניה", "יפן", "צרפת", "איטליה", "ספרד", "שוודיה", "קוריאה", "ארהב", "ארה\"ב", "בריטניה", "אנגליה", "סין", "הודו", "טורקיה", "תורכיה", "בלגיה", "הולנד", "אוסטריה").any { mod.contains(it) }

        var modelClean = if (isCountryPlaceholder) "" else mod
            .replace("4x4", "")
            .replace("awd", "")
            .replace("2x4", "")
            .replace("hybrid", "")
            .trim()
            .split(" ", "-", "_")
            .firstOrNull { it.isNotBlank() && !it.contains("גרמנ") } ?: ""

        if (modelClean.isBlank() || modelClean == "car") {
            // Extract from trimLevel or category
            when {
                trim.contains("404") || trim.contains("o 404") || trim.contains("o404") -> modelClean = "O404"
                trim.contains("405") || trim.contains("o 405") || trim.contains("o405") -> modelClean = "O405"
                trim.contains("303") || trim.contains("o 303") || trim.contains("o303") -> modelClean = "O303"
                trim.contains("sprinter") || trim.contains("ספרינטר") -> modelClean = "Sprinter"
                trim.contains("vito") || trim.contains("ויטו") -> modelClean = "Vito"
                trim.contains("v-class") || trim.contains("v class") -> modelClean = "V-Class"
                trim.contains("savana") || trim.contains("סוואנה") -> modelClean = "Savana"
                trim.contains("transit") || trim.contains("טרנזיט") -> modelClean = "Transit"
                trim.contains("crafter") || trim.contains("קראפטר") -> modelClean = "Crafter"
                cat.contains("אמבולנס") || trim.contains("אמבולנס") -> modelClean = "Ambulance"
                cat.contains("אוטובוס") || trim.contains("אוטובוס") -> modelClean = "Bus"
                else -> {
                    val alphanumeric = Regex("[A-Za-z0-9]+").findAll(trimLevel.orEmpty())
                        .map { it.value }
                        .filter { it.length >= 2 && !it.all { c -> c == '0' } }
                        .firstOrNull()
                    modelClean = alphanumeric ?: "car"
                }
            }
        }

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
            c.contains("גרמניה") || c.contains("גרמנ") || c.contains("germany") -> "de"
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

    data class BodyTypeInfo(
        val title: String,
        val iconEmoji: String,
        val subtitle: String
    )

    fun resolveBodyType(vehicle: VehicleRecord, techSpec: VehicleTechnicalSpecRecord?): BodyTypeInfo {
        val bt = techSpec?.bodyType.orEmpty().trim().lowercase()
        val mod = vehicle.model.orEmpty().trim().lowercase()
        val cat = (vehicle.effectiveVehicleCategory ?: vehicle.vehicleCategory).orEmpty().trim().lowercase()
        val std = (vehicle.effectiveStandardType ?: vehicle.standardType).orEmpty().trim().uppercase()
        val seats = vehicle.effectiveSeats ?: techSpec?.seats ?: 0
        val trim = vehicle.trimLevel.orEmpty().trim().lowercase()

        val isBus = cat.contains("אוטובוס") || trim.contains("אוטובוס") ||
                std.startsWith("M3") || (std.startsWith("M2") && seats > 16)

        val isAmbulanceOrRescue = cat.contains("אמבולנס") || mod.contains("ambulance") || trim.contains("אמבולנס") ||
                cat.contains("מיוחד") || cat.contains("הצלה") || cat.contains("ביטחון") ||
                trim.contains("הצלה") || trim.contains("כיבוי") ||
                (std.startsWith("M2") && (mod.contains("sprinter") || mod.contains("savana") || mod.contains("transit") || mod.contains("crafter") || seats in 1..4))

        return when {
            isBus ->
                BodyTypeInfo("אוטובוס / היסעים", "🚌", "רכב להסעת נוסעים ציבורי / פרטי")
            isAmbulanceOrRescue ->
                BodyTypeInfo("רכב מיוחד (אמבולנס / ביטחון)", "🚑", "רכב רפואי והצלה ייעודי")
            bt.contains("פנאי") || bt.contains("שטח") || bt.contains("suv") || mod.contains("cross") || mod.contains("suv") ->
                BodyTypeInfo("פנאי-שטח (SUV / קרוסאובר)", "🚙", "מרכב פנאי מוגבה 5 דלתות")
            bt.contains("סדאן") || bt.contains("sedan") || bt.contains("4 דלת") ->
                BodyTypeInfo("סדאן (4 דלתות)", "🚗", "מרכב נוסעים משפחתי קלאסי")
            bt.contains("הצ'בק") || bt.contains("האצ'בק") || bt.contains("hatchback") || bt.contains("5 דלת") || bt.contains("3 דלת") ->
                BodyTypeInfo("האצ'בק (5 דלתות / מיני)", "🚗", "מרכב נוסעים קומפקטי עם דלת תא מטען")
            bt.contains("סטיישן") || bt.contains("wagon") || bt.contains("estate") || mod.contains("combi") || mod.contains("touring") || mod.contains("sw") ->
                BodyTypeInfo("סטיישן (Wagon / קומבי)", "🚘", "מרכב נוסעים ארוך עם תא מטען מוגדל")
            bt.contains("קופה") || bt.contains("coupe") || bt.contains("ספורט") ->
                BodyTypeInfo("קופה / ספורט", "🏎️", "מרכב ספורטיבי 2-3 דלתות")
            bt.contains("קבריולט") || bt.contains("קבריו") || bt.contains("cabrio") || bt.contains("convertible") || bt.contains("רודסטר") ->
                BodyTypeInfo("קבריולט (גג פתוח / רודסטר)", "🏎️", "מרכב ספורטיבי פתוח / גג נפתח")
            std.startsWith("M2") || std.startsWith("M3") || bt.contains("מיניוואן") || bt.contains("מיקרוואן") || bt.contains("mpv") || bt.contains("וואן") || bt.contains("אחוד") || seats >= 7 ->
                BodyTypeInfo("מיניוואן / היסעים (MPV / M2)", "🚐", "מרכב רב-נוסעים / היסעים מרווח")
            bt.contains("טנדר") || bt.contains("pickup") || bt.contains("פיק-אפ") || mod.contains("hilux") || mod.contains("d-max") ->
                BodyTypeInfo("טנדר (Pick-Up)", "🛻", "מרכב מסחרי פתוח להעמסה")
            vehicle.modelType == "M" || cat.contains("משא") || std.startsWith("N") || (vehicle.totalWeight ?: 0) > 3500 ->
                BodyTypeInfo("משא / מסחרי", "🚚", "רכב עבודה ומטען")
            vehicle.modelType == "A" || cat.contains("אופנוע") || cat.contains("קטנוע") ->
                BodyTypeInfo("דו-גלגלי (אופנוע / קטנוע)", "🏍️", "רכב דו-גלגלי מנועי")
            else ->
                BodyTypeInfo("רכב נוסעים פרטי (M1)", "🚗", "מרכב נוסעים סטנדרטי")
        }
    }

    fun resolveLegalLicenseClass(vehicle: VehicleRecord, techSpec: VehicleTechnicalSpecRecord?): Pair<String, String> {
        val totalWeight = techSpec?.totalWeight ?: vehicle.totalWeight ?: 1600
        val std = (vehicle.effectiveStandardType ?: vehicle.standardType).orEmpty().trim().uppercase()
        val cat = (vehicle.effectiveVehicleCategory ?: vehicle.vehicleCategory).orEmpty().trim().lowercase()
        val mod = vehicle.model.orEmpty().trim().lowercase()
        val seats = vehicle.effectiveSeats ?: techSpec?.seats ?: 0
        val trim = vehicle.trimLevel.orEmpty().trim().lowercase()

        val isBus = cat.contains("אוטובוס") || trim.contains("אוטובוס") ||
                std.startsWith("M3") || (std.startsWith("M2") && seats > 16)

        val isAmbulanceOrSpecial = cat.contains("אמבולנס") || mod.contains("ambulance") || trim.contains("אמבולנס") ||
                cat.contains("מיוחד") || cat.contains("הצלה") || cat.contains("ביטחון") ||
                trim.contains("הצלה") || trim.contains("כיבוי") ||
                (std.startsWith("M2") && (mod.contains("sprinter") || mod.contains("savana") || mod.contains("transit") || seats in 1..4))

        return when {
            isBus ->
                Pair("אוטובוס / היסעים (${std.ifBlank { "M3" }})", "רכב להסעת נוסעים • דורש רישיון ייעודי D / D1")
            isAmbulanceOrSpecial ->
                Pair("רכב ביטחון והצלה (אמבולנס $std)", "רכב ייעודי ברישום מיוחד")
            cat.contains("אוטובוס") || std.startsWith("M3") || (std.startsWith("M2") && seats > 4) ->
                Pair("רכב היסעים / אוטובוס זעיר ($std)", "מורשה להסעת נוסעים / דורש רישיון ייעודי")
            std.startsWith("N2") || std.startsWith("N3") || (totalWeight > 3500 && !std.startsWith("M")) ->
                Pair("משא כבד ($std) • מעל 3.5 טון", "משקל כולל %,d ק\"ג (דרגת רישיון משא C1 ומעלה)".format(totalWeight))
            vehicle.modelType == "M" || std.startsWith("N1") || cat.contains("משא") ->
                Pair("משא / מסחרי קל ($std) • עד 3.5 טון", "משקל כולל עד 3,500 ק\"ג (דרגת רישיון B)")
            else ->
                Pair("פרטי נוסעים (M1) • עד 3.5 טון", "משקל כולל עד 3,500 ק\"ג (דרגת רישיון B רגיל)")
        }
    }

    fun resolveCountryOfOrigin(vehicle: VehicleRecord, techSpec: VehicleTechnicalSpecRecord?): String? {
        // Priority 1: Official country of origin field from tech spec or vehicle record
        val rawCountry = techSpec?.countryOfOrigin ?: vehicle.countryOfOrigin
        if (!rawCountry.isNullOrBlank() && rawCountry != "אין מידע" && rawCountry != "null") {
            return formatCountry(rawCountry)
        }

        // Priority 2: Extract country token explicitly mentioned in make/model/trim
        val combinedText = "${vehicle.make.orEmpty()} ${vehicle.model.orEmpty()} ${vehicle.trimLevel.orEmpty()}".lowercase()
        val detectedFromText = when {
            combinedText.contains("גרמנ") || combinedText.contains("גרמניה") || combinedText.contains("germany") -> "גרמניה"
            combinedText.contains("יפן") || combinedText.contains("japan") -> "יפן"
            combinedText.contains("צרפת") || combinedText.contains("france") -> "צרפת"
            combinedText.contains("איטלי") || combinedText.contains("איטליה") || combinedText.contains("italy") -> "איטליה"
            combinedText.contains("ארהב") || combinedText.contains("ארה\"ב") || combinedText.contains("usa") -> "ארה\"ב"
            combinedText.contains("שוודי") || combinedText.contains("שוודיה") || combinedText.contains("שבדיה") || combinedText.contains("sweden") -> "שוודיה"
            combinedText.contains("קוריאה") || combinedText.contains("korea") -> "דרום קוריאה"
            combinedText.contains("בריטניה") || combinedText.contains("אנגליה") || combinedText.contains("uk") -> "בריטניה"
            combinedText.contains("ספרד") || combinedText.contains("spain") -> "ספרד"
            combinedText.contains("צ'כיה") || combinedText.contains("צכיה") || combinedText.contains("czech") -> "צ'כיה"
            combinedText.contains("רומניה") || combinedText.contains("romania") -> "רומניה"
            combinedText.contains("הונגריה") || combinedText.contains("hungary") -> "הונגריה"
            combinedText.contains("פולין") || combinedText.contains("poland") -> "פולין"
            combinedText.contains("בלגיה") || combinedText.contains("belgium") -> "בלגיה"
            combinedText.contains("סין") || combinedText.contains("china") -> "סין"
            combinedText.contains("הודו") || combinedText.contains("india") -> "הודו"
            combinedText.contains("טורקיה") || combinedText.contains("תורכיה") || combinedText.contains("turkey") -> "טורקיה"
            combinedText.contains("תאילנד") || combinedText.contains("thailand") -> "תאילנד"
            combinedText.contains("מקסיקו") || combinedText.contains("mexico") -> "מקסיקו"
            combinedText.contains("קנדה") || combinedText.contains("canada") -> "קנדה"
            combinedText.contains("אוסטריה") || combinedText.contains("austria") -> "אוסטריה"
            combinedText.contains("הולנד") || combinedText.contains("netherlands") -> "הולנד"
            else -> null
        }
        if (detectedFromText != null) return detectedFromText

        // Priority 3 (Last Resort): Infer origin from brand manufacturer headquarters
        val brandSlug = getBrandSlug(vehicle.make)
        return when (brandSlug) {
            "mercedes-benz", "bmw", "volkswagen", "audi", "porsche", "opel", "man" -> "גרמניה"
            "toyota", "mazda", "honda", "subaru", "nissan", "suzuki", "mitsubishi", "lexus", "daihatsu", "infiniti", "isuzu", "yamaha", "kawasaki" -> "יפן"
            "hyundai", "kia", "genesis", "ssangyong" -> "דרום קוריאה"
            "volvo", "polestar", "scania", "husqvarna" -> "שוודיה"
            "renault", "peugeot", "citroen", "alpine", "bugatti" -> "צרפת"
            "fiat", "alfa-romeo", "ferrari", "maserati", "lamborghini", "abarth", "lancia", "iveco", "ducati", "piaggio", "vespa", "aprilia", "moto-guzzi" -> "איטליה"
            "ford", "chevrolet", "tesla", "cadillac", "jeep", "dodge", "ram", "chrysler", "gmc", "lincoln", "buick", "pontiac", "oldsmobile", "rivian", "lucid", "harley-davidson", "caterpillar", "john-deere", "bobcat", "mack" -> "ארה\"ב"
            "skoda" -> "צ'כיה"
            "seat", "cupra" -> "ספרד"
            "dacia" -> "רומניה"
            "land-rover", "jaguar", "mini", "aston-martin", "bentley", "rolls-royce", "mclaren", "lotus", "rover", "triumph", "royal-enfield" -> "בריטניה"
            "byd", "geely", "mg", "chery", "zeekr", "xpeng", "nio", "voyah", "omoda", "jaecoo", "leapmotor", "seres", "skywell", "maxus", "forthing", "gac", "changan", "dongfeng", "hongqi", "ora", "neta", "farizon", "wey", "golden-dragon", "yutong", "higer", "king-long", "ankai", "foton", "cfmoto", "voge", "qjmotor" -> "סין"
            "ktm" -> "אוסטריה"
            "sym", "kymco" -> "טאיוואן"
            "daf" -> "הולנד"
            "jcb" -> "בריטניה"
            "komatsu" -> "יפן"
            else -> null
        }
    }

    fun resolveQuickClassification(
        make: String?,
        model: String?,
        modelType: String? = null,
        ownership: String? = null,
        trimLevel: String? = null,
        fuel: String? = null,
        category: String? = null
    ): String {
        val m = model.orEmpty().lowercase()
        val mk = make.orEmpty().lowercase()
        val t = modelType.orEmpty().lowercase()
        val o = ownership.orEmpty().lowercase()
        val tl = trimLevel.orEmpty().lowercase()
        val cat = category.orEmpty().lowercase()
        val combined = "$m $mk $t $o $tl $cat"

        return when {
            // 0. Trailers & Semi-trailers (גרורים ונתמכים - נצר סירני וכו')
            combined.contains("סירני") || combined.contains("נתמך") || combined.contains("גרור") || 
            combined.contains("נגרר") || combined.contains("trailer") || combined.contains("o4") || 
            combined.contains("o3") || combined.contains("o2") || combined.contains("o1") -> "🚛 נתמך / גרור"

            // 0b. Heavy Machinery / Construction (צמ"ה)
            combined.contains("הנדסי") || combined.contains("צמ\"ה") || combined.contains("צמה") || 
            mk.contains("קטרפילר") || mk.contains("komatsu") || mk.contains("caterpillar") || mk.contains("jcb") || mk.contains("bobcat") -> "🚜 ציוד הנדסי"

            // 1. Ambulance & Emergency Vehicles (מד"א / איחוד הצלה / אמבולנס / ספרינטר מד"א)
            combined.contains("אמבולנס") || combined.contains("ambulance") || combined.contains("הצלה") ||
            combined.contains("רפואי") || combined.contains("מגן דוד") || combined.contains("מד\"א") ||
            combined.contains("מדא") || (mk.contains("מרצדס") && (m.contains("ספרינטר") || m.contains("sprinter")) && (o.contains("חברה") || o.contains("עירייה") || t.contains("בטחון") || t.contains("מיוחד"))) -> "🚑 אמבולנס"

            // 2. Bus & Minibus (אוטובוס / אוטובוס זעיר)
            combined.contains("אוטובוס") || combined.contains("bus") || combined.contains("o404") || combined.contains("o405") ||
            combined.contains("tourismo") || combined.contains("citaro") || combined.contains("travego") || combined.contains("b12") ||
            combined.contains("b7") || combined.contains("centroliner") || combined.contains("lion") || (mk.contains("מרצדס") && m.contains("o 404")) ||
            t.contains("אוטובוס") || cat.contains("אוטובוס") -> "🚌 אוטובוס"

            // 3. Commercial Vans & Transporters (הייאס, טרנזיט, קנגו, ברלינגו, דוקאטו, טרנספורטר, ספרינטר, קאדי, סוואנה)
            m.contains("hiace") || m.contains("הייאס") || m.contains("היאס") || m.contains("transit") || m.contains("טרנזיט") ||
            m.contains("kangoo") || m.contains("קנגו") || m.contains("קנגורו") || m.contains("berlingo") || m.contains("ברלינגו") ||
            m.contains("partner") || m.contains("פרטנר") || m.contains("caddy") || m.contains("קאדי") || m.contains("ducato") ||
            m.contains("דוקאטו") || m.contains("jumper") || m.contains("ג'אמפר") || m.contains("boxer") || m.contains("בוקסר") ||
            m.contains("transporter") || m.contains("crafter") || m.contains("master") || m.contains("מאסטר") || m.contains("savana") ||
            m.contains("סוואנה") || m.contains("סבאנה") || m.contains("express") || m.contains("אקספרס") || m.contains("expert") ||
            m.contains("proace") || m.contains("nv200") || m.contains("nv400") || m.contains("vivaro") || m.contains("sprinter") ||
            m.contains("ספרינטר") || t.contains("משא אחוד") || t.contains("מסחרי") || t.contains("n1") || cat.contains("משא אחוד") -> "🚐 מסחרית / ואן"

            // 4. Heavy Trucks (משאית / משא כבד)
            combined.contains("משאית") || combined.contains("משא כבד") || t.contains("משא") || cat.contains("משא") || 
            m.contains("משא") || m.contains("actros") || m.contains("atego") || m.contains("axor") || m.contains("fh") || 
            m.contains("fm") || m.contains("tgx") || m.contains("tgs") || m.contains("tgl") || m.contains("stralis") || 
            m.contains("eurocargo") || m.contains("scania") || m.contains("man ") || mk.contains("סקניה") ||
            mk.contains("דאף") || mk.contains("daf") || mk.contains("מאק") || mk.contains("mack") -> "🚚 משא / מסחרי"

            // 5. Motorcycles & Scooters (אופנוע / קטנוע)
            combined.contains("אופנוע") || combined.contains("קטנוע") || combined.contains("scooter") || combined.contains("motorcycle") ||
            mk.contains("ימאהה") || mk.contains("yamaha") || mk.contains("סאנגיאנג") || mk.contains("sym") || mk.contains("קימקו") ||
            mk.contains("kymco") || mk.contains("הארלי") || mk.contains("harley") || mk.contains("דוקאטי") || mk.contains("ducati") ||
            mk.contains("ק.ט.מ") || mk.contains("ktm") || mk.contains("קוואסאקי") || mk.contains("kawasaki") || mk.contains("פיאג'ו") ||
            mk.contains("piaggio") || mk.contains("vespa") || mk.contains("וספה") || t.contains("אופנוע") || cat.contains("אופנוע") -> "🏍️ אופנוע"

            // 6. Pickups (טנדר)
            m.contains("hilux") || m.contains("היילקס") || m.contains("d-max") || m.contains("דימקס") || m.contains("די מקס") ||
            m.contains("navara") || m.contains("נבארה") || m.contains("triton") || m.contains("טרייטון") || m.contains("l200") ||
            m.contains("amarok") || m.contains("אמארוק") || m.contains("f-150") || m.contains("f-250") || m.contains("f-350") ||
            m.contains("silverado") || m.contains("סילברדו") || m.contains("ram 1500") || m.contains("ram 2500") || m.contains("טנדר") -> "🛻 טנדר"

            // 7. Minivans / MPV (מיניוואן משפחתי)
            m.contains("carnival") || m.contains("קרניבל") || m.contains("sienna") || m.contains("סיינה") || m.contains("voyager") ||
            m.contains("וויאג'ר") || m.contains("pacifica") || m.contains("פסיפיקה") || m.contains("galaxy") || m.contains("s-max") ||
            m.contains("sharan") || m.contains("carens") || m.contains("קארנס") || m.contains("touran") || m.contains("גרנד סניק") ||
            m.contains("grand scenic") || m.contains("lodgy") || m.contains("לודג'י") || m.contains("routan") || combined.contains("מיניוואן") -> "🚐 מיניוואן"

            // 8. SUV & Crossovers (פנאי-שטח SUV) - Note: Fix "cross" word boundary so "lacrosse" does NOT match!
            m.contains("suv") || m.contains("sportage") || m.contains("ספורטאז") || m.contains("tucson") || m.contains("טוסון") ||
            m.contains("qashqai") || m.contains("קשקאי") || m.contains("duster") || m.contains("דאסטר") || m.contains("rav4") ||
            m.contains("ראב 4") || m.contains("ראב4") || m.contains("x-trail") || m.contains("אקס טרייל") || m.contains("cx-5") ||
            m.contains("cx-30") || m.contains("cx-60") || m.contains("cx-90") || m.contains("3008") || m.contains("2008") ||
            m.contains("5008") || m.contains("outlander") || m.contains("אאוטלנדר") || m.contains("kuga") || m.contains("קוגה") ||
            m.contains("tiguan") || m.contains("טיגואן") || m.contains("kodiaq") || m.contains("קודיאק") || m.contains("ateca") ||
            m.contains("אטקה") || m.contains("arona") || m.contains("ארונה") || m.contains("kamiq") || m.contains("קאמיק") ||
            m.contains("karoq") || m.contains("קארוק") || m.contains("ev6") || m.contains("ioniq 5") || m.contains("איוניק 5") ||
            m.contains("model y") || m.contains("מודל y") || m.contains("atto 3") || m.contains("אטו 3") || m.contains("tang") ||
            m.contains("seal u") || m.contains("ch-r") || m.contains("c-hr") || m.contains("corolla cross") || m.contains("yaris cross") ||
            m.contains("t-roc") || m.contains("t-cross") || m.contains("taigo") || m.contains("stonic") || m.contains("סטוניק") ||
            m.contains("niro") || m.contains("נירו") || m.contains("seltos") || m.contains("פורסטר") || m.contains("forester") ||
            m.contains("crosstrek") || m.contains("קרוסטרק") || m.contains("xv") || m.contains("wrangler") || m.contains("רנגלר") ||
            m.contains("cherokee") || m.contains("צ'ירוקי") || m.contains("land cruiser") || m.contains("לנד קרוזר") || m.contains("land-cruiser") ||
            m.contains("pajero") || m.contains("פאג'רו") || m.contains("defender") || m.contains("דיפנדר") || m.contains("discovery") ||
            m.contains("דיסקברי") || m.contains("patrol") || m.contains("פאטרול") || (m.contains("cross") && !m.contains("lacrosse")) -> "🚙 פנאי-שטח SUV"

            // 9. Hatchbacks & Small City Cars (הצ'בק / סופרמיני)
            m.contains("הצ'בק") || m.contains("האצ'בק") || m.contains("mg4") || m.contains("golf") || m.contains("גולף") ||
            m.contains("polo") || m.contains("פולו") || m.contains("ibiza") || m.contains("איביזה") || m.contains("leon") ||
            m.contains("לאון") || m.contains("clio") || m.contains("קליאו") || m.contains("208") || m.contains("yaris") ||
            m.contains("יאריס") || m.contains("i20") || m.contains("i10") || m.contains("picanto") || m.contains("פיקנטו") ||
            m.contains("micra") || m.contains("מיקרה") || m.contains("fiesta") || m.contains("פיאסטה") || m.contains("spark") ||
            m.contains("ספארק") || m.contains("space star") || m.contains("ספייס סטאר") || m.contains("fabia") || m.contains("פאביה") ||
            m.contains("sandero") || m.contains("סנדרו") || m.contains("c3") || m.contains("500") || m.contains("swift") ||
            m.contains("סוויפט") || m.contains("ignis") || m.contains("איגניס") || m.contains("aygo") || m.contains("אייגו") ||
            m.contains("leaf") || m.contains("ליף") || m.contains("zoe") || m.contains("dolphin") || m.contains("דולפין") -> "🚗 הצ'בק"

            // 10. Vintage / Collector
            combined.contains("אספנות") || (t.contains("רכב אספנות")) -> "🏆 אספנות"

            // 11. Default Passenger Car (פרטי / סדאן / מנהלים)
            else -> "🚗 רכב פרטי"
        }
    }
}