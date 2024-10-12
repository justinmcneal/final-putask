package com.example.puttask

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.puttask.api.Task

class ListsAdapter(
    private val taskList: MutableList<Task>,
    private val onTaskCheckedChange: (Task, Boolean) -> Unit
) : RecyclerView.Adapter<ListsAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.lists_recyclerview, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.tvTitle.text = task.task_name
        holder.tvTaskDescription.text = task.task_description
        holder.tvTime.text = task.start_datetime // Adjust this if needed
        holder.checkBox.isChecked = task.isChecked // Ensure `isChecked` is a property in your `Task` data class

        // Handle checkbox state change
        holder.checkBox.setOnCheckedChangeListener(null) // Clear previous listener
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onTaskCheckedChange(task, isChecked) // Notify the listener of the change
        }
    }

    override fun getItemCount(): Int = taskList.size

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvTaskDescription: TextView = itemView.findViewById(R.id.tvTaskDescription)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
    }
}
