package com.example

import com.example.data.local.entity.AssignmentEntity
import com.example.data.local.entity.AttendanceEntity
import com.example.data.local.entity.ClassEntity
import com.example.data.local.entity.ScoreEntity
import com.example.data.local.entity.StudentEntity
import com.example.utils.ExportUtils
import com.example.utils.StudentImportParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class ExampleUnitTest {

  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testAttendanceCsvExport() {
    val classEntity = ClassEntity(id = 1, name = "X MIPA 1", academicYear = "2024/2025", semester = "Ganjil")
    val students = listOf(
      StudentEntity(id = 1, classId = 1, nis = "101", name = "Budi Santoso", gender = "L"),
      StudentEntity(id = 2, classId = 1, nis = "102", name = "Siti Rahma", gender = "P")
    )
    val attendances = listOf(
      AttendanceEntity(id = 1, classId = 1, studentId = 1, date = "2024-10-01", status = "H"),
      AttendanceEntity(id = 2, classId = 1, studentId = 2, date = "2024-10-01", status = "S")
    )

    val csv = ExportUtils.generateAttendanceCsv(classEntity, students, attendances, "Oktober 2024")
    assertTrue(csv.contains("REKAP ABSENSI SISWA - GURUKITA"))
    assertTrue(csv.contains("X MIPA 1"))
    assertTrue(csv.contains("Budi Santoso"))
    assertTrue(csv.contains("Siti Rahma"))
    assertTrue(csv.contains("Hadir (H)"))
  }

  @Test
  fun testGradesCsvExport() {
    val classEntity = ClassEntity(id = 1, name = "X MIPA 1", academicYear = "2024/2025", semester = "Ganjil")
    val students = listOf(
      StudentEntity(id = 1, classId = 1, nis = "101", name = "Budi Santoso", gender = "L")
    )
    val assignments = listOf(
      AssignmentEntity(id = 1, classId = 1, subject = "Matematika", title = "Tugas 1", deadline = "2024-10-10", passingGrade = 75.0)
    )
    val scores = listOf(
      ScoreEntity(id = 1, assignmentId = 1, studentId = 1, score = 85.0)
    )

    val csv = ExportUtils.generateGradesCsv(classEntity, students, assignments, scores)
    assertTrue(csv.contains("REKAP NILAI SISWA - GURUKITA"))
    assertTrue(csv.contains("Tugas 1"))
    assertTrue(csv.contains("85"))
    assertTrue(csv.contains("Tuntas"))
  }

  @Test
  fun testParseCsvStudentImport() {
    val csvContent = """
      NIS,Nama Siswa,Jenis Kelamin,No HP
      102401,Ahmad Fauzi,L,081234567890
      102402,Aisyah Putri,P,081234567891
      102403,Budi Santoso,L,081234567892
    """.trimIndent()

    val stream = ByteArrayInputStream(csvContent.toByteArray(Charsets.UTF_8))
    val result = StudentImportParser.parseCsv(stream)

    assertEquals(3, result.size)
    assertEquals("102401", result[0].nis)
    assertEquals("Ahmad Fauzi", result[0].name)
    assertEquals("L", result[0].gender)
    assertEquals("081234567890", result[0].phone)

    assertEquals("102402", result[1].nis)
    assertEquals("Aisyah Putri", result[1].name)
    assertEquals("P", result[1].gender)

    assertEquals("102403", result[2].nis)
    assertEquals("Budi Santoso", result[2].name)
    assertEquals("L", result[2].gender)
  }

  @Test
  fun testParseSemicolonCsvImport() {
    val csvContent = """
      No Induk;Nama Peserta;JK;Kontak WA
      1101;Dewi Sartika;Perempuan;0852112233
      1102;Chandra Wijaya;Laki-laki;0852112244
    """.trimIndent()

    val stream = ByteArrayInputStream(csvContent.toByteArray(Charsets.UTF_8))
    val result = StudentImportParser.parseCsv(stream)

    assertEquals(2, result.size)
    assertEquals("1101", result[0].nis)
    assertEquals("Dewi Sartika", result[0].name)
    assertEquals("P", result[0].gender)
    assertEquals("0852112233", result[0].phone)

    assertEquals("1102", result[1].nis)
    assertEquals("Chandra Wijaya", result[1].name)
    assertEquals("L", result[1].gender)
  }

  @Test
  fun testParseRawTextFromSpreadsheet() {
    // Spreadsheet copy clipboard (TSV)
    val clipboardText = "2001\tEko Prasetyo\tL\t081999888\n2002\tFitri Handayani\tP\t081999777"
    val result = StudentImportParser.parseRawText(clipboardText)

    assertEquals(2, result.size)
    assertEquals("2001", result[0].nis)
    assertEquals("Eko Prasetyo", result[0].name)
    assertEquals("L", result[0].gender)

    assertEquals("2002", result[1].nis)
    assertEquals("Fitri Handayani", result[1].name)
    assertEquals("P", result[1].gender)
  }
}
