package my.gov.met.nwsmalaysia.data.model

import com.google.gson.annotations.SerializedName

data class ForecastResponse(
    val date: String,
    val location: LocationRef,
    @SerializedName("morning_forecast") val morningForecast: Int,
    @SerializedName("afternoon_forecast") val afternoonForecast: Int,
    @SerializedName("night_forecast") val nightForecast: Int,
    @SerializedName("summary_forecast") val summaryForecast: Int,
    @SerializedName("min_temp") val minTemp: Int,
    @SerializedName("max_temp") val maxTemp: Int
)

data class LocationRef(
    @SerializedName("location_id") val locationId: String,
    val name: String? = null
)
