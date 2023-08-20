package com.rmblack.todoapp.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.rmblack.todoapp.adapters.viewholders.TaskHolder
import com.rmblack.todoapp.adapters.viewholders.WITHOUT_DATE_LABLE
import com.rmblack.todoapp.adapters.viewholders.WITH_DATE_LABLE
import com.rmblack.todoapp.databinding.SharedTasksRvItemWithLableBinding
import com.rmblack.todoapp.databinding.SharedTasksRvRowBinding
import com.rmblack.todoapp.models.Task
import com.rmblack.todoapp.utils.PersianNum
import com.rmblack.todoapp.viewmodels.SharedTasksViewModel
import com.suke.widget.SwitchButton
import kotlin.math.ceil

class SharedTaskHolder(
    private val binding: SharedTasksRvRowBinding,
    private val viewModel: SharedTasksViewModel,
    editClickListener: EditClickListener,
    recyclerView: RecyclerView
) : TaskHolder(
    editClickListener,
    recyclerView,
    binding
) {

    fun bind(
        tasks: List<Task>,
        pos: Int,
        adapter: SharedTasksAdapter
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
            composerNameTv.text = task.user.name
        }
    }

    private fun setEachTaskClick(
        pos: Int,
        adapter: SharedTasksAdapter,
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

    private fun setBackground(pos: Int, rootConstraint: ConstraintLayout) {
        if (viewModel.detailsVisibility[pos]) {
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

class SharedTaskHolderWithLable(
    private val binding: SharedTasksRvItemWithLableBinding,
    private val viewModel: SharedTasksViewModel,
    editClickListener: EditClickListener,
    recyclerView: RecyclerView
) : TaskHolder(
    editClickListener,
    recyclerView,
    binding
) {
    fun bind(
        tasks: List<Task>,
        pos: Int,
        adapter: SharedTasksAdapter
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
            setRemainingDays(task, remainingDaysTv, remainingDaysLable)
            composerNameTv.text = task.user.name
        }
    }

    private fun setEachTaskClick(
        pos: Int,
        adapter: SharedTasksAdapter,
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

    private fun setRemainingDays(task: Task, remainingDaysTv: AppCompatTextView, remainingDaysLable: AppCompatTextView) {
        val disInDays = ceil(calculateDateDistance(task.deadLine)).toInt()
        if (disInDays == 0) {
            remainingDaysLable.text = ""
            remainingDaysTv.text = "امروز"
        } else if(disInDays == 1) {
            remainingDaysLable.text = ""
            remainingDaysTv.text = "فردا"
        } else if (disInDays == -1) {
            remainingDaysLable.text = ""
            remainingDaysTv.text = "دیروز"
        } else if (disInDays > 1) {
            remainingDaysTv.text = PersianNum.convert(disInDays.toString())
            remainingDaysLable.visibility = View.VISIBLE
            remainingDaysLable.text = "روز باقی مانده"
        } else {
            remainingDaysTv.text = PersianNum.convert((-1 * disInDays).toString())
            remainingDaysLable.visibility = View.VISIBLE
            remainingDaysLable.text = "روز گذشته"
        }
    }

    private fun setBackground(pos: Int, rootConstraint: ConstraintLayout) {
        if (viewModel.detailsVisibility[pos]) {
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
    private val tasks: List<Task>,
    private val viewModel: SharedTasksViewModel,
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
            val binding = SharedTasksRvItemWithLableBinding.inflate(inflater, parent, false)
            SharedTaskHolderWithLable(binding, viewModel, editClickListener, recyclerView)
        } else {
            val binding = SharedTasksRvRowBinding.inflate(inflater, parent, false)
            SharedTaskHolder(binding, viewModel, editClickListener, recyclerView)
        }
    }

    override fun onBindViewHolder(holder: TaskHolder, position: Int) {
        if (holder is SharedTaskHolder) {
            holder.bind(tasks, position, this)
        } else if (holder is SharedTaskHolderWithLable) {
            holder.bind(tasks, position, this)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (
            position > 0
            &&
            tasks[position].deadLine.shortDateString != tasks[position - 1].deadLine.shortDateString
        ) {
            WITH_DATE_LABLE
        } else if (position == 0) {
            WITH_DATE_LABLE
        } else {
            WITHOUT_DATE_LABLE
        }
    }

    override fun getItemCount() = tasks.size
}