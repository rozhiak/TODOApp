package com.rmblack.todoapp.viewmodels

import androidx.lifecycle.ViewModel
import com.rmblack.todoapp.models.server.requests.LoginRequest
import com.rmblack.todoapp.models.server.requests.NewUserRequest
import com.rmblack.todoapp.models.server.requests.ValidateUserRequest
import com.rmblack.todoapp.models.server.success.ValidateUserResponse
import com.rmblack.todoapp.utils.SharedPreferencesManager
import com.rmblack.todoapp.webservice.repository.ApiRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Response

class LoginViewModel(private val sharedPreferencesManager: SharedPreferencesManager): ViewModel() {

    private val customScope = CoroutineScope(Dispatchers.IO)

    private val apiRepository: ApiRepository = ApiRepository()

    private var bottomIcVisibility = true

    var _loginRequestCode = MutableStateFlow(0)

    val loginRequestCode : StateFlow<Int>
        get() = _loginRequestCode.asStateFlow()

    var _newUserRequestCode = MutableStateFlow(0)

    val newUserRequestCode : StateFlow<Int>
        get() = _newUserRequestCode.asStateFlow()

    private var _verifyingPhone = ""

    val verifyingPhone
        get() = _verifyingPhone

    fun loginUser(phoneNumber: String) {
        val loginRequest = LoginRequest(phoneNumber)

        customScope.launch {
            val response = apiRepository.loginUser(loginRequest)
            if (response.code() == 200) {
                _verifyingPhone = phoneNumber
            }
            _loginRequestCode.update {
                response.code()
            }
        }
    }

    fun newUser(phoneNumber: String, name: String) {
        val newUserRequest = NewUserRequest(
            phoneNumber,
            name
        )

        customScope.launch {
            val response = apiRepository.newUser(newUserRequest)
            if (response.code() == 201) {
                _verifyingPhone = phoneNumber
            }
            _newUserRequestCode.update {
                response.code()
            }
        }
    }

    suspend fun validateUser(code: String): Boolean {
        val validateUserRequest = ValidateUserRequest(
            verifyingPhone,
            code.toInt()
        )

        val response = apiRepository.validateUser(validateUserRequest)
        if (response.code() == 200) {
            //TODO: sync tasks with server - merge all tasks
            saveUserInSharedPreferences(response)
            changeEntranceState(true)
            return true
        } else {
            //Not found
        }
        return false
    }

    fun changeEntranceState(state: Boolean) {
        sharedPreferencesManager.saveEntranceState(state)
    }

    private fun saveUserInSharedPreferences(
        response: Response<ValidateUserResponse>
    ) {
        response.body()?.user?.let { sharedPreferencesManager.saveUser(it) }
    }

    fun resetLoginRequestCode() {
        _loginRequestCode.value = 0
    }

    fun resetNewUserRequestCode() {
        _newUserRequestCode.value = 0
    }

    fun getBottomICVisibility() : Boolean {
        return bottomIcVisibility
    }

    fun setBottomICVisibility(visibility: Boolean) {
        bottomIcVisibility = visibility
    }

    override fun onCleared() {
        super.onCleared()
        customScope.cancel() // Cancel the coroutine scope when the ViewModel is cleared
    }

}