package com.example.puttask

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.puttask.api.Task
import com.example.puttask.fragments.AddTask2

class ListsAdapter(
    private val taskList: MutableList<Task>,
    private val onTaskCheckedChange: (Task, Boolean) -> Unit,
    private val onItemClick: (Task) -> Unit // Add click listener for the entire item
) : RecyclerView.Adapter<ListsAdapter.TaskViewHolder>() {

    private var onDeleteClick: ((Task) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.lists_recyclerview, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.tvTitle.text = task.task_name
        holder.tvTime.text = task.end_date // Ensure this matches your desired format

        // Handle checkbox state change
        holder.checkBox.setOnCheckedChangeListener(null) // Clear previous listener
        holder.checkBox.isChecked = task.isChecked // Example property for checked state
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onTaskCheckedChange(task, isChecked) // Notify the listener of the change
        }

        // Handle item click for navigation
        holder.itemView.setOnClickListener {
            try {
                // Create the intent for AddTask2 activity
                val intent = Intent(holder.itemView.context, AddTask2::class.java).apply {
                    putExtra("task_name", task.task_name)
                    putExtra("end_date", task.end_date)
                    putExtra("end_time", task.end_time)
                    putExtra("task_id", task.id)
                    // Include any additional task properties as needed
                }

                // Start the AddTask2 activity
                holder.itemView.context.startActivity(intent)

                // Trigger the onItemClick function when the item is clicked
                onItemClick(task)
            } catch (e: Exception) {
                Toast.makeText(holder.itemView.context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace() // Log the exception for debugging
            }
        }

        // Handle delete button click
        holder.deleteOption.setOnClickListener {
            onDeleteClick?.invoke(task) // Invoke the delete click listener
        }
    }

    override fun getItemCount(): Int = taskList.size

    // Method to add a new task to the list and notify the adapter
    fun addTask(task: Task) {
        taskList.add(task)
        notifyItemInserted(taskList.size - 1) // Notify adapter of the new item
    }

    // Method to update the list of tasks, useful for refreshing after creation
    fun updateTasks(newTasks: List<Task>) {
        taskList.clear()
        taskList.addAll(newTasks)
        notifyDataSetChanged() // Notify adapter to refresh
    }

    // Method to set the delete click listener
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
