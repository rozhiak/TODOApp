package com.rmblack.todoapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.rmblack.todoapp.databinding.FragmentConnectionStatusBinding
import com.rmblack.todoapp.utils.SharedPreferencesManager
import com.rmblack.todoapp.viewmodels.ConnectUserViewModel

class ConnectionStatusFragment: Fragment(), DisconnectUserCallback {

    private lateinit var viewModel : ConnectUserViewModel

    private var _binding: FragmentConnectionStatusBinding? = null

    private val binding
        get() = checkNotNull(_binding) {
            "Binding is null. Is view visible?"
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentConnectionStatusBinding.inflate(inflater, container, false)

        val sharedPropertiesManager = SharedPreferencesManager(requireContext())

        viewModel = ViewModelProvider(
            requireActivity(),
            ConnectUserFragment.ConnectUserViewModelFactory(sharedPropertiesManager)
        )[ConnectUserViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDetails()
        setClickListeners()
    }

    private fun setClickListeners() {
        binding.disconnectBtnCard.setOnClickListener {
            viewModel.disconnectUserFromSharedList(this)
        }
    }

    private fun setDetails() {
        binding.connectedPhoneTv.text = viewModel.getConnectedPhone()
    }

    override fun onSuccess() {
        //TODO
    }

    override fun onFailure() {
        //TODO
    }

}

interface DisconnectUserCallback {
    fun onSuccess()
    fun onFailure()
}