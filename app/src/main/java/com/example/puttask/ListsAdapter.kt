package com.example.puttask

import android.util.Log
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
    private val onTaskCheckedChange: (Task, Boolean) -> Unit,
    private val onItemClick: (Task) -> Unit,
    private val onDeleteTask: (Int) -> Unit // Using task id for deletion
) : RecyclerView.Adapter<ListsAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.bind(task)
    }

    override fun getItemCount(): Int = taskList.size

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
        val taskName: TextView = itemView.findViewById(R.id.taskname)
        val taskDescription: TextView = itemView.findViewById(R.id.taskdescription)
        val taskTime: TextView = itemView.findViewById(R.id.tvTime)
        val deleteButton: ImageButton = itemView.findViewById(R.id.ic_delete)

        fun bind(task: Task) {
            Log.d("TaskBinding", "Binding task: ${task.task_name}")
            taskName.text = task.task_name
            taskDescription.text = task.task_description
            taskTime.text = task.start_datetime // Assuming `start_datetime` exists in Task

            checkBox.isChecked = task.isChecked // Ensure `isChecked` exists in Task
            checkBox.setOnCheckedChangeListener(null) // Clear previous listener
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                onTaskCheckedChange(task, isChecked)
            }

            itemView.setOnClickListener {
                onItemClick(task) // Handle item click for navigation
            }

            deleteButton.setOnClickListener {
                onDeleteTask(task.id) // Invoke deletion using task ID
            }
        }
    }
}
