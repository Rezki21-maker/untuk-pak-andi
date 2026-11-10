package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_records",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ClassEntity::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["studentId", "date"], unique = true),
        Index(value = ["classId", "date"])
    ]
)
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val classId: Long,
    val studentId: Long,
    val date: String, // Format: YYYY-MM-DD
    val status: String, // "H" (Hadir), "I" (Izin), "S" (Sakit), "A" (Alpa)
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
