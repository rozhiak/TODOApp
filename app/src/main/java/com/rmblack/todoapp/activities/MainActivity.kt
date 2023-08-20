package com.rmblack.todoapp.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.aminography.primecalendar.persian.PersianCalendar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.rmblack.todoapp.R
import com.rmblack.todoapp.databinding.ActivityMainBinding
import com.rmblack.todoapp.fragments.EditTaskBottomSheet
import com.rmblack.todoapp.fragments.PrivateTasksFragment
import com.rmblack.todoapp.fragments.SharedTasksFragment
import com.rmblack.todoapp.models.Task
import com.rmblack.todoapp.models.User
import com.rmblack.todoapp.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import java.util.UUID


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

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

        val firstFragment: Fragment = PrivateTasksFragment()
        val secondFragment: Fragment = SharedTasksFragment()
        val fm = supportFragmentManager

        fm.beginTransaction().add(R.id.fragment_container, secondFragment, "2").hide(secondFragment).commit()
        fm.beginTransaction().add(R.id.fragment_container, firstFragment, "1").commit()

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.privateTasksFragment -> {
                    fm.beginTransaction().hide(secondFragment).show(firstFragment).commit()
                }
                R.id.sharedTasksFragment -> {
                    fm.beginTransaction().hide(firstFragment).show(secondFragment).commit()
                }
            }
            true
        }

        binding.fab.setOnClickListener {
            showNewTask()
        }
    }

    private fun showNewTask() {
        lifecycleScope.launch {
            //TODO() This should be deleted
            val user = User(
                "rozhiak",
                "099393139575",
                "123",
                "456"
            )

            val newTask = if (binding.bottomNavigationView.selectedItemId == R.id.privateTasksFragment) {
                Task(
                    title = "",
                    id = UUID.randomUUID(),
                    description = "",
                    addedTime = PersianCalendar(),
                    deadLine = PersianCalendar(),
                    isUrgent = false,
                    isDone = false,
                    isShared = false,
                    user = user,
                    groupId = "123"
                )
            } else {
                Task(
                    title = "",
                    id = UUID.randomUUID(),
                    description = "",
                    addedTime = PersianCalendar(),
                    deadLine = PersianCalendar(),
                    isUrgent = false,
                    isDone = false,
                    isShared = true,
                    user = user,
                    groupId = "123"
                )
            }

            viewModel.addTask(newTask)

            val editTaskBottomSheet = EditTaskBottomSheet()
            val args = Bundle()
            args.putString("taskId", newTask.id.toString())
            editTaskBottomSheet.arguments = args
            editTaskBottomSheet.show(supportFragmentManager, "TODO tag")
        }
    }
}