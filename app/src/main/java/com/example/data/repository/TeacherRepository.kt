package com.example.data.repository

import com.example.data.local.dao.TeacherDao
import com.example.data.local.entity.AssignmentEntity
import com.example.data.local.entity.AttendanceEntity
import com.example.data.local.entity.ClassEntity
import com.example.data.local.entity.ScoreEntity
import com.example.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

class TeacherRepository(private val teacherDao: TeacherDao) {

    // === KELAS ===
    val allClasses: Flow<List<ClassEntity>> = teacherDao.getAllClasses()

    fun getClassById(id: Long): Flow<ClassEntity?> = teacherDao.getClassById(id)

    suspend fun insertClass(classEntity: ClassEntity): Long = teacherDao.insertClass(classEntity)

    suspend fun updateClass(classEntity: ClassEntity) = teacherDao.updateClass(classEntity)

    suspend fun deleteClass(classEntity: ClassEntity) = teacherDao.deleteClass(classEntity)

    // === SISWA ===
    fun getStudentsByClass(classId: Long): Flow<List<StudentEntity>> = teacherDao.getStudentsByClass(classId)

    suspend fun insertStudent(student: StudentEntity): Long = teacherDao.insertStudent(student)

    suspend fun insertStudents(students: List<StudentEntity>) = teacherDao.insertStudents(students)

    suspend fun updateStudent(student: StudentEntity) = teacherDao.updateStudent(student)

    suspend fun deleteStudent(student: StudentEntity) = teacherDao.deleteStudent(student)

    // === ABSENSI ===
    fun getAttendanceByClassAndDate(classId: Long, date: String): Flow<List<AttendanceEntity>> =
        teacherDao.getAttendanceByClassAndDate(classId, date)

    fun getAttendanceByClassAndMonth(classId: Long, monthPrefix: String): Flow<List<AttendanceEntity>> =
        teacherDao.getAttendanceByClassAndMonth(classId, monthPrefix)

    fun getAllAttendanceByClass(classId: Long): Flow<List<AttendanceEntity>> =
        teacherDao.getAllAttendanceByClass(classId)

    suspend fun saveAttendance(attendance: AttendanceEntity) =
        teacherDao.insertOrUpdateAttendance(attendance)

    suspend fun saveAttendances(attendances: List<AttendanceEntity>) =
        teacherDao.insertOrUpdateAttendances(attendances)

    // 1-Tap Mark All Present
    suspend fun markAllStudentsPresent(classId: Long, students: List<StudentEntity>, date: String) {
        val records = students.map { student ->
            AttendanceEntity(
                classId = classId,
                studentId = student.id,
                date = date,
                status = "H",
                notes = ""
            )
        }
        teacherDao.insertOrUpdateAttendances(records)
    }

    // === TUGAS ===
    fun getAssignmentsByClass(classId: Long): Flow<List<AssignmentEntity>> =
        teacherDao.getAssignmentsByClass(classId)

    fun getAssignmentById(id: Long): Flow<AssignmentEntity?> =
        teacherDao.getAssignmentById(id)

    suspend fun insertAssignment(assignment: AssignmentEntity): Long =
        teacherDao.insertAssignment(assignment)

    suspend fun updateAssignment(assignment: AssignmentEntity) =
        teacherDao.updateAssignment(assignment)

    suspend fun deleteAssignment(assignment: AssignmentEntity) =
        teacherDao.deleteAssignment(assignment)

    // === NILAI ===
    fun getScoresByAssignment(assignmentId: Long): Flow<List<ScoreEntity>> =
        teacherDao.getScoresByAssignment(assignmentId)

    fun getAllScoresByClass(classId: Long): Flow<List<ScoreEntity>> =
        teacherDao.getAllScoresByClass(classId)

    suspend fun saveScore(score: ScoreEntity) =
        teacherDao.insertOrUpdateScore(score)

    suspend fun saveScores(scores: List<ScoreEntity>) =
        teacherDao.insertOrUpdateScores(scores)
}
