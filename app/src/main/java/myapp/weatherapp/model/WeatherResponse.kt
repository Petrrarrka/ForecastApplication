package myapp.weatherapp.model


data class WeatherResponse(
    val lat: Double,
    val lon: Double,
    val timezone: String,
    val timezone_offset: String,

    val current: Current,
    val daily: List<Daily>
)
