package com.example.puttask

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListsAdapter(private val taskList: List<Task.Task>) : RecyclerView.Adapter<ListsAdapter.TaskViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.lists_recyclerview, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.tvTitle.text = task.title
        holder.tvTaskDescription.text = task.description
        holder.tvTime.text = task.time
        holder.checkBox.isChecked = task.isChecked
    }

    override fun getItemCount(): Int = taskList.size
    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvTaskDescription: TextView = itemView.findViewById(R.id.tvTaskDescription)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
    }
}
