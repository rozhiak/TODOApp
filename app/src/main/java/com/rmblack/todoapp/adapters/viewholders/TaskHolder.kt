package com.rmblack.todoapp.adapters.viewholders

import android.app.Activity
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.view.View
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.rmblack.todoapp.R
import com.rmblack.todoapp.adapters.TaskAdapter
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.utils.Utilities
import com.rmblack.todoapp.viewmodels.TasksViewModel
import com.suke.widget.SwitchButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

const val TASK = 0

const val REMAINING_DAYS_LABLE = 1

open class TaskHolder(
    private val scope: CoroutineScope?,
    private val isSyncing: StateFlow<Boolean>,
    private val editClickListener: EditClickListener,
    private val recyclerView: RecyclerView,
    binding: ViewBinding,
) : RecyclerView.ViewHolder(binding.root) {

    interface EditClickListener {
        fun onEditClick(task: Task)
    }

    fun setClickOnUrgentLable(urgentLable: AppCompatTextView, urgentSwitch: SwitchButton) {
        urgentLable.setOnClickListener {
            urgentSwitch.toggle()
        }
    }

    fun setEditClick(
        task: Task, editCard: CardView
    ) {
        editCard.setOnClickListener {
            editClickListener.onEditClick(task)
        }
        scope?.launch {
            isSyncing.collect {
                editCard.isEnabled = !it
            }
        }
    }

    fun setLongPress(task: Task, cardView: CardView) {
        cardView.setOnLongClickListener {
            if (!isSyncing.value) {
                editClickListener.onEditClick(task)
                return@setOnLongClickListener true
            }
            return@setOnLongClickListener true
        }
    }

    fun setDoneUi(
        task: Task,
        doneCheckBox: AppCompatCheckBox
    ) {
        doneCheckBox.isChecked = task.isDone
        scope?.launch {
            isSyncing.collect {
                doneCheckBox.isEnabled = !it
            }
        }
    }

    open fun setTaskDetails(
        task: Task,
        titleTv: AppCompatTextView,
        deadLineTv: AppCompatTextView,
        descriptionTv: AppCompatTextView,
        descriptionLable: AppCompatTextView
    ) {
        titleTv.text = task.title
        deadLineTv.text = task.deadLine.shortDateString
        if (task.description != "") {
            descriptionTv.text = task.description
            descriptionLable.text = "توضیحات:"
        } else {
            descriptionTv.text = ""
            descriptionLable.text = "توضیحات: -"
        }
    }

    fun setUrgentUi(
        task: Task,
        titleTv: AppCompatTextView,
        doneCheckBox: AppCompatCheckBox,
        rightColoredLine: AppCompatImageView,
        urgentSwitch: SwitchButton
    ) {
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

        scope?.launch {
            isSyncing.collect {
                urgentSwitch.isEnabled = !it
            }
        }
    }

    fun setDetailsVisibility(
        visibility: Boolean,
        descriptionLable: AppCompatTextView,
        descriptionTv: AppCompatTextView,
        urgentLable: AppCompatTextView,
        urgentSwitch: SwitchButton,
        editCard: CardView,
        deleteBtn: AppCompatImageView
    ) {
        if (visibility) {
            descriptionLable.visibility = View.VISIBLE
            descriptionTv.visibility = View.VISIBLE
            urgentLable.visibility = View.VISIBLE
            urgentSwitch.visibility = View.VISIBLE
            editCard.visibility = View.VISIBLE
            deleteBtn.visibility = View.VISIBLE
        } else {
            descriptionLable.visibility = View.GONE
            descriptionTv.visibility = View.GONE
            urgentLable.visibility = View.GONE
            urgentSwitch.visibility = View.GONE
            editCard.visibility = View.GONE
            deleteBtn.visibility = View.GONE
        }
    }

    fun configUrgentSwitch(
        viewModel: TasksViewModel,
        task: Task, urgentSwitch: SwitchButton
    ) {
        urgentSwitch.setOnCheckedChangeListener { _, isUrgent ->
            viewModel.updateUrgentState(isUrgent, task.id)
            viewModel.editTaskInServer(task.copy(isUrgent = isUrgent))
        }
    }

    fun configDoneCheckBox(
        viewModel: TasksViewModel,
        task: Task, doneCheckBox: AppCompatCheckBox
    ) {
        doneCheckBox.setOnCheckedChangeListener { _, isDone ->
            if (isDone != task.isDone) {
                viewModel.updateDoneState(isDone, task.id)
                viewModel.editTaskInServer(task.copy(isDone = isDone))
            }
        }
    }

    fun setBackground(
        viewModel: TasksViewModel, pos: Int, rootConstraint: ConstraintLayout, resources: Resources
    ) {
        if (viewModel.tasks.value[pos]?.detailsVisibility == true) {
            rootConstraint.setBackgroundColor(
                ResourcesCompat.getColor(resources, R.color.expanded_task_back_color, null)
            )
        } else {
            rootConstraint.setBackgroundColor(
                ResourcesCompat.getColor(resources, R.color.collapsed_task_back_color, null)
            )
        }
    }

    fun setUpDelete(
        pos: Int,
        task: Task,
        deleteBtn: AppCompatImageView,
        viewModel: TasksViewModel,
        activity: Activity,
    ) {
        deleteBtn.setOnClickListener {
            viewModel.deleteTask(task)
            val deleteReq = viewModel.makeDeleteRequest(task.serverID)
            if (deleteReq != null) {
                viewModel.cashDeleteRequest(deleteReq)
            }
            val snackBar = Utilities.makeDeleteSnackBar(activity, recyclerView) {
                viewModel.insertTask(task)
                recyclerView.post {
                    recyclerView.smoothScrollToPosition(pos)
                }
                if (deleteReq != null) {
                    viewModel.removeDeleteRequest(deleteReq)
                }
            }

            snackBar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    if (event != Snackbar.Callback.DISMISS_EVENT_MANUAL && deleteReq != null) {
                        viewModel.deleteTaskFromServer(deleteReq, task)
                    }
                }
            })
        }
    }

    fun setEachTaskClick(
        pos: Int, adapter: TaskAdapter, rootCard: CardView, viewModel: TasksViewModel
    ) {
        rootCard.setOnClickListener {
            if (viewModel.tasks.value[pos]?.detailsVisibility == false) {
                for (i in viewModel.tasks.value.indices) {
                    if (i != pos && viewModel.tasks.value[i]?.detailsVisibility == true) {
                        val oldVis = viewModel.tasks.value[i]?.detailsVisibility ?: false
                        viewModel.tasks.value[i]?.detailsVisibility = !oldVis
                        adapter.notifyItemChanged(i)
                        viewModel.tasks.value[i]?.id?.let { id ->
                            viewModel.updateDetailsVisibility(
                                !oldVis, id
                            )
                        }
                    }
                }
            }
            val oldVis = viewModel.tasks.value[pos]?.detailsVisibility ?: false
            viewModel.tasks.value[pos]?.detailsVisibility = !oldVis
            adapter.notifyItemChanged(pos)
            viewModel.tasks.value[pos]?.id?.let { id ->
                viewModel.updateDetailsVisibility(
                    !oldVis, id
                )
            }
            recyclerView.post {
                recyclerView.smoothScrollToPosition(pos)
            }
        }
    }
}