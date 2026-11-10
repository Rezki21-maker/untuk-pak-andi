package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AssignmentEntity
import com.example.data.local.entity.AttendanceEntity
import com.example.data.local.entity.ClassEntity
import com.example.data.local.entity.ScoreEntity
import com.example.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TeacherDao {

    // === KELAS ===
    @Query("SELECT * FROM classes ORDER BY name ASC")
    fun getAllClasses(): Flow<List<ClassEntity>>

    @Query("SELECT * FROM classes WHERE id = :id LIMIT 1")
    fun getClassById(id: Long): Flow<ClassEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(classEntity: ClassEntity): Long

    @Update
    suspend fun updateClass(classEntity: ClassEntity)

    @Delete
    suspend fun deleteClass(classEntity: ClassEntity)

    // === SISWA ===
    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY name ASC")
    fun getStudentsByClass(classId: Long): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    // === ABSENSI ===
    @Query("SELECT * FROM attendance_records WHERE classId = :classId AND date = :date")
    fun getAttendanceByClassAndDate(classId: Long, date: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance_records WHERE classId = :classId AND date LIKE :monthPrefix || '%'")
    fun getAttendanceByClassAndMonth(classId: Long, monthPrefix: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance_records WHERE classId = :classId")
    fun getAllAttendanceByClass(classId: Long): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAttendance(attendance: AttendanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAttendances(attendances: List<AttendanceEntity>)

    @Delete
    suspend fun deleteAttendance(attendance: AttendanceEntity)

    // === TUGAS / PENILAIAN ===
    @Query("SELECT * FROM assignments WHERE classId = :classId ORDER BY deadline DESC, id DESC")
    fun getAssignmentsByClass(classId: Long): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE id = :id LIMIT 1")
    fun getAssignmentById(id: Long): Flow<AssignmentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: AssignmentEntity): Long

    @Update
    suspend fun updateAssignment(assignment: AssignmentEntity)

    @Delete
    suspend fun deleteAssignment(assignment: AssignmentEntity)

    // === NILAI TUGAS ===
    @Query("SELECT * FROM assignment_scores WHERE assignmentId = :assignmentId")
    fun getScoresByAssignment(assignmentId: Long): Flow<List<ScoreEntity>>

    @Query("SELECT s.* FROM assignment_scores s INNER JOIN assignments a ON s.assignmentId = a.id WHERE a.classId = :classId")
    fun getAllScoresByClass(classId: Long): Flow<List<ScoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateScore(score: ScoreEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateScores(scores: List<ScoreEntity>)
}
