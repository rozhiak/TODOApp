package com.rmblack.todoapp.activities

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.rmblack.todoapp.R
import com.rmblack.todoapp.adapters.EventsAdapter
import com.rmblack.todoapp.databinding.ActivityCalendarBinding
import com.rmblack.todoapp.viewmodels.CalendarViewModel
import dagger.hilt.android.AndroidEntryPoint
import ir.mirrajabi.persiancalendar.core.models.CalendarEvent
import ir.mirrajabi.persiancalendar.core.models.PersianDate
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
        setEventsOnCalView()
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.selectedDay != null) binding.calendarView.goToDate(viewModel.selectedDay)
    }

    private fun setEventsOnCalView() {
        val calHandler = binding.calendarView.calendar
        calHandler.setHighlightOfficialEvents(false)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tasks.collect { tasks ->
                    for (t in tasks) {
                        calHandler.addLocalEvent(
                            CalendarEvent(
                                PersianDate(
                                    t.deadLine.year,
                                    t.deadLine.month + 1, // Persian calendar months start from 0
                                    t.deadLine.dayOfMonth
                                ),
                                t.title,
                                false
                            )
                        )
                        binding.calendarView.update()
                    }
                }
            }
        }
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
        val eventAdapter = EventsAdapter(this)
        binding.rvEvents.apply {
            layoutManager = LinearLayoutManager(this@CalendarActivity)
            adapter = eventAdapter
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { events ->
                    events?.let {
                        if (it.isNotEmpty()) {
                            eventAdapter.setData(it)
                            binding.tvHint.visibility = View.GONE
                            binding.ivHint.visibility = View.GONE
                        } else {
                            eventAdapter.setData(emptyList())
                            binding.tvHint.visibility = View.VISIBLE
                            binding.ivHint.visibility = View.VISIBLE
                            binding.ivHint.setImageResource(R.drawable.ic_no_task)
                            binding.tvHint.text = getString(R.string.no_task)
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
                viewModel.setSelectedDay(it)
            }

            calendarView.setOnDayLongClickedListener {
                viewModel.setEvents(it)
                viewModel.setSelectedDay(it)
            }

            cvToday.setOnClickListener {
                binding.calendarView.goToToday()
                val today = binding.calendarView.calendar.today
                viewModel.setEvents(today)
                viewModel.setSelectedDay(today)
            }

            ivBackBtn.setOnClickListener {
                finish()
            }
        }
    }

}