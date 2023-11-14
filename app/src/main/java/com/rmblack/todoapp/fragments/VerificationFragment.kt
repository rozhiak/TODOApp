package com.rmblack.todoapp.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.rmblack.todoapp.activities.MainActivity
import com.rmblack.todoapp.databinding.FragmentVerificationBinding
import com.rmblack.todoapp.utils.CONNECTION_ERROR_CODE
import com.rmblack.todoapp.utils.SharedPreferencesManager
import com.rmblack.todoapp.viewmodels.LoginViewModel
import kotlinx.coroutines.launch

class VerificationFragment : Fragment() {

    private var _binding: FragmentVerificationBinding? = null

    val binding
        get() = checkNotNull(_binding) {
            "Binding is null, is the view visible?"
        }

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerificationBinding.inflate(inflater, container, false)

        val sharedPreferencesManager = SharedPreferencesManager(requireContext())
        viewModel = ViewModelProvider(
            requireActivity(), LoginFragment.LoginViewModelFactory(sharedPreferencesManager)
        )[LoginViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()
        setLoadingState()
        onBackPressed()
    }

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(),
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })
    }

    private fun setLoadingState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.verificationFragmentLoading.collect { isLoading ->
                if (isLoading) {
                    showProgressing()
                } else {
                    binding.progressBtn.hideProgress("تایید")
                }
            }
        }
    }

    private fun setOnClickListeners() {
        binding.progressBtn.setOnClickListener {
            binding.tvError.text = ""
            if (isCodeCompleted()) {
                viewModel.updateVerificationLoadingState(true)
                viewModel.validateUser(binding.verifyCodeEditText.text)
                collectVerifyRequestCode()
            } else {
                showError()
            }
        }
    }

    private fun showError() {
        val timer = object : CountDownTimer(2300, 400) {
            var onError = false
            override fun onTick(millisUntilFinished: Long) {
                onError = if (onError) {
                    binding.verifyCodeEditText.resetCodeItemLineDrawable()
                    false
                } else {
                    binding.verifyCodeEditText.setCodeItemErrorLineDrawable()
                    true
                }
            }

            override fun onFinish() {
                binding.verifyCodeEditText.resetCodeItemLineDrawable()
            }
        }
        timer.start()
    }

    private fun collectVerifyRequestCode() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.verifyRequestCode.collect { code ->
                when (code) {
                    200 -> {
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    }

                    404 -> {
                        binding.tvError.text = "◌ حساب کاربر یافت نشد."
                    }

                    CONNECTION_ERROR_CODE -> {
                        binding.tvError.text = "◌ مشکل در اتصال به اینترنت"
                    }
                    // There would be a case for when otp code is not entered correctly.
                    //CASE -> {
                    //    binding.tvError.text = "◌ کد وارد شده صحیح نیست"
                    //}
                }
                viewModel.resetVerifyRequestCode()
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
            buttonText = "کمی صبر...   "
        }
    }

}