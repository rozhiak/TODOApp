package com.rmblack.todoapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.rmblack.todoapp.utils.SharedPreferencesManager
import com.rmblack.todoapp.viewmodels.ConnectUserViewModel

class ConnectionStatusFragment: Fragment() {

    private lateinit var viewModel : ConnectUserViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val sharedPropertiesManager = SharedPreferencesManager(requireContext())

        viewModel = ViewModelProvider(
            requireActivity(),
            ConnectUserFragment.ConnectUserViewModelFactory(sharedPropertiesManager)
        )[ConnectUserViewModel::class.java]

        return super.onCreateView(inflater, container, savedInstanceState)
    }

}