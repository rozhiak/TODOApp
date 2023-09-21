package com.rmblack.todoapp.viewmodels

import androidx.lifecycle.ViewModel
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.models.server.requests.AddTaskRequest
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
import java.lang.Exception
import java.util.UUID

class LoginViewModel(private val sharedPreferencesManager: SharedPreferencesManager): ViewModel() {

    private val customScope = CoroutineScope(Dispatchers.IO)

    val taskRepository = TaskRepository.get()

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

    private var _loginFragmentLoading = MutableStateFlow<Boolean>(false)

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
            val response = apiRepository.loginUser(loginRequest)
            updateLoginLoadingState(false)
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
            updateLoginLoadingState(false)
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
        updateVerificationLoadingState(false)
        if (response.code() == 200) {
            response.body()?.user?.token?.let { syncTasksWithServer(it) }
            saveUserInSharedPreferences(response)
            changeEntranceState(true)
            return true
        } else {
            //Not found
        }
        return false
    }

    private suspend fun syncTasksWithServer(token: String) {
//        val allServerTasks = token.let { apiRepository.getAllTasks(it).body()?.data }
        val privateLocalTasks = taskRepository.getPrivateTasks().toList()
//        val sharedLocalTasks = taskRepository.getSharedTasks().toList()

//        if (allServerTasks != null) {
//            for(pTask in allServerTasks.private) {
//                if (!checkIfContains(privateLocalTasks, pTask.id)) {
//                    val task = pTask.convertToLocalTask()
//                    taskRepository.addTask(task)
//                }
//            }
//            for(sTask in allServerTasks.shared) {
//                if (!checkIfContains(sharedLocalTasks, sTask.id)) {
//                    val task = sTask.convertToLocalTask()
//                    taskRepository.addTask(task)
//                }
//            }
//        }

        for (pTask in privateLocalTasks) {
            if (pTask.serverID == "") {
                val addRequest = AddTaskRequest(
                    token,
                    pTask.title,
                    pTask.addedTime.timeInMillis.toString(),
                    pTask.description,
                    pTask.deadLine.timeInMillis.toString(),
                    pTask.isUrgent,
                    pTask.isDone,
                    pTask.isShared
                )

                try {
                    val response = apiRepository.addNewTask(addRequest)
                    if (response.isSuccessful) {
                        response.body()?.data?.id?.let { updateServerID(pTask.id, it) }
                    } else {
                        if (response.code() == 403) {
                            //invalid token
                        }
                    }
                } catch (e: Exception) {
                    sharedPreferencesManager.insertFailedAddRequest(addRequest)
                }
            }
        }
    }

    private fun updateServerID(id: UUID, serverID: String) {
        taskRepository.updateServerID(id, serverID)
    }

//    private fun checkIfContains(localTasks: List<Task>, serverID: String): Boolean {
//        for (eachTask in localTasks) {
//            if (serverID == eachTask.serverID) {
//                return true
//            }
//        }
//        return false
//    }

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