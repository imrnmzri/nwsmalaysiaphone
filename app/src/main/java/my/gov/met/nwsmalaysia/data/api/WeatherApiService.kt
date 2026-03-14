package my.gov.met.nwsmalaysia.data.api

import my.gov.met.nwsmalaysia.data.model.ForecastResponse
import my.gov.met.nwsmalaysia.data.model.WarningResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("weather/forecast/")
    suspend fun getForecast(
        @Query("location_id") locationId: String
    ): List<ForecastResponse>

    @GET("weather/warning/")
    suspend fun getWarnings(): List<WarningResponse>
}
