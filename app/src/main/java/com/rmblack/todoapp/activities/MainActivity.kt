package com.rmblack.todoapp.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aminography.primecalendar.persian.PersianCalendar
import com.github.leandroborgesferreira.loadingbutton.customViews.CircularProgressButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
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
import com.rmblack.todoapp.viewmodels.SAME_USER_NAME
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import java.util.UUID


const val newlyAddedTaskServerID = "newly added"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferencesManager = SharedPreferencesManager(this)
        viewModel = ViewModelProvider(this, MainViewModelFactory(sharedPreferencesManager))[MainViewModel::class.java]
        viewModel.removeNoTitleTasks()

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
                val (popupView, popupWindow) = createWindow()

                setUserName(popupView)

                changeUserName(popupView)


                popupWindow.elevation = 60.0f
                popupWindow.showAsDropDown(binding.ivProfile, -470, 25, 0)

                popupWindow.setOnDismissListener {
                    binding.ivProfile.setImageResource(R.drawable.ic_person)
                }
            }
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun changeUserName(popupView: View) {
        val saveBTN = popupView.findViewById<CircularProgressButton>(R.id.save_btn)
        saveBTN.setOnClickListener { saveBtnView ->
            hideKeyboard(saveBtnView)
            val nameEt = popupView.findViewById<TextInputEditText>(R.id.name_et)
            if (nameEt.text.toString() != "") {
                saveBTN.startAnimation {
                    lifecycleScope.launch {
                        val res = viewModel.updateUser(
                            nameEt.text.toString()
                        )
                        res.onSuccess {
                            viewModel.getUserFromSharedPreferences()?.let { token ->
                                val result = Utilities.syncTasksWithServer(
                                    token.token,
                                    this@MainActivity
                                )
                                result.onSuccess {
                                    saveBTN.revertAnimation()
                                    Utilities.makeWarningSnack(
                                        this@MainActivity,
                                        binding.root,
                                        "تغییرات با موفقیت لحاظ شد."
                                    )
                                }
                                result.onFailure { e ->
                                    saveBTN.revertAnimation()
                                    when(e) {
                                        is UnknownHostException -> {
                                            Utilities.makeWarningSnack(
                                                this@MainActivity,
                                                binding.root,
                                                "به دلیل عدم اتصال به اینترنت ، هم رسانی تسک ها صورت نگرفت."
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        res.onFailure {
                            saveBTN.revertAnimation()
                            when (it) {
                                is MainViewModel.UpdateUserException -> {
                                    if (it.intValue == 404) {
                                        Utilities.makeWarningSnack(
                                            this@MainActivity,
                                            binding.root,
                                            "مشکلی در فرآیند ورودتان به برنامه پیش آمده"
                                        )
                                    } else if (it.intValue == SAME_USER_NAME) {
                                        Utilities.makeWarningSnack(
                                            this@MainActivity,
                                            binding.root,
                                            "نام جدید با نام فعلی نمی تواند یکسان باشد."
                                        )
                                    }
                                }

                                is UnknownHostException -> {
                                    Utilities.makeWarningSnack(
                                        this@MainActivity,
                                        binding.root,
                                        "مشکل در اتصال به اینترنت ، لطفا از اتصال خود مطمئن شوید."
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Utilities.makeWarningSnack(
                    this@MainActivity,
                    binding.root,
                    "نام کاربری نمی تواند خالی باشد."
                )
            }
        }
    }

    private fun createWindow(): Pair<View, PopupWindow> {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.profile_pop_up, binding.root, false)

        if (popupView.isVisible) {
            binding.ivProfile.setImageResource(R.drawable.ic_person_selected)
        }

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        return Pair(popupView, popupWindow)
    }

    private fun setUserName(popupView: View) {
        val nameEt = popupView.findViewById<TextInputEditText>(R.id.name_et)
        nameEt.setText(viewModel.getUserFromSharedPreferences()?.name ?: "")
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
                composer = viewModel.getUserFromSharedPreferences()?.name ?: "",
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