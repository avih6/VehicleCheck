package com.avih6.vehiclecheck

import com.avih6.vehiclecheck.data.EmissionFilterRecord
import com.avih6.vehiclecheck.data.EvChargingStationRecord
import com.avih6.vehiclecheck.data.GarageRecord
import com.avih6.vehiclecheck.data.NetworkClient
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class ServicesDataTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Test
    fun testEmissionFilterRecordParsing() {
        val sampleJson = """
            {
              "_id": 1,
              "mispar_rechev": 5822614,
              "sug_rechev_EU_cd": "N3",
              "shnat_yitzur": 2006,
              "rishum_rishon_dt": "2005-12-18",
              "taarich_hatkana": "2019-08-12"
            }
        """.trimIndent()

        val record = json.decodeFromString<EmissionFilterRecord>(sampleJson)
        assertEquals(5822614L, record.licensePlate)
        assertEquals(2006, record.year)
        assertEquals("2019-08-12", record.installDate)
    }

    @Test
    fun testGarageRecordParsing() {
        val sampleJson = """
            {
              "_id": 101,
              "mispar_mosah": 16,
              "shem_mosah": "מכון רישוי טסט ליין",
              "cod_sug_mosah": 6,
              "sug_mosah": "מכון רישוי",
              "ktovet": "דרך השלום 12",
              "yishuv": "תל אביב",
              "telephone": "03-1234567",
              "mikud": 67890,
              "cod_miktzoa": 10,
              "miktzoa": "מבחני רישוי שנתי",
              "menahel_miktzoa": "ישראל ישראלי"
            }
        """.trimIndent()

        val record = json.decodeFromString<GarageRecord>(sampleJson)
        assertEquals("מכון רישוי טסט ליין", record.garageName)
        assertEquals("תל אביב", record.city)
        assertEquals("03-1234567", record.phone)
        assertTrue("Should be identified as test station", record.isTestStation)
    }

    @Test
    fun testEvChargingStationRecordParsing() {
        val sampleJson = """
            {
              "_id": 1,
              "OID": 50,
              "OBJECTID": 100,
              "op": "AfconEv",
              "name": "קניון עזריאלי תל אביב",
              "Address": "דרך מנחם בגין 132",
              "count": 6,
              "cnt_fast": 2,
              "cnt_slow": 4
            }
        """.trimIndent()

        val record = json.decodeFromString<EvChargingStationRecord>(sampleJson)
        assertEquals("AfconEv", record.operator)
        assertEquals("קניון עזריאלי תל אביב", record.stationName)
        assertEquals(6, record.totalSockets)
        assertEquals(2, record.fastSockets)
        assertEquals(4, record.slowSockets)
        assertTrue("Should have fast charging", record.hasFastCharging)
    }

    @Test
    fun testServicesSpecialties() {
        val acOpt = com.avih6.vehiclecheck.data.ServicesSpecialties.garageOptions.firstOrNull { it.title.contains("מיזוג") }
        assertNotNull("Air conditioning option must exist", acOpt)
        assertTrue("Air conditioning must contain official 'שירות תיקון למזגן אויר לרכב'", acOpt!!.dbValues.contains("שירות תיקון למזגן אויר לרכב"))

        val testAnnualOpt = com.avih6.vehiclecheck.data.ServicesSpecialties.testStationOptions.firstOrNull { it.title.contains("טסט") }
        assertNotNull("Test station annual option must exist", testAnnualOpt)
        assertEquals("מכון רישוי-פתוח לשרות הציבור", testAnnualOpt!!.dbValue)
    }

    @Test
    fun testCarDealerRecordParsing() {
        val sampleJson = """
            {
              "_id": 1,
              "shem": "אזולאי עודד",
              "yishuv": "מבשרת ציון",
              "mikud": 90805,
              "ktovet": "ארבל 2",
              "het_pei": "514001122"
            }
        """.trimIndent()
        val record = json.decodeFromString<com.avih6.vehiclecheck.data.CarDealerRecord>(sampleJson)
        assertEquals("אזולאי עודד", record.name)
        assertEquals("מבשרת ציון", record.city)
        assertEquals("514001122", record.companyId)
    }

    @Test
    fun testCarAppraiserRecordParsing() {
        val sampleJson = """
            {
              "_id": 1,
              "mispar_rishayon": 1040,
              "shem_prati": "עלי",
              "shem_mishpaha": "חאג'",
              "yishuv": "אבו סנאן"
            }
        """.trimIndent()
        val record = json.decodeFromString<com.avih6.vehiclecheck.data.CarAppraiserRecord>(sampleJson)
        assertEquals(1040, record.licenseNumber)
        assertEquals("עלי חאג'", record.fullName)
        assertEquals("אבו סנאן", record.city)
    }

    @Test
    fun testPartsTradeRecordParsing() {
        val sampleJson = """
            {
              "_id": 2,
              "mispar_esek": 1,
              "shem_esek": "אייל רחמים 4 על 4",
              "sug_esek": "סחר",
              "yishuv": "קריית מלאכי",
              "ktovet": "ח.התעשיה 24",
              "telephone": "0559901800",
              "cod_isuk": 120,
              "isuk": "חלקי חילוף חדשים"
            }
        """.trimIndent()
        val record = json.decodeFromString<com.avih6.vehiclecheck.data.PartsTradeRecord>(sampleJson)
        assertEquals("אייל רחמים 4 על 4", record.businessName)
        assertEquals("סחר", record.businessType)
        assertEquals("0559901800", record.phone)
        assertEquals("חלקי חילוף חדשים", record.occupation)
    }

    @Test
    fun testServicesCategoryResourceIds() {
        com.avih6.vehiclecheck.data.ServicesCategory.values().forEach { cat ->
            assertFalse("Resource ID must not be blank for ${cat.name}", cat.resourceId.isBlank())
        }
        assertEquals(6, com.avih6.vehiclecheck.data.ServicesCategory.values().size)
    }
}
