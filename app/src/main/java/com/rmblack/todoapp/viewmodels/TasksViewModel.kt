package com.rmblack.todoapp.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.models.server.requests.AddTaskRequest
import com.rmblack.todoapp.models.server.requests.DeleteTaskRequest
import com.rmblack.todoapp.models.server.requests.EditTaskRequest
import com.rmblack.todoapp.models.server.success.User
import com.rmblack.todoapp.utils.SharedPreferencesManager
import com.rmblack.todoapp.webservice.repository.ApiRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.UUID

open class TasksViewModel(application: Application) : ViewModel() {

    val sharedPreferencesManager = SharedPreferencesManager(application)

    private val apiRepository = ApiRepository()

    val taskRepository = TaskRepository.get()

    protected val _tasks: MutableStateFlow<List<Task?>> = MutableStateFlow(listOf(null))
    val tasks: StateFlow<List<Task?>>
        get() = _tasks.asStateFlow()

    private var _lastExpandedID: UUID? = null

    val lastExpandedID
        get() = _lastExpandedID

    //Server properties
    private var addJob: Job? = null

    private var editJob: Job? = null

    private var deleteJob: Job? = null
    //End of server properties

    fun setPreviouslyExpandedID(id: UUID?) {
        _lastExpandedID = id
    }

    fun updateDetailsVisibility(isVisible: Boolean, id: UUID) {
        taskRepository.updateDetailsVisibility(id, isVisible)
    }

    fun updateUrgentState(isUrgent: Boolean, id: UUID) {
        taskRepository.updateUrgentState(isUrgent, id)
    }

    fun updateDoneState(isDone: Boolean, id: UUID) {
        taskRepository.updateDoneState(isDone, id)
    }

    private fun updateServerID(id: UUID, serverID: String) {
        taskRepository.updateServerID(id, serverID)
    }

    fun collapseExpandedTask() {
        for (t in tasks.value) {
            if (t?.detailsVisibility == true) {
                taskRepository.updateDetailsVisibility(t.id, false)
                setPreviouslyExpandedID(t.id)
                return
            }
        }
        setPreviouslyExpandedID(null)
    }

    fun reopenLastExpandedTask() {
        lastExpandedID?.let { id -> taskRepository.updateDetailsVisibility(id, true) }
    }

    fun deleteTask(task: Task?) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
        }
    }

    fun insertTask(task: Task) {
        viewModelScope.launch {
            taskRepository.addTask(task)
        }
    }

    fun addTaskToServer(task: Task) {
        val user = getUser()
        if (user?.token != null) {
            val addRequest = AddTaskRequest(
                user.token,
                task.title,
                task.addedTime.timeInMillis.toString(),
                task.description,
                task.deadLine.timeInMillis.toString(),
                task.isUrgent,
                task.isDone,
                task.isShared,
                task.id
            )
            sharedPreferencesManager.insertCashedAddRequest(addRequest)
            sendAddRequest(addRequest, task)
        }
    }

    private fun sendAddRequest(
        addRequest: AddTaskRequest, task: Task
    ) {
        addJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiRepository.addNewTask(addRequest.convertToServerAddModel())
                if (response.isSuccessful) {
                    response.body()?.data?.id?.let { updateServerID(task.id, it) }
                    sharedPreferencesManager.removeCashedAddRequest(addRequest)
                } else {
                    if (response.code() == 403) {
                        //invalid token
                        sharedPreferencesManager.removeCashedAddRequest(addRequest)
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun editTaskInServer(editedTask: Task) {
        val addRequests = sharedPreferencesManager.getCashedAddRequests()
        val index = addRequests.indexOfFirst { req ->
            req.localTaskID == editedTask.id
        }
        if (index != -1) {
            // Related add request found
            sharedPreferencesManager.removeCashedAddRequest(addRequests[index])
            addTaskToServer(editedTask)
        } else {
            val user = getUser()
            if (user?.token != null) {
                val editRequests = sharedPreferencesManager.getCashedEditRequests()
                val editIndex = editRequests.indexOfFirst { req ->
                    req.localTaskId == editedTask.id
                }
                val editRequest = if (editIndex == -1) {
                    val req = EditTaskRequest(
                        user.token,
                        editedTask.serverID,
                        editedTask.title,
                        editedTask.description,
                        editedTask.deadLine.timeInMillis.toString(),
                        editedTask.isUrgent,
                        editedTask.isDone,
                        editedTask.isShared,
                        editedTask.id
                    )
                    sharedPreferencesManager.insertCashedEditRequest(req)
                    req
                } else {
                    val editReq = editRequests[editIndex]
                    val newEditReq = editReq.copy(
                        title = editedTask.title,
                        description = editedTask.description,
                        deadline = editedTask.deadLine.timeInMillis.toString(),
                        isShared = editedTask.isShared,
                        isDone = editedTask.isDone,
                        isUrgent = editedTask.isUrgent,
                    )
                    sharedPreferencesManager.removeCashedEditRequest(editReq)
                    sharedPreferencesManager.insertCashedEditRequest(newEditReq)
                    newEditReq
                }

                sendEditRequest(editRequest)
            }
        }
    }

    private fun sendEditRequest(editRequest: EditTaskRequest) {
        editJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiRepository.editTask(editRequest.convertToServerEditModel())
                if (response.isSuccessful) {
                    sharedPreferencesManager.removeCashedEditRequest(editRequest)
                } else {
                    if (response.code() == 403) {
                        //invalid token or access denied
                        sharedPreferencesManager.removeCashedEditRequest(editRequest)
                    } else if (response.code() == 404) {
                        //Task not found
                        sharedPreferencesManager.removeCashedEditRequest(editRequest)
                    }
                }
            } catch (_: Exception) {

            }
        }
    }

    fun deleteTaskFromServer(deleteRequest: DeleteTaskRequest, taskToDelete: Task) {
        val user = getUser()
        if (user?.token != null) {
            val addRequests = sharedPreferencesManager.getCashedAddRequests()
            val addIndex = addRequests.indexOfFirst { req ->
                req.localTaskID == taskToDelete.id
            }
            if (addIndex != -1) {
                sharedPreferencesManager.removeCashedAddRequest(addRequests[addIndex])
            } else {
                sendDeleteRequest(deleteRequest)
            }

            val editRequests = sharedPreferencesManager.getCashedEditRequests()
            for (req in editRequests) {
                if (req.localTaskId == taskToDelete.id) {
                    sharedPreferencesManager.removeCashedEditRequest(req)
                }
            }
        }
    }

    private fun sendDeleteRequest(deleteRequest: DeleteTaskRequest) {
        deleteJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiRepository.deleteTask(deleteRequest)
                if (response.isSuccessful) {
                    removeDeleteRequest(deleteRequest)
                } else if (response.code() == 403) {
                    //Invalid token or access denied
                    removeDeleteRequest(deleteRequest)
                } else if (response.code() == 404) {
                    //task not found
                    removeDeleteRequest(deleteRequest)
                }
            } catch (_: Exception) {

            }
        }
    }

    fun cashDeleteRequest(deleteRequest: DeleteTaskRequest) {
        sharedPreferencesManager.insertCashedDeleteRequest(deleteRequest)
    }

    fun makeDeleteRequest(serverID: String): DeleteTaskRequest? {
        val user = getUser()
        if (user?.token != null) {
            return DeleteTaskRequest(
                user.token, serverID
            )
        }
        return null
    }

    fun removeDeleteRequest(deleteRequest: DeleteTaskRequest) {
        sharedPreferencesManager.removeCashedDeleteRequest(deleteRequest)
    }

    fun getUserToken(): String? {
        val user = getUser()
        return user?.token
    }

    fun getUser(): User? {
        return sharedPreferencesManager.getUser()
    }

    fun getConnectedPhones(): List<String>? {
        return sharedPreferencesManager.getConnectedPhone()
    }

    suspend fun syncConnectedPhonesWithServer() {
        val receivedPhones = getConnectedPhonesFromServer()
        if (receivedPhones != null) {
            val connectedPhones = getConnectedPhones()
            if (connectedPhones != null) {
                if (connectedPhones != receivedPhones) {
                    saveConnectedPhonesInSP(receivedPhones)
                }
            } else if (receivedPhones.isNotEmpty()) {
                saveConnectedPhonesInSP(receivedPhones)
            }
        }
    }

    private suspend fun getConnectedPhonesFromServer(): List<String>? {
        if (getUserToken() != null) {
            return try {
                val response = apiRepository.getConnectedPhones(getUserToken()!!)
                if (response.isSuccessful) {
                    response.body()?.data
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }
        }
        return null
    }

    private fun saveConnectedPhonesInSP(phones: List<String>) {
        sharedPreferencesManager.saveConnectedPhones(phones)
    }

    fun updateTasks(tasks: List<Task>) {
        val tasksWithDatePositionNull = mutableListOf<Task?>()

        if (tasks.isNotEmpty()) {
            val sortedTasks = tasks.sortedBy { it.deadLine }
            tasksWithDatePositionNull.add(null)
            tasksWithDatePositionNull.add(sortedTasks[0]) //Because of if condition we can not access i - 1 position (-1)
            for (i in 1 until sortedTasks.size) {
                if (sortedTasks[i].deadLine.shortDateString != sortedTasks[i - 1].deadLine.shortDateString) {
                    tasksWithDatePositionNull.add(null)
                }
                tasksWithDatePositionNull.add(sortedTasks[i])
            }
        }

        _tasks.value = tasksWithDatePositionNull.toList()
    }

    override fun onCleared() {
        addJob?.cancel()
        editJob?.cancel()
        deleteJob?.cancel()
        super.onCleared()
    }

}