package com.rmblack.todoapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rmblack.todoapp.R
import com.rmblack.todoapp.databinding.FragmentVerificationBinding
import com.rmblack.todoapp.viewmodels.LoginViewModel


class VerificationFragment : Fragment() {

    var _binding: FragmentVerificationBinding? = null

    val binding
        get() = checkNotNull(_binding) {
            "Binding is null, is the view visible?"
        }

    private lateinit var viewModel: ViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVerificationBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(requireActivity())[LoginViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}