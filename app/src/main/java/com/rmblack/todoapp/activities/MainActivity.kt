package com.rmblack.todoapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rmblack.todoapp.databinding.ActivityMainBinding
import com.rmblack.todoapp.fragments.MySampleFabFragment


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigationView.background = null
        binding.bottomNavigationView.menu.getItem(1).isEnabled = false
        binding.bottomNavigationView.menu.getItem(1).isCheckable = false

        binding.fab.setOnClickListener {
            val dialogFrag: MySampleFabFragment = MySampleFabFragment.newInstance()
            dialogFrag.setParentFab(binding.fab)
            dialogFrag.show(supportFragmentManager, dialogFrag.tag)
        }
    }
}