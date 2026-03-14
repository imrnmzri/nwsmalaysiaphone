package my.gov.met.nwsmalaysia.util

import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Formats a raw API timestamp ("yyyy-MM-dd HH:mm:ss") into a human-readable string.
 *
 * @param short true  → "15 Jan, 8:00 AM"   (for compact card use)
 *              false → "Mon, 15 Jan 2024, 8:00 AM"  (for detail screen use)
 */
fun formatWarningTime(raw: String, short: Boolean = false): String {
    if (raw.isBlank()) return raw
    return try {
        val inputFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val date = inputFmt.parse(raw.trim()) ?: return raw
        val outputFmt = if (short) {
            SimpleDateFormat("d MMM, h:mm a", Locale.getDefault())
        } else {
            SimpleDateFormat("EEE, d MMM yyyy, h:mm a", Locale.getDefault())
        }
        outputFmt.format(date)
    } catch (_: Exception) {
        raw
    }
}
