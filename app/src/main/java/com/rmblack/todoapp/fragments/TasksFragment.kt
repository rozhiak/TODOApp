package com.rmblack.todoapp.fragments

import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.rmblack.todoapp.adapters.viewholders.REMAINING_DAYS_LABLE
import com.rmblack.todoapp.adapters.viewholders.TaskHolder
import com.rmblack.todoapp.databinding.FragmentTasksBinding
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.utils.Utilities
import com.rmblack.todoapp.viewmodels.TasksViewModel
import com.rmblack.todoapp.webservice.ApiService
import com.rmblack.todoapp.webservice.repository.ApiRepository
import java.util.UUID

open class TasksFragment: Fragment(), TaskHolder.EditClickListener {

    protected var isFirstTime = true

    protected var _binding: FragmentTasksBinding? = null

    protected val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    protected lateinit var viewModel: TasksViewModel

    protected fun setUpSwipeToDelete() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.absoluteAdapterPosition
                var visibility = viewModel.detailsVisibility[position]
                val deletedTask: Task? = viewModel.tasks.value[position]
                if (deletedTask != null) {
                    viewModel.deleteTask(viewModel.tasks.value[position], position)
                    binding.tasksRv.adapter?.notifyItemRemoved(position)
                    val snackBar = Utilities.makeDeleteSnackBar(requireActivity(), binding.tasksRv) {
                        for (b in viewModel.detailsVisibility) {
                            if (b) {
                                visibility = false
                                break
                            }
                        }
                        viewModel.insertTask(deletedTask)
                        viewModel.insertVisibility(position, visibility)
                        binding.tasksRv.post {
                            binding.tasksRv.smoothScrollToPosition(position)
                        }
                    }

                    snackBar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            super.onDismissed(transientBottomBar, event)
                            if (event != Snackbar.Callback.DISMISS_EVENT_MANUAL) {
                                viewModel.deleteTaskFromServer(deletedTask.serverID)
                            }
                        }
                    })
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val position = viewHolder.absoluteAdapterPosition
                    val viewType = binding.tasksRv.adapter?.getItemViewType(position)
                    if (viewType == REMAINING_DAYS_LABLE) {
                        super.onChildDraw(
                            c,
                            recyclerView,
                            viewHolder,
                            0f,
                            dY,
                            actionState,
                            isCurrentlyActive
                        )
                        return
                    }
                }
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }).attachToRecyclerView(binding.tasksRv)
    }

    protected fun setUpNoTaskIconAndText(isNotEmpty: Boolean) {
        if (isNotEmpty) {
            binding.ivNoTask.visibility = View.GONE
            binding.tvNoTask.visibility = View.GONE
        } else {
            binding.ivNoTask.visibility = View.VISIBLE
            binding.tvNoTask.visibility = View.VISIBLE
        }
    }

    protected fun setUpIfLabeledTaskMoved() {
        val firstFalseIndex = viewModel.detailsVisibility.indexOfFirst { !it }
        viewModel.deleteVisibility(firstFalseIndex)
    }

    protected fun setUpForNewOrEditTask(
        tasks: List<Task?>,
        editedTaskId: UUID?,
        isNewTask: Boolean?
    ) {
        val editedTaskIndex = tasks.indexOfFirst { (it?.id ?: 0) == editedTaskId }
        val oldIndex = viewModel.detailsVisibility.indexOfFirst { it }
        if (oldIndex != editedTaskIndex) {
            if (oldIndex != -1 && editedTaskIndex != -1) viewModel.updateVisibility(oldIndex, false)
            if (editedTaskIndex != -1) {
                viewModel.updateVisibility(editedTaskIndex, true)
                binding.tasksRv.post {
                    binding.tasksRv.smoothScrollToPosition(editedTaskIndex)
                }
            }
        }
        if (isNewTask != null && editedTaskIndex != -1) {
            if (isNewTask) {
                //Add new task to server
                tasks[editedTaskIndex]?.let { viewModel.addTaskToServer(it) }
            } else {
                tasks[editedTaskIndex]?.let { viewModel.editTaskInServer(it) }
            }
        }

    }

    protected fun setUpForTaskMoving() {
        val movedTaskIndex = viewModel.detailsVisibility.indexOfFirst { it }
        viewModel.deleteVisibility(movedTaskIndex)
    }

    override fun onEditClick(task: Task) {
        val editTaskBottomSheet = EditTaskBottomSheet()
        val args = Bundle()
        args.putString("taskId", task.id.toString())
        args.putBoolean("isNewTask", false)
        editTaskBottomSheet.arguments = args
        editTaskBottomSheet.show(parentFragmentManager, "TODO tag")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}