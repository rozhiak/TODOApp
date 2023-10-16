package com.rmblack.todoapp.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.ViewModelProvider
import com.rmblack.todoapp.R
import com.rmblack.todoapp.databinding.FragmentConnectionStatusBinding
import com.rmblack.todoapp.utils.CONNECTION_ERROR_CODE
import com.rmblack.todoapp.utils.SharedPreferencesManager
import com.rmblack.todoapp.utils.Utilities
import com.rmblack.todoapp.viewmodels.ConnectUserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.UnknownHostException

class ConnectionStatusFragment: Fragment(), DisconnectUserCallback {

    private lateinit var activity: Activity

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

        val sharedPreferencesManager = SharedPreferencesManager(requireContext())

        activity = requireActivity()

        viewModel = ViewModelProvider(
            activity as FragmentActivity,
            ConnectUserFragment.ConnectUserViewModelFactory(sharedPreferencesManager)
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
        val job = CoroutineScope(Dispatchers.Default).launch {
            val response = Utilities.syncTasksWithServer(viewModel.getUserToken(), requireContext())
            response.onSuccess {
                requireActivity().runOnUiThread {
                    if (_binding != null) binding.disconnectProgressBtn.revertAnimation()
                }
                showConnectUserFragment()
            }

            response.onFailure {e ->
                requireActivity().runOnUiThread {
                    if (_binding != null) binding.disconnectProgressBtn.revertAnimation()
                }
                if (e is UnknownHostException) {
                    Utilities.makeWarningSnack(
                        activity,
                        requireParentFragment().requireView(),
                        "به دلیل عدم اتصال به اینترنت ، هم رسانی تسک ها صورت نگرفت."
                    )
                }
                showConnectUserFragment()
            }

        }

        job.invokeOnCompletion {
            job.cancel()
        }

        viewModel.saveConnectedPhone("")
    }

    private fun showConnectUserFragment() {
        val fragmentContainerView =
            activity.findViewById<FragmentContainerView>(R.id.manage_user_connection_container)
        val fragmentManager = parentFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(fragmentContainerView.id, ConnectUserFragment())
        transaction.commit()
    }

    override fun onFailure(errorCode: Int) {
        binding.disconnectProgressBtn.revertAnimation()
        when (errorCode) {
            CONNECTION_ERROR_CODE -> {
                Utilities.makeWarningSnack(
                    activity,
                    binding.root,
                    "مشکل در اتصال به اینترنت ، لطفا از اتصال خود مطمئن شوید."
                )
            }

            403 -> {
                Utilities.makeWarningSnack(
                    activity,
                    binding.root,
                    "مشکلی در فرآیند ورودتان به برنامه پیش آمده"
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}

interface DisconnectUserCallback {
    fun onSuccess()
    fun onFailure(errorCode: Int)
}