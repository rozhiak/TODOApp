package com.rmblack.todoapp.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.rmblack.todoapp.R
import com.rmblack.todoapp.databinding.EventsRvItemBinding
import com.rmblack.todoapp.models.local.Task

class EventHolder(private val binding: EventsRvItemBinding, private val context: Context) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(event: Task) {
        binding.tvTitle.text = event.title
        if (event.isUrgent) {
            val urgentRed = ResourcesCompat.getColor(context.resources, R.color.urgent_red, null)
            binding.tvTitle.setTextColor(urgentRed)
            binding.verticalLine.imageTintList = ColorStateList.valueOf(urgentRed)
        } else {
            val green = ResourcesCompat.getColor(context.resources, R.color.green, null)
            binding.tvTitle.setTextColor(green)
            binding.verticalLine.imageTintList = ColorStateList.valueOf(green)
        }
        binding.ivShare.visibility = if (event.isShared) {
            View.VISIBLE
        } else {
            View.GONE
        }
        binding.tvDescription.visibility = if (event.description.isNotEmpty()) {
            binding.tvDescription.text = event.description
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}

class EventsAdapter(private val context: Context) :
    RecyclerView.Adapter<EventHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = EventsRvItemBinding.inflate(inflater, parent, false)
        return EventHolder(binding, context)
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }

    override fun onBindViewHolder(holder: EventHolder, position: Int) {
        holder.bind(asyncListDiffer.currentList[position])
    }

    private val diffUtil = object : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task):
                Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task):
                Boolean {
            return oldItem == newItem
        }
    }

    private val asyncListDiffer = AsyncListDiffer(this, diffUtil)

    fun setData(newData: List<Task>) {
        asyncListDiffer.submitList(newData)
    }
}