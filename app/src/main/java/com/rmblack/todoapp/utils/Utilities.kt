package com.rmblack.todoapp.utils

import android.app.Activity
import android.graphics.Color
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.material.behavior.SwipeDismissBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import com.rmblack.todoapp.R
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.webservice.repository.ApiRepository

class Utilities {

    companion object {

        fun makeDeleteSnackBar(activity: Activity, container: View, onUndo: () -> Unit): Snackbar {
            val snackBar = Snackbar.make(container, "", Snackbar.LENGTH_LONG)
            val customSnackView: View =
                activity.layoutInflater.inflate(R.layout.delete_snack, null)
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

        //TODO before using this function , check user login state
        suspend fun syncTasksWithServer(token: String) {
            //TODO before anything you should check if there is any cashed request here
            //TODO be  careful that all cashed requests should be performed and then continue

            val apiRepository = ApiRepository()
            val taskRepository = TaskRepository.get()

            val allServerTasks = apiRepository.getAllTasks(token).body()?.data

            val privateLocalTasks = taskRepository.getPrivateTasks()
            val privateLocalTasksPair: MutableList<Pair<Task, Boolean>> = privateLocalTasks.map { task ->
                Pair(task, false)
            } as MutableList<Pair<Task, Boolean>>

            val sharedLocalTasks = taskRepository.getSharedTasks()
            val sharedLocalTasksPair: MutableList<Pair<Task, Boolean>> = sharedLocalTasks.map {task ->
                Pair(task, false)
            } as MutableList<Pair<Task, Boolean>>

            if (allServerTasks != null) {
                for (pServerTask in allServerTasks.private) {
                    val index = privateLocalTasksPair.indexOfFirst {pair ->
                        pair.first.serverID == pServerTask.id && !pair.second
                    }
                    if (index >= 0) {
                        //Task found
                        val localTask = privateLocalTasksPair[index].first
                        if (!pServerTask.checkEquality(localTask)) {
                            //Task has been changed -> update in local database
                            taskRepository.updateTask(pServerTask.convertToLocalTaskWithLocalID(localTask.id))
                        }
                        privateLocalTasksPair[index] = Pair(localTask, true)
                    } else {
                        //Not Found in local database -> add the task in local data base.
                    }
                }

                for (sServerTask in allServerTasks.shared) {
                    val index = sharedLocalTasksPair.indexOfFirst {pair ->
                        pair.first.serverID == sServerTask.id  && !pair.second
                    }
                    if (index >= 0) {
                        //Task found
                        val localTask = sharedLocalTasksPair[index].first
                        if (!sServerTask.checkEquality(localTask)) {
                            //Task has been changed -> update in local database
                            taskRepository.updateTask(sServerTask.convertToLocalTaskWithLocalID(localTask.id))
                        }
                        sharedLocalTasksPair[index] = Pair(localTask, true)
                    } else {
                        //Not Found in local database -> add the task in local data base.
                    }
                }
            }

            val privateTasksToDelete: List<Task> = privateLocalTasksPair.filter { pair ->
                !pair.second
            }.map { pair ->
                pair.first
            }

            for (toDelete in privateTasksToDelete) {
                taskRepository.deleteTask(toDelete)
            }

            val sharedTasksToDelete: List<Task> = sharedLocalTasksPair.filter { pair ->
                !pair.second
            }.map { pair ->
                pair.first
            }

            for (toDelete in sharedTasksToDelete) {
                taskRepository.deleteTask(toDelete)
            }
        }
    }

}