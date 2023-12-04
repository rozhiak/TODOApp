package com.rmblack.todoapp.fragments

import android.app.Application
import android.os.Build
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.rmblack.todoapp.adapters.PrivateTasksAdapter
import com.rmblack.todoapp.viewmodels.PrivateTasksViewModel
import kotlinx.coroutines.launch
import java.util.UUID

class PrivateTasksFragment : TasksFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            this, PrivateFragmentViewModelFactory(requireActivity().application)
        )[PrivateTasksViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val res = super.onCreateView(inflater, container, savedInstanceState)
        binding.tasksRv.adapter = createPrivateTasksAdapter()
        return res
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restoreLastExpandedID(savedInstanceState)
        setUpRecyclerview()
    }

    private fun restoreLastExpandedID(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (savedInstanceState != null) {
                val id = savedInstanceState.getSerializable(LAST_EXPANDED_ID_KEY, UUID::class.java)
                viewModel.setPreviouslyExpandedID(id)
            }
        }
    }

    private fun setUpRecyclerview() {
        var editedTaskId: UUID? = null

        var isNewTask: Boolean? = null

        setFragmentResultListener(
            EditTaskBottomSheet.REQUEST_KEY_ID_PRIVATE
        ) { _, bundle ->
            editedTaskId =
                bundle.getSerializable(EditTaskBottomSheet.BUNDLE_KEY_ID_PRIVATE) as UUID?
        }

        setFragmentResultListener(
            EditTaskBottomSheet.REQUEST_KEY_IS_NEW_PRIVATE
        ) { _, bundle ->
            isNewTask = bundle.getBoolean(EditTaskBottomSheet.BUNDLE_KEY_IS_NEW_PRIVATE)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tasks.collect { tasks ->
                    setUpForNewOrEditTask(tasks, editedTaskId, isNewTask)
                    editedTaskId = null
                    isNewTask = null

                    val layoutManager = binding.tasksRv.layoutManager as LinearLayoutManager
                    val firstVisibleItem = layoutManager.getChildAt(0)
                    val offset = firstVisibleItem?.top ?: 0
                    val pos = layoutManager.findFirstVisibleItemPosition()
                    val marginTop = firstVisibleItem?.marginTop ?: 0

                    val adapter = binding.tasksRv.adapter as PrivateTasksAdapter
                    adapter.updateData(tasks)

                    layoutManager.scrollToPositionWithOffset(pos, offset - marginTop)

                    if (tasks.size != 1) setUpNoTaskIconAndText(tasks.isNotEmpty())
                }
            }
        }
    }

    private fun createPrivateTasksAdapter(): PrivateTasksAdapter = PrivateTasksAdapter(
        viewLifecycleOwner.lifecycleScope, viewModel, this, requireActivity()
    )

    class PrivateFragmentViewModelFactory(private val application: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(PrivateTasksViewModel::class.java)) {
                PrivateTasksViewModel(application) as T
            } else {
                throw IllegalArgumentException("ViewModel Not Found")
            }
        }
    }
}