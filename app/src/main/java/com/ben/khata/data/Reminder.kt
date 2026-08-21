package com.ben.khata.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    foreignKeys = [ForeignKey(entity = Person::class, parentColumns = ["id"], childColumns = ["personId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("personId")]
)
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personId: Long,
    val type: Int, // 0 = Pay, 1 = Receive
    val amount: Long,
    val dueDate: Long,
    val reminderTiming: Int, // 1, 2, 3 days before, 0 for on day
    val note: String = "",
    val status: Int = 0, // 0 = Pending, 1 = Completed
    val createdAt: Long = System.currentTimeMillis()
)

data class ReminderWithPerson(
    val id: Long,
    val personId: Long,
    val type: Int,
    val amount: Long,
    val dueDate: Long,
    val reminderTiming: Int,
    val note: String,
    val status: Int,
    val createdAt: Long,
    val personName: String
)
