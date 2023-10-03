package com.rmblack.todoapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.rmblack.todoapp.adapters.viewholders.REMAINING_DAYS_LABLE
import com.rmblack.todoapp.adapters.viewholders.TASK
import com.rmblack.todoapp.adapters.viewholders.TaskHolder
import com.rmblack.todoapp.databinding.PrivateTasksRvItemBinding
import com.rmblack.todoapp.viewmodels.TasksViewModel

open class TaskAdapter(
    private val viewModel: TasksViewModel,
    private val editClickListener: TaskHolder.EditClickListener,
) : RecyclerView.Adapter<ViewHolder>() {

    lateinit var recyclerView: RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PrivateTasksRvItemBinding.inflate(inflater, parent, false)
        return TaskHolder(editClickListener, recyclerView, binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {}

    override fun getItemViewType(position: Int): Int {
        return if (position >= 0 && viewModel.tasks.value[position] != null) {
            TASK
        } else {
            REMAINING_DAYS_LABLE
        }
    }

    override fun getItemCount() = viewModel.tasks.value.size

}