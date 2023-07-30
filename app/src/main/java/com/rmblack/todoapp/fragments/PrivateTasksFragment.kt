package com.rmblack.todoapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.rmblack.todoapp.adapters.PrivateTaskListAdapter
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.databinding.FragmentPrivateTasksBinding
import com.rmblack.todoapp.models.Task
import com.rmblack.todoapp.viewmodels.PrivateTasksViewModel
import kotlinx.coroutines.launch

class PrivateTasksFragment : Fragment() {

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
                viewModel.privateTasks.collect {
                    binding.privateTasksRv.adapter = PrivateTaskListAdapter(it)
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}