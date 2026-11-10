package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "assignments",
    foreignKeys = [
        ForeignKey(
            entity = ClassEntity::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["classId"])]
)
data class AssignmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val classId: Long,
    val subject: String,
    val title: String,
    val description: String = "",
    val deadline: String, // Format: YYYY-MM-DD
    val maxScore: Double = 100.0,
    val passingGrade: Double = 75.0, // KKM
    val createdAt: Long = System.currentTimeMillis()
)
