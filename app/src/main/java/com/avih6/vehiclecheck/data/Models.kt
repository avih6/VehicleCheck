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

object FlexibleStringSerializer : KSerializer<String?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FlexibleString", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: String?) {
        if (value != null) encoder.encodeString(value) else encoder.encodeNull()
    }
    override fun deserialize(decoder: Decoder): String? {
        val jsonDecoder = decoder as? JsonDecoder ?: return null
        val element = jsonDecoder.decodeJsonElement()
        if (element is JsonNull) return null
        return element.jsonPrimitive.content.trim().ifBlank { null }
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
    @SerialName("grira_nm") val towingCapacityHeavy: String? = null,
    @SerialName("mispar_manoa") val engineNumber: String? = null,
    @SerialName("bitul_dt") val cancellationDate: String? = null
) {
    val effectiveModel: String? get() = if (!model.isNullOrBlank()) model else if (!modelCode.isNullOrBlank()) modelCode else null
    val effectiveVin: String? get() = if (!vin.isNullOrBlank()) vin else if (!vinAlt.isNullOrBlank()) vinAlt else vinHeavy
    val effectiveStandardType: String? get() = if (!standardType.isNullOrBlank()) standardType else standardTypeHeavy
    val effectiveVehicleCategory: String? get() = if (!vehicleCategory.isNullOrBlank()) vehicleCategory else vehicleCategoryHeavy
    val effectiveSeats: Int? get() = seats ?: seatsHeavy
    val effectiveSeatsNextToDriver: Int? get() = seatsNextToDriver ?: seatsNextToDriverHeavy
    val effectiveCargoWeight: Int? get() = cargoWeight ?: cargoWeightHeavy ?: if (totalWeight != null && curbWeight != null && totalWeight > curbWeight) totalWeight - curbWeight else null
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
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("mispar_rechev") val licensePlateRaw: String? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("tozeret_cd") val makeCodeRaw: String? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("tozeret_nm") val make: String? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("degem_cd") val modelCodeRaw: String? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("degem_nm") val modelCode: String? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("sug_rechev_nm") val vehicleType: String? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("sug_degem") val sugDegem: String? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("moed_aliya_lakvish") val onRoadDate: String? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("bitul_dt") val cancellationDate: String? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("misgeret") val vin: String? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("shilda") val vinAlt: String? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("mispar_shilda") val vinAlt2: String? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("degem_manoa") val engineModel: String? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("mispar_manoa") val engineNumber: String? = null,
    @Serializable(with = FlexibleIntSerializer::class) @SerialName("nefach_manoa") val engineDisplacementRaw: Int? = null,
    @Serializable(with = FlexibleIntSerializer::class) @SerialName("mishkal_kolel") val totalWeightRaw: Int? = null,
    @Serializable(with = FlexibleIntSerializer::class) @SerialName("mishkal_azmi") val curbWeightRaw: Int? = null,
    @Serializable(with = FlexibleIntSerializer::class) @SerialName("mishkal_mitan_harama") val cargoWeightRaw: Int? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("hanaa_nm") val driveType: String? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("hanaa_cd") val driveTypeCd: String? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("tozeret_eretz_nm") val countryOfOrigin: String? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("tkina_EU") val standardType: String? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("ramat_gimur") val trimLevel: String? = null,
    @Serializable(with = FlexibleIntSerializer::class) @SerialName("shnat_yitzur") val yearRaw: Int? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("baalut") val ownership: String? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("tzeva_rechev") val color: String? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("zmig_kidmi") val frontTire: String? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("zmig_ahori") val rearTire: String? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("sug_delek_nm") val fuelType: String? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("horaat_rishum") val registrationDirectiveRaw: String? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("kinuy_mishari") val model: String? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("mivchan_acharon_dt") val lastTestDate: String? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("mivchan_aharon_dt") val lastTestDateAlt: String? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("tokef_dt") val testExpiryDate: String? = null
) {
    fun toVehicleRecord(): VehicleRecord {
        val plate = licensePlateRaw?.filter { it.isDigit() }?.toLongOrNull()
        val year = yearRaw
        val makeCd = makeCodeRaw?.filter { it.isDigit() }?.toLongOrNull()
        val modelCd = modelCodeRaw?.filter { it.isDigit() }?.toLongOrNull()
        val directive = registrationDirectiveRaw?.filter { it.isDigit() }?.toLongOrNull()
        val effectiveLastTest = lastTestDate ?: lastTestDateAlt
        val effectiveExpiry = testExpiryDate ?: cancellationDate
        val rawVin = vin ?: vinAlt ?: vinAlt2
        val effectiveDrive = if (!driveType.isNullOrBlank()) driveType else if (driveTypeCd == "1") "4X2" else if (driveTypeCd == "2") "4X4" else null
        val effectiveCc = engineDisplacementRaw?.let { if (it > 0) it else null }
        val effectiveTotalWeight = totalWeightRaw?.let { if (it > 0) it else null }
        val effectiveCurbWeight = curbWeightRaw?.let { if (it > 0) it else null }
        val effectiveCargoWeight = cargoWeightRaw?.let { if (it > 0) it else null }

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
            engineNumber = engineNumber,
            engineDisplacement = effectiveCc,
            totalWeight = effectiveTotalWeight,
            curbWeight = effectiveCurbWeight,
            driveType = effectiveDrive,
            countryOfOrigin = countryOfOrigin,
            standardType = standardType,
            cancellationDate = cancellationDate,
            frontTire = frontTire,
            rearTire = rearTire,
            safetyRating = null,
            emissionGroup = null,
            vin = rawVin,
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
            ownership = "ציוד עבודה / חברה",
            modelType = vehicleType ?: "ציוד מכני הנדסי (צמ\"ה)",
            fuelType = "דיזל / מנוע תעשייתי",
            color = "צהוב / תעשייתי",
            vin = vin
        )
    }
}

@Serializable
data class EngineeringPollutionRecord(
    @SerialName("_id") val id: Long? = null,
    @SerialName("mispar_tzama") val licensePlate: Long? = null,
    @SerialName("dargat_zihum_avir") val pollutionLevel: String? = null,
    @SerialName("hutkan_mesanen_helkikim") val particulateFilterInstalled: String? = null,
    @SerialName("murshe_peelut") val activityAuthorized: String? = null,
    @SerialName("power_engine_kilowalt") val powerKw: Double? = null
)

@Serializable
data class SafetyDiscountRecord(
    @SerialName("_id") val id: Long? = null,
    @SerialName("mispar_rechev") val licensePlate: Long? = null,
    @SerialName("updated_dt") val updatedDate: String? = null
)

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
    @Serializable(with = FlexibleLongSerializer::class) @SerialName("_id") val id: Long? = null,
    @Serializable(with = FlexibleLongSerializer::class) @SerialName("mispar_rechev") val licensePlate: Long? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("mispar_manoa") val engineNumber: String? = null,
    @Serializable(with = FlexibleLongSerializer::class) @SerialName("kilometer_test_aharon") val lastTestMileage: Long? = null,
    @Serializable(with = FlexibleIntSerializer::class) @SerialName("shinui_mivne_ind") val structuralChange: Int? = null,
    @Serializable(with = FlexibleIntSerializer::class) @SerialName("gapam_ind") val lpgInstalled: Int? = null,
    @Serializable(with = FlexibleIntSerializer::class) @SerialName("shnui_zeva_ind") val colorChange: Int? = null,
    @Serializable(with = FlexibleIntSerializer::class) @SerialName("shinui_zmig_ind") val tireChange: Int? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("rishum_rishon_dt") val firstRegistrationDate: String? = null,
    @Serializable(with = FlexibleStringSerializer::class) @SerialName("mkoriut_nm") val originality: String? = null
)

@Serializable
data class DisabledPermitRecord(
    @Serializable(with = FlexibleLongSerializer::class) @SerialName("_id") val id: Long? = null,
    @Serializable(with = FlexibleLongSerializer::class) @SerialName("MISPAR RECHEV") val licensePlate: Long? = null,
    @Serializable(with = FlexibleLongSerializer::class) @SerialName("TAARICH HAFAKAT TAG") val issueDate: Long? = null,
    @Serializable(with = FlexibleLongSerializer::class) @SerialName("SUG TAV") val permitType: Long? = null
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

data class NationalFleetStats(
    val activePrivate: Int = 4_176_920,
    val activeHeavy: Int = 421_662,
    val activeMotorcycles: Int = 191_434,
    val inactive2017: Int = 1_207_744,
    val inactive2010_2016: Int = 670_293,
    val inactive2000_2009: Int = 499_791,
    val inactiveVintagePre2000: Int = 1_438_964,
    val engineeringEquipment: Int = 183_845
) {
    val totalActive: Int get() = activePrivate + activeHeavy + activeMotorcycles
    val totalInactiveModern: Int get() = inactive2017 + inactive2010_2016 + inactive2000_2009
    val totalInactive: Int get() = totalInactiveModern + inactiveVintagePre2000
    val grandTotal: Int get() = totalActive + totalInactive + engineeringEquipment

    val activePrivatePercent: Float get() = if (totalActive > 0) (activePrivate.toFloat() / totalActive) * 100f else 87.2f
    val activeHeavyPercent: Float get() = if (totalActive > 0) (activeHeavy.toFloat() / totalActive) * 100f else 8.8f
    val activeMotorcyclesPercent: Float get() = if (totalActive > 0) (activeMotorcycles.toFloat() / totalActive) * 100f else 4.0f
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
        val alternateVehicle: VehicleRecord? = null,
        val alternateVehicleIsOffRoad: Boolean = false,
        val alternateVehicleOffRoadDate: String? = null,
        val equipmentPollution: EngineeringPollutionRecord? = null,
        val safetyDiscount: SafetyDiscountRecord? = null
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
            clean.contains("ישראל") || clean.equals("ISRAEL", ignoreCase = true) -> "ישראל 🇮🇱"
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

    fun getBrandLogoUrls(hebrewMake: String?, modelName: String? = null): List<String> {
        val slug = getBrandSlug(hebrewMake, modelName)
        if (slug == "car" || slug == "trailer") return emptyList()

        val list = mutableListOf<String>()

        // 1. High-speed jsDelivr CDN (Instant CDN delivery, 100% reliable, no 403 or Wikimedia rate limits)
        list.add("https://cdn.jsdelivr.net/gh/filippofilip95/car-logos-dataset@master/logos/optimized/$slug.png")
        val cleanSlug = slug.replace("-", "")
        if (cleanSlug != slug) {
            list.add("https://cdn.jsdelivr.net/gh/filippofilip95/car-logos-dataset@master/logos/optimized/$cleanSlug.png")
        }

        // 2. Direct high-res vectors and verified fallbacks for major and specialized brands
        when (slug) {
            "skoda" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/8/87/%C5%A0koda_logo_%282023%29.svg/500px-%C5%A0koda_logo_%282023%29.svg.png")
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/3/3d/%C5%A0koda_Auto_logo_%282016%29.svg/500px-%C5%A0koda_Auto_logo_%282016%29.svg.png")
            }
            "subaru" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/e/e7/Subaru_logo_%282019%29.svg/500px-Subaru_logo_%282019%29.svg.png")
            }
            "toyota" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/e/ee/Toyota_logo_%282020%29.svg/500px-Toyota_logo_%282020%29.svg.png")
            }
            "hyundai" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/4/44/Hyundai_Motor_Company_logo.svg/500px-Hyundai_Motor_Company_logo.svg.png")
            }
            "kia" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/4/47/Kia_logo.svg/500px-Kia_logo.svg.png")
            }
            "volkswagen" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/6/6d/Volkswagen_logo_2019.svg/500px-Volkswagen_logo_2019.svg.png")
            }
            "seat" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/8/83/SEAT_Logo_2017.svg/500px-SEAT_Logo_2017.svg.png")
            }
            "peugeot" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/f/f9/Logo_Peugeot.svg/500px-Logo_Peugeot.svg.png")
            }
            "renault" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/b/b7/Renault_2021_Logo.svg/500px-Renault_2021_Logo.svg.png")
            }
            "bmw" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/4/44/BMW.svg/500px-BMW.svg.png")
            }
            "mercedes-benz", "mercedes" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/9/90/Mercedes-Logo.svg/500px-Mercedes-Logo.svg.png")
            }
            "audi" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/9/92/Audi-Logo_2016.svg/500px-Audi-Logo_2016.svg.png")
            }
            "mazda" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/3/36/Mazda_Logo.svg/500px-Mazda_Logo.svg.png")
            }
            "nissan" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/8/8c/Nissan_2020_logo.svg/500px-Nissan_2020_logo.svg.png")
            }
            "honda" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/3/38/Honda.svg/500px-Honda.svg.png")
            }
            "suzuki" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/1/12/Suzuki_logo_2.svg/500px-Suzuki_logo_2.svg.png")
            }
            "mitsubishi" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/5/5a/Mitsubishi_logo.svg/500px-Mitsubishi_logo.svg.png")
            }
            "fiat" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/1/12/Fiat_Automobiles_logo.svg/500px-Fiat_Automobiles_logo.svg.png")
            }
            "citroen" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/b/b2/Citro%C3%ABn_2022.svg/500px-Citro%C3%ABn_2022.svg.png")
            }
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
            "atlas-copco" -> {
                list.add("https://upload.wikimedia.org/wikipedia/commons/thumb/6/6f/Atlas_Copco_logo.svg/500px-Atlas_Copco_logo.svg.png")
                list.add("https://cdn.jsdelivr.net/gh/filippofilip95/car-logos-dataset@master/logos/optimized/atlas-copco.png")
            }
        }

        // Additional fallbacks
        list.add("https://raw.githubusercontent.com/filippofilip95/car-logos-dataset/master/logos/optimized/$slug.png")
        list.add("https://cdn.jsdelivr.net/gh/filippofilip95/car-logos-dataset@master/logos/thumb/$slug.png")

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

    fun getBrandSlug(hebrewMake: String?, modelName: String? = null): String {
        val m = "${hebrewMake.orEmpty()} ${modelName.orEmpty()}".lowercase()
        val predefined = when {
            m.contains("סוסיתא") || m.contains("סוסיטה") || m.contains("אוטוקרס") || m.contains("כרמל") || m.contains("רום כרמל") || m.contains("תעשיות רכב") || m.contains("תע\"ר") || m.contains("sussita") || m.contains("autocars") -> "sussita"
            m.contains("ג'קו") || m.contains("ג'אקו") || m.contains("גקו") || m.contains("גאקו") || m.contains("ג'ייקו") || m.contains("גייקו") || m.contains("jaecoo") -> "jaecoo"
            m.contains("אומודה") || m.contains("omoda") -> "omoda"
            m.contains("קופרה") || m.contains("cupra") -> "cupra"
            m.contains("פולסטאר") || m.contains("פול סטאר") || m.contains("polestar") -> "polestar"
            m.contains("ג'נסיס") || m.contains("גנסיס") || m.contains("genesis") -> "genesis"
            m.contains("קורבט") || m.contains("corvette") -> "corvette"
            m.contains("אבארט") || m.contains("אברט") || m.contains("abarth") -> "abarth"
            m.contains("די אס") || m.contains("ds automobiles") || m.contains("ds3") || m.contains("ds4") || m.contains("ds7") || m.contains("ds9") -> "ds"
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
            m.contains("אטלס קופקו") || m.contains("אטלס-קופקו") || m.contains("אטלס") || m.contains("atlas copco") || m.contains("atlas-copco") || m.contains("atlas") -> "atlas-copco"
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

    fun getCountryFlagEmoji(countryName: String?): String? {
        val iso = getCountryIsoCode(countryName) ?: return null
        if (iso.length != 2) return null
        val upper = iso.uppercase()
        val firstChar = Character.codePointAt(upper, 0) - 0x41 + 0x1F1E6
        val secondChar = Character.codePointAt(upper, 1) - 0x41 + 0x1F1E6
        return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
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
            combinedText.contains("ישראל") || combinedText.contains("israel") ||
            combinedText.contains("סוסיתא") || combinedText.contains("אוטוקרס") ||
            combinedText.contains("כרמל") || combinedText.contains("רום כרמל") ||
            combinedText.contains("תעשיות רכב") || combinedText.contains("תע\"ר") ||
            combinedText.contains("תער") || combinedText.contains("סברה") ||
            combinedText.contains("סופה") || combinedText.contains("אביר") ||
            combinedText.contains("הארגז") || combinedText.contains("מרכבים") -> "ישראל 🇮🇱"
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
            "sussita" -> "ישראל 🇮🇱"
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
            m.contains("דיסקברי") || m.contains("patrol") || m.contains("פאטרול") || (m.contains("cross") && !m.contains("lacrosse")) -> "🚙 פנאי-שטח"

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

data class ModelSuggestion(
    val brandHebrew: String,
    val brandEnglish: String,
    val modelHebrew: String,
    val modelEnglish: String,
    val searchQuery: String
)

object VehicleModelCatalog {
    val allModels: List<ModelSuggestion> = listOf(
        // Hyundai
        ModelSuggestion("יונדאי", "Hyundai", "טוסון", "Tucson", "יונדאי טוסון"),
        ModelSuggestion("יונדאי", "Hyundai", "איוניק 5", "Ioniq 5", "יונדאי איוניק 5"),
        ModelSuggestion("יונדאי", "Hyundai", "איוניק 6", "Ioniq 6", "יונדאי איוניק 6"),
        ModelSuggestion("יונדאי", "Hyundai", "איוניק 9", "Ioniq 9", "יונדאי איוניק 9"),
        ModelSuggestion("יונדאי", "Hyundai", "איוניק", "Ioniq", "יונדאי איוניק"),
        ModelSuggestion("יונדאי", "Hyundai", "קונה", "Kona", "יונדאי קונה"),
        ModelSuggestion("יונדאי", "Hyundai", "אלנטרה", "Elantra", "יונדאי אלנטרה"),
        ModelSuggestion("יונדאי", "Hyundai", "סנטה פה", "Santa Fe", "יונדאי סנטה פה"),
        ModelSuggestion("יונדאי", "Hyundai", "פליסייד", "Palisade", "יונדאי פליסייד"),
        ModelSuggestion("יונדאי", "Hyundai", "באיון", "Bayon", "יונדאי באיון"),
        ModelSuggestion("יונדאי", "Hyundai", "וניו", "Venue", "יונדאי וניו"),
        ModelSuggestion("יונדאי", "Hyundai", "סטאריה", "Staria", "יונדאי סטאריה"),
        ModelSuggestion("יונדאי", "Hyundai", "אינספר", "Inster", "יונדאי אינספר"),
        ModelSuggestion("יונדאי", "Hyundai", "i10", "I10", "יונדאי i10"),
        ModelSuggestion("יונדאי", "Hyundai", "i20", "I20", "יונדאי i20"),
        ModelSuggestion("יונדאי", "Hyundai", "i30", "I30", "יונדאי i30"),
        ModelSuggestion("יונדאי", "Hyundai", "i35", "I35", "יונדאי i35"),
        ModelSuggestion("יונדאי", "Hyundai", "i25", "I25", "יונדאי i25"),
        ModelSuggestion("יונדאי", "Hyundai", "גטס", "Getz", "יונדאי גטס"),
        ModelSuggestion("יונדאי", "Hyundai", "אקסנט", "Accent", "יונדאי אקסנט"),

        // Toyota
        ModelSuggestion("טויוטה", "Toyota", "קורולה", "Corolla", "טויוטה קורולה"),
        ModelSuggestion("טויוטה", "Toyota", "קורולה קרוס", "Corolla Cross", "טויוטה קורולה קרוס"),
        ModelSuggestion("טויוטה", "Toyota", "יאריס", "Yaris", "טויוטה יאריס"),
        ModelSuggestion("טויוטה", "Toyota", "יאריס קרוס", "Yaris Cross", "טויוטה יאריס קרוס"),
        ModelSuggestion("טויוטה", "Toyota", "ראב 4", "RAV4", "טויוטה ראב 4"),
        ModelSuggestion("טויוטה", "Toyota", "C-HR", "C-HR", "טויוטה C-HR"),
        ModelSuggestion("טויוטה", "Toyota", "קאמרי", "Camry", "טויוטה קאמרי"),
        ModelSuggestion("טויוטה", "Toyota", "פריוס", "Prius", "טויוטה פריוס"),
        ModelSuggestion("טויוטה", "Toyota", "פריוס פלוס", "Prius Plus", "טויוטה פריוס פלוס"),
        ModelSuggestion("טויוטה", "Toyota", "לנד קרוזר", "Land Cruiser", "טויוטה לנד קרוזר"),
        ModelSuggestion("טויוטה", "Toyota", "היילקס", "Hilux", "טויוטה היילקס"),
        ModelSuggestion("טויוטה", "Toyota", "היילנדר", "Highlander", "טויוטה היילנדר"),
        ModelSuggestion("טויוטה", "Toyota", "אייגו", "Aygo", "טויוטה אייגו"),
        ModelSuggestion("טויוטה", "Toyota", "אייגו X", "Aygo X", "טויוטה אייגו X"),
        ModelSuggestion("טויוטה", "Toyota", "bZ4X", "bZ4X", "טויוטה bZ4X"),
        ModelSuggestion("טויוטה", "Toyota", "פרואייס", "Proace", "טויוטה פרואייס"),
        ModelSuggestion("טויוטה", "Toyota", "אוריס", "Auris", "טויוטה אוריס"),
        ModelSuggestion("טויוטה", "Toyota", "אוונסיס", "Avensis", "טויוטה אוונסיס"),
        ModelSuggestion("טויוטה", "Toyota", "ורסו", "Verso", "טויוטה ורסו"),

        // Kia
        ModelSuggestion("קיה", "Kia", "פיקנטו", "Picanto", "קיה פיקנטו"),
        ModelSuggestion("קיה", "Kia", "ספורטאז'", "Sportage", "קיה ספורטאז'"),
        ModelSuggestion("קיה", "Kia", "נירו", "Niro", "קיה נירו"),
        ModelSuggestion("קיה", "Kia", "נירו פלוס", "Niro Plus", "קיה נירו פלוס"),
        ModelSuggestion("קיה", "Kia", "סטוניק", "Stonic", "קיה סטוניק"),
        ModelSuggestion("קיה", "Kia", "סורנטו", "Sorento", "קיה סורנטו"),
        ModelSuggestion("קיה", "Kia", "קרניבל", "Carnival", "קיה קרניבל"),
        ModelSuggestion("קיה", "Kia", "סיד", "Ceed", "קיה סיד"),
        ModelSuggestion("קיה", "Kia", "אקסיד", "XCeed", "קיה אקסיד"),
        ModelSuggestion("קיה", "Kia", "פרוסיד", "Proceed", "קיה פרוסיד"),
        ModelSuggestion("קיה", "Kia", "ריו", "Rio", "קיה ריו"),
        ModelSuggestion("קיה", "Kia", "סלטוס", "Seltos", "קיה סלטוס"),
        ModelSuggestion("קיה", "Kia", "EV3", "EV3", "קיה EV3"),
        ModelSuggestion("קיה", "Kia", "EV6", "EV6", "קיה EV6"),
        ModelSuggestion("קיה", "Kia", "EV9", "EV9", "קיה EV9"),
        ModelSuggestion("קיה", "Kia", "סול", "Soul", "קיה סול"),
        ModelSuggestion("קיה", "Kia", "פורטה", "Forte", "קיה פורטה"),
        ModelSuggestion("קיה", "Kia", "קארנס", "Carens", "קיה קארנס"),
        ModelSuggestion("קיה", "Kia", "אופטימה", "Optima", "קיה אופטימה"),

        // Skoda
        ModelSuggestion("סקודה", "Skoda", "אוקטביה", "Octavia", "סקודה אוקטביה"),
        ModelSuggestion("סקודה", "Skoda", "סופרב", "Superb", "סקודה סופרב"),
        ModelSuggestion("סקודה", "Skoda", "קודיאק", "Kodiaq", "סקודה קודיאק"),
        ModelSuggestion("סקודה", "Skoda", "קארוק", "Karoq", "סקודה קארוק"),
        ModelSuggestion("סקודה", "Skoda", "קאמיק", "Kamiq", "סקודה קאמיק"),
        ModelSuggestion("סקודה", "Skoda", "פאביה", "Fabia", "סקודה פאביה"),
        ModelSuggestion("סקודה", "Skoda", "סקאלה", "Scala", "סקודה סקאלה"),
        ModelSuggestion("סקודה", "Skoda", "אניאק", "Enyaq", "סקודה אניאק"),
        ModelSuggestion("סקודה", "Skoda", "אניאק קופה", "Enyaq Coupe", "סקודה אניאק קופה"),
        ModelSuggestion("סקודה", "Skoda", "אלרוק", "Elroq", "סקודה אלרוק"),
        ModelSuggestion("סקודה", "Skoda", "ראפיד", "Rapid", "סקודה ראפיד"),
        ModelSuggestion("סקודה", "Skoda", "יטי", "Yeti", "סקודה יטי"),
        ModelSuggestion("סקודה", "Skoda", "רומסטר", "Roomster", "סקודה רומסטר"),
        ModelSuggestion("סקודה", "Skoda", "סיטיגו", "Citigo", "סקודה סיטיגו"),

        // Mazda
        ModelSuggestion("מאזדה", "Mazda", "מאזדה 3", "Mazda 3", "מאזדה 3"),
        ModelSuggestion("מאזדה", "Mazda", "מאזדה 2", "Mazda 2", "מאזדה 2"),
        ModelSuggestion("מאזדה", "Mazda", "מאזדה 6", "Mazda 6", "מאזדה 6"),
        ModelSuggestion("מאזדה", "Mazda", "CX-5", "CX-5", "מאזדה CX-5"),
        ModelSuggestion("מאזדה", "Mazda", "CX-30", "CX-30", "מאזדה CX-30"),
        ModelSuggestion("מאזדה", "Mazda", "CX-60", "CX-60", "מאזדה CX-60"),
        ModelSuggestion("מאזדה", "Mazda", "CX-90", "CX-90", "מאזדה CX-90"),
        ModelSuggestion("מאזדה", "Mazda", "מיאטה MX-5", "MX-5 Miata", "מאזדה מיאטה"),

        // BYD
        ModelSuggestion("BYD", "BYD", "אטו 3", "Atto 3", "BYD Atto 3"),
        ModelSuggestion("BYD", "BYD", "דולפין", "Dolphin", "BYD Dolphin"),
        ModelSuggestion("BYD", "BYD", "דולפין מיני", "Dolphin Mini", "BYD Dolphin Mini"),
        ModelSuggestion("BYD", "BYD", "סיגאל", "Seagull", "BYD Seagull"),
        ModelSuggestion("BYD", "BYD", "סיל", "Seal", "BYD Seal"),
        ModelSuggestion("BYD", "BYD", "סיל U", "Seal U", "BYD Seal U"),
        ModelSuggestion("BYD", "BYD", "סילאיון 7", "Sealion 7", "BYD Sealion 7"),
        ModelSuggestion("BYD", "BYD", "טאנג", "Tang", "BYD Tang"),
        ModelSuggestion("BYD", "BYD", "האן", "Han", "BYD Han"),

        // Tesla
        ModelSuggestion("טסלה", "Tesla", "מודל 3", "Model 3", "טסלה מודל 3"),
        ModelSuggestion("טסלה", "Tesla", "מודל Y", "Model Y", "טסלה מודל Y"),
        ModelSuggestion("טסלה", "Tesla", "מודל S", "Model S", "טסלה מודל S"),
        ModelSuggestion("טסלה", "Tesla", "מודל X", "Model X", "טסלה מודל X"),
        ModelSuggestion("טסלה", "Tesla", "סייברטראק", "Cybertruck", "טסלה סייברטראק"),

        // Volkswagen
        ModelSuggestion("פולקסווגן", "Volkswagen", "גולף", "Golf", "פולקסווגן גולף"),
        ModelSuggestion("פולקסווגן", "Volkswagen", "פולו", "Polo", "פולקסווגן פולו"),
        ModelSuggestion("פולקסווגן", "Volkswagen", "טיגואן", "Tiguan", "פולקסווגן טיגואן"),
        ModelSuggestion("פולקסווגן", "Volkswagen", "טיגואן אולספייס", "Tiguan Allspace", "פולקסווגן טיגואן אולספייס"),
        ModelSuggestion("פולקסווגן", "Volkswagen", "טי-רוק", "T-Roc", "פולקסווגן טי רוק"),
        ModelSuggestion("פולקסווגן", "Volkswagen", "טי-קרוס", "T-Cross", "פולקסווגן טי קרוס"),
        ModelSuggestion("פולקסווגן", "Volkswagen", "טאיגו", "Taigo", "פולקסווגן טאיגו"),
        ModelSuggestion("פולקסווגן", "Volkswagen", "טוארג", "Touareg", "פולקסווגן טוארג"),
        ModelSuggestion("פולקסווגן", "Volkswagen", "פאסאט", "Passat", "פולקסווגן פאסאט"),
        ModelSuggestion("פולקסווגן", "Volkswagen", "קאדי", "Caddy", "פולקסווגן קאדי"),
        ModelSuggestion("פולקסווגן", "Volkswagen", "טרנספורטר", "Transporter", "פולקסווגן טרנספורטר"),
        ModelSuggestion("פולקסווגן", "Volkswagen", "מולטיוואן", "Multivan", "פולקסווגן מולטיוואן"),
        ModelSuggestion("פולקסווגן", "Volkswagen", "קראפטר", "Crafter", "פולקסווגן קראפטר"),
        ModelSuggestion("פולקסווגן", "Volkswagen", "שירוקו", "Scirocco", "פולקסווגן שירוקו"),
        ModelSuggestion("פולקסווגן", "Volkswagen", "ארטאון", "Arteon", "פולקסווגן ארטאון"),
        ModelSuggestion("פולקסווגן", "Volkswagen", "ID.3", "ID.3", "פולקסווגן ID.3"),
        ModelSuggestion("פולקסווגן", "Volkswagen", "ID.4", "ID.4", "פולקסווגן ID.4"),
        ModelSuggestion("פולקסווגן", "Volkswagen", "ID.5", "ID.5", "פולקסווגן ID.5"),
        ModelSuggestion("פולקסווגן", "Volkswagen", "ID.7", "ID.7", "פולקסווגן ID.7"),
        ModelSuggestion("פולקסווגן", "Volkswagen", "ID.Buzz", "ID.Buzz", "פולקסווגן ID Buzz"),

        // Seat & Cupra
        ModelSuggestion("סיאט", "Seat", "איביזה", "Ibiza", "סיאט איביזה"),
        ModelSuggestion("סיאט", "Seat", "ארונה", "Arona", "סיאט ארונה"),
        ModelSuggestion("סיאט", "Seat", "אטקה", "Ateca", "סיאט אטקה"),
        ModelSuggestion("סיאט", "Seat", "לאון", "Leon", "סיאט לאון"),
        ModelSuggestion("סיאט", "Seat", "טראקו", "Tarraco", "סיאט טראקו"),
        ModelSuggestion("קופרה", "Cupra", "פורמנטור", "Formentor", "קופרה פורמנטור"),
        ModelSuggestion("קופרה", "Cupra", "לאון", "Leon", "קופרה לאון"),
        ModelSuggestion("קופרה", "Cupra", "אטקה", "Ateca", "קופרה אטקה"),
        ModelSuggestion("קופרה", "Cupra", "בורן", "Born", "קופרה בורן"),
        ModelSuggestion("קופרה", "Cupra", "טוואסקאן", "Tavascan", "קופרה טוואסקאן"),

        // Geely & Zeekr
        ModelSuggestion("ג'ילי", "Geely", "גיאומטרי C", "Geometry C", "ג'ילי גיאומטרי C"),
        ModelSuggestion("ג'ילי", "Geely", "EX5", "EX5", "ג'ילי EX5"),
        ModelSuggestion("ג'ילי", "Geely", "מונג'ארו", "Monjaro", "ג'ילי מונג'ארו"),
        ModelSuggestion("זיקר", "Zeekr", "001", "001", "זיקר 001"),
        ModelSuggestion("זיקר", "Zeekr", "X", "X", "זיקר X"),
        ModelSuggestion("זיקר", "Zeekr", "7X", "7X", "זיקר 7X"),
        ModelSuggestion("זיקר", "Zeekr", "007", "007", "זיקר 007"),
        ModelSuggestion("זיקר", "Zeekr", "009", "009", "זיקר 009"),

        // MG
        ModelSuggestion("MG", "MG", "3", "3", "MG 3"),
        ModelSuggestion("MG", "MG", "4", "4", "MG 4"),
        ModelSuggestion("MG", "MG", "5", "5", "MG 5"),
        ModelSuggestion("MG", "MG", "ZS", "ZS", "MG ZS"),
        ModelSuggestion("MG", "MG", "ZS EV", "ZS EV", "MG ZS EV"),
        ModelSuggestion("MG", "MG", "HS", "HS", "MG HS"),
        ModelSuggestion("MG", "MG", "EHS", "EHS", "MG EHS"),
        ModelSuggestion("MG", "MG", "מארוול R", "Marvel R", "MG מארוול R"),
        ModelSuggestion("MG", "MG", "סייברסטר", "Cyberster", "MG סייברסטר"),

        // Chery & Jaecoo & Omoda
        ModelSuggestion("צ'רי", "Chery", "טיגו 8 פרו", "Tiggo 8 Pro", "צ'רי טיגו 8 פרו"),
        ModelSuggestion("צ'רי", "Chery", "טיגו 7 פרו", "Tiggo 7 Pro", "צ'רי טיגו 7 פרו"),
        ModelSuggestion("צ'רי", "Chery", "FX", "FX", "צ'רי FX"),
        ModelSuggestion("ג'אקו", "Jaecoo", "5", "5", "ג'אקו 5"),
        ModelSuggestion("ג'אקו", "Jaecoo", "5 בנזין", "5 Gasoline", "ג'אקו 5"),
        ModelSuggestion("ג'אקו", "Jaecoo", "5 הייבריד", "5 HEV", "ג'אקו 5"),
        ModelSuggestion("ג'אקו", "Jaecoo", "5 חשמלי", "5 BEV", "ג'אקו 5"),
        ModelSuggestion("ג'אקו", "Jaecoo", "7", "7", "ג'אקו 7"),
        ModelSuggestion("ג'אקו", "Jaecoo", "7 פלאג-אין", "7 PHEV", "ג'אקו 7"),
        ModelSuggestion("ג'אקו", "Jaecoo", "8", "8", "ג'אקו 8"),
        ModelSuggestion("ג'אקו", "Jaecoo", "8 פלאג-אין", "8 PHEV", "ג'אקו 8"),
        ModelSuggestion("אומודה", "Omoda", "5", "5", "אומודה 5"),
        ModelSuggestion("אומודה", "Omoda", "7", "7", "אומודה 7"),
        ModelSuggestion("אומודה", "Omoda", "9", "9", "אומודה 9"),

        // Xpeng
        ModelSuggestion("אקספנג", "Xpeng", "G6", "G6", "אקספנג G6"),
        ModelSuggestion("אקספנג", "Xpeng", "G9", "G9", "אקספנג G9"),
        ModelSuggestion("אקספנג", "Xpeng", "P7", "P7", "אקספנג P7"),

        // Leapmotor
        ModelSuggestion("ליפמוטור", "Leapmotor", "T03", "T03", "ליפמוטור T03"),
        ModelSuggestion("ליפמוטור", "Leapmotor", "C10", "C10", "ליפמוטור C10"),
        ModelSuggestion("ליפמוטור", "Leapmotor", "B10", "B10", "ליפמוטור B10"),

        // Smart
        ModelSuggestion("סמארט", "Smart", "#1", "#1", "סמארט 1"),
        ModelSuggestion("סמארט", "Smart", "#3", "#3", "סמארט 3"),
        ModelSuggestion("סמארט", "Smart", "#5", "#5", "סמארט 5"),

        // Peugeot
        ModelSuggestion("פיג'ו", "Peugeot", "208", "208", "פיג'ו 208"),
        ModelSuggestion("פיג'ו", "Peugeot", "2008", "2008", "פיג'ו 2008"),
        ModelSuggestion("פיג'ו", "Peugeot", "3008", "3008", "פיג'ו 3008"),
        ModelSuggestion("פיג'ו", "Peugeot", "5008", "5008", "פיג'ו 5008"),
        ModelSuggestion("פיג'ו", "Peugeot", "308", "308", "פיג'ו 308"),
        ModelSuggestion("פיג'ו", "Peugeot", "408", "408", "פיג'ו 408"),
        ModelSuggestion("פיג'ו", "Peugeot", "508", "508", "פיג'ו 508"),
        ModelSuggestion("פיג'ו", "Peugeot", "ריפטר", "Rifter", "פיג'ו ריפטר"),
        ModelSuggestion("פיג'ו", "Peugeot", "פרטנר", "Partner", "פיג'ו פרטנר"),
        ModelSuggestion("פיג'ו", "Peugeot", "בוקסר", "Boxer", "פיג'ו בוקסר"),

        // Renault
        ModelSuggestion("רנו", "Renault", "קליאו", "Clio", "רנו קליאו"),
        ModelSuggestion("רנו", "Renault", "קפצ'ור", "Captur", "רנו קפצ'ור"),
        ModelSuggestion("רנו", "Renault", "מגאן גרנד קופה", "Megane Grand Coupe", "רנו מגאן"),
        ModelSuggestion("רנו", "Renault", "מגאן E-Tech", "Megane E-Tech", "רנו מגאן חשמלית"),
        ModelSuggestion("רנו", "Renault", "ארקנה", "Arkana", "רנו ארקנה"),
        ModelSuggestion("רנו", "Renault", "אוסטרל", "Austral", "רנו אוסטרל"),
        ModelSuggestion("רנו", "Renault", "קנגו", "Kangoo", "רנו קנגו"),
        ModelSuggestion("רנו", "Renault", "מאסטר", "Master", "רנו מאסטר"),
        ModelSuggestion("רנו", "Renault", "טראפיק", "Trafic", "רנו טראפיק"),
        ModelSuggestion("רנו", "Renault", "קוליאוס", "Koleos", "רנו קוליאוס"),

        // Citroen
        ModelSuggestion("סיטרואן", "Citroen", "C3", "C3", "סיטרואן C3"),
        ModelSuggestion("סיטרואן", "Citroen", "C3 איירקרוס", "C3 Aircross", "סיטרואן C3 איירקרוס"),
        ModelSuggestion("סיטרואן", "Citroen", "C4", "C4", "סיטרואן C4"),
        ModelSuggestion("סיטרואן", "Citroen", "C4X", "C4X", "סיטרואן C4X"),
        ModelSuggestion("סיטרואן", "Citroen", "C5 איירקרוס", "C5 Aircross", "סיטרואן C5"),
        ModelSuggestion("סיטרואן", "Citroen", "ברלינגו", "Berlingo", "סיטרואן ברלינגו"),
        ModelSuggestion("סיטרואן", "Citroen", "ג'אמפי", "Jumpy", "סיטרואן ג'אמפי"),
        ModelSuggestion("סיטרואן", "Citroen", "ג'אמפר", "Jumper", "סיטרואן ג'אמפר"),
        ModelSuggestion("סיטרואן", "Citroen", "C1", "C1", "סיטרואן C1"),

        // Chevrolet
        ModelSuggestion("שברולט", "Chevrolet", "ספארק", "Spark", "שברולט ספארק"),
        ModelSuggestion("שברולט", "Chevrolet", "טראוורס", "Traverse", "שברולט טראוורס"),
        ModelSuggestion("שברולט", "Chevrolet", "אקווינוקס", "Equinox", "שברולט אקווינוקס"),
        ModelSuggestion("שברולט", "Chevrolet", "בלייזר", "Blazer", "שברולט בלייזר"),
        ModelSuggestion("שברולט", "Chevrolet", "טראקס", "Trax", "שברולט טראקס"),
        ModelSuggestion("שברולט", "Chevrolet", "טריילבלייזר", "Trailblazer", "שברולט טריילבלייזר"),
        ModelSuggestion("שברולט", "Chevrolet", "סילברדו", "Silverado", "שברולט סילברדו"),
        ModelSuggestion("שברולט", "Chevrolet", "טאהו", "Tahoe", "שברולט טאהו"),
        ModelSuggestion("שברולט", "Chevrolet", "סברבן", "Suburban", "שברולט סברבן"),
        ModelSuggestion("שברולט", "Chevrolet", "קמארו", "Camaro", "שברולט קמארו"),
        ModelSuggestion("שברולט", "Chevrolet", "קורבט", "Corvette", "שברולט קורבט"),
        ModelSuggestion("שברולט", "Chevrolet", "מאליבו", "Malibu", "שברולט מאליבו"),
        ModelSuggestion("שברולט", "Chevrolet", "קרוז", "Cruze", "שברולט קרוז"),
        ModelSuggestion("שברולט", "Chevrolet", "סוניק", "Sonic", "שברולט סוניק"),
        ModelSuggestion("שברולט", "Chevrolet", "אורלנדו", "Orlando", "שברולט אורלנדו"),
        ModelSuggestion("שברולט", "Chevrolet", "קפטיבה", "Captiva", "שברולט קפטיבה"),
        ModelSuggestion("שברולט", "Chevrolet", "בולט", "Bolt", "שברולט בולט"),

        // Subaru
        ModelSuggestion("סובארו", "Subaru", "פורסטר", "Forester", "סובארו פורסטר"),
        ModelSuggestion("סובארו", "Subaru", "קרוסטרק (XV)", "Crosstrek XV", "סובארו קרוסטרק"),
        ModelSuggestion("סובארו", "Subaru", "אאוטבק", "Outback", "סובארו אאוטבק"),
        ModelSuggestion("סובארו", "Subaru", "אימפרזה", "Impreza", "סובארו אימפרזה"),
        ModelSuggestion("סובארו", "Subaru", "B4 (לגאסי)", "B4 Legacy", "סובארו B4"),
        ModelSuggestion("סובארו", "Subaru", "BRZ", "BRZ", "סובארו BRZ"),
        ModelSuggestion("סובארו", "Subaru", "אבולטיס", "Evoltis", "סובארו אבולטיס"),
        ModelSuggestion("סובארו", "Subaru", "סולטרה", "Solterra", "סובארו סולטרה"),

        // Suzuki
        ModelSuggestion("סוזוקי", "Suzuki", "איגניס", "Ignis", "סוזוקי איגניס"),
        ModelSuggestion("סוזוקי", "Suzuki", "סוויפט", "Swift", "סוזוקי סוויפט"),
        ModelSuggestion("סוזוקי", "Suzuki", "ויטארה", "Vitara", "סוזוקי ויטארה"),
        ModelSuggestion("סוזוקי", "Suzuki", "S-Cross קרוסאובר", "S-Cross", "סוזוקי קרוסאובר"),
        ModelSuggestion("סוזוקי", "Suzuki", "ג'ימני", "Jimny", "סוזוקי ג'ימני"),
        ModelSuggestion("סוזוקי", "Suzuki", "בלנו", "Baleno", "סוזוקי בלנו"),
        ModelSuggestion("סוזוקי", "Suzuki", "אלטו", "Alto", "סוזוקי אלטו"),
        ModelSuggestion("סוזוקי", "Suzuki", "סלריו", "Celerio", "סוזוקי סלריו"),
        ModelSuggestion("סוזוקי", "Suzuki", "ספלאש", "Splash", "סוזוקי ספלאש"),
        ModelSuggestion("סוזוקי", "Suzuki", "SX4", "SX4", "סוזוקי SX4"),

        // Nissan
        ModelSuggestion("ניסאן", "Nissan", "קשקאי", "Qashqai", "ניסאן קשקאי"),
        ModelSuggestion("ניסאן", "Nissan", "ג'וק", "Juke", "ניסאן ג'וק"),
        ModelSuggestion("ניסאן", "Nissan", "אקס-טרייל", "X-Trail", "ניסאן אקס טרייל"),
        ModelSuggestion("ניסאן", "Nissan", "מיקרה", "Micra", "ניסאן מיקרה"),
        ModelSuggestion("ניסאן", "Nissan", "סנטרה", "Sentra", "ניסאן סנטרה"),
        ModelSuggestion("ניסאן", "Nissan", "אריה", "Ariya", "ניסאן אריה"),
        ModelSuggestion("ניסאן", "Nissan", "ליף", "Leaf", "ניסאן ליף"),
        ModelSuggestion("ניסאן", "Nissan", "נווארה", "Navara", "ניסאן נווארה"),
        ModelSuggestion("ניסאן", "Nissan", "אלטימה", "Altima", "ניסאן אלטימה"),
        ModelSuggestion("ניסאן", "Nissan", "מקסימה", "Maxima", "ניסאן מקסימה"),
        ModelSuggestion("ניסאן", "Nissan", "NV200", "NV200", "ניסאן NV200"),

        // Mitsubishi
        ModelSuggestion("מיצובישי", "Mitsubishi", "אאוטלנדר", "Outlander", "מיצובישי אאוטלנדר"),
        ModelSuggestion("מיצובישי", "Mitsubishi", "ASX", "ASX", "מיצובישי ASX"),
        ModelSuggestion("מיצובישי", "Mitsubishi", "ספייס סטאר", "Space Star", "מיצובישי ספייס סטאר"),
        ModelSuggestion("מיצובישי", "Mitsubishi", "אקליפס קרוס", "Eclipse Cross", "מיצובישי אקליפס קרוס"),
        ModelSuggestion("מיצובישי", "Mitsubishi", "אטרז'", "Attrage", "מיצובישי אטרז'"),
        ModelSuggestion("מיצובישי", "Mitsubishi", "פאג'רו", "Pajero", "מיצובישי פאג'רו"),
        ModelSuggestion("מיצובישי", "Mitsubishi", "טרייטון (L200)", "Triton L200", "מיצובישי טרייטון"),
        ModelSuggestion("מיצובישי", "Mitsubishi", "לנסר", "Lancer", "מיצובישי לנסר"),

        // Honda
        ModelSuggestion("הונדה", "Honda", "סיוויק", "Civic", "הונדה סיוויק"),
        ModelSuggestion("הונדה", "Honda", "CR-V", "CR-V", "הונדה CR-V"),
        ModelSuggestion("הונדה", "Honda", "HR-V", "HR-V", "הונדה HR-V"),
        ModelSuggestion("הונדה", "Honda", "ג'אז", "Jazz", "הונדה ג'אז"),
        ModelSuggestion("הונדה", "Honda", "אקורד", "Accord", "הונדה אקורד"),
        ModelSuggestion("הונדה", "Honda", "ZR-V", "ZR-V", "הונדה ZR-V"),
        ModelSuggestion("הונדה", "Honda", "e:Ny1", "e:Ny1", "הונדה eNy1"),

        // Mercedes
        ModelSuggestion("מרצדס", "Mercedes-Benz", "A-Class", "A-Class", "מרצדס A-Class"),
        ModelSuggestion("מרצדס", "Mercedes-Benz", "C-Class", "C-Class", "מרצדס C-Class"),
        ModelSuggestion("מרצדס", "Mercedes-Benz", "E-Class", "E-Class", "מרצדס E-Class"),
        ModelSuggestion("מרצדס", "Mercedes-Benz", "S-Class", "S-Class", "מרצדס S-Class"),
        ModelSuggestion("מרצדס", "Mercedes-Benz", "CLA", "CLA", "מרצדס CLA"),
        ModelSuggestion("מרצדס", "Mercedes-Benz", "GLA", "GLA", "מרצדס GLA"),
        ModelSuggestion("מרצדס", "Mercedes-Benz", "GLB", "GLB", "מרצדס GLB"),
        ModelSuggestion("מרצדס", "Mercedes-Benz", "GLC", "GLC", "מרצדס GLC"),
        ModelSuggestion("מרצדס", "Mercedes-Benz", "GLE", "GLE", "מרצדס GLE"),
        ModelSuggestion("מרצדס", "Mercedes-Benz", "GLS", "GLS", "מרצדס GLS"),
        ModelSuggestion("מרצדס", "Mercedes-Benz", "G-Class", "G-Class", "מרצדס G-Class"),
        ModelSuggestion("מרצדס", "Mercedes-Benz", "EQA", "EQA", "מרצדס EQA"),
        ModelSuggestion("מרצדס", "Mercedes-Benz", "EQB", "EQB", "מרצדס EQB"),
        ModelSuggestion("מרצדס", "Mercedes-Benz", "EQE", "EQE", "מרצדס EQE"),
        ModelSuggestion("מרצדס", "Mercedes-Benz", "EQS", "EQS", "מרצדס EQS"),
        ModelSuggestion("מרצדס", "Mercedes-Benz", "ספרינטר", "Sprinter", "מרצדס ספרינטר"),
        ModelSuggestion("מרצדס", "Mercedes-Benz", "ויטו", "Vito", "מרצדס ויטו"),
        ModelSuggestion("מרצדס", "Mercedes-Benz", "V-Class", "V-Class", "מרצדס V-Class"),

        // BMW
        ModelSuggestion("ב.מ.וו", "BMW", "סדרה 1", "1 Series", "ב.מ.וו סדרה 1"),
        ModelSuggestion("ב.מ.וו", "BMW", "סדרה 2", "2 Series", "ב.מ.וו סדרה 2"),
        ModelSuggestion("ב.מ.וו", "BMW", "סדרה 3", "3 Series", "ב.מ.וו סדרה 3"),
        ModelSuggestion("ב.מ.וו", "BMW", "סדרה 4", "4 Series", "ב.מ.וו סדרה 4"),
        ModelSuggestion("ב.מ.וו", "BMW", "סדרה 5", "5 Series", "ב.מ.וו סדרה 5"),
        ModelSuggestion("ב.מ.וו", "BMW", "סדרה 7", "7 Series", "ב.מ.וו סדרה 7"),
        ModelSuggestion("ב.מ.וו", "BMW", "X1", "X1", "ב.מ.וו X1"),
        ModelSuggestion("ב.מ.וו", "BMW", "X2", "X2", "ב.מ.וו X2"),
        ModelSuggestion("ב.מ.וו", "BMW", "X3", "X3", "ב.מ.וו X3"),
        ModelSuggestion("ב.מ.וו", "BMW", "X4", "X4", "ב.מ.וו X4"),
        ModelSuggestion("ב.מ.וו", "BMW", "X5", "X5", "ב.מ.וו X5"),
        ModelSuggestion("ב.מ.וו", "BMW", "X6", "X6", "ב.מ.וו X6"),
        ModelSuggestion("ב.מ.וו", "BMW", "X7", "X7", "ב.מ.וו X7"),
        ModelSuggestion("ב.מ.וו", "BMW", "i4", "i4", "ב.מ.וו i4"),
        ModelSuggestion("ב.מ.וו", "BMW", "iX1", "iX1", "ב.מ.וו iX1"),
        ModelSuggestion("ב.מ.וו", "BMW", "iX3", "iX3", "ב.מ.וו iX3"),
        ModelSuggestion("ב.מ.וו", "BMW", "iX", "iX", "ב.מ.וו iX"),
        ModelSuggestion("ב.מ.וו", "BMW", "M3", "M3", "ב.מ.וו M3"),
        ModelSuggestion("ב.מ.וו", "BMW", "M4", "M4", "ב.מ.וו M4"),
        ModelSuggestion("ב.מ.וו", "BMW", "M5", "M5", "ב.מ.וו M5"),

        // Audi
        ModelSuggestion("אאודי", "Audi", "A1", "A1", "אאודי A1"),
        ModelSuggestion("אאודי", "Audi", "A3", "A3", "אאודי A3"),
        ModelSuggestion("אאודי", "Audi", "A4", "A4", "אאודי A4"),
        ModelSuggestion("אאודי", "Audi", "A5", "A5", "אאודי A5"),
        ModelSuggestion("אאודי", "Audi", "A6", "A6", "אאודי A6"),
        ModelSuggestion("אאודי", "Audi", "A7", "A7", "אאודי A7"),
        ModelSuggestion("אאודי", "Audi", "A8", "A8", "אאודי A8"),
        ModelSuggestion("אאודי", "Audi", "Q2", "Q2", "אאודי Q2"),
        ModelSuggestion("אאודי", "Audi", "Q3", "Q3", "אאודי Q3"),
        ModelSuggestion("אאודי", "Audi", "Q4 e-tron", "Q4 e-tron", "אאודי Q4 e-tron"),
        ModelSuggestion("אאודי", "Audi", "Q5", "Q5", "אאודי Q5"),
        ModelSuggestion("אאודי", "Audi", "Q7", "Q7", "אאודי Q7"),
        ModelSuggestion("אאודי", "Audi", "Q8", "Q8", "אאודי Q8"),
        ModelSuggestion("אאודי", "Audi", "e-tron GT", "e-tron GT", "אאודי e-tron GT"),
        ModelSuggestion("אאודי", "Audi", "TT", "TT", "אאודי TT"),

        // Volvo
        ModelSuggestion("וולוו", "Volvo", "XC40", "XC40", "וולוו XC40"),
        ModelSuggestion("וולוו", "Volvo", "XC60", "XC60", "וולוו XC60"),
        ModelSuggestion("וולוו", "Volvo", "XC90", "XC90", "וולוו XC90"),
        ModelSuggestion("וולוו", "Volvo", "EX30", "EX30", "וולוו EX30"),
        ModelSuggestion("וולוו", "Volvo", "EX90", "EX90", "וולוו EX90"),
        ModelSuggestion("וולוו", "Volvo", "S60", "S60", "וולוו S60"),
        ModelSuggestion("וולוו", "Volvo", "S90", "S90", "וולוו S90"),
        ModelSuggestion("וולוו", "Volvo", "V40", "V40", "וולוו V40"),

        // Polestar & Lynk & Co
        ModelSuggestion("פולסטאר", "Polestar", "פולסטאר 2", "Polestar 2", "פולסטאר 2"),
        ModelSuggestion("פולסטאר", "Polestar", "פולסטאר 3", "Polestar 3", "פולסטאר 3"),
        ModelSuggestion("פולסטאר", "Polestar", "פולסטאר 4", "Polestar 4", "פולסטאר 4"),
        ModelSuggestion("לינק אנד קו", "Lynk & Co", "01", "01", "לינק אנד קו 01"),
        ModelSuggestion("לינק אנד קו", "Lynk & Co", "02", "02", "לינק אנד קו 02"),

        // XPeng & ORA & WEY & Leapmotor & Voyah & Hongqi & Seres & Smart
        ModelSuggestion("אקספנג", "XPeng", "G6", "G6", "אקספנג G6"),
        ModelSuggestion("אקספנג", "XPeng", "G9", "G9", "אקספנג G9"),
        ModelSuggestion("אקספנג", "XPeng", "P7", "P7", "אקספנג P7"),
        ModelSuggestion("אורה", "ORA", "03 (פאנקי קאט)", "03 Funky Cat", "אורה 03"),
        ModelSuggestion("אורה", "ORA", "07", "07", "אורה 07"),
        ModelSuggestion("וויי", "WEY", "קופי 01", "Coffee 01", "וויי קופי 01"),
        ModelSuggestion("וויי", "WEY", "קופי 02", "Coffee 02", "וויי קופי 02"),
        ModelSuggestion("ליפמוטור", "Leapmotor", "T03", "T03", "ליפמוטור T03"),
        ModelSuggestion("ליפמוטור", "Leapmotor", "C10", "C10", "ליפמוטור C10"),
        ModelSuggestion("סמארט", "Smart", "#1", "#1", "סמארט 1"),
        ModelSuggestion("סמארט", "Smart", "#3", "#3", "סמארט 3"),
        ModelSuggestion("סרס", "Seres", "סרס 3", "Seres 3", "סרס 3"),
        ModelSuggestion("סרס", "Seres", "סרס 5", "Seres 5", "סרס 5"),
        ModelSuggestion("סרס", "Seres", "סרס 7", "Seres 7", "סרס 7"),
        ModelSuggestion("וויה", "Voyah", "פרי", "Free", "וויה פרי"),
        ModelSuggestion("הונגצ'י", "Hongqi", "E-HS9", "E-HS9", "הונגצ'י E-HS9"),
        ModelSuggestion("אייווייס", "Aiways", "U5", "U5", "אייווייס U5"),
        ModelSuggestion("אייווייס", "Aiways", "U6", "U6", "אייווייס U6"),
        ModelSuggestion("סקייוול", "Skywell", "ET5", "ET5", "סקייוול ET5"),
        ModelSuggestion("פורת'ינג", "Forthing", "פריידיי", "Friday T5 EVO", "פורת'ינג פריידיי"),

        // Dacia
        ModelSuggestion("דאצ'יה", "Dacia", "דאסטר", "Duster", "דאצ'יה דאסטר"),
        ModelSuggestion("דאצ'יה", "Dacia", "סנדרו סטפווי", "Sandero Stepway", "דאצ'יה סנדרו"),
        ModelSuggestion("דאצ'יה", "Dacia", "ג'וגר", "Jogger", "דאצ'יה ג'וגר"),
        ModelSuggestion("דאצ'יה", "Dacia", "ספרינג", "Spring", "דאצ'יה ספרינג"),
        ModelSuggestion("דאצ'יה", "Dacia", "לודג'י", "Lodgy", "דאצ'יה לודג'י"),
        ModelSuggestion("דאצ'יה", "Dacia", "דוקר", "Dokker", "דאצ'יה דוקר"),

        // Jeep
        ModelSuggestion("ג'יפ", "Jeep", "רנגלר", "Wrangler", "ג'יפ רנגלר"),
        ModelSuggestion("ג'יפ", "Jeep", "גרנד צ'ירוקי", "Grand Cherokee", "ג'יפ גרנד צ'ירוקי"),
        ModelSuggestion("ג'יפ", "Jeep", "קומפאס", "Compass", "ג'יפ קומפאס"),
        ModelSuggestion("ג'יפ", "Jeep", "רנגייד", "Renegade", "ג'יפ רנגייד"),
        ModelSuggestion("ג'יפ", "Jeep", "אוונג'ר", "Avenger", "ג'יפ אוונג'ר"),
        ModelSuggestion("ג'יפ", "Jeep", "גלדיאטור", "Gladiator", "ג'יפ גלדיאטור"),

        // Ford
        ModelSuggestion("פורד", "Ford", "פוקוס", "Focus", "פורד פוקוס"),
        ModelSuggestion("פורד", "Ford", "פומה", "Puma", "פורד פומה"),
        ModelSuggestion("פורד", "Ford", "קוגה", "Kuga", "פורד קוגה"),
        ModelSuggestion("פורד", "Ford", "מוסטנג", "Mustang", "פורד מוסטנג"),
        ModelSuggestion("פורד", "Ford", "מוסטנג מאך-E", "Mustang Mach-E", "פורד מוסטנג מאך E"),
        ModelSuggestion("פורד", "Ford", "אקספלורר", "Explorer", "פורד אקספלורר"),
        ModelSuggestion("פורד", "Ford", "ברונקו", "Bronco", "פורד ברונקו"),
        ModelSuggestion("פורד", "Ford", "ברונקו ספורט", "Bronco Sport", "פורד ברונקו ספורט"),
        ModelSuggestion("פורד", "Ford", "פיאסטה", "Fiesta", "פורד פיאסטה"),
        ModelSuggestion("פורד", "Ford", "מונדאו", "Mondeo", "פורד מונדאו"),
        ModelSuggestion("פורד", "Ford", "אדג'", "Edge", "פורד אדג'"),
        ModelSuggestion("פורד", "Ford", "טרנזיט", "Transit", "פורד טרנזיט"),
        ModelSuggestion("פורד", "Ford", "טרנזיט קאסטום", "Transit Custom", "פורד טרנזיט"),
        ModelSuggestion("פורד", "Ford", "F-150", "F-150", "פורד F-150"),
        ModelSuggestion("פורד", "Ford", "F-250", "F-250", "פורד F-250"),
        ModelSuggestion("פורד", "Ford", "F-350", "F-350", "פורד F-350"),
        ModelSuggestion("פורד", "Ford", "ריינג'ר", "Ranger", "פורד ריינג'ר"),

        // Porsche
        ModelSuggestion("פורשה", "Porsche", "911", "911", "פורשה 911"),
        ModelSuggestion("פורשה", "Porsche", "קאיין", "Cayenne", "פורשה קאיין"),
        ModelSuggestion("פורשה", "Porsche", "מקאן", "Macan", "פורשה מקאן"),
        ModelSuggestion("פורשה", "Porsche", "פאנאמרה", "Panamera", "פורשה פאנאמרה"),
        ModelSuggestion("פורשה", "Porsche", "טייקאן", "Taycan", "פורשה טייקאן"),
        ModelSuggestion("פורשה", "Porsche", "בוקסטר 718", "718 Boxster", "פורשה בוקסטר"),
        ModelSuggestion("פורשה", "Porsche", "קיימן 718", "718 Cayman", "פורשה קיימן")
    )

    fun search(query: String): List<ModelSuggestion> {
        val clean = query.trim().lowercase()
        if (clean.isBlank()) return emptyList()

        fun norm(s: String) = s.lowercase()
            .replace("'", "")
            .replace("\"", "")
            .replace("״", "")
            .replace("׳", "")
            .replace("`", "")
            .replace("-", " ")
            .trim()

        val normClean = norm(clean)
        val tokens = clean.split("\\s+".toRegex()).filter { it.isNotBlank() }
        val normTokens = normClean.split("\\s+".toRegex()).filter { it.isNotBlank() }

        return allModels.map { suggestion ->
            var score = 0
            val mHeb = suggestion.modelHebrew.lowercase()
            val mEng = suggestion.modelEnglish.lowercase()
            val bHeb = suggestion.brandHebrew.lowercase()
            val bEng = suggestion.brandEnglish.lowercase()
            val fullHeb = suggestion.searchQuery.lowercase()

            val nmHeb = norm(mHeb)
            val nmEng = norm(mEng)
            val nbHeb = norm(bHeb)
            val nbEng = norm(bEng)
            val nfullHeb = norm(fullHeb)

            // Exact match
            if (mHeb == clean || mEng == clean || fullHeb == clean ||
                nmHeb == normClean || nmEng == normClean || nfullHeb == normClean) {
                score += 1000
            }

            // Starts with
            if (mHeb.startsWith(clean) || mEng.startsWith(clean) || bHeb.startsWith(clean) || bEng.startsWith(clean) || fullHeb.startsWith(clean) ||
                nmHeb.startsWith(normClean) || nmEng.startsWith(normClean) || nbHeb.startsWith(normClean) || nbEng.startsWith(normClean) || nfullHeb.startsWith(normClean)) {
                score += 500
            }

            // Contains
            if (mHeb.contains(clean) || mEng.contains(clean) || bHeb.contains(clean) || bEng.contains(clean) || fullHeb.contains(clean) ||
                nmHeb.contains(normClean) || nmEng.contains(normClean) || nbHeb.contains(normClean) || nbEng.contains(normClean) || nfullHeb.contains(normClean)) {
                score += 250
            }

            // Token match
            val allMatched = tokens.all { t ->
                mHeb.contains(t) || mEng.contains(t) || bHeb.contains(t) || bEng.contains(t)
            } || normTokens.all { t ->
                nmHeb.contains(t) || nmEng.contains(t) || nbHeb.contains(t) || nbEng.contains(t)
            }
            if (allMatched && (tokens.size > 1 || normTokens.size > 1)) {
                score += 300
            }

            suggestion to score
        }
        .filter { it.second > 0 }
        .sortedByDescending { it.second }
        .map { it.first }
    }
}