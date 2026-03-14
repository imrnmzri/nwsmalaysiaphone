package my.gov.met.nwsmalaysia.util

import my.gov.met.nwsmalaysia.data.model.WarningResponse
import java.security.MessageDigest

object WarningFingerprint {

    fun compute(w: WarningResponse): String {
        val title = w.warningIssue?.titleBm.orEmpty().ifBlank { w.warningIssue?.titleEn.orEmpty() }
        val raw = "$title|${w.validFrom.orEmpty()}|${w.validTo.orEmpty()}|${w.state.orEmpty()}"
        return md5(raw)
    }

    private fun md5(input: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
