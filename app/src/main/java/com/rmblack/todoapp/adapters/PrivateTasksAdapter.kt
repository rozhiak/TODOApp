package com.rmblack.todoapp.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rmblack.todoapp.databinding.PrivateTasksRvItemBinding
import com.rmblack.todoapp.models.Task

class PrivateTaskHolder(
     private val binding: PrivateTasksRvItemBinding
) :RecyclerView.ViewHolder(binding.root) {
    fun bind(tasks: List<Task>,
             pos: Int,
             adapter: PrivateTaskListAdapter) {
        binding.apply {
            setDetailsVisibility(tasks[pos])
            setUrgentUi(tasks[pos])
            setEachTaskClick(tasks, pos, adapter)
            setTaskDetails(tasks, pos)
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

class PrivateTaskListAdapter(private val tasks: List<Task>): RecyclerView.Adapter<PrivateTaskHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrivateTaskHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PrivateTasksRvItemBinding.inflate(inflater, parent, false)
        return PrivateTaskHolder(binding)
    }

    override fun onBindViewHolder(holder: PrivateTaskHolder, position: Int) {
        holder.bind(tasks, position, this)
    }

    override fun getItemCount() = tasks.size
}