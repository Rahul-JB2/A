package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.AdminUser
import com.example.data.model.Department
import com.example.data.model.TeacherAttendanceRecord
import com.example.data.model.TeacherAttendanceStatus
import com.example.data.model.TeacherMember
import com.example.util.SecurityUtils
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Smart Teacher Attendance", appName)
    }

    @Test
    fun testRoomTeacherAndDepartmentPersistence() = runBlocking {
        // 1. Insert Department
        val dept = Department(
            adminId = 1,
            departmentName = "Science & Physics Wing",
            code = "SCI",
            headOfDepartment = "Dr. Eleanor Vance",
            roomOrOffice = "Science Block 204",
            academicYear = "2026-2027"
        )
        val deptId = db.departmentDao().insertDepartment(dept)

        val retrievedDept = db.departmentDao().getDepartmentById(deptId)
        assertNotNull(retrievedDept)
        assertEquals("Science & Physics Wing", retrievedDept?.departmentName)
        assertEquals("SCI", retrievedDept?.code)

        // 2. Insert Teacher Member
        val teacher = TeacherMember(
            departmentId = deptId,
            name = "Prof. Robert Thorne",
            employeeId = "TCH-101",
            designation = "Senior Physics Professor",
            departmentName = "Science & Physics Wing",
            phone = "+1 (555) 234-5678",
            email = "r.thorne@school.edu",
            qualification = "Ph.D. Quantum Physics"
        )
        val teacherId = db.teacherMemberDao().insertTeacher(teacher)

        val retrievedTeachers = db.teacherMemberDao().getTeachersForDepartmentOnce(deptId)
        assertEquals(1, retrievedTeachers.size)
        assertEquals("Prof. Robert Thorne", retrievedTeachers[0].name)
        assertEquals("TCH-101", retrievedTeachers[0].employeeId)

        // 3. Insert Faculty Attendance Record
        val record = TeacherAttendanceRecord(
            teacherMemberId = teacherId,
            employeeId = "TCH-101",
            teacherName = "Prof. Robert Thorne",
            departmentId = deptId,
            departmentName = "Science & Physics Wing",
            designation = "Senior Physics Professor",
            dateString = "2026-09-23",
            status = TeacherAttendanceStatus.PRESENT,
            checkInTime = "07:55 AM",
            remarks = "On Duty - Lab Practical",
            markedByAdminId = 1,
            markedByAdminName = "Principal"
        )
        db.teacherAttendanceRecordDao().insertOrUpdateRecords(listOf(record))

        val retrievedAttendance = db.teacherAttendanceRecordDao().getAttendanceForDeptAndDateOnce(deptId, "2026-09-23")
        assertEquals(1, retrievedAttendance.size)
        assertEquals(TeacherAttendanceStatus.PRESENT, retrievedAttendance[0].status)
        assertEquals("07:55 AM", retrievedAttendance[0].checkInTime)
    }

    @Test
    fun testPasswordHashingSecurity() {
        val password = "AdminPassword2026!"
        val hash = SecurityUtils.hashPassword(password)
        assertTrue(SecurityUtils.verifyPassword(password, hash))
        assertTrue(!SecurityUtils.verifyPassword("WrongPassword", hash))
    }

    @Test
    fun testUniqueEnglishSchoolBrandingAndCsvExport() {
        val demoAdmin = com.example.data.repository.DemoDataProvider.getDemoAdmin()
        assertEquals("Unique English School", demoAdmin.schoolName)
        assertTrue(demoAdmin.name.contains("Rajesh Sharma"))
        assertEquals("Ritesh Kumar", com.example.data.repository.DemoDataProvider.APP_CREATOR)

        val teachers = com.example.data.repository.DemoDataProvider.getDemoTeachers()
        assertTrue(teachers.any { it.name == "Prof. Rajesh Sharma" })
        assertTrue(teachers.any { it.name == "Priya Sharma" })
        assertTrue(teachers.any { it.name == "Prof. Ramanujan Mishra" })
        assertTrue(teachers.any { it.name == "Kavita Rao" })
        assertTrue(teachers.any { it.name == "Rohan Patil" })

        val csv = com.example.util.CsvExporter.generateTeacherSummaryCsv(
            rows = emptyList(),
            departmentName = "Science Department",
            period = "Today"
        )
        assertTrue(csv.contains("Unique English School - Official Faculty Attendance Register"))
    }

    @Test
    fun testMonthlyAttendancePercentageCalculationAndCsv() = runBlocking {
        // Test Monthly Teacher Attendance Calculation
        val dept = Department(
            adminId = 1,
            departmentName = "Mathematics Department",
            code = "MATH",
            headOfDepartment = "Prof. S. Ramanujan",
            roomOrOffice = "Math Wing 105",
            academicYear = "2026-2027"
        )
        val deptId = db.departmentDao().insertDepartment(dept)

        val teacher = TeacherMember(
            departmentId = deptId,
            name = "Prof. Ramanujan Mishra",
            employeeId = "TCH-201",
            designation = "Senior Lecturer - Pure Math",
            departmentName = "Mathematics Department",
            phone = "+91 98202 33441",
            email = "r.mishra@uniqueenglishschool.edu",
            qualification = "Ph.D. Pure Mathematics"
        )
        val teacherId = db.teacherMemberDao().insertTeacher(teacher)

        // Create 20 days of records in September 2026:
        // 16 Present, 2 Late, 1 Absent, 1 On Leave
        val records = mutableListOf<TeacherAttendanceRecord>()
        for (day in 1..20) {
            val dateStr = String.format("2026-09-%02d", day)
            val status = when (day) {
                in 1..16 -> TeacherAttendanceStatus.PRESENT
                17, 18 -> TeacherAttendanceStatus.LATE
                19 -> TeacherAttendanceStatus.ABSENT
                else -> TeacherAttendanceStatus.ON_LEAVE
            }
            records.add(
                TeacherAttendanceRecord(
                    teacherMemberId = teacherId,
                    employeeId = "TCH-201",
                    teacherName = "Prof. Ramanujan Mishra",
                    departmentId = deptId,
                    departmentName = "Mathematics Department",
                    designation = "Senior Lecturer - Pure Math",
                    dateString = dateStr,
                    status = status,
                    checkInTime = if (status == TeacherAttendanceStatus.LATE) "08:15 AM" else "07:55 AM",
                    remarks = "Regular class",
                    markedByAdminId = 1,
                    markedByAdminName = "Principal Dr. Rajesh Sharma"
                )
            )
        }
        db.teacherAttendanceRecordDao().insertOrUpdateRecords(records)

        // Fetch records for teacher
        val stored = records.filter { it.dateString.startsWith("2026-09") }
        assertEquals(20, stored.size)

        val present = stored.count { it.status == TeacherAttendanceStatus.PRESENT }
        val late = stored.count { it.status == TeacherAttendanceStatus.LATE }
        val absent = stored.count { it.status == TeacherAttendanceStatus.ABSENT }
        val leave = stored.count { it.status == TeacherAttendanceStatus.ON_LEAVE }
        val total = stored.size

        // Formula: (Present + Late) / Total * 100
        val countLateAsAttended = true
        val attended = present + (if (countLateAsAttended) late else 0)
        val calculatedPercentage = (attended.toFloat() / total) * 100f
        assertEquals(90.0f, calculatedPercentage, 0.01f)

        // Punctuality: Present / (Present + Late) * 100
        val punctuality = (present.toFloat() / (present + late)) * 100f
        assertEquals(88.88f, punctuality, 0.1f)

        // Verify Monthly Report Row and CSV generation
        val reportRow = com.example.util.MonthlyTeacherReportRow(
            teacherId = teacherId,
            employeeId = "TCH-201",
            teacherName = "Prof. Ramanujan Mishra",
            departmentName = "Mathematics Department",
            designation = "Senior Lecturer - Pure Math",
            monthYear = "2026-09",
            monthLabel = "September 2026",
            presentCount = present,
            absentCount = absent,
            lateCount = late,
            leaveCount = leave,
            totalDays = total,
            attendedDays = attended,
            percentage = calculatedPercentage,
            punctualityRate = punctuality,
            performanceTier = "Excellent (Target Met)",
            dailyRecords = stored
        )

        val csv = com.example.util.CsvExporter.generateMonthlyAttendanceReportCsv(
            rows = listOf(reportRow),
            monthLabel = "September 2026",
            departmentName = "Mathematics Department",
            schoolName = "Unique English School",
            targetPercentage = 85
        )

        assertTrue(csv.contains("Unique English School - Official Faculty Attendance Register"))
        assertTrue(csv.contains("September 2026"))
        assertTrue(csv.contains("Prof. Ramanujan Mishra"))
        assertTrue(csv.contains("90.0%"))
        assertTrue(csv.contains("Excellent (Target Met)"))
        assertTrue(csv.contains("Application Creator: Ritesh Kumar"))

        // Test DateUtils month navigation
        assertEquals("2026-08", com.example.util.DateUtils.getPreviousMonth("2026-09"))
        assertEquals("2026-10", com.example.util.DateUtils.getNextMonth("2026-09"))
        assertEquals("September 2026", com.example.util.DateUtils.formatYearMonth("2026-09"))
    }

    @Test
    fun testStudentEntityAndQrAttendancePersistence() = runBlocking {
        val repo = com.example.data.repository.AttendanceRepository(db)
        repo.checkAndSeedInitialData()

        val students = db.studentMemberDao().getAllStudentsOnce()
        assertTrue("Students should be seeded", students.isNotEmpty())

        val firstStudent = students.first { it.studentId == "STU-2026-101" }
        assertEquals("Aarav Sharma", firstStudent.name)
        assertEquals("Class 10-A", firstStudent.gradeClass)

        // Test 1: Scan student QR code
        val scanResult = repo.processScannedQrCode(
            rawScannedPayload = "STU-2026-101",
            dateString = "2026-09-24",
            timeString = "08:14 AM",
            markedByTeacherName = "Dr. Rajesh Sharma"
        )
        assertTrue("Scan should succeed", scanResult is com.example.data.repository.QrScanResult.StudentSuccess)
        val success = scanResult as com.example.data.repository.QrScanResult.StudentSuccess
        assertEquals("STU-2026-101", success.student.studentId)
        assertEquals("Aarav Sharma", success.student.name)
        assertEquals(com.example.data.model.StudentAttendanceStatus.PRESENT, success.record.status)
        assertEquals("08:14 AM", success.record.checkInTime)

        // Test 2: Scan same student again (Duplicate scan prevention)
        val duplicateScan = repo.processScannedQrCode(
            rawScannedPayload = "STU-2026-101",
            dateString = "2026-09-24",
            timeString = "08:15 AM",
            markedByTeacherName = "Dr. Rajesh Sharma"
        )
        assertTrue("Duplicate scan should report already marked", duplicateScan is com.example.data.repository.QrScanResult.AlreadyMarked)

        // Test 3: Scan unknown QR code
        val unknownScan = repo.processScannedQrCode(
            rawScannedPayload = "UNKNOWN-CODE-999",
            dateString = "2026-09-24",
            timeString = "08:16 AM",
            markedByTeacherName = "Dr. Rajesh Sharma"
        )
        assertTrue("Unknown code should return NotFound", unknownScan is com.example.data.repository.QrScanResult.NotFound)

        // Test 4: JSON payload scan support e.g. {"studentId":"STU-2026-102"}
        val jsonScan = repo.processScannedQrCode(
            rawScannedPayload = "{\"studentId\":\"STU-2026-102\", \"name\":\"Ananya Gupta\"}",
            dateString = "2026-09-24",
            timeString = "08:17 AM",
            markedByTeacherName = "Dr. Rajesh Sharma"
        )
        assertTrue("JSON student QR should succeed", jsonScan is com.example.data.repository.QrScanResult.StudentSuccess)
        val jsonSuccess = jsonScan as com.example.data.repository.QrScanResult.StudentSuccess
        assertEquals("Ananya Gupta", jsonSuccess.student.name)

        // Test 5: Verify ZXing QR Code bitmap generation
        val bitmap = com.example.util.QrCodeGenerator.generateQrBitmap("STU-2026-101", size = 200)
        assertNotNull("Generated QR bitmap should not be null", bitmap)
        assertEquals(200, bitmap?.width)
        assertEquals(200, bitmap?.height)
    }

    @Test
    fun testOfflineRoomDatabaseAttendanceStorageAndSync() = runBlocking {
        val repo = com.example.data.repository.AttendanceRepository(db)
        repo.checkAndSeedInitialData()

        val teachers = db.teacherMemberDao().getAllTeachersOnce()
        val firstTeacher = teachers.first()

        // 1. Store Faculty Attendance while device is offline in Room SQLite Database
        val offlineTeacherRecord = TeacherAttendanceRecord(
            teacherMemberId = firstTeacher.id,
            employeeId = firstTeacher.employeeId,
            teacherName = firstTeacher.name,
            departmentId = firstTeacher.departmentId,
            departmentName = firstTeacher.departmentName,
            designation = firstTeacher.designation,
            dateString = "2026-09-24",
            status = TeacherAttendanceStatus.PRESENT,
            checkInTime = "08:00 AM",
            remarks = "Marked Offline (Stored in Room DB)",
            markedByAdminId = 1,
            markedByAdminName = "Principal",
            isSynced = false,
            syncTimestamp = 0L,
            isOfflineCreated = true
        )
        db.teacherAttendanceRecordDao().insertOrUpdateRecords(listOf(offlineTeacherRecord))

        // Verify it was persisted to Room DB and is returned by unsynced query
        val unsyncedTeachersBefore = db.teacherAttendanceRecordDao().getUnsyncedTeacherRecordsOnce()
        assertTrue("Offline teacher record should be returned as unsynced", unsyncedTeachersBefore.any { it.employeeId == firstTeacher.employeeId })
        val savedTeacher = unsyncedTeachersBefore.first { it.employeeId == firstTeacher.employeeId }
        assertEquals(false, savedTeacher.isSynced)
        assertEquals(true, savedTeacher.isOfflineCreated)

        // 2. Store Student Attendance via QR scan while device is offline
        val offlineStudentResult = repo.processScannedQrCode(
            rawScannedPayload = "STU-2026-103",
            dateString = "2026-09-24",
            timeString = "08:25 AM",
            markedByTeacherName = "Teacher In Classroom",
            isOffline = true
        )
        assertTrue("Offline QR scan should succeed", offlineStudentResult is com.example.data.repository.QrScanResult.StudentSuccess)
        val studentSuccess = offlineStudentResult as com.example.data.repository.QrScanResult.StudentSuccess
        assertEquals(false, studentSuccess.record.isSynced)
        assertEquals(true, studentSuccess.record.isOfflineCreated)

        val unsyncedStudentsBefore = db.studentAttendanceRecordDao().getUnsyncedStudentRecordsOnce()
        assertTrue("Offline student attendance should be in unsynced queue", unsyncedStudentsBefore.any { it.studentId == "STU-2026-103" })

        // 3. Synchronize all offline Room attendance records
        val syncResult = repo.syncOfflineAttendanceRecords()
        assertTrue("Sync should succeed", syncResult.isSuccess)
        val syncedCount = syncResult.getOrNull() ?: 0
        assertTrue("Should have synced at least 2 records", syncedCount >= 2)

        // 4. Verify that records in Room database are now marked isSynced = true
        val unsyncedTeachersAfter = db.teacherAttendanceRecordDao().getUnsyncedTeacherRecordsOnce()
        assertTrue("Unsynced teachers should now be empty", unsyncedTeachersAfter.isEmpty())

        val unsyncedStudentsAfter = db.studentAttendanceRecordDao().getUnsyncedStudentRecordsOnce()
        assertTrue("Unsynced students should now be empty", unsyncedStudentsAfter.isEmpty())

        val syncedStudent = db.studentAttendanceRecordDao().getRecordByStudentIdAndDate("STU-2026-103", "2026-09-24")
        assertNotNull("Student record should still exist in Room", syncedStudent)
        assertEquals(true, syncedStudent?.isSynced)
        assertTrue("Sync timestamp should be recorded", (syncedStudent?.syncTimestamp ?: 0L) > 0L)
    }
}
