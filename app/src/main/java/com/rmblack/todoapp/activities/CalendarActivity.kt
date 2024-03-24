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

                        }
                    }
                }
            }
        }
    }

    private fun setClickListeners() {
        binding.apply {
            calendarView.setOnDayClickedListener {
                viewModel.setSelectedDate(it)
            }

            calendarView.setOnDayLongClickedListener {
                viewModel.setSelectedDate(it)
            }
        }
    }

}