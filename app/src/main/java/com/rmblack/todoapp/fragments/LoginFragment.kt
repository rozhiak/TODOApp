package com.rmblack.todoapp.fragments

import android.animation.Animator
import android.animation.ObjectAnimator
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.rmblack.todoapp.R
import com.rmblack.todoapp.activities.MainActivity
import com.rmblack.todoapp.databinding.FragmentLoginBinding
import com.rmblack.todoapp.utils.SharedPreferencesManager
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        val sharedPreferencesManager = SharedPreferencesManager(requireContext())
        viewModel = ViewModelProvider(
            requireActivity(),
            LoginViewModelFactory(sharedPreferencesManager)
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
    }

    private fun setUpProgressingState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginFragmentLoading.collect { isLoading ->
                    if (isLoading) {
                        showProgressing()
                    } else {
                        binding.progressBtn.hideProgress("ادامه")
                    }
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
            } else if (phone.isNotEmpty() && phone.length != 11){
                setIncorrectPhoneHint()
            } else if (binding.nameField.visibility == View.VISIBLE && name.isEmpty()) {
                setEmptyNameHint()
            } else  {
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
    }

    private fun setEmptyNameHint() {
        binding.nameEt.requestFocus()
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(binding.nameEt, InputMethodManager.SHOW_IMPLICIT)
        binding.errorHintTv.text = "◌ لطفا نام خود را وارد کنید."
    }

    private fun setIncorrectPhoneHint() {
        binding.phoneEt.requestFocus()
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(binding.phoneEt, InputMethodManager.SHOW_IMPLICIT)
        binding.errorHintTv.text = "◌ فرمت شماره غلط است."
    }

    private fun setEmptyPhoneHint() {
        binding.phoneEt.requestFocus()
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(binding.phoneEt, InputMethodManager.SHOW_IMPLICIT)
        binding.errorHintTv.text = "◌ لطفا ، شماره همراه را وارد کنید"
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
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginRequestCode.collect { code ->
                    if (code == 200) {
                        findNavController().navigate(
                            LoginFragmentDirections.verifyPhoneNumber()
                        )
                    } else if(code == 404) {
                        bringPhoneUp()
                    }
                    viewModel.resetLoginRequestCode()
                }
            }
        }
    }

    private fun collectNewUserRequestCode() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.newUserRequestCode.collect { code ->
                    if (code == 201) {
                        findNavController().navigate(
                            LoginFragmentDirections.verifyPhoneNumber()
                        )
                    } else if(code == 400) {
                        //User already exist -this situation must not happen
                        //because we are calling login before any thing
                    }
                    viewModel.resetNewUserRequestCode()
                }
            }
        }
    }

    private fun bringPhoneUp() {
        val oaHint = ObjectAnimator.ofFloat(binding.errorHintTv, "translationY", -230F).apply {
            duration = 500
        }
        oaHint.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {
                binding.nameEt.setText("")
            }

            override fun onAnimationEnd(p0: Animator) {}

            override fun onAnimationCancel(p0: Animator) {}

            override fun onAnimationRepeat(p0: Animator) {}

        })
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

    class LoginViewModelFactory(private val sharedPreferencesManager: SharedPreferencesManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                return LoginViewModel(sharedPreferencesManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}