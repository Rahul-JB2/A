package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Administrator or Principal managing the school's faculty attendance.
 */
@Entity(tableName = "admin_users")
data class AdminUser(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val schoolName: String,
    val role: String = "Principal", // Principal, Vice Principal, Admin, Attendance Coordinator
    val phone: String = "",
    val countLateAsAttended: Boolean = true,
    val minimumAttendanceTarget: Int = 85, // Standard staff benchmark %
    val standardCheckInTime: String = "08:00 AM",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * School Department or Academic Wing (e.g., Science, Mathematics, Languages, Primary Wing).
 */
@Entity(tableName = "departments")
data class Department(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val adminId: Long = 0,
    val departmentName: String,
    val code: String = "", // e.g. SCI, MATH, LANG
    val headOfDepartment: String = "",
    val roomOrOffice: String = "",
    val academicYear: String = "2026-2027",
    val createdAt: Long = System.currentTimeMillis()
) {
    val fullDisplayName: String
        get() = if (code.isNotBlank()) "$departmentName ($code)" else departmentName
}

/**
 * Teacher / Faculty Member whose attendance is recorded daily.
 */
@Entity(
    tableName = "faculty_teachers",
    indices = [
        Index(value = ["employeeId"], unique = true),
        Index(value = ["departmentId"])
    ]
)
data class TeacherMember(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val departmentId: Long,
    val name: String,
    val employeeId: String, // e.g. TCH-101
    val designation: String, // e.g. Senior Lecturer, PGT Physics, TGT English, Lab Instructor
    val departmentName: String,
    val phone: String = "",
    val email: String = "",
    val qualification: String = "", // e.g. M.Sc., B.Ed., Ph.D.
    val gender: String = "Other",
    val isActive: Boolean = true, // Active or on long leave/inactive
    val createdAt: Long = System.currentTimeMillis()
)

enum class TeacherAttendanceStatus {
    PRESENT,
    ABSENT,
    LATE,
    ON_LEAVE
}

/**
 * Daily Attendance Record for an individual teacher/faculty member.
 */
@Entity(
    tableName = "teacher_attendance_records",
    indices = [
        Index(value = ["teacherMemberId", "dateString"], unique = true),
        Index(value = ["departmentId", "dateString"]),
        Index(value = ["dateString"]),
        Index(value = ["teacherMemberId"]),
        Index(value = ["isSynced"])
    ]
)
data class TeacherAttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val teacherMemberId: Long,
    val employeeId: String,
    val teacherName: String,
    val departmentId: Long,
    val departmentName: String,
    val designation: String,
    val dateString: String, // Format: YYYY-MM-DD
    val status: TeacherAttendanceStatus,
    val checkInTime: String = "", // e.g. 07:55 AM
    val remarks: String = "", // e.g. Casual Leave, Medical Leave, Arrived 8:20 AM, On Duty Training
    val markedByAdminId: Long = 0,
    val markedByAdminName: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false, // Stored locally in Room; true when synced to remote/cloud
    val syncTimestamp: Long = 0L,
    val isOfflineCreated: Boolean = true // Created while offline or via Room local store
)

/**
 * Staff Room Announcements & Circulars for Teachers.
 */
@Entity(tableName = "staff_announcements")
data class StaffAnnouncement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val adminId: Long = 0,
    val authorName: String,
    val departmentId: Long = 0, // 0 = All Faculty / All Departments
    val targetAudience: String = "All Faculty",
    val title: String,
    val message: String,
    val priority: String = "Normal", // Normal, Important, Urgent
    val dateString: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Student enrolled in the school whose attendance can be logged by scanning their Student ID QR Code.
 */
@Entity(
    tableName = "students",
    indices = [
        Index(value = ["studentId"], unique = true),
        Index(value = ["gradeClass"])
    ]
)
data class StudentMember(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: String, // e.g. "STU-2026-101"
    val rollNumber: String, // e.g. "101"
    val name: String, // e.g. "Aarav Sharma"
    val gradeClass: String, // e.g. "Class 10-A"
    val section: String = "A",
    val gender: String = "Male",
    val guardianName: String = "",
    val guardianPhone: String = "",
    val qrCodeData: String = studentId,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

enum class StudentAttendanceStatus {
    PRESENT,
    ABSENT,
    LATE,
    EXCUSED
}

/**
 * Daily student attendance record logged by teachers, either via QR Code Scanner or quick-tap manual entry.
 */
@Entity(
    tableName = "student_attendance_records",
    indices = [
        Index(value = ["studentMemberId", "dateString"], unique = true),
        Index(value = ["gradeClass", "dateString"]),
        Index(value = ["dateString"]),
        Index(value = ["studentMemberId"]),
        Index(value = ["isSynced"])
    ]
)
data class StudentAttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentMemberId: Long,
    val studentId: String,
    val studentName: String,
    val rollNumber: String,
    val gradeClass: String,
    val dateString: String, // YYYY-MM-DD
    val status: StudentAttendanceStatus,
    val checkInTime: String = "", // e.g. "08:12 AM"
    val scanMethod: String = "QR_SCAN", // "QR_SCAN" or "MANUAL"
    val markedByTeacherName: String = "",
    val remarks: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false, // Stored locally in Room; true when synced to remote/cloud
    val syncTimestamp: Long = 0L,
    val isOfflineCreated: Boolean = true // Created while offline or via Room local store
)

/**
 * Offline and Room database synchronization summary.
 */
data class OfflineSyncSummary(
    val totalTeacherRecords: Int = 0,
    val totalStudentRecords: Int = 0,
    val unsyncedTeacherCount: Int = 0,
    val unsyncedStudentCount: Int = 0,
    val isOffline: Boolean = false,
    val lastSyncTimestamp: Long = 0L
) {
    val totalUnsyncedCount: Int
        get() = unsyncedTeacherCount + unsyncedStudentCount

    val totalRecordsCount: Int
        get() = totalTeacherRecords + totalStudentRecords
}
