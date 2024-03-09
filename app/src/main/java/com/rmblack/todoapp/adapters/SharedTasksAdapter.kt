package com.rmblack.todoapp.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.rmblack.todoapp.adapters.viewholders.TaskHolder
import com.rmblack.todoapp.adapters.viewholders.RemainingDaysLableHolder
import com.rmblack.todoapp.alarm.AlarmScheduler
import com.rmblack.todoapp.databinding.RemainingDaysLableBinding
import com.rmblack.todoapp.databinding.SharedTasksRvItemBinding
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.viewmodels.TasksViewModel
import kotlinx.coroutines.CoroutineScope

class SharedTaskHolder(
    scope: CoroutineScope,
    private val binding: SharedTasksRvItemBinding,
    private val viewModel: TasksViewModel,
    private val activity: Activity,
    editClickListener: EditClickListener,
    recyclerView: RecyclerView,
    private val alarmScheduler: AlarmScheduler
) : TaskHolder(
    scope, editClickListener, recyclerView, binding
) {
    fun bind(
        tasks: List<Task?>, pos: Int, adapter: TaskAdapter
    ) {
        val task = tasks[pos]
        task?.let { notNullTask ->
            binding.apply {
                configUrgentSwitch(viewModel, notNullTask, urgentSwitch)
                configDoneCheckBox(viewModel, notNullTask, doneCheckBox)
                setDetailsVisibility(
                    viewModel.tasks.value[pos]?.detailsVisibility ?: false,
                    descriptionLable,
                    descriptionTv,
                    urgentLable,
                    urgentSwitch,
                    editCard,
                    deleteBtn
                )
                setUrgentUi(
                    notNullTask,
                    titleTv,
                    doneCheckBox,
                    rightColoredLine,
                    urgentSwitch,
                    activity.resources
                )
                setEachTaskClick(pos, adapter, rootCard, viewModel)
                setTaskDetails(
                    notNullTask, titleTv, deadLineTv, descriptionTv, descriptionLable, doneCheckBox
                )
                setEditClick(notNullTask, editCard)
                setBackground(viewModel, pos, rootConstraint, activity.resources)
                setUpDelete(pos, notNullTask, deleteBtn, viewModel, activity, alarmScheduler)
                setClickOnUrgentLable(urgentLable, urgentSwitch)
                setLongPress(notNullTask, rootCard)
                composerNameTv.text = notNullTask.composer
            }
        }
    }
}

class SharedTasksAdapter(
    private val scope: CoroutineScope,
    private val viewModel: TasksViewModel,
    private val editClickListener: TaskHolder.EditClickListener,
    private val activity: Activity,
    private val alarmScheduler: AlarmScheduler
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
            val binding = SharedTasksRvItemBinding.inflate(inflater, parent, false)
            SharedTaskHolder(
                scope, binding, viewModel, activity, editClickListener, recyclerView, alarmScheduler
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is SharedTaskHolder) {
            holder.bind(viewModel.tasks.value, position, this)
        } else if (holder is RemainingDaysLableHolder && position + 1 < viewModel.tasks.value.size) {
            //Check if item is not the last position in rec
            viewModel.tasks.value[position + 1]?.let { holder.bind(it.deadLine) }
        }
    }
}