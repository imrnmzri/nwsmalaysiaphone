package my.gov.met.nwsmalaysia.domain.model

data class Warning(
    val fingerprint: String,
    val issued: String,
    val validFrom: String,
    val validTo: String,
    val titleEn: String,
    val headingEn: String,
    val textEn: String,
    val state: String?,
    val district: String?
)

data class Forecast(
    val date: String,
    val locationId: String,
    val morningCode: Int,
    val afternoonCode: Int,
    val nightCode: Int,
    val summaryCode: Int,
    val minTemp: Int,
    val maxTemp: Int
)

data class CurrentConditions(
    val temperature: Double,
    val apparentTemperature: Double,
    val humidity: Int,
    val weatherCode: Int,
    val windSpeed: Double,
    val windDirection: Int,
    val precipitation: Double
)

data class HourlyForecast(
    val time: String,
    val temperature: Double,
    val weatherCode: Int,
    val precipProbability: Int
)

data class DailyForecast(
    val date: String,
    val weatherCode: Int,
    val maxTemp: Double,
    val minTemp: Double
)

data class Location(
    val locationId: String,
    val name: String,
    val type: String,
    val state: String,
    val latitude: Double?,
    val longitude: Double?
)

data class WeatherState(
    val location: Location?,
    val currentConditions: CurrentConditions?,
    val hourlyForecasts: List<HourlyForecast>,
    val dailyForecasts: List<DailyForecast>,
    val warnings: List<Warning>,
    val signifikanText: String?,
    val signifikanLoading: Boolean = true,
    val isLoading: Boolean,
    val error: String?
)
