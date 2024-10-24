package com.example.puttask

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.puttask.api.Task
import com.example.puttask.fragments.Analytics

class ListsAdapter(
    private val taskList: MutableList<Task>,
    private val onItemClick: (Task) -> Unit

) : RecyclerView.Adapter<ListsAdapter.TaskViewHolder>() {

//    companion object {
//        const val REQUEST_CODE_EDIT_TASK = 1001
//    }

    private var onDeleteClick: ((Task) -> Unit)? = null

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

        // Handle delete task optio     n
        holder.deleteOption.setOnClickListener {
            onDeleteClick?.invoke(task)
        }

        // Set the checkbox state based on the task's isChecked status
        holder.checkBox.isChecked = task.isChecked

        // Set up a listener for when the user checks/unchecks the checkbox
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            task.isChecked = isChecked
            // Mark task as complete/incomplete
            markTaskComplete(task)
        }
    }



    override fun getItemCount(): Int = taskList.size

    // Method to add a task to the list
//    fun addTask(task: Task) {
//        taskList.add(task)
//        notifyItemInserted(taskList.size - 1)
//    }

    // Set the delete listener to handle task deletion
    fun setOnDeleteClickListener(listener: (Task) -> Unit) {
        onDeleteClick = listener
    }

    // Method to update the list with new tasks
    fun updateTasks(newTasks: List<Task>) {
        taskList.clear()
        taskList.addAll(newTasks)
        notifyDataSetChanged()
    }


    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val deleteOption: ImageButton = itemView.findViewById(R.id.ic_delete)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
    }
}
