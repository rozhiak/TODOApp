package com.rmblack.todoapp.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.behavior.SwipeDismissBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import com.rmblack.todoapp.R
import com.rmblack.todoapp.activities.newlyAddedTaskServerID
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.models.server.success.TaskResponse
import com.rmblack.todoapp.models.server.success.Tasks
import com.rmblack.todoapp.webservice.repository.ApiRepository
import retrofit2.Response
import java.lang.Exception

const val CONNECTION_ERROR_CODE = 0

const val UNKNOWN_ERROR_CODE = 7

class Utilities {

    companion object {
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
        suspend fun syncTasksWithServer(token: String, context: Context): Result<Unit> {
            val sharedPreferencesManager = SharedPreferencesManager(context)
            var failedAddRequests = sharedPreferencesManager.getCashedAddRequests()
            var failedEditRequests = sharedPreferencesManager.getCashedEditRequests()
            var failedDeleteRequests = sharedPreferencesManager.getCashedDeleteRequests()

            val apiRepository = ApiRepository()
            val taskRepository = TaskRepository.get()

            for (addReq in failedAddRequests) {
                val response: Response<TaskResponse>
                try {
                    response = apiRepository.addNewTask(addReq.convertToServerAddModel())
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

            for (editReq in failedEditRequests) {
                try {
                    val response = apiRepository.editTask(editReq.convertToServerEditModel())
                    if (response.code() == 200 || response.code() == 404) sharedPreferencesManager.removeCashedEditRequest(
                        editReq
                    )
                } catch (e: Exception) {
                    return Result.failure(e)
                }
            }

            for (deleteReq in failedDeleteRequests) {
                try {
                    val response = apiRepository.deleteTask(deleteReq)
                    if (response.code() == 200 || response.code() == 404) sharedPreferencesManager.removeCashedDeleteRequest(
                        deleteReq
                    )
                } catch (e: Exception) {
                    return Result.failure(e)
                }
            }

            failedAddRequests = sharedPreferencesManager.getCashedAddRequests()
            failedEditRequests = sharedPreferencesManager.getCashedEditRequests()
            failedDeleteRequests = sharedPreferencesManager.getCashedDeleteRequests()

            var isThereAnyFailedRequest = false
            if (failedAddRequests.isNotEmpty()) {
                isThereAnyFailedRequest = true
            } else if (failedEditRequests.isNotEmpty()) {
                isThereAnyFailedRequest = true
            } else if (failedDeleteRequests.isNotEmpty()) {
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
                                taskRepository.updateTask(
                                    pServerTask.convertToLocalTaskWithLocalID(
                                        localTask.id, localTask.detailsVisibility
                                    )
                                )
                            }
                            privateLocalTasksPair[index] = Pair(localTask, true)
                        } else {
                            val failedRequestIndex =
                                sharedPreferencesManager.getCashedDeleteRequests()
                                    .indexOfFirst { deleteReq ->
                                        deleteReq.task_id == pServerTask.id
                                    }
                            if (failedRequestIndex == -1) {
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
                                taskRepository.updateTask(
                                    sServerTask.convertToLocalTaskWithLocalID(
                                        localTask.id, localTask.detailsVisibility
                                    )
                                )
                            }
                            sharedLocalTasksPair[index] = Pair(localTask, true)
                        } else {
                            val failedRequestIndex =
                                sharedPreferencesManager.getCashedDeleteRequests()
                                    .indexOfFirst { deleteReq ->
                                        deleteReq.task_id == sServerTask.id
                                    }
                            if (failedRequestIndex == -1) {
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
                }

                val sharedTasksToDelete: List<Task> = sharedLocalTasksPair.filter { pair ->
                    !pair.second
                }.map { pair ->
                    pair.first
                }

                for (toDelete in sharedTasksToDelete) {
                    if (toDelete.serverID == newlyAddedTaskServerID) continue
                    taskRepository.deleteTask(toDelete)
                }
            } else {
                return Result.failure(Exception())
            }

            return Result.success(Unit)
        }
    }

}