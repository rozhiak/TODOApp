package com.rmblack.todoapp.utils

import android.app.Activity
import android.graphics.Color
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.aminography.primecalendar.persian.PersianCalendar
import com.google.android.material.behavior.SwipeDismissBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import com.rmblack.todoapp.R
import com.rmblack.todoapp.activities.newlyAddedTaskServerID
import com.rmblack.todoapp.alarm.AlarmScheduler
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.models.server.success.Tasks
import com.rmblack.todoapp.webservice.repository.ApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.lang.Exception

const val CONNECTION_ERROR_CODE = 0

const val UNKNOWN_ERROR_CODE = 7

class Utilities {

    object SharedObject {
        private val _isSyncing: MutableStateFlow<Boolean> = MutableStateFlow(false)
        val isSyncing: StateFlow<Boolean>
            get() = _isSyncing

        fun setSyncingState(isSyncing: Boolean) {
            _isSyncing.value = isSyncing
        }
    }

    companion object {
        fun calculateDateDistance(calendarDate: PersianCalendar): Double {
            val currentDate = PersianCalendar()
            val targetDate = PersianCalendar()

            targetDate.timeInMillis = calendarDate.timeInMillis

            currentDate.hourOfDay = 0
            targetDate.hourOfDay = 0
            currentDate.minute = 0
            targetDate.minute = 0
            currentDate.second = 0
            targetDate.second = 0
            currentDate.millisecond = 0
            targetDate.millisecond = 0

            val dateInMillis = calendarDate.timeInMillis
            val currentDateInMillis = currentDate.timeInMillis

            val millisInDay = 1000.0 * 60.0 * 60.0 * 24.0
            val distanceInMillis = dateInMillis - currentDateInMillis

            return distanceInMillis / millisInDay
        }

        fun makeDeleteSnackBar(activity: Activity, container: View, onUndo: () -> Unit): Snackbar {
            val snackBar = Snackbar.make(container, "", Snackbar.LENGTH_LONG)
            val customSnackView: View = activity.layoutInflater.inflate(R.layout.delete_snack, null)
            snackBar.view.setBackgroundColor(Color.TRANSPARENT)
            val snackBarLayout = snackBar.view as SnackbarLayout
            snackBarLayout.setPadding(0, 0, 0, 0)
            val undo: AppCompatImageView = customSnackView.findViewById(R.id.undo)
            undo.setOnClickListener {
                onUndo()
                snackBar.dismiss()
            }
            snackBarLayout.addView(customSnackView, 0)
            val behavior = BaseTransientBottomBar.Behavior().apply {
                setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_ANY)
            }
            snackBar.behavior = behavior
            val fab = activity.findViewById<FloatingActionButton>(R.id.fab)
            snackBar.anchorView = fab
            snackBar.show()
            return snackBar
        }

        fun makeWarningSnack(activity: Activity, container: View, warningMsg: String): Snackbar {
            val snackBar = Snackbar.make(container, "", Snackbar.LENGTH_LONG)
            val customSnackView: View =
                activity.layoutInflater.inflate(R.layout.warning_snack, null)
            val tvWarning = customSnackView.findViewById<AppCompatTextView>(R.id.tv_warning)
            tvWarning.text = warningMsg
            snackBar.view.setBackgroundColor(Color.TRANSPARENT)
            val snackBarLayout = snackBar.view as SnackbarLayout
            snackBarLayout.setPadding(0, 0, 0, 0)
            snackBarLayout.addView(customSnackView, 0)
            val behavior = BaseTransientBottomBar.Behavior().apply {
                setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_ANY)
            }
            snackBar.behavior = behavior
            val fab = activity.findViewById<FloatingActionButton>(R.id.fab)
            snackBar.anchorView = fab
            snackBar.show()
            return snackBar
        }

        // before using this function , check user login state
        suspend fun syncTasksWithServer(
            token: String,
            sharedPreferencesManager: SharedPreferencesManager,
            alarmScheduler: AlarmScheduler
        ): Result<Unit> {
            val apiRepository = ApiRepository()
            val taskRepository = TaskRepository.get()

            var cashedAddRequests = sharedPreferencesManager.getCashedAddRequests()
            var cashedEditRequests = sharedPreferencesManager.getCashedEditRequests()
            var cashedDeleteRequests = sharedPreferencesManager.getCashedDeleteRequests()

            for (addReq in cashedAddRequests) {
                try {
                    val response = apiRepository.addNewTask(addReq.convertToServerAddModel())
                    if (response.isSuccessful) {
                        sharedPreferencesManager.removeCashedAddRequest(addReq)
                        response.body()?.data?.id?.let {
                            taskRepository.updateServerID(addReq.localTaskID, it)
                        }
                    }
                } catch (e: Exception) {
                    return Result.failure(e)
                }
            }

            for (editReq in cashedEditRequests) {
                try {
                    val response = apiRepository.editTask(editReq.convertToServerEditModel())
                    if (response.code() == 200 || response.code() == 404) {
                        sharedPreferencesManager.removeCashedEditRequest(
                            editReq
                        )
                    }
                } catch (e: Exception) {
                    return Result.failure(e)
                }
            }

            for (deleteReq in cashedDeleteRequests) {
                try {
                    val response = apiRepository.deleteTask(deleteReq)
                    if (response.code() == 200 || response.code() == 404) {
                        sharedPreferencesManager.removeCashedDeleteRequest(
                            deleteReq
                        )
                    }
                } catch (e: Exception) {
                    return Result.failure(e)
                }
            }

            cashedAddRequests = sharedPreferencesManager.getCashedAddRequests()
            cashedEditRequests = sharedPreferencesManager.getCashedEditRequests()
            cashedDeleteRequests = sharedPreferencesManager.getCashedDeleteRequests()

            var isThereAnyFailedRequest = false
            if (cashedAddRequests.isNotEmpty()) {
                isThereAnyFailedRequest = true
            } else if (cashedEditRequests.isNotEmpty()) {
                isThereAnyFailedRequest = true
            } else if (cashedDeleteRequests.isNotEmpty()) {
                isThereAnyFailedRequest = true
            }

            if (!isThereAnyFailedRequest) {
                val allServerTasks: Tasks?
                try {
                    allServerTasks = apiRepository.getAllTasks(token).body()?.data
                } catch (e: Exception) {
                    return Result.failure(e)
                }

                val privateLocalTasks = taskRepository.getPrivateTasks()
                val privateLocalTasksPair: MutableList<Pair<Task, Boolean>> =
                    privateLocalTasks.map { task ->
                        Pair(task, false)
                    } as MutableList<Pair<Task, Boolean>>

                val sharedLocalTasks = taskRepository.getSharedTasks()
                val sharedLocalTasksPair: MutableList<Pair<Task, Boolean>> =
                    sharedLocalTasks.map { task ->
                        Pair(task, false)
                    } as MutableList<Pair<Task, Boolean>>

                if (allServerTasks != null) {
                    for (pServerTask in allServerTasks.private) {
                        val index = privateLocalTasksPair.indexOfFirst { pair ->
                            pair.first.serverID == pServerTask.id && !pair.second
                        }
                        if (index >= 0) {
                            val localTask = privateLocalTasksPair[index].first
                            if (!pServerTask.checkEquality(localTask)) {
                                val newTask = pServerTask.convertToLocalTaskWithLocalID(
                                    localTask.id, localTask.detailsVisibility, localTask.alarm
                                )
                                taskRepository.updateTask(newTask)
                                if (localTask.alarm &&
                                    pServerTask.deadline.toLong() != localTask.deadLine.timeInMillis)
                                    resetAlarm(alarmScheduler, newTask)
                            }
                            privateLocalTasksPair[index] = Pair(localTask, true)
                        } else {
                            val deleteReqIndex = sharedPreferencesManager.getCashedDeleteRequests()
                                .indexOfFirst { deleteReq ->
                                    deleteReq.task_id == pServerTask.id
                                }
                            if (deleteReqIndex == -1) {
                                //If the task is not in deleted cash, add it
                                taskRepository.addTask(pServerTask.convertToLocalTask())
                            }
                        }
                    }

                    for (sServerTask in allServerTasks.shared) {
                        val index = sharedLocalTasksPair.indexOfFirst { pair ->
                            pair.first.serverID == sServerTask.id && !pair.second
                        }
                        if (index >= 0) {
                            val localTask = sharedLocalTasksPair[index].first
                            if (!sServerTask.checkEquality(localTask)) {
                                val newTask = sServerTask.convertToLocalTaskWithLocalID(
                                    localTask.id, localTask.detailsVisibility, localTask.alarm
                                )
                                taskRepository.updateTask(newTask)
                                if (localTask.alarm &&
                                    sServerTask.deadline.toLong() != localTask.deadLine.timeInMillis)
                                    resetAlarm(alarmScheduler, newTask)
                            }
                            sharedLocalTasksPair[index] = Pair(localTask, true)
                        } else {
                            val deleteReqIndex = sharedPreferencesManager.getCashedDeleteRequests()
                                .indexOfFirst { deleteReq ->
                                    deleteReq.task_id == sServerTask.id
                                }
                            if (deleteReqIndex == -1) {
                                //If the task is not in deleted cash, add it
                                taskRepository.addTask(sServerTask.convertToLocalTask())
                            }
                        }
                    }
                }

                val privateTasksToDelete: List<Task> = privateLocalTasksPair.filter { pair ->
                    !pair.second
                }.map { pair ->
                    pair.first
                }

                for (toDelete in privateTasksToDelete) {
                    if (toDelete.serverID == newlyAddedTaskServerID) continue
                    taskRepository.deleteTask(toDelete)
                    if (toDelete.alarm) alarmScheduler.cancel(toDelete.id)
                }

                val sharedTasksToDelete: List<Task> = sharedLocalTasksPair.filter { pair ->
                    !pair.second
                }.map { pair ->
                    pair.first
                }

                for (toDelete in sharedTasksToDelete) {
                    if (toDelete.serverID == newlyAddedTaskServerID) continue
                    taskRepository.deleteTask(toDelete)
                    if (toDelete.alarm) alarmScheduler.cancel(toDelete.id)
                }
            } else {
                return Result.failure(Exception())
            }

            return Result.success(Unit)
        }

        private fun resetAlarm(alarmScheduler: AlarmScheduler, task: Task) {
            alarmScheduler.cancel(task.id)
            val deadlineCopy = PersianCalendar()
            deadlineCopy.timeInMillis = task.deadLine.timeInMillis
            deadlineCopy.second = 0
            alarmScheduler.schedule(deadlineCopy.timeInMillis, task.id)
        }
    }

}