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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rmblack.todoapp.adapters.PrivateTaskHolder
import com.rmblack.todoapp.adapters.PrivateTaskListAdapter
import com.rmblack.todoapp.databinding.FragmentPrivateTasksBinding
import com.rmblack.todoapp.models.Task
import com.rmblack.todoapp.viewmodels.PrivateTasksViewModel
import kotlinx.coroutines.launch


class PrivateTasksFragment : Fragment(), PrivateTaskHolder.EditClickListener {

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
                    binding.privateTasksRv.adapter = createPrivateTasksAdapter(tasks)
                }
            }
        }
    }

    private fun createPrivateTasksAdapter(tasks: List<Task>) =
        PrivateTaskListAdapter(tasks, viewModel, this)


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onEditClick(task: Task) {
        findNavController().navigate(
            PrivateTasksFragmentDirections.showEditBottomSheetFP(task.id)
        )
    }
}