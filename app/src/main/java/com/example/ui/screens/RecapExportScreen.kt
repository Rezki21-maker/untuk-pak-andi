package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
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
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ClassSelectorHeader
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusAlpaText
import com.example.ui.theme.StatusHadirGreen
import com.example.ui.theme.StatusIzinText
import com.example.ui.theme.StatusSakitAmber
import com.example.ui.viewmodel.TeacherViewModel
import com.example.utils.ExportUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecapExportScreen(
    viewModel: TeacherViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val classes by viewModel.allClasses.collectAsState()
    val selectedClassId by viewModel.selectedClassId.collectAsState()
    val students by viewModel.currentStudents.collectAsState()
    val allAttendance by viewModel.allAttendanceForClass.collectAsState()
    val assignments by viewModel.currentAssignments.collectAsState()
    val allScores by viewModel.allScoresForClass.collectAsState()

    val currentClass = classes.find { it.id == selectedClassId }

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0 = Presensi, 1 = Nilai
    var periodFilter by remember { mutableStateOf("Bulan Ini") } // "Bulan Ini", "Semester Ini"

    val currentMonthPrefix = remember { SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date()) }
    val filteredAttendance = remember(allAttendance, periodFilter) {
        if (periodFilter == "Bulan Ini") {
            allAttendance.filter { it.date.startsWith(currentMonthPrefix) }
        } else {
            allAttendance
        }
    }

    // Hitung rata-rata kehadiran kelas
    val totalHadirAll = filteredAttendance.count { it.status == "H" }
    val totalRecordsAll = filteredAttendance.size
    val classAttendanceRate = if (totalRecordsAll > 0) {
        String.format(Locale.US, "%.1f", (totalHadirAll.toDouble() / totalRecordsAll.toDouble()) * 100.0)
    } else "0.0"

    // Hitung rata-rata nilai kelas
    val validScores = allScores.mapNotNull { it.score }
    val classAverageScore = if (validScores.isNotEmpty()) {
        String.format(Locale.US, "%.1f", validScores.average())
    } else "-"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Rekap & Ekspor Laporan",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentClass?.name ?: "Pilih Kelas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Class selector
            ClassSelectorHeader(
                classes = classes,
                selectedClassId = selectedClassId,
                onSelectClass = { viewModel.selectClass(it) },
                onManageClasses = {},
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Tab Bar
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Rekap Presensi", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Rekap Nilai Siswa", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            // Overview Metric Bar & Period Selector
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = periodFilter == "Bulan Ini",
                                onClick = { periodFilter = "Bulan Ini" },
                                label = { Text("Bulan Ini") }
                            )
                            FilterChip(
                                selected = periodFilter == "Semester Ini",
                                onClick = { periodFilter = "Semester Ini" },
                                label = { Text("Semester Ini") }
                            )
                        }

                        // Summary Stat Badge
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (selectedTabIndex == 0) "Kehadiran: $classAttendanceRate%" else "Rata-rata: $classAverageScore",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Action Buttons: Excel / CSV & Print PDF
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (currentClass == null || students.isEmpty()) {
                                    Toast.makeText(context, "Data kelas atau siswa masih kosong", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (selectedTabIndex == 0) {
                                    val csv = ExportUtils.generateAttendanceCsv(
                                        classEntity = currentClass,
                                        students = students,
                                        attendances = filteredAttendance,
                                        period = periodFilter
                                    )
                                    ExportUtils.shareCsvContent(
                                        context = context,
                                        csvContent = csv,
                                        fileName = "Rekap_Presensi_${currentClass.name.replace(" ", "_")}.csv",
                                        subject = "Rekap Presensi ${currentClass.name}"
                                    )
                                } else {
                                    val csv = ExportUtils.generateGradesCsv(
                                        classEntity = currentClass,
                                        students = students,
                                        assignments = assignments,
                                        scores = allScores
                                    )
                                    ExportUtils.shareCsvContent(
                                        context = context,
                                        csvContent = csv,
                                        fileName = "Rekap_Nilai_${currentClass.name.replace(" ", "_")}.csv",
                                        subject = "Rekap Nilai ${currentClass.name}"
                                    )
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_export_excel"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ekspor Excel (.csv)", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                if (currentClass == null || students.isEmpty()) {
                                    Toast.makeText(context, "Data kelas atau siswa masih kosong", Toast.LENGTH_SHORT).show()
                                    return@OutlinedButton
                                }
                                if (selectedTabIndex == 0) {
                                    val meta = mapOf(
                                        "Kelas" to currentClass.name,
                                        "Tahun Ajaran" to "${currentClass.academicYear} - Semester ${currentClass.semester}",
                                        "Periode" to periodFilter,
                                        "Total Siswa" to "${students.size} Orang",
                                        "Rata-rata Kehadiran" to "$classAttendanceRate%"
                                    )
                                    val headers = listOf("No", "NIS", "Nama Siswa", "L/P", "Hadir", "Izin", "Sakit", "Alpa", "Persen")
                                    val rows = students.mapIndexed { idx, s ->
                                        val sAtt = filteredAttendance.filter { it.studentId == s.id }
                                        val h = sAtt.count { it.status == "H" }
                                        val i = sAtt.count { it.status == "I" }
                                        val sk = sAtt.count { it.status == "S" }
                                        val a = sAtt.count { it.status == "A" }
                                        val tot = h + i + sk + a
                                        val pct = if (tot > 0) String.format(Locale.US, "%.1f", (h.toDouble() / tot.toDouble()) * 100) else "0.0"
                                        listOf("${idx + 1}", s.nis, s.name, s.gender, "$h", "$i", "$sk", "$a", "$pct%")
                                    }
                                    val html = ExportUtils.generatePrintableHtml(
                                        title = "Laporan Rekapitulasi Presensi Siswa",
                                        subtitle = "Sekolah Menengah - Aplikasi GuruKita",
                                        metaInfo = meta,
                                        headers = headers,
                                        rows = rows
                                    )
                                    ExportUtils.printDocument(context, html, "Presensi_${currentClass.name}")
                                } else {
                                    val meta = mapOf(
                                        "Kelas" to currentClass.name,
                                        "Tahun Ajaran" to "${currentClass.academicYear} - Semester ${currentClass.semester}",
                                        "Total Siswa" to "${students.size} Orang",
                                        "Total Tugas/Ulangan" to "${assignments.size} Tugas"
                                    )
                                    val headers = mutableListOf("No", "NIS", "Nama Siswa", "L/P")
                                    assignments.forEach { headers.add("${it.title}\n(KKM ${it.passingGrade.toInt()})") }
                                    headers.add("Rata-rata")
                                    headers.add("Status")

                                    val rows = students.mapIndexed { idx, s ->
                                        val sScores = allScores.filter { it.studentId == s.id }
                                        val row = mutableListOf("${idx + 1}", s.nis, s.name, s.gender)
                                        var sum = 0.0
                                        var cnt = 0
                                        var pass = true
                                        assignments.forEach { t ->
                                            val sc = sScores.find { it.assignmentId == t.id }?.score
                                            if (sc != null) {
                                                row.add("${sc.toInt()}")
                                                sum += sc
                                                cnt++
                                                if (sc < t.passingGrade) pass = false
                                            } else {
                                                row.add("-")
                                            }
                                        }
                                        val avg = if (cnt > 0) String.format(Locale.US, "%.1f", sum / cnt) else "-"
                                        val stat = if (cnt == 0) "Belum Dinilai" else if (pass) "Tuntas" else "Remedial"
                                        row.add(avg)
                                        row.add(stat)
                                        row
                                    }
                                    val html = ExportUtils.generatePrintableHtml(
                                        title = "Laporan Rekapitulasi Nilai Siswa",
                                        subtitle = "Sekolah Menengah - Aplikasi GuruKita",
                                        metaInfo = meta,
                                        headers = headers,
                                        rows = rows
                                    )
                                    ExportUtils.printDocument(context, html, "Nilai_${currentClass.name}")
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_print_pdf"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cetak / PDF", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Preview Table
            val horizontalScrollState = rememberScrollState()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                if (students.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada siswa di kelas ini.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (selectedTabIndex == 0) {
                    // Preview Tabel Presensi
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(horizontalScrollState)
                    ) {
                        // Table Header
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TableCell("No", 40.dp, true)
                                TableCell("NIS", 70.dp, true)
                                TableCell("Nama Siswa", 180.dp, true)
                                TableCell("L/P", 40.dp, true)
                                TableCell("Hadir (H)", 75.dp, true)
                                TableCell("Izin (I)", 70.dp, true)
                                TableCell("Sakit (S)", 70.dp, true)
                                TableCell("Alpa (A)", 70.dp, true)
                                TableCell("% Hadir", 80.dp, true)
                            }
                        }

                        // Table Body
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(students) { index, student ->
                                val studentAtt = filteredAttendance.filter { it.studentId == student.id }
                                val h = studentAtt.count { it.status == "H" }
                                val i = studentAtt.count { it.status == "I" }
                                val s = studentAtt.count { it.status == "S" }
                                val a = studentAtt.count { it.status == "A" }
                                val tot = h + i + s + a
                                val pct = if (tot > 0) String.format(Locale.US, "%.1f", (h.toDouble() / tot.toDouble()) * 100) else "0.0"

                                val rowBg = if (index % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

                                Surface(color = rowBg) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TableCell("${index + 1}", 40.dp)
                                        TableCell(student.nis, 70.dp)
                                        TableCell(student.name, 180.dp, isBold = true)
                                        TableCell(student.gender, 40.dp)
                                        TableCell("$h", 75.dp, textColor = StatusHadirGreen)
                                        TableCell("$i", 70.dp, textColor = StatusIzinText)
                                        TableCell("$s", 70.dp, textColor = StatusSakitAmber)
                                        TableCell("$a", 70.dp, textColor = StatusAlpaText)
                                        TableCell("$pct%", 80.dp, isBold = true)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Preview Tabel Nilai
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(horizontalScrollState)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TableCell("No", 40.dp, true)
                                TableCell("NIS", 70.dp, true)
                                TableCell("Nama Siswa", 180.dp, true)
                                assignments.forEach { task ->
                                    TableCell("${task.title}\n(KKM ${task.passingGrade.toInt()})", 140.dp, true)
                                }
                                TableCell("Rata-rata", 90.dp, true)
                                TableCell("Status", 100.dp, true)
                            }
                        }

                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            itemsIndexed(students) { index, student ->
                                val studentScores = allScores.filter { it.studentId == student.id }
                                var sum = 0.0
                                var cnt = 0
                                var isAllPass = true

                                val rowBg = if (index % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

                                Surface(color = rowBg) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TableCell("${index + 1}", 40.dp)
                                        TableCell(student.nis, 70.dp)
                                        TableCell(student.name, 180.dp, isBold = true)

                                        assignments.forEach { task ->
                                            val sc = studentScores.find { it.assignmentId == task.id }?.score
                                            if (sc != null) {
                                                sum += sc
                                                cnt++
                                                if (sc < task.passingGrade) isAllPass = false
                                                val col = if (sc >= task.passingGrade) StatusHadirGreen else StatusAlpaText
                                                TableCell("${sc.toInt()}", 140.dp, textColor = col, isBold = true)
                                            } else {
                                                TableCell("-", 140.dp)
                                            }
                                        }

                                        val avg = if (cnt > 0) String.format(Locale.US, "%.1f", sum / cnt) else "-"
                                        val status = if (cnt == 0) "Belum Ada" else if (isAllPass) "Tuntas" else "Remedial"
                                        val statusCol = if (isAllPass && cnt > 0) StatusHadirGreen else if (cnt > 0) StatusAlpaText else Color.Gray

                                        TableCell(avg, 90.dp, isBold = true)
                                        TableCell(status, 100.dp, textColor = statusCol, isBold = true)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TableCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    isHeader: Boolean = false,
    isBold: Boolean = false,
    textColor: Color = Color.Unspecified
) {
    Text(
        text = text,
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp),
        style = if (isHeader) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodySmall,
        fontWeight = if (isHeader || isBold) FontWeight.Bold else FontWeight.Normal,
        textAlign = TextAlign.Center,
        color = if (textColor != Color.Unspecified) textColor else if (isHeader) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        maxLines = 2
    )
}
