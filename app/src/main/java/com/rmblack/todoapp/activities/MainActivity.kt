package com.rmblack.todoapp.activities

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aminography.primecalendar.persian.PersianCalendar
import com.github.leandroborgesferreira.loadingbutton.customViews.CircularProgressButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.rmblack.todoapp.R
import com.rmblack.todoapp.alarm.AlarmScheduler
import com.rmblack.todoapp.alarm.AlarmSchedulerImpl
import com.rmblack.todoapp.databinding.ActivityMainBinding
import com.rmblack.todoapp.fragments.CalendarBottomSheetFragment
import com.rmblack.todoapp.fragments.EditTaskBottomSheet
import com.rmblack.todoapp.fragments.FilterSettingBottomSheet
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.utils.PersianNum
import com.rmblack.todoapp.utils.Utilities
import com.rmblack.todoapp.utils.Utilities.SharedObject.setSyncingState
import com.rmblack.todoapp.viewmodels.MainViewModel
import com.rmblack.todoapp.viewmodels.SAME_USER_NAME_CODE
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import java.util.UUID

const val newlyAddedTaskServerID = "newly added"

private const val FRAGMENT_ID_KEY = "FRAGMENT_ID_KEY"

private const val NR_CODE = 99

private const val VR_CODE = 98

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(this.application, alarmScheduler)
    }

    private val alarmScheduler: AlarmScheduler by lazy {
        AlarmSchedulerImpl(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel.removeNoTitleTasks()
        checkLoginState()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.syncTasks()
        setNavigationBarColor()
        setFragmentContainer(savedInstanceState)
        showToday()
        setUpProfileBtn()
        setUpFilterBtn()
        requestNotificationPermission()
        requestVibrateRequest()
        wireUpCalendar()
    }

    private fun wireUpCalendar() {
        binding.dayOfMonth.setOnClickListener {
            showCalendar()
        }
        binding.dayOfWeek.setOnClickListener {
            showCalendar()
        }
        binding.monthOfYear.setOnClickListener {
            showCalendar()
        }
    }

    private fun showCalendar() {
        val calendarBS = CalendarBottomSheetFragment()
        supportFragmentManager.let {
            calendarBS.show(it, CalendarBottomSheetFragment.TAG)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(FRAGMENT_ID_KEY, binding.bottomNavigationView.selectedItemId)
    }

    private fun requestVibrateRequest() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.VIBRATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.VIBRATE),
                VR_CODE
            )
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), NR_CODE)
        }
    }

    private fun setUpFilterBtn() {
        binding.filterBtn.setOnClickListener {
            val filterSettingBottomSheet = FilterSettingBottomSheet()
            filterSettingBottomSheet.show(this.supportFragmentManager, "filter setting")
        }
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
                saveNewName(popupView, popupWindow)

                popupWindow.elevation = 60.0f
                popupWindow.showAsDropDown(binding.appBarLayout, 450, 25, 0)

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

    private fun saveNewName(popupView: View, popupWindow: PopupWindow) {
        val saveBTN = popupView.findViewById<CircularProgressButton>(R.id.save_btn)
        saveBTN.setOnClickListener { saveBtnView ->
            hideKeyboard(saveBtnView)
            val nameEt = popupView.findViewById<TextInputEditText>(R.id.name_et)
            if (nameEt.text.toString() != "") {
                saveBTN.startAnimation {
                    performChangeNameRequest(nameEt, saveBTN, popupWindow)
                }
            } else {
                makeSnack("نام کاربری نمی تواند خالی باشد.")
            }
        }
    }

    private fun performChangeNameRequest(
        nameEt: TextInputEditText, saveBTN: CircularProgressButton, popupWindow: PopupWindow
    ) {
        lifecycleScope.launch {
            val res = viewModel.updateUserInServer(
                nameEt.text.toString()
            )
            res.onSuccess {
                syncTasksWithNewName(saveBTN)
                popupWindow.dismiss()
            }
            res.onFailure {
                saveBTN.revertAnimation()
                when (it) {
                    is MainViewModel.UpdateUserException -> {
                        if (it.intValue == 404) {
                            makeSnack("مشکلی در فرآیند ورودتان به برنامه پیش آمده")
                        } else if (it.intValue == SAME_USER_NAME_CODE) {
                            makeSnack("نام جدید با نام فعلی نمی تواند یکسان باشد.")
                        }
                    }

                    is UnknownHostException -> {
                        makeSnack("مشکل در اتصال به اینترنت ، لطفا از اتصال خود مطمئن شوید.")
                    }
                }
            }
        }
    }

    private suspend fun syncTasksWithNewName(saveBTN: CircularProgressButton) {
        viewModel.getUserFromSharedPreferences()?.let { user ->
            setSyncingState(true)
            val result = Utilities.syncTasksWithServer(
                user.token, viewModel.sharedPreferencesManager, alarmScheduler
            )
            result.onSuccess {
                setSyncingState(false)
                saveBTN.revertAnimation()
                makeSnack("تغییرات با موفقیت لحاظ شد.")
            }
            result.onFailure { e ->
                setSyncingState(false)
                saveBTN.revertAnimation()
                when (e) {
                    is UnknownHostException -> {
                        makeSnack("به دلیل عدم اتصال به اینترنت ، هم رسانی تسک ها صورت نگرفت.")
                    }
                }
            }
        }
    }

    private fun makeSnack(
        message: String
    ) {
        Utilities.makeWarningSnack(
            this, binding.root, message
        )
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

    private fun setNavigationBarColor() {
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

    private fun setFragmentContainer(savedInstanceState: Bundle?) {
        binding.bottomNavigationView.background = null
        binding.bottomNavigationView.menu.getItem(1).isEnabled = false
        binding.bottomNavigationView.menu.getItem(1).isCheckable = false

        val privateTasksFragment: Fragment = viewModel.privateTasksFragment
        val sharedTasksFragment: Fragment = viewModel.sharedTasksFragment
        val fm = supportFragmentManager

        setVisibleFragment(savedInstanceState, fm, sharedTasksFragment, privateTasksFragment)

        wireUpBottomNav(fm, sharedTasksFragment, privateTasksFragment)
    }

    private fun wireUpBottomNav(
        fm: FragmentManager, sharedTasksFragment: Fragment, privateTasksFragment: Fragment
    ) {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.privateTasksFragment -> {
                    fm.beginTransaction().hide(sharedTasksFragment).show(privateTasksFragment)
                        .commit()
                }

                R.id.sharedTasksFragment -> {
                    fm.beginTransaction().hide(privateTasksFragment).show(sharedTasksFragment)
                        .commit()
                }
            }
            true
        }

        binding.fab.setOnClickListener {
            if (binding.bottomNavigationView.selectedItemId == R.id.sharedTasksFragment && viewModel.getUserFromSharedPreferences()?.token == null) {
                makeSnack("برای استفاده از بخش اشتراکی باید ابتدا وارد حساب کاربری خود شوید.")
            } else {
                showNewTask()
            }
        }
    }

    private fun setVisibleFragment(
        savedInstanceState: Bundle?,
        fm: FragmentManager,
        sharedTasksFragment: Fragment,
        privateTasksFragment: Fragment
    ) {
        if (savedInstanceState != null) {
            fm.beginTransaction().replace(R.id.main_fragment_container, Fragment())
                .commit() // Clear the fm

            val lastVisibleFragID = savedInstanceState.getInt(FRAGMENT_ID_KEY)
            if (lastVisibleFragID == R.id.privateTasksFragment) {
                fm.beginTransaction().add(R.id.main_fragment_container, sharedTasksFragment)
                    .hide(sharedTasksFragment).commit()
                fm.beginTransaction().add(R.id.main_fragment_container, privateTasksFragment)
                    .commit()
            } else {
                fm.beginTransaction().add(R.id.main_fragment_container, privateTasksFragment)
                    .hide(privateTasksFragment).commit()
                fm.beginTransaction().add(R.id.main_fragment_container, sharedTasksFragment)
                    .commit()
            }
        } else {
            fm.beginTransaction().add(R.id.main_fragment_container, sharedTasksFragment)
                .hide(sharedTasksFragment).commit()
            fm.beginTransaction().add(R.id.main_fragment_container, privateTasksFragment).commit()
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
                groupId = "",
                detailsVisibility = false
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
            parent: CoordinatorLayout, child: View, dependency: View
        ): Boolean {
            return dependency is Snackbar.SnackbarLayout
        }

        override fun onDependentViewChanged(
            parent: CoordinatorLayout, child: View, dependency: View
        ): Boolean {
            val translationY = minOf(0f, dependency.translationY - dependency.height)
            if (dependency.translationY != 0f) child.translationY = translationY
            return true
        }
    }

    class MainViewModelFactory(
        private val application: Application,
        private val alarmScheduler: AlarmScheduler
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(application, alarmScheduler) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}