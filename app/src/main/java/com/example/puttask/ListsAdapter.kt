package com.example.puttask

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.puttask.api.Task
import com.example.puttask.fragments.AddTask2

class ListsAdapter(
    private val taskList: MutableList<Task>,
    private val onTaskCheckedChange: (Task, Boolean) -> Unit,
    private val onItemClick: (Task) -> Unit
) : RecyclerView.Adapter<ListsAdapter.TaskViewHolder>() {

    companion object {
        const val REQUEST_CODE_EDIT_TASK = 1001
    }

    private var onDeleteClick: ((Task) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.lists_recyclerview, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.tvTitle.text = task.task_name
        holder.tvTime.text = task.end_date

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = task.isChecked
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onTaskCheckedChange(task, isChecked)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, AddTask2::class.java).apply {
                putExtra("task_id", task.id) // Ensure task.id is Int
                putExtra("task_name", task.task_name) // Now required, non-nullable String
                putExtra("task_description", task.task_description) // Now required, non-nullable String
                putExtra("end_date", task.end_date) // Now required, non-nullable String
                putExtra("end_time", task.end_time) // Now required, non-nullable String
            }
            (holder.itemView.context as Activity).startActivityForResult(intent, REQUEST_CODE_EDIT_TASK)
            onItemClick(task)
        }

        holder.deleteOption.setOnClickListener {
            onDeleteClick?.invoke(task)
        }
    }

    override fun getItemCount(): Int = taskList.size

    fun addTask(task: Task) {
        taskList.add(task)
        notifyItemInserted(taskList.size - 1)
    }

    fun updateTask(updatedTask: Task) {
        val index = taskList.indexOfFirst { it.id == updatedTask.id }
        if (index != -1) {
            taskList[index] = updatedTask
            notifyItemChanged(index)
        }
    }

    fun deleteTask(task: Task) {
        val index = taskList.indexOf(task)
        if (index != -1) {
            taskList.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun setOnDeleteClickListener(listener: (Task) -> Unit) {
        onDeleteClick = listener
    }

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val deleteOption: ImageButton = itemView.findViewById(R.id.ic_delete)
    }
}
