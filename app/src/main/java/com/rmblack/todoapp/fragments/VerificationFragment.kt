package com.rmblack.todoapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rmblack.todoapp.R
import com.rmblack.todoapp.databinding.FragmentVerificationBinding


class VerificationFragment : Fragment() {

    var _binding: FragmentVerificationBinding? = null

    val binding
        get() = checkNotNull(_binding) {
            "Binding is null, is the view visible?"
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVerificationBinding.inflate(inflater, container, false)

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