package my.gov.met.nwsmalaysia.data.model

import com.google.gson.annotations.SerializedName

data class ForecastResponse(
    val date: String,
    val location: LocationRef,
    @SerializedName("morning_forecast") val morningForecast: String = "",
    @SerializedName("afternoon_forecast") val afternoonForecast: String = "",
    @SerializedName("night_forecast") val nightForecast: String = "",
    @SerializedName("summary_forecast") val summaryForecast: String = "",
    @SerializedName("summary_when") val summaryWhen: String? = null,
    @SerializedName("min_temp") val minTemp: Int = 0,
    @SerializedName("max_temp") val maxTemp: Int = 0
)

data class LocationRef(
    @SerializedName("location_id") val locationId: String,
    val name: String? = null
)
