package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import com.example.ui.viewmodel.AppScreen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Department
import com.example.data.model.TeacherAttendanceStatus
import com.example.data.model.TeacherMember
import com.example.ui.components.OfflineStatusBar
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber600
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Indigo100
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Rose100
import com.example.ui.theme.Rose600
import com.example.ui.theme.Slate600
import com.example.ui.viewmodel.AttendanceViewModel
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkAttendanceScreen(
    viewModel: AttendanceViewModel
) {
    val state by viewModel.markAttendanceState.collectAsState()
    val departments by viewModel.departments.collectAsState()
    val allTeachers by viewModel.teachers.collectAsState()

    val currentDeptTeachers = allTeachers
        .filter { it.departmentId == state.selectedDepartmentId && it.isActive }
        .sortedBy { it.name }

    var deptDropdownExpanded by remember { mutableStateOf(false) }
    var showCustomDateDialog by remember { mutableStateOf(false) }
    var showSaveConfirmDialog by remember { mutableStateOf(false) }

    val selectedDept = departments.find { it.id == state.selectedDepartmentId }

    // Counts for current marked selection
    val presentCount = currentDeptTeachers.count {
        (state.attendanceMap[it.id] ?: TeacherAttendanceStatus.PRESENT) == TeacherAttendanceStatus.PRESENT
    }
    val absentCount = currentDeptTeachers.count {
        state.attendanceMap[it.id] == TeacherAttendanceStatus.ABSENT
    }
    val lateCount = currentDeptTeachers.count {
        state.attendanceMap[it.id] == TeacherAttendanceStatus.LATE
    }
    val leaveCount = currentDeptTeachers.count {
        state.attendanceMap[it.id] == TeacherAttendanceStatus.ON_LEAVE
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("mark_attendance_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Quick Banner to Switch to CameraX Student QR Attendance
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(AppScreen.STUDENT_QR_SCANNER) }
                    .testTag("banner_switch_to_student_qr")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.QrCodeScanner,
                        contentDescription = null,
                        tint = Indigo600,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Switch to Student QR Attendance",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Use CameraX to quickly scan student ID badges",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Text(
                        text = "Open Scanner →",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Indigo600
                    )
                }
            }
        }

        // Room Database & Offline Connectivity Status Card
        item {
            OfflineStatusBar(viewModel = viewModel)
        }

        // Confirmation Banner
        if (state.confirmationMessage != null) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Emerald100),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Emerald600)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = state.confirmationMessage ?: "",
                            color = Emerald600,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.clearConfirmationMessage() }) {
                            Text("OK", color = Emerald600, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Edit Mode Notice
        if (state.isAlreadySaved) {
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Amber100,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, tint = Amber600, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Editing previously recorded faculty attendance for this department and date.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Amber600,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 3-Step Easy Guide Banner
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "3 Easy Steps to Mark Attendance:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("1️⃣ Pick Dept & Date", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("➔", fontSize = 11.sp, color = Slate600)
                        Text("2️⃣ Tap Status (P/A/L/LV)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("➔", fontSize = 11.sp, color = Slate600)
                        Text("3️⃣ Tap Save", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Selection Header Card (Department & Date)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Select Department & Attendance Date",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Department Dropdown
                    ExposedDropdownMenuBox(
                        expanded = deptDropdownExpanded,
                        onExpandedChange = { deptDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedDept?.fullDisplayName ?: "Select Department",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Department / Academic Wing") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("dropdown_select_department"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = deptDropdownExpanded,
                            onDismissRequest = { deptDropdownExpanded = false }
                        ) {
                            departments.forEach { dept ->
                                DropdownMenuItem(
                                    text = { Text(dept.fullDisplayName) },
                                    onClick = {
                                        viewModel.onSelectDeptForAttendance(dept.id)
                                        deptDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Date Selector Chips
                    Text(
                        text = "Date: ${DateUtils.formatIsoToDisplay(state.selectedDate)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val today = DateUtils.getTodayIso()
                        val yesterday = DateUtils.getDaysAgoIso(1)

                        FilterChip(
                            selected = state.selectedDate == today,
                            onClick = { viewModel.onSelectDateForAttendance(today) },
                            label = { Text("Today") },
                            modifier = Modifier.testTag("chip_date_today")
                        )

                        FilterChip(
                            selected = state.selectedDate == yesterday,
                            onClick = { viewModel.onSelectDateForAttendance(yesterday) },
                            label = { Text("Yesterday") },
                            modifier = Modifier.testTag("chip_date_yesterday")
                        )

                        FilterChip(
                            selected = state.selectedDate != today && state.selectedDate != yesterday,
                            onClick = { showCustomDateDialog = true },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Past Date")
                                }
                            },
                            modifier = Modifier.testTag("chip_date_custom")
                        )
                    }
                }
            }
        }

        // Live Counters Pill Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusPill("Total", "${currentDeptTeachers.size}", Indigo100, Indigo600, Modifier.weight(1f))
                StatusPill("Present", "$presentCount", Emerald100, Emerald600, Modifier.weight(1f))
                StatusPill("Absent", "$absentCount", Rose100, Rose600, Modifier.weight(1f))
                StatusPill("Late", "$lateCount", Amber100, Amber600, Modifier.weight(1f))
                StatusPill("Leave", "$leaveCount", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            }
        }

        // Bulk Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.markAllAs(TeacherAttendanceStatus.PRESENT) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_mark_all_present"),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("All Present", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { viewModel.markAllAs(TeacherAttendanceStatus.ABSENT) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_mark_all_absent"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("All Absent", fontSize = 12.sp, color = Rose600, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { viewModel.markAllAs(TeacherAttendanceStatus.ON_LEAVE) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_mark_all_leave"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("All Leave", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Helpful Pro Tip
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Emerald100.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💡", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tip: Tap 'All Present' to quickly mark the full department, then adjust any staff who are Late, Absent, or on Leave!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Emerald600,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Section Title & Status Legend
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Faculty Members (${currentDeptTeachers.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "🟢 P  🟡 L  🔴 A  🟣 LV",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate600
                    )
                }
                Text(
                    text = "Tap any button (Present, Absent, Late, Leave) to update teacher status",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate600
                )
            }
        }

        // Teacher Attendance Cards
        if (currentDeptTeachers.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = Slate600, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No active faculty members in this department.", fontWeight = FontWeight.Medium)
                        Text("Add teachers in the Faculty section first.", style = MaterialTheme.typography.bodySmall, color = Slate600)
                    }
                }
            }
        }

        items(currentDeptTeachers) { teacher ->
            val status = state.attendanceMap[teacher.id] ?: TeacherAttendanceStatus.PRESENT
            val checkIn = state.checkInTimeMap[teacher.id] ?: ""
            val remark = state.remarksMap[teacher.id] ?: ""

            TeacherAttendanceCard(
                teacher = teacher,
                status = status,
                checkInTime = checkIn,
                remarks = remark,
                onStatusChange = { newStatus -> viewModel.setTeacherStatus(teacher.id, newStatus) },
                onCheckInChange = { newTime -> viewModel.setTeacherCheckInTime(teacher.id, newTime) },
                onRemarksChange = { newRemark -> viewModel.setTeacherRemark(teacher.id, newRemark) }
            )
        }

        // Save Button at Bottom
        item {
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { showSaveConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("btn_save_attendance"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = currentDeptTeachers.isNotEmpty() && !state.isSaving
            ) {
                Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.isSaving) "Saving Attendance..." else "Save Faculty Attendance (${currentDeptTeachers.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Save Confirmation Dialog
    if (showSaveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSaveConfirmDialog = false },
            title = { Text("Confirm Faculty Attendance Save") },
            text = {
                Column {
                    Text("Department: ${selectedDept?.fullDisplayName}")
                    Text("Date: ${DateUtils.formatIsoToDisplay(state.selectedDate)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Present: $presentCount", color = Emerald600, fontWeight = FontWeight.Bold)
                    Text("• Late: $lateCount", color = Amber600, fontWeight = FontWeight.Bold)
                    Text("• Absent: $absentCount", color = Rose600, fontWeight = FontWeight.Bold)
                    Text("• On Leave: $leaveCount", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Duplicate records are automatically prevented. Do you wish to commit these records?", style = MaterialTheme.typography.bodySmall, color = Slate600)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSaveConfirmDialog = false
                        viewModel.saveAttendance()
                    },
                    modifier = Modifier.testTag("btn_confirm_save_dialog")
                ) {
                    Text("Yes, Save Records")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Past Date Dialog
    if (showCustomDateDialog) {
        PastDateDialog(
            currentDate = state.selectedDate,
            onDismiss = { showCustomDateDialog = false },
            onSelectDate = { dateStr ->
                viewModel.onSelectDateForAttendance(dateStr)
                showCustomDateDialog = false
            }
        )
    }
}

@Composable
fun StatusPill(
    label: String,
    count: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = contentColor
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
fun TeacherAttendanceCard(
    teacher: TeacherMember,
    status: TeacherAttendanceStatus,
    checkInTime: String,
    remarks: String,
    onStatusChange: (TeacherAttendanceStatus) -> Unit,
    onCheckInChange: (String) -> Unit,
    onRemarksChange: (String) -> Unit
) {
    var expandedRemarks by remember { mutableStateOf(remarks.isNotBlank()) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("teacher_card_${teacher.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when (status) {
                                    TeacherAttendanceStatus.PRESENT -> Emerald100
                                    TeacherAttendanceStatus.ABSENT -> Rose100
                                    TeacherAttendanceStatus.LATE -> Amber100
                                    TeacherAttendanceStatus.ON_LEAVE -> Indigo100
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (status) {
                                TeacherAttendanceStatus.PRESENT -> "P"
                                TeacherAttendanceStatus.ABSENT -> "A"
                                TeacherAttendanceStatus.LATE -> "L"
                                TeacherAttendanceStatus.ON_LEAVE -> "LV"
                            },
                            fontWeight = FontWeight.Bold,
                            color = when (status) {
                                TeacherAttendanceStatus.PRESENT -> Emerald600
                                TeacherAttendanceStatus.ABSENT -> Rose600
                                TeacherAttendanceStatus.LATE -> Amber600
                                TeacherAttendanceStatus.ON_LEAVE -> Indigo600
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = teacher.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${teacher.employeeId} • ${teacher.designation}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "💾 Local Room DB",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate600,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4 Tactile Status Toggle Buttons: P, A, L, OL
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusToggleButton(
                    label = "Present",
                    shortcut = "P",
                    isSelected = status == TeacherAttendanceStatus.PRESENT,
                    activeColor = Emerald600,
                    activeBg = Emerald100,
                    onClick = { onStatusChange(TeacherAttendanceStatus.PRESENT) },
                    modifier = Modifier.weight(1f).testTag("btn_status_present_${teacher.id}")
                )

                StatusToggleButton(
                    label = "Absent",
                    shortcut = "A",
                    isSelected = status == TeacherAttendanceStatus.ABSENT,
                    activeColor = Rose600,
                    activeBg = Rose100,
                    onClick = { onStatusChange(TeacherAttendanceStatus.ABSENT) },
                    modifier = Modifier.weight(1f).testTag("btn_status_absent_${teacher.id}")
                )

                StatusToggleButton(
                    label = "Late",
                    shortcut = "L",
                    isSelected = status == TeacherAttendanceStatus.LATE,
                    activeColor = Amber600,
                    activeBg = Amber100,
                    onClick = { onStatusChange(TeacherAttendanceStatus.LATE) },
                    modifier = Modifier.weight(1f).testTag("btn_status_late_${teacher.id}")
                )

                StatusToggleButton(
                    label = "Leave",
                    shortcut = "LV",
                    isSelected = status == TeacherAttendanceStatus.ON_LEAVE,
                    activeColor = Indigo600,
                    activeBg = Indigo100,
                    onClick = { onStatusChange(TeacherAttendanceStatus.ON_LEAVE) },
                    modifier = Modifier.weight(1f).testTag("btn_status_leave_${teacher.id}")
                )
            }

            // Optional Check-In Time & Notes
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (status == TeacherAttendanceStatus.PRESENT || status == TeacherAttendanceStatus.LATE) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Schedule, contentDescription = null, tint = Slate600, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (checkInTime.isNotBlank()) "In: $checkInTime" else "In: 08:00 AM",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate600
                        )
                    }
                } else if (status == TeacherAttendanceStatus.ON_LEAVE) {
                    Text(
                        text = "Leave Type: Casual / Medical",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Text(
                    text = if (expandedRemarks) "Hide Note" else if (remarks.isNotBlank()) "Note: $remarks" else "+ Add Note",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { expandedRemarks = !expandedRemarks }
                )
            }

            if (expandedRemarks) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = remarks,
                    onValueChange = onRemarksChange,
                    placeholder = { Text("e.g. Traffic delay, Approved Sick Leave, On Duty Workshop") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_remarks_${teacher.id}"),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
fun StatusToggleButton(
    label: String,
    shortcut: String,
    isSelected: Boolean,
    activeColor: Color,
    activeBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) activeBg else Color.Transparent)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) activeColor else Slate600.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = shortcut,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isSelected) activeColor else Slate600
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) activeColor else Slate600
            )
        }
    }
}

@Composable
fun PastDateDialog(
    currentDate: String,
    onDismiss: () -> Unit,
    onSelectDate: (String) -> Unit
) {
    val quickPastDates = (0..6).map { DateUtils.getDaysAgoIso(it) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Attendance Date", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Choose from the last 7 calendar days to record or adjust teacher attendance:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )

                Spacer(modifier = Modifier.height(12.dp))

                quickPastDates.forEach { d ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (d == currentDate) Indigo100 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectDate(d) }
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = DateUtils.formatIsoToDisplay(d),
                                fontWeight = if (d == currentDate) FontWeight.Bold else FontWeight.Medium,
                                color = if (d == currentDate) Indigo600 else MaterialTheme.colorScheme.onSurface
                            )
                            if (d == currentDate) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = Indigo600, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
