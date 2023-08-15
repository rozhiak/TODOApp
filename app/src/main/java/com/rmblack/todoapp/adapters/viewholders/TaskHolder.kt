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
import com.aminography.primecalendar.persian.PersianCalendar
import com.rmblack.todoapp.adapters.PrivateTaskListAdapter
import com.rmblack.todoapp.databinding.PrivateTasksRvItemBinding
import com.rmblack.todoapp.databinding.PrivateTasksRvItemWithLableBinding
import com.rmblack.todoapp.models.Task
import com.rmblack.todoapp.viewmodels.PrivateTasksViewModel
import com.suke.widget.SwitchButton

open class TaskHolder(
    private val viewModel: PrivateTasksViewModel,
    private val editClickListener: EditClickListener,
    val recyclerView: RecyclerView,
    binding: ViewBinding
) :RecyclerView.ViewHolder(binding.root) {

    interface EditClickListener {
        fun onEditClick(task: Task)
    }

    fun setBackground(pos: Int, rootConstraint: ConstraintLayout) {
        if (viewModel.detailsVisibility[pos]) {
            rootConstraint.setBackgroundColor(Color.parseColor("#f0fcf7"))
        } else {
            rootConstraint.setBackgroundColor(Color.parseColor("#19E2FFF3"))
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

    fun configUrgentSwitch(
        task: Task,
        pos: Int,
        urgentSwitch: SwitchButton
    ) {
        urgentSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateUrgentState(isChecked, task.id, pos)
        }
    }

    fun configDoneCheckBox(
        task: Task,
        pos: Int,
        doneCheckBox: AppCompatCheckBox
    ) {
        doneCheckBox.setOnCheckedChangeListener { _, b ->
            viewModel.updateDoneState(b, task.id, pos)
        }
    }

    fun setTaskDetails(
        task: Task,
        titleTv: AppCompatTextView,
        deadLineTv: AppCompatTextView,
        descriptionTv: AppCompatTextView
    ) {
        titleTv.text = task.title
        deadLineTv.text = task.deadLine.shortDateString
        descriptionTv.text = task.description
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
        editCard: CardView
    ) {
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

    fun calculateDateDistance(calendarDate: PersianCalendar) : Int {
        val currentDate = PersianCalendar()
        val dateInMillis = calendarDate.timeInMillis
        val currentDateInMillis = currentDate.timeInMillis

        val millisInDay = 1000 * 60 * 60 * 24
        val distanceInMillis = dateInMillis - currentDateInMillis

        return distanceInMillis.toInt() / millisInDay
    }

//    fun bind(
//        tasks: List<Task>,
//        pos: Int,
//        adapter: PrivateTaskListAdapter
//    ) {
//        val binding = if (this.binding is PrivateTasksRvItemBinding) {
//            this.binding as PrivateTasksRvItemBinding
//        } else {
//            this.binding as PrivateTasksRvItemWithLableBinding
//        }
//        val task = tasks[pos]
//        binding.apply {
//            configUrgentSwitch(task, pos, urgentSwitch)
//            configDoneCheckBox(task, pos, doneCheckBox)
//            setDetailsVisibility(
//                viewModel.detailsVisibility[pos],
//                descriptionLable,
//                descriptionTv,
//                urgentLable,
//                urgentSwitch,
//                editCard
//            )
//            setUrgentUi(task, titleTv, doneCheckBox, rightColoredLine, urgentSwitch)
//            setDoneUi(task, doneCheckBox)
//            setEachTaskClick(pos, adapter, rootCard)
//            setTaskDetails(task, titleTv, deadLineTv, descriptionTv)
//            setEditClick(task, editCard)
//            setBackground(pos, rootConstraint)
//        }
//    }

}