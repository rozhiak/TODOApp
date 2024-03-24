package com.rmblack.todoapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rmblack.todoapp.databinding.EventsRvItemBinding
import com.rmblack.todoapp.models.local.Task

class EventHolder(private val binding: EventsRvItemBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(event: Task) {
        binding.tvTitle.text = event.title
    }
}

class EventsAdapter(private val events: List<Task>): RecyclerView.Adapter<EventHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = EventsRvItemBinding.inflate(inflater, parent, false)
        return EventHolder(binding)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    override fun onBindViewHolder(holder: EventHolder, position: Int) {
        holder.bind(events[position])
    }
}