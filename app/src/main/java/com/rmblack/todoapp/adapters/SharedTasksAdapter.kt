package com.rmblack.todoapp.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.rmblack.todoapp.adapters.viewholders.TaskHolder
import com.rmblack.todoapp.adapters.viewholders.REMAINING_DAYS_LABLE
import com.rmblack.todoapp.adapters.viewholders.RemainingDaysLableHolder
import com.rmblack.todoapp.adapters.viewholders.TASK
import com.rmblack.todoapp.databinding.RemainingDaysLableBinding
import com.rmblack.todoapp.databinding.SharedTasksRvItemBinding
import com.rmblack.todoapp.models.Task
import com.rmblack.todoapp.viewmodels.SharedTasksViewModel
import com.suke.widget.SwitchButton

class SharedTaskHolder(
    private val binding: SharedTasksRvItemBinding,
    private val viewModel: SharedTasksViewModel,
    editClickListener: EditClickListener,
    recyclerView: RecyclerView
) : TaskHolder(
    editClickListener,
    recyclerView,
    binding
) {

    fun bind(
        tasks: List<Task?>,
        pos: Int,
        adapter: SharedTasksAdapter
    ) {
        val task = tasks[pos]
        task?.let {
            binding.apply {
                configUrgentSwitch(it, pos, urgentSwitch)
                configDoneCheckBox(it, pos, doneCheckBox)
                if (pos in viewModel.detailsVisibility.indices) {
                    setDetailsVisibility(
                        viewModel.detailsVisibility[pos],
                        descriptionLable,
                        descriptionTv,
                        urgentLable,
                        urgentSwitch,
                        editCard,
                    )
                }
                setUrgentUi(it, titleTv, doneCheckBox, rightColoredLine, urgentSwitch)
                setDoneUi(it, doneCheckBox)
                setEachTaskClick(pos, adapter, rootCard)
                setTaskDetails(it, titleTv, deadLineTv, descriptionTv, descriptionLable)
                setEditClick(it, editCard)
                setBackground(pos, rootConstraint)
                composerNameTv.text = it.user.name
            }
        }
    }

    private fun setEachTaskClick(
        pos: Int,
        adapter: SharedTasksAdapter,
        rootCard: CardView
    ) {
        rootCard.setOnClickListener {
            if (pos in viewModel.detailsVisibility.indices && !viewModel.detailsVisibility[pos]) {
                for (i in viewModel.tasks.value.indices) {
                    if (i != pos && viewModel.detailsVisibility[i]) {
                        viewModel.updateVisibility(i, !viewModel.detailsVisibility[i])
                        adapter.notifyItemChanged(i)
                    }
                }
            }
            if (pos in viewModel.detailsVisibility.indices) {
                viewModel.updateVisibility(pos, !viewModel.detailsVisibility[pos])
            }
            adapter.notifyItemChanged(pos)
            recyclerView.post {
                recyclerView.smoothScrollToPosition(pos)
            }
        }
    }

    private fun setBackground(pos: Int, rootConstraint: ConstraintLayout) {
        if (pos in viewModel.detailsVisibility.indices && viewModel.detailsVisibility[pos]) {
            rootConstraint.setBackgroundColor(Color.parseColor("#f0fcf7"))
        } else {
            rootConstraint.setBackgroundColor(Color.parseColor("#19E2FFF3"))
        }
    }

    private fun configUrgentSwitch(
        task: Task,
        pos: Int,
        urgentSwitch: SwitchButton
    ) {
        urgentSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateUrgentState(isChecked, task.id, pos)
        }
    }

    private fun configDoneCheckBox(
        task: Task,
        pos: Int,
        doneCheckBox: AppCompatCheckBox
    ) {
        doneCheckBox.setOnCheckedChangeListener { _, b ->
            viewModel.updateDoneState(b, task.id, pos)
        }
    }
}

class SharedTasksAdapter(
    private val tasks: List<Task?>,
    private val viewModel: SharedTasksViewModel,
    private val editClickListener: TaskHolder.EditClickListener
) : RecyclerView.Adapter<ViewHolder>() {

    private lateinit var recyclerView: RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == REMAINING_DAYS_LABLE) {
            val binding = RemainingDaysLableBinding.inflate(inflater, parent, false)
            RemainingDaysLableHolder(binding)
        } else {
            val binding = SharedTasksRvItemBinding.inflate(inflater, parent, false)
            SharedTaskHolder(binding, viewModel, editClickListener, recyclerView)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is SharedTaskHolder) {
            holder.bind(tasks, position, this)
        } else if (holder is RemainingDaysLableHolder && position + 1 < tasks.size) {
            tasks[position+1]?.let { holder.bind(it.deadLine) }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position >= 0 && tasks[position] != null) {
            TASK
        } else {
            REMAINING_DAYS_LABLE
        }
    }

    override fun getItemCount() = tasks.size
}