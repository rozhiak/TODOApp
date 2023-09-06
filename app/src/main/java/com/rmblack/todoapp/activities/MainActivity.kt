package com.rmblack.todoapp.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aminography.primecalendar.persian.PersianCalendar
import com.rmblack.todoapp.R
import com.rmblack.todoapp.databinding.ActivityMainBinding
import com.rmblack.todoapp.fragments.EditTaskBottomSheet
import com.rmblack.todoapp.fragments.PrivateTasksFragment
import com.rmblack.todoapp.fragments.SharedTasksFragment
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.models.local.TaskState
import com.rmblack.todoapp.models.local.User
import com.rmblack.todoapp.utils.PersianNum
import com.rmblack.todoapp.viewmodels.MainViewModel
import com.rmblack.todoapp.webservice.repository.ApiRepository
import kotlinx.coroutines.launch
import java.util.UUID


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkLoginState()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpUI()
        wireUpBottomNav()
        showToday()
    }

    private fun checkLoginState() {
        val loggedIn = true
        if (!loggedIn) {
            val intent = Intent(this, StarterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setUpUI() {
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> window.navigationBarColor =
                ContextCompat.getColor(this, R.color.dark_bottom_navigation)

            Configuration.UI_MODE_NIGHT_NO -> window.navigationBarColor =
                ContextCompat.getColor(this, R.color.white)

            Configuration.UI_MODE_NIGHT_UNDEFINED -> window.navigationBarColor =
                ContextCompat.getColor(this, R.color.white)
        }
    }

    private fun showToday() {
        val today = PersianCalendar()
        binding.dayOfMonth.text = PersianNum.convert(today.dayOfMonth.toString())
        binding.dayOfWeek.text = today.weekDayName
        binding.monthOfYear.text = today.monthName
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
                    groupId = "123",
                    TaskState.NEW
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
                    groupId = "123",
                    TaskState.NEW
                )
            }

            val editTaskBottomSheet = EditTaskBottomSheet()
            val args = Bundle()
            args.putString("taskId", newTask.id.toString())
            args.putBoolean("isNewTask", true)
            editTaskBottomSheet.arguments = args
            editTaskBottomSheet.show(supportFragmentManager, "TODO tag")

            viewModel.addTask(newTask)
        }
    }
}