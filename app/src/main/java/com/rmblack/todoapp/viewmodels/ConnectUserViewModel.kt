package com.rmblack.todoapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rmblack.todoapp.fragments.ConnectUserCallback
import com.rmblack.todoapp.fragments.DisconnectUserCallback
import com.rmblack.todoapp.models.server.requests.ConnectUserRequest
import com.rmblack.todoapp.models.server.requests.DisconnectUserRequest
import com.rmblack.todoapp.utils.CONNECTION_ERROR_CODE
import com.rmblack.todoapp.utils.SharedPreferencesManager
import com.rmblack.todoapp.utils.UNKNOWN_ERROR_CODE
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

    private val _connectedPhones : MutableStateFlow<List<String>> =
        MutableStateFlow(sharedPreferencesManager.getConnectedPhone() ?: listOf())

    val connectedPhones: StateFlow<List<String>>
        get() = _connectedPhones.asStateFlow()

    fun setConnectedPhonesSF(phones: List<String>) {
        _connectedPhones.update { phones }
    }

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
        val cachedReq = sharedPreferencesManager.getCachedConnectRequest()
        val connectUserRequest = if (cachedReq == null) {
            val req = sharedPreferencesManager.getUser()?.token?.let {token ->
                ConnectUserRequest(
                    token,
                    phoneNumber
                )
            }
            sharedPreferencesManager.cacheConnectRequest(req)
            req
        } else {
            val req = if (cachedReq.new_phone_number == phoneNumber) {
                cachedReq
            } else {
                val copy = cachedReq.copy(new_phone_number = phoneNumber)
                sharedPreferencesManager.cacheConnectRequest(copy)
                copy
            }
            req
        }

        viewModelScope.launch {
            if (connectUserRequest != null) {
                try {
                    val response = apiRepository.connectUser(connectUserRequest)
                    if (response.isSuccessful) {
                        removeCachedConnectRequest()
                        val phones = getConnectedPhonesFromServer()
                        if (phones == null) {
                            saveConnectedPhonesInSP(listOf(connectUserRequest.new_phone_number))
                            setConnectedPhonesSF(listOf(connectUserRequest.new_phone_number))
                        } else {
                            saveConnectedPhonesInSP(phones)
                            setConnectedPhonesSF(phones)
                        }
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
                        connectCallback.onConnectUserFailure(CONNECTION_ERROR_CODE)
                    } else {
                        connectCallback.onConnectUserFailure(UNKNOWN_ERROR_CODE)
                    }
                }
            }
        }
    }

    fun removeCachedConnectRequest() {
        sharedPreferencesManager.removeConnectRequest()
    }

    fun disconnectUserFromSharedList(disconnectCallback: DisconnectUserCallback) {
        val disconnectUserRequest = sharedPreferencesManager.getUser()?.token?.let {token ->
            DisconnectUserRequest(
                token
            )
        }

        sharedPreferencesManager.cacheDisconnectRequest(disconnectUserRequest)

        viewModelScope.launch {
            if (disconnectUserRequest != null) {
                try {
                    val response = apiRepository.disconnectUser(disconnectUserRequest)
                    if (response.isSuccessful) {
                        removeConnectedPhonesFromSP()
                        setConnectedPhonesSF(listOf())
                        removeCachedDisconnectRequestFromSP()
                        disconnectCallback.onSuccessDisconnection()
                    } else {
                        disconnectCallback.onFailureDisconnection(response.code())
                    }
                } catch (e:Exception) {
                    if (e is UnknownHostException) {
                        disconnectCallback.onFailureDisconnection(CONNECTION_ERROR_CODE)
                    } else {
                        disconnectCallback.onFailureDisconnection(UNKNOWN_ERROR_CODE)
                    }
                }
            }
        }
    }

    private fun removeConnectedPhonesFromSP() {
        sharedPreferencesManager.removeConnectedPhones()
    }


    private suspend fun getConnectedPhonesFromServer(): List<String>? {
        return try {
            val response = apiRepository.getConnectedPhones(getUserToken())
            if (response.isSuccessful) {
                response.body()?.data
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun removeCachedDisconnectRequestFromSP( ) {
        sharedPreferencesManager.removeDisconnectRequest()
    }

    fun getUserToken() : String {
        return sharedPreferencesManager.getUser()?.token ?: ""
    }

    private fun saveConnectedPhonesInSP(phones: List<String>) {
        sharedPreferencesManager.saveConnectedPhones(phones)
    }

    fun getConnectedPhonesFromSP(): List<String>? {
        return sharedPreferencesManager.getConnectedPhone()
    }

    fun getCachedConnectUser(): ConnectUserRequest? {
        return sharedPreferencesManager.getCachedConnectRequest()
    }

    fun getCachedDisconnectRequest(): DisconnectUserRequest? {
        return sharedPreferencesManager.getCachedDisconnectRequest()
    }
}