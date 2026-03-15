package my.gov.met.nwsmalaysia.util

import android.content.Context
import my.gov.met.nwsmalaysia.data.db.LocationEntity

/**
 * Hardcoded coordinates for locations that are commonly selected.
 * Keys are the exact location IDs from locations.csv.
 */
private val KNOWN_COORDS: Map<String, Pair<Double, Double>> = mapOf(
    // WP / Selangor towns
    "Tn079" to Pair(3.1390, 101.6869),   // Kuala Lumpur
    "Tn076" to Pair(3.1073, 101.6070),   // Petaling Jaya
    "Tn070" to Pair(3.0733, 101.5185),   // Shah Alam
    "Tn066" to Pair(3.0400, 101.4537),   // Pelabuhan Klang
    "Tn077" to Pair(3.0565, 101.5858),   // Subang Jaya
    "Tn081" to Pair(3.1483, 101.7057),   // Ampang
    "Tn064" to Pair(3.3208, 101.5730),   // Rawang
    "Tn097" to Pair(2.7297, 101.7018),   // Sepang
    "Tn067" to Pair(3.2131, 101.6419),   // Kepong
    "Tn087" to Pair(2.9213, 101.6559),   // Cyberjaya
    "Tn088" to Pair(2.9264, 101.6964),   // Putrajaya
    "Tn080" to Pair(3.1429, 101.7130),   // Bukit Bintang
    "Tn071" to Pair(3.1726, 101.6896),   // Sentul
    "Tn083" to Pair(3.0838, 101.7047),   // Sungai Besi
    "Tn086" to Pair(3.1094, 101.7457),   // Cheras
    // Districts
    "Ds058" to Pair(3.1390, 101.6869),   // Kuala Lumpur district
    "Ds054" to Pair(3.0450, 101.4489),   // Klang district
    "Ds057" to Pair(3.1073, 101.6070),   // Petaling district
    "Ds062" to Pair(2.9264, 101.6964),   // Putrajaya district
    "Ds064" to Pair(2.7297, 101.7018),   // Sepang district
    // States (capital city coords)
    "St001" to Pair(6.4414, 100.1982),   // Perlis (Kangar)
    "St002" to Pair(6.1184, 100.3685),   // Kedah (Alor Setar)
    "St003" to Pair(5.4145, 100.3288),   // Pulau Pinang (George Town)
    "St004" to Pair(4.5975, 101.0901),   // Perak (Ipoh)
    "St005" to Pair(6.1254, 102.2381),   // Kelantan (Kota Bharu)
    "St006" to Pair(5.3117, 103.1324),   // Terengganu (Kuala Terengganu)
    "St007" to Pair(3.8077, 103.3260),   // Pahang (Kuantan)
    "St008" to Pair(3.0738, 101.5183),   // Selangor (Shah Alam)
    "St009" to Pair(3.1390, 101.6869),   // WP Kuala Lumpur
    "St010" to Pair(2.9264, 101.6964),   // WP Putrajaya
    "St011" to Pair(2.7297, 101.9381),   // Negeri Sembilan (Seremban)
    "St012" to Pair(2.1896, 102.2501),   // Melaka
    "St013" to Pair(1.4854, 103.7618),   // Johor (JB)
    "St501" to Pair(1.5533, 110.3592),   // Sarawak (Kuching)
    "St502" to Pair(5.9804, 116.0735),   // Sabah (Kota Kinabalu)
    "St503" to Pair(5.2831, 115.2308),   // WP Labuan
    // A few popular recreational/tourist spots
    "Rc014" to Pair(3.7167, 101.7333),   // Bukit Fraser
    "Rc017" to Pair(3.3500, 101.8167),   // Bukit Tinggi
    "Rc024" to Pair(2.8052, 104.1336),   // Pulau Tioman
    "Rc009" to Pair(5.7477, 103.0020),   // Pulau Redang
)

object CsvParser {

    fun parseLocations(context: Context): List<LocationEntity> {
        val result = mutableListOf<LocationEntity>()
        context.assets.open("locations.csv").bufferedReader().useLines { lines ->
            lines.forEachIndexed { index, line ->
                if (index == 0) return@forEachIndexed // skip header
                val parts = line.split(",")
                if (parts.size < 4) return@forEachIndexed
                val type = parts[0].trim()
                // Skip State and District entries — only show Towns, Recreational, etc.
                if (type == "State" || type == "District") return@forEachIndexed
                val id = parts[1].trim()
                val name = parts[2].trim()
                val state = parts[3].trim()

                // Try to read lat/lon from CSV first (columns 4 & 5)
                var lat: Double? = null
                var lon: Double? = null
                if (parts.size >= 6) {
                    try {
                        val latStr = parts[4].trim()
                        val lonStr = parts[5].trim()
                        if (latStr.isNotEmpty() && lonStr.isNotEmpty()) {
                            lat = latStr.toDoubleOrNull()
                            lon = lonStr.toDoubleOrNull()
                        }
                    } catch (e: Exception) {
                        // ignore parsing errors
                    }
                }

                // Fall back to KNOWN_COORDS if CSV coordinates are missing
                if (lat == null || lon == null) {
                    val coords = KNOWN_COORDS[id]
                    lat = coords?.first
                    lon = coords?.second
                }

                result.add(
                    LocationEntity(
                        locationId = id,
                        name = name,
                        type = type,
                        state = state,
                        latitude = lat,
                        longitude = lon
                    )
                )
            }
        }
        return result
    }
}
