package com.example.puttask.api
import android.os.Parcel
import android.os.Parcelable

data class CreateRequest(
    val task_name: String,        // Ensure this matches the API's expected JSON format
    val task_description: String,
    val end_date: String?,
    val end_time: String?,
    val repeat_days: List<String>?, // Nullable in case it's optional
    val category: String
)

// Recycler View Shit
data class Task(
    val id: String,
    val task_name: String,
    val task_description: String,
    val end_date: String,
    val end_time: String,
    var repeat_days: List<String>? = null,
    val category: String,
    var is_completed: Boolean // Add this line
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList(),
        parcel.readString()!!,
        parcel.readByte() != 0.toByte() // Read isChecked as a Boolean from the Parcel
    ) {
        // Read is_completed from the Parcel
        is_completed = parcel.readByte() != 0.toByte() // Read is_completed as a Boolean from the Parcel
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(task_name)
        parcel.writeString(task_description)
        parcel.writeString(end_date)
        parcel.writeString(end_time)
        parcel.writeStringList(repeat_days)
        parcel.writeString(category)
        parcel.writeByte(if (is_completed) 1 else 0) // Write is_completed to the Parcel
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Task> {
        override fun createFromParcel(parcel: Parcel): Task {
            return Task(parcel)
        }

        override fun newArray(size: Int): Array<Task?> {
            return arrayOfNulls(size)
        }
    }
}




// Request to update an existing task
data class UpdateRequest(
    val task_name: String?,       // Nullable in case only some fields are being updated
    val task_description: String?,
    val end_date: String?,
    val end_time: String?,
    val repeat_days: List<String>?,
    val category: String
)

// Response after deleting a task
data class DeleteResponse(
        val message: String?
)


data class TaskResponse(
    val success: Boolean?,
    val message: String?
)
