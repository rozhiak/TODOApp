package com.rmblack.todoapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rmblack.todoapp.adapters.PrivateTaskListAdapter
import com.rmblack.todoapp.adapters.viewholders.TaskHolder
import com.rmblack.todoapp.databinding.FragmentPrivateTasksBinding
import com.rmblack.todoapp.models.Task
import com.rmblack.todoapp.viewmodels.PrivateTasksViewModel
import kotlinx.coroutines.launch
import java.util.UUID


class PrivateTasksFragment : Fragment(), TaskHolder.EditClickListener {

    private var _binding: FragmentPrivateTasksBinding ?= null

    private val viewModel: PrivateTasksViewModel by viewModels()

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPrivateTasksBinding.inflate(inflater, container, false)

        binding.privateTasksRv.layoutManager = LinearLayoutManager(context)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var editedTaskId : UUID? = null
        setFragmentResultListener(
            EditTaskBottomSheet.REQUEST_KEY_ID
        ) { _, bundle ->
            editedTaskId = bundle.getSerializable(EditTaskBottomSheet.BUNDLE_KEY_ID) as UUID?
        }

         viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tasks.collect {tasks ->

                    val editedTaskIndex = tasks.indexOfFirst { it.id == editedTaskId }
                    val oldIndex = viewModel.detailsVisibility.indexOfFirst { it }

                    if (oldIndex != editedTaskIndex && oldIndex != -1 && editedTaskIndex != -1) {
                        if (viewModel.detailsVisibility[oldIndex]) {
                            viewModel.updateVisibility(oldIndex, false)
                            binding.privateTasksRv.adapter?.notifyItemChanged(oldIndex)
                        }

                        if (!viewModel.detailsVisibility[editedTaskIndex]) {
                            viewModel.updateVisibility(editedTaskIndex, true)
                            binding.privateTasksRv.adapter?.notifyItemChanged(editedTaskIndex)
                            binding.privateTasksRv.smoothScrollToPosition(editedTaskIndex)
                        }
                    }

                    val layoutManager = binding.privateTasksRv.layoutManager as LinearLayoutManager
                    val firstVisibleItem = layoutManager.getChildAt(0)
                    val offset = firstVisibleItem?.top ?: 0
                    val pos = layoutManager.findFirstVisibleItemPosition()
                    val marginTop = firstVisibleItem?.marginTop ?: 0

                    binding.privateTasksRv.adapter = createPrivateTasksAdapter(tasks)

                    layoutManager.scrollToPositionWithOffset(pos, offset - marginTop)
                }
            }
        }
    }

    private fun createPrivateTasksAdapter(tasks: List<Task>): PrivateTaskListAdapter {
        return PrivateTaskListAdapter(tasks, viewModel, this)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onEditClick(task: Task) {
        val editTaskBottomSheet = EditTaskBottomSheet()
        val args = Bundle()
        args.putString("taskId", task.id.toString())
        editTaskBottomSheet.arguments = args
        editTaskBottomSheet.show(parentFragmentManager, "TODO tag")
    }
}