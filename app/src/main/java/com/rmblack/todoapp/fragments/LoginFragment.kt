package com.rmblack.todoapp.fragments

import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.rmblack.todoapp.R
import com.rmblack.todoapp.activities.MainActivity
import com.rmblack.todoapp.databinding.FragmentLoginBinding
import com.rmblack.todoapp.utils.CONNECTION_ERROR_CODE
import com.rmblack.todoapp.viewmodels.LoginViewModel
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Binding is null, is the view visible?"
        }

    private lateinit var viewModel: LoginViewModel

    val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(
            requireActivity(), LoginViewModelFactory(requireActivity().application)
        )[LoginViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpBottomIC()
        setUpClickListeners()
        hideBottomICByScroll()
        setUpProgressButton()
        setUpProgressingState()
        setOnTextChangedListener()
    }

    private fun setOnTextChangedListener() {
        binding.phoneEt.doOnTextChanged { _, _, _, _ ->
            if (binding.nameField.isVisible) {
                hideName()
            }
        }
    }

    private fun hideName() {
        binding.nameField.visibility = View.GONE
        binding.nameEt.setText("")
        binding.errorHintTv.text = ""

        val oaHint = ObjectAnimator.ofFloat(
            binding.errorHintTv, "translationY", 15F
        ).apply {
            duration = 500
        }
        oaHint.start()

        val oaPhone = ObjectAnimator.ofFloat(binding.phoneField, "translationY", 15F).apply {
            duration = 500
        }
        oaPhone.start()

        binding.rootScroll.fullScroll(ScrollView.FOCUS_DOWN)
    }

    private fun setUpProgressingState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loginFragmentLoading.collect { isLoading ->
                if (isLoading) {
                    showProgressing()
                } else {
                    binding.progressBtn.hideProgress("ادامه")
                }
            }
        }
    }

    private fun setUpBottomIC() {
        if (viewModel.getBottomICVisibility()) {
            setUpBottomICAnim()
        } else {
            binding.icBottom.visibility = View.GONE
        }
    }

    private fun setUpProgressButton() {
        bindProgressButton(binding.progressBtn)
        binding.progressBtn.attachTextChangeAnimator()
    }

    private fun hideBottomICByScroll() {
        binding.rootScroll.setOnScrollChangeListener { _, _, _, _, i4 ->
            if (i4 > 350) {
                hideBottomIC()
            }
        }
    }

    private fun setUpBottomICAnim() {
        val vibrateAnimation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.vibrate_animation)

        vibrateAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                handler.postDelayed({
                    binding.icBottom.startAnimation(vibrateAnimation)
                }, 1500)
            }

            override fun onAnimationEnd(animation: Animation) {}

            override fun onAnimationRepeat(animation: Animation) {}
        })

        binding.icBottom.startAnimation(vibrateAnimation)
    }

    private fun hideBottomIC() {
        binding.icBottom.visibility = View.GONE
        handler.removeCallbacksAndMessages(null)
        viewModel.setBottomICVisibility(false)
    }

    private fun setUpClickListeners() {
        binding.progressBtn.setOnClickListener {
            val phone = binding.phoneEt.text.toString()
            var name = ""
            if (binding.nameField.visibility == View.VISIBLE) {
                name = binding.nameEt.text.toString()
            }
            if (phone.isEmpty()) {
                setEmptyPhoneHint()
            } else if (phone.length != 11) {
                setIncorrectPhoneHint()
            } else if (binding.nameField.visibility == View.VISIBLE && name.isEmpty()) {
                setEmptyNameHint()
            } else {
                viewModel.updateLoginLoadingState(true)
                binding.errorHintTv.text = ""
                if (binding.nameField.visibility == View.VISIBLE) {
                    viewModel.newUser(phone, name)
                    collectNewUserRequestCode()
                } else {
                    viewModel.loginUser(phone)
                    collectPhoneRequestCode()
                }
            }
        }

        binding.icBottom.setOnClickListener {
            binding.rootScroll.post {
                binding.rootScroll.post { binding.rootScroll.fullScroll(ScrollView.FOCUS_DOWN) }
            }
            hideBottomIC()
        }

        binding.enterWithoutLoginLable.setOnClickListener {
            viewModel.changeEntranceState(true)
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

        binding.policyLableTv.setOnClickListener {
            val policyBottomSheet = PolicyBottomSheet()
            policyBottomSheet.show(requireActivity().supportFragmentManager, "policy")
        }
    }

    private fun setEmptyNameHint() {
        binding.nameEt.requestFocus()
        showKeyboard(binding.nameEt)
        binding.errorHintTv.text = "◌ لطفا نام خود را وارد کنید."
    }

    private fun setIncorrectPhoneHint() {
        binding.phoneEt.requestFocus()
        showKeyboard(binding.phoneEt)
        binding.errorHintTv.text = "◌ فرمت شماره غلط است."
    }

    private fun setEmptyPhoneHint() {
        binding.phoneEt.requestFocus()
        showKeyboard(binding.phoneEt)
        binding.errorHintTv.text = "◌ لطفا ، شماره همراه را وارد کنید"
    }

    private fun showKeyboard(et: AppCompatEditText) {
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun showProgressing() {
        binding.progressBtn.setPadding(0, 12, 0, 0)
        binding.progressBtn.showProgress {
            progressColor = Color.WHITE
            buttonText = "کمی صبر...   "
        }
    }

    private fun collectPhoneRequestCode() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loginRequestCode.collect { code ->
                when (code) {
                    200 -> {
                        findNavController().navigate(
                            LoginFragmentDirections.verifyPhoneNumber()
                        )
                    }

                    404 -> {
                        bringPhoneUp()
                    }

                    CONNECTION_ERROR_CODE -> {
                        binding.errorHintTv.text = "◌ مشکل در اتصال به اینترنت"
                    }
                }
                viewModel.resetLoginRequestCode()
            }
        }
    }

    private fun collectNewUserRequestCode() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.newUserRequestCode.collect { code ->
                when (code) {
                    201 -> {
                        findNavController().navigate(
                            LoginFragmentDirections.verifyPhoneNumber()
                        )
                    }

                    400 -> {
                        val phone = binding.phoneEt.text.toString()
                        viewModel.loginUser(phone)
                        collectPhoneRequestCode()
                    }

                    CONNECTION_ERROR_CODE -> {
                        binding.errorHintTv.text = "◌ مشکل در اتصال به اینترنت"
                    }
                }
                viewModel.resetNewUserRequestCode()
            }
        }
    }

    private fun bringPhoneUp() {
        val oaHint = ObjectAnimator.ofFloat(
            binding.errorHintTv, "translationY", -230F
        ).apply {
            duration = 500
        }
        oaHint.start()

        val oaPhone = ObjectAnimator.ofFloat(binding.phoneField, "translationY", -230F).apply {
            duration = 500
        }
        oaPhone.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {}

            override fun onAnimationEnd(p0: Animator) {
                binding.nameField.visibility = View.VISIBLE
            }

            override fun onAnimationCancel(p0: Animator) {}

            override fun onAnimationRepeat(p0: Animator) {}

        })
        oaPhone.start()
    }

    class LoginViewModelFactory(private val application: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                return LoginViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}