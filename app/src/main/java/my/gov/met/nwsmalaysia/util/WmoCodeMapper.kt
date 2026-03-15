package my.gov.met.nwsmalaysia.util

data class WmoInfo(
    val descriptionEn: String,
    val descriptionBm: String
)

object WmoCodeMapper {

    private val map = mapOf(
        0  to WmoInfo("Clear Sky",              "Langit Cerah"),
        1  to WmoInfo("Mainly Clear",           "Kebanyakan Cerah"),
        2  to WmoInfo("Partly Cloudy",          "Sebahagian Berawan"),
        3  to WmoInfo("Overcast",               "Mendung"),
        45 to WmoInfo("Fog",                    "Berkabus"),
        48 to WmoInfo("Freezing Fog",           "Kabus Beku"),
        51 to WmoInfo("Light Drizzle",          "Hujan Renyai Ringan"),
        53 to WmoInfo("Drizzle",                "Hujan Renyai"),
        55 to WmoInfo("Dense Drizzle",          "Hujan Renyai Lebat"),
        61 to WmoInfo("Slight Rain",            "Hujan Ringan"),
        63 to WmoInfo("Rain",                   "Hujan"),
        65 to WmoInfo("Heavy Rain",             "Hujan Lebat"),
        71 to WmoInfo("Light Snow",             "Salji Ringan"),
        73 to WmoInfo("Snow",                   "Salji"),
        75 to WmoInfo("Heavy Snow",             "Salji Lebat"),
        77 to WmoInfo("Snow Grains",            "Butiran Salji"),
        80 to WmoInfo("Light Showers",          "Hujan Lebat Seketika"),
        81 to WmoInfo("Showers",                "Hujan Lebat"),
        82 to WmoInfo("Violent Showers",        "Hujan Lebat Sangat"),
        85 to WmoInfo("Snow Showers",           "Hujan Salji"),
        86 to WmoInfo("Heavy Snow Showers",     "Hujan Salji Lebat"),
        95 to WmoInfo("Thunderstorm",           "Ribut Petir"),
        96 to WmoInfo("Thunderstorm + Hail",    "Ribut Petir dan Hujan Batu"),
        99 to WmoInfo("Heavy Thunderstorm",     "Ribut Petir Kuat"),
    )

    fun get(code: Int): WmoInfo = map[code] ?: WmoInfo("Unknown", "Tidak Diketahui")

    fun descriptionEn(code: Int): String = get(code).descriptionEn

    fun isNight(localHour: Int): Boolean = localHour < 6 || localHour >= 19
}
