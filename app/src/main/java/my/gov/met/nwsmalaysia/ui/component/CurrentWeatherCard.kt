package my.gov.met.nwsmalaysia.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import my.gov.met.nwsmalaysia.domain.model.CurrentConditions
import my.gov.met.nwsmalaysia.util.WmoCodeMapper
import kotlin.math.roundToInt

@Composable
fun CurrentWeatherCard(
    conditions: CurrentConditions,
    locationName: String,
    modifier: Modifier = Modifier
) {
    val wmoInfo = WmoCodeMapper.get(conditions.weatherCode)
    val windDirLabel = windDirCompass(conditions.windDirection)

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = locationName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val currentHour = remember { java.time.LocalTime.now().hour }
                WmoIcon(
                    code = conditions.weatherCode,
                    isNight = WmoCodeMapper.isNight(currentHour),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${conditions.temperature.roundToInt()}°C",
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = wmoInfo.descriptionEn,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Feels like ${conditions.apparentTemperature.roundToInt()}°C",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherDetail(
                    icon = Icons.Default.Water,
                    label = "Humidity",
                    value = "${conditions.humidity}%"
                )
                WeatherDetail(
                    icon = Icons.Default.Air,
                    label = "Wind",
                    value = "${conditions.windSpeed.roundToInt()} km/h $windDirLabel"
                )
                WeatherDetail(
                    icon = Icons.Default.Grain,
                    label = "Rain",
                    value = "${conditions.precipitation} mm"
                )
            }
        }
    }
}

@Composable
private fun WeatherDetail(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun WmoIcon(
    code: Int,
    isNight: Boolean = false,
    modifier: Modifier = Modifier
) {
    val (icon, tint) = when (code) {
        0, 1 -> if (isNight)
            Icons.Default.Bedtime       to Color(0xFF7986CB)
        else
            Icons.Default.WbSunny       to Color(0xFFF9A825)

        2 -> if (isNight)
            Icons.Default.NightsStay    to Color(0xFF7986CB)
        else
            Icons.Default.WbCloudy      to Color(0xFFFFB300)

        3 ->
            Icons.Default.Cloud         to Color(0xFF90A4AE)

        45, 48 ->
            Icons.Default.Foggy         to Color(0xFFB0BEC5)

        51, 53, 55,
        61, 63, 65 ->
            Icons.Default.WaterDrop     to Color(0xFF1E88E5)

        71, 73, 75, 77, 85, 86 ->
            Icons.Default.AcUnit        to Color(0xFF90CAF9)

        80, 81, 82 ->
            Icons.Default.Water         to Color(0xFF0288D1)

        95, 96, 99 ->
            Icons.Default.Thunderstorm  to Color(0xFFFDD835)

        else -> if (isNight)
            Icons.Default.Bedtime       to Color(0xFF7986CB)
        else
            Icons.Default.WbSunny       to Color(0xFFF9A825)
    }
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = modifier
    )
}

private fun windDirCompass(deg: Int): String {
    val dirs = listOf("N","NNE","NE","ENE","E","ESE","SE","SSE","S","SSW","SW","WSW","W","WNW","NW","NNW")
    return dirs[(deg / 22.5).roundToInt() % 16]
}
