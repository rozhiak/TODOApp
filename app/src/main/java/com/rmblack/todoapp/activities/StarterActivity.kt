package com.rmblack.todoapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rmblack.todoapp.databinding.ActivityStarterBinding

class StarterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStarterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStarterBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}