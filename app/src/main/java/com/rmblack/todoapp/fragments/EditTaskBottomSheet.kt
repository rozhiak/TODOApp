package com.rmblack.todoapp.fragments

import android.R.attr.typeface
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aminography.primecalendar.persian.PersianCalendar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rmblack.todoapp.R
import com.rmblack.todoapp.databinding.FragmentEditTaskBottomSheetBinding
import com.rmblack.todoapp.viewmodels.EditTaskViewModel
import com.rmblack.todoapp.viewmodels.EditTaskViewModelFactory
import ir.hamsaa.persiandatepicker.PersianDatePickerDialog
import ir.hamsaa.persiandatepicker.api.PersianPickerDate
import ir.hamsaa.persiandatepicker.api.PersianPickerListener
import ir.hamsaa.persiandatepicker.util.PersianCalendarUtils
import kotlinx.coroutines.launch
import java.util.UUID


class EditTaskBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: EditTaskViewModel by viewModels {
        val taskId = arguments?.getString("taskId")
        EditTaskViewModelFactory(UUID.fromString(taskId))
    }

    private var _binding: FragmentEditTaskBottomSheetBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    override fun getTheme(): Int  = R.style.Theme_NoWiredStrapInNavigationBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditTaskBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setCollapseBtnListener()
        updateUi()
        syncUserInput()
        setSaveBtnListener()
    }

    private fun syncUserInput() {
        binding.apply {
//            etTitle.doOnTextChanged { text, _, _, _ ->
//                viewModel.updateTask { oldTask ->
//                    oldTask.copy(title = text.toString())
//                }
//            }

            urgentSwitch.setOnCheckedChangeListener { _, b ->
                viewModel.updateTask { oldTask ->
                    oldTask.copy(isUrgent = b)
                }
            }

//            etDescription.doOnTextChanged { text, _, _, _ ->
//                viewModel.updateTask { oldTask ->
//                    oldTask.copy(description = text.toString())
//                }
//            }

            segmentedBtn.setOnPositionChangedListener {pos ->
                if (pos == 1) {
                    viewModel.updateTask {oldTask ->
                        oldTask.copy(isShared = false)
                    }
                } else if (pos == 0) {
                    viewModel.updateTask {oldTask ->
                        oldTask.copy(isShared = true)
                    }
                }
            }

            deadlineTv.setOnClickListener {

                val today = PersianCalendar()

                val deadline = viewModel.task.value?.deadLine
                val year = deadline?.year ?: today.year
                val month = deadline?.month ?: today.month
                val day = deadline?.dayOfMonth ?: today.dayOfMonth



                val picker = PersianDatePickerDialog(requireContext())
                    .setPositiveButtonString("باشه")
                    .setNegativeButton("بیخیال")
                    .setTodayButton("امروز")
                    .setTodayButtonVisible(true)
                    .setMinYear(1400)
                    .setInitDate(year, month, day)
                    .setActionTextColor(Color.parseColor("#5DD0A3"))
                    .setTypeFace(Typeface.DEFAULT_BOLD)
                    .setTitleType(PersianDatePickerDialog.WEEKDAY_DAY_MONTH_YEAR)
                    .setListener(object : PersianPickerListener {
                        override fun onDateSelected(persianPickerDate: PersianPickerDate) {
                            Log.d(
                                "TAG",
                                "onDateSelected: " + persianPickerDate.timestamp
                            ) //675930448000
                            Log.d(
                                "TAG",
                                "onDateSelected: " + persianPickerDate.gregorianDate
                            ) //Mon Jun 03 10:57:28 GMT+04:30 1991
                            Log.d(
                                "TAG",
                                "onDateSelected: " + persianPickerDate.persianLongDate
                            ) // دوشنبه  13  خرداد  1370
                            Log.d(
                                "TAG",
                                "onDateSelected: " + persianPickerDate.persianMonthName
                            ) //خرداد
                            Log.d(
                                "TAG",
                                "onDateSelected: " + PersianCalendarUtils.isPersianLeapYear(
                                    persianPickerDate.persianYear
                                )
                            ) //true
                            Toast.makeText(
                                context,
                                persianPickerDate.persianYear.toString() + "/" + persianPickerDate.persianMonth + "/" + persianPickerDate.persianDay,
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onDismissed() {}
                    })

                picker.show()



            }
        }
    }

    private fun updateUi() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.task.collect { task ->
                    task?.let {notNullTask ->
                        binding.apply {
                            urgentSwitch.isChecked = notNullTask.isUrgent
                            etTitle.setText(notNullTask.title)
                            deadlineTv.text = notNullTask.deadLine.longDateString
                            etDescription.setText(notNullTask.description)
                            if (notNullTask.isShared) {
                                segmentedBtn.setPosition(0, false)
                            } else {
                                segmentedBtn.setPosition(1, false)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setSaveBtnListener() {
        binding.saveBtn.setOnClickListener {
            viewModel.save()
            dismiss()
        }
    }

    private fun setCollapseBtnListener() {
        binding.collapseBtn.setOnClickListener {
            dismiss()
        }
    }

    override fun onPause() {
        super.onPause()
        if (binding.etTitle.text?.equals(viewModel.task.value?.title) == false) {
            viewModel.updateTask { oldTask ->
                oldTask.copy(title = binding.etTitle.text.toString())
            }
        }
        if (binding.etDescription.text?.equals(viewModel.task.value?.description) == false) {
            viewModel.updateTask { oldTask ->
                oldTask.copy(description = binding.etDescription.text.toString())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}