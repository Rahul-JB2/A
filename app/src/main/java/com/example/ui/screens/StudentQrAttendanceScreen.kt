package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SimCardDownload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentAttendanceRecord
import com.example.data.model.StudentAttendanceStatus
import com.example.data.model.StudentMember
import com.example.data.repository.QrScanResult
import com.example.ui.components.CameraXQrScannerView
import com.example.ui.components.OfflineStatusBar
import com.example.ui.theme.Amber500
import com.example.ui.theme.Amber600
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Red500
import com.example.ui.theme.Red600
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.viewmodel.AttendanceViewModel
import com.example.util.DateUtils
import com.example.util.QrCodeGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentQrAttendanceScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val scannerState by viewModel.studentQrScannerState.collectAsState()
    val classes by viewModel.allClasses.collectAsState()
    val students by viewModel.currentClassStudents.collectAsState()
    val attendanceRecords by viewModel.currentClassStudentAttendance.collectAsState()
    val admin by viewModel.currentAdmin.collectAsState()

    val availableClasses = if (classes.isEmpty()) {
        listOf("Class 10-A", "Class 10-B", "Class 12-Science", "Class 9-A")
    } else {
        classes
    }

    // Attendance Calculations for this class
    val totalStudents = students.size
    val presentCount = attendanceRecords.count { it.status == StudentAttendanceStatus.PRESENT }
    val lateCount = attendanceRecords.count { it.status == StudentAttendanceStatus.LATE }
    val absentCount = attendanceRecords.count { it.status == StudentAttendanceStatus.ABSENT }
    val attendancePct = if (totalStudents > 0) ((presentCount + lateCount).toFloat() / totalStudents * 100f) else 0f

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Live Scanner & Class Roster, 1: Student QR Badges Gallery

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header Section
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.QrCodeScanner,
                                contentDescription = null,
                                tint = Indigo600,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CameraX Student QR Scanner",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${admin?.schoolName ?: "Unique English School"} • Live Classroom Attendance",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Present Benchmark Pill
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (attendancePct >= 80f) Emerald500.copy(alpha = 0.15f) else Amber500.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (attendancePct >= 80f) Emerald600 else Amber600)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$presentCount / $totalStudents Present (${attendancePct.toInt()}%)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (attendancePct >= 80f) Emerald600 else Amber600
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Class Selector Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(availableClasses) { className ->
                        val isSelected = (className == scannerState.selectedClass)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectClassForQrScanner(className) },
                            label = { Text(className, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.School,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Indigo600,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            modifier = Modifier.testTag("chip_class_$className")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = Indigo600,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Live CameraX Scanner", fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Filled.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Student QR ID Badges", fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Filled.Badge, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }
        }

        if (selectedTabIndex == 0) {
            // Tab 0: Live CameraX Scanner and Class Attendance Roster
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                // Room Database & Offline Connectivity Status
                item {
                    OfflineStatusBar(viewModel = viewModel)
                }

                // 1. CameraX Scanner Window
                item {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(20.dp))
                        ) {
                            CameraXQrScannerView(
                                modifier = Modifier.fillMaxSize(),
                                isScanningActive = scannerState.isScanningActive,
                                onQrCodeScanned = { code ->
                                    viewModel.onQrCodeScanned(code)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Controls bar below camera: Pause/Resume, Badge Gallery shortcut
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.setScanningActive(!scannerState.isScanningActive)
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_toggle_scanning")
                            ) {
                                Icon(
                                    imageVector = if (scannerState.isScanningActive) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (scannerState.isScanningActive) "Pause Camera" else "Resume Camera",
                                    fontSize = 12.sp
                                )
                            }

                            TextButton(
                                onClick = {
                                    viewModel.openStudentBadgeDialog(null)
                                },
                                modifier = Modifier.testTag("btn_open_test_badges")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Badge,
                                    contentDescription = null,
                                    tint = Indigo600,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Test QR ID Cards", color = Indigo600, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 2. Scan Feedback Result Card (if any recent scan)
                scannerState.lastScanResult?.let { result ->
                    item {
                        ScanResultFeedbackCard(
                            result = result,
                            onDismiss = { viewModel.clearLastScanResult() }
                        )
                    }
                }

                // 3. Quick Manual ID Entry & Student Scan Simulator
                item {
                    ManualIdInputCard(
                        manualInput = scannerState.manualInputId,
                        onInputChange = { viewModel.setManualScanInput(it) },
                        onSubmit = { viewModel.submitManualScan() },
                        studentsInClass = students,
                        onSimulateScan = { student -> viewModel.simulateScanForStudent(student) }
                    )
                }

                // 4. Class Roster Attendance Status Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${scannerState.selectedClass} Student Roster",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Date: ${DateUtils.formatIsoToDisplay(scannerState.selectedDate)} • Tap student to view QR or toggle status",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Quick Mark All Present
                            IconButton(
                                onClick = { viewModel.quickMarkAllStudentsPresent(scannerState.selectedClass) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Emerald600.copy(alpha = 0.12f))
                                    .testTag("btn_quick_mark_all_present")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DoneAll,
                                    contentDescription = "Mark All Present",
                                    tint = Emerald600,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Clear Class Attendance
                            IconButton(
                                onClick = { viewModel.clearClassStudentAttendanceToday(scannerState.selectedClass) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Slate200)
                                    .testTag("btn_clear_class_attendance")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Reset Class Attendance",
                                    tint = Slate700,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // 5. Student List with live attendance indicators
                if (students.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Filled.School, contentDescription = null, tint = Slate500, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No students enrolled in ${scannerState.selectedClass}", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                } else {
                    items(students) { student ->
                        val record = attendanceRecords.firstOrNull { it.studentMemberId == student.id }
                        StudentRosterRow(
                            student = student,
                            record = record,
                            onStatusChange = { newStatus ->
                                viewModel.toggleStudentAttendanceStatus(student, newStatus)
                            },
                            onViewBadge = {
                                viewModel.openStudentBadgeDialog(student)
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        } else {
            // Tab 1: Student QR ID Badges Gallery
            StudentBadgesGalleryTab(
                students = students,
                selectedClass = scannerState.selectedClass,
                schoolName = admin?.schoolName ?: "Unique English School",
                onSimulateScan = { student ->
                    viewModel.simulateScanForStudent(student)
                    selectedTabIndex = 0
                }
            )
        }
    }

    // Modal Dialog to display full-resolution scannable student ID card
    if (scannerState.showQrBadgesDialog) {
        StudentIdBadgeModalDialog(
            student = scannerState.selectedStudentForBadge ?: students.firstOrNull(),
            schoolName = admin?.schoolName ?: "Unique English School",
            onDismiss = { viewModel.closeStudentBadgeDialog() },
            onSimulateScan = { student ->
                viewModel.simulateScanForStudent(student)
                viewModel.closeStudentBadgeDialog()
            }
        )
    }
}

@Composable
private fun ScanResultFeedbackCard(
    result: QrScanResult,
    onDismiss: () -> Unit
) {
    val bgColor: Color
    val iconColor: Color
    val title: String
    val subtitle: String

    when (result) {
        is QrScanResult.StudentSuccess -> {
            bgColor = Emerald600.copy(alpha = 0.12f)
            iconColor = Emerald600
            title = "✓ Scanned: ${result.student.name} (Roll #${result.student.rollNumber})"
            subtitle = "${result.student.gradeClass} • ID: ${result.student.studentId} • Marked Present at ${result.record.checkInTime}"
        }
        is QrScanResult.FacultySuccess -> {
            bgColor = Indigo600.copy(alpha = 0.12f)
            iconColor = Indigo600
            title = "✓ Faculty Scanned: ${result.teacher.name}"
            subtitle = "${result.teacher.departmentName} (${result.teacher.employeeId}) • Checked In at ${result.record.checkInTime}"
        }
        is QrScanResult.AlreadyMarked -> {
            bgColor = Amber500.copy(alpha = 0.15f)
            iconColor = Amber600
            title = "ℹ Already Logged: ${result.name} (${result.id})"
            subtitle = "${result.statusText} today at ${result.time}"
        }
        is QrScanResult.NotFound -> {
            bgColor = Red500.copy(alpha = 0.12f)
            iconColor = Red600
            title = "⚠ Unrecognized ID Card"
            subtitle = "Scanned: '${result.rawCode}'. Please check student ID or register student."
        }
        is QrScanResult.Error -> {
            bgColor = Red500.copy(alpha = 0.12f)
            iconColor = Red600
            title = "Scanning Error"
            subtitle = result.message
        }
    }

    ElevatedCard(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, iconColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .testTag("card_scan_feedback")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (result) {
                        is QrScanResult.StudentSuccess, is QrScanResult.FacultySuccess -> Icons.Filled.CheckCircle
                        is QrScanResult.AlreadyMarked -> Icons.Filled.Info
                        else -> Icons.Filled.Error
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Dismiss",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ManualIdInputCard(
    manualInput: String,
    onInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    studentsInClass: List<StudentMember>,
    onSimulateScan: (StudentMember) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Manual ID Log / Test",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Without Camera",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = manualInput,
                    onValueChange = onInputChange,
                    placeholder = { Text("e.g. STU-2026-101 or 1", fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Indigo600,
                        unfocusedBorderColor = Slate200
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_manual_student_id")
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onSubmit,
                    enabled = manualInput.isNotBlank(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                    modifier = Modifier.testTag("btn_submit_manual_scan")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Submit",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log", fontSize = 13.sp)
                }
            }

            // Quick Tap Simulation Chips for the class students
            if (studentsInClass.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Tap any student to test scan instantly:",
                    fontSize = 11.sp,
                    color = Slate600
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(studentsInClass.take(6)) { student ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Slate100,
                            modifier = Modifier
                                .clickable { onSimulateScan(student) }
                                .testTag("chip_simulate_${student.studentId}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.QrCode,
                                    contentDescription = null,
                                    tint = Indigo600,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${student.name.split(" ").first()} (#${student.rollNumber})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentRosterRow(
    student: StudentMember,
    record: StudentAttendanceRecord?,
    onStatusChange: (StudentAttendanceStatus) -> Unit,
    onViewBadge: () -> Unit
) {
    val isPresent = record?.status == StudentAttendanceStatus.PRESENT
    val isLate = record?.status == StudentAttendanceStatus.LATE
    val isAbsent = record?.status == StudentAttendanceStatus.ABSENT

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isPresent -> Emerald500.copy(alpha = 0.06f)
                isLate -> Amber500.copy(alpha = 0.08f)
                isAbsent -> Red500.copy(alpha = 0.06f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("row_student_${student.studentId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Roll Number circle avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isPresent -> Emerald600
                            isLate -> Amber600
                            isAbsent -> Red600
                            else -> Slate200
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.rollNumber,
                    fontWeight = FontWeight.Bold,
                    color = if (record != null) Color.White else Slate700,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Student Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = student.studentId,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (record != null && record.checkInTime.isNotBlank()) {
                        Text(
                            text = " • In: ${record.checkInTime}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isPresent) Emerald600 else Amber600
                        )
                        if (record.scanMethod == "QR_SCAN") {
                            Text(
                                text = " [QR]",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Indigo600
                            )
                        }
                        Text(
                            text = if (!record.isSynced) " • 💾 Offline (Room DB)" else " • 💾 Room DB",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (!record.isSynced) Amber600 else Slate600
                        )
                    }
                }
            }

            // Student QR Badge preview button
            IconButton(
                onClick = onViewBadge,
                modifier = Modifier
                    .size(34.dp)
                    .testTag("btn_badge_${student.studentId}")
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCode,
                    contentDescription = "View QR Badge",
                    tint = Indigo600,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Toggle Quick Attendance Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Present Toggle
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isPresent) Emerald600 else Slate100)
                        .clickable { onStatusChange(StudentAttendanceStatus.PRESENT) }
                        .testTag("btn_mark_present_${student.studentId}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "P",
                        fontWeight = FontWeight.Bold,
                        color = if (isPresent) Color.White else Slate600,
                        fontSize = 12.sp
                    )
                }

                // Late Toggle
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isLate) Amber600 else Slate100)
                        .clickable { onStatusChange(StudentAttendanceStatus.LATE) }
                        .testTag("btn_mark_late_${student.studentId}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "L",
                        fontWeight = FontWeight.Bold,
                        color = if (isLate) Color.White else Slate600,
                        fontSize = 12.sp
                    )
                }

                // Absent Toggle
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isAbsent) Red600 else Slate100)
                        .clickable { onStatusChange(StudentAttendanceStatus.ABSENT) }
                        .testTag("btn_mark_absent_${student.studentId}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "A",
                        fontWeight = FontWeight.Bold,
                        color = if (isAbsent) Color.White else Slate600,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StudentBadgesGalleryTab(
    students: List<StudentMember>,
    selectedClass: String,
    schoolName: String,
    onSimulateScan: (StudentMember) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Indigo600.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Badge, contentDescription = null, tint = Indigo600, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Classroom Printable & Digital ID Badges",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Each student card contains a high-density ZXing QR code. Point another phone or CameraX at the screen to test scanning.",
                            fontSize = 12.sp,
                            color = Slate600
                        )
                    }
                }
            }
        }

        items(students) { student ->
            StudentIdCardDisplay(
                student = student,
                schoolName = schoolName,
                onSimulateScan = { onSimulateScan(student) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StudentIdCardDisplay(
    student: StudentMember,
    schoolName: String,
    onSimulateScan: () -> Unit
) {
    val qrBitmap = remember(student.studentId) {
        QrCodeGenerator.generateQrBitmap(student.studentId, size = 300)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate200, RoundedCornerShape(16.dp))
    ) {
        Column {
            // Card Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Indigo600)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = schoolName.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "STUDENT IDENTITY CARD",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Card Body
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Student details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ID: ${student.studentId} • Roll: #${student.rollNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Indigo600,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Class: ${student.gradeClass} (Sec ${student.section})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (student.guardianName.isNotBlank()) {
                        Text(
                            text = "Parent: ${student.guardianName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onSimulateScan,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_test_scan_${student.studentId}")
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simulate Scan", fontSize = 11.sp)
                    }
                }

                // Right: Generated Scannable QR Code
                qrBitmap?.let { bmp ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .size(100.dp)
                            .border(1.dp, Slate200, RoundedCornerShape(10.dp))
                            .padding(4.dp)
                    ) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "QR Code for ${student.name}",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentIdBadgeModalDialog(
    student: StudentMember?,
    schoolName: String,
    onDismiss: () -> Unit,
    onSimulateScan: (StudentMember) -> Unit
) {
    if (student == null) return

    val qrBitmap = remember(student.studentId) {
        QrCodeGenerator.generateQrBitmap(student.studentId, size = 450)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onSimulateScan(student) },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_dialog_simulate_scan")
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log Present (Simulate Scan)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Badge, contentDescription = null, tint = Indigo600)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Student ID Badge", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    modifier = Modifier
                        .size(180.dp)
                        .padding(8.dp)
                ) {
                    qrBitmap?.let { bmp ->
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "QR Code for ${student.studentId}",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "ID: ${student.studentId} • Roll: #${student.rollNumber}",
                    fontWeight = FontWeight.SemiBold,
                    color = Indigo600,
                    fontSize = 13.sp
                )
                Text(
                    text = "${student.gradeClass} • ${schoolName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )
                if (student.guardianPhone.isNotBlank()) {
                    Text(
                        text = "Contact: ${student.guardianPhone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }
            }
        }
    )
}
