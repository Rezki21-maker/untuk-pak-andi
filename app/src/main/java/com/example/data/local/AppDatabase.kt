package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.TeacherDao
import com.example.data.local.entity.AssignmentEntity
import com.example.data.local.entity.AttendanceEntity
import com.example.data.local.entity.ClassEntity
import com.example.data.local.entity.ScoreEntity
import com.example.data.local.entity.StudentEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Database(
    entities = [
        ClassEntity::class,
        StudentEntity::class,
        AttendanceEntity::class,
        AssignmentEntity::class,
        ScoreEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun teacherDao(): TeacherDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gurukita_teacher.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        seedInitialData(database.teacherDao())
                    }
                }
            }

            private suspend fun seedInitialData(dao: TeacherDao) {
                // 1. Seed Kelas
                val classAId = dao.insertClass(
                    ClassEntity(
                        name = "X MIPA 1",
                        academicYear = "2024/2025",
                        semester = "Ganjil",
                        subjectDefault = "Matematika Wajib"
                    )
                )

                val classBId = dao.insertClass(
                    ClassEntity(
                        name = "XI IPS 2",
                        academicYear = "2024/2025",
                        semester = "Ganjil",
                        subjectDefault = "Sosiologi"
                    )
                )

                // 2. Seed Siswa Kelas X MIPA 1
                val studentsClassA = listOf(
                    StudentEntity(classId = classAId, nis = "102401", name = "Ahmad Fauzi", gender = "L", phone = "081234567890"),
                    StudentEntity(classId = classAId, nis = "102402", name = "Aisyah Putri Rahma", gender = "P", phone = "081234567891"),
                    StudentEntity(classId = classAId, nis = "102403", name = "Budi Santoso", gender = "L", phone = "081234567892"),
                    StudentEntity(classId = classAId, nis = "102404", name = "Citra Dewi Lestari", gender = "P", phone = "081234567893"),
                    StudentEntity(classId = classAId, nis = "102405", name = "Dimas Arya Pratama", gender = "L", phone = "081234567894"),
                    StudentEntity(classId = classAId, nis = "102406", name = "Farhan Maulana", gender = "L", phone = "081234567895"),
                    StudentEntity(classId = classAId, nis = "102407", name = "Gita Nurhaliza", gender = "P", phone = "081234567896"),
                    StudentEntity(classId = classAId, nis = "102408", name = "Hendra Setiawan", gender = "L", phone = "081234567897"),
                    StudentEntity(classId = classAId, nis = "102409", name = "Indah Permatasari", gender = "P", phone = "081234567898"),
                    StudentEntity(classId = classAId, nis = "102410", name = "Joko Wicaksono", gender = "L", phone = "081234567899")
                )

                // Seed Siswa Kelas XI IPS 2
                val studentsClassB = listOf(
                    StudentEntity(classId = classBId, nis = "112401", name = "Anisa Rahma", gender = "P", phone = "085211223344"),
                    StudentEntity(classId = classBId, nis = "112402", name = "Bagus Saputra", gender = "L", phone = "085211223345"),
                    StudentEntity(classId = classBId, nis = "112403", name = "Chandra Wijaya", gender = "L", phone = "085211223346"),
                    StudentEntity(classId = classBId, nis = "112404", name = "Dina Mariana", gender = "P", phone = "085211223347"),
                    StudentEntity(classId = classBId, nis = "112405", name = "Eko Prasetyo", gender = "L", phone = "085211223348")
                )

                dao.insertStudents(studentsClassA)
                dao.insertStudents(studentsClassB)

                // 3. Seed Tugas
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val cal = Calendar.getInstance()
                val todayStr = dateFormat.format(cal.time)

                cal.add(Calendar.DAY_OF_YEAR, -5)
                val task1Deadline = dateFormat.format(cal.time)

                cal.add(Calendar.DAY_OF_YEAR, 10)
                val task2Deadline = dateFormat.format(cal.time)

                val assign1Id = dao.insertAssignment(
                    AssignmentEntity(
                        classId = classAId,
                        subject = "Matematika",
                        title = "Tugas 1: Persamaan & Pertidaksamaan Nilai Mutlak",
                        description = "Kerjakan soal Latihan 1.2 di buku paket halaman 45, kumpulkan di buku tugas.",
                        deadline = task1Deadline,
                        maxScore = 100.0,
                        passingGrade = 75.0
                    )
                )

                val assign2Id = dao.insertAssignment(
                    AssignmentEntity(
                        classId = classAId,
                        subject = "Matematika",
                        title = "Ulangan Harian 1: Aljabar Linear",
                        description = "Ulangan harian bab 1 dan bab 2.",
                        deadline = task2Deadline,
                        maxScore = 100.0,
                        passingGrade = 75.0
                    )
                )

                // 4. Seed Beberapa Nilai Tugas 1
                dao.insertOrUpdateScores(
                    listOf(
                        ScoreEntity(assignmentId = assign1Id, studentId = 1, score = 88.0, feedback = "Pengerjaan sangat rapi"),
                        ScoreEntity(assignmentId = assign1Id, studentId = 2, score = 95.0, feedback = "Luar biasa, nilai sempurna di essay"),
                        ScoreEntity(assignmentId = assign1Id, studentId = 3, score = 70.0, feedback = "Perlu remedial di nomor 4 & 5"),
                        ScoreEntity(assignmentId = assign1Id, studentId = 4, score = 85.0, feedback = "Bagus"),
                        ScoreEntity(assignmentId = assign1Id, studentId = 5, score = 92.0, feedback = "Sangat teliti")
                    )
                )

                // 5. Seed Absensi Hari Ini untuk Kelas X MIPA 1
                val initialAttendance = listOf(
                    AttendanceEntity(classId = classAId, studentId = 1, date = todayStr, status = "H", notes = ""),
                    AttendanceEntity(classId = classAId, studentId = 2, date = todayStr, status = "H", notes = ""),
                    AttendanceEntity(classId = classAId, studentId = 3, date = todayStr, status = "S", notes = "Demam tinggi, surat dokter terlampir"),
                    AttendanceEntity(classId = classAId, studentId = 4, date = todayStr, status = "H", notes = ""),
                    AttendanceEntity(classId = classAId, studentId = 5, date = todayStr, status = "I", notes = "Acara keluarga"),
                    AttendanceEntity(classId = classAId, studentId = 6, date = todayStr, status = "H", notes = ""),
                    AttendanceEntity(classId = classAId, studentId = 7, date = todayStr, status = "H", notes = ""),
                    AttendanceEntity(classId = classAId, studentId = 8, date = todayStr, status = "A", notes = "Tanpa keterangan"),
                    AttendanceEntity(classId = classAId, studentId = 9, date = todayStr, status = "H", notes = ""),
                    AttendanceEntity(classId = classAId, studentId = 10, date = todayStr, status = "H", notes = "")
                )
                dao.insertOrUpdateAttendances(initialAttendance)
            }
        }
    }
}
