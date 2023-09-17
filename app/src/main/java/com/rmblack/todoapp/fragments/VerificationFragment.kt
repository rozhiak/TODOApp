package com.rmblack.todoapp.fragments

import android.content.Intent
import android.graphics.Color
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
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.rmblack.todoapp.R
import com.rmblack.todoapp.activities.MainActivity
import com.rmblack.todoapp.activities.StarterActivity
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
        binding.progressBtn.setOnClickListener {
            if (isCodeCompleted()) {
                showProgressing()
                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        val response = viewModel.validateUser(binding.verifyCodeEditText.text)
                        if (response) {
                            val intent = Intent(requireContext(), MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                        } else {
                            binding.progressBtn.hideProgress("تایید")
                        }
                    }
                }
            } else {
                //TODO verification code is not completed by user
            }
        }
    }

    private fun isCodeCompleted(): Boolean {
        val code = binding.verifyCodeEditText.text
        if (code.length != 4) return false
        return true
    }

    private fun showProgressing() {
        binding.progressBtn.setPadding(0, 8, 20, 0)
        binding.progressBtn.showProgress {
            progressColor = Color.WHITE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}