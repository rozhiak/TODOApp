package com.rmblack.todoapp.fragments

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Selection
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.aminography.primecalendar.persian.PersianCalendar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rmblack.todoapp.R
import com.rmblack.todoapp.databinding.FragmentEditTaskBottomSheetBinding
import com.rmblack.todoapp.models.local.TaskState
import com.rmblack.todoapp.viewmodels.EditTaskViewModel
import com.rmblack.todoapp.viewmodels.EditTaskViewModelFactory
import ir.hamsaa.persiandatepicker.PersianDatePickerDialog
import ir.hamsaa.persiandatepicker.api.PersianPickerDate
import ir.hamsaa.persiandatepicker.api.PersianPickerListener
import ir.hamsaa.persiandatepicker.date.PersianDateImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

    override fun getTheme(): Int = R.style.Theme_NoWiredStrapInNavigationBar

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
    }

    private fun syncUserInput() {
        binding.apply {
            urgentSwitch.setOnCheckedChangeListener { _, b ->
                viewModel.updateTask { oldTask ->
                    oldTask.copy(
                        isUrgent = b,
                        title = binding.etTitle.text.toString(),
                        description = binding.etDescription.text.toString(),
                        state = if (viewModel.task.value?.state == TaskState.NEW) TaskState.NEW else TaskState.CHANGED
                    )
                }
                resetCursorsPosition()
            }

            segmentedBtn.setOnPositionChangedListener { pos ->
                if (pos == 1) {
                    viewModel.updateTask { oldTask ->
                        oldTask.copy(
                            isShared = false,
                            title = binding.etTitle.text.toString(),
                            description = binding.etDescription.text.toString(),
                            state = if (viewModel.task.value?.state == TaskState.NEW) TaskState.NEW else TaskState.CHANGED
                        )
                    }
                    resetCursorsPosition()
                } else if (pos == 0) {
                    viewModel.updateTask { oldTask ->
                        oldTask.copy(
                            isShared = true,
                            title = binding.etTitle.text.toString(),
                            description = binding.etDescription.text.toString(),
                            state = if (viewModel.task.value?.state == TaskState.NEW) TaskState.NEW else TaskState.CHANGED
                        )
                    }
                    resetCursorsPosition()
                }
            }

            deadlineTv.setOnClickListener {
                saveTitleAndDescription()
                showDatePicker()
                resetCursorsPosition()
            }

            calendarIc.setOnClickListener {
                saveTitleAndDescription()
                showDatePicker()
                resetCursorsPosition()
            }

            urgentLable.setOnClickListener {
                urgentSwitch.toggle()
            }
        }
    }

    private fun resetCursorsPosition() {
        val etTitle: Editable? = binding.etTitle.text
        Selection.setSelection(etTitle, binding.etTitle.text?.length ?: 0)

        val etDec: Editable? = binding.etDescription.text
        Selection.setSelection(etDec, binding.etDescription.text?.length ?: 0)
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

        val picker = PersianDatePickerDialog(requireContext())
            .setPositiveButtonString("باشه")
            .setNegativeButton("بیخیال")
            .setTodayButton("امروز")
            .setTodayButtonVisible(true)
            .setInitDate(persianPickerDate, true)
            .setActionTextColor(Color.parseColor("#5DD0A3"))
            .setTitleType(PersianDatePickerDialog.WEEKDAY_DAY_MONTH_YEAR)
            .setBackgroundColor(Color.parseColor("#eefaf5"))
            .setPickerBackgroundColor(Color.parseColor("#eefaf5"))
            .setAllButtonsTextSize(16)
            .setListener(object : PersianPickerListener {
                override fun onDateSelected(persianPickerDate: PersianPickerDate) {
                    viewModel.updateTask {
                        val newDeadline = PersianCalendar()
                        newDeadline.year = persianPickerDate.persianYear
                        newDeadline.month = persianPickerDate.persianMonth - 1
                        newDeadline.dayOfMonth = persianPickerDate.persianDay
                        it.copy(
                            deadLine = newDeadline,
                            state = if (viewModel.task.value?.state == TaskState.NEW) TaskState.NEW else TaskState.CHANGED
                        )
                    }
                }

                override fun onDismissed() {

                }
            })

        picker.show()
    }

    private fun updateUi() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.task.collect { task ->
                    task?.let { notNullTask ->
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

    private fun setCollapseBtnListener() {
        binding.saveBtn.setOnClickListener {
            if (binding.etTitle.text?.isBlank() == true || binding.etTitle.text?.isEmpty() == true) {

                binding.etTitle.setHintTextColor(Color.parseColor("#D05D8A"))
                binding.etTitle.hint = "عنوان را تایپ کنید"

                val scope = CoroutineScope(Dispatchers.Main)
                scope.launch {
                    delay(1300)
                    binding.etTitle.hint = "عنوان"
                    binding.etTitle.setHintTextColor(Color.parseColor("#74000000"))

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
        super.onPause()
        saveTitleAndDescription()
    }

    private fun saveTitleAndDescription() {
        if (binding.etTitle.text?.equals(viewModel.task.value?.title) == false ||
            binding.etDescription.text?.equals(viewModel.task.value?.description) == false) {
            viewModel.updateTask { oldTask ->
                oldTask.copy(
                    title = binding.etTitle.text.toString(),
                    description = binding.etDescription.text.toString(),
                    state = if (viewModel.task.value?.state == TaskState.NEW) TaskState.NEW else TaskState.CHANGED
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (binding.etTitle.text?.isNotBlank() == true || binding.etTitle.text?.isNotEmpty() == true) {
            setFragmentResult(
                REQUEST_KEY_ID_PRIVATE,
                bundleOf(BUNDLE_KEY_ID_PRIVATE to viewModel.task.value?.id)
            )
            setFragmentResult(
                REQUEST_KEY_ID_SHARED,
                bundleOf(BUNDLE_KEY_ID_SHARED to viewModel.task.value?.id)
            )
        }
    }

    companion object {
        const val REQUEST_KEY_ID_PRIVATE = "REQUEST_KEY_ID_PRIVATE"

        const val BUNDLE_KEY_ID_PRIVATE = "BUNDLE_KEY_ID_PRIVATE"

        const val REQUEST_KEY_ID_SHARED = "REQUEST_KEY_ID_SHARED"

        const val BUNDLE_KEY_ID_SHARED = "BUNDLE_KEY_ID_PRIVATE_SHARED"
    }

}