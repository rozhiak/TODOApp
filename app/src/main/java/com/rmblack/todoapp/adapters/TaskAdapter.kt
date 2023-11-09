package com.rmblack.todoapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.rmblack.todoapp.adapters.viewholders.REMAINING_DAYS_LABLE
import com.rmblack.todoapp.adapters.viewholders.TASK
import com.rmblack.todoapp.adapters.viewholders.TaskHolder
import com.rmblack.todoapp.databinding.PrivateTasksRvItemBinding
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.utils.Utilities.Companion.calculateDateDistance
import com.rmblack.todoapp.viewmodels.TasksViewModel
import kotlinx.coroutines.flow.MutableStateFlow

open class TaskAdapter(
    private val viewModel: TasksViewModel,
    private val editClickListener: TaskHolder.EditClickListener,
) : RecyclerView.Adapter<ViewHolder>() {

    private var tasks: List<Task?> = emptyList()

    lateinit var recyclerView: RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PrivateTasksRvItemBinding.inflate(inflater, parent, false)
        return TaskHolder(null, editClickListener, recyclerView, binding)
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

    fun updateData(newTasks: List<Task?>) {
        val diffResult = DiffUtil.calculateDiff(TaskDiffCallback(tasks, newTasks))
        diffResult.dispatchUpdatesTo(this)
        tasks = newTasks
    }
}

class TaskDiffCallback(
    private val oldTasks: List<Task?>,
    private val newTasks: List<Task?>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldTasks.size
    override fun getNewListSize(): Int = newTasks.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        if (newListSize != 1 && oldListSize != 1) {
            val old = oldTasks[oldItemPosition]
            val new = newTasks[newItemPosition]
            return if (old == null && new == null) {
                val dis1 = oldTasks[oldItemPosition + 1]?.let { calculateDateDistance(it.deadLine) }
                val dis2 = newTasks[newItemPosition + 1]?.let { calculateDateDistance(it.deadLine) }
                val dis1InDay = dis1?.let { kotlin.math.floor(it).toInt() }
                val dis2InDay = dis2?.let { kotlin.math.floor(it).toInt() }
                dis1InDay == dis2InDay
            } else if (old == null || new == null) {
                false
            } else {
                new.id == old.id
            }
        }
        return false
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        if (newListSize != 1 && oldListSize != 1) {
            if (oldItemPosition in oldTasks.indices && oldItemPosition in newTasks.indices) {
                val old = oldTasks[oldItemPosition]
                val new = newTasks[oldItemPosition]
                if (old == null && new == null) {
                    val dis1 = oldTasks[oldItemPosition + 1]?.let { calculateDateDistance(it.deadLine) }
                    val dis2 = newTasks[newItemPosition + 1]?.let { calculateDateDistance(it.deadLine) }
                    val dis1InDay = dis1?.let { kotlin.math.floor(it).toInt() }
                    val dis2InDay = dis2?.let { kotlin.math.floor(it).toInt() }
                    return dis1InDay == dis2InDay
                } else if (old == null || new == null) {
                    return false
                } else {
                    if (old.isDone != new.isDone) {
                        return false
                    } else if (old.id != new.id) {
                        return false
                    } else if (old.isUrgent != new.isUrgent) {
                        return false
                    } else if (old.isShared != new.isShared) {
                        return false
                    } else if (old.title != new.title) {
                        return false
                    } else if (old.deadLine != new.deadLine) {
                        return false
                    } else if (old.composer != new.composer) {
                        return false
                    } else if (old.description != new.description) {
                        return false
                    }
                    return true
                }
            }
        }
        return false
    }
}