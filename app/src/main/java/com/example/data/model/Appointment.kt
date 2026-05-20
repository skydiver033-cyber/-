package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientName: String,
    val patientPhone: String,
    val therapyType: String,
    val appointmentDate: String,
    val appointmentTime: String,
    val gender: String, // "ذكر" / "أنثى"
    val notes: String = "",
    val status: String = "قيد الانتظار", // "قيد الانتظار" (Pending), "مؤكد" (Confirmed), "ملغى" (Cancelled)
    val creationTimestamp: Long = System.currentTimeMillis()
)
