package com.rmblack.todoapp.fragments

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.rmblack.todoapp.R
import com.rmblack.todoapp.databinding.FragmentLoginBinding
import com.rmblack.todoapp.viewmodels.LoginViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Binding is null, is the view visible?"
        }

    private lateinit var viewModel: ViewModel

    val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(requireActivity())[LoginViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpBottomICAnim()
        setUpClickListeners()
        hideBottomICByScroll()
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
    }

    private fun setUpClickListeners() {
        binding.continueCard.setOnClickListener {
            val phone = binding.phoneEt.text
            if (phone?.isEmpty() == true) {

            } else {


                //TODO start from here: Error on empty phone number
                binding.phoneEt.setLinkTextColor(Color.parseColor("#D05D8A"))

                binding.phoneEt.requestFocus()
                val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(binding.phoneEt, InputMethodManager.SHOW_IMPLICIT)

                val scope = CoroutineScope(Dispatchers.Main)
                scope.launch {
                    delay(1300)
                    binding.phoneEt.hint = "شماره تماس"
                    binding.phoneEt.setLinkTextColor(Color.TRANSPARENT)
                }
                //


            }

//            bringContinueUp()

//            findNavController().navigate(
//                LoginFragmentDirections.verifyPhoneNumber()
//            )
        }

        binding.icBottom.setOnClickListener {
            binding.rootScroll.post {
                binding.rootScroll.post { binding.rootScroll.fullScroll(ScrollView.FOCUS_DOWN) }
            }
            hideBottomIC()
        }

    }

    private fun bringContinueUp() {
        val oa = ObjectAnimator.ofFloat(binding.phoneField, "translationY", -230F).apply {
            duration = 500
        }
        oa.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {}

            override fun onAnimationEnd(p0: Animator) {
                binding.nameField.visibility = View.VISIBLE
            }

            override fun onAnimationCancel(p0: Animator) {}

            override fun onAnimationRepeat(p0: Animator) {}

        })
        oa.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}