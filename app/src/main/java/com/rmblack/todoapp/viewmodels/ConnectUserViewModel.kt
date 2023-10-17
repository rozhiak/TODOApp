package com.rmblack.todoapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rmblack.todoapp.fragments.ConnectUserCallback
import com.rmblack.todoapp.fragments.DisconnectUserCallback
import com.rmblack.todoapp.models.server.requests.ConnectUserRequest
import com.rmblack.todoapp.models.server.requests.DisconnectUserRequest
import com.rmblack.todoapp.utils.CONNECTION_ERROR_CODE
import com.rmblack.todoapp.utils.SharedPreferencesManager
import com.rmblack.todoapp.webservice.repository.ApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.lang.Exception
import java.net.UnknownHostException

class ConnectUserViewModel(
    private val sharedPreferencesManager: SharedPreferencesManager
) : ViewModel() {

    private val apiRepository = ApiRepository()

    private val _connectLoading : MutableStateFlow<Boolean> = MutableStateFlow(false)

    val connectLoading: StateFlow<Boolean>
        get() = _connectLoading.asStateFlow()

    private val _disconnectLoading : MutableStateFlow<Boolean> = MutableStateFlow(false)

    val disconnectLoading: StateFlow<Boolean>
        get() = _disconnectLoading.asStateFlow()

    fun setConnectLoadingState(state: Boolean) {
        _connectLoading.update {
            state
        }
    }

    fun setDisconnectLoadingState(state: Boolean) {
        _disconnectLoading.update {
            state
        }
    }

    fun connectUserToSharedList(phoneNumber: String, connectCallback: ConnectUserCallback) {
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
                        connectCallback.onConnectUserSuccess()
                    } else if(response.code() == 500){
                        val disconnectReq = DisconnectUserRequest(
                            getUserToken()
                        )
                        apiRepository.disconnectUser(disconnectReq)
                        connectUserToSharedList(phoneNumber, connectCallback)
                    } else {
                        connectCallback.onConnectUserFailure(response.code())
                    }
                } catch (e: Exception) {
                    if (e is UnknownHostException) {
                        connectCallback.onConnectUserFailure(0)
                    }
                }
            }
        }
    }

    fun disconnectUserFromSharedList(disconnectCallback: DisconnectUserCallback) {
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
                        disconnectCallback.onSuccess()
                    } else {
                        disconnectCallback.onFailure(response.code())
                    }
                } catch (e:Exception) {
                    if (e is UnknownHostException) {
                        disconnectCallback.onFailure(CONNECTION_ERROR_CODE)
                    }
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