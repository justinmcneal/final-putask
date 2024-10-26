package com.example.puttask

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.puttask.api.Task

class TimelineAdapter(
    private val taskList: List<Task>
) : RecyclerView.Adapter<TimelineAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.timeline_recyclerview, parent, false) // Use a simplified layout without checkbox or delete button
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.tvTitle.text = task.task_name
        holder.tvTime.text = task.end_date

        // Set the task name and end date, truncating to 10 characters
        holder.tvTitle.text = if (task.task_name.length > 10) {
            "${task.task_name.take(15)}..."
        } else {
            task.task_name
        }
    }

    override fun getItemCount(): Int = taskList.size

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
    }
}
