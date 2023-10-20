package com.rmblack.todoapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rmblack.todoapp.databinding.ConnectedPhoneRvItemBinding

class ConnectedPhoneHolder(
    private val binding: ConnectedPhoneRvItemBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(phone: String) {
        binding.connectedPhoneTv.text = phone
    }
}

class ConnectedPhonesAdapter(private val phones: List<String>) : RecyclerView.Adapter<ConnectedPhoneHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConnectedPhoneHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ConnectedPhoneRvItemBinding.inflate(inflater, parent, false)
        return ConnectedPhoneHolder(binding)
    }

    override fun getItemCount(): Int {
        return phones.size
    }

    override fun onBindViewHolder(holder: ConnectedPhoneHolder, position: Int) {
        holder.bind(phones[position])
    }
}