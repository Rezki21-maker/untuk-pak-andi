package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddClassDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, academicYear: String, semester: String, subject: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var academicYear by remember { mutableStateOf("2024/2025") }
    var semester by remember { mutableStateOf("Ganjil") }
    var subject by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Kelas Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Kelas (cth: X MIPA 2 / 8A)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_class_name")
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Mata Pelajaran (cth: Matematika)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = academicYear,
                    onValueChange = { academicYear = it },
                    label = { Text("Tahun Ajaran") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Semester: ", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    listOf("Ganjil", "Genap").forEach { sem ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .selectable(
                                    selected = (semester == sem),
                                    onClick = { semester = sem }
                                )
                                .padding(end = 12.dp)
                        ) {
                            RadioButton(
                                selected = (semester == sem),
                                onClick = { semester = sem }
                            )
                            Text(sem)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), academicYear.trim(), semester, subject.trim().ifEmpty { "Umum" })
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("submit_add_class")
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun AddStudentDialog(
    onDismiss: () -> Unit,
    onConfirm: (nis: String, name: String, gender: String, phone: String) -> Unit
) {
    var nis by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("L") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Siswa Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nis,
                    onValueChange = { nis = it },
                    label = { Text("NIS / No. Absen") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("input_student_nis")
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Lengkap Siswa") },
                    modifier = Modifier.fillMaxWidth().testTag("input_student_name")
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Jenis Kelamin: ", style = MaterialTheme.typography.bodyMedium)
                    listOf("L" to "Laki-laki", "P" to "Perempuan").forEach { (code, text) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .selectable(
                                    selected = (gender == code),
                                    onClick = { gender = code }
                                )
                                .padding(end = 8.dp)
                        ) {
                            RadioButton(
                                selected = (gender == code),
                                onClick = { gender = code }
                            )
                            Text(text)
                        }
                    }
                }
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("No. HP / WA Wali Siswa (Opsional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val finalNis = nis.trim().ifEmpty { (1000..9999).random().toString() }
                        onConfirm(finalNis, name.trim(), gender, phone.trim())
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("submit_add_student")
            ) {
                Text("Daftarkan")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun AddAssignmentDialog(
    defaultSubject: String,
    onDismiss: () -> Unit,
    onConfirm: (subject: String, title: String, description: String, deadline: String, maxScore: Double, passingGrade: Double) -> Unit
) {
    var subject by remember { mutableStateOf(defaultSubject) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var deadline by remember { mutableStateOf(today) }
    var maxScoreStr by remember { mutableStateOf("100") }
    var passingGradeStr by remember { mutableStateOf("75") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buat Tugas / Topik Nilai Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Mata Pelajaran") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Tugas (cth: UH 1 / Tugas Bab 3)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_assignment_title")
                )
                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("Batas Waktu / Deadline (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = passingGradeStr,
                        onValueChange = { passingGradeStr = it },
                        label = { Text("KKM (Batas Tuntas)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxScoreStr,
                        onValueChange = { maxScoreStr = it },
                        label = { Text("Nilai Maksimal") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Keterangan / Instruksi (Opsional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val maxScore = maxScoreStr.toDoubleOrNull() ?: 100.0
                        val kkm = passingGradeStr.toDoubleOrNull() ?: 75.0
                        onConfirm(subject.trim(), title.trim(), description.trim(), deadline.trim(), maxScore, kkm)
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("submit_add_assignment")
            ) {
                Text("Buat Tugas")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun StudentNoteDialog(
    studentName: String,
    currentNote: String,
    onDismiss: () -> Unit,
    onSaveNote: (String) -> Unit
) {
    var note by remember { mutableStateOf(currentNote) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catatan Absensi: $studentName", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Tulis alasan izin, sakit, atau catatan khusus:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("Contoh: Surat dokter nomor 12, izin lomba sains...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSaveNote(note.trim()) }) {
                Text("Simpan")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}
