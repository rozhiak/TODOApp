package com.rmblack.todoapp.adapters

import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.rmblack.todoapp.adapters.viewholders.TaskHolder
import com.rmblack.todoapp.adapters.viewholders.REMAINING_DAYS_LABLE
import com.rmblack.todoapp.adapters.viewholders.RemainingDaysLableHolder
import com.rmblack.todoapp.adapters.viewholders.TASK
import com.rmblack.todoapp.databinding.RemainingDaysLableBinding
import com.rmblack.todoapp.databinding.SharedTasksRvItemBinding
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.utils.Utilities
import com.rmblack.todoapp.viewmodels.SharedTasksViewModel
import com.rmblack.todoapp.viewmodels.TasksViewModel
import com.suke.widget.SwitchButton

class SharedTaskHolder(
    token: String?,
    private val binding: SharedTasksRvItemBinding,
    private val viewModel: TasksViewModel,
    private val activity: Activity,
    editClickListener: EditClickListener,
    recyclerView: RecyclerView
) : TaskHolder(
    token,
    editClickListener,
    recyclerView,
    binding
) {
    fun bind(
        tasks: List<Task?>,
        pos: Int,
        adapter: SharedTasksAdapter
    ) {
        val task = tasks[pos]
        task?.let {
            binding.apply {
                configUrgentSwitch(viewModel, it, urgentSwitch)
                configDoneCheckBox(viewModel, it, doneCheckBox)
                if (pos in viewModel.detailsVisibility.indices) {
                    setDetailsVisibility(
                        viewModel.detailsVisibility[pos],
                        descriptionLable,
                        descriptionTv,
                        urgentLable,
                        urgentSwitch,
                        editCard,
                        deleteBtn
                    )
                }
                setUrgentUi(it, titleTv, doneCheckBox, rightColoredLine, urgentSwitch)
                setDoneUi(it, doneCheckBox)
                setEachTaskClick(pos, adapter, rootCard)
                setTaskDetails(it, titleTv, deadLineTv, descriptionTv, descriptionLable)
                setEditClick(it, editCard)
                setBackground(viewModel, pos, rootConstraint)
                setUpDelete(pos, it, adapter, deleteBtn, viewModel, activity)
                setClickOnUrgentLable(urgentLable, urgentSwitch)
                composerNameTv.text = it.composer
            }
        }
    }

    private fun setUpDelete(
        pos: Int,
        task: Task,
        adapter: SharedTasksAdapter,
        deleteBtn: AppCompatImageView,
        viewModel: TasksViewModel,
        activity: Activity
    ) {
        deleteBtn.setOnClickListener {
            var visibility = viewModel.detailsVisibility[pos]
            viewModel.deleteTask(task, pos)
            adapter.notifyItemRemoved(pos)
            val snackBar = Utilities.makeDeleteSnackBar(activity, recyclerView) {
                for (b in viewModel.detailsVisibility) {
                    if (b) {
                        visibility = false
                        break
                    }
                }
                viewModel.insertTask(task)
                viewModel.insertVisibility(pos, visibility)
                recyclerView.post {
                    recyclerView.smoothScrollToPosition(pos)
                }
            }
            snackBar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    if (event != Snackbar.Callback.DISMISS_EVENT_MANUAL) {
                        viewModel.deleteTaskFromServer(task.serverID)
                    }
                }
            })
        }
    }

    private fun setEachTaskClick(
        pos: Int,
        adapter: SharedTasksAdapter,
        rootCard: CardView
    ) {
        rootCard.setOnClickListener {
            if (pos in viewModel.detailsVisibility.indices && !viewModel.detailsVisibility[pos]) {
                for (i in viewModel.tasks.value.indices) {
                    if (i != pos && viewModel.detailsVisibility[i]) {
                        viewModel.updateVisibility(i, !viewModel.detailsVisibility[i])
                        adapter.notifyItemChanged(i)
                    }
                }
            }
            if (pos in viewModel.detailsVisibility.indices) {
                viewModel.updateVisibility(pos, !viewModel.detailsVisibility[pos])
            }
            adapter.notifyItemChanged(pos)
            recyclerView.post {
                recyclerView.smoothScrollToPosition(pos)
            }
        }
    }
}

class SharedTasksAdapter(
    private val token: String?,
    private val viewModel: TasksViewModel,
    private val editClickListener: TaskHolder.EditClickListener,
    private val activity: Activity
) : RecyclerView.Adapter<ViewHolder>() {

    private lateinit var recyclerView: RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == REMAINING_DAYS_LABLE) {
            val binding = RemainingDaysLableBinding.inflate(inflater, parent, false)
            RemainingDaysLableHolder(binding)
        } else {
            val binding = SharedTasksRvItemBinding.inflate(inflater, parent, false)
            SharedTaskHolder(token, binding, viewModel, activity, editClickListener, recyclerView)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is SharedTaskHolder) {
            holder.bind(viewModel.tasks.value, position, this)
        } else if (holder is RemainingDaysLableHolder && position + 1 < viewModel.tasks.value.size) {
            viewModel.tasks.value[position+1]?.let { holder.bind(it.deadLine) }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position >= 0 && viewModel.tasks.value[position] != null) {
            TASK
        } else {
            REMAINING_DAYS_LABLE
        }
    }

    override fun getItemCount() = viewModel.tasks.value.size
}