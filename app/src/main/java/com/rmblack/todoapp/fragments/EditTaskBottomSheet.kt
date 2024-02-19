package com.rmblack.todoapp.fragments

import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Selection
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.aminography.primecalendar.persian.PersianCalendar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.rmblack.todoapp.R
import com.rmblack.todoapp.databinding.FragmentEditTaskBottomSheetBinding
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.utils.PersianNum
import com.rmblack.todoapp.utils.SharedPreferencesManager
import com.rmblack.todoapp.viewmodels.EditTaskViewModel
import com.rmblack.todoapp.viewmodels.EditTaskViewModelFactory
import ir.hamsaa.persiandatepicker.PersianDatePickerDialog
import ir.hamsaa.persiandatepicker.api.PersianPickerDate
import ir.hamsaa.persiandatepicker.api.PersianPickerListener
import ir.hamsaa.persiandatepicker.date.PersianDateImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

private const val PRIMARY_TASK_KEY = "PRIMARY_TASK_KEY"

class EditTaskBottomSheet : BottomSheetDialogFragment() {

    private lateinit var context: Context

    private val viewModel: EditTaskViewModel by viewModels {
        val taskId = arguments?.getString("taskId")
        EditTaskViewModelFactory(UUID.fromString(taskId))
    }

    private var _binding: FragmentEditTaskBottomSheetBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    override fun getTheme(): Int = R.style.Theme_NoWiredStrapInNavigationBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditTaskBottomSheetBinding.inflate(inflater, container, false)
        context = requireContext()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        resetIfNeeded(savedInstanceState)
        super.onViewCreated(view, savedInstanceState)
        setClickListeners()
        updateUi()
        syncUserInput()
        val isNew = arguments?.getBoolean("isNewTask")
        if (isNew == true) {
            focusOnTitle()
        }
    }

    // Rollback if activity destroyed.
    private fun resetIfNeeded(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            viewModel.doNotSave = true
            val taskJson = savedInstanceState.getString(PRIMARY_TASK_KEY)
            val primaryTask = Gson().fromJson(taskJson, Task::class.java)
            val isNew = arguments?.getBoolean("isNewTask")
            if (isNew == true) {
                viewModel.deleteTaskFromRoom(primaryTask)
            } else {
                viewModel.updateTaskInRoom(primaryTask)
            }
            dismiss()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val taskJson = Gson().toJson(viewModel.primaryTask)
        outState.putString(PRIMARY_TASK_KEY, taskJson)
    }

    private fun focusOnTitle() {
        binding.etTitle.requestFocus()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            binding.etTitle.windowInsetsController?.show(WindowInsetsCompat.Type.ime())
        } else {
            val windowInsetsController =
                activity?.window?.let { WindowCompat.getInsetsController(it, requireView()) }
            windowInsetsController?.show(WindowInsetsCompat.Type.ime())
        }
    }

    private fun syncUserInput() {
        binding.apply {
            segmentedBtn.setOnPositionChangedListener { pos ->
                val sharedPropertiesManager = SharedPreferencesManager(context)
                val user = sharedPropertiesManager.getUser()

                if (user?.token != null) {
                    viewModel.updateTask { oldTask ->
                        oldTask.copy(
                            isShared = pos == 0,
                            title = binding.etTitle.text.toString(),
                            description = binding.etDescription.text.toString(),
                            composer = if (pos == 0 && oldTask.composer == "") user.name else oldTask.composer
                        )
                    }
                } else {
                    binding.segmentedBtn.visibility = View.GONE
                }
            }

            urgentSwitch.setOnCheckedChangeListener { _, b ->
                viewModel.updateTask { oldTask ->
                    oldTask.copy(
                        isUrgent = b,
                        title = binding.etTitle.text.toString(),
                        description = binding.etDescription.text.toString(),
                    )
                }
            }

            deadlineTv.setOnClickListener {
                viewModel.saveTitleAndDescription(
                    binding.etTitle.text.toString(), binding.etDescription.text.toString()
                )
                showDatePicker()
            }

            calendarIc.setOnClickListener {
                viewModel.saveTitleAndDescription(
                    binding.etTitle.text.toString(), binding.etDescription.text.toString()
                )
                showDatePicker()
            }

            urgentLable.setOnClickListener {
                urgentSwitch.toggle()
            }

            clockTv.setOnClickListener { showClockPicker() }

            clockIc.setOnClickListener { showClockPicker() }
        }
    }

    private fun showClockPicker() {
        viewModel.saveTitleAndDescription(
            binding.etTitle.text.toString(), binding.etDescription.text.toString()
        )

        val cal = viewModel.task.value?.deadLine

        cal?.let {
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                viewModel.updateTask { oldTask ->
                    val newDeadLine = PersianCalendar()
                    newDeadLine.set(
                        oldTask.deadLine.year,
                        oldTask.deadLine.month,
                        oldTask.deadLine.dayOfMonth,
                        hour,
                        minute
                    )
                    oldTask.copy(
                        deadLine = newDeadLine
                    )
                }
            }

            TimePickerDialog(
                requireContext(), timeSetListener, cal.hourOfDay, cal.minute, true
            ).show()
        }
    }

    private fun resetCursorsPosition() {
        val etTitle: Editable? = binding.etTitle.text
        Selection.setSelection(etTitle, binding.etTitle.text?.length ?: 0)

        val etDes: Editable? = binding.etDescription.text
        Selection.setSelection(etDes, binding.etDescription.text?.length ?: 0)
    }

    private fun showDatePicker() {
        val today = PersianCalendar()
        val deadline = viewModel.task.value?.deadLine
        val year = deadline?.year ?: today.year
        var month = deadline?.month ?: today.month
        month++
        val day = deadline?.dayOfMonth ?: today.dayOfMonth
        val persianPickerDate = PersianDateImpl()
        persianPickerDate.setDate(year, month, day)

        val picker = makeDatePicker(persianPickerDate)
        picker.show()
    }

    private fun makeDatePicker(taskDeadline: PersianDateImpl): PersianDatePickerDialog {
        val picker = PersianDatePickerDialog(context).setPositiveButtonString("تایید")
            .setNegativeButton("لغو").setTodayButton("برو به امروز").setTodayButtonVisible(true)
            .setInitDate(taskDeadline, true)
            .setActionTextColor(ResourcesCompat.getColor(resources, R.color.green, null))
            .setTitleType(PersianDatePickerDialog.WEEKDAY_DAY_MONTH_YEAR)
            .setTitleColor(ResourcesCompat.getColor(resources, R.color.title_black, null))
            .setBackgroundColor(
                ResourcesCompat.getColor(
                    resources, R.color.bottom_sheet_back_color, null
                )
            ).setPickerBackgroundColor(
                ResourcesCompat.getColor(
                    resources, R.color.bottom_sheet_back_color, null
                )
            ).setAllButtonsTextSize(16).setListener(object : PersianPickerListener {
                override fun onDateSelected(persianPickerDate: PersianPickerDate) {
                    viewModel.updateTask { oldTask ->
                        val newDeadline = PersianCalendar()

                        newDeadline.set(
                            persianPickerDate.persianYear,
                            persianPickerDate.persianMonth - 1,
                            persianPickerDate.persianDay,
                            oldTask.deadLine.hourOfDay,
                            oldTask.deadLine.minute
                        )

                        oldTask.copy(
                            deadLine = newDeadline,
                        )

                    }
                }

                override fun onDismissed() {}
            })
        return picker
    }

    private fun updateUi() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.task.collect { task ->
                task?.let { notNullTask ->
                    binding.apply {
                        urgentSwitch.isChecked = notNullTask.isUrgent
                        etTitle.setText(notNullTask.title)
                        etDescription.setText(notNullTask.description)
                        resetCursorsPosition()
                        deadlineTv.text = notNullTask.deadLine.longDateString
                        if (notNullTask.isShared) {
                            if (segmentedBtn.position != 0) segmentedBtn.setPosition(0, false)
                        } else {
                            if (segmentedBtn.position != 1) segmentedBtn.setPosition(1, false)
                        }
                        setClock(notNullTask)
                    }
                }
            }
        }
    }

    private fun FragmentEditTaskBottomSheetBinding.setClock(notNullTask: Task) {
        var hourStr = PersianNum.convert(notNullTask.deadLine.hourOfDay.toString())
        if (hourStr.length == 1) {
            hourStr = "۰$hourStr"
        }
        var minuteStr = PersianNum.convert(notNullTask.deadLine.minute.toString())
        if (minuteStr.length == 1) {
            minuteStr = "۰$minuteStr"
        }
        val formattedText = String.format(getString(R.string.clock_format), hourStr, minuteStr)
        clockTv.text = formattedText
    }

    private fun setClickListeners() {
        binding.saveBtn.setOnClickListener {
            if (binding.etTitle.text?.isBlank() == true || binding.etTitle.text?.isEmpty() == true) {
                binding.etTitle.setHintTextColor(
                    ContextCompat.getColor(
                        context, R.color.urgent_red
                    )
                )
                binding.etTitle.hint = "عنوان را تایپ کنید"

                binding.etTitle.requestFocus()
                val inputMethodManager =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(binding.etTitle, InputMethodManager.SHOW_IMPLICIT)

                viewLifecycleOwner.lifecycleScope.launch {
                    delay(1300)
                    binding.etTitle.hint = "عنوان"
                    binding.etTitle.setHintTextColor(
                        ContextCompat.getColor(
                            context, R.color.hint_text_color
                        )
                    )
                }
            } else {
                dismiss()
            }
        }

        binding.collapseBtn.setOnClickListener {
            dismiss()
        }
    }

    override fun onPause() {
        viewModel.saveTitleAndDescription(
            binding.etTitle.text.toString(), binding.etDescription.text.toString()
        )
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (binding.etTitle.text?.isNotBlank() == true || binding.etTitle.text?.isNotEmpty() == true) {
            setFragmentResult(
                REQUEST_KEY_ID_PRIVATE, bundleOf(BUNDLE_KEY_ID_PRIVATE to viewModel.task.value?.id)
            )
            setFragmentResult(
                REQUEST_KEY_ID_SHARED, bundleOf(BUNDLE_KEY_ID_SHARED to viewModel.task.value?.id)
            )

            val isNew = arguments?.getBoolean("isNewTask")
            setFragmentResult(
                REQUEST_KEY_IS_NEW_PRIVATE, bundleOf(BUNDLE_KEY_IS_NEW_PRIVATE to isNew)
            )
            setFragmentResult(
                REQUEST_KEY_IS_NEW_SHARED, bundleOf(BUNDLE_KEY_IS_NEW_SHARED to isNew)
            )
        }
    }

    companion object {
        const val REQUEST_KEY_ID_PRIVATE = "REQUEST_KEY_ID_PRIVATE"

        const val BUNDLE_KEY_ID_PRIVATE = "BUNDLE_KEY_ID_PRIVATE"

        const val REQUEST_KEY_ID_SHARED = "REQUEST_KEY_ID_SHARED"

        const val BUNDLE_KEY_ID_SHARED = "BUNDLE_KEY_ID_PRIVATE_SHARED"

        const val REQUEST_KEY_IS_NEW_PRIVATE = "REQUEST_KEY_IS_NEW_PRIVATE"

        const val REQUEST_KEY_IS_NEW_SHARED = "REQUEST_KEY_IS_NEW_SHARED"

        const val BUNDLE_KEY_IS_NEW_PRIVATE = "BUNDLE_KEY_IS_NEW_PRIVATE"

        const val BUNDLE_KEY_IS_NEW_SHARED = "BUNDLE_KEY_IS_NEW_SHARED"
    }

}