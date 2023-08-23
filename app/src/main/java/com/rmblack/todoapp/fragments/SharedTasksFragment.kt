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
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.rmblack.todoapp.adapters.SharedTasksAdapter
import com.rmblack.todoapp.adapters.viewholders.REMAINING_DAYS_LABLE
import com.rmblack.todoapp.adapters.viewholders.TaskHolder
import com.rmblack.todoapp.databinding.FragmentSharedTasksBinding
import com.rmblack.todoapp.models.Task
import com.rmblack.todoapp.utils.Utilities
import com.rmblack.todoapp.viewmodels.SharedTasksViewModel
import kotlinx.coroutines.launch
import java.util.UUID

class SharedTasksFragment : Fragment(), TaskHolder.EditClickListener {

    private var _binding : FragmentSharedTasksBinding? = null

    private val viewModel: SharedTasksViewModel by viewModels()

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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerview()
        setUpSweepToDelete()
    }

    private fun setUpSweepToDelete() {
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
                val deletedTask: Task? = viewModel.tasks.value[position]
                if (deletedTask != null) {
                    viewModel.deleteTask(viewModel.tasks.value[position])
                    binding.sharedTasksRv.adapter?.notifyItemRemoved(position)
                    Utilities.makeDeleteSnackBar(requireActivity(), binding.sharedTasksRv) {
                        viewModel.insertTask(deletedTask)
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
            EditTaskBottomSheet.REQUEST_KEY_ID
        ) { _, bundle ->
            editedTaskId = bundle.getSerializable(EditTaskBottomSheet.BUNDLE_KEY_ID) as UUID?
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tasks.collect { tasks ->

                    val editedTaskIndex = tasks.indexOfFirst { (it?.id ?: 0) == editedTaskId }
                    val oldIndex = viewModel.detailsVisibility.indexOfFirst { it }

                    if (oldIndex != editedTaskIndex && oldIndex != -1 && editedTaskIndex != -1) {
                        if (viewModel.detailsVisibility[oldIndex]) {
                            viewModel.updateVisibility(oldIndex, false)
                            binding.sharedTasksRv.adapter?.notifyItemChanged(oldIndex)
                        }

                        if (!viewModel.detailsVisibility[editedTaskIndex]) {
                            viewModel.updateVisibility(editedTaskIndex, true)
                            binding.sharedTasksRv.adapter?.notifyItemChanged(editedTaskIndex)
                            binding.sharedTasksRv.smoothScrollToPosition(editedTaskIndex)
                        }
                    }

                    val layoutManager = binding.sharedTasksRv.layoutManager as LinearLayoutManager
                    val firstVisibleItem = layoutManager.getChildAt(0)
                    val offset = firstVisibleItem?.top ?: 0
                    val pos = layoutManager.findFirstVisibleItemPosition()
                    val marginTop = firstVisibleItem?.marginTop ?: 0

                    binding.sharedTasksRv.adapter = createSharedTasksAdapter(tasks)

                    layoutManager.scrollToPositionWithOffset(pos, offset - marginTop)
                }
            }
        }
    }

    private fun createSharedTasksAdapter(tasks: List<Task?>) =
        SharedTasksAdapter(tasks, viewModel, this)


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

}