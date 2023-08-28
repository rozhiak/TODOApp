package com.rmblack.todoapp.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.rmblack.todoapp.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        binding.confirmCard.setOnClickListener {
//
//        }
//
//        binding.confirmCard.setOnClickListener {
//
//            val oa = ObjectAnimator.ofFloat(binding.phoneField, "translationY", -230F).apply {
//                duration = 500
//                start()
//            }
//
//            oa.addListener(object : Animator.AnimatorListener{
//                override fun onAnimationStart(p0: Animator) {
//                }
//
//                override fun onAnimationEnd(p0: Animator) {
//                    binding.nameField.visibility = View.VISIBLE
//                }
//
//                override fun onAnimationCancel(p0: Animator) {
//                }
//
//                override fun onAnimationRepeat(p0: Animator) {
//                }
//
//            })
//
//            oa.start()
//
//        }
    }
}