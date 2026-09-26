package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AdminUser
import com.example.data.model.Department
import com.example.data.model.StaffAnnouncement
import com.example.data.model.StudentAttendanceRecord
import com.example.data.model.StudentMember
import com.example.data.model.TeacherAttendanceRecord
import com.example.data.model.TeacherMember

@Database(
    entities = [
        AdminUser::class,
        Department::class,
        TeacherMember::class,
        TeacherAttendanceRecord::class,
        StaffAnnouncement::class,
        StudentMember::class,
        StudentAttendanceRecord::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun adminUserDao(): AdminUserDao
    abstract fun departmentDao(): DepartmentDao
    abstract fun teacherMemberDao(): TeacherMemberDao
    abstract fun teacherAttendanceRecordDao(): TeacherAttendanceRecordDao
    abstract fun staffAnnouncementDao(): StaffAnnouncementDao
    abstract fun studentMemberDao(): StudentMemberDao
    abstract fun studentAttendanceRecordDao(): StudentAttendanceRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_attendance_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
