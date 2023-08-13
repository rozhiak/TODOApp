package com.rmblack.todoapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginTop
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rmblack.todoapp.adapters.SharedTaskHolder
import com.rmblack.todoapp.adapters.SharedTasksAdapter
import com.rmblack.todoapp.databinding.FragmentSharedTasksBinding
import com.rmblack.todoapp.models.Task
import com.rmblack.todoapp.viewmodels.SharedTasksViewModel
import kotlinx.coroutines.launch

class SharedTasksFragment : Fragment(), SharedTaskHolder.EditClickListener {

    private var _binding : FragmentSharedTasksBinding? = null

    private val viewModel: SharedTasksViewModel by viewModels()

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSharedTasksBinding.inflate(inflater, container, false)

        binding.sharedTasksRv.layoutManager = LinearLayoutManager(context)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.sharedTasks.collect {tasks ->
                    val layoutManager = binding.sharedTasksRv.layoutManager as LinearLayoutManager
                    val firstVisibleItem = layoutManager.getChildAt(0)
                    val pos = layoutManager.findFirstVisibleItemPosition()
                    val offset = firstVisibleItem?.top ?: 0
                    val marginTop = firstVisibleItem?.marginTop ?: 0

                    binding.sharedTasksRv.adapter = createSharedTasksAdapter(tasks)
                    layoutManager.scrollToPositionWithOffset(pos, offset - marginTop)
                }
            }
        }
    }

    private fun createSharedTasksAdapter(tasks: List<Task>) =
        SharedTasksAdapter(tasks, viewModel, this)


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onEditClick(task: Task) {
        val editTaskBottomSheet = EditTaskBottomSheet()
        val args = Bundle()
        args.putSerializable("taskId", task.id)
        editTaskBottomSheet.arguments = args
        editTaskBottomSheet.show(parentFragmentManager, "TODO tag")
    }

}