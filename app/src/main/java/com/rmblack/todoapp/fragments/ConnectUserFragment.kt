package com.rmblack.todoapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rmblack.todoapp.R
import com.rmblack.todoapp.databinding.FragmentConnectUserBinding
import com.rmblack.todoapp.utils.SharedPreferencesManager
import com.rmblack.todoapp.utils.Utilities
import com.rmblack.todoapp.viewmodels.ConnectUserViewModel
import java.lang.Exception

class ConnectUserFragment: Fragment() {

    private lateinit var viewModel : ConnectUserViewModel

    private var _binding : FragmentConnectUserBinding? = null

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
                try {
                    viewModel.connectUserToSharedList(phone.toString())

                    val fragmentManager = requireActivity().supportFragmentManager
                    val transaction = fragmentManager.beginTransaction()
                    val newFragment = ConnectionStatusFragment()
                    transaction.add(R.id.manage_user_connection_container, newFragment).hide(this)
                    transaction.commit()

                    viewModel.saveConnectedPhone(phone.toString())

                    binding.connectProgressBtn.revertAnimation()
                } catch (e: Exception) {

                }
            } else {
                //Phone format is not correct
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
        super.onDestroy()
    }

}