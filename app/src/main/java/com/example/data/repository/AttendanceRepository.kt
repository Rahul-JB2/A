package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.AdminUser
import com.example.data.model.Department
import com.example.data.model.StaffAnnouncement
import com.example.data.model.StudentAttendanceRecord
import com.example.data.model.StudentAttendanceStatus
import com.example.data.model.StudentMember
import com.example.data.model.TeacherAttendanceRecord
import com.example.data.model.TeacherAttendanceStatus
import com.example.data.model.TeacherMember
import com.example.util.SecurityUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

sealed class QrScanResult {
    data class StudentSuccess(
        val student: StudentMember,
        val record: StudentAttendanceRecord,
        val isFirstTimeToday: Boolean
    ) : QrScanResult()

    data class FacultySuccess(
        val teacher: TeacherMember,
        val record: TeacherAttendanceRecord,
        val isFirstTimeToday: Boolean
    ) : QrScanResult()

    data class AlreadyMarked(
        val name: String,
        val id: String,
        val statusText: String,
        val time: String,
        val isStudent: Boolean = true
    ) : QrScanResult()

    data class NotFound(val rawCode: String) : QrScanResult()
    data class Error(val message: String) : QrScanResult()
}

class AttendanceRepository(private val database: AppDatabase) {

    private val adminDao = database.adminUserDao()
    private val deptDao = database.departmentDao()
    private val teacherDao = database.teacherMemberDao()
    private val attendanceDao = database.teacherAttendanceRecordDao()
    private val announcementDao = database.staffAnnouncementDao()
    private val studentDao = database.studentMemberDao()
    private val studentAttendanceDao = database.studentAttendanceRecordDao()

    val allDepartments: Flow<List<Department>> = deptDao.getAllDepartments()
    val allTeachers: Flow<List<TeacherMember>> = teacherDao.getAllTeachers()
    val allAnnouncements: Flow<List<StaffAnnouncement>> = announcementDao.getAllAnnouncements()
    val allAttendanceRecords: Flow<List<TeacherAttendanceRecord>> = attendanceDao.getAllAttendanceRecords()
    val allStudents: Flow<List<StudentMember>> = studentDao.getAllStudents()
    val allClasses: Flow<List<String>> = studentDao.getAllClasses()

    // Offline and Room Local Database Synchronization flows
    val unsyncedTeacherCount: Flow<Int> = attendanceDao.getUnsyncedTeacherCount()
    val unsyncedStudentCount: Flow<Int> = studentAttendanceDao.getUnsyncedStudentCount()
    val totalUnsyncedCount: Flow<Int> = combine(unsyncedTeacherCount, unsyncedStudentCount) { t, s -> t + s }
    val totalStoredRecordsCount: Flow<Int> = combine(
        attendanceDao.getTotalTeacherRecordsCount(),
        studentAttendanceDao.getTotalStudentRecordsCount()
    ) { t, s -> t + s }

    /**
     * Synchronizes all locally persisted offline Room attendance records (both faculty and student)
     * marking them as synced with a timestamp.
     */
    suspend fun syncOfflineAttendanceRecords(): Result<Int> {
        return try {
            val syncTimestamp = System.currentTimeMillis()
            val unsyncedTeachers = attendanceDao.getUnsyncedTeacherRecordsOnce()
            val unsyncedStudents = studentAttendanceDao.getUnsyncedStudentRecordsOnce()

            val teacherIds = unsyncedTeachers.map { it.id }.filter { it > 0 }
            val studentIds = unsyncedStudents.map { it.id }.filter { it > 0 }

            if (teacherIds.isNotEmpty()) {
                attendanceDao.markTeacherRecordsSynced(teacherIds, syncTimestamp)
            }
            if (studentIds.isNotEmpty()) {
                studentAttendanceDao.markStudentRecordsSynced(studentIds, syncTimestamp)
            }

            val totalSynced = teacherIds.size + studentIds.size
            Result.success(totalSynced)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkAndSeedInitialData() {
        val adminCount = adminDao.getCount()
        val existingTeachers = teacherDao.getAllTeachersOnce()
        val hasOldNames = existingTeachers.any {
            it.name.contains("Jenkins") || it.name.contains("Vance") || it.name.contains("Miller") || it.name.contains("Chen") || it.name.contains("Curie")
        }

        if (adminCount == 0 || hasOldNames) {
            seedDemoData()
        } else {
            val existingStudents = studentDao.getAllStudentsOnce()
            if (existingStudents.isEmpty()) {
                studentDao.insertStudents(DemoDataProvider.getDemoStudents())
            }

            val admin = adminDao.getAdminById(1)
            if (admin != null && (admin.schoolName.contains("Oakridge", ignoreCase = true) || admin.name.contains("Arthur Vance"))) {
                adminDao.updateAdmin(admin.copy(
                    schoolName = "Unique English School",
                    name = "Principal Dr. Rajesh Sharma",
                    email = "principal@uniqueenglishschool.edu",
                    phone = "+91 98765 43210"
                ))
            }
        }
    }

    suspend fun seedDemoData() {
        // Insert Admin
        adminDao.insertAdmin(DemoDataProvider.getDemoAdmin())

        // Insert Departments
        val depts = DemoDataProvider.getDemoDepartments()
        depts.forEach { deptDao.insertDepartment(it) }

        // Insert Teachers
        val teachers = DemoDataProvider.getDemoTeachers()
        teacherDao.insertTeachers(teachers)

        // Insert Attendance Records
        val attendance = DemoDataProvider.getDemoAttendanceRecords(teachers)
        attendanceDao.insertOrUpdateRecords(attendance)

        // Insert Announcements
        val announcements = DemoDataProvider.getDemoAnnouncements()
        announcements.forEach { announcementDao.insertAnnouncement(it) }

        // Insert Students
        studentDao.insertStudents(DemoDataProvider.getDemoStudents())
    }

    // Admin Auth
    suspend fun login(email: String, pass: String): Result<AdminUser> {
        val user = adminDao.getAdminByEmail(email.trim().lowercase())
            ?: return Result.failure(Exception("No account found with this email address."))
        val hashed = SecurityUtils.hashPassword(pass)
        return if (user.passwordHash == hashed) {
            Result.success(user)
        } else {
            Result.failure(Exception("Incorrect password. Please try again."))
        }
    }

    suspend fun register(
        name: String,
        email: String,
        pass: String,
        schoolName: String,
        role: String,
        phone: String
    ): Result<AdminUser> {
        val cleanEmail = email.trim().lowercase()
        val existing = adminDao.getAdminByEmail(cleanEmail)
        if (existing != null) {
            return Result.failure(Exception("An account with this email already exists."))
        }
        val admin = AdminUser(
            name = name.trim(),
            email = cleanEmail,
            passwordHash = SecurityUtils.hashPassword(pass),
            schoolName = schoolName.trim(),
            role = role.trim(),
            phone = phone.trim()
        )
        val id = adminDao.insertAdmin(admin)
        return Result.success(admin.copy(id = id))
    }

    suspend fun getAdminById(id: Long): AdminUser? = adminDao.getAdminById(id)
    suspend fun updateAdmin(admin: AdminUser) = adminDao.updateAdmin(admin)

    suspend fun changePassword(adminId: Long, oldPass: String, newPass: String): Result<Unit> {
        val admin = adminDao.getAdminById(adminId)
            ?: return Result.failure(Exception("Admin account not found."))
        if (admin.passwordHash != SecurityUtils.hashPassword(oldPass)) {
            return Result.failure(Exception("Current password does not match."))
        }
        val updated = admin.copy(passwordHash = SecurityUtils.hashPassword(newPass))
        adminDao.updateAdmin(updated)
        return Result.success(Unit)
    }

    // Department operations
    suspend fun insertDepartment(dept: Department): Long = deptDao.insertDepartment(dept)
    suspend fun updateDepartment(dept: Department) = deptDao.updateDepartment(dept)
    suspend fun deleteDepartment(dept: Department) = deptDao.deleteDepartment(dept)
    suspend fun getDepartmentById(id: Long): Department? = deptDao.getDepartmentById(id)

    // Teacher Member operations
    suspend fun insertTeacher(teacher: TeacherMember): Long = teacherDao.insertTeacher(teacher)
    suspend fun updateTeacher(teacher: TeacherMember) = teacherDao.updateTeacher(teacher)
    suspend fun deleteTeacher(teacher: TeacherMember) = teacherDao.deleteTeacher(teacher)
    suspend fun setTeacherActiveState(id: Long, isActive: Boolean) = teacherDao.setTeacherActiveState(id, isActive)
    suspend fun getTeacherById(id: Long): TeacherMember? = teacherDao.getTeacherById(id)
    suspend fun getTeachersForDepartmentOnce(deptId: Long): List<TeacherMember> =
        teacherDao.getTeachersForDepartmentOnce(deptId)

    // Attendance operations
    suspend fun saveAttendanceRecords(records: List<TeacherAttendanceRecord>) {
        attendanceDao.insertOrUpdateRecords(records)
    }

    suspend fun getAttendanceForDeptAndDateOnce(deptId: Long, dateString: String): List<TeacherAttendanceRecord> =
        attendanceDao.getAttendanceForDeptAndDateOnce(deptId, dateString)

    suspend fun getAttendanceForDateOnce(dateString: String): List<TeacherAttendanceRecord> =
        attendanceDao.getAttendanceForDateOnce(dateString)

    fun getAttendanceForTeacher(teacherId: Long): Flow<List<TeacherAttendanceRecord>> =
        attendanceDao.getAttendanceForTeacher(teacherId)

    // Staff Announcements
    suspend fun insertAnnouncement(announcement: StaffAnnouncement): Long =
        announcementDao.insertAnnouncement(announcement)

    suspend fun deleteAnnouncement(id: Long) = announcementDao.deleteAnnouncement(id)

    // Student operations
    fun getStudentsByClass(gradeClass: String): Flow<List<StudentMember>> =
        studentDao.getStudentsByClass(gradeClass)

    suspend fun getStudentsByClassOnce(gradeClass: String): List<StudentMember> =
        studentDao.getStudentsByClassOnce(gradeClass)

    fun getStudentAttendanceForClassAndDate(gradeClass: String, dateString: String): Flow<List<StudentAttendanceRecord>> =
        studentAttendanceDao.getAttendanceForClassAndDate(gradeClass, dateString)

    suspend fun getStudentAttendanceForClassAndDateOnce(gradeClass: String, dateString: String): List<StudentAttendanceRecord> =
        studentAttendanceDao.getAttendanceForClassAndDateOnce(gradeClass, dateString)

    suspend fun getStudentByCode(code: String): StudentMember? =
        studentDao.getStudentByCode(code.trim())

    suspend fun insertStudent(student: StudentMember): Long =
        studentDao.insertStudent(student)

    suspend fun updateStudent(student: StudentMember) =
        studentDao.updateStudent(student)

    suspend fun deleteStudent(student: StudentMember) =
        studentDao.deleteStudent(student)

    suspend fun recordStudentAttendance(
        student: StudentMember,
        dateString: String,
        status: StudentAttendanceStatus,
        checkInTime: String,
        scanMethod: String,
        teacherName: String,
        remarks: String = "",
        isOffline: Boolean = false
    ): StudentAttendanceRecord {
        val existing = studentAttendanceDao.getRecordForStudentAndDate(student.id, dateString)
        val record = if (existing != null) {
            existing.copy(
                status = status,
                checkInTime = checkInTime,
                scanMethod = scanMethod,
                markedByTeacherName = teacherName,
                remarks = remarks,
                timestamp = System.currentTimeMillis(),
                isSynced = !isOffline,
                syncTimestamp = if (!isOffline) System.currentTimeMillis() else 0L,
                isOfflineCreated = isOffline || existing.isOfflineCreated
            )
        } else {
            StudentAttendanceRecord(
                studentMemberId = student.id,
                studentId = student.studentId,
                studentName = student.name,
                rollNumber = student.rollNumber,
                gradeClass = student.gradeClass,
                dateString = dateString,
                status = status,
                checkInTime = checkInTime,
                scanMethod = scanMethod,
                markedByTeacherName = teacherName,
                remarks = remarks,
                timestamp = System.currentTimeMillis(),
                isSynced = !isOffline,
                syncTimestamp = if (!isOffline) System.currentTimeMillis() else 0L,
                isOfflineCreated = isOffline
            )
        }
        val id = studentAttendanceDao.insertOrUpdateRecord(record)
        return record.copy(id = if (record.id == 0L) id else record.id)
    }

    suspend fun saveStudentAttendanceBatch(
        records: List<StudentAttendanceRecord>,
        isOffline: Boolean = false
    ) {
        val updatedRecords = records.map {
            it.copy(
                isSynced = !isOffline,
                syncTimestamp = if (!isOffline) System.currentTimeMillis() else 0L,
                isOfflineCreated = isOffline
            )
        }
        studentAttendanceDao.insertOrUpdateRecords(updatedRecords)
    }

    suspend fun deleteStudentAttendanceForClassAndDate(gradeClass: String, dateString: String) {
        studentAttendanceDao.deleteAttendanceForClassAndDate(gradeClass, dateString)
    }

    /**
     * Processes any scanned QR code payload:
     * 1. Extracts student ID or code token.
     * 2. Checks against Student database.
     * 3. If matched, checks if already marked today. If yes, returns AlreadyMarked.
     *    If not, logs attendance as PRESENT with timestamp and returns StudentSuccess.
     * 4. If not a student, checks faculty teachers (dual-support for kiosk/teacher ID badges).
     * 5. If no match, returns NotFound.
     */
    suspend fun processScannedQrCode(
        rawScannedPayload: String,
        dateString: String,
        timeString: String,
        markedByTeacherName: String,
        preferredClass: String? = null,
        isOffline: Boolean = false
    ): QrScanResult {
        val raw = rawScannedPayload.trim()
        if (raw.isBlank()) {
            return QrScanResult.Error("Empty QR code payload scanned.")
        }

        // 1. Extract potential ID token from plain string, json or url
        val candidateCodes = mutableListOf<String>()
        candidateCodes.add(raw)

        // If JSON format like {"studentId":"STU-2026-101"}
        if (raw.contains("studentId") || raw.contains("id") || raw.contains("roll")) {
            val jsonPattern = Regex("\"(?:studentId|id|code|employeeId)\"\\s*:\\s*\"([^\"]+)\"")
            jsonPattern.find(raw)?.groupValues?.getOrNull(1)?.let { candidateCodes.add(it.trim()) }
        }

        // If URL format like ?id=STU-2026-101
        if (raw.contains("=")) {
            val urlPattern = Regex("[?&](?:id|studentId|code)=([^&]+)")
            urlPattern.find(raw)?.groupValues?.getOrNull(1)?.let { candidateCodes.add(it.trim()) }
        }

        // Prefix stripping e.g. "ID: STU-2026-101"
        if (raw.contains(":")) {
            val afterColon = raw.substringAfter(":").trim()
            if (afterColon.isNotBlank()) candidateCodes.add(afterColon)
        }

        // 2. Check Students
        for (code in candidateCodes) {
            var student = studentDao.getStudentByCode(code)
            if (student == null) {
                // Try case-insensitive matching from all students
                val allStudents = studentDao.getAllStudentsOnce()
                student = allStudents.firstOrNull {
                    it.studentId.equals(code, ignoreCase = true) ||
                            it.qrCodeData.equals(code, ignoreCase = true) ||
                            (preferredClass != null && it.gradeClass == preferredClass && it.rollNumber == code)
                }
            }

            if (student != null) {
                val existingRecord = studentAttendanceDao.getRecordForStudentAndDate(student.id, dateString)
                if (existingRecord != null && existingRecord.status == StudentAttendanceStatus.PRESENT) {
                    return QrScanResult.AlreadyMarked(
                        name = student.name,
                        id = student.studentId,
                        statusText = "Already marked Present",
                        time = existingRecord.checkInTime.ifBlank { timeString },
                        isStudent = true
                    )
                }

                val savedRecord = recordStudentAttendance(
                    student = student,
                    dateString = dateString,
                    status = StudentAttendanceStatus.PRESENT,
                    checkInTime = timeString,
                    scanMethod = "QR_SCAN",
                    teacherName = markedByTeacherName,
                    remarks = if (isOffline) "Scanned Offline (Stored in Room DB)" else "Scanned via CameraX QR Scanner",
                    isOffline = isOffline
                )

                return QrScanResult.StudentSuccess(
                    student = student,
                    record = savedRecord,
                    isFirstTimeToday = (existingRecord == null)
                )
            }
        }

        // 3. Fallback: Check if it is a Faculty Member ID
        val teachers = teacherDao.getAllTeachersOnce()
        for (code in candidateCodes) {
            val matchedTeacher = teachers.firstOrNull {
                it.employeeId.equals(code, ignoreCase = true)
            }
            if (matchedTeacher != null) {
                val records = attendanceDao.getAttendanceForDeptAndDateOnce(matchedTeacher.departmentId, dateString)
                val existing = records.firstOrNull { it.teacherMemberId == matchedTeacher.id }
                if (existing != null && existing.status == TeacherAttendanceStatus.PRESENT) {
                    return QrScanResult.AlreadyMarked(
                        name = matchedTeacher.name,
                        id = matchedTeacher.employeeId,
                        statusText = "Faculty already marked Present",
                        time = existing.checkInTime.ifBlank { timeString },
                        isStudent = false
                    )
                }

                val newRecord = TeacherAttendanceRecord(
                    teacherMemberId = matchedTeacher.id,
                    employeeId = matchedTeacher.employeeId,
                    teacherName = matchedTeacher.name,
                    departmentId = matchedTeacher.departmentId,
                    departmentName = matchedTeacher.departmentName,
                    designation = matchedTeacher.designation,
                    dateString = dateString,
                    status = TeacherAttendanceStatus.PRESENT,
                    checkInTime = timeString,
                    remarks = if (isOffline) "Kiosk Offline Scan (Room DB)" else "Kiosk CameraX Scan",
                    markedByAdminName = markedByTeacherName,
                    isSynced = !isOffline,
                    syncTimestamp = if (!isOffline) System.currentTimeMillis() else 0L,
                    isOfflineCreated = isOffline
                )
                attendanceDao.insertOrUpdateRecord(newRecord)
                return QrScanResult.FacultySuccess(
                    teacher = matchedTeacher,
                    record = newRecord,
                    isFirstTimeToday = (existing == null)
                )
            }
        }

        return QrScanResult.NotFound(raw)
    }
}
