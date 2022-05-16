package myapp.weatherapp.ui.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.model.LatLng
import myapp.weatherapp.R
import myapp.weatherapp.databinding.ActivityMainBinding
import myapp.weatherapp.model.CurrentCityResponse
import myapp.weatherapp.model.WeatherResponse
import myapp.weatherapp.ui.weather.adapters.daily.DailyAdapter
import myapp.weatherapp.util.Constants
import myapp.weatherapp.util.IconProvider.Companion.setWeatherIcon
import myapp.weatherapp.util.LocationUtils.Companion.isLocationEnabled
import myapp.weatherapp.util.MyResult
import myapp.weatherapp.util.TextFormat.Companion.formatDescription
import myapp.weatherapp.util.TimeUtils.Companion.unixTime
import myapp.weatherapp.viewmodel.ViewModelProviderFactory

class WeatherActivity : AppCompatActivity() {

    private lateinit var viewModel: WeatherViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var dailyAdapter: DailyAdapter
    private lateinit var ll: MutableList<LatLng>

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        binding.srlContainer.setOnRefreshListener {
            prepareWeatherUpdate()
        }
        showProgress()
        init()

        binding.toolbar.btnSearch.setOnClickListener {
            searchByCity()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        binding.rvDaily.adapter = null
    }

    private fun init() {
        initButtons()
        setUpAdapters()
        setUpViewModel()
        prepareWeatherUpdate()
    }

    private fun initButtons() {
        binding.btRefresh.setOnClickListener{
            prepareWeatherUpdate()
        }
    }

    private fun setUpAdapters() {
        with(binding) {
            rvDaily.setHasFixedSize(true)

            val dailyLinearLayoutManager = LinearLayoutManager(this@WeatherActivity)
            rvDaily.layoutManager = dailyLinearLayoutManager
            dailyAdapter = DailyAdapter()
            rvDaily.adapter = dailyAdapter
        }
    }

    private fun setUpInterface(response: Pair<WeatherResponse, CurrentCityResponse>) {
        val weather = response.first
        val currentWeather = response.first.current

        with(binding) {

            ivCurrentWeather.setWeatherIcon(currentWeather.weather[0].icon)
            tvCurrentWeatherDescription.text =
                formatDescription(currentWeather.weather[0].description)

            tvTemperature.text = (currentWeather.temp.toInt() - 273).toString()
            tvFeelsLike.text = (currentWeather.feels_like.toInt() - 273).toString()

            tvHumidity.text = getString(R.string.percent_of, currentWeather.humidity)
            tvClouds.text = getString(R.string.percent_of, currentWeather.clouds)
            tvWind.text = currentWeather.wind_speed.toString()
            tvPressure.text = getString(R.string.pressure_value, currentWeather.pressure)
            tvSunrise.text = unixTime(currentWeather.sunrise)
            tvSunset.text = unixTime(currentWeather.sunset)

            setUpRecyclerViews(weather)
        }
    }

    private fun setUpSearchInterface(response: WeatherResponse) {
        val currentWeather = response.current

        with(binding) {

            ivCurrentWeather.setWeatherIcon(currentWeather.weather[0].icon)
            tvCurrentWeatherDescription.text =
                formatDescription(currentWeather.weather[0].description)

            tvTemperature.text = (currentWeather.temp.toInt() - 273).toString()
            tvFeelsLike.text = (currentWeather.feels_like.toInt() - 273).toString()

            tvHumidity.text = getString(R.string.percent_of, currentWeather.humidity)
            tvClouds.text = getString(R.string.percent_of, currentWeather.clouds)
            tvWind.text = currentWeather.wind_speed.toString()
            tvPressure.text = getString(R.string.pressure_value, currentWeather.pressure)
            tvSunrise.text = unixTime(currentWeather.sunrise)
            tvSunset.text = unixTime(currentWeather.sunset)

            setUpRecyclerViews(response)
        }
    }

    private fun ActivityMainBinding.setUpRecyclerViews(weather: WeatherResponse) {

        dailyAdapter.submitList(weather.daily)
        rvDaily.adapter = dailyAdapter
    }

    private fun prepareWeatherUpdate() {
        if (ContextCompat.checkSelfPermission(
                baseContext!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val permissionRequest = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            requestPermissions(permissionRequest, Constants.LOCATION_PERMISSION_REQUEST_CODE)
        } else if (!isLocationEnabled(this)) {
            showError(getString(R.string.location_off_error))
        } else {
            viewModel.refreshLocation()
        }
    }

    private fun setUpViewModel() {
        val factory = ViewModelProviderFactory(application)
        viewModel = ViewModelProvider(this, factory).get(WeatherViewModel::class.java)
        getLocationWeather()
    }

    private fun getLocationWeather() {
        viewModel.locationData.observe(this) { location ->
            Log.i("Location Data", location.toString())
            viewModel.getWeather(location)
        }

        viewModel.weatherData.observe(this
        ) { result ->
            when (result) {
                is MyResult.Success -> {
                    setUpInterface(result.data!!)
                    hideProgress()
                }
                is MyResult.Error -> {
                    result.message?.let { message ->
                        showError(message)
                    }
                }
                is MyResult.Loading -> {
                    if (!binding.srlContainer.isRefreshing) {
                        showProgress()
                    }
                }
            }
        }
    }

    private fun fromCityNameToCoord(city: String) {
        val gc = Geocoder(this)
        val address: List<Address> = gc.getFromLocationName(city, 5)

        ll = ArrayList(address.size)

        for (a: Address in address) {
            if (a.hasLatitude() && a.hasLongitude()){
                ll.add(LatLng(a.latitude, a.longitude))
            }
        }
    }

    private fun searchByCity() {
        val city = binding.toolbar.etCitySearch.editableText.toString()
        fromCityNameToCoord(city)
        viewModel.searchCity(ll[0].latitude, ll[0].longitude)
        viewModel.searchData.observe(this
        ) { result ->
            when (result) {
                is MyResult.Success -> {
                    setUpSearchInterface(result.data!!)
                    hideProgress()
                }
                is MyResult.Error -> {
                    result.message?.let { message ->
                        showError(message)
                    }
                }
                is MyResult.Loading -> {
                    if (!binding.srlContainer.isRefreshing) {
                        showProgress()
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            Constants.LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    prepareWeatherUpdate()
                } else {
                    showError(getString(R.string.location_permission_error))
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    private fun showError(message: String) {
        hideProgress()
        hideViews()
        binding.tvMessage.text = message
        binding.tvMessage.visibility = View.VISIBLE
        binding.ivError.visibility = View.VISIBLE
        binding.btRefresh.visibility = View.VISIBLE
    }

    private fun showProgress() {
        hideViews()
        binding.tvMessage.text = getString(R.string.loading_your_forecast)
        binding.pbLoading.visibility = View.VISIBLE
        binding.tvMessage.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        showViews()
        binding.pbLoading.visibility = View.INVISIBLE
        binding.tvMessage.visibility = View.INVISIBLE
        binding.srlContainer.isRefreshing = false
    }

    private fun hideViews() {
        binding.srlContainer.visibility = View.INVISIBLE
        binding.toolbar.container.visibility = View.INVISIBLE
        binding.tvMessage.visibility = View.INVISIBLE
        binding.ivError.visibility = View.INVISIBLE
        binding.btRefresh.visibility = View.INVISIBLE
    }

    private fun showViews() {
        binding.srlContainer.visibility = View.VISIBLE
        binding.toolbar.container.visibility = View.VISIBLE
    }
}