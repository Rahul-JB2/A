package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AdminUser
import com.example.data.model.Department
import com.example.data.model.StaffAnnouncement
import com.example.data.model.StudentAttendanceRecord
import com.example.data.model.StudentMember
import com.example.data.model.TeacherAttendanceRecord
import com.example.data.model.TeacherMember
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminUserDao {
    @Query("SELECT * FROM admin_users WHERE email = :email LIMIT 1")
    suspend fun getAdminByEmail(email: String): AdminUser?

    @Query("SELECT * FROM admin_users WHERE id = :id LIMIT 1")
    suspend fun getAdminById(id: Long): AdminUser?

    @Query("SELECT COUNT(*) FROM admin_users")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdmin(admin: AdminUser): Long

    @Update
    suspend fun updateAdmin(admin: AdminUser)
}

@Dao
interface DepartmentDao {
    @Query("SELECT * FROM departments ORDER BY departmentName ASC")
    fun getAllDepartments(): Flow<List<Department>>

    @Query("SELECT * FROM departments WHERE id = :id LIMIT 1")
    suspend fun getDepartmentById(id: Long): Department?

    @Query("SELECT COUNT(*) FROM departments")
    fun getDepartmentCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepartment(department: Department): Long

    @Update
    suspend fun updateDepartment(department: Department)

    @Delete
    suspend fun deleteDepartment(department: Department)
}

@Dao
interface TeacherMemberDao {
    @Query("SELECT * FROM faculty_teachers ORDER BY name ASC")
    fun getAllTeachers(): Flow<List<TeacherMember>>

    @Query("SELECT * FROM faculty_teachers ORDER BY name ASC")
    suspend fun getAllTeachersOnce(): List<TeacherMember>

    @Query("SELECT * FROM faculty_teachers WHERE departmentId = :deptId ORDER BY name ASC")
    fun getTeachersForDepartment(deptId: Long): Flow<List<TeacherMember>>

    @Query("SELECT * FROM faculty_teachers WHERE departmentId = :deptId ORDER BY name ASC")
    suspend fun getTeachersForDepartmentOnce(deptId: Long): List<TeacherMember>

    @Query("SELECT * FROM faculty_teachers WHERE id = :id LIMIT 1")
    suspend fun getTeacherById(id: Long): TeacherMember?

    @Query("SELECT COUNT(*) FROM faculty_teachers WHERE isActive = 1")
    fun getActiveTeacherCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: TeacherMember): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeachers(teachers: List<TeacherMember>)

    @Update
    suspend fun updateTeacher(teacher: TeacherMember)

    @Delete
    suspend fun deleteTeacher(teacher: TeacherMember)

    @Query("UPDATE faculty_teachers SET isActive = :isActive WHERE id = :id")
    suspend fun setTeacherActiveState(id: Long, isActive: Boolean)
}

@Dao
interface TeacherAttendanceRecordDao {
    @Query("SELECT * FROM teacher_attendance_records WHERE departmentId = :deptId AND dateString = :dateString")
    fun getAttendanceForDeptAndDate(deptId: Long, dateString: String): Flow<List<TeacherAttendanceRecord>>

    @Query("SELECT * FROM teacher_attendance_records WHERE departmentId = :deptId AND dateString = :dateString")
    suspend fun getAttendanceForDeptAndDateOnce(deptId: Long, dateString: String): List<TeacherAttendanceRecord>

    @Query("SELECT * FROM teacher_attendance_records WHERE dateString = :dateString")
    fun getAttendanceForDate(dateString: String): Flow<List<TeacherAttendanceRecord>>

    @Query("SELECT * FROM teacher_attendance_records WHERE dateString = :dateString")
    suspend fun getAttendanceForDateOnce(dateString: String): List<TeacherAttendanceRecord>

    @Query("SELECT * FROM teacher_attendance_records WHERE teacherMemberId = :teacherId ORDER BY dateString DESC")
    fun getAttendanceForTeacher(teacherId: Long): Flow<List<TeacherAttendanceRecord>>

    @Query("SELECT * FROM teacher_attendance_records ORDER BY dateString DESC, teacherName ASC")
    fun getAllAttendanceRecords(): Flow<List<TeacherAttendanceRecord>>

    @Query("SELECT * FROM teacher_attendance_records WHERE dateString >= :startDate AND dateString <= :endDate ORDER BY dateString DESC")
    fun getAttendanceBetweenDates(startDate: String, endDate: String): Flow<List<TeacherAttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRecords(records: List<TeacherAttendanceRecord>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRecord(record: TeacherAttendanceRecord): Long

    @Query("SELECT * FROM teacher_attendance_records WHERE isSynced = 0 ORDER BY timestamp ASC")
    fun getUnsyncedTeacherRecords(): Flow<List<TeacherAttendanceRecord>>

    @Query("SELECT * FROM teacher_attendance_records WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsyncedTeacherRecordsOnce(): List<TeacherAttendanceRecord>

    @Query("SELECT COUNT(*) FROM teacher_attendance_records WHERE isSynced = 0")
    fun getUnsyncedTeacherCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM teacher_attendance_records")
    fun getTotalTeacherRecordsCount(): Flow<Int>

    @Query("UPDATE teacher_attendance_records SET isSynced = 1, syncTimestamp = :syncTimestamp WHERE id IN (:ids)")
    suspend fun markTeacherRecordsSynced(ids: List<Long>, syncTimestamp: Long)

    @Query("DELETE FROM teacher_attendance_records WHERE departmentId = :deptId AND dateString = :dateString")
    suspend fun deleteAttendanceForDeptAndDate(deptId: Long, dateString: String)
}

@Dao
interface StaffAnnouncementDao {
    @Query("SELECT * FROM staff_announcements ORDER BY timestamp DESC")
    fun getAllAnnouncements(): Flow<List<StaffAnnouncement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: StaffAnnouncement): Long

    @Query("DELETE FROM staff_announcements WHERE id = :id")
    suspend fun deleteAnnouncement(id: Long)
}

@Dao
interface StudentMemberDao {
    @Query("SELECT * FROM students ORDER BY gradeClass ASC, rollNumber ASC")
    fun getAllStudents(): Flow<List<StudentMember>>

    @Query("SELECT * FROM students ORDER BY gradeClass ASC, rollNumber ASC")
    suspend fun getAllStudentsOnce(): List<StudentMember>

    @Query("SELECT * FROM students WHERE gradeClass = :gradeClass ORDER BY CAST(rollNumber AS INTEGER) ASC, name ASC")
    fun getStudentsByClass(gradeClass: String): Flow<List<StudentMember>>

    @Query("SELECT * FROM students WHERE gradeClass = :gradeClass ORDER BY CAST(rollNumber AS INTEGER) ASC, name ASC")
    suspend fun getStudentsByClassOnce(gradeClass: String): List<StudentMember>

    @Query("SELECT DISTINCT gradeClass FROM students ORDER BY gradeClass ASC")
    fun getAllClasses(): Flow<List<String>>

    @Query("SELECT * FROM students WHERE studentId = :studentId OR qrCodeData = :studentId LIMIT 1")
    suspend fun getStudentByCode(studentId: String): StudentMember?

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentById(id: Long): StudentMember?

    @Query("SELECT COUNT(*) FROM students WHERE isActive = 1")
    fun getActiveStudentCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentMember): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentMember>)

    @Update
    suspend fun updateStudent(student: StudentMember)

    @Delete
    suspend fun deleteStudent(student: StudentMember)
}

@Dao
interface StudentAttendanceRecordDao {
    @Query("SELECT * FROM student_attendance_records WHERE gradeClass = :gradeClass AND dateString = :dateString")
    fun getAttendanceForClassAndDate(gradeClass: String, dateString: String): Flow<List<StudentAttendanceRecord>>

    @Query("SELECT * FROM student_attendance_records WHERE gradeClass = :gradeClass AND dateString = :dateString")
    suspend fun getAttendanceForClassAndDateOnce(gradeClass: String, dateString: String): List<StudentAttendanceRecord>

    @Query("SELECT * FROM student_attendance_records WHERE dateString = :dateString")
    fun getAttendanceForDate(dateString: String): Flow<List<StudentAttendanceRecord>>

    @Query("SELECT * FROM student_attendance_records WHERE studentMemberId = :studentMemberId AND dateString = :dateString LIMIT 1")
    suspend fun getRecordForStudentAndDate(studentMemberId: Long, dateString: String): StudentAttendanceRecord?

    @Query("SELECT * FROM student_attendance_records WHERE studentId = :studentId AND dateString = :dateString LIMIT 1")
    suspend fun getRecordByStudentIdAndDate(studentId: String, dateString: String): StudentAttendanceRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRecord(record: StudentAttendanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRecords(records: List<StudentAttendanceRecord>)

    @Query("SELECT * FROM student_attendance_records WHERE isSynced = 0 ORDER BY timestamp ASC")
    fun getUnsyncedStudentRecords(): Flow<List<StudentAttendanceRecord>>

    @Query("SELECT * FROM student_attendance_records WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsyncedStudentRecordsOnce(): List<StudentAttendanceRecord>

    @Query("SELECT COUNT(*) FROM student_attendance_records WHERE isSynced = 0")
    fun getUnsyncedStudentCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM student_attendance_records")
    fun getTotalStudentRecordsCount(): Flow<Int>

    @Query("UPDATE student_attendance_records SET isSynced = 1, syncTimestamp = :syncTimestamp WHERE id IN (:ids)")
    suspend fun markStudentRecordsSynced(ids: List<Long>, syncTimestamp: Long)

    @Query("DELETE FROM student_attendance_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("DELETE FROM student_attendance_records WHERE gradeClass = :gradeClass AND dateString = :dateString")
    suspend fun deleteAttendanceForClassAndDate(gradeClass: String, dateString: String)
}
