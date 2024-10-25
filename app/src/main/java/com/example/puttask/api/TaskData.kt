import android.os.Parcel
import android.os.Parcelable

data class Task(
    val id: String,
    val task_name: String,
    val task_description: String,
    val end_date: String,
    val end_time: String,
    val completed: Boolean,
    var repeat_days: List<String>,
    val category: String,
    var isChecked: Boolean = false // Default value is false
) : Parcelable {

    constructor(parcel: Parcel) : this(
        id = parcel.readString()!!,
        task_name = parcel.readString()!!,
        task_description = parcel.readString()!!,
        end_date = parcel.readString()!!,
        end_time = parcel.readString()!!,
        completed = parcel.readByte() != 0.toByte(),
        repeat_days = parcel.createStringArrayList() ?: arrayListOf(), // Handle potential null value
        category = parcel.readString()!!,
        isChecked = parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(task_name)
        parcel.writeString(task_description)
        parcel.writeString(end_date)
        parcel.writeString(end_time)
        parcel.writeByte(if (completed) 1 else 0)
        parcel.writeStringList(repeat_days)
        parcel.writeString(category)
        parcel.writeByte(if (isChecked) 1 else 0)
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
