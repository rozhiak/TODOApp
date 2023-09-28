package com.rmblack.todoapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.rmblack.todoapp.R
import com.rmblack.todoapp.databinding.FragmentConnectionStatusBinding
import com.rmblack.todoapp.utils.CONNECTION_ERROR_CODE
import com.rmblack.todoapp.utils.SharedPreferencesManager
import com.rmblack.todoapp.utils.Utilities
import com.rmblack.todoapp.viewmodels.ConnectUserViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.UnknownHostException

class ConnectionStatusFragment: Fragment(), DisconnectUserCallback {

    private var syncTasksJob : Job? = null

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
        binding.disconnectProgressBtn.setOnClickListener {
            binding.disconnectProgressBtn.startAnimation()
            viewModel.disconnectUserFromSharedList(this)
        }
    }

    private fun setDetails() {
        binding.connectedPhoneTv.text = viewModel.getConnectedPhone()
    }

    override fun onSuccess() {
        binding.disconnectProgressBtn.revertAnimation()

        syncTasksJob = viewLifecycleOwner.lifecycleScope.launch {
            val response = Utilities.syncTasksWithServer(viewModel.getUserToken(), requireContext())
            response.onFailure {e ->
                if (e is UnknownHostException) {
                    //TODO say to user: Couldn't sync data due to network connection issue
                }
            }
        }

        val fragmentContainerView = requireActivity().findViewById<FragmentContainerView>(R.id.manage_user_connection_container)
        val fragmentManager = parentFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(fragmentContainerView.id, ConnectUserFragment())
        transaction.commit()

        viewModel.saveConnectedPhone("")


    }

    override fun onFailure(errorCode: Int) {
        binding.disconnectProgressBtn.revertAnimation()
        when (errorCode) {
            CONNECTION_ERROR_CODE -> {

            }
            403 -> {
                //Invalid token
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        syncTasksJob?.cancel()
    }

}

interface DisconnectUserCallback {
    fun onSuccess()
    fun onFailure(errorCode: Int)
}