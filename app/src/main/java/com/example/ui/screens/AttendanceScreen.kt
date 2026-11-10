package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.StudentEntity
import com.example.ui.components.AttendanceToggleGroup
import com.example.ui.components.ClassSelectorHeader
import com.example.ui.components.StatCountPill
import com.example.ui.components.StudentNoteDialog
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusAlpaBg
import com.example.ui.theme.StatusAlpaRed
import com.example.ui.theme.StatusHadirBg
import com.example.ui.theme.StatusHadirGreen
import com.example.ui.theme.StatusIzinBg
import com.example.ui.theme.StatusIzinBlue
import com.example.ui.theme.StatusSakitBg
import com.example.ui.theme.StatusSakitAmber
import com.example.ui.viewmodel.TeacherViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: TeacherViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val classes by viewModel.allClasses.collectAsState()
    val selectedClassId by viewModel.selectedClassId.collectAsState()
    val students by viewModel.currentStudents.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val attendanceRecords by viewModel.currentAttendance.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Semua") } // "Semua", "Belum", "Hadir", "Izin", "Sakit", "Alpa"

    // Dialog catatan absensi
    var studentForNote by remember { mutableStateOf<Pair<StudentEntity, String>?>(null) }

    // Hitung status
    val hadirCount = attendanceRecords.count { it.status == "H" }
    val izinCount = attendanceRecords.count { it.status == "I" }
    val sakitCount = attendanceRecords.count { it.status == "S" }
    val alpaCount = attendanceRecords.count { it.status == "A" }
    val belumCount = students.size - attendanceRecords.size

    val filteredStudents = students.filter { student ->
        val record = attendanceRecords.find { it.studentId == student.id }
        val matchesQuery = student.name.contains(searchQuery, ignoreCase = true) || student.nis.contains(searchQuery)
        val matchesFilter = when (selectedFilter) {
            "Semua" -> true
            "Belum" -> record == null
            "Hadir" -> record?.status == "H"
            "Izin" -> record?.status == "I"
            "Sakit" -> record?.status == "S"
            "Alpa" -> record?.status == "A"
            else -> true
        }
        matchesQuery && matchesFilter
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Presensi Harian",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Format Cepat 1-Ketukan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_back")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Bar: Tanggal & Mark All Present Button
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Quick Date Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
                        val yesterdayStr = remember {
                            val cal = Calendar.getInstance()
                            cal.add(Calendar.DAY_OF_YEAR, -1)
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = selectedDate == todayStr,
                                onClick = { viewModel.selectDate(todayStr) },
                                label = { Text("Hari Ini") },
                                leadingIcon = if (selectedDate == todayStr) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                            FilterChip(
                                selected = selectedDate == yesterdayStr,
                                onClick = { viewModel.selectDate(yesterdayStr) },
                                label = { Text("Kemarin") }
                            )
                        }

                        // Badge tanggal aktif
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = selectedDate,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // 1-Tap Hadir Semua Action Bar
                    Button(
                        onClick = { viewModel.markAllPresent() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_mark_all_present_full"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusHadirGreen)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TANDAI SEMUA HADIR (1-TAP)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }

                    // Breakdown Status Counts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatCountPill("Hadir", hadirCount, StatusHadirGreen, StatusHadirBg)
                        StatCountPill("Izin", izinCount, StatusIzinBlue, StatusIzinBg)
                        StatCountPill("Sakit", sakitCount, StatusSakitAmber, StatusSakitBg)
                        StatCountPill("Alpa", alpaCount, StatusAlpaRed, StatusAlpaBg)
                    }

                    // Search & Filter
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari nama siswa atau NIS...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Hapus")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_students_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Quick Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(bottom = 4.dp)
                    ) {
                        val filters = listOf("Semua", "Belum ($belumCount)", "Hadir ($hadirCount)", "Izin ($izinCount)", "Sakit ($sakitCount)", "Alpa ($alpaCount)")
                        items(filters) { filterLabel ->
                            val rawCode = filterLabel.substringBefore(" (")
                            val isSelected = selectedFilter == rawCode
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilter = rawCode },
                                label = { Text(filterLabel) }
                            )
                        }
                    }
                }
            }

            // Student Attendance List
            if (filteredStudents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (students.isEmpty()) "Belum ada siswa di kelas ini.\nTambahkan siswa melalui menu Kelola Siswa." else "Tidak ada siswa yang sesuai filter.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredStudents, key = { it.id }) { student ->
                        val record = attendanceRecords.find { it.studentId == student.id }
                        StudentAttendanceCard(
                            student = student,
                            currentStatus = record?.status,
                            notes = record?.notes ?: "",
                            onStatusSelected = { status ->
                                viewModel.updateAttendanceStatus(student.id, status, record?.notes ?: "")
                            },
                            onOpenNote = {
                                studentForNote = Pair(student, record?.notes ?: "")
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog Tambah Catatan Izin/Sakit
    studentForNote?.let { (student, currentNote) ->
        StudentNoteDialog(
            studentName = student.name,
            currentNote = currentNote,
            onDismiss = { studentForNote = null },
            onSaveNote = { newNote ->
                val currentRec = attendanceRecords.find { it.studentId == student.id }
                val currentStat = currentRec?.status ?: "I" // Default to Izin if note added without status
                viewModel.updateAttendanceStatus(student.id, currentStat, newNote)
                studentForNote = null
            }
        )
    }
}

@Composable
fun StudentAttendanceCard(
    student: StudentEntity,
    currentStatus: String?,
    notes: String,
    onStatusSelected: (String) -> Unit,
    onOpenNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Info Siswa (NIS + Nama)
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (student.gender == "L") Color(0xFFE0F2FE) else Color(0xFFFCE7F3),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = student.gender,
                            color = if (student.gender == "L") Color(0xFF0369A1) else Color(0xFFBE185D),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = student.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = "NIS: ${student.nis}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Note Button
                IconButton(
                    onClick = onOpenNote,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = "Catatan",
                        tint = if (notes.isNotBlank()) StatusSakitAmber else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // 1-Tap Toggle Group (H / I / S / A)
                AttendanceToggleGroup(
                    currentStatus = currentStatus,
                    onStatusSelected = onStatusSelected
                )
            }

            // Catatan Siswa jika ada
            if (notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = StatusSakitBg.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Catatan: $notes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
