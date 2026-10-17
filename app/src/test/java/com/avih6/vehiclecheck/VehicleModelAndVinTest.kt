package com.avih6.vehiclecheck

import com.avih6.vehiclecheck.data.VehicleUtils
import org.junit.Assert.*
import org.junit.Test

class VehicleModelAndVinTest {

    @Test
    fun testCleanIdentificationCodeWithPlusAndSymbols() {
        // VIN surrounded by plus characters as in Israeli Ministry of Transport records
        assertEquals("F351E-483903", VehicleUtils.cleanIdentificationCode("+F351E-483903+"))
        assertEquals("123456", VehicleUtils.cleanIdentificationCode("*123456*"))
        assertEquals("123456", VehicleUtils.cleanIdentificationCode("#123456#"))
        assertEquals("123456", VehicleUtils.cleanIdentificationCode("~123456~"))
        assertEquals("123456", VehicleUtils.cleanIdentificationCode("<123456>"))
    }

    @Test
    fun testCleanIdentificationCodeFiltersDummyValues() {
        // Engine model / number dummy values should return null
        assertNull(VehicleUtils.cleanIdentificationCode("1"))
        assertNull(VehicleUtils.cleanIdentificationCode("0"))
        assertNull(VehicleUtils.cleanIdentificationCode("00"))
        assertNull(VehicleUtils.cleanIdentificationCode("-"))
        assertNull(VehicleUtils.cleanIdentificationCode("."))
        assertNull(VehicleUtils.cleanIdentificationCode("?"))
        assertNull(VehicleUtils.cleanIdentificationCode("לא ידוע"))
        assertNull(VehicleUtils.cleanIdentificationCode("אין מידע"))
        assertNull(VehicleUtils.cleanIdentificationCode("NULL"))
        assertNull(VehicleUtils.cleanIdentificationCode("NONE"))
        assertNull(VehicleUtils.cleanIdentificationCode("N/A"))
    }

    @Test
    fun testFordFSeriesModelRecognition() {
        // F-350 with Hebrew transliteration "אף 350"
        val known350 = VehicleUtils.resolveKnownModelName(make = "פורד", modelCode = null, modelName = "אף 350", vin = null)
        assertEquals("F-350", known350)

        val baseInfo350 = VehicleUtils.extractBaseModel(make = "פורד", model = "אף 350", modelCode = null)
        assertEquals("F-350", baseInfo350.baseModel)
        assertTrue(baseInfo350.searchTerms.contains("F350"))
        assertTrue(baseInfo350.searchTerms.contains("F-350"))
        assertTrue(baseInfo350.searchTerms.contains("אף 350"))

        // F-250 with Hebrew transliteration "אף 250"
        val known250 = VehicleUtils.resolveKnownModelName(make = "פורד", modelCode = null, modelName = "אף 250", vin = null)
        assertEquals("F-250", known250)

        // F-150 with Hebrew transliteration "אף 150"
        val known150 = VehicleUtils.resolveKnownModelName(make = "פורד", modelCode = null, modelName = "אף 150", vin = null)
        assertEquals("F-150", known150)
    }

    @Test
    fun testAutomaticModelVariantsForSpecificVehicles() {
        // Vehicle 600-457: Ford אף 350
        val baseInfo457 = VehicleUtils.extractBaseModel(make = "פורד", model = "אף 350", modelCode = null)
        assertEquals("F-350", baseInfo457.baseModel)
        assertTrue(baseInfo457.searchTerms.contains("F-350"))
        assertTrue(baseInfo457.searchTerms.contains("F350"))
        assertTrue(baseInfo457.searchTerms.contains("אף 350"))

        // Vehicle 600-118: Chevy סי 3803
        val baseInfo118 = VehicleUtils.extractBaseModel(make = "שברול", model = "סי 3803", modelCode = null)
        assertEquals("C-3803", baseInfo118.baseModel)
        assertTrue(baseInfo118.searchTerms.contains("C-3803"))
        assertTrue(baseInfo118.searchTerms.contains("C3803"))
        assertTrue(baseInfo118.searchTerms.contains("סי 3803"))
        assertTrue(baseInfo118.searchTerms.contains("3803"))

        // Vehicle 600-115: Peugeot 403 בי 8
        val baseInfo115 = VehicleUtils.extractBaseModel(make = "פיג'ו", model = "403 בי 8", modelCode = null)
        assertEquals("403 B8", baseInfo115.baseModel)
        assertTrue(baseInfo115.searchTerms.contains("403"))
        assertTrue(baseInfo115.searchTerms.contains("403 B8"))
        assertTrue(baseInfo115.searchTerms.contains("403 בי 8"))
    }

    @Test
    fun testAutomaticPhoneticsAndAcronymsWithoutHardcoding() {
        // Dynamic letter-number models (BMW, Mercedes, Audi, Chevy, etc.)
        val x5 = VehicleUtils.extractBaseModel(make = "במוו", model = "איקס 5", modelCode = null)
        assertEquals("X-5", x5.baseModel)
        assertTrue(x5.searchTerms.contains("X5"))
        assertTrue(x5.searchTerms.contains("איקס 5"))

        val e220 = VehicleUtils.extractBaseModel(make = "מרצדס", model = "אי 220", modelCode = null)
        assertEquals("E-220", e220.baseModel)
        assertTrue(e220.searchTerms.contains("E220"))
        assertTrue(e220.searchTerms.contains("אי 220"))

        val rav4 = VehicleUtils.extractBaseModel(make = "טויוטה", model = "ראב 4", modelCode = null)
        assertTrue(rav4.searchTerms.contains("RAV4") || rav4.searchTerms.contains("RAV 4"))
        assertTrue(rav4.searchTerms.contains("ראב 4"))

        val dmax = VehicleUtils.extractBaseModel(make = "איסוזו", model = "די מקס", modelCode = null)
        assertTrue(dmax.searchTerms.contains("D-MAX") || dmax.searchTerms.contains("DMAX"))
        assertTrue(dmax.searchTerms.contains("די מקס"))
    }

    @Test
    fun testSubaruForesterModelExtraction() {
        // Vehicle 179-67-103: Subaru Forester with technical modelCode SK9DYJL
        val baseInfo = VehicleUtils.extractBaseModel(
            make = "סובארו",
            model = "FORESTER",
            modelCode = "SK9DYJL",
            vin = "JF2SKESC1NH473374"
        )
        assertEquals("פורסטר", baseInfo.baseModel)
        assertTrue("searchTerms should contain FORESTER", baseInfo.searchTerms.contains("FORESTER"))
        assertTrue("searchTerms should contain פורסטר", baseInfo.searchTerms.contains("פורסטר"))
        assertFalse("Technical trim code SK9DYJL should NOT pollute search terms when model name is present", baseInfo.searchTerms.contains("SK9DYJL"))
        assertEquals("FORESTER", baseInfo.exactKinuyFilter)
    }
}
