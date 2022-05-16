package myapp.weatherapp.ui.weather.adapters.daily

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import myapp.weatherapp.R
import myapp.weatherapp.databinding.ItemDailyForecastBinding
import myapp.weatherapp.model.Daily
import myapp.weatherapp.util.DateUtils.Companion.unixDay
import myapp.weatherapp.util.IconProvider.Companion.setExpandIcon
import myapp.weatherapp.util.IconProvider.Companion.setWeatherIcon
import myapp.weatherapp.util.TextFormat.Companion.formatDescription
import myapp.weatherapp.util.TextViewUtils.Companion.setPoP
import myapp.weatherapp.util.TimeUtils

class DailyAdapter : ListAdapter<Daily, DailyAdapter.DailyViewHolder>(DailyDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyViewHolder {
        val binding = ItemDailyForecastBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DailyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DailyViewHolder, position: Int) {
        getItem(position).let { daily ->
            holder.bind(daily)
        }
    }

    class DailyViewHolder(private val binding: ItemDailyForecastBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val context = binding.root.context

        fun bind(daily: Daily) {
            with(binding) {
                tvMaxTemp.text = (daily.temp.max.toInt() - 273).toString()
                tvMinTemp.text = (daily.temp.min.toInt() - 273).toString()

                tvDate.text = unixDay(daily.dt)
                ivDailyWeather.setWeatherIcon(daily.weather[0].icon)
                tvProbabilityOfPrecipitation.setPoP(daily.pop)

                ivWeatherExpand.setWeatherIcon(daily.weather[0].icon)
                tvWind.text = daily.wind_speed.toString()
                tvPressure.text = context.getString(
                    R.string.pressure_value,
                    daily.pressure
                )

                tvSunrise.text = TimeUtils.unixTime(daily.sunrise)
                tvSunset.text = TimeUtils.unixTime(daily.sunset)

                tvDescription.text = formatDescription(daily.weather[0].description)
                itemDaily.setOnClickListener {
                    binding.changeExpandableVisibility()
                }
            }
        }

        private fun ItemDailyForecastBinding.changeExpandableVisibility() {
            with(this) {
                when (this.llExpandable.visibility) {
                    View.GONE, View.INVISIBLE -> {
                        this.llExpandable.visibility = View.VISIBLE
                        this.ivExpand.setExpandIcon(true)
                    }
                    View.VISIBLE -> {
                        this.llExpandable.visibility = View.GONE
                        this.ivExpand.setExpandIcon(false)
                    }
                }
            }
        }
    }
}