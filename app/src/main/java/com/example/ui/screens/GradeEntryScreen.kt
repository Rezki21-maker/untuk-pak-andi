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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AssignmentEntity
import com.example.data.local.entity.StudentEntity
import com.example.ui.theme.StatusAlpaBg
import com.example.ui.theme.StatusAlpaRed
import com.example.ui.theme.StatusHadirBg
import com.example.ui.theme.StatusHadirGreen
import com.example.ui.theme.StatusSakitAmber
import com.example.ui.theme.StatusSakitBg
import com.example.ui.viewmodel.TeacherViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeEntryScreen(
    assignmentId: Long,
    viewModel: TeacherViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val assignments by viewModel.currentAssignments.collectAsState()
    val students by viewModel.currentStudents.collectAsState()
    val currentScores by viewModel.currentScores.collectAsState()

    val assignment = assignments.find { it.id == assignmentId }

    // Map nilai lokal untuk form input cepat
    val scoreInputMap = remember { mutableStateMapOf<Long, String>() }

    // Inisialisasi input dengan data tersimpan di DB
    LaunchedEffect(currentScores, assignmentId) {
        currentScores.forEach { scoreEntity ->
            if (scoreEntity.score != null) {
                val scoreStr = if (scoreEntity.score % 1.0 == 0.0) {
                    scoreEntity.score.toInt().toString()
                } else {
                    scoreEntity.score.toString()
                }
                scoreInputMap[scoreEntity.studentId] = scoreStr
            }
        }
    }

    // Perhitungan statistik live
    val enteredScores = scoreInputMap.values.mapNotNull { it.toDoubleOrNull() }
    val passingGrade = assignment?.passingGrade ?: 75.0
    val totalGraded = enteredScores.size
    val averageScore = if (enteredScores.isNotEmpty()) enteredScores.average() else 0.0
    val highestScore = enteredScores.maxOrNull() ?: 0.0
    val lowestScore = enteredScores.minOrNull() ?: 0.0
    val tuntasCount = enteredScores.count { it >= passingGrade }
    val tuntasPercent = if (totalGraded > 0) (tuntasCount.toDouble() / totalGraded.toDouble() * 100).toInt() else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Penilaian Tugas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = assignment?.title ?: "Input Nilai",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            val mapToSave = scoreInputMap.mapValues { it.value.toDoubleOrNull() }
                            viewModel.batchSetScores(mapToSave)
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("btn_save_all_grades"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan Nilai")
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
            // Summary Header Card
            assignment?.let { task ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${task.subject} • KKM: ${task.passingGrade.toInt()} • Batas: ${task.deadline}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                color = if (tuntasPercent >= 75) StatusHadirBg else StatusSakitBg,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "$tuntasCount / $totalGraded Tuntas ($tuntasPercent%)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tuntasPercent >= 75) StatusHadirGreen else StatusSakitAmber,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Statistik Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatBox("Rata-rata", String.format(Locale.US, "%.1f", averageScore))
                            StatBox("Tertinggi", highestScore.toInt().toString())
                            StatBox("Terendah", lowestScore.toInt().toString())
                            StatBox("Total Dinilai", "$totalGraded / ${students.size}")
                        }
                    }
                }
            }

            // Student Grade List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(students, key = { it.id }) { student ->
                    val currentVal = scoreInputMap[student.id] ?: ""
                    val currentDouble = currentVal.toDoubleOrNull()
                    val isTuntas = currentDouble != null && currentDouble >= passingGrade

                    StudentGradeRowCard(
                        student = student,
                        currentScore = currentVal,
                        isTuntas = isTuntas,
                        kkm = passingGrade.toInt(),
                        onScoreChange = { newVal ->
                            scoreInputMap[student.id] = newVal
                        },
                        onQuickPreset = { presetScore ->
                            scoreInputMap[student.id] = presetScore.toString()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StatBox(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun StudentGradeRowCard(
    student: StudentEntity,
    currentScore: String,
    isTuntas: Boolean,
    kkm: Int,
    onScoreChange: (String) -> Unit,
    onQuickPreset: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
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
                // Info Siswa
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleSmall,
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

                // Status Tag (Tuntas / Remedial / Belum)
                if (currentScore.isNotBlank()) {
                    Surface(
                        color = if (isTuntas) StatusHadirBg else StatusAlpaBg,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isTuntas) "Tuntas" else "Remedial",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isTuntas) StatusHadirGreen else StatusAlpaRed,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Score Input Box
                OutlinedTextField(
                    value = currentScore,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() || it == '.' }
                        if (filtered.length <= 4) {
                            onScoreChange(filtered)
                        }
                    },
                    label = { Text("Nilai") },
                    placeholder = { Text("0-100") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .width(90.dp)
                        .testTag("grade_input_${student.id}"),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Preset Chips (+ 75 KKM, 80, 85, 90, 100)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Preset Cepat:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                val presets = listOf(kkm, 80, 85, 90, 100)
                presets.forEach { presetVal ->
                    Surface(
                        color = if (currentScore == presetVal.toString()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onQuickPreset(presetVal) }
                    ) {
                        Text(
                            text = if (presetVal == kkm) "$presetVal (KKM)" else presetVal.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (currentScore == presetVal.toString()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}
