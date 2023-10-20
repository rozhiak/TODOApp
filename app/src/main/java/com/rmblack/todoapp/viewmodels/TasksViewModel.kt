package com.rmblack.todoapp.viewmodels

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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.UUID

open class TasksViewModel(private val sharedPreferencesManager: SharedPreferencesManager) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)

    val isSyncing get() = _isSyncing.asStateFlow()

    fun updateSyncState(state: Boolean) {
        _isSyncing.update {
            state
        }
    }

    private val apiRepository = ApiRepository()

    val taskRepository = TaskRepository.get()

    protected val _tasks: MutableStateFlow<List<Task?>> = MutableStateFlow(listOf(null))

    val tasks: StateFlow<List<Task?>>
        get() = _tasks.asStateFlow()

    private var lastExpandedID : UUID? = null

    //Server properties
    private var addJob: Job? = null

    private var editJob: Job? = null

    private var deleteJob : Job? = null

    //End of server properties

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
                lastExpandedID = t.id
                return
            }
        }
        lastExpandedID = null
    }

    fun resetLastExpandedTask() {
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

            addJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = apiRepository.addNewTask(addRequest.convertToServerAddModel())
                    if (response.isSuccessful) {
                        response.body()?.data?.id?.let { updateServerID(task.id, it) }
                    } else {
                        if (response.code() == 403) {
                            //invalid token
                        } else {
                            sharedPreferencesManager.insertFailedAddRequest(addRequest)
                        }
                    }
                } catch (e: Exception) {
                    sharedPreferencesManager.insertFailedAddRequest(addRequest)
                }
            }
        }
    }

    fun editTaskInServer(editedTask: Task) {
        val addRequests = sharedPreferencesManager.getFailedAddRequests()
        val index = addRequests.indexOfFirst { req ->
            req.localTaskID == editedTask.id
        }
        if (index != -1) {
            sharedPreferencesManager.removeFailedAddRequest(addRequests[index])
            addTaskToServer(editedTask)
        } else {
            val user = getUser()
            if (user?.token != null) {
                val editRequests = sharedPreferencesManager.getFailedEditRequests()
                val editIndex = editRequests.indexOfFirst {req ->
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
                    sharedPreferencesManager.insertFailedEditRequest(req)
                    req
                } else {
                    val editReq = editRequests[editIndex]
                    val newEditReq = editReq.copy(
                        title = editedTask.title,
                        description = editedTask.description,
                        deadline = editedTask.deadLine.timeInMillis.toString(),
                        is_shared = editedTask.isShared,
                        is_done = editedTask.isDone,
                        is_urgent = editedTask.isUrgent,
                    )
                    sharedPreferencesManager.removeFailedEditRequest(editReq)
                    sharedPreferencesManager.insertFailedEditRequest(newEditReq)
                    newEditReq
                }

                editJob = CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = apiRepository.editTask(editRequest.convertToServerEditModel())
                        if (response.isSuccessful) {
                            sharedPreferencesManager.removeFailedEditRequest(editRequest)
                        } else {
                            if (response.code() == 403) {
                                //invalid token or access denied
                                sharedPreferencesManager.removeFailedEditRequest(editRequest)
                            } else if (response.code() == 404) {
                                //Task not found
                                sharedPreferencesManager.removeFailedEditRequest(editRequest)
                            }
                        }
                    } catch (_: Exception) {

                    }
                }
            }
        }
    }

    fun deleteTaskFromServer(deleteRequest: DeleteTaskRequest, taskToDelete: Task) {
        val user = getUser()
        if (user?.token != null) {
            val addRequests = sharedPreferencesManager.getFailedAddRequests()
            val addIndex = addRequests.indexOfFirst { req ->
                req.localTaskID == taskToDelete.id
            }
            if (addIndex != -1) {
                sharedPreferencesManager.removeFailedAddRequest(addRequests[addIndex])
            } else {
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

            val editRequests = sharedPreferencesManager.getFailedEditRequests()
            for (req in editRequests) {
                if (req.localTaskId == taskToDelete.id) {
                    sharedPreferencesManager.removeFailedEditRequest(req)
                }
            }
        }
    }

    fun cashDeleteRequest(deleteRequest: DeleteTaskRequest) {
        sharedPreferencesManager.insertFailedDeleteRequest(deleteRequest)
    }

    fun makeDeleteRequest(serverID: String): DeleteTaskRequest? {
        val user = getUser()
        if (user?.token != null) {
            return DeleteTaskRequest(
                user.token,
                serverID
            )
        }
        return null
    }

    fun removeDeleteRequest(deleteRequest: DeleteTaskRequest) {
        sharedPreferencesManager.removeFailedDeleteRequest(deleteRequest)
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

    override fun onCleared() {
        addJob?.cancel()
        editJob?.cancel()
        deleteJob?.cancel()
        super.onCleared()
    }

}