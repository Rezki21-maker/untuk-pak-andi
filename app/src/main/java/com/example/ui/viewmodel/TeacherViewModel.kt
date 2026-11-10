package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AssignmentEntity
import com.example.data.local.entity.AttendanceEntity
import com.example.data.local.entity.ClassEntity
import com.example.data.local.entity.ScoreEntity
import com.example.data.local.entity.StudentEntity
import com.example.data.repository.TeacherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TeacherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TeacherRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TeacherRepository(database.teacherDao())
    }

    // Tanggal default hari ini (YYYY-MM-DD)
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDateString: String = dateFormatter.format(Date())

    // State Kelas
    val allClasses: StateFlow<List<ClassEntity>> = repository.allClasses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedClassId = MutableStateFlow<Long?>(null)
    val selectedClassId: StateFlow<Long?> = _selectedClassId.asStateFlow()

    // Tanggal absensi yang sedang dilihat
    private val _selectedDate = MutableStateFlow(todayDateString)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Siswa di kelas terpilih
    val currentStudents: StateFlow<List<StudentEntity>> = _selectedClassId.flatMapLatest { classId ->
        if (classId != null) repository.getStudentsByClass(classId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Catatan absensi tanggal terpilih
    val currentAttendance: StateFlow<List<AttendanceEntity>> = combine(
        _selectedClassId,
        _selectedDate
    ) { classId, date ->
        Pair(classId, date)
    }.flatMapLatest { (classId, date) ->
        if (classId != null) repository.getAttendanceByClassAndDate(classId, date) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Semua absensi untuk rekapitulasi kelas
    val allAttendanceForClass: StateFlow<List<AttendanceEntity>> = _selectedClassId.flatMapLatest { classId ->
        if (classId != null) repository.getAllAttendanceByClass(classId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Daftar Tugas
    val currentAssignments: StateFlow<List<AssignmentEntity>> = _selectedClassId.flatMapLatest { classId ->
        if (classId != null) repository.getAssignmentsByClass(classId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedAssignmentId = MutableStateFlow<Long?>(null)
    val selectedAssignmentId: StateFlow<Long?> = _selectedAssignmentId.asStateFlow()

    // Nilai untuk tugas terpilih
    val currentScores: StateFlow<List<ScoreEntity>> = _selectedAssignmentId.flatMapLatest { assignId ->
        if (assignId != null) repository.getScoresByAssignment(assignId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Semua nilai untuk rekap
    val allScoresForClass: StateFlow<List<ScoreEntity>> = _selectedClassId.flatMapLatest { classId ->
        if (classId != null) repository.getAllScoresByClass(classId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Accessibility & Pengaturan Font untuk Guru Senior
    private val _fontScale = MutableStateFlow(1.0f) // 1.0f: Normal, 1.15f: Nyaman, 1.3f: Besar
    val fontScale: StateFlow<Float> = _fontScale.asStateFlow()

    private val _isDarkTheme = MutableStateFlow<Boolean?>(null) // null = ikut sistem
    val isDarkTheme: StateFlow<Boolean?> = _isDarkTheme.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        // Auto select first class when available
        viewModelScope.launch {
            allClasses.collect { list ->
                if (_selectedClassId.value == null && list.isNotEmpty()) {
                    _selectedClassId.value = list.first().id
                }
            }
        }
    }

    fun selectClass(classId: Long) {
        _selectedClassId.value = classId
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    fun selectAssignment(assignmentId: Long?) {
        _selectedAssignmentId.value = assignmentId
    }

    fun setFontScale(scale: Float) {
        _fontScale.value = scale
    }

    fun toggleTheme(isDark: Boolean?) {
        _isDarkTheme.value = isDark
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    // === AKSI ABSENSI ===
    fun updateAttendanceStatus(studentId: Long, status: String, notes: String = "") {
        val classId = _selectedClassId.value ?: return
        val date = _selectedDate.value
        viewModelScope.launch {
            val record = AttendanceEntity(
                classId = classId,
                studentId = studentId,
                date = date,
                status = status,
                notes = notes
            )
            repository.saveAttendance(record)
        }
    }

    // 1-Tap Toggle: Hadirkan Semua Siswa Sekaligus
    fun markAllPresent() {
        val classId = _selectedClassId.value ?: return
        val students = currentStudents.value
        if (students.isEmpty()) return
        val date = _selectedDate.value
        viewModelScope.launch {
            repository.markAllStudentsPresent(classId, students, date)
            showMessage("Semua siswa ditandai Hadir (H)")
        }
    }

    // === AKSI TUGAS & NILAI ===
    fun createAssignment(
        subject: String,
        title: String,
        description: String,
        deadline: String,
        maxScore: Double = 100.0,
        passingGrade: Double = 75.0
    ) {
        val classId = _selectedClassId.value ?: return
        viewModelScope.launch {
            val id = repository.insertAssignment(
                AssignmentEntity(
                    classId = classId,
                    subject = subject,
                    title = title,
                    description = description,
                    deadline = deadline,
                    maxScore = maxScore,
                    passingGrade = passingGrade
                )
            )
            _selectedAssignmentId.value = id
            showMessage("Tugas baru berhasil dibuat")
        }
    }

    fun deleteAssignment(assignment: AssignmentEntity) {
        viewModelScope.launch {
            repository.deleteAssignment(assignment)
            if (_selectedAssignmentId.value == assignment.id) {
                _selectedAssignmentId.value = null
            }
            showMessage("Tugas dihapus")
        }
    }

    fun updateScore(studentId: Long, score: Double?, feedback: String = "") {
        val assignmentId = _selectedAssignmentId.value ?: return
        viewModelScope.launch {
            repository.saveScore(
                ScoreEntity(
                    assignmentId = assignmentId,
                    studentId = studentId,
                    score = score,
                    feedback = feedback
                )
            )
        }
    }

    fun batchSetScores(scoreMap: Map<Long, Double?>) {
        val assignmentId = _selectedAssignmentId.value ?: return
        viewModelScope.launch {
            val entities = scoreMap.map { (studentId, score) ->
                ScoreEntity(
                    assignmentId = assignmentId,
                    studentId = studentId,
                    score = score,
                    feedback = ""
                )
            }
            repository.saveScores(entities)
            showMessage("Nilai tugas berhasil disimpan")
        }
    }

    // === AKSI KELAS & SISWA ===
    fun createClass(name: String, academicYear: String, semester: String, subject: String) {
        viewModelScope.launch {
            val id = repository.insertClass(
                ClassEntity(
                    name = name,
                    academicYear = academicYear,
                    semester = semester,
                    subjectDefault = subject
                )
            )
            _selectedClassId.value = id
            showMessage("Kelas $name berhasil ditambahkan")
        }
    }

    fun addStudent(nis: String, name: String, gender: String, phone: String) {
        val classId = _selectedClassId.value ?: return
        viewModelScope.launch {
            repository.insertStudent(
                StudentEntity(
                    classId = classId,
                    nis = nis,
                    name = name,
                    gender = gender,
                    phone = phone
                )
            )
            showMessage("Siswa $name berhasil didaftarkan")
        }
    }

    fun deleteStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            showMessage("Siswa ${student.name} telah dihapus")
        }
    }

    fun importStudents(parsedList: List<com.example.utils.ParsedStudent>) {
        val classId = _selectedClassId.value ?: return
        if (parsedList.isEmpty()) {
            showMessage("Tidak ada data siswa yang valid untuk diimpor")
            return
        }
        viewModelScope.launch {
            val entities = parsedList.map {
                StudentEntity(
                    classId = classId,
                    nis = it.nis,
                    name = it.name,
                    gender = it.gender,
                    phone = it.phone
                )
            }
            repository.insertStudents(entities)
            showMessage("Berhasil mengimpor ${entities.size} siswa ke kelas")
        }
    }
}
