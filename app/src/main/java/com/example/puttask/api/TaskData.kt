package com.example.puttask.api
import android.os.Parcel
import android.os.Parcelable

data class CreateRequest(
    val task_name: String,
    val task_description: String,
    val end_date: String?,
    val end_time: String?,
    val repeat_days: List<String>?,
    val category: String
)

// Recycler View Shit
data class Task(
    val id: String,
    val task_name: String,
    val task_description: String,
    val end_date: String,
    val end_time: String,
    var repeat_days: List<String>?,
    val category: String,
    val isChecked: Boolean
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createStringArrayList(),
        parcel.readString()!!,
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(task_name)
        parcel.writeString(task_description)
        parcel.writeString(end_date)
        parcel.writeString(end_time)
        parcel.writeStringList(repeat_days)
        parcel.writeString(category)
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

data class UpdateRequest(
    val task_name: String?,
    val task_description: String?,
    val end_date: String?,
    val end_time: String?,
    val repeat_days: List<String>?,
    val category: String
)

data class DeleteResponse(
        val message: String?
)
