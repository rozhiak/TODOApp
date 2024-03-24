package com.rmblack.todoapp.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.rmblack.todoapp.databinding.ActivityCalendarBinding
import com.rmblack.todoapp.viewmodels.CalendarViewModel

class CalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarBinding

    private val viewModel: CalendarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}