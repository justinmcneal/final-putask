package com.example.puttask

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.puttask.api.Task

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
    }

    class DateAdapter(
        private val datesList: List<String>,
        private val onDateClick: (String) -> Unit
    ) : RecyclerView.Adapter<DateAdapter.DateViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.datelayout, parent, false)
            return DateViewHolder(view)
        }

        override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
            val date = datesList[position]
            holder.tvDate.text = date

            holder.itemView.setOnClickListener {
                onDateClick(date)
            }
        }

        override fun getItemCount(): Int = datesList.size

        class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvDate: TextView = itemView.findViewById(R.id.dateRecyclerView) // Assuming you have a TextView for the date
        }
    }
}
