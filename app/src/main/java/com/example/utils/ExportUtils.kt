package com.example.utils

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.FileProvider
import com.example.data.local.entity.AssignmentEntity
import com.example.data.local.entity.AttendanceEntity
import com.example.data.local.entity.ClassEntity
import com.example.data.local.entity.ScoreEntity
import com.example.data.local.entity.StudentEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtils {

    /**
     * Menghasilkan format CSV (Excel-Compatible) untuk Rekap Absensi Siswa.
     * Menggunakan UTF-8 BOM agar Microsoft Excel langsung membaca aksen & pemisah kolom dengan tepat.
     */
    fun generateAttendanceCsv(
        classEntity: ClassEntity,
        students: List<StudentEntity>,
        attendances: List<AttendanceEntity>,
        period: String
    ): String {
        val sb = StringBuilder()
        // UTF-8 BOM for Microsoft Excel auto-detect
        sb.append("\uFEFF")
        sb.append("REKAP ABSENSI SISWA - GURUKITA\n")
        sb.append("Kelas,${classEntity.name}\n")
        sb.append("Tahun Ajaran,${classEntity.academicYear} - Semester ${classEntity.semester}\n")
        sb.append("Periode,${period}\n")
        sb.append("Tanggal Cetak,${SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID")).format(Date())}\n\n")

        // Header Tabel
        sb.append("No,NIS,Nama Siswa,L/P,Hadir (H),Izin (I),Sakit (S),Alpa (A),Total Hari,Persentase Kehadiran (%)\n")

        students.forEachIndexed { index, student ->
            val studentAtt = attendances.filter { it.studentId == student.id }
            val hadir = studentAtt.count { it.status == "H" }
            val izin = studentAtt.count { it.status == "I" }
            val sakit = studentAtt.count { it.status == "S" }
            val alpa = studentAtt.count { it.status == "A" }
            val total = hadir + izin + sakit + alpa
            val percent = if (total > 0) String.format(Locale.US, "%.1f", (hadir.toDouble() / total.toDouble()) * 100.0) else "0.0"

            val escapedName = "\"${student.name.replace("\"", "\"\"")}\""
            sb.append("${index + 1},${student.nis},$escapedName,${student.gender},$hadir,$izin,$sakit,$alpa,$total,$percent%\n")
        }

        return sb.toString()
    }

    /**
     * Menghasilkan format CSV (Excel-Compatible) untuk Rekap Nilai Tugas & Ujian.
     */
    fun generateGradesCsv(
        classEntity: ClassEntity,
        students: List<StudentEntity>,
        assignments: List<AssignmentEntity>,
        scores: List<ScoreEntity>
    ): String {
        val sb = StringBuilder()
        sb.append("\uFEFF")
        sb.append("REKAP NILAI SISWA - GURUKITA\n")
        sb.append("Kelas,${classEntity.name}\n")
        sb.append("Tahun Ajaran,${classEntity.academicYear} - Semester ${classEntity.semester}\n")
        sb.append("Tanggal Cetak,${SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID")).format(Date())}\n\n")

        // Header baris 1
        sb.append("No,NIS,Nama Siswa,L/P")
        assignments.forEach { task ->
            val escapedTask = "\"${task.title.replace("\"", "\"\"")} (KKM: ${task.passingGrade.toInt()})\""
            sb.append(",$escapedTask")
        }
        sb.append(",Rata-rata,Status Kelulusan\n")

        students.forEachIndexed { index, student ->
            val studentScores = scores.filter { it.studentId == student.id }
            sb.append("${index + 1},${student.nis},\"${student.name.replace("\"", "\"\"")}\",${student.gender}")

            var totalValidScore = 0.0
            var countValid = 0
            var isAllPassing = true

            assignments.forEach { task ->
                val s = studentScores.find { it.assignmentId == task.id }?.score
                if (s != null) {
                    sb.append(",${s.toInt()}")
                    totalValidScore += s
                    countValid++
                    if (s < task.passingGrade) {
                        isAllPassing = false
                    }
                } else {
                    sb.append(",-")
                }
            }

            val avg = if (countValid > 0) String.format(Locale.US, "%.1f", totalValidScore / countValid) else "0.0"
            val status = if (countValid == 0) "Belum Ada Nilai" else if (isAllPassing) "Tuntas" else "Remedial"
            sb.append(",$avg,$status\n")
        }

        return sb.toString()
    }

    /**
     * Menghasilkan HTML cetak yang rapi untuk dicetak ke PDF atau Printer Fisik melalui Android PrintManager.
     */
    fun generatePrintableHtml(
        title: String,
        subtitle: String,
        metaInfo: Map<String, String>,
        headers: List<String>,
        rows: List<List<String>>,
        notes: String = ""
    ): String {
        val metaHtml = metaInfo.entries.joinToString("") {
            "<tr><td style='width:160px; font-weight:600; color:#374151;'>${it.key}</td><td style='color:#111827;'>: ${it.value}</td></tr>"
        }

        val headerHtml = headers.joinToString("") {
            "<th style='background-color:#1E3A8A; color:white; padding:10px 8px; font-size:12px; border:1px solid #CBD5E1;'>$it</th>"
        }

        val rowsHtml = rows.mapIndexed { idx, row ->
            val bg = if (idx % 2 == 0) "#FFFFFF" else "#F8FAFC"
            val cells = row.joinToString("") { cell ->
                "<td style='padding:8px; font-size:12px; border:1px solid #E2E8F0; text-align:center;'>$cell</td>"
            }
            "<tr style='background-color:$bg;'>$cells</tr>"
        }.joinToString("")

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <style>
                    body { font-family: 'Helvetica Neue', Arial, sans-serif; margin: 24px; color: #1F2937; }
                    .header { border-bottom: 2px solid #1E3A8A; padding-bottom: 12px; margin-bottom: 16px; }
                    .title { font-size: 20px; font-weight: bold; color: #1E3A8A; margin: 0; }
                    .subtitle { font-size: 14px; color: #4B5563; margin-top: 4px; }
                    table.meta { margin-bottom: 16px; border-collapse: collapse; font-size: 13px; }
                    table.data { width: 100%; border-collapse: collapse; margin-top: 12px; }
                    .footer { margin-top: 32px; font-size: 12px; text-align: right; color: #6B7280; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1 class="title">$title</h1>
                    <div class="subtitle">$subtitle</div>
                </div>
                <table class="meta">
                    $metaHtml
                </table>
                <table class="data">
                    <thead><tr>$headerHtml</tr></thead>
                    <tbody>$rowsHtml</tbody>
                </table>
                <div class="footer">
                    <p>Dicetak otomatis melalui Aplikasi <strong>GuruKita</strong> pada ${SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID")).format(Date())}</p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    /**
     * Membuka Android PrintManager untuk membuat file PDF resmi atau mencetak ke printer.
     */
    fun printDocument(context: Context, htmlContent: String, jobName: String) {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                printManager?.print(jobName, printAdapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null)
    }

    /**
     * Membagikan file text/CSV ke WhatsApp, Email, Drive, atau aplikasi lain.
     */
    fun shareCsvContent(context: Context, csvContent: String, fileName: String, subject: String) {
        try {
            val cacheDir = File(context.cacheDir, "exports")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val file = File(cacheDir, fileName)
            FileOutputStream(file).use { out ->
                out.write(csvContent.toByteArray(Charsets.UTF_8))
            }

            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Bagikan Rekap GuruKita"))
        } catch (e: Exception) {
            // Fallback plain text share jika FileProvider belum di-mount
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, csvContent)
            }
            context.startActivity(Intent.createChooser(intent, "Bagikan Rekap"))
        }
    }
}
