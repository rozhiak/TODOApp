package com.rmblack.todoapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rmblack.todoapp.R
import com.rmblack.todoapp.databinding.FragmentEditTaskBottomSheetBinding
import com.rmblack.todoapp.viewmodels.EditTaskViewModel
import com.rmblack.todoapp.viewmodels.EditTaskViewModelFactory
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

    //
    override fun getTheme(): Int  = R.style.Theme_NoWiredStrapInNavigationBar
    //

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