package com.rmblack.todoapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.fragments.ConnectUserCallback
import com.rmblack.todoapp.fragments.DisconnectUserCallback
import com.rmblack.todoapp.models.server.requests.ConnectUserRequest
import com.rmblack.todoapp.models.server.requests.DisconnectUserRequest
import com.rmblack.todoapp.utils.SharedPreferencesManager
import com.rmblack.todoapp.utils.Utilities
import com.rmblack.todoapp.webservice.repository.ApiRepository
import kotlinx.coroutines.launch
import java.lang.Exception
import java.net.UnknownHostException

const val CONNECTION_ERROR_CODE = 0

class ConnectUserViewModel(
    private val sharedPreferencesManager: SharedPreferencesManager
) : ViewModel() {

    private val apiRepository = ApiRepository()

    fun connectUserToSharedList(phoneNumber: String, connectUserCallback: ConnectUserCallback) {
        val connectUserRequest = sharedPreferencesManager.getUser()?.token?.let {token ->
            ConnectUserRequest(
                token,
                phoneNumber
            )
        }

        viewModelScope.launch {
            if (connectUserRequest != null) {
                try {
                    val response = apiRepository.connectUser(connectUserRequest)
                    if (response.isSuccessful) {
                        connectUserCallback.onConnectUserSuccess()
                    } else {
                        connectUserCallback.onConnectUserFailure(response.code())
                    }
                } catch (e: Exception) {
                    if (e is UnknownHostException) {
                        connectUserCallback.onConnectUserFailure(0)
                    }
                }
            }
        }
    }

    fun disconnectUserFromSharedList(callback: DisconnectUserCallback) {
        val disconnectUserRequest = sharedPreferencesManager.getUser()?.token?.let {token ->
            DisconnectUserRequest(
                token
            )
        }

        viewModelScope.launch {
            if (disconnectUserRequest != null) {
                try {
                    val response = apiRepository.disconnectUser(disconnectUserRequest)
                    if (response.isSuccessful) {
                    } else {
                    }
                } catch (e:Exception) {

                }
            }
        }
    }

    fun getUserToken() : String {
        return sharedPreferencesManager.getUser()?.token ?: ""
    }

    fun saveConnectedPhone(phone: String) {
        sharedPreferencesManager.saveConnectedPhone(phone)
    }

    fun getConnectedPhone(): String {
        return sharedPreferencesManager.getConnectedPhone() ?: ""
    }

}