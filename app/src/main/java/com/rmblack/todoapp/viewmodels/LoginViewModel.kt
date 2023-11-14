package com.rmblack.todoapp.viewmodels

import androidx.lifecycle.ViewModel
import com.rmblack.todoapp.activities.newlyAddedTaskServerID
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.server.requests.AddTaskRequest
import com.rmblack.todoapp.models.server.requests.LoginRequest
import com.rmblack.todoapp.models.server.requests.NewUserRequest
import com.rmblack.todoapp.models.server.requests.ValidateUserRequest
import com.rmblack.todoapp.models.server.success.UserResponse
import com.rmblack.todoapp.utils.CONNECTION_ERROR_CODE
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
import java.lang.Exception
import java.net.UnknownHostException
import java.util.UUID

class LoginViewModel(private val sharedPreferencesManager: SharedPreferencesManager) : ViewModel() {

    private val customScope = CoroutineScope(Dispatchers.IO)

    private val taskRepository = TaskRepository.get()

    private val apiRepository: ApiRepository = ApiRepository()

    private var bottomIcVisibility = true

    private var _loginRequestCode = MutableStateFlow(-1)

    val loginRequestCode: StateFlow<Int>
        get() = _loginRequestCode.asStateFlow()

    private var _verifyRequestCode = MutableStateFlow(-1)

    val verifyRequestCode: StateFlow<Int>
        get() = _verifyRequestCode.asStateFlow()

    private var _newUserRequestCode = MutableStateFlow(-1)

    val newUserRequestCode: StateFlow<Int>
        get() = _newUserRequestCode.asStateFlow()

    private var _verifyingPhone = ""

    private val verifyingPhone
        get() = _verifyingPhone

    private var _loginFragmentLoading = MutableStateFlow(false)

    val loginFragmentLoading: StateFlow<Boolean>
        get() = _loginFragmentLoading.asStateFlow()

    private var _verificationFragmentLoading = MutableStateFlow(false)

    val verificationFragmentLoading: StateFlow<Boolean>
        get() = _verificationFragmentLoading.asStateFlow()

    fun updateLoginLoadingState(isLoading: Boolean) {
        _loginFragmentLoading.update {
            isLoading
        }
    }

    fun updateVerificationLoadingState(isLoading: Boolean) {
        _verificationFragmentLoading.update {
            isLoading
        }
    }

    fun loginUser(phoneNumber: String) {
        val loginRequest = LoginRequest(phoneNumber)

        customScope.launch {
            try {
                val response = apiRepository.loginUser(loginRequest)
                if (response.code() == 200) {
                    _verifyingPhone = phoneNumber
                }
                _loginRequestCode.update {
                    response.code()
                }
                updateLoginLoadingState(false)
            } catch (e: Exception) {
                if (e is UnknownHostException) {
                    _loginRequestCode.update {
                        CONNECTION_ERROR_CODE
                    }
                }
                updateLoginLoadingState(false)
            }
        }
    }

    fun newUser(phoneNumber: String, name: String) {
        val newUserRequest = NewUserRequest(
            phoneNumber,
            name
        )

        customScope.launch {
            try {
                val response = apiRepository.newUser(newUserRequest)
                if (response.code() == 201) {
                    _verifyingPhone = phoneNumber
                }
                _newUserRequestCode.update {
                    response.code()
                }
                updateLoginLoadingState(false)
            } catch (e: Exception) {
                if (e is UnknownHostException) {
                    _newUserRequestCode.update {
                        CONNECTION_ERROR_CODE
                    }
                }
                updateLoginLoadingState(false)
            }
        }
    }

    fun validateUser(code: String) {
        val validateUserRequest = ValidateUserRequest(
            verifyingPhone,
            code.toInt()
        )

        customScope.launch {
            try {
                val response = apiRepository.validateUser(validateUserRequest)
                if (response.code() == 200) {
                    response.body()?.user?.token?.let { syncTasksWithServerOnValidate(it) }
                    saveUserInSharedPreferences(response)
                    response.body()?.user?.connectedPhones?.let { phones ->
                        if (phones.isNotEmpty()) {
                            sharedPreferencesManager.saveConnectedPhones(
                                phones
                            )
                        }
                    }
                    changeEntranceState(true)
                }
                _verifyRequestCode.update {
                    response.code()
                }
                updateVerificationLoadingState(false)
            } catch (e: Exception) {
                if (e is UnknownHostException) {
                    _verifyRequestCode.update {
                        CONNECTION_ERROR_CODE
                    }
                }
                updateVerificationLoadingState(false)
            }
        }
    }

    private suspend fun syncTasksWithServerOnValidate(token: String) {
        val privateLocalTasks = taskRepository.getPrivateTasks().toList()

        for (pTask in privateLocalTasks) {
            if (pTask.serverID == newlyAddedTaskServerID) {
                val addRequest = AddTaskRequest(
                    token,
                    pTask.title,
                    pTask.addedTime.timeInMillis.toString(),
                    pTask.description,
                    pTask.deadLine.timeInMillis.toString(),
                    pTask.isUrgent,
                    pTask.isDone,
                    pTask.isShared,
                    pTask.id
                )

                try {
                    val response = apiRepository.addNewTask(addRequest.convertToServerAddModel())
                    if (response.isSuccessful) {
                        response.body()?.data?.id?.let { updateServerID(pTask.id, it) }
                    }
                } catch (e: Exception) {
                    sharedPreferencesManager.insertCashedAddRequest(addRequest)
                }
            }
        }
    }

    private fun updateServerID(id: UUID, serverID: String) {
        taskRepository.updateServerID(id, serverID)
    }

    fun changeEntranceState(state: Boolean) {
        sharedPreferencesManager.saveEntranceState(state)
    }

    private fun saveUserInSharedPreferences(
        response: Response<UserResponse>
    ) {
        response.body()?.user?.let { sharedPreferencesManager.saveUser(it) }
    }

    fun resetLoginRequestCode() {
        _loginRequestCode.value = -1
    }

    fun resetNewUserRequestCode() {
        _newUserRequestCode.value = -1
    }

    fun resetVerifyRequestCode() {
        _verifyRequestCode.value = -1
    }

    fun getBottomICVisibility(): Boolean {
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