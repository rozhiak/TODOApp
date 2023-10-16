package com.rmblack.todoapp.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.internal.ViewUtils.hideKeyboard
import com.rmblack.todoapp.R
import com.rmblack.todoapp.databinding.FragmentConnectUserBinding
import com.rmblack.todoapp.utils.CONNECTION_ERROR_CODE
import com.rmblack.todoapp.utils.SharedPreferencesManager
import com.rmblack.todoapp.utils.Utilities
import com.rmblack.todoapp.viewmodels.ConnectUserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.UnknownHostException


class ConnectUserFragment: Fragment() , ConnectUserCallback{

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
                hideKeyboard()
                binding.connectProgressBtn.startAnimation()
                viewModel.connectUserToSharedList(phone.toString(), this)
            } else {
                val timer = object : CountDownTimer(1000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        binding.phoneEt.setTextColor(resources.getColor(R.color.urgent_red, null))
                    }
                    override fun onFinish() {
                        binding.phoneEt.setTextColor(resources.getColor(R.color.green, null))
                    }
                }
                timer.start()
            }
        }
    }
    private fun hideKeyboard() {
        val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
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

    override fun onConnectUserSuccess() {
        val job = CoroutineScope(Dispatchers.Default).launch {
            val response = Utilities.syncTasksWithServer(viewModel.getUserToken(), requireContext())
            response.onSuccess {
                requireActivity().runOnUiThread {
                    if (_binding != null) binding.connectProgressBtn.revertAnimation()
                }
                showConnectionStatusFragment()
            }

            response.onFailure {e ->
                requireActivity().runOnUiThread {
                    if (_binding != null) binding.connectProgressBtn.revertAnimation()
                }

                if (e is UnknownHostException) {
                    Utilities.makeWarningSnack(
                        requireActivity(),
                        binding.root,
                        "به دلیل عدم اتصال به اینترنت ، هم رسانی تسک ها صورت نگرفت."
                    )
                }

                showConnectionStatusFragment()
            }

        }

        job.invokeOnCompletion {
            job.cancel()
        }

        val phone = binding.phoneEt.text
        viewModel.saveConnectedPhone(phone.toString())
    }

    private fun showConnectionStatusFragment() {
        val fragmentContainerView =
            requireActivity().findViewById<FragmentContainerView>(R.id.manage_user_connection_container)
        val fragmentManager = parentFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(fragmentContainerView.id, ConnectionStatusFragment())
        transaction.commit()
    }

    override fun onConnectUserFailure(errorCode: Int) {
        binding.connectProgressBtn.revertAnimation()
        when (errorCode) {
            CONNECTION_ERROR_CODE -> {
                Utilities.makeWarningSnack(
                    requireActivity(),
                    binding.root,
                    "مشکل در اتصال به اینترنت ، لطفا از اتصال خود مطمئن شوید."
                )
            }
            404 -> {
                Utilities.makeWarningSnack(
                    requireActivity(),
                    binding.root,
                    "شماره همراه مورد نظر شما یافت نشد."
                )            }
            403 -> {
                Utilities.makeWarningSnack(
                    requireActivity(),
                    binding.root,
                    "مشکلی در فرآیند ورودتان به برنامه پیش آمده"
                )
            }
            400 -> {
                Utilities.makeWarningSnack(
                    requireActivity(),
                    binding.root,
                    "نمی توانید شماره خودتان را در این قسمت وارد کنید."
                )
            }
        }
    }

}

interface ConnectUserCallback {
    fun onConnectUserSuccess()
    fun onConnectUserFailure(errorCode: Int)
}