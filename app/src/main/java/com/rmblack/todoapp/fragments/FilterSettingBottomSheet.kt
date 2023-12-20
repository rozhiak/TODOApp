package com.rmblack.todoapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rmblack.todoapp.R
import com.rmblack.todoapp.databinding.FragmentFilterSettingBottomSheetBinding

class FilterSettingBottomSheet: BottomSheetDialogFragment() {

    private var _binding: FragmentFilterSettingBottomSheetBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    override fun getTheme(): Int = R.style.Theme_NoWiredStrapInNavigationBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterSettingBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClickListeners()
    }

    private fun setClickListeners() {
        binding.collapseBtn.setOnClickListener {
            dismiss()
        }
    }

}