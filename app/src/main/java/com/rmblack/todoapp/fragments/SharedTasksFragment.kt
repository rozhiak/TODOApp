package com.rmblack.todoapp.fragments

import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginTop
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.rmblack.todoapp.adapters.SharedTasksAdapter
import com.rmblack.todoapp.adapters.viewholders.REMAINING_DAYS_LABLE
import com.rmblack.todoapp.databinding.FragmentTasksBinding
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.utils.SharedPreferencesManager
import com.rmblack.todoapp.utils.Utilities
import com.rmblack.todoapp.viewmodels.SharedTasksViewModel
import com.rmblack.todoapp.webservice.repository.ApiRepository
import kotlinx.coroutines.launch
import java.util.UUID

class SharedTasksFragment : TasksFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        binding.tasksRv.layoutManager = LinearLayoutManager(context)

        val sharedPreferencesManager = SharedPreferencesManager(requireContext())
        viewModel = ViewModelProvider(this, SharedFragmentViewModelFactory(sharedPreferencesManager))
            .get(SharedTasksViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerview()
        setUpSwipeToDelete()
    }

    private fun setUpRecyclerview() {
        var editedTaskId: UUID? = null

        var isNewTask: Boolean? = null

        setFragmentResultListener(
            EditTaskBottomSheet.REQUEST_KEY_ID_SHARED
        ) { _, bundle ->
            editedTaskId = bundle.getSerializable(EditTaskBottomSheet.BUNDLE_KEY_ID_SHARED) as UUID?
        }

        setFragmentResultListener(
            EditTaskBottomSheet.REQUEST_KEY_IS_NEW_SHARED
        ) { _, bundle ->
            isNewTask = bundle.getBoolean(EditTaskBottomSheet.BUNDLE_KEY_IS_NEW_SHARED)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tasks.collect { tasks ->
                    if (viewModel.detailsVisibility.size != viewModel.tasks.value.size) {
                        setUpForTaskMoving()
                    } else if (editedTaskId != null) {
                        setUpForNewOrEditTask(tasks, editedTaskId, isNewTask)
                    }
                    editedTaskId = null
                    isNewTask = null
                    while (viewModel.detailsVisibility.size > viewModel.tasks.value.size) {
                        setUpIfLabeledTaskMoved()
                    }

                    val layoutManager = binding.tasksRv.layoutManager as LinearLayoutManager
                    val firstVisibleItem = layoutManager.getChildAt(0)
                    val offset = firstVisibleItem?.top ?: 0
                    val pos = layoutManager.findFirstVisibleItemPosition()
                    val marginTop = firstVisibleItem?.marginTop ?: 0

                    binding.tasksRv.adapter = createSharedTasksAdapter()

                    layoutManager.scrollToPositionWithOffset(pos, offset - marginTop)

                    if (!isFirstTime) {
                        setUpNoTaskIconAndText(tasks.isNotEmpty())
                    }
                    isFirstTime = false
                }
            }
        }
    }

    private fun createSharedTasksAdapter() =
        SharedTasksAdapter(viewModel, this, requireActivity())

    class SharedFragmentViewModelFactory(private val sharedPreferencesManager: SharedPreferencesManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(SharedTasksViewModel::class.java)) {
                SharedTasksViewModel(sharedPreferencesManager) as T
            } else {
                throw IllegalArgumentException("ViewModel Not Found")
            }
        }
    }
}