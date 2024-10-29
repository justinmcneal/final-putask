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
            "${task.task_name.take(20)}..."
        } else {
            task.task_name
        }

        // Convert end_time to 12-hour format with AM/PM
        val endDate = task.end_date
        val endTime24 = task.end_time
        val timeFormat24 = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeFormat12 = SimpleDateFormat("hh:mm a", Locale.getDefault()) // 12-hour format with AM/PM

        val formattedTime = try {
            val date = timeFormat24.parse(endTime24)
            timeFormat12.format(date!!).uppercase(Locale.getDefault())
        } catch (e: Exception) {
            endTime24 // Fallback to original if parsing fails
        }

        // Set the date and formatted time with AM/PM in tvTime
        holder.tvTime.text = "$endDate $formattedTime"

        // Determine task status based on due date and set background color
        val backgroundDrawable = holder.itemView.background as? GradientDrawable ?: GradientDrawable()
        backgroundDrawable.cornerRadius = 50f

        if (isTaskOverdue(task.end_date, task.end_time)) {
            backgroundDrawable.setColor(holder.itemView.context.getColor(R.color.very_light_red))
        } else {
            backgroundDrawable.setColor(holder.itemView.context.getColor(R.color.very_light_yellow))
        }

        holder.itemView.background = backgroundDrawable

        // Handle item click, delete option, and checkbox state
        holder.itemView.setOnClickListener { onItemClick(task) }
        holder.deleteOption.setOnClickListener { onDeleteClick?.invoke(task) }
        holder.checkBox.isChecked = task.isChecked
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            task.isChecked = isChecked
            onTaskCheckedChangeListener?.invoke(task, isChecked)
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
    private fun isTaskOverdue(endDate: String, endTime: String): Boolean {
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Date format
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault()) // Time format

        return try {
            // Parse end date and end time
            val dueDate = dateFormatter.parse(endDate) ?: return false
            val dueTime = timeFormatter.parse(endTime) ?: return false

            // Combine the date and time into a single Date object
            val dueDateTime = Calendar.getInstance().apply {
                time = dueDate
                set(Calendar.HOUR_OF_DAY, dueTime.hours)
                set(Calendar.MINUTE, dueTime.minutes)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val currentDateTime = Date() // Get current date and time
            dueDateTime.before(currentDateTime) // Returns true if the due date-time is before the current date-time
        } catch (e: Exception) {
            false // Return false if parsing fails
        }
    }
}
