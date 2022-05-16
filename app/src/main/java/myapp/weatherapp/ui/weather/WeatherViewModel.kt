package myapp.weatherapp.ui.weather

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import myapp.weatherapp.R
import myapp.weatherapp.model.Coord
import myapp.weatherapp.model.CurrentCityResponse
import myapp.weatherapp.model.WeatherResponse
import myapp.weatherapp.repository.WeatherRepository
import myapp.weatherapp.source.ApiConstants.APP_ID
import myapp.weatherapp.source.ApiConstants.EXCLUDE
import myapp.weatherapp.util.LocationLiveData
import myapp.weatherapp.util.MyResult
import myapp.weatherapp.util.NetworkUtils
import retrofit2.Response

class WeatherViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val weatherRepository: WeatherRepository = WeatherRepository(application)

    val locationData = LocationLiveData(application)
    val weatherData: MutableLiveData<MyResult<Pair<WeatherResponse, CurrentCityResponse>>> =
        MutableLiveData()

    val searchData: MutableLiveData<MyResult<WeatherResponse>> = MutableLiveData()

    fun refreshLocation() = locationData.locationUpdate()

    fun getWeather(coord: Coord) = viewModelScope.launch {
        fetchWeather(
            coord.lat,
            coord.lon,
            APP_ID,
            EXCLUDE
        )
    }

    fun searchCity(lat: Double, lon: Double) = viewModelScope.launch {
        searchFetchWeather(
            lat,
            lon,
            APP_ID
        )
    }

    private suspend fun fetchWeather(
        lat: Double,
        lon: Double,
        appid: String,
        exclude: String
    ) {
        weatherData.postValue(MyResult.Loading())
        try {
            if (NetworkUtils.isNetworkAvailable(getApplication<Application>())) {
                val weatherResponse = weatherRepository.getWeather(lat, lon, appid, exclude)
                val cityResponse = weatherRepository.getCurrentCity(lat, lon, appid)
                weatherData.postValue(handlePairResponse(weatherResponse, cityResponse))
            } else {
                weatherData.postValue(MyResult.Error(getApplication<Application>().getString(R.string.network_error)))
            }
        } catch (t: Throwable) {
            weatherData.postValue(MyResult.Error(t.message.toString()))
        }
    }

    private suspend fun searchFetchWeather(
        lat: Double,
        lon: Double,
        appid: String,
    ) {
        searchData.postValue(MyResult.Loading())
        try {
            if (NetworkUtils.isNetworkAvailable(getApplication<Application>())) {
                val searchResponse = weatherRepository.searchCity(lat, lon, appid)
                searchData.postValue(handleSearchResponse(searchResponse))
            } else {
                searchData.postValue(MyResult.Error(getApplication<Application>().getString(R.string.network_error)))
            }
        } catch (t: Throwable) {
            searchData.postValue(MyResult.Error(t.message.toString()))
        }
    }

    private fun handlePairResponse(
        weather: Response<WeatherResponse>,
        city: Response<CurrentCityResponse>
    ): MyResult<Pair<WeatherResponse, CurrentCityResponse>> {
        if (weather.isSuccessful && city.isSuccessful) {
            if (weather.body() != null && city.body() != null) {
                return MyResult.Success(Pair(weather.body()!!, city.body()!!))
            }
        }
        return MyResult.Error(weather.message())
    }

    private fun handleSearchResponse(
        search: Response<WeatherResponse>
    ): MyResult<WeatherResponse> {
        if (search.isSuccessful){
            if (search.body() != null) {
                return MyResult.Success(search.body()!!)
            }
        }
        return MyResult.Error(search.message())
    }
}

