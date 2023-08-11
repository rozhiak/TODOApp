package com.rmblack.todoapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.aminography.primecalendar.persian.PersianCalendar
import com.rmblack.todoapp.R
import com.rmblack.todoapp.databinding.ActivityMainBinding
import com.rmblack.todoapp.models.Task
import java.util.UUID


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.white)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        wireUpBottomNav()
    }

    private fun wireUpBottomNav() {
        binding.bottomNavigationView.background = null
        binding.bottomNavigationView.menu.getItem(1).isEnabled = false
        binding.bottomNavigationView.menu.getItem(1).isCheckable = false
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        val navController: NavController = navHostFragment?.findNavController() ?: return
        binding.bottomNavigationView.setupWithNavController(navController)
    }
}