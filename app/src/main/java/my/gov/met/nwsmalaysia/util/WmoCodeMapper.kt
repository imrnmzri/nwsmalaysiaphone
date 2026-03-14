package my.gov.met.nwsmalaysia.util

data class WmoInfo(
    val descriptionEn: String,
    val descriptionBm: String,
    val iconName: String   // Material icon name or drawable resource name
)

object WmoCodeMapper {

    private val map = mapOf(
        0  to WmoInfo("Clear Sky",              "Langit Cerah",             "wb_sunny"),
        1  to WmoInfo("Mainly Clear",           "Kebanyakan Cerah",         "wb_sunny"),
        2  to WmoInfo("Partly Cloudy",          "Sebahagian Berawan",       "partly_cloudy_day"),
        3  to WmoInfo("Overcast",               "Mendung",                  "cloud"),
        45 to WmoInfo("Fog",                    "Berkabus",                 "foggy"),
        48 to WmoInfo("Freezing Fog",           "Kabus Beku",               "foggy"),
        51 to WmoInfo("Light Drizzle",          "Hujan Renyai Ringan",      "grain"),
        53 to WmoInfo("Drizzle",                "Hujan Renyai",             "grain"),
        55 to WmoInfo("Dense Drizzle",          "Hujan Renyai Lebat",       "grain"),
        61 to WmoInfo("Slight Rain",            "Hujan Ringan",             "water_drop"),
        63 to WmoInfo("Rain",                   "Hujan",                    "water_drop"),
        65 to WmoInfo("Heavy Rain",             "Hujan Lebat",              "water_drop"),
        71 to WmoInfo("Light Snow",             "Salji Ringan",             "ac_unit"),
        73 to WmoInfo("Snow",                   "Salji",                    "ac_unit"),
        75 to WmoInfo("Heavy Snow",             "Salji Lebat",              "ac_unit"),
        77 to WmoInfo("Snow Grains",            "Butiran Salji",            "ac_unit"),
        80 to WmoInfo("Light Showers",          "Hujan Lebat Seketika",     "thunderstorm"),
        81 to WmoInfo("Showers",                "Hujan Lebat",              "thunderstorm"),
        82 to WmoInfo("Violent Showers",        "Hujan Lebat Sangat",       "thunderstorm"),
        85 to WmoInfo("Snow Showers",           "Hujan Salji",              "ac_unit"),
        86 to WmoInfo("Heavy Snow Showers",     "Hujan Salji Lebat",        "ac_unit"),
        95 to WmoInfo("Thunderstorm",           "Ribut Petir",              "bolt"),
        96 to WmoInfo("Thunderstorm + Hail",    "Ribut Petir dan Hujan Batu","bolt"),
        99 to WmoInfo("Heavy Thunderstorm",     "Ribut Petir Kuat",         "bolt"),
    )

    fun get(code: Int): WmoInfo = map[code] ?: WmoInfo("Unknown", "Tidak Diketahui", "help_outline")

    fun descriptionEn(code: Int): String = get(code).descriptionEn

    fun isNight(localHour: Int): Boolean = localHour < 6 || localHour >= 19
}
