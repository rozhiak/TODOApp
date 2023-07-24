package com.rmblack.todoapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.rmblack.todoapp.R
import com.rmblack.todoapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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