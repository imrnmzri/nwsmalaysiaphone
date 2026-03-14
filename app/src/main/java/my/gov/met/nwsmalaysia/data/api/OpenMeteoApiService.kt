package my.gov.met.nwsmalaysia.data.api

import my.gov.met.nwsmalaysia.data.model.OpenMeteoResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoApiService {
    @GET("v1/forecast")
    suspend fun getCurrent(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String,
        @Query("hourly") hourly: String,
        @Query("daily") daily: String,
        @Query("timezone") timezone: String = "Asia/Kuala_Lumpur",
        @Query("forecast_days") days: Int = 7
    ): OpenMeteoResponse
}
