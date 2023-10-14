package com.rmblack.todoapp.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.rmblack.todoapp.adapters.viewholders.TaskHolder
import com.rmblack.todoapp.adapters.viewholders.REMAINING_DAYS_LABLE
import com.rmblack.todoapp.adapters.viewholders.RemainingDaysLableHolder
import com.rmblack.todoapp.databinding.PrivateTasksRvItemBinding
import com.rmblack.todoapp.databinding.RemainingDaysLableBinding
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.viewmodels.TasksViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

class PrivateTaskHolder(
    scope: CoroutineScope,
    isSyncing: StateFlow<Boolean>,
    private val binding: PrivateTasksRvItemBinding,
    private val viewModel: TasksViewModel,
    private val activity: Activity,
    editClickListener: EditClickListener,
    recyclerView: RecyclerView,
) : TaskHolder(
    scope,
    isSyncing,
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
                setDetailsVisibility(
                    viewModel.tasks.value[pos]?.detailsVisibility ?: false,
                    descriptionLable,
                    descriptionTv,
                    urgentLable,
                    urgentSwitch,
                    editCard,
                    deleteBtn
                )
                setUrgentUi(it, titleTv, doneCheckBox, rightColoredLine, urgentSwitch)
                setDoneUi(it, doneCheckBox)
                setEachTaskClick(pos, adapter, rootCard, viewModel)
                setTaskDetails(it, titleTv, deadLineTv, descriptionTv, descriptionLable)
                setEditClick(it, editCard)
                setBackground(viewModel, pos, rootConstraint, activity.resources)
                setUpDelete(pos, it, deleteBtn, viewModel, activity)
                setClickOnUrgentLable(urgentLable, urgentSwitch)
            }
        }
    }
}

class PrivateTaskListAdapter(
    private val scope: CoroutineScope,
    private val isSyncing: StateFlow<Boolean>,
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
            PrivateTaskHolder(scope, isSyncing, binding, viewModel ,activity ,editClickListener, recyclerView)
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