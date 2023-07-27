package com.rmblack.todoapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.rmblack.todoapp.adapters.PrivateTaskListAdapter
import com.rmblack.todoapp.databinding.FragmentPrivateTasksBinding
import com.rmblack.todoapp.viewmodels.PrivateTasksViewModel

class PrivateTasksFragment : Fragment() {

    private var _binding: FragmentPrivateTasksBinding ?= null

    private val privateTasksViewModel: PrivateTasksViewModel by viewModels()

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPrivateTasksBinding.inflate(inflater, container, false)

        binding.privateTasksRv.layoutManager = LinearLayoutManager(context)
        val adapter = PrivateTaskListAdapter(privateTasksViewModel.tasks)
        binding.privateTasksRv.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            //Here wire up views.
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}