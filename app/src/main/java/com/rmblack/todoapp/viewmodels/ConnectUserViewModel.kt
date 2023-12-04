package com.rmblack.todoapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rmblack.todoapp.data.repository.TaskRepository
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
    application: Application
) : AndroidViewModel(application) {

    val sharedPreferencesManager = SharedPreferencesManager(application)

    private val taskRepository = TaskRepository.get()

    private val apiRepository = ApiRepository()

    private val _connectLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val connectLoading: StateFlow<Boolean>
        get() = _connectLoading.asStateFlow()

    private val _disconnectLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val disconnectLoading: StateFlow<Boolean>
        get() = _disconnectLoading.asStateFlow()

    private val _connectedPhones: MutableStateFlow<List<String>> =
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
        val token = getUserToken()
        token?.let {
            val connectUserRequest = ConnectUserRequest(it, phoneNumber)

            viewModelScope.launch {
                try {
                    val response = apiRepository.connectUser(connectUserRequest)
                    if (response.isSuccessful) {
                        val phones = getConnectedPhonesFromServer()
                        if (phones == null) {
                            saveConnectedPhonesInSP(listOf(connectUserRequest.new_phone_number))
                            setConnectedPhonesSF(listOf(connectUserRequest.new_phone_number))
                        } else {
                            saveConnectedPhonesInSP(phones)
                            setConnectedPhonesSF(phones)
                        }
                        connectCallback.onConnectUserSuccess()
                    } else if (response.code() == 500) {
                        val disconnectReq = DisconnectUserRequest(
                            it
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

    fun disconnectUserFromSharedList(disconnectCallback: DisconnectUserCallback) {
        val token = getUserToken()
        token?.let {
            val disconnectUserRequest = DisconnectUserRequest(it)
            viewModelScope.launch {
                try {
                    performSharedCashedRequests()
                    val response = apiRepository.disconnectUser(disconnectUserRequest)
                    if (response.isSuccessful) {
                        deleteSharedTasks()
                        removeConnectedPhonesFromSP()
                        disconnectCallback.onSuccessDisconnection()
                    } else {
                        disconnectCallback.onFailureDisconnection(response.code())
                    }
                } catch (e: Exception) {
                    if (e is UnknownHostException) {
                        disconnectCallback.onFailureDisconnection(CONNECTION_ERROR_CODE)
                    } else {
                        disconnectCallback.onFailureDisconnection(UNKNOWN_ERROR_CODE)
                    }
                }
            }
        }
    }

    private suspend fun performSharedCashedRequests() {
        val failedAddRequests = sharedPreferencesManager.getCashedAddRequests()
        val failedEditRequests = sharedPreferencesManager.getCashedEditRequests()
        val failedDeleteRequests = sharedPreferencesManager.getCashedDeleteRequests()

        for (addReq in failedAddRequests) {
            if (addReq.isShared) {
                val response = apiRepository.addNewTask(addReq.convertToServerAddModel())
                if (response.isSuccessful) {
                    sharedPreferencesManager.removeCashedAddRequest(addReq)
                }
            }
        }

        for (editReq in failedEditRequests) {
            if (editReq.isShared) {
                val response = apiRepository.editTask(editReq.convertToServerEditModel())
                if (response.code() == 200 || response.code() == 404) {
                    sharedPreferencesManager.removeCashedEditRequest(editReq)
                }
            }
        }

        for (deleteReq in failedDeleteRequests) {
            val response = apiRepository.deleteTask(deleteReq)
            if (response.code() == 200 || response.code() == 404) {
                sharedPreferencesManager.removeCashedDeleteRequest(deleteReq)
            }
        }
    }

    private fun removeConnectedPhonesFromSP() {
        sharedPreferencesManager.removeConnectedPhones()
    }


    private suspend fun getConnectedPhonesFromServer(): List<String>? {
        return try {
            val token = getUserToken()
            if (token == null) {
                return null
            } else {
                val response = apiRepository.getConnectedPhones(token)
                if (response.isSuccessful) {
                    response.body()?.data
                } else {
                    null
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    fun getUserToken(): String? {
        return sharedPreferencesManager.getUser()?.token
    }

    private fun saveConnectedPhonesInSP(phones: List<String>) {
        sharedPreferencesManager.saveConnectedPhones(phones)
    }

    fun getConnectedPhonesFromSP(): List<String>? {
        return sharedPreferencesManager.getConnectedPhone()
    }

    private fun deleteSharedTasks() {
        taskRepository.deleteSharedTasks()
    }
}