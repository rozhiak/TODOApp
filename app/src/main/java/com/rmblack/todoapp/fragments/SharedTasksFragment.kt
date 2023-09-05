package com.rmblack.todoapp.fragments

import android.graphics.Canvas
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginTop
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rmblack.todoapp.adapters.SharedTasksAdapter
import com.rmblack.todoapp.adapters.viewholders.REMAINING_DAYS_LABLE
import com.rmblack.todoapp.adapters.viewholders.TaskHolder
import com.rmblack.todoapp.databinding.FragmentSharedTasksBinding
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.models.local.TaskState
import com.rmblack.todoapp.utils.Utilities
import com.rmblack.todoapp.viewmodels.MainViewModel
import com.rmblack.todoapp.viewmodels.PrivateTasksViewModel
import com.rmblack.todoapp.viewmodels.SharedTasksViewModel
import com.rmblack.todoapp.webservice.ApiService
import com.rmblack.todoapp.webservice.repository.ApiRepository
import kotlinx.coroutines.launch
import java.util.UUID

class SharedTasksFragment : Fragment(), TaskHolder.EditClickListener {

    private var _binding : FragmentSharedTasksBinding? = null

    private lateinit var viewModel: SharedTasksViewModel

    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSharedTasksBinding.inflate(inflater, container, false)

        binding.sharedTasksRv.layoutManager = LinearLayoutManager(context)

        val apiService = ApiService.getInstance()
        val apiRepository = ApiRepository(apiService)
        viewModel = ViewModelProvider(this, SharedFragmentViewModelFactory(apiRepository)).get(SharedTasksViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerview()
        setUpSwipeToDelete()
    }

    private fun setUpSwipeToDelete() {
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
                    binding.sharedTasksRv.adapter?.notifyItemRemoved(position)
                    Utilities.makeDeleteSnackBar(requireActivity(), binding.sharedTasksRv) {
                        for (b in viewModel.detailsVisibility) {
                            if (b) {
                                visibility = false
                                break
                            }
                        }
                        viewModel.insertTask(deletedTask)
                        viewModel.insertVisibility(position, visibility)

                    }
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
                    val viewType = binding.sharedTasksRv.adapter?.getItemViewType(position)
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
        }).attachToRecyclerView(binding.sharedTasksRv)
    }

    private fun setUpRecyclerview() {
        var editedTaskId: UUID? = null

        setFragmentResultListener(
            EditTaskBottomSheet.REQUEST_KEY_ID_SHARED
        ) { _, bundle ->
            editedTaskId = bundle.getSerializable(EditTaskBottomSheet.BUNDLE_KEY_ID_SHARED) as UUID?
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tasks.collect { tasks ->
                    if (viewModel.detailsVisibility.size != viewModel.tasks.value.size) {
                        setUpForTaskMoving()
                    } else if (editedTaskId != null) {
                        setUpForNewOrEditTask(tasks, editedTaskId)
                    }
                    editedTaskId = null
                    while (viewModel.detailsVisibility.size > viewModel.tasks.value.size) {
                        setUpIfLabeledTaskMoved()
                    }

//                    println("=================================")
//                    for (t in viewModel.tasks.value) {
//                        println(t?.title)
//                    }
//                    println(viewModel.detailsVisibility)
//                    println("=================================")

                    val layoutManager = binding.sharedTasksRv.layoutManager as LinearLayoutManager
                    val firstVisibleItem = layoutManager.getChildAt(0)
                    val offset = firstVisibleItem?.top ?: 0
                    val pos = layoutManager.findFirstVisibleItemPosition()
                    val marginTop = firstVisibleItem?.marginTop ?: 0

                    binding.sharedTasksRv.adapter = createSharedTasksAdapter(tasks)

                    layoutManager.scrollToPositionWithOffset(pos, offset - marginTop)

                    setUpNoTaskIconAndText(tasks)
                }
            }
        }
    }

    private fun setUpNoTaskIconAndText(tasks: List<Task?>) {
        if (tasks.isNotEmpty()) {
            binding.ivNoTask.visibility = View.GONE
            binding.tvNoTask.visibility = View.GONE
        } else {
            binding.ivNoTask.visibility = View.VISIBLE
            binding.tvNoTask.visibility = View.VISIBLE
        }
    }

    //If a task is moved which had has a label, visibility
    // for the label above the task should be deleted.
    private fun setUpIfLabeledTaskMoved() {
        val firstFalseIndex = viewModel.detailsVisibility.indexOfFirst { !it }
        viewModel.deleteVisibility(firstFalseIndex)
    }

    private fun setUpForNewOrEditTask(
        tasks: List<Task?>,
        editedTaskId: UUID?
    ) {
        val editedTaskIndex = tasks.indexOfFirst { (it?.id ?: 0) == editedTaskId }
        val oldIndex = viewModel.detailsVisibility.indexOfFirst { it }
        if (oldIndex != editedTaskIndex) {
            if (oldIndex != -1 && editedTaskIndex != -1) viewModel.updateVisibility(oldIndex, false)
            if (editedTaskIndex != -1) {
                viewModel.updateVisibility(editedTaskIndex, true)
                binding.sharedTasksRv.post {
                    binding.sharedTasksRv.smoothScrollToPosition(editedTaskIndex)
                }
                tasks[editedTaskIndex]?.let {newTask ->
                    if (newTask.state == TaskState.NEW) {
                        viewModel.addTaskToServer(newTask, editedTaskIndex)
                    }
                }
            }
        }
    }

    private fun setUpForTaskMoving() {
        val movedTaskIndex = viewModel.detailsVisibility.indexOfFirst { it }
        viewModel.deleteVisibility(movedTaskIndex)
    }

    private fun createSharedTasksAdapter(tasks: List<Task?>) =
        SharedTasksAdapter(viewModel, this, requireActivity())


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onEditClick(task: Task) {
        val editTaskBottomSheet = EditTaskBottomSheet()
        val args = Bundle()
        args.putString("taskId", task.id.toString())
        editTaskBottomSheet.arguments = args
        editTaskBottomSheet.show(parentFragmentManager, "TODO tag")
    }


    class SharedFragmentViewModelFactory constructor(private val repository: ApiRepository): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(SharedTasksViewModel::class.java)) {
                SharedTasksViewModel(this.repository) as T
            } else {
                throw IllegalArgumentException("ViewModel Not Found")
            }
        }
    }
}