package com.rmblack.todoapp.fragments

import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.rmblack.todoapp.adapters.viewholders.REMAINING_DAYS_LABLE
import com.rmblack.todoapp.adapters.viewholders.TaskHolder
import com.rmblack.todoapp.databinding.FragmentTasksBinding
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.utils.Utilities
import com.rmblack.todoapp.viewmodels.TasksViewModel
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import java.util.UUID

open class TasksFragment: Fragment(), TaskHolder.EditClickListener {

    protected lateinit var viewModel: TasksViewModel

    private var _binding: FragmentTasksBinding? = null

    protected val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        binding.tasksRv.layoutManager = LinearLayoutManager(context)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpSwipeToDelete()
        setUpRefreshLayout()
    }

    private fun setUpRefreshLayout() {
        val userToken = viewModel.getUserToken()

        if (userToken == null) {
            binding.refreshLayout.isEnabled = false
        } else {
            binding.refreshLayout.setOnRefreshListener {

//                val formerOpenTaskIndex = viewModel.detailsVisibility.indexOf(true)
//                var lastOpenTaskID : UUID? = null
//                if (formerOpenTaskIndex != -1) {
//                    lastOpenTaskID = viewModel.tasks.value[formerOpenTaskIndex]?.id
//                }
//                println("old Insex " + formerOpenTaskIndex)

                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        val response = Utilities.syncTasksWithServer(userToken, requireContext())
                        response.onSuccess {
                            binding.refreshLayout.isRefreshing = false
//                            if (lastOpenTaskID != null) {
//                                val newIndex = viewModel.tasks.value.indexOfFirst { task ->
//                                    task?.id == lastOpenTaskID
//                                }
//                                println("new index " + newIndex)
//                                if (newIndex == -1) {
//                                    viewModel.updateVisibility(formerOpenTaskIndex, false)
//                                    binding.tasksRv.adapter?.notifyItemChanged(formerOpenTaskIndex)
//                                } else if (newIndex != formerOpenTaskIndex) {
//                                    viewModel.updateVisibility(formerOpenTaskIndex, false)
//                                    binding.tasksRv.adapter?.notifyItemChanged(formerOpenTaskIndex)
//                                    viewModel.updateVisibility(newIndex, true)
//                                    binding.tasksRv.adapter?.notifyItemChanged(newIndex)
//                                }
//                            }

                        }
                        response.onFailure { e ->
                            if (e is UnknownHostException) {
                                Utilities.makeWarningSnack(
                                    requireActivity(),
                                    binding.root,
                                "مشکل در اتصال به اینترنت ، لطفا از اتصال خود مطمئن شوید.")
                                binding.refreshLayout.isRefreshing = false
                            } else {
                                binding.refreshLayout.isRefreshing = false
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setUpSwipeToDelete() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) {

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
                    val deleteReq = viewModel.makeDeleteRequest(deletedTask.serverID)
                    if (deleteReq != null) {
                        viewModel.cashDeleteRequest(deleteReq)
                    }
                    var isDateLableRemoved = false
                    binding.tasksRv.adapter?.let {adapter ->
                        isDateLableRemoved = viewModel.deleteTask(
                            viewModel.tasks.value[position],
                            position,
                            adapter
                        )
                    }
                    val snackBar = Utilities.makeDeleteSnackBar(requireActivity(), binding.tasksRv) {
                        for (b in viewModel.detailsVisibility) {
                            if (b) {
                                visibility = false
                                break
                            }
                        }
                        viewModel.insertTask(deletedTask)
                        viewModel.insertVisibility(position, visibility, isDateLableRemoved)
                        binding.tasksRv.post {
                            binding.tasksRv.smoothScrollToPosition(position)
                        }
                        if (deleteReq != null) {
                            viewModel.removeDeleteRequest(deleteReq)
                        }
                    }

                    snackBar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            super.onDismissed(transientBottomBar, event)
                            if (event != Snackbar.Callback.DISMISS_EVENT_MANUAL && deleteReq != null) {
                                viewModel.deleteTaskFromServer(deleteReq)
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

                binding.refreshLayout.isEnabled = !isCurrentlyActive

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val position = viewHolder.absoluteAdapterPosition
                    if (position >= 0 && position < (binding.tasksRv.adapter?.itemCount ?: 0)) {
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

    protected open fun setUpNoTaskIconAndText(hide: Boolean) {
        if (hide) {
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