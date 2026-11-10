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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ClassSelectorHeader
import com.example.ui.components.StatCountPill
import com.example.ui.theme.NavyDark
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
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: TeacherViewModel,
    onNavigateToAttendance: () -> Unit,
    onNavigateToAssignments: () -> Unit,
    onNavigateToRecap: () -> Unit,
    onNavigateToClasses: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val classes by viewModel.allClasses.collectAsState()
    val selectedClassId by viewModel.selectedClassId.collectAsState()
    val students by viewModel.currentStudents.collectAsState()
    val attendanceRecords by viewModel.currentAttendance.collectAsState()
    val assignments by viewModel.currentAssignments.collectAsState()

    val currentClass = classes.find { it.id == selectedClassId }

    // Hitung statistik absensi hari ini
    val totalStudents = students.size
    val hadirCount = attendanceRecords.count { it.status == "H" }
    val izinCount = attendanceRecords.count { it.status == "I" }
    val sakitCount = attendanceRecords.count { it.status == "S" }
    val alpaCount = attendanceRecords.count { it.status == "A" }
    val totalAbsen = hadirCount + izinCount + sakitCount + alpaCount
    val attendanceProgress = if (totalStudents > 0) totalAbsen.toFloat() / totalStudents.toFloat() else 0f

    val todayFormatted = remember {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(Date())
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Selector Kelas Aktif
        item {
            ClassSelectorHeader(
                classes = classes,
                selectedClassId = selectedClassId,
                onSelectClass = { viewModel.selectClass(it) },
                onManageClasses = onNavigateToClasses
            )
        }

        // 2. Greeting & Tanggal
        item {
            Column {
                Text(
                    text = "Selamat Mengajar, Guru Hebat!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = todayFormatted,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 3. Hero Card: Absensi Hari Ini
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_attendance_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(StatusHadirBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HowToReg,
                                    contentDescription = null,
                                    tint = StatusHadirGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Absensi Hari Ini",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            color = if (totalAbsen == totalStudents && totalStudents > 0) StatusHadirBg else MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (totalAbsen == totalStudents && totalStudents > 0) "Lengkap" else "$totalAbsen / $totalStudents Siswa",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (totalAbsen == totalStudents && totalStudents > 0) StatusHadirGreen else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { attendanceProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = StatusHadirGreen,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Breakdown Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatCountPill("Hadir", hadirCount, StatusHadirGreen, StatusHadirBg)
                        StatCountPill("Izin", izinCount, StatusIzinBlue, StatusIzinBg)
                        StatCountPill("Sakit", sakitCount, StatusSakitAmber, StatusSakitBg)
                        StatCountPill("Alpa", alpaCount, StatusAlpaRed, StatusAlpaBg)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onNavigateToAttendance,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_open_attendance"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.HowToReg, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buka Absensi", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.markAllPresent() },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_quick_mark_all_present"),
                            shape = RoundedCornerShape(12.dp),
                            enabled = totalStudents > 0
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = StatusHadirGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Hadir Semua", color = StatusHadirGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4. Quick Action Grid
        item {
            Text(
                text = "Menu Utama",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Tugas & Nilai",
                    subtitle = "${assignments.size} Tugas Aktif",
                    icon = Icons.Default.Assignment,
                    iconBg = Color(0xFFEFF6FF),
                    iconTint = NavyPrimary,
                    onClick = onNavigateToAssignments,
                    modifier = Modifier.weight(1f)
                )

                QuickActionCard(
                    title = "Rekap & Ekspor",
                    subtitle = "PDF & Excel (.xlsx)",
                    icon = Icons.Default.Assessment,
                    iconBg = Color(0xFFFEF3C7),
                    iconTint = StatusSakitAmber,
                    onClick = onNavigateToRecap,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Kelola Siswa",
                    subtitle = "$totalStudents Siswa • Impor Excel",
                    icon = Icons.Default.Group,
                    iconBg = Color(0xFFDCFCE7),
                    iconTint = StatusHadirGreen,
                    onClick = onNavigateToClasses,
                    modifier = Modifier.weight(1f)
                )

                QuickActionCard(
                    title = "Kenyamanan Guru",
                    subtitle = "Ukuran Font & Tema",
                    icon = Icons.Default.Settings,
                    iconBg = Color(0xFFF3E8FF),
                    iconTint = Color(0xFF7E22CE),
                    onClick = onNavigateToSettings,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 5. Informasi Offline-First & Keamanan Data
        item {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Mode Offline-First Aktif",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Semua absensi & nilai tersimpan aman di HP. Anda dapat mengajar tanpa internet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
