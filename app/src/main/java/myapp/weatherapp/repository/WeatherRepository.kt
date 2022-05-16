package myapp.weatherapp.repository

import android.app.Application
import myapp.weatherapp.source.RetrofitInstance
import myapp.weatherapp.source.OpenWeatherService

class WeatherRepository(
    private val application: Application
) {
    private val client: OpenWeatherService = RetrofitInstance.weatherApi

    suspend fun getWeather(
        lat: Double,
        lon: Double,
        appid: String,
        exclude: String
    ) = client.getWeather(lat, lon, appid, exclude)

    suspend fun getCurrentCity(
        lat: Double,
        lon: Double,
        appid: String,
    ) = client.getCurrentLocation(lat, lon, appid)

    suspend fun searchCity(
        lat: Double,
        lon: Double,
        appid: String,
    ) = client.searchCity(lat, lon, appid)

}