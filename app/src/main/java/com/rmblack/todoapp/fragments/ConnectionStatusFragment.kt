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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.rmblack.todoapp.R
import com.rmblack.todoapp.adapters.ConnectedPhonesAdapter
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
        performCachedReq()
        setDetails()
        setClickListeners()
        setUpLoadingState()
    }

    private fun performCachedReq() {
        val cachedReq = viewModel.getCachedDisconnectRequest()
        if (cachedReq != null) {
            viewModel.setDisconnectLoadingState(true)
            viewModel.disconnectUserFromSharedList(this)
        }
    }

    private fun setUpLoadingState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.disconnectLoading.collect {
                if (it) {
                    binding.disconnectProgressBtn.startAnimation()
                } else {
                    binding.disconnectProgressBtn.revertAnimation()
                }
            }
        }
    }

    private fun setClickListeners() {
        binding.disconnectProgressBtn.setOnClickListener {
            viewModel.setDisconnectLoadingState(true)
            viewModel.disconnectUserFromSharedList(this)
        }
    }

    private fun setDetails() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.syncConnectedPhonesWithServer()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.connectedPhones.collect {phones ->
                if (phones.isEmpty()) {

                } else  {
                    val adapter = ConnectedPhonesAdapter(phones)
                    binding.listMatesRv.adapter = adapter
                    binding.listMatesRv.layoutManager = LinearLayoutManager(requireContext())
                }
            }
        }
    }

    override fun onSuccessDisconnection() {
        val job = CoroutineScope(Dispatchers.Default).launch {
            val response = Utilities.syncTasksWithServer(viewModel.getUserToken(), requireContext())
            response.onSuccess {
                requireActivity().runOnUiThread {
                    if (_binding != null) viewModel.setDisconnectLoadingState(false)
                }
                showConnectUserFragment()
            }

            response.onFailure {e ->
                requireActivity().runOnUiThread {
                    if (_binding != null) viewModel.setDisconnectLoadingState(false)
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
    }

    private fun showConnectUserFragment() {
        val fragmentContainerView =
            activity.findViewById<FragmentContainerView>(R.id.manage_user_connection_container)
        val fragmentManager = parentFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(fragmentContainerView.id, ConnectUserFragment())
        try {
            transaction.commit()
        } catch (ignored: IllegalStateException) {}
    }

    override fun onFailureDisconnection(errorCode: Int) {
        viewModel.setDisconnectLoadingState(false)
        when (errorCode) {
            CONNECTION_ERROR_CODE -> {
                Utilities.makeWarningSnack(
                    activity,
                    binding.root,
                    "مشکل در اتصال به اینترنت ، لطفا از اتصال خود مطمئن شوید."
                )
            }

            403 -> {
                viewModel.removeCachedDisconnectRequestFromSP()
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
    fun onSuccessDisconnection()
    fun onFailureDisconnection(errorCode: Int)
}