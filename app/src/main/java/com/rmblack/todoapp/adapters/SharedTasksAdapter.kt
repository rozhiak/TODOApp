package com.rmblack.todoapp.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rmblack.todoapp.databinding.SharedTasksRvRowBinding
import com.rmblack.todoapp.models.Task
import com.rmblack.todoapp.viewmodels.SharedTasksViewModel

class SharedTaskHolder(
    private val binding: SharedTasksRvRowBinding,
    private val viewModel: SharedTasksViewModel
) : RecyclerView.ViewHolder(binding.root){
    fun bind(tasks: List<Task>, pos: Int, adapter: SharedTasksAdapter) {
        binding.apply {
            configUrgentSwitch(tasks, pos)
            configDoneCheckBox(tasks, pos)
            setDetailVisibility(tasks[pos])
            setUrgentUi(tasks[pos])
            setDoneUi(tasks, pos)
            setEachTaskClick(tasks, pos, adapter)
            setTaskDetails(tasks[pos])
        }
    }

    private fun SharedTasksRvRowBinding.configDoneCheckBox(
        tasks: List<Task>,
        pos: Int
    ) {
        doneCheckBox.setOnCheckedChangeListener { _, b ->
            viewModel.updateTasks { oldTasks ->
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
                    tarTask.detailsVisibility
                )
                updatedTasks[pos] = updatedTask
                updatedTasks
            }
            viewModel.updateDoneState(b, tasks[pos].id)
        }
    }

    private fun SharedTasksRvRowBinding.configUrgentSwitch(
        tasks: List<Task>,
        pos: Int
    ) {
        urgentSwitchCompat.setOnCheckedChangeListener { _, b ->
            viewModel.updateTasks { oldTasks ->
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
                    tarTask.detailsVisibility
                )
                updatedTasks[pos] = updatedTask
                updatedTasks
            }
            viewModel.updateUrgentState(b, tasks[pos].id)
        }
    }

    private fun SharedTasksRvRowBinding.setDoneUi(
        tasks: List<Task>,
        pos: Int
    ) {
        doneCheckBox.isChecked = tasks[pos].isDone
    }

    private fun SharedTasksRvRowBinding.setEachTaskClick(
        tasks: List<Task>,
        pos: Int,
        adapter: SharedTasksAdapter
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

    private fun SharedTasksRvRowBinding.setTaskDetails(task: Task) {
        titleTv.text = task.title
        deadLineTv.text = task.deadLine.shortDateString
        descriptionTv.text = task.description
        composerNameTv.text = task.user.name
    }

    private fun SharedTasksRvRowBinding.setUrgentUi(task: Task) {
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

    private fun SharedTasksRvRowBinding.setDetailVisibility(task: Task) {
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

class SharedTasksAdapter(private val tasks: List<Task>, private val viewModel: SharedTasksViewModel) : RecyclerView.Adapter<SharedTaskHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SharedTaskHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SharedTasksRvRowBinding.inflate(inflater, parent, false)
        return SharedTaskHolder(binding, viewModel)
    }

    override fun onBindViewHolder(holder: SharedTaskHolder, position: Int) {
        holder.bind(tasks, position, this)
    }

    override fun getItemCount() = tasks.size
}