package com.rmblack.todoapp.adapters.viewholders

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.models.server.requests.EditTaskRequest
import com.rmblack.todoapp.viewmodels.TasksViewModel
import com.rmblack.todoapp.webservice.repository.ApiRepository
import com.suke.widget.SwitchButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

const val TASK = 0

const val REMAINING_DAYS_LABLE = 1

open class TaskHolder(
    private val token: String?,
    private val editClickListener: EditClickListener,
    val recyclerView: RecyclerView,
    binding: ViewBinding,

) :RecyclerView.ViewHolder(binding.root) {

    val apiRepository = ApiRepository()

    interface EditClickListener {
        fun onEditClick(task: Task)
    }

    fun setClickOnUrgentLable(urgentLable: AppCompatTextView, urgentSwitch: SwitchButton) {
        urgentLable.setOnClickListener {
            urgentSwitch.toggle()
        }
    }

    fun setEditClick(
        task: Task,
        editCard: CardView
    ) {
        editCard.setOnClickListener {
            editClickListener.onEditClick(task)
        }
    }

    fun setDoneUi(
        task: Task,
        doneCheckBox: AppCompatCheckBox
    ) {
        doneCheckBox.isChecked = task.isDone
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
        task: Task,
        urgentSwitch: SwitchButton
    ) {
        urgentSwitch.setOnCheckedChangeListener { _, isUrgent ->
            viewModel.updateUrgentState(isUrgent, task.id)
            viewModel.editTaskInServer(task.copy(isUrgent = isUrgent))
        }
    }

    fun configDoneCheckBox(
        viewModel: TasksViewModel,
        task: Task,
        doneCheckBox: AppCompatCheckBox
    ) {
        doneCheckBox.setOnCheckedChangeListener { _, isDone ->
            viewModel.updateDoneState(isDone, task.id)
            viewModel.editTaskInServer(task.copy(isDone = isDone))
        }
    }

    fun setBackground(
        viewModel: TasksViewModel,
        pos: Int,
        rootConstraint: ConstraintLayout
    ) {
        if (pos in viewModel.detailsVisibility.indices && viewModel.detailsVisibility[pos]) {
            rootConstraint.setBackgroundColor(Color.parseColor("#f0fcf7"))
        } else {
            rootConstraint.setBackgroundColor(Color.parseColor("#19E2FFF3"))
        }
    }

}