package com.rmblack.todoapp.fragments

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.rmblack.todoapp.R
import com.rmblack.todoapp.alarm.AlarmSchedulerImpl
import com.rmblack.todoapp.databinding.FragmentConnectUserBinding
import com.rmblack.todoapp.utils.CONNECTION_ERROR_CODE
import com.rmblack.todoapp.utils.Utilities
import com.rmblack.todoapp.viewmodels.ConnectUserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.UnknownHostException

class ConnectUserFragment : Fragment(), ConnectUserCallback, RefreshCallback {

    private lateinit var activity: Activity

    private lateinit var fragmentManager: FragmentManager

    private lateinit var viewModel: ConnectUserViewModel

    private var _binding: FragmentConnectUserBinding? = null

    val binding
        get() = checkNotNull(_binding) {
            "Binding is null, is the view visible?"
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentConnectUserBinding.inflate(inflater, container, false)

        activity = requireActivity()

        fragmentManager = parentFragmentManager

        viewModel = ViewModelProvider(
            requireActivity(), ConnectUserViewModelFactory(requireActivity().application)
        )[ConnectUserViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpClickListeners()
        setUpLoadingState()
    }

    override fun onRefresh() {
        try {
            val phones = viewModel.getConnectedPhonesFromSP()
            if (phones != null) {
                showConnectionStatusFragment()
            }
        } catch (_: UninitializedPropertyAccessException) {
        }
    }

    private fun setUpLoadingState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.connectLoading.collect {
                if (it) {
                    binding.connectProgressBtn.startAnimation()
                } else {
                    binding.connectProgressBtn.revertAnimation()
                }
            }
        }
    }

    private fun setUpClickListeners() {
        binding.connectProgressBtn.setOnClickListener {
            connectUser()
        }

        binding.phoneEt.setOnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                connectUser()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun connectUser() {
        val phone = binding.phoneEt.text ?: ""
        if (phone.length == 11) {
            hideKeyboard()
            viewModel.setConnectLoadingState(true)
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

    private fun hideKeyboard() {
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    class ConnectUserViewModelFactory(
        private val application: Application
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ConnectUserViewModel::class.java)) {
                return ConnectUserViewModel(application) as T
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
            val token = viewModel.getUserToken()
            token?.let {
                val alarmScheduler = AlarmSchedulerImpl(requireContext())
                val response = Utilities.syncTasksWithServer(
                    it, viewModel.sharedPreferencesManager, alarmScheduler
                )
                response.onSuccess {
                    activity.runOnUiThread {
                        if (_binding != null) viewModel.setConnectLoadingState(false)
                    }
                    showConnectionStatusFragment()
                }

                response.onFailure { e ->
                    requireActivity().runOnUiThread {
                        if (_binding != null) viewModel.setConnectLoadingState(false)
                    }

                    if (e is UnknownHostException) {
                        makeSnack("به دلیل عدم اتصال به اینترنت ، هم رسانی تسک ها صورت نگرفت.")
                    }

                    showConnectionStatusFragment()
                }
            }
        }

        job.invokeOnCompletion {
            job.cancel()
        }
    }

    private fun showConnectionStatusFragment() {
        val fragmentContainerView =
            activity.findViewById<FragmentContainerView>(R.id.manage_user_connection_container)
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(fragmentContainerView.id, ConnectionStatusFragment())
        try {
            transaction.commit()
        } catch (_: IllegalStateException) {
        }
    }

    override fun onConnectUserFailure(errorCode: Int) {
        viewModel.setConnectLoadingState(false)
        when (errorCode) {
            CONNECTION_ERROR_CODE -> {
                makeSnack("مشکل در اتصال به اینترنت ، لطفا از اتصال خود مطمئن شوید.")
            }

            404 -> {
                makeSnack("شماره همراه مورد نظر شما یافت نشد.")
            }

            403 -> {
                makeSnack("مشکلی در فرآیند ورودتان به برنامه پیش آمده")
            }

            400 -> {
                makeSnack("نمی توانید شماره خودتان را در این قسمت وارد کنید.")
            }
        }
    }

    private fun makeSnack(msg: String) {
        Utilities.makeWarningSnack(
            requireActivity(), binding.root, msg
        )
    }
}

interface ConnectUserCallback {
    fun onConnectUserSuccess()
    fun onConnectUserFailure(errorCode: Int)
}