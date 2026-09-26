package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.IconButton
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
import com.example.ui.components.OfflineStatusBar
import com.example.ui.components.StatCard
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber600
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Indigo100
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Rose100
import com.example.ui.theme.Rose600
import com.example.ui.theme.Slate600
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.AttendanceViewModel
import com.example.ui.viewmodel.ReportPeriod
import com.example.util.DateUtils

@Composable
fun DashboardScreen(
    viewModel: AttendanceViewModel,
    onNavigate: (AppScreen) -> Unit
) {
    val admin by viewModel.currentAdmin.collectAsState()
    val metrics by viewModel.todayMetrics.collectAsState()
    val departments by viewModel.departments.collectAsState()
    val attendanceRecords by viewModel.allAttendanceRecords.collectAsState()
    val announcements by viewModel.announcements.collectAsState()

    val todayIso = DateUtils.getTodayIso()
    val todayRecords = attendanceRecords.filter { it.dateString == todayIso }
    var showHelpGuide by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Banner Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("welcome_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.padding(bottom = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.School,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = admin?.schoolName ?: "Unique English School",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            Text(
                                text = "Welcome, ${admin?.name ?: "Principal"}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Faculty Attendance Portal • ${DateUtils.formatIsoToDisplay(todayIso)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.White.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "Created by Ritesh Kumar",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.HowToReg,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Overall Attendance Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Faculty Attendance Rate (Today)",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = String.format("%.1f%%", metrics.overallAttendancePercent),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { (metrics.overallAttendancePercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                }
            }
        }

        // Room Database Integration & Offline Storage Status
        item {
            OfflineStatusBar(viewModel = viewModel)
        }

        // Easy Understanding Quick Guide Card (Expandable)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_how_to_use")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showHelpGuide = !showHelpGuide },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Indigo100,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.HelpOutline,
                                        contentDescription = null,
                                        tint = Indigo600,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "How to Use - Quick Guide",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (showHelpGuide) "Tap to collapse guide" else "Tap for easy 4-step explanation",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate600
                                )
                            }
                        }

                        IconButton(onClick = { showHelpGuide = !showHelpGuide }) {
                            Icon(
                                imageVector = if (showHelpGuide) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = if (showHelpGuide) "Collapse" else "Expand"
                            )
                        }
                    }

                    if (showHelpGuide) {
                        Spacer(modifier = Modifier.height(12.dp))
                        androidx.compose.material3.HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        // Step 1
                        Row(verticalAlignment = Alignment.Top) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("1", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Pick Department & Date", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Tap 'Mark Faculty', pick your department (e.g., Science, Math) and select today's date.", style = MaterialTheme.typography.bodySmall, color = Slate600)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Step 2
                        Row(verticalAlignment = Alignment.Top) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("2", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Set Teacher Status with 1-Tap", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Use 'All Present' for instant marking. For exceptions, tap Late (L), Absent (A), or Leave (LV).", style = MaterialTheme.typography.bodySmall, color = Slate600)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Step 3
                        Row(verticalAlignment = Alignment.Top) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("3", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Save Faculty Register", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Tap 'Save Faculty Attendance' at the bottom. Duplicates are automatically prevented.", style = MaterialTheme.typography.bodySmall, color = Slate600)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Step 4
                        Row(verticalAlignment = Alignment.Top) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("4", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Reports & Signed Registers", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Head to Staff Reports to view weekly/monthly percentages and export official CSV files.", style = MaterialTheme.typography.bodySmall, color = Slate600)
                            }
                        }
                    }
                }
            }
        }

        // Attendance Status Meanings (Quick Understanding Legend)
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status Legend (What each color means):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate600
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Emerald100,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🟢 P", fontWeight = FontWeight.Bold, color = Emerald600, fontSize = 12.sp)
                                Text("Present", color = Emerald600, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Amber100,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🟡 L", fontWeight = FontWeight.Bold, color = Amber600, fontSize = 12.sp)
                                Text("Late (Time)", color = Amber600, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Rose100,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🔴 A", fontWeight = FontWeight.Bold, color = Rose600, fontSize = 12.sp)
                                Text("Absent", color = Rose600, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Indigo100,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🟣 LV", fontWeight = FontWeight.Bold, color = Indigo600, fontSize = 12.sp)
                                Text("On Leave", color = Indigo600, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // Quick Action Buttons
        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // CameraX Student QR Attendance Hero Action Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(AppScreen.STUDENT_QR_SCANNER) }
                    .testTag("card_quick_student_qr_scanner")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Indigo600),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.QrCodeScanner,
                            contentDescription = "CameraX Scanner",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Scan Student QR Codes",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Emerald600
                            ) {
                                Text(
                                    text = "CameraX Live",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Log student classroom attendance by scanning student ID badges with camera.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Indigo600,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onNavigate(AppScreen.MARK_ATTENDANCE) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("btn_quick_mark_attendance"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
                ) {
                    Icon(Icons.Filled.HowToReg, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mark Faculty", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        viewModel.setReportPeriod(ReportPeriod.MONTHLY)
                        onNavigate(AppScreen.REPORTS)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("btn_quick_reports"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Assessment, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Monthly Reports", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onNavigate(AppScreen.TEACHERS) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.People, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Teachers", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { onNavigate(AppScreen.DEPARTMENTS) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Business, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Depts", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { onNavigate(AppScreen.HISTORY) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Register", fontSize = 12.sp)
                }
            }
        }

        // Metrics Grid (6 Metric Stat Cards)
        item {
            Text(
                text = "Today's Faculty Attendance Metrics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Row 1: Total Faculty & Departments
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Faculty",
                    value = metrics.totalTeachers.toString(),
                    subtitle = "Teaching Staff",
                    icon = Icons.Filled.People,
                    iconColor = Indigo600,
                    iconBgColor = Indigo100,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stat_total_teachers")
                )
                StatCard(
                    title = "Departments",
                    value = metrics.totalDepartments.toString(),
                    subtitle = "Academic Wings",
                    icon = Icons.Filled.Business,
                    iconColor = Indigo600,
                    iconBgColor = Indigo100,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stat_total_departments")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Present & Late
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Present Today",
                    value = metrics.presentToday.toString(),
                    subtitle = "On Campus",
                    icon = Icons.Filled.CheckCircle,
                    iconColor = Emerald600,
                    iconBgColor = Emerald100,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stat_present_today")
                )
                StatCard(
                    title = "Late Arrivals",
                    value = metrics.lateToday.toString(),
                    subtitle = "After 8:00 AM",
                    icon = Icons.Filled.Schedule,
                    iconColor = Amber600,
                    iconBgColor = Amber100,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stat_late_today")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 3: Absent & On Leave
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Absent Today",
                    value = metrics.absentToday.toString(),
                    subtitle = "Unexcused",
                    icon = Icons.Filled.PersonOff,
                    iconColor = Rose600,
                    iconBgColor = Rose100,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stat_absent_today")
                )
                StatCard(
                    title = "On Leave",
                    value = metrics.onLeaveToday.toString(),
                    subtitle = "Approved Leaves",
                    icon = Icons.Filled.EventBusy,
                    iconColor = Amber600,
                    iconBgColor = Amber100,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("stat_on_leave_today")
                )
            }
        }

        // Today's Department Marking Status
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Departments Status (Today)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${departments.count { dept -> todayRecords.any { it.departmentId == dept.id } }}/${departments.size} Marked",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        items(departments) { dept ->
            val deptTodayRecords = todayRecords.filter { it.departmentId == dept.id }
            val isMarked = deptTodayRecords.isNotEmpty()
            val presentCount = deptTodayRecords.count { it.status == com.example.data.model.TeacherAttendanceStatus.PRESENT }
            val absentCount = deptTodayRecords.count { it.status == com.example.data.model.TeacherAttendanceStatus.ABSENT }
            val lateCount = deptTodayRecords.count { it.status == com.example.data.model.TeacherAttendanceStatus.LATE }
            val leaveCount = deptTodayRecords.count { it.status == com.example.data.model.TeacherAttendanceStatus.ON_LEAVE }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dept_card_${dept.id}")
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = dept.fullDisplayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isMarked) Emerald100 else Amber100
                            ) {
                                Text(
                                    text = if (isMarked) "Marked" else "Pending",
                                    color = if (isMarked) Emerald600 else Amber600,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "HOD: ${dept.headOfDepartment} • ${dept.roomOrOffice}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )

                        if (isMarked) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "P: $presentCount | A: $absentCount | L: $lateCount | Leave: $leaveCount",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            viewModel.onSelectDeptForAttendance(dept.id)
                            viewModel.onSelectDateForAttendance(todayIso)
                            onNavigate(AppScreen.MARK_ATTENDANCE)
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isMarked) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = if (isMarked) Icons.Filled.Edit else Icons.Filled.HowToReg,
                            contentDescription = null,
                            tint = if (isMarked) MaterialTheme.colorScheme.onSecondaryContainer else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isMarked) "Edit" else "Mark",
                            color = if (isMarked) MaterialTheme.colorScheme.onSecondaryContainer else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Recent Staff Circulars
        if (announcements.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Staff Room Circulars",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigate(AppScreen.ANNOUNCEMENTS) }
                    )
                }
            }

            items(announcements.take(2)) { ann ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Campaign,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = ann.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(text = ann.message, maxLines = 1, style = MaterialTheme.typography.bodySmall, color = Slate600)
                        }
                    }
                }
            }
        }

        // App Creator Attribution Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_creator_footer")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Unique English School",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Created by Ritesh Kumar",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
