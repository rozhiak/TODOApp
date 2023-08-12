package com.rmblack.todoapp.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rmblack.todoapp.databinding.PrivateTasksRvItemBinding
import com.rmblack.todoapp.databinding.SharedTasksRvRowBinding
import com.rmblack.todoapp.models.Task
import com.rmblack.todoapp.viewmodels.SharedTasksViewModel

class SharedTaskHolder(
    private val binding: SharedTasksRvRowBinding,
    private val viewModel: SharedTasksViewModel,
    private val editClickListener: EditClickListener
) : RecyclerView.ViewHolder(binding.root){

    interface EditClickListener {
        fun onEditClick(task: Task)
    }

    fun bind(tasks: List<Task>, pos: Int, adapter: SharedTasksAdapter) {
        binding.apply {
            configUrgentSwitch(tasks, pos)
            configDoneCheckBox(tasks, pos)
            setDetailVisibility(viewModel.detailsVisibility[pos])
            setUrgentUi(tasks[pos])
            setDoneUi(tasks, pos)
            setEachTaskClick(pos, adapter)
            setTaskDetails(tasks[pos])
            setEditClick(tasks, pos)
            setBackground(pos)
        }
    }

    private fun SharedTasksRvRowBinding.setBackground(pos: Int) {
        if (viewModel.detailsVisibility[pos]) {
            rootConstraint.setBackgroundColor(Color.parseColor("#f0fcf7"))
        } else {
            rootConstraint.setBackgroundColor(Color.parseColor("#19E2FFF3"))
        }
    }

    private fun SharedTasksRvRowBinding.setEditClick(
        tasks: List<Task>,
        pos: Int
    ) {
        editCard.setOnClickListener {
            editClickListener.onEditClick(tasks[pos])
        }
    }

    private fun SharedTasksRvRowBinding.configDoneCheckBox(
        tasks: List<Task>,
        pos: Int
    ) {
        doneCheckBox.setOnCheckedChangeListener { _, b ->
            viewModel.updateDoneState(b, tasks[pos].id, pos)
        }
    }

    private fun SharedTasksRvRowBinding.configUrgentSwitch(
        tasks: List<Task>,
        pos: Int
    ) {
        urgentSwitch.setOnCheckedChangeListener { _, b ->
            viewModel.updateUrgentState(b, tasks[pos].id, pos)
        }
    }

    private fun SharedTasksRvRowBinding.setDoneUi(
        tasks: List<Task>,
        pos: Int
    ) {
        doneCheckBox.isChecked = tasks[pos].isDone
    }

    private fun SharedTasksRvRowBinding.setEachTaskClick(
        pos: Int,
        adapter: SharedTasksAdapter
    ) {
        rootCard.setOnClickListener {
            if (!viewModel.detailsVisibility[pos]) {
                for (i in viewModel.detailsVisibility.indices) {
                    if (i != pos && viewModel.detailsVisibility[i]) {
                        viewModel.updateVisibility(i, !viewModel.detailsVisibility[i])
                        adapter.notifyItemChanged(i)
                    }
                }
            }
            viewModel.updateVisibility(pos, !viewModel.detailsVisibility[pos])
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
            urgentSwitch.isChecked = true
        } else {
            titleTv.setTextColor(Color.parseColor("#5DD0A3"))
            doneCheckBox.buttonTintList = ColorStateList.valueOf(Color.parseColor("#5DD0A3"))
            rightColoredLine.imageTintList = ColorStateList.valueOf(Color.parseColor("#5DD0A3"))
            urgentSwitch.isChecked = false
        }
    }

    private fun SharedTasksRvRowBinding.setDetailVisibility(visibility: Boolean) {
        if (visibility) {
            descriptionLable.visibility = View.VISIBLE
            descriptionTv.visibility = View.VISIBLE
            urgentLable.visibility = View.VISIBLE
            urgentSwitch.visibility = View.VISIBLE
            editCard.visibility = View.VISIBLE
        } else {
            descriptionLable.visibility = View.GONE
            descriptionTv.visibility = View.GONE
            urgentLable.visibility = View.GONE
            urgentSwitch.visibility = View.GONE
            editCard.visibility = View.GONE
        }
    }
}

class SharedTasksAdapter(
    private val tasks: List<Task>,
    private val viewModel: SharedTasksViewModel,
    private val editClickListener: SharedTaskHolder.EditClickListener) : RecyclerView.Adapter<SharedTaskHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SharedTaskHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SharedTasksRvRowBinding.inflate(inflater, parent, false)
        return SharedTaskHolder(binding, viewModel, editClickListener)
    }

    override fun onBindViewHolder(holder: SharedTaskHolder, position: Int) {
        holder.bind(tasks, position, this)
    }

    override fun getItemCount() = tasks.size
}