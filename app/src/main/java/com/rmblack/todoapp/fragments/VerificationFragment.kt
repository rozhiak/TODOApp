package com.rmblack.todoapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.rmblack.todoapp.R
import com.rmblack.todoapp.databinding.FragmentVerificationBinding
import com.rmblack.todoapp.utils.SharedPreferencesManager
import com.rmblack.todoapp.viewmodels.LoginViewModel
import kotlinx.coroutines.launch


class VerificationFragment : Fragment() {

    var _binding: FragmentVerificationBinding? = null

    val binding
        get() = checkNotNull(_binding) {
            "Binding is null, is the view visible?"
        }

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVerificationBinding.inflate(inflater, container, false)

        val sharedPreferencesManager = SharedPreferencesManager(requireContext())
        viewModel = ViewModelProvider(requireActivity(),
            LoginFragment.LoginViewModelFactory(sharedPreferencesManager)
        )[LoginViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        binding.confirmCard.setOnClickListener {
            if (isCodeCompleted()) {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        val response = viewModel.validateUser(binding.verifyCodeEditText.text)
                        if (response) {
                            viewModel.changeEntranceState(true)
                        }
                    }
                }
            } else {
                //verification code is not completed by user
            }
        }
    }

    private fun isCodeCompleted(): Boolean {
        val code = binding.verifyCodeEditText.text
        if (code.length != 4) return false
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}