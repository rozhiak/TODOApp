package com.rmblack.todoapp.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.rmblack.todoapp.adapters.EventsAdapter
import com.rmblack.todoapp.databinding.ActivityCalendarBinding
import com.rmblack.todoapp.viewmodels.CalendarViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarBinding

    private val viewModel: CalendarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setClickListeners()
        syncEventsRecyclerViewWithData()
        syncMonthWithCalendar()
    }

    private fun syncMonthWithCalendar() {
        setMonthText(binding.calendarView.calendar.today.month)
        binding.calendarView.setOnMonthChangedListener {
            setMonthText(it.month)
        }
    }

    private fun setMonthText(monthNum: Int) {
        when (monthNum) {
            1 -> {
                binding.tvMonth.text = "فــروردیــن"
            }
            2 -> {
                binding.tvMonth.text = "اردیـبهـشــت"
            }
            3 -> {
                binding.tvMonth.text = "خــرداد"
            }
            4 -> {
                binding.tvMonth.text = "تــیــر"
            }
            5 -> {
                binding.tvMonth.text = "مــرداد"
            }
            6 -> {
                binding.tvMonth.text = "شــهــریور"
            }
            7 -> {
                binding.tvMonth.text = "مــهــر"
            }
            8 -> {
                binding.tvMonth.text = "آبــان"
            }
            9 -> {
                binding.tvMonth.text = "آذر"
            }
            10 -> {
                binding.tvMonth.text = "دی"
            }
            11 -> {
                binding.tvMonth.text = "بــهــمــن"
            }
            12 -> {
                binding.tvMonth.text = "اســفــنــد"
            }
        }
    }

    private fun syncEventsRecyclerViewWithData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { events ->
                    events?.let {
                        if (it.isNotEmpty()) {
                            binding.rvEvents.apply {
                                layoutManager = LinearLayoutManager(this@CalendarActivity)
                                adapter = EventsAdapter(it)
                            }
                        } else {
                            binding.rvEvents.adapter = null
                        }
                    }
                }
            }
        }
    }

    private fun setClickListeners() {
        binding.apply {
            calendarView.setOnDayClickedListener {
                viewModel.setEvents(it)
            }

            calendarView.setOnDayLongClickedListener {
                viewModel.setEvents(it)
            }

            cvToday.setOnClickListener {
                binding.calendarView.goToToday()
                viewModel.setEvents(binding.calendarView.calendar.today)
            }

            ivBackBtn.setOnClickListener {
                finish()
            }
        }
    }

}