package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.util.Locale

data class TeacherReportRow(
    val employeeId: String,
    val teacherName: String,
    val departmentName: String,
    val designation: String,
    val presentCount: Int,
    val absentCount: Int,
    val lateCount: Int,
    val leaveCount: Int,
    val totalDays: Int,
    val percentage: Float
)

data class MonthlyTeacherReportRow(
    val teacherId: Long,
    val employeeId: String,
    val teacherName: String,
    val departmentName: String,
    val designation: String,
    val monthYear: String,
    val monthLabel: String,
    val presentCount: Int,
    val absentCount: Int,
    val lateCount: Int,
    val leaveCount: Int,
    val totalDays: Int,
    val attendedDays: Int,
    val percentage: Float,
    val punctualityRate: Float,
    val performanceTier: String,
    val dailyRecords: List<com.example.data.model.TeacherAttendanceRecord> = emptyList()
)

object CsvExporter {

    fun generateTeacherSummaryCsv(
        rows: List<TeacherReportRow>,
        departmentName: String,
        period: String,
        schoolName: String = "Unique English School"
    ): String {
        val sb = StringBuilder()
        sb.append("${escape(schoolName)} - Official Faculty Attendance Register\n")
        sb.append("Department: ,\"${escape(departmentName)}\"\n")
        sb.append("Report Period: ,\"${escape(period)}\"\n")
        sb.append("Generated On: ,\"${DateUtils.formatIsoToDisplay(DateUtils.getTodayIso())}\"\n\n")

        // CSV Header
        sb.append("Employee ID,Teacher Name,Department,Designation,Present Days,Absent Days,Late Days,Leave Days,Total Days,Attendance %\n")

        // Rows
        for (row in rows) {
            sb.append("\"${escape(row.employeeId)}\",")
            sb.append("\"${escape(row.teacherName)}\",")
            sb.append("\"${escape(row.departmentName)}\",")
            sb.append("\"${escape(row.designation)}\",")
            sb.append("${row.presentCount},")
            sb.append("${row.absentCount},")
            sb.append("${row.lateCount},")
            sb.append("${row.leaveCount},")
            sb.append("${row.totalDays},")
            sb.append("\"${String.format(Locale.US, "%.1f", row.percentage)}%\"\n")
        }

        return sb.toString()
    }

    fun generateMonthlyAttendanceReportCsv(
        rows: List<MonthlyTeacherReportRow>,
        monthLabel: String,
        departmentName: String,
        schoolName: String = "Unique English School",
        targetPercentage: Int = 85
    ): String {
        val sb = StringBuilder()
        sb.append("${escape(schoolName)} - Official Faculty Attendance Register\n")
        sb.append("Report Type: ,\"Official Monthly Faculty Attendance Register\"\n")
        sb.append("Month / Period: ,\"${escape(monthLabel)}\"\n")
        sb.append("Department Filter: ,\"${escape(departmentName)}\"\n")
        sb.append("School Attendance Benchmark: ,\"$targetPercentage%\"\n")
        sb.append("Generated On: ,\"${DateUtils.formatIsoToDisplay(DateUtils.getTodayIso())}\"\n\n")

        val totalTeachers = rows.size
        val avgPercentage = if (rows.isNotEmpty()) rows.map { it.percentage }.average().toFloat() else 0f
        val meetingTarget = rows.count { it.percentage >= targetPercentage }
        val belowBenchmark = rows.count { it.percentage < 75f }

        sb.append("MONTHLY SUMMARY KPI\n")
        sb.append("Total Faculty Evaluated: ,$totalTeachers\n")
        sb.append("Monthly Faculty Attendance Average: ,\"${String.format(Locale.US, "%.1f", avgPercentage)}%\"\n")
        sb.append("Faculty Meeting Target (${targetPercentage}%+): ,$meetingTarget\n")
        sb.append("Faculty Below 75% Benchmark: ,$belowBenchmark\n\n")

        sb.append("Employee ID,Teacher Name,Department,Designation,Total Working Days,Present Days,Late Days,Absent Days,Leave Days,Attended Days,Monthly Attendance %,Punctuality %,Performance Rating\n")

        for (row in rows) {
            sb.append("\"${escape(row.employeeId)}\",")
            sb.append("\"${escape(row.teacherName)}\",")
            sb.append("\"${escape(row.departmentName)}\",")
            sb.append("\"${escape(row.designation)}\",")
            sb.append("${row.totalDays},")
            sb.append("${row.presentCount},")
            sb.append("${row.lateCount},")
            sb.append("${row.absentCount},")
            sb.append("${row.leaveCount},")
            sb.append("${row.attendedDays},")
            sb.append("\"${String.format(Locale.US, "%.1f", row.percentage)}%\",")
            sb.append("\"${String.format(Locale.US, "%.1f", row.punctualityRate)}%\",")
            sb.append("\"${escape(row.performanceTier)}\"\n")
        }

        sb.append("\n\"Certified and verified by Attendance Incharge / Principal\"\n")
        sb.append("\"${escape(schoolName)} - Faculty Academic Directorate\"\n")
        sb.append("\"Application Creator: Ritesh Kumar\"\n")

        return sb.toString()
    }

    private fun escape(data: String): String {
        return data.replace("\"", "\"\"")
    }

    fun shareCsv(context: Context, csvContent: String, fileNamePrefix: String) {
        try {
            val fileName = "${fileNamePrefix}_${System.currentTimeMillis()}.csv"
            val cachePath = File(context.cacheDir, "reports")
            cachePath.mkdirs()
            val file = File(cachePath, fileName)
            file.writeText(csvContent)

            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "Faculty Attendance Report - $fileNamePrefix")
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Share Teacher Attendance Report"))
        } catch (e: Exception) {
            // Fallback to plain text share if FileProvider is unavailable
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Faculty Attendance Report")
                putExtra(Intent.EXTRA_TEXT, csvContent)
            }
            context.startActivity(Intent.createChooser(intent, "Share Teacher Attendance Report"))
        }
    }
}
