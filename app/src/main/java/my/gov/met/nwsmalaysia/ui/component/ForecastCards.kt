package my.gov.met.nwsmalaysia.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import my.gov.met.nwsmalaysia.domain.model.DailyForecast
import my.gov.met.nwsmalaysia.domain.model.HourlyForecast
import my.gov.met.nwsmalaysia.util.WmoCodeMapper
import kotlin.math.roundToInt

@Composable
fun HourlyForecastRow(
    forecasts: List<HourlyForecast>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            Text(
                text = "Hourly Forecast",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(forecasts) { h ->
                    HourlyItem(h)
                }
            }
        }
    }
}

@Composable
private fun HourlyItem(forecast: HourlyForecast) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val timeLabel = forecast.time.substringAfter("T").take(5)
        Text(text = timeLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        WmoIcon(code = forecast.weatherCode, modifier = Modifier.size(24.dp))
        Text(text = "${forecast.temperature.roundToInt()}°", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        if (forecast.precipProbability > 0) {
            Text(text = "${forecast.precipProbability}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontSize = 10.sp)
        }
    }
}

@Composable
fun DailyForecastCard(
    forecasts: List<DailyForecast>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "7-Day Forecast",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(8.dp))
            forecasts.forEach { day ->
                DailyRow(day)
                if (day != forecasts.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyRow(forecast: DailyForecast) {
    val dayLabel = try {
        val parts = forecast.date.split("-")
        val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        "${months[parts[1].toInt() - 1]} ${parts[2]}"
    } catch (e: Exception) {
        forecast.date
    }
    val wmoInfo = WmoCodeMapper.get(forecast.weatherCode)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dayLabel,
            modifier = Modifier.width(60.dp),
            style = MaterialTheme.typography.bodyMedium
        )
        WmoIcon(code = forecast.weatherCode, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = wmoInfo.descriptionEn,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = "${forecast.minTemp.roundToInt()}° / ${forecast.maxTemp.roundToInt()}°",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
