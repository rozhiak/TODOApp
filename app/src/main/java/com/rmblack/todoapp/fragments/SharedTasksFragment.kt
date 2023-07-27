package com.rmblack.todoapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.rmblack.todoapp.adapters.SharedTasksAdapter
import com.rmblack.todoapp.databinding.FragmentSharedTasksBinding
import com.rmblack.todoapp.viewmodels.SharedTasksViewModel

class SharedTasksFragment : Fragment() {

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
        // Inflate the layout for this fragment
        _binding = FragmentSharedTasksBinding.inflate(inflater, container, false)

        binding.sharedTasksRv.layoutManager = LinearLayoutManager(context)
        val adapter = SharedTasksAdapter(viewModel.tasks)
        binding.sharedTasksRv.adapter = adapter
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