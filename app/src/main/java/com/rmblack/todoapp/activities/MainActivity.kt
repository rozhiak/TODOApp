package com.rmblack.todoapp.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.aminography.primecalendar.persian.PersianCalendar
import com.rmblack.todoapp.R
import com.rmblack.todoapp.databinding.ActivityMainBinding
import com.rmblack.todoapp.fragments.EditTaskBottomSheet
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
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        val navController: NavController = navHostFragment?.findNavController() ?: return
        binding.bottomNavigationView.setupWithNavController(navController)

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
            //

            val newTask = Task(
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

            viewModel.addTask(newTask)

            val editTaskBottomSheet = EditTaskBottomSheet()
            val args = Bundle()
            args.putSerializable("taskId", newTask.id)
            editTaskBottomSheet.arguments = args
            editTaskBottomSheet.show(supportFragmentManager, "TODO tag")
        }
    }
}