package myapp.weatherapp.ui.weather.adapters.daily

import androidx.recyclerview.widget.DiffUtil
import myapp.weatherapp.model.Daily

class DailyDiffUtilCallback : DiffUtil.ItemCallback<Daily>(){
    override fun areItemsTheSame(oldItem: Daily, newItem: Daily): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: Daily, newItem: Daily): Boolean {
        return oldItem  == newItem
    }
}
