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
import com.rmblack.todoapp.viewmodels.PrivateTasksViewModel

class PrivateTaskHolder(
     private val binding: PrivateTasksRvItemBinding,
     private val viewModel: PrivateTasksViewModel,
     private val editClickListener: EditClickListener,
     private val recyclerView: RecyclerView
) :RecyclerView.ViewHolder(binding.root) {

    interface EditClickListener {
        fun onEditClick(task: Task)
    }

    fun bind(
        tasks: List<Task>,
        pos: Int,
        adapter: PrivateTaskListAdapter
    ) {
        val task = tasks[pos]
        binding.apply {
            configUrgentSwitch(task, pos)
            configDoneCheckBox(task, pos)
            setDetailsVisibility(viewModel.detailsVisibility[pos])
            setUrgentUi(task)
            setDoneUi(task)
            setEachTaskClick(pos, adapter)
            setTaskDetails(task)
            setEditClick(task)
            setBackground(pos)
        }
    }

    private fun PrivateTasksRvItemBinding.setBackground(pos: Int) {
        if (viewModel.detailsVisibility[pos]) {
            rootConstraint.setBackgroundColor(Color.parseColor("#f0fcf7"))
        } else {
            rootConstraint.setBackgroundColor(Color.parseColor("#19E2FFF3"))
        }
    }

    private fun PrivateTasksRvItemBinding.setEditClick(
        task: Task
    ) {
        editCard.setOnClickListener {
            editClickListener.onEditClick(task)
        }
    }

    private fun PrivateTasksRvItemBinding.setDoneUi(
        task: Task
    ) {
        doneCheckBox.isChecked = task.isDone
    }

    private fun PrivateTasksRvItemBinding.configUrgentSwitch(
        task: Task,
        pos: Int
    ) {
        urgentSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateUrgentState(isChecked, task.id, pos)
        }
    }

    private fun PrivateTasksRvItemBinding.configDoneCheckBox(
        task: Task,
        pos: Int
    ) {
        doneCheckBox.setOnCheckedChangeListener { _, b ->
            viewModel.updateDoneState(b, task.id, pos)
        }
    }

    private fun PrivateTasksRvItemBinding.setEachTaskClick(
        pos: Int,
        adapter: PrivateTaskListAdapter
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
            recyclerView.post {
                recyclerView.smoothScrollToPosition(pos)
            }
        }
    }

    private fun PrivateTasksRvItemBinding.setTaskDetails(
        task: Task
    ) {
        titleTv.text = task.title
        deadLineTv.text = task.deadLine.shortDateString
        descriptionTv.text = task.description
    }

    private fun PrivateTasksRvItemBinding.setUrgentUi(task: Task) {
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

    private fun PrivateTasksRvItemBinding.setDetailsVisibility(visibility: Boolean) {
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

class PrivateTaskListAdapter(
    private val tasks: List<Task>,
    private val viewModel: PrivateTasksViewModel,
    private val editClickListener: PrivateTaskHolder.EditClickListener
    ): RecyclerView.Adapter<PrivateTaskHolder>() {

    private lateinit var recyclerView: RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrivateTaskHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PrivateTasksRvItemBinding.inflate(inflater, parent, false)
        return PrivateTaskHolder(binding, viewModel, editClickListener, recyclerView)
    }

    override fun onBindViewHolder(holder: PrivateTaskHolder, position: Int) {
        holder.bind(tasks, position, this)
    }

    override fun getItemCount() = tasks.size
}