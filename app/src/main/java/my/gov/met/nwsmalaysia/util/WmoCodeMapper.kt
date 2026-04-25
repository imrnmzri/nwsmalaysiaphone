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

    fun iconUnicode(code: Int, isNight: Boolean): Char {
        val d = !isNight
        return when (code) {
            0 -> if (d) '\uf00d' else '\uf02e'   // sun / night-clear
            1 -> if (d) '\uf00c' else '\uf083'   // sun-overcast / night-partly-cloudy
            2 -> if (d) '\uf002' else '\uf086'   // day-cloudy / night-alt-cloudy
            3 -> '\uf041'                          // cloud (overcast)
            45, 48 -> if (d) '\uf003' else '\uf04a' // day-fog / night-fog
            51, 53, 55 -> if (d) '\uf00b' else '\uf02b' // day-sprinkle / night-alt-sprinkle
            61, 63, 65 -> if (d) '\uf008' else '\uf028' // day-rain / night-alt-rain
            71, 73, 75, 77 -> if (d) '\uf00a' else '\uf02a' // day-snow / night-alt-snow
            80, 81, 82 -> if (d) '\uf009' else '\uf029' // day-showers / night-alt-showers
            85, 86 -> if (d) '\uf00a' else '\uf02a'  // day-snow / night-alt-snow
            95, 96, 99 -> if (d) '\uf010' else '\uf02d' // day-thunderstorm / night-alt-thunderstorm
            else -> if (d) '\uf00d' else '\uf02e'   // fallback: sun / night-clear
        }
    }
}
