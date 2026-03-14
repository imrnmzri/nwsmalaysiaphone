package my.gov.met.nwsmalaysia

import my.gov.met.nwsmalaysia.data.model.WarningIssue
import my.gov.met.nwsmalaysia.data.model.WarningResponse
import my.gov.met.nwsmalaysia.util.WarningFingerprint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class WarningFingerprintTest {

    private fun makeWarning(
        titleBm: String = "AMARAN BANJIR",
        titleEn: String = "FLOOD WARNING",
        validFrom: String = "2025-01-01 08:00:00",
        validTo: String = "2025-01-02 08:00:00",
        state: String? = "Selangor"
    ) = WarningResponse(
        validFrom = validFrom,
        validTo = validTo,
        headingBm = "",
        headingEn = "",
        textBm = "",
        textEn = "",
        warningIssue = WarningIssue(
            titleBm = titleBm,
            titleEn = titleEn,
            issued = "2025-01-01 06:00:00"
        ),
        state = state,
        district = null
    )

    @Test
    fun sameWarningProducesSameFingerprint() {
        val w = makeWarning()
        assertEquals(WarningFingerprint.compute(w), WarningFingerprint.compute(w))
    }

    @Test
    fun differentStateProducesDifferentFingerprint() {
        val w1 = makeWarning(state = "Selangor")
        val w2 = makeWarning(state = "Sabah")
        assertNotEquals(WarningFingerprint.compute(w1), WarningFingerprint.compute(w2))
    }

    @Test
    fun levelExtractionAmaran() {
        assertEquals("AMARAN", WarningFingerprint.extractLevelString("AMARAN BANJIR"))
    }

    @Test
    fun levelExtractionWaspada() {
        assertEquals("WASPADA", WarningFingerprint.extractLevelString("WASPADA HUJAN LEBAT"))
    }

    @Test
    fun levelExtractionNasihat() {
        assertEquals("NASIHAT", WarningFingerprint.extractLevelString("NASIHAT CUACA"))
    }

    @Test
    fun levelPriorityOrder() {
        assert(WarningFingerprint.levelPriority("AMARAN") > WarningFingerprint.levelPriority("WASPADA"))
        assert(WarningFingerprint.levelPriority("WASPADA") > WarningFingerprint.levelPriority("NASIHAT"))
    }
}
