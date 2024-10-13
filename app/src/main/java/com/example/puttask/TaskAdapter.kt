package com.example.puttask

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.puttask.api.Task

class TaskAdapter(private val tasks: List<Task>, private val onDeleteTask: (Int) -> Unit) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskName: TextView = itemView.findViewById(R.id.taskname)
        val taskDescription: TextView = itemView.findViewById(R.id.taskdescription)
        val deleteButton: ImageButton = itemView.findViewById(R.id.tvDelete)

        fun bind(task: Task) {
            Log.d("TaskBinding", "Binding task: ${task.task_name}")
            taskName.text = task.task_name
            taskDescription.text = task.task_description
            deleteButton.setOnClickListener {
                onDeleteTask(task.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)

        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int {
        return tasks.size
    }
}

