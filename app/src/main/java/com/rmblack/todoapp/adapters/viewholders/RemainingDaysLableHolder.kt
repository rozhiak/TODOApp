package com.rmblack.todoapp.adapters.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.aminography.primecalendar.persian.PersianCalendar
import com.rmblack.todoapp.databinding.PrivateTasksRvItemBinding
import com.rmblack.todoapp.databinding.RemainingDaysLableBinding
import com.rmblack.todoapp.models.Task
import com.rmblack.todoapp.utils.PersianNum
import com.rmblack.todoapp.viewmodels.PrivateTasksViewModel
import java.lang.Math.ceil

class RemainingDaysLableHolder(
    private val binding: RemainingDaysLableBinding,
): RecyclerView.ViewHolder(binding.root) {
    fun bind(deadline: PersianCalendar) {
        binding.apply {
            val disInDays = kotlin.math.floor(calculateDateDistance(deadline)).toInt()
            if (disInDays == 0) {
                remainingDaysLable.text = ""
                remainingDaysTv.text = "امروز"
            } else if(disInDays == 1) {
                remainingDaysLable.text = ""
                remainingDaysTv.text = "فردا"
            } else if (disInDays == -1) {
                remainingDaysLable.text = ""
                remainingDaysTv.text = "دیروز"
            } else if (disInDays > 1) {
                remainingDaysTv.text = PersianNum.convert(disInDays.toString())
                remainingDaysLable.text = "روز باقی مانده"
            } else {
                remainingDaysTv.text = PersianNum.convert((-1 * disInDays).toString())
                remainingDaysLable.text = "روز گذشته"
            }
        }
    }

    private fun calculateDateDistance(calendarDate: PersianCalendar) : Double {
        val currentDate = PersianCalendar()
        val targetDate = PersianCalendar()

        targetDate.timeInMillis = calendarDate.timeInMillis

        currentDate.hourOfDay = 0
        targetDate.hourOfDay = 0
        currentDate.minute = 0
        targetDate.minute = 0
        currentDate.second = 0
        targetDate.second = 0
        currentDate.millisecond = 0
        targetDate.millisecond = 0

        val dateInMillis = calendarDate.timeInMillis
        val currentDateInMillis = currentDate.timeInMillis

        val millisInDay = 1000.0 * 60.0 * 60.0 * 24.0
        val distanceInMillis = dateInMillis - currentDateInMillis

        return distanceInMillis / millisInDay
    }

}