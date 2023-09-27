package com.rmblack.todoapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.rmblack.todoapp.R
import com.rmblack.todoapp.databinding.FragmentConnectUserBinding
import com.rmblack.todoapp.utils.SharedPreferencesManager
import com.rmblack.todoapp.utils.Utilities
import com.rmblack.todoapp.viewmodels.CONNECTION_ERROR_CODE
import com.rmblack.todoapp.viewmodels.ConnectUserViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Exception

class ConnectUserFragment: Fragment() , ConnectUserCallback{

    private lateinit var viewModel : ConnectUserViewModel

    private var _binding : FragmentConnectUserBinding? = null

    private var syncTasksJob : Job? = null

    val binding
        get() = checkNotNull(_binding) {
            "Binding is null, is the view visible?"
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentConnectUserBinding.inflate(inflater, container, false)

        val sharedPreferencesManager = SharedPreferencesManager(requireContext())

        viewModel = ViewModelProvider(
            requireActivity(),
            ConnectUserViewModelFactory(sharedPreferencesManager)
        )[ConnectUserViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpClickListeners()
    }

    private fun setUpClickListeners() {
        binding.connectProgressBtn.setOnClickListener {
            val phone = binding.phoneEt.text ?: ""
            if (phone.length == 11) {
                binding.connectProgressBtn.startAnimation()
                viewModel.connectUserToSharedList(phone.toString(), this)
            } else {
                //TODO Phone format is not correct
            }
        }
    }

    class ConnectUserViewModelFactory(
        private val sharedPreferencesManager: SharedPreferencesManager
    ): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ConnectUserViewModel::class.java)) {
                return ConnectUserViewModel(sharedPreferencesManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    override fun onDestroy() {
        _binding = null
        syncTasksJob?.cancel()
        super.onDestroy()
    }

    override fun onConnectUserSuccess() {
        binding.connectProgressBtn.revertAnimation()

        syncTasksJob = viewLifecycleOwner.lifecycleScope.launch {
            Utilities.syncTasksWithServer(viewModel.getUserToken(), requireContext())
        }

        val fragmentContainerView = requireActivity().findViewById<FragmentContainerView>(R.id.manage_user_connection_container)
        val fragmentManager = parentFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(fragmentContainerView.id, ConnectionStatusFragment())
        transaction.commit()

        val phone = binding.phoneEt.text
        viewModel.saveConnectedPhone(phone.toString())
    }

    override fun onConnectUserFailure(errorCode: Int) {
        binding.connectProgressBtn.revertAnimation()
        when (errorCode) {
            CONNECTION_ERROR_CODE -> {
                //TODO say to user: connection error
            }
            404 -> {
                //phone number not found
            }
            403 -> {
                //invalid token
            }
            400 -> {
                //same giver and receiver
            }
        }
    }

}

interface ConnectUserCallback {
    fun onConnectUserSuccess()
    fun onConnectUserFailure(errorCode: Int)
}