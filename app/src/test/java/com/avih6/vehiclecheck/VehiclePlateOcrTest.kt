package com.avih6.vehiclecheck

import com.avih6.vehiclecheck.data.VehicleUtils
import org.junit.Assert.*
import org.junit.Test

class VehiclePlateOcrTest {

    @Test
    fun testHyphenatedPlate8Digits() {
        val ocrSample = """
            IL ישראל
            786-92-603
            MERCEDES SPRINTER
        """.trimIndent()

        val candidates = VehicleUtils.extractLicensePlateCandidates(ocrSample)
        assertEquals(1, candidates.size)
        assertEquals("78692603", candidates.first())
    }

    @Test
    fun testHyphenatedPlate7Digits() {
        val ocrSample = """
            12-345-67
            TOYOTA RAV4
        """.trimIndent()

        val candidates = VehicleUtils.extractLicensePlateCandidates(ocrSample)
        assertEquals(1, candidates.size)
        assertEquals("1234567", candidates.first())
    }

    @Test
    fun testSpacedPlate() {
        val ocrSample = "IL 123 45 678 ISRAEL"
        val candidates = VehicleUtils.extractLicensePlateCandidates(ocrSample)
        assertEquals(1, candidates.size)
        assertEquals("12345678", candidates.first())
    }

    @Test
    fun testColonSeparatedPlate() {
        val ocrSample = "56:661:26"
        val candidates = VehicleUtils.extractLicensePlateCandidates(ocrSample)
        assertEquals(1, candidates.size)
        assertEquals("5666126", candidates.first())
    }

    @Test
    fun testCleanRawDigitsOnLine() {
        val ocrSample = """
            IL
            12345678
        """.trimIndent()

        val candidates = VehicleUtils.extractLicensePlateCandidates(ocrSample)
        assertEquals(1, candidates.size)
        assertEquals("12345678", candidates.first())
    }

    @Test
    fun testMultiplePlatesDetected() {
        val ocrSample = """
            Parking lot photo
            Car 1: 12-345-67
            Car 2: 786-92-603
        """.trimIndent()

        val candidates = VehicleUtils.extractLicensePlateCandidates(ocrSample)
        assertEquals(2, candidates.size)
        assertTrue(candidates.contains("1234567"))
        assertTrue(candidates.contains("78692603"))
    }

    @Test
    fun testNoPlateFoundInRandomText() {
        val ocrSample = """
            Hello World!
            This is a landscape with trees and mountains.
            No vehicle here.
        """.trimIndent()

        val candidates = VehicleUtils.extractLicensePlateCandidates(ocrSample)
        assertTrue(candidates.isEmpty())
    }

    @Test
    fun testHistorical5DigitPlate() {
        val ocrSample = """
            Collector Car
            45-120
        """.trimIndent()

        val candidates = VehicleUtils.extractLicensePlateCandidates(ocrSample)
        assertEquals(1, candidates.size)
        assertEquals("45120", candidates.first())
    }
}
