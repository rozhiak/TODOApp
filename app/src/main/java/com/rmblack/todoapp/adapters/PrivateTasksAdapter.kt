package com.rmblack.todoapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.rmblack.todoapp.adapters.viewholders.TaskHolder
import com.rmblack.todoapp.databinding.PrivateTasksRvItemBinding
import com.rmblack.todoapp.databinding.PrivateTasksRvItemWithLableBinding
import com.rmblack.todoapp.models.Task
import com.rmblack.todoapp.utils.PersianNum
import com.rmblack.todoapp.viewmodels.PrivateTasksViewModel

class PrivateTaskHolder(
    private val binding: PrivateTasksRvItemBinding,
    private val viewModel: PrivateTasksViewModel,
    editClickListener: EditClickListener,
    recyclerView: RecyclerView
) : TaskHolder(viewModel, editClickListener, recyclerView, binding) {

    fun bind(
        tasks: List<Task>,
        pos: Int,
        adapter: PrivateTaskListAdapter
    ) {
        val task = tasks[pos]
        binding.apply {
            configUrgentSwitch(task, pos, urgentSwitch)
            configDoneCheckBox(task, pos, doneCheckBox)
            setDetailsVisibility(
                viewModel.detailsVisibility[pos],
                descriptionLable,
                descriptionTv,
                urgentLable,
                urgentSwitch,
                editCard
            )
            setUrgentUi(task, titleTv, doneCheckBox, rightColoredLine, urgentSwitch)
            setDoneUi(task, doneCheckBox)
            setEachTaskClick(pos, adapter, rootCard)
            setTaskDetails(task, titleTv, deadLineTv, descriptionTv)
            setEditClick(task, editCard)
            setBackground(pos, rootConstraint)
        }
    }

    private fun setEachTaskClick(
        pos: Int,
        adapter: PrivateTaskListAdapter,
        rootCard: CardView
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
}

class PrivateTaskHolderWithLable(
    private val binding: PrivateTasksRvItemWithLableBinding,
    private val viewModel: PrivateTasksViewModel,
    editClickListener: EditClickListener,
    recyclerView: RecyclerView
) : TaskHolder(
    viewModel,
    editClickListener,
    recyclerView,
    binding
) {
    fun bind(
        tasks: List<Task>,
        pos: Int,
        adapter: PrivateTaskListAdapter
    ) {
        val task = tasks[pos]
        binding.apply {
            configUrgentSwitch(task, pos, urgentSwitch)
            configDoneCheckBox(task, pos, doneCheckBox)
            setDetailsVisibility(
                viewModel.detailsVisibility[pos],
                descriptionLable,
                descriptionTv,
                urgentLable,
                urgentSwitch,
                editCard
            )
            setUrgentUi(task, titleTv, doneCheckBox, rightColoredLine, urgentSwitch)
            setDoneUi(task, doneCheckBox)
            setEachTaskClick(pos, adapter, rootCard)
            setTaskDetails(task, titleTv, deadLineTv, descriptionTv)
            setEditClick(task, editCard)
            setBackground(pos, rootConstraint)
            setRemainingDays(task, remainingDaysTv)
        }
    }

    private fun setEachTaskClick(
        pos: Int,
        adapter: PrivateTaskListAdapter,
        rootCard: CardView
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

    private fun setRemainingDays(task: Task, remainingDaysTv: AppCompatTextView) {
        remainingDaysTv.text = PersianNum.convert(calculateDateDistance(task.deadLine).toString())
    }
}

class PrivateTaskListAdapter(
    private val tasks: List<Task>,
    private val viewModel: PrivateTasksViewModel,
    private val editClickListener: TaskHolder.EditClickListener
) : RecyclerView.Adapter<TaskHolder>() {

    private lateinit var recyclerView: RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == WITH_DATE_LABLE) {
            val binding = PrivateTasksRvItemWithLableBinding.inflate(inflater, parent, false)
            PrivateTaskHolderWithLable(binding, viewModel, editClickListener, recyclerView)
        } else {
            val binding = PrivateTasksRvItemBinding.inflate(inflater, parent, false)
            PrivateTaskHolder(binding, viewModel, editClickListener, recyclerView)
        }
    }

    override fun onBindViewHolder(holder: TaskHolder, position: Int) {
        if (holder is PrivateTaskHolder) {
            holder.bind(tasks, position, this)
        } else if (holder is PrivateTaskHolderWithLable) {
            holder.bind(tasks, position, this)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position > 0 && tasks[position].deadLine.date != tasks[position - 1].deadLine.date) {
            WITH_DATE_LABLE
        } else if (position == 0) {
            WITH_DATE_LABLE
        } else {
            WITHOUT_DATE_LABLE
        }
    }

    override fun getItemCount() = tasks.size
}