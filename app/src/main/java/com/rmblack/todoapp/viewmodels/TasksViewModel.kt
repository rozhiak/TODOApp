package com.rmblack.todoapp.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.models.local.TaskState
import com.rmblack.todoapp.models.server.ServerTask
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

open class TasksViewModel constructor(private val apiRepository: ApiRepository) : ViewModel() {

    val taskRepository = TaskRepository.get()

    protected val _tasks: MutableStateFlow<List<Task?>> = MutableStateFlow(emptyList())

    val tasks: StateFlow<List<Task?>>
        get() = _tasks.asStateFlow()

    protected val _detailsVisibility: ArrayList<Boolean> = ArrayList()

    val detailsVisibility: List<Boolean>
        get() = _detailsVisibility.toList()

    //Server properties
    var job: Job? = null

    private val errorMessage = MutableLiveData<String>()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError("Exception handled: ${throwable.localizedMessage}")
    }

    private val loading = MutableLiveData<Boolean>()
    //End of server properties

    private fun updateTasks(onUpdate: (List<Task?>) -> List<Task?>) {
        _tasks.update { oldTasks ->
            onUpdate(oldTasks)
        }
    }

    fun updateUrgentState(isUrgent: Boolean, id: UUID, pos: Int) {
        taskRepository.updateUrgentState(isUrgent, id)

        updateTasks { oldTasks ->
            val updatedTasks = oldTasks.toMutableList()
            updatedTasks[pos] = tasks.value[pos]?.copy(isUrgent = isUrgent)
            updatedTasks
        }
    }

    fun updateDoneState(isDone: Boolean, id: UUID, pos: Int) {
        taskRepository.updateDoneState(isDone, id)

        updateTasks { oldTasks ->
            val updatedTasks = oldTasks.toMutableList()
            updatedTasks[pos] = tasks.value[pos]?.copy(isDone = isDone)
            updatedTasks
        }
    }

    fun updateTaskState(state: TaskState, id: UUID, pos: Int) {
        taskRepository.updateTaskState(state, id)

        updateTasks { oldTasks ->
            val updatedTasks = oldTasks.toMutableList()
            updatedTasks[pos] = tasks.value[pos]?.copy(state = state)
            updatedTasks
        }
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
        //user token should be used here
        val newTask = ServerTask(
            "",
            task.title,
            task.addedTime.timeInMillis.toString(),
            task.description,
            task.deadLine.timeInMillis.toString(),
            task.isUrgent,
            task.isDone,
            task.isDone
        )

        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = apiRepository.addNewTask(newTask)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    loading.value = false
                } else {
                    onError("Error : ${response.message()} ")
                }
            }
        }
    }

    private fun onError(message: String) {
        errorMessage.value = message
        loading.value = false
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }

}