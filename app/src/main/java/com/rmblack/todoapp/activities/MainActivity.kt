package com.rmblack.todoapp.activities

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aminography.primecalendar.persian.PersianCalendar
import com.google.android.material.snackbar.Snackbar
import com.rmblack.todoapp.R
import com.rmblack.todoapp.databinding.ActivityMainBinding
import com.rmblack.todoapp.fragments.EditTaskBottomSheet
import com.rmblack.todoapp.fragments.PrivateTasksFragment
import com.rmblack.todoapp.fragments.SharedTasksFragment
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.utils.PersianNum
import com.rmblack.todoapp.utils.SharedPreferencesManager
import com.rmblack.todoapp.utils.Utilities
import com.rmblack.todoapp.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import java.util.UUID

const val newlyAddedTaskServerID = "newly added"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferencesManager = SharedPreferencesManager(this)
        viewModel = ViewModelProvider(this, MainViewModelFactory(sharedPreferencesManager))[MainViewModel::class.java]

        checkLoginState()

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.syncTasksWithServer(this)
        setUpUI()
        wireUpBottomNav()
        showToday()
        setUpProfileBtn()
    }

    private fun setUpProfileBtn() {
        val userToken = viewModel.getUserFromSharedPreferences()?.token
        if (userToken == null) {
            binding.ivProfile.setImageResource(R.drawable.ic_login)
            binding.ivProfile.setOnClickListener {
                goToStarterActivity()
            }
        } else {
            binding.ivProfile.setImageResource(R.drawable.ic_person)
            binding.ivProfile.setOnClickListener {
                //TODO show profile content
            }
        }
    }

    private fun goToStarterActivity() {
        val intent = Intent(this, StarterActivity::class.java)
        startActivity(intent)
    }

    private fun checkLoginState() {
        val enterWithoutLogin = viewModel.getEntranceState()
        if (!enterWithoutLogin) {
            val intent = Intent(this, StarterActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
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
        if (today.dayOfWeek == 3) {
            binding.dayOfWeek.text = "سه شنبه"
        } else {
            binding.dayOfWeek.text = today.weekDayName
        }
        binding.monthOfYear.text = today.monthName
    }

    private fun wireUpBottomNav() {
        binding.bottomNavigationView.background = null
        binding.bottomNavigationView.menu.getItem(1).isEnabled = false
        binding.bottomNavigationView.menu.getItem(1).isCheckable = false

        val firstFragment: Fragment = PrivateTasksFragment()
        val secondFragment: Fragment = SharedTasksFragment()
        val fm = supportFragmentManager

        fm.beginTransaction().add(R.id.main_fragment_container, secondFragment, "2").hide(secondFragment).commit()
        fm.beginTransaction().add(R.id.main_fragment_container, firstFragment, "1").commit()

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
            if(binding.bottomNavigationView.selectedItemId == R.id.sharedTasksFragment &&
                viewModel.getUserFromSharedPreferences()?.token == null) {
                Utilities.makeWarningSnack(
                    this,
                    binding.root,
                    "برای استفاده از بخش اشتراکی باید ابتدا وارد حساب کاربری خود شوید."
                )
            } else {
                showNewTask()
            }
        }
    }

    private fun showNewTask() {
        lifecycleScope.launch {

            val newTask = Task(
                serverID = newlyAddedTaskServerID,
                title = "",
                id = UUID.randomUUID(),
                description = "",
                addedTime = PersianCalendar(),
                deadLine = PersianCalendar(),
                isUrgent = false,
                isDone = false,
                isShared = binding.bottomNavigationView.selectedItemId == R.id.sharedTasksFragment,
                composer = "user",
                groupId = "123",
            )

            val editTaskBottomSheet = EditTaskBottomSheet()
            val args = Bundle()
            args.putString("taskId", newTask.id.toString())
            args.putBoolean("isNewTask", true)
            editTaskBottomSheet.arguments = args
            editTaskBottomSheet.show(supportFragmentManager, "TODO tag")

            viewModel.addTask(newTask)
        }
    }

    class MoveUpwardBehavior(context: Context?, attrs: AttributeSet?) :
        CoordinatorLayout.Behavior<View>(context, attrs) {

        override fun layoutDependsOn(
            parent: CoordinatorLayout,
            child: View,
            dependency: View
        ): Boolean {
            return dependency is Snackbar.SnackbarLayout
        }

        override fun onDependentViewChanged(
            parent: CoordinatorLayout,
            child: View,
            dependency: View
        ): Boolean {
            val translationY = minOf(0f, dependency.translationY - dependency.height)
            if (dependency.translationY != 0f)
                child.translationY = translationY
            return true
        }
    }

    class MainViewModelFactory(private val sharedPreferencesManager: SharedPreferencesManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(sharedPreferencesManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}