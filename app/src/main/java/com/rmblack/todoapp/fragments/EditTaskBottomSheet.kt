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
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rmblack.todoapp.R
import com.rmblack.todoapp.databinding.FragmentEditTaskBottomSheetBinding
import com.rmblack.todoapp.models.Task
import com.rmblack.todoapp.viewmodels.EditTaskViewModel
import com.rmblack.todoapp.viewmodels.EditTaskViewModelFactory
import kotlinx.coroutines.launch


class EditTaskBottomSheet : BottomSheetDialogFragment() {

    private val args: EditTaskBottomSheetArgs by navArgs()

    private val viewModel: EditTaskViewModel by viewModels {
        EditTaskViewModelFactory(args.taskId)
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
    }

    private fun syncUserInput() {
        binding.apply {
            etTitle.doOnTextChanged { text, _, _, _ ->
                viewModel.updateTask { oldTask ->
                    val updatedTask = oldTask.copy(title = text.toString())
                    updatedTask
                }
            }

            urgentSwitch.setOnCheckedChangeListener { _, b ->
                viewModel.updateTask { oldTask ->
                    val updatedTask = oldTask.copy(isUrgent = b)
                    updatedTask
                }
            }

            etDescription.doOnTextChanged { text, _, _, _ ->
                viewModel.updateTask { oldTask ->
                    val updatedTask = oldTask.copy(description = text.toString())
                    updatedTask
                }
            }
        }
    }

    private fun updateUi() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.task.collect { task ->
                    task?.let {
                        binding.apply {
                            urgentSwitch.isChecked = it.isUrgent
                            etTitle.setText(it.title)
                            deadlineTv.text = it.deadLine.longDateString
                            etDescription.setText(it.description)
                        }
                    }
                }
            }
        }
    }

    private fun setCollapseBtnListener() {
        binding.collapseBtn.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}