package com.rmblack.todoapp.viewmodels

import androidx.lifecycle.ViewModel
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.server.requests.ConnectUserRequest
import com.rmblack.todoapp.models.server.requests.DisconnectUserRequest
import com.rmblack.todoapp.utils.SharedPreferencesManager
import com.rmblack.todoapp.webservice.repository.ApiRepository

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
    }

    fun disconnectUserFromSharedList() {
        val disconnectUserRequest = sharedPreferencesManager.getUser()?.token?.let {token ->
            DisconnectUserRequest(
                token
            )
        }
    }

}