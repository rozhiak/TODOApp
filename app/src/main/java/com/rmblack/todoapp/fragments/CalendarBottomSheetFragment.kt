package com.rmblack.todoapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rmblack.todoapp.R
import com.rmblack.todoapp.databinding.FragmentCalendarBottomSheetBinding
import com.rmblack.todoapp.viewmodels.CalendarViewModel

class CalendarBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentCalendarBottomSheetBinding

    private val viewModel by lazy {
        CalendarViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalendarBottomSheetBinding.inflate(inflater, container, false)
        binding.calendarCollapseBtn.setOnClickListener {
            dismiss()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpBottomSheet()
    }

    private fun setUpBottomSheet() {
        val bottomSheet: FrameLayout =
            dialog?.findViewById(com.google.android.material.R.id.design_bottom_sheet)!!

        bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

        val behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.apply {
            peekHeight = resources.displayMetrics.heightPixels
            state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    companion object {
        const val TAG = "CalendarModalBottomSheetDialog"
    }

    override fun getTheme(): Int = R.style.Theme_NoWiredStrapInNavigationBar

}