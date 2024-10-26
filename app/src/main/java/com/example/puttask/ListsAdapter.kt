package com.example.puttask

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.puttask.api.Task
import java.text.SimpleDateFormat
import java.util.*

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

        // Set the task name and end date, truncating to 10 characters
        holder.tvTitle.text = if (task.task_name.length > 10) {
            "${task.task_name.take(15)}..."
        } else {
            task.task_name
        }

        holder.tvTime.text = task.end_date

        // Determine task status based on due date and create a background with corner radius
        val backgroundDrawable = holder.itemView.background as? GradientDrawable ?: GradientDrawable()

        backgroundDrawable.cornerRadius = 50f // Set your desired corner radius

        if (isTaskOverdue(task.end_date)) {
            backgroundDrawable.setColor(holder.itemView.context.getColor(R.color.very_light_red)) // Change to red
        } else {
            backgroundDrawable.setColor(holder.itemView.context.getColor(R.color.very_light_yellow)) // Change to yellow
        }

        holder.itemView.background = backgroundDrawable

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
        }
    }

    override fun getItemCount(): Int = taskList.size // Ensure this is correctly placed inside the class

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

    // Function to check if the task is overdue
    private fun isTaskOverdue(endDate: String): Boolean {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Adjust format as needed
        return try {
            val dueDate = formatter.parse(endDate) ?: return false
            val currentDate = Date()
            dueDate.before(currentDate) // Returns true if the due date is before the current date
        } catch (e: Exception) {
            false // Return false if parsing fails
        }
    }
}
