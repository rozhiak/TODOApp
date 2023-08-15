package com.rmblack.todoapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.rmblack.todoapp.adapters.PrivateTaskListAdapter
import com.rmblack.todoapp.adapters.viewholders.TaskHolder
import com.rmblack.todoapp.databinding.FragmentPrivateTasksBinding
import com.rmblack.todoapp.models.Task
import com.rmblack.todoapp.viewmodels.PrivateTasksViewModel
import kotlinx.coroutines.launch


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
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.privateTasks.collect {tasks ->
                    val layoutManager = binding.privateTasksRv.layoutManager as LinearLayoutManager
                    val firstVisibleItem = layoutManager.getChildAt(0)
                    val offset = firstVisibleItem?.top ?: 0

                    val pos: Int = if (offset < -52) {
                        layoutManager.findFirstVisibleItemPosition()
                    } else {
                        layoutManager.findFirstVisibleItemPosition() + 1
                    }

                    binding.privateTasksRv.adapter = createPrivateTasksAdapter(tasks)
                    layoutManager.scrollToPositionWithOffset(pos, offset + 53)
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