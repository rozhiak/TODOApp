package com.rmblack.todoapp.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.models.server.requests.AddTaskRequest
import com.rmblack.todoapp.models.server.requests.DeleteTaskRequest
import com.rmblack.todoapp.models.server.requests.EditTaskRequest
import com.rmblack.todoapp.utils.SharedPreferencesManager
import com.rmblack.todoapp.webservice.repository.ApiRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

open class TasksViewModel(val sharedPreferencesManager: SharedPreferencesManager) : ViewModel() {

    private val apiRepository = ApiRepository()

    val taskRepository = TaskRepository.get()

    protected val _tasks: MutableStateFlow<List<Task?>> = MutableStateFlow(emptyList())

    val tasks: StateFlow<List<Task?>>
        get() = _tasks.asStateFlow()

    protected val _detailsVisibility: ArrayList<Boolean> = ArrayList()

    val detailsVisibility: List<Boolean>
        get() = _detailsVisibility.toList()

    //Server properties
    private var addJob: Job? = null

    private var editJob: Job? = null

    private var deleteJob : Job? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, _ ->
//        loading.value = false
    }

    private val loading = MutableLiveData<Boolean>()
    //End of server properties


    fun updateUrgentState(isUrgent: Boolean, id: UUID) {
        taskRepository.updateUrgentState(isUrgent, id)
    }

    fun updateDoneState(isDone: Boolean, id: UUID) {
        taskRepository.updateDoneState(isDone, id)
    }

    private fun updateServerID(id: UUID, serverID: String) {
        taskRepository.updateServerID(id, serverID)
    }

    fun updateVisibility(index: Int, visibility: Boolean) {
        if (index < _detailsVisibility.size) _detailsVisibility[index] = visibility
    }

    fun insertVisibility(pos: Int, b: Boolean) {
        //If there had been a date lable before deleted task, the visibility for lable
        // is deleted so it is needed to add false to reach to the desired size
        while (pos > _detailsVisibility.size) {
            _detailsVisibility.add(false)
        }
        _detailsVisibility.add(pos, b)
    }

    fun deleteVisibility(pos: Int) {
        if (pos in detailsVisibility.indices) _detailsVisibility.removeAt(pos)
    }

    fun deleteTask(task: Task?, position: Int) {
        //Extra deletion is for date labels
        _detailsVisibility.removeAt(position)
        if (position + 1 < tasks.value.size) {
            if (tasks.value[position - 1] == null && tasks.value[position + 1] == null) {
                _detailsVisibility.removeAt(position - 1)
            }
        } else {
            if (tasks.value[position - 1] == null) {
                _detailsVisibility.removeAt(position - 1)
            }
        }

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
        val user = sharedPreferencesManager.getUser()
        if (user != null) {
            val addRequest = AddTaskRequest(
                user.token,
                task.title,
                task.addedTime.timeInMillis.toString(),
                task.description,
                task.deadLine.timeInMillis.toString(),
                task.isUrgent,
                task.isDone,
                task.isShared
            )

            addJob = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
                val response = apiRepository.addNewTask(addRequest)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        loading.value = false
                        response.body()?.data?.id?.let { updateServerID(task.id, it) }
                    } else {
                        if (response.code() == 403) {
                            //invalid token
                        }
                    }
                }
            }
        }
    }

    fun editTaskInServer(task: Task) {
        val user = sharedPreferencesManager.getUser()
        if (user != null) {
            val editRequest = EditTaskRequest(
                user.token,
                task.serverID,
                task.title,
                task.deadLine.timeInMillis.toString(),
                task.isUrgent,
                task.isDone,
                task.isShared
            )

            editJob = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
                val response = apiRepository.editTask(editRequest)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        loading.value = false
                    } else {
                        if (response.code() == 403) {
                            //invalid token or access denied
                        } else if (response.code() == 404) {
                            //Task not found
                        }
                    }
                }
            }
        }
    }

    fun deleteTaskFromServer(serverID: String) {
        val user = sharedPreferencesManager.getUser()
        if (user != null) {
            val deleteRequest = DeleteTaskRequest(
                user.token,
                serverID
            )
            deleteJob = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
                val response = apiRepository.deleteTask(deleteRequest)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        loading.value = false
                    } else if (response.code() == 403) {
                        //Invalid token or access denied
                    } else if (response.code() == 404) {
                        //task not found
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        addJob?.cancel()
        editJob?.cancel()
        deleteJob?.cancel()
    }

}