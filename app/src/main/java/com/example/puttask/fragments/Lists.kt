package com.example.puttask.fragments

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.puttask.ListsAdapter
import com.example.puttask.R
import com.example.puttask.data.Task

class Lists : Fragment(R.layout.fragment_lists) {

    private lateinit var listsrecyclerView: RecyclerView
    private lateinit var listsAdapter: ListsAdapter
    private lateinit var tvDropdownLists: TextView
    private lateinit var ic_sort: ImageView
    private lateinit var popupcardviewLists: CardView

    private val taskList = mutableListOf(
        Task(1, "Title 1", null, "10:00 AM", "11:00 AM", null, false),
        Task(2, "Title 2", null, "12:00 PM", "1:00 PM", null, true),
        Task(3, "Title 3", null, "3:00 PM", "4:00 PM", null, false),
        Task(4, "Title 4", null, "10:00 AM", "11:00 AM", null, false),
        Task(5, "Title 5", null, "10:00 AM", "11:00 AM", null, false),
        Task(6, "Title 6", null, "10:00 AM", "11:00 AM", null, false)
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        ic_sort = view.findViewById(R.id.ic_sort)
        tvDropdownLists = view.findViewById(R.id.tvDropdownLists)
        popupcardviewLists = view.findViewById(R.id.popupcardviewLists)

        // Updated this lists dropdown as a customized so that icon would be inside
        val dropdownLists = PopupMenu(requireContext(), tvDropdownLists)
        val menuMap = mapOf(
            R.id.allItems to "All Items",
            R.id.personal to "Personal",
            R.id.work to "Work",
            R.id.school to "School",
            R.id.social to "Social"
        )

        dropdownLists.menuInflater.inflate(R.menu.dropdown_lists, dropdownLists.menu)

        tvDropdownLists.setOnClickListener {
            dropdownLists.setOnMenuItemClickListener { menuItem ->
                menuMap[menuItem.itemId]?.let {
                    tvDropdownLists.text = it
                    true
                } ?: false
            }
            dropdownLists.show()
        }

        // Sort options
        ic_sort.setOnClickListener {
            visibilityChecker()
        }

        // Set up the RecyclerView
        listsrecyclerView = view.findViewById(R.id.listsrecyclerView)
        listsrecyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize the adapter and set it to RecyclerView
        listsAdapter = ListsAdapter(taskList) { task, isChecked ->
            // Update the task state here if needed
            val index = taskList.indexOf(task)
            if (index != -1) {
                taskList[index] = task.copy(isChecked = isChecked)
            }
        }
        listsrecyclerView.adapter = listsAdapter
    }

    // CardView pop up for sort options
    private fun visibilityChecker() {
        popupcardviewLists.visibility = if (popupcardviewLists.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }
}
