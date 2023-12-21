package com.rmblack.todoapp.fragments

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rmblack.todoapp.R
import com.rmblack.todoapp.databinding.FragmentFilterSettingBottomSheetBinding
import com.rmblack.todoapp.viewmodels.FilterSettingViewModel
import kotlinx.coroutines.launch

class FilterSettingBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentFilterSettingBottomSheetBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private lateinit var viewModel: FilterSettingViewModel

    override fun getTheme(): Int = R.style.Theme_NoWiredStrapInNavigationBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(
            requireActivity(), FilterSettingViewModelFactory(requireActivity().application)
        )[FilterSettingViewModel::class.java]
        _binding = FragmentFilterSettingBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClickListeners()
        setDoneTasksFilterCheckBox()
    }

    private fun setDoneTasksFilterCheckBox() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.doNotShowDoneTasksState.collect {
                binding.doneFilterCheckBox.isChecked = it
            }
        }

        binding.doneFilterCheckBox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setDoNotShowDoneTasks(isChecked)
        }
    }

    private fun setClickListeners() {
        binding.collapseBtn.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroy() {
        viewModel.saveDoneTasksFilterState()
        super.onDestroy()
    }

}

@Suppress("UNCHECKED_CAST")
class FilterSettingViewModelFactory(private val application: Application) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FilterSettingViewModel::class.java)) {
            return FilterSettingViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}