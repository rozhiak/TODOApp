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
import com.rmblack.todoapp.databinding.PrivateTasksRvItemBinding
import com.rmblack.todoapp.databinding.RemainingDaysLableBinding
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.utils.Utilities
import com.rmblack.todoapp.viewmodels.TasksViewModel
import com.suke.widget.SwitchButton

class PrivateTaskHolder(
    private val binding: PrivateTasksRvItemBinding,
    private val viewModel: TasksViewModel,
    private val activity: Activity,
    editClickListener: EditClickListener,
    recyclerView: RecyclerView,
) : TaskHolder(
    editClickListener,
    recyclerView,
    binding
) {
    fun bind(
        tasks: List<Task?>,
        pos: Int,
        adapter: TaskAdapter
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
                setEachTaskClick(pos, adapter, rootCard, viewModel)
                setTaskDetails(it, titleTv, deadLineTv, descriptionTv, descriptionLable)
                setEditClick(it, editCard)
                setBackground(viewModel, pos, rootConstraint)
                setUpDelete(pos, it, adapter, deleteBtn, viewModel, activity)
                setClickOnUrgentLable(urgentLable, urgentSwitch)
            }
        }
    }
}

class PrivateTaskListAdapter(
    private val viewModel: TasksViewModel,
    private val editClickListener: TaskHolder.EditClickListener,
    private val activity: Activity
) : TaskAdapter(
    viewModel,
    editClickListener,
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == REMAINING_DAYS_LABLE) {
            val binding = RemainingDaysLableBinding.inflate(inflater, parent, false)
            RemainingDaysLableHolder(binding)
        } else {
            val binding = PrivateTasksRvItemBinding.inflate(inflater, parent, false)
            PrivateTaskHolder(binding, viewModel ,activity ,editClickListener, recyclerView)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is PrivateTaskHolder) {
            holder.bind(viewModel.tasks.value, position, this)
        } else if (holder is RemainingDaysLableHolder && position + 1 < viewModel.tasks.value.size) {
            viewModel.tasks.value[position+1]?.let { holder.bind(it.deadLine) }
        }
    }

}