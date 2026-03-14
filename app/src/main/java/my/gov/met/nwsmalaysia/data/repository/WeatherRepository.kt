package my.gov.met.nwsmalaysia.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import my.gov.met.nwsmalaysia.data.api.OpenMeteoApiService
import my.gov.met.nwsmalaysia.data.api.WeatherApiService
import my.gov.met.nwsmalaysia.data.db.CachedForecastEntity
import my.gov.met.nwsmalaysia.data.db.ForecastDao
import my.gov.met.nwsmalaysia.data.model.ForecastResponse
import my.gov.met.nwsmalaysia.data.model.OpenMeteoResponse
import my.gov.met.nwsmalaysia.domain.model.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

private const val CACHE_TTL_MS = 30 * 60 * 1000L  // 30 minutes

@Singleton
class WeatherRepository @Inject constructor(
    private val weatherApiService: WeatherApiService,
    private val openMeteoApiService: OpenMeteoApiService,
    private val forecastDao: ForecastDao,
    private val gson: Gson
) {

    suspend fun getForecast(locationId: String): List<Forecast> {
        val cached = forecastDao.getForLocation(locationId)
        val now = System.currentTimeMillis()
        if (cached != null && now - cached.fetchedAt < CACHE_TTL_MS) {
            return parseForecastJson(cached.json)
        }
        return try {
            val response = weatherApiService.getForecast(locationId)
            val json = gson.toJson(response)
            forecastDao.insert(CachedForecastEntity(locationId, json, now))
            response.map { it.toDomain() }
        } catch (e: Exception) {
            cached?.let { parseForecastJson(it.json) } ?: emptyList()
        }
    }

    suspend fun getCurrentConditions(lat: Double, lon: Double): CurrentConditionsData? {
        return try {
            val response = openMeteoApiService.getCurrent(
                lat = lat,
                lon = lon,
                current = "temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,wind_direction_10m,precipitation,apparent_temperature",
                hourly = "temperature_2m,weather_code,precipitation_probability",
                daily = "weather_code,temperature_2m_max,temperature_2m_min"
            )
            response.toCurrentConditionsData()
        } catch (e: Exception) {
            null
        }
    }

    private fun parseForecastJson(json: String): List<Forecast> {
        return try {
            val type = object : TypeToken<List<ForecastResponse>>() {}.type
            val list: List<ForecastResponse> = gson.fromJson(json, type)
            list.map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun ForecastResponse.toDomain() = Forecast(
        date = date,
        locationId = location.locationId,
        morningCode = morningForecast,
        afternoonCode = afternoonForecast,
        nightCode = nightForecast,
        summaryCode = summaryForecast,
        minTemp = minTemp,
        maxTemp = maxTemp
    )

    private fun OpenMeteoResponse.toCurrentConditionsData(): CurrentConditionsData? {
        val c = current ?: return null
        val hourlyData = buildHourlyList()
        val dailyData = buildDailyList()
        return CurrentConditionsData(
            current = CurrentConditions(
                temperature = c.temperature ?: 0.0,
                apparentTemperature = c.apparentTemperature ?: 0.0,
                humidity = c.relativeHumidity ?: 0,
                weatherCode = c.weatherCode ?: 0,
                windSpeed = c.windSpeed ?: 0.0,
                windDirection = c.windDirection ?: 0,
                precipitation = c.precipitation ?: 0.0
            ),
            hourly = hourlyData,
            daily = dailyData
        )
    }

    private fun OpenMeteoResponse.buildHourlyList(): List<HourlyForecast> {
        val h = hourly ?: return emptyList()
        val times = h.time ?: return emptyList()
        return times.indices.take(24).mapNotNull { i ->
            val time = times.getOrNull(i) ?: return@mapNotNull null
            HourlyForecast(
                time = time,
                temperature = h.temperature?.getOrNull(i) ?: return@mapNotNull null,
                weatherCode = h.weatherCode?.getOrNull(i) ?: 0,
                precipProbability = h.precipitationProbability?.getOrNull(i) ?: 0
            )
        }
    }

    private fun OpenMeteoResponse.buildDailyList(): List<DailyForecast> {
        val d = daily ?: return emptyList()
        val dates = d.time ?: return emptyList()
        return dates.indices.mapNotNull { i ->
            val date = dates.getOrNull(i) ?: return@mapNotNull null
            DailyForecast(
                date = date,
                weatherCode = d.weatherCode?.getOrNull(i) ?: 0,
                maxTemp = d.temperatureMax?.getOrNull(i) ?: 0.0,
                minTemp = d.temperatureMin?.getOrNull(i) ?: 0.0
            )
        }
    }
}

data class CurrentConditionsData(
    val current: CurrentConditions,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>
)
