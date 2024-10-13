package com.example.puttask

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton // Import ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.puttask.api.Task

class ListsAdapter(
    private val taskList: MutableList<Task>,
    private val onTaskCheckedChange: (Task, Boolean) -> Unit,
    private val onItemClick: (Task) -> Unit // Add click listener for the entire item
) : RecyclerView.Adapter<ListsAdapter.TaskViewHolder>() {

    // Add a listener for delete clicks
    private var onDeleteClick: ((Task) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.lists_recyclerview, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.tvTitle.text = task.task_name
        holder.tvTime.text = task.start_datetime // Adjust this if needed
        holder.checkBox.isChecked = task.isChecked // Ensure `isChecked` is a property in your `Task` data class

        // Handle checkbox state change
        holder.checkBox.setOnCheckedChangeListener(null) // Clear previous listener
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onTaskCheckedChange(task, isChecked) // Notify the listener of the change
        }

        // Handle item click for navigation
        holder.itemView.setOnClickListener {
            onItemClick(task) // Trigger the onItemClick function when the item is clicked
        }

        // Handle delete button click
        holder.deleteOption.setOnClickListener {
            onDeleteClick?.invoke(task) // Invoke the delete click listener
        }
    }

    override fun getItemCount(): Int = taskList.size

    // Add a method to set the delete click listener
    fun setOnDeleteClickListener(listener: (Task) -> Unit) {
        onDeleteClick = listener
    }

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val deleteOption: ImageButton = itemView.findViewById(R.id.ic_delete) // Initialize delete button
    }
}
