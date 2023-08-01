package com.rmblack.todoapp.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rmblack.todoapp.databinding.PrivateTasksRvItemBinding
import com.rmblack.todoapp.models.Task
import com.rmblack.todoapp.viewmodels.PrivateTasksViewModel

class PrivateTaskHolder(
     private val binding: PrivateTasksRvItemBinding,
     private val viewModel: PrivateTasksViewModel,
     private val editClickListener: EditClickListener
) :RecyclerView.ViewHolder(binding.root) {

    //this should be implemented for Shared fragment too
    interface EditClickListener {
        fun onEditClick(task: Task)
    }

    fun bind(tasks: List<Task>,
             pos: Int,
             adapter: PrivateTaskListAdapter) {
        binding.apply {
            configUrgentSwitch(viewModel, tasks, pos)
            configDoneCheckBox(viewModel, tasks, pos)
            setDetailsVisibility(tasks[pos])
            setUrgentUi(tasks[pos])
            setDoneUi(tasks, pos)
            setEachTaskClick(tasks, pos, adapter)
            setTaskDetails(tasks, pos)
            setEditClick(tasks, pos)
        }
    }

    private fun PrivateTasksRvItemBinding.setEditClick(
        tasks: List<Task>,
        pos: Int
    ) {
        editCard.setOnClickListener {
            editClickListener.onEditClick(tasks[pos])
        }
    }

    private fun PrivateTasksRvItemBinding.setDoneUi(
        tasks: List<Task>,
        pos: Int
    ) {
        doneCheckBox.isChecked = tasks[pos].isDone
    }

    private fun PrivateTasksRvItemBinding.configUrgentSwitch(
        viewModel: PrivateTasksViewModel,
        tasks: List<Task>,
        pos: Int
    ) {
        urgentSwitchCompat.setOnCheckedChangeListener { _, b ->
            viewModel.updateTask { oldTasks ->
                val updatedTasks = oldTasks.toMutableList()
                val tarTask = tasks[pos]
                val updatedTask = Task(
                    tarTask.title,
                    tarTask.id,
                    tarTask.description,
                    tarTask.addedTime,
                    tarTask.deadLine,
                    b,
                    tarTask.isDone,
                    tarTask.isShared,
                    tarTask.user,
                    tarTask.groupId,
                    tarTask.detailsVisibility)
                updatedTasks[pos] = updatedTask
                updatedTasks
            }
            viewModel.updateUrgentState(b, tasks[pos].id)
        }
    }

    private fun PrivateTasksRvItemBinding.configDoneCheckBox(
        viewModel: PrivateTasksViewModel,
        tasks: List<Task>,
        pos: Int
    ) {
        doneCheckBox.setOnCheckedChangeListener { _, b ->
            viewModel.updateTask { oldTasks ->
                val updatedTasks = oldTasks.toMutableList()
                val tarTask = tasks[pos]
                val updatedTask = Task(
                    tarTask.title,
                    tarTask.id,
                    tarTask.description,
                    tarTask.addedTime,
                    tarTask.deadLine,
                    tarTask.isUrgent,
                    b,
                    tarTask.isShared,
                    tarTask.user,
                    tarTask.groupId,
                    tarTask.detailsVisibility)
                updatedTasks[pos] = updatedTask
                updatedTasks
            }
            viewModel.updateDoneState(b, tasks[pos].id)
        }
    }

    private fun PrivateTasksRvItemBinding.setEachTaskClick(
        tasks: List<Task>,
        pos: Int,
        adapter: PrivateTaskListAdapter
    ) {
        rootCard.setOnClickListener {
            if (!tasks[pos].detailsVisibility) {
                for (i in tasks.indices) {
                    if (i != pos && tasks[i].detailsVisibility) {
                        tasks[i].detailsVisibility = !tasks[i].detailsVisibility
                        adapter.notifyItemChanged(i)
                    }
                }
            }
            tasks[pos].detailsVisibility = !tasks[pos].detailsVisibility
            adapter.notifyItemChanged(pos)
        }
    }

    private fun PrivateTasksRvItemBinding.setTaskDetails(
        tasks: List<Task>,
        pos: Int
    ) {
        titleTv.text = tasks[pos].title
        deadLineTv.text = tasks[pos].deadLine.shortDateString
        descriptionTv.text = tasks[pos].description
    }

    private fun PrivateTasksRvItemBinding.setUrgentUi(task: Task) {
        if (task.isUrgent) {
            titleTv.setTextColor(Color.parseColor("#D05D8A"))
            doneCheckBox.buttonTintList = ColorStateList.valueOf(Color.parseColor("#D05D8A"))
            rightColoredLine.imageTintList = ColorStateList.valueOf(Color.parseColor("#D05D8A"))
            urgentSwitchCompat.isChecked = true
        } else {
            titleTv.setTextColor(Color.parseColor("#5DD0A3"))
            doneCheckBox.buttonTintList = ColorStateList.valueOf(Color.parseColor("#5DD0A3"))
            rightColoredLine.imageTintList = ColorStateList.valueOf(Color.parseColor("#5DD0A3"))
            urgentSwitchCompat.isChecked = false
        }
    }

    private fun PrivateTasksRvItemBinding.setDetailsVisibility(task: Task) {
        if (task.detailsVisibility) {
            descriptionLable.visibility = View.VISIBLE
            descriptionTv.visibility = View.VISIBLE
            urgentLable.visibility = View.VISIBLE
            urgentSwitchCompat.visibility = View.VISIBLE
            editCard.visibility = View.VISIBLE
        } else {
            descriptionLable.visibility = View.GONE
            descriptionTv.visibility = View.GONE
            urgentLable.visibility = View.GONE
            urgentSwitchCompat.visibility = View.GONE
            editCard.visibility = View.GONE
        }
    }
}

class PrivateTaskListAdapter(
    private val tasks: List<Task>,
    private val viewModel: PrivateTasksViewModel,
    private val editClickListener: PrivateTaskHolder.EditClickListener
    ): RecyclerView.Adapter<PrivateTaskHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrivateTaskHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PrivateTasksRvItemBinding.inflate(inflater, parent, false)
        return PrivateTaskHolder(binding, viewModel, editClickListener)
    }

    override fun onBindViewHolder(holder: PrivateTaskHolder, position: Int) {
        holder.bind(tasks, position, this)
    }

    override fun getItemCount() = tasks.size
}