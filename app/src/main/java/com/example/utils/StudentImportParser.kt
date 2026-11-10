package com.example.utils

import android.content.Context
import android.net.Uri
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Locale
import java.util.zip.ZipInputStream

data class ParsedStudent(
    val nis: String,
    val name: String,
    val gender: String = "L", // "L" atau "P"
    val phone: String = ""
)

object StudentImportParser {

    /**
     * Membaca file Excel (.xlsx) atau file CSV/TSV berdasarkan ekstensi atau deteksi konten.
     */
    fun parseStream(inputStream: InputStream, fileName: String? = null): List<ParsedStudent> {
        val lowerName = fileName?.lowercase(Locale.ROOT) ?: ""
        // Baca seluruh byte stream ke memori agar bisa di-rewind jika diperlukan
        val bytes = inputStream.use { it.readBytes() }
        if (bytes.isEmpty()) return emptyList()

        return if (lowerName.endsWith(".xlsx") || isZipArchive(bytes)) {
            try {
                parseXlsx(ByteArrayInputStream(bytes))
            } catch (e: Exception) {
                // Fallback jika bukan zip valid
                parseCsv(ByteArrayInputStream(bytes))
            }
        } else {
            parseCsv(ByteArrayInputStream(bytes))
        }
    }

    /**
     * Membaca teks mentah yang ditempel (copy-paste langsung dari Microsoft Excel atau Google Sheets).
     * Saat copy tabel di Excel, clipboard menyimpan format Tab-Separated Values (TSV).
     */
    fun parseRawText(rawText: String): List<ParsedStudent> {
        if (rawText.isBlank()) return emptyList()
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        return parseRows(lines.map { parseLineToColumns(it) })
    }

    /**
     * Parser native untuk file Excel (.xlsx).
     * Format XLSX adalah arsip ZIP yang memuat xl/sharedStrings.xml dan xl/worksheets/sheet1.xml.
     */
    fun parseXlsx(inputStream: InputStream): List<ParsedStudent> {
        val zip = ZipInputStream(inputStream)
        val sharedStrings = mutableListOf<String>()
        var sheetBytes: ByteArray? = null

        var entry = zip.nextEntry
        while (entry != null) {
            val name = entry.name
            if (name == "xl/sharedStrings.xml" || name.endsWith("/sharedStrings.xml")) {
                sharedStrings.addAll(parseSharedStrings(zip))
            } else if (name == "xl/worksheets/sheet1.xml" || name.contains("worksheets/sheet1.xml")) {
                val out = ByteArrayOutputStream()
                val buffer = ByteArray(4096)
                var len: Int
                while (zip.read(buffer).also { len = it } > 0) {
                    out.write(buffer, 0, len)
                }
                sheetBytes = out.toByteArray()
            }
            zip.closeEntry()
            entry = zip.nextEntry
        }

        if (sheetBytes == null) return emptyList()

        val rows = parseSheetXml(ByteArrayInputStream(sheetBytes), sharedStrings)
        return parseRows(rows)
    }

    private fun parseSharedStrings(stream: InputStream): List<String> {
        val list = mutableListOf<String>()
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(stream, "UTF-8")

        var eventType = parser.eventType
        var inText = false
        val currentStr = StringBuilder()

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "si") {
                        currentStr.setLength(0)
                    } else if (parser.name == "t") {
                        inText = true
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inText) {
                        currentStr.append(parser.text)
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "t") {
                        inText = false
                    } else if (parser.name == "si") {
                        list.add(currentStr.toString())
                    }
                }
            }
            eventType = parser.next()
        }
        return list
    }

    private fun parseSheetXml(stream: InputStream, sharedStrings: List<String>): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(stream, "UTF-8")

        var eventType = parser.eventType
        var currentRow = mutableListOf<String>()
        var currentCellType = ""
        var cellValue = StringBuilder()
        var inV = false
        var lastColIndex = -1

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "row" -> {
                            currentRow = mutableListOf()
                            lastColIndex = -1
                        }
                        "c" -> {
                            currentCellType = parser.getAttributeValue(null, "t") ?: ""
                            val cellRef = parser.getAttributeValue(null, "r") ?: ""
                            val colIndex = columnRefToIndex(cellRef)
                            // Jika ada sel kosong yang dilewati di Excel (misal A1 lalu C1), isi dengan string kosong
                            if (colIndex > lastColIndex + 1 && lastColIndex >= 0) {
                                for (i in (lastColIndex + 1) until colIndex) {
                                    currentRow.add("")
                                }
                            }
                            lastColIndex = colIndex
                            cellValue.setLength(0)
                        }
                        "v", "t" -> inV = true
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inV) {
                        cellValue.append(parser.text)
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "v", "t" -> inV = false
                        "c" -> {
                            val raw = cellValue.toString().trim()
                            val finalVal = if (currentCellType == "s") {
                                val idx = raw.toIntOrNull()
                                if (idx != null && idx in sharedStrings.indices) {
                                    sharedStrings[idx]
                                } else raw
                            } else {
                                cleanNumericString(raw)
                            }
                            currentRow.add(finalVal)
                        }
                        "row" -> {
                            if (currentRow.any { it.isNotBlank() }) {
                                rows.add(currentRow)
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return rows
    }

    private fun columnRefToIndex(cellRef: String): Int {
        var colIndex = 0
        var foundChar = false
        for (ch in cellRef.uppercase(Locale.US)) {
            if (ch in 'A'..'Z') {
                colIndex = colIndex * 26 + (ch - 'A' + 1)
                foundChar = true
            } else {
                break
            }
        }
        return if (foundChar) colIndex - 1 else 0
    }

    /**
     * Membaca file CSV / TSV dengan deteksi otomatis pemisah (koma, titik koma, atau tab).
     */
    fun parseCsv(inputStream: InputStream): List<ParsedStudent> {
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val rawLines = mutableListOf<String>()
        var line: String? = reader.readLine()
        while (line != null) {
            val trimmed = line.trim()
            if (trimmed.isNotEmpty()) {
                rawLines.add(trimmed)
            }
            line = reader.readLine()
        }

        if (rawLines.isEmpty()) return emptyList()

        val rows = rawLines.map { parseLineToColumns(it) }
        return parseRows(rows)
    }

    private fun parseLineToColumns(line: String): List<String> {
        // Hapus UTF-8 BOM jika ada
        val clean = if (line.startsWith("\uFEFF")) line.substring(1) else line

        // Tentukan delimiter: tab, titik koma, atau koma
        val delimiter = when {
            clean.contains("\t") -> '\t'
            clean.count { it == ';' } >= clean.count { it == ',' } && clean.contains(";") -> ';'
            else -> ','
        }

        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false

        for (ch in clean) {
            when {
                ch == '\"' -> inQuotes = !inQuotes
                ch == delimiter && !inQuotes -> {
                    result.add(current.toString().trim())
                    current.setLength(0)
                }
                else -> current.append(ch)
            }
        }
        result.add(current.toString().trim())
        return result.map { cleanNumericString(it) }
    }

    /**
     * Memproses baris tabel mentah menjadi daftar Siswa.
     * Mendeteksi header otomatis (NIS, Nama, Jenis Kelamin, No HP).
     */
    private fun parseRows(rows: List<List<String>>): List<ParsedStudent> {
        if (rows.isEmpty()) return emptyList()

        // Periksa apakah baris pertama adalah baris header
        val firstRow = rows[0].map { it.lowercase(Locale.ROOT).trim() }
        var hasHeader = false

        var nisCol = -1
        var nameCol = -1
        var genderCol = -1
        var phoneCol = -1

        firstRow.forEachIndexed { index, title ->
            when {
                title.contains("gender") || title.contains("jk") || title.contains("kelamin") || title.contains("l/p") || title.contains("sex") -> {
                    genderCol = index
                    hasHeader = true
                }
                title.contains("nama") || title.contains("siswa") || title.contains("peserta") || title.contains("name") -> {
                    nameCol = index
                    hasHeader = true
                }
                title.contains("hp") || title.contains("wa") || title.contains("telp") || title.contains("phone") || title.contains("kontak") -> {
                    phoneCol = index
                    hasHeader = true
                }
                !title.contains("jenis") && (title.contains("nis") || title.contains("induk") || title.contains("no absen") || title.contains("absen") || title == "no" || title == "nomor") -> {
                    nisCol = index
                    hasHeader = true
                }
            }
        }

        val dataRows = if (hasHeader) rows.drop(1) else rows

        // Jika tidak ada header terdeteksi, gunakan urutan kolom default:
        // Kolom 0: NIS, Kolom 1: Nama, Kolom 2: L/P, Kolom 3: No HP
        // Atau jika hanya 2 kolom: Kolom 0: NIS / No, Kolom 1: Nama
        if (nameCol == -1) {
            when {
                rows.firstOrNull()?.size == 1 -> {
                    nameCol = 0
                }
                rows.firstOrNull()?.size ?: 0 >= 2 -> {
                    // Cek apakah kolom 0 berupa angka (NIS) dan kolom 1 teks (Nama)
                    val sample = rows.firstOrNull()
                    val col0IsDigit = sample?.getOrNull(0)?.all { it.isDigit() || it == '-' } == true
                    if (col0IsDigit) {
                        nisCol = 0
                        nameCol = 1
                        genderCol = 2
                        phoneCol = 3
                    } else {
                        nameCol = 0
                        genderCol = 1
                        phoneCol = 2
                    }
                }
            }
        }

        val result = mutableListOf<ParsedStudent>()
        var autoNisCounter = 101

        dataRows.forEach { row ->
            val rawName = if (nameCol in row.indices) row[nameCol].trim() else ""
            if (rawName.isNotBlank() && !isHeaderRow(rawName)) {
                var rawNis = if (nisCol in row.indices) row[nisCol].trim() else ""
                if (rawNis.isBlank()) {
                    rawNis = (autoNisCounter++).toString()
                }

                val rawGender = if (genderCol in row.indices) row[genderCol].trim() else "L"
                val rawPhone = if (phoneCol in row.indices) row[phoneCol].trim() else ""

                result.add(
                    ParsedStudent(
                        nis = cleanNumericString(rawNis),
                        name = rawName,
                        gender = normalizeGender(rawGender),
                        phone = cleanNumericString(rawPhone)
                    )
                )
            }
        }

        return result
    }

    private fun isHeaderRow(name: String): Boolean {
        val lower = name.lowercase(Locale.ROOT)
        return lower == "nama" || lower == "nama siswa" || lower == "nama lengkap" || lower == "name"
    }

    private fun normalizeGender(genderRaw: String): String {
        val lower = genderRaw.trim().lowercase(Locale.ROOT)
        return when {
            lower.startsWith("p") || lower == "wanita" || lower == "female" || lower == "perempuan" || lower == "f" -> "P"
            else -> "L"
        }
    }

    private fun cleanNumericString(str: String): String {
        val s = str.trim()
        // Hilangkan akhiran .0 dari float Excel (cth: "102401.0" -> "102401")
        return if (s.endsWith(".0") && s.substringBefore(".0").all { it.isDigit() }) {
            s.substringBefore(".0")
        } else {
            s
        }
    }

    private fun isZipArchive(bytes: ByteArray): Boolean {
        return bytes.size >= 4 &&
                bytes[0] == 0x50.toByte() &&
                bytes[1] == 0x4B.toByte() &&
                bytes[2] == 0x03.toByte() &&
                bytes[3] == 0x04.toByte()
    }

    /**
     * Menghasilkan teks template file Excel / CSV yang siap diisi guru.
     */
    fun generateTemplateCsv(): String {
        val sb = StringBuilder()
        sb.append("\uFEFF") // UTF-8 BOM
        sb.append("NIS,Nama Siswa,Jenis Kelamin (L/P),No HP\n")
        sb.append("102401,Ahmad Fauzi,L,081234567890\n")
        sb.append("102402,Aisyah Putri Rahma,P,081234567891\n")
        sb.append("102403,Budi Santoso,L,081234567892\n")
        sb.append("102404,Citra Dewi Lestari,P,081234567893\n")
        sb.append("102405,Dimas Arya Pratama,L,081234567894\n")
        return sb.toString()
    }
}
