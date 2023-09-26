package com.rmblack.todoapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.server.requests.ConnectUserRequest
import com.rmblack.todoapp.models.server.requests.DisconnectUserRequest
import com.rmblack.todoapp.utils.SharedPreferencesManager
import com.rmblack.todoapp.webservice.repository.ApiRepository
import kotlinx.coroutines.launch
import java.lang.Exception

class ConnectUserViewModel(
    private val sharedPreferencesManager: SharedPreferencesManager
) : ViewModel() {

    private val apiRepository = ApiRepository()

    private val taskRepository = TaskRepository.get()

    fun connectUserToSharedList(phoneNumber: String) {

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
                    } else {

                    }
                } catch (e: Exception) {

                }
            }
        }
    }

    fun disconnectUserFromSharedList() {
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