package com.example.puttask.api

import android.os.Parcel
import android.os.Parcelable

data class CreateRequest(
    val task_name: String,        // Ensure this matches the API's expected JSON format
    val task_description: String,
    val start_datetime: String,
    val end_datetime: String,
    val repeat_days: List<String>?, // Nullable in case it's optional
    val category: String
)

// Recycler View Shit
data class Task(
    val id: Int,
    val task_name: String,
    val task_description: String,
    val start_datetime: String,
    val end_datetime: String,
    val repeat_days: List<String>?, // Nullable list
    val category: String,
    val isChecked: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList(), // Read the list of repeat_days
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte() // Read isChecked as boolean
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(task_name)
        parcel.writeString(task_description)
        parcel.writeString(start_datetime)
        parcel.writeString(end_datetime)
        parcel.writeStringList(repeat_days) // Write the list of repeat_days
        parcel.writeString(category)
        parcel.writeByte(if (isChecked) 1 else 0) // Write isChecked as byte (1 for true, 0 for false)
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
    val start_datetime: String?,
    val end_datetime: String?,
    val repeat_days: List<String>?,
    val category: String
)

// Response after deleting a task
data class DeleteResponse(
    val success: Boolean,
    val message: String?
)
