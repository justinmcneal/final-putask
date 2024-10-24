package com.example.puttask

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.puttask.api.Task

class ListsAdapter(
    private val taskList: MutableList<Task>,
    private val onItemClick: (Task) -> Unit
) : RecyclerView.Adapter<ListsAdapter.TaskViewHolder>() {

    private var onDeleteClick: ((Task) -> Unit)? = null
    var onTaskCheckedChangeListener: ((Task, Boolean) -> Unit)? = null // Listener for checkbox changes

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.lists_recyclerview, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]

        // Set the task name and end date
        holder.tvTitle.text = task.task_name
        holder.tvTime.text = task.end_date

        // Handle click to edit the task
        holder.itemView.setOnClickListener {
            onItemClick(task)
        }

        // Handle delete task option
        holder.deleteOption.setOnClickListener {
            onDeleteClick?.invoke(task)
        }

        // Set the checkbox state based on the task's isChecked status
        holder.checkBox.isChecked = task.isChecked

        // Clear previous listener to avoid triggering the listener on initialization
        holder.checkBox.setOnCheckedChangeListener(null)

        // Set a listener on the checkbox
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            // Update the task's isChecked property
            task.isChecked = isChecked

            // Notify the listener for the change
            onTaskCheckedChangeListener?.invoke(task, isChecked) // Notify parent fragment

            // Mark as complete if checked
            if (isChecked) {
                // Call the method to mark the task as complete
                onTaskCheckedChangeListener?.invoke(task, true) // Optionally, pass true or handle it accordingly
            }
        }
    }

    override fun getItemCount(): Int = taskList.size

    // Set the delete listener to handle task deletion
    fun setOnDeleteClickListener(listener: (Task) -> Unit) {
        onDeleteClick = listener
    }

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val deleteOption: ImageButton = itemView.findViewById(R.id.ic_delete)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
    }
}

