package com.rmblack.todoapp.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rmblack.todoapp.R
import com.rmblack.todoapp.databinding.PrivateTasksRvItemBinding
import com.rmblack.todoapp.models.Task

class TaskHolder(
     private val binding: PrivateTasksRvItemBinding
) :RecyclerView.ViewHolder(binding.root) {
    fun bind(task: Task) {
        binding.apply {
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
            titleTv.text = task.title
            deadLineTv.text = task.deadLine.shortDateString
            descriptionTv.text = task.description
        }
    }
}

class PrivateTaskListAdapter(private val tasks: List<Task>): RecyclerView.Adapter<TaskHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PrivateTasksRvItemBinding.inflate(inflater, parent, false)
        return TaskHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task)
    }

    override fun getItemCount() = tasks.size

}