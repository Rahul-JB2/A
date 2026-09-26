package com.example.data.repository

import com.example.data.model.AdminUser
import com.example.data.model.Department
import com.example.data.model.StaffAnnouncement
import com.example.data.model.StudentMember
import com.example.data.model.TeacherAttendanceRecord
import com.example.data.model.TeacherAttendanceStatus
import com.example.data.model.TeacherMember
import com.example.util.DateUtils
import com.example.util.SecurityUtils

object DemoDataProvider {

    const val APP_CREATOR = "Ritesh Kumar"

    fun getDemoAdmin(): AdminUser {
        return AdminUser(
            id = 1,
            name = "Principal Dr. Rajesh Sharma",
            email = "principal@uniqueenglishschool.edu",
            passwordHash = SecurityUtils.hashPassword("password123"),
            schoolName = "Unique English School",
            role = "Principal & Attendance Incharge",
            phone = "+91 98765 43210",
            countLateAsAttended = true,
            minimumAttendanceTarget = 85,
            standardCheckInTime = "08:00 AM"
        )
    }

    fun getDemoDepartments(): List<Department> {
        return listOf(
            Department(
                id = 1,
                adminId = 1,
                departmentName = "Science & Technology",
                code = "SCI",
                headOfDepartment = "Dr. C.V. Raman",
                roomOrOffice = "Science Block 201",
                academicYear = "2026-2027"
            ),
            Department(
                id = 2,
                adminId = 1,
                departmentName = "Mathematics Department",
                code = "MATH",
                headOfDepartment = "Prof. S. Ramanujan",
                roomOrOffice = "Math Wing 105",
                academicYear = "2026-2027"
            ),
            Department(
                id = 3,
                adminId = 1,
                departmentName = "Languages & Literature",
                code = "LANG",
                headOfDepartment = "Prof. Rabindranath Tagore",
                roomOrOffice = "Humanities Hall 302",
                academicYear = "2026-2027"
            ),
            Department(
                id = 4,
                adminId = 1,
                departmentName = "Social Sciences & Commerce",
                code = "SOC",
                headOfDepartment = "Dr. B.R. Ambedkar",
                roomOrOffice = "Main Building 112",
                academicYear = "2026-2027"
            ),
            Department(
                id = 5,
                adminId = 1,
                departmentName = "Arts & Physical Education",
                code = "APE",
                headOfDepartment = "Major Dhyan Chand",
                roomOrOffice = "Gymnasium Complex",
                academicYear = "2026-2027"
            )
        )
    }

    fun getDemoTeachers(): List<TeacherMember> {
        return listOf(
            // Science & Technology Wing
            TeacherMember(
                id = 1,
                departmentId = 1,
                name = "Prof. Rajesh Sharma",
                employeeId = "TCH-101",
                designation = "Senior Professor - Physics",
                departmentName = "Science & Technology",
                phone = "+91 98101 22331",
                email = "r.sharma@uniqueenglishschool.edu",
                qualification = "M.Sc., Ph.D. Applied Physics",
                gender = "Male",
                isActive = true
            ),
            TeacherMember(
                id = 2,
                departmentId = 1,
                name = "Dr. Amit Verma",
                employeeId = "TCH-102",
                designation = "Lead Chemistry Instructor",
                departmentName = "Science & Technology",
                phone = "+91 98101 22332",
                email = "a.verma@uniqueenglishschool.edu",
                qualification = "Ph.D. Organic Chemistry",
                gender = "Male",
                isActive = true
            ),
            TeacherMember(
                id = 3,
                departmentId = 1,
                name = "Priya Sharma",
                employeeId = "TCH-103",
                designation = "Computer Science & Robotics Head",
                departmentName = "Science & Technology",
                phone = "+91 98101 22333",
                email = "p.sharma@uniqueenglishschool.edu",
                qualification = "M.Tech Software Engineering",
                gender = "Female",
                isActive = true
            ),
            TeacherMember(
                id = 4,
                departmentId = 1,
                name = "Dr. Ananya Sen",
                employeeId = "TCH-104",
                designation = "Biology & Ecology Specialist",
                departmentName = "Science & Technology",
                phone = "+91 98101 22334",
                email = "a.sen@uniqueenglishschool.edu",
                qualification = "M.Sc., B.Ed. Botany",
                gender = "Female",
                isActive = true
            ),

            // Mathematics Wing
            TeacherMember(
                id = 5,
                departmentId = 2,
                name = "Prof. Ramanujan Mishra",
                employeeId = "TCH-201",
                designation = "Senior Lecturer - Pure Math",
                departmentName = "Mathematics Department",
                phone = "+91 98202 33441",
                email = "r.mishra@uniqueenglishschool.edu",
                qualification = "Ph.D. Pure Mathematics",
                gender = "Male",
                isActive = true
            ),
            TeacherMember(
                id = 6,
                departmentId = 2,
                name = "Vikramaditya Patel",
                employeeId = "TCH-202",
                designation = "Statistics & Calculus Teacher",
                departmentName = "Mathematics Department",
                phone = "+91 98202 33442",
                email = "v.patel@uniqueenglishschool.edu",
                qualification = "M.Sc. Applied Mathematics, B.Ed.",
                gender = "Male",
                isActive = true
            ),

            // Languages & Literature Wing
            TeacherMember(
                id = 7,
                departmentId = 3,
                name = "Kavita Rao",
                employeeId = "TCH-301",
                designation = "HOD English Literature",
                departmentName = "Languages & Literature",
                phone = "+91 98303 44551",
                email = "k.rao@uniqueenglishschool.edu",
                qualification = "M.A. English Literature, B.Ed.",
                gender = "Female",
                isActive = true
            ),
            TeacherMember(
                id = 8,
                departmentId = 3,
                name = "Meenakshi Sundaram",
                employeeId = "TCH-302",
                designation = "Hindi & Sanskrit Faculty",
                departmentName = "Languages & Literature",
                phone = "+91 98303 44552",
                email = "m.sundaram@uniqueenglishschool.edu",
                qualification = "M.A. Hindi, M.Phil.",
                gender = "Female",
                isActive = true
            ),

            // Social Sciences & Commerce Wing
            TeacherMember(
                id = 9,
                departmentId = 4,
                name = "Arvind Narang",
                employeeId = "TCH-401",
                designation = "History & Civics Senior Faculty",
                departmentName = "Social Sciences & Commerce",
                phone = "+91 98404 55661",
                email = "a.narang@uniqueenglishschool.edu",
                qualification = "M.A. Modern History",
                gender = "Male",
                isActive = true
            ),
            TeacherMember(
                id = 10,
                departmentId = 4,
                name = "Manoj Agarwal",
                employeeId = "TCH-402",
                designation = "Economics & Accountancy Faculty",
                departmentName = "Social Sciences & Commerce",
                phone = "+91 98404 55662",
                email = "m.agarwal@uniqueenglishschool.edu",
                qualification = "M.Com., B.Ed.",
                gender = "Male",
                isActive = true
            ),

            // Arts & Physical Education Wing
            TeacherMember(
                id = 11,
                departmentId = 5,
                name = "Rohan Patil",
                employeeId = "TCH-501",
                designation = "Physical Education & Sports Director",
                departmentName = "Arts & Physical Education",
                phone = "+91 98505 66771",
                email = "r.patil@uniqueenglishschool.edu",
                qualification = "M.P.Ed., NIS Certified Coach",
                gender = "Male",
                isActive = true
            ),
            TeacherMember(
                id = 12,
                departmentId = 5,
                name = "Shilpa Joshi",
                employeeId = "TCH-502",
                designation = "Fine Arts & Classical Music Faculty",
                departmentName = "Arts & Physical Education",
                phone = "+91 98505 66772",
                email = "s.joshi@uniqueenglishschool.edu",
                qualification = "M.F.A. Fine Arts",
                gender = "Female",
                isActive = true
            )
        )
    }

    fun getDemoAttendanceRecords(teachers: List<TeacherMember>): List<TeacherAttendanceRecord> {
        val records = mutableListOf<TeacherAttendanceRecord>()
        val adminName = "Dr. Rajesh Sharma (Principal)"
        val days = mutableListOf<String>()
        // Generate 25 recent days so monthly calculations have comprehensive stored records
        for (i in 0 until 25) {
            days.add(DateUtils.getDaysAgoIso(i))
        }

        teachers.forEach { teacher ->
            days.forEachIndexed { dayIndex, dateStr ->
                // Deterministic realistic attendance variation
                val hash = (teacher.id * 37 + dayIndex * 19).toInt() % 20
                val (status, checkIn, remark) = when {
                    hash in 0..14 -> Triple(TeacherAttendanceStatus.PRESENT, "07:5${(hash + teacher.id.toInt()) % 6} AM", "On time")
                    hash in 15..16 -> Triple(TeacherAttendanceStatus.LATE, "08:1${(hash % 9) + 1} AM", "Traffic congestion delay")
                    hash == 17 -> Triple(TeacherAttendanceStatus.ON_LEAVE, "--", "Approved Casual / Medical Leave")
                    else -> Triple(TeacherAttendanceStatus.ABSENT, "--", "Unexcused Absence")
                }

                records.add(
                    TeacherAttendanceRecord(
                        teacherMemberId = teacher.id,
                        employeeId = teacher.employeeId,
                        teacherName = teacher.name,
                        departmentId = teacher.departmentId,
                        departmentName = teacher.departmentName,
                        designation = teacher.designation,
                        dateString = dateStr,
                        status = status,
                        checkInTime = checkIn,
                        remarks = remark,
                        markedByAdminId = 1,
                        markedByAdminName = adminName
                    )
                )
            }
        }
        return records
    }

    fun getDemoAnnouncements(): List<StaffAnnouncement> {
        return listOf(
            StaffAnnouncement(
                id = 1,
                adminId = 1,
                authorName = "Principal Dr. Rajesh Sharma",
                departmentId = 0,
                targetAudience = "All Faculty",
                title = "Mandatory Faculty Meeting Tomorrow at 3:30 PM",
                message = "All department heads and teaching staff of Unique English School are requested to assemble in the Main Auditorium for the Mid-Term Evaluation Briefing.",
                priority = "Urgent",
                dateString = DateUtils.getTodayIso()
            ),
            StaffAnnouncement(
                id = 2,
                adminId = 1,
                authorName = "Ritesh Kumar (System Creator)",
                departmentId = 0,
                targetAudience = "All Faculty",
                title = "Welcome to Unique English School Faculty Portal",
                message = "Faculty attendance management application created by Ritesh Kumar for Unique English School. Features monthly percentage calculations, punctuality tracking, and official register exports.",
                priority = "Important",
                dateString = DateUtils.getTodayIso()
            ),
            StaffAnnouncement(
                id = 3,
                adminId = 1,
                authorName = "Dr. Amit Verma (Science HOD)",
                departmentId = 1,
                targetAudience = "Science & Technology",
                title = "Laboratory Equipment Safety Audit",
                message = "The Science Department labs will undergo safety inspection this Friday. Please ensure all chemicals and apparatus are labeled properly.",
                priority = "Important",
                dateString = DateUtils.getDaysAgoIso(1)
            ),
            StaffAnnouncement(
                id = 4,
                adminId = 1,
                authorName = "HR & Administration",
                departmentId = 0,
                targetAudience = "All Faculty",
                title = "Submission of Monthly Lesson Plans & Duty Registers",
                message = "Please submit verified attendance registers and curriculum lesson plans to the Principal's office by Friday evening.",
                priority = "Normal",
                dateString = DateUtils.getDaysAgoIso(2)
            )
        )
    }

    fun getDemoStudents(): List<StudentMember> {
        return listOf(
            // Class 10-A
            StudentMember(
                id = 1,
                studentId = "STU-2026-101",
                rollNumber = "1",
                name = "Aarav Sharma",
                gradeClass = "Class 10-A",
                section = "A",
                gender = "Male",
                guardianName = "Sanjay Sharma",
                guardianPhone = "+91 98765 11001",
                qrCodeData = "STU-2026-101"
            ),
            StudentMember(
                id = 2,
                studentId = "STU-2026-102",
                rollNumber = "2",
                name = "Ananya Gupta",
                gradeClass = "Class 10-A",
                section = "A",
                gender = "Female",
                guardianName = "Rajiv Gupta",
                guardianPhone = "+91 98765 11002",
                qrCodeData = "STU-2026-102"
            ),
            StudentMember(
                id = 3,
                studentId = "STU-2026-103",
                rollNumber = "3",
                name = "Rohan Verma",
                gradeClass = "Class 10-A",
                section = "A",
                gender = "Male",
                guardianName = "Sunil Verma",
                guardianPhone = "+91 98765 11003",
                qrCodeData = "STU-2026-103"
            ),
            StudentMember(
                id = 4,
                studentId = "STU-2026-104",
                rollNumber = "4",
                name = "Priya Patel",
                gradeClass = "Class 10-A",
                section = "A",
                gender = "Female",
                guardianName = "Bhavesh Patel",
                guardianPhone = "+91 98765 11004",
                qrCodeData = "STU-2026-104"
            ),
            StudentMember(
                id = 5,
                studentId = "STU-2026-105",
                rollNumber = "5",
                name = "Kabir Singh",
                gradeClass = "Class 10-A",
                section = "A",
                gender = "Male",
                guardianName = "Harpreet Singh",
                guardianPhone = "+91 98765 11005",
                qrCodeData = "STU-2026-105"
            ),
            StudentMember(
                id = 6,
                studentId = "STU-2026-106",
                rollNumber = "6",
                name = "Diya Malhotra",
                gradeClass = "Class 10-A",
                section = "A",
                gender = "Female",
                guardianName = "Vikas Malhotra",
                guardianPhone = "+91 98765 11006",
                qrCodeData = "STU-2026-106"
            ),
            StudentMember(
                id = 7,
                studentId = "STU-2026-107",
                rollNumber = "7",
                name = "Ishaan Nair",
                gradeClass = "Class 10-A",
                section = "A",
                gender = "Male",
                guardianName = "Ramesh Nair",
                guardianPhone = "+91 98765 11007",
                qrCodeData = "STU-2026-107"
            ),
            StudentMember(
                id = 8,
                studentId = "STU-2026-108",
                rollNumber = "8",
                name = "Sneha Iyer",
                gradeClass = "Class 10-A",
                section = "A",
                gender = "Female",
                guardianName = "Venkat Iyer",
                guardianPhone = "+91 98765 11008",
                qrCodeData = "STU-2026-108"
            ),

            // Class 10-B
            StudentMember(
                id = 9,
                studentId = "STU-2026-201",
                rollNumber = "1",
                name = "Aditya Mishra",
                gradeClass = "Class 10-B",
                section = "B",
                gender = "Male",
                guardianName = "Deepak Mishra",
                guardianPhone = "+91 98765 22001",
                qrCodeData = "STU-2026-201"
            ),
            StudentMember(
                id = 10,
                studentId = "STU-2026-202",
                rollNumber = "2",
                name = "Meera Joshi",
                gradeClass = "Class 10-B",
                section = "B",
                gender = "Female",
                guardianName = "Anil Joshi",
                guardianPhone = "+91 98765 22002",
                qrCodeData = "STU-2026-202"
            ),
            StudentMember(
                id = 11,
                studentId = "STU-2026-203",
                rollNumber = "3",
                name = "Aryan Choudhury",
                gradeClass = "Class 10-B",
                section = "B",
                gender = "Male",
                guardianName = "Manoj Choudhury",
                guardianPhone = "+91 98765 22003",
                qrCodeData = "STU-2026-203"
            ),
            StudentMember(
                id = 12,
                studentId = "STU-2026-204",
                rollNumber = "4",
                name = "Riya Sengupta",
                gradeClass = "Class 10-B",
                section = "B",
                gender = "Female",
                guardianName = "Pradip Sengupta",
                guardianPhone = "+91 98765 22004",
                qrCodeData = "STU-2026-204"
            ),

            // Class 12-Science
            StudentMember(
                id = 13,
                studentId = "STU-2026-301",
                rollNumber = "1",
                name = "Siddharth Rao",
                gradeClass = "Class 12-Science",
                section = "Science",
                gender = "Male",
                guardianName = "Gopal Rao",
                guardianPhone = "+91 98765 33001",
                qrCodeData = "STU-2026-301"
            ),
            StudentMember(
                id = 14,
                studentId = "STU-2026-302",
                rollNumber = "2",
                name = "Pooja Nambiar",
                gradeClass = "Class 12-Science",
                section = "Science",
                gender = "Female",
                guardianName = "Karthik Nambiar",
                guardianPhone = "+91 98765 33002",
                qrCodeData = "STU-2026-302"
            ),
            StudentMember(
                id = 15,
                studentId = "STU-2026-303",
                rollNumber = "3",
                name = "Varun Deshmukh",
                gradeClass = "Class 12-Science",
                section = "Science",
                gender = "Male",
                guardianName = "Ajay Deshmukh",
                guardianPhone = "+91 98765 33003",
                qrCodeData = "STU-2026-303"
            ),
            StudentMember(
                id = 16,
                studentId = "STU-2026-304",
                rollNumber = "4",
                name = "Shreya Bannerjee",
                gradeClass = "Class 12-Science",
                section = "Science",
                gender = "Female",
                guardianName = "Subhash Bannerjee",
                guardianPhone = "+91 98765 33004",
                qrCodeData = "STU-2026-304"
            ),

            // Class 9-A
            StudentMember(
                id = 17,
                studentId = "STU-2026-401",
                rollNumber = "1",
                name = "Arjun Reddy",
                gradeClass = "Class 9-A",
                section = "A",
                gender = "Male",
                guardianName = "Vijay Reddy",
                guardianPhone = "+91 98765 44001",
                qrCodeData = "STU-2026-401"
            ),
            StudentMember(
                id = 18,
                studentId = "STU-2026-402",
                rollNumber = "2",
                name = "Kavya Bhatt",
                gradeClass = "Class 9-A",
                section = "A",
                gender = "Female",
                guardianName = "Dinesh Bhatt",
                guardianPhone = "+91 98765 44002",
                qrCodeData = "STU-2026-402"
            )
        )
    }
}
