package com.avih6.vehiclecheck

import com.avih6.vehiclecheck.data.DtcRepository
import org.junit.Assert.*
import org.junit.BeforeClass
import org.junit.Test
import java.io.File
import java.io.FileInputStream

class DtcRepositoryTest {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setUp() {
            val candidates = listOf(
                File("src/main/assets/dtc_codes.tsv"),
                File("src/main/assets/dtc_codes.tsv.gz"),
                File("app/src/main/assets/dtc_codes.tsv"),
                File("app/src/main/assets/dtc_codes.tsv.gz")
            )
            val assetFile = candidates.firstOrNull { it.exists() }
            if (assetFile != null) {
                FileInputStream(assetFile).use {
                    DtcRepository.loadDatabase(it)
                }
            }
        }
    }

    @Test
    fun testU0301AndU0310AreDistinct() {
        val u0301 = DtcRepository.lookupCode("U0301")
        val u0310 = DtcRepository.lookupCode("U0310")

        assertNotNull("U0301 should be found", u0301)
        assertNotNull("U0310 should be found", u0310)

        assertNotEquals("U0301 and U0310 must have distinct Hebrew titles", u0301!!.titleHe, u0310!!.titleHe)
        assertNotEquals("U0301 and U0310 must have distinct English titles", u0301.titleEn, u0310.titleEn)

        assertTrue("U0301 must mention ECM/PCM or מחשב ניהול מנוע", u0301.titleHe.contains("מנוע") || u0301.titleEn.contains("ECM/PCM"))
        assertTrue("U0310 must mention Fuel Pump or משאבת דלק", u0310.titleHe.contains("משאבת דלק") || u0310.titleEn.contains("Fuel Pump"))
    }

    @Test
    fun testCuratedCodesHavePriority() {
        val p0300 = DtcRepository.lookupCode("P0300")
        assertNotNull(p0300)
        assertEquals("P0300", p0300!!.code)
        assertTrue(p0300.symptomsHe.isNotEmpty())
        assertTrue(p0300.possibleCausesHe.isNotEmpty())
    }

    @Test
    fun testSearchCodes() {
        val fuelPumpResults = DtcRepository.searchCodes("משאבת דלק")
        assertTrue("Searching for משאבת דלק should return results", fuelPumpResults.isNotEmpty())

        val u0301Results = DtcRepository.searchCodes("U0301")
        assertTrue("Searching for U0301 should return U0301", u0301Results.any { it.code == "U0301" })
    }
}
