package com.rmblack.todoapp.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.behavior.SwipeDismissBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import com.rmblack.todoapp.R
import com.rmblack.todoapp.data.repository.TaskRepository
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
            val allPrivateLocalTasks = taskRepository.getPrivateTasks()
            val allSharedLocalTasks = taskRepository.getSharedTasks()

            if (allServerTasks != null) {
                for (pServerTask in allServerTasks.private) {
                    val index = allPrivateLocalTasks.indexOfFirst { it.serverID == pServerTask.id }
                    if (index >= 0) {
                        //Task found
                        val task = allPrivateLocalTasks[index]
                        if (!pServerTask.checkEquality(task)) {
                            //Task has been changed -> update in local database
                        }
                    } else {
                        //Not Found in local database -> add the task in local data base.
                    }
                }

                for (sServerTask in allServerTasks.shared) {
                    val index = allSharedLocalTasks.indexOfFirst { it.serverID == sServerTask.id }
                    if (index >= 0) {
                        //Task found
                        val task = allSharedLocalTasks[index]
                        if (!sServerTask.checkEquality(task)) {
                            //Task has been changed -> update in local database
                        }
                    } else {
                        //Not Found in local database -> add the task in local data base.
                    }
                }
            }
        }




        //TODO at the end you should check if any task is not checked it means the it is removed from server and you should remove
        //TODO it from local data base too. (for that we should create a list of Pairs (task, boolean).  )
    }

}