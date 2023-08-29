package com.rmblack.todoapp.activities

import android.animation.Animator
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.rmblack.todoapp.R
import com.rmblack.todoapp.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpBottomICAnim()
        setUpClickListeners()

    }

    private fun setUpBottomICAnim() {
        val vibrateAnimation = AnimationUtils.loadAnimation(this, R.anim.vibrate_animation)

        vibrateAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.icBottom.startAnimation(vibrateAnimation)
                }, 1500)
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })

        binding.icBottom.startAnimation(vibrateAnimation)
    }

    private fun setUpClickListeners() {
        binding.confirmCard.setOnClickListener {

            val oa = ObjectAnimator.ofFloat(binding.phoneField, "translationY", -230F).apply {
                duration = 500
                start()
            }

            oa.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(p0: Animator) {}

                override fun onAnimationEnd(p0: Animator) {
                    binding.nameField.visibility = View.VISIBLE }

                override fun onAnimationCancel(p0: Animator) {}

                override fun onAnimationRepeat(p0: Animator) {}

            })

            oa.start()
        }
    }
}