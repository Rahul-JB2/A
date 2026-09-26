package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TeacherAttendanceStatus
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
import com.example.ui.viewmodel.ReportPeriod
import com.example.ui.viewmodel.ReportSortOrder
import com.example.util.CsvExporter
import com.example.util.DateUtils
import com.example.util.MonthlyTeacherReportRow
import java.util.Locale

@Composable
fun ReportsScreen(
    viewModel: AttendanceViewModel
) {
    val context = LocalContext.current
    val admin by viewModel.currentAdmin.collectAsState()
    val departments by viewModel.departments.collectAsState()
    val reportPeriod by viewModel.reportPeriod.collectAsState()
    val reportDeptFilter by viewModel.reportDeptFilter.collectAsState()

    val selectedMonth by viewModel.reportSelectedMonth.collectAsState()
    val reportSearchQuery by viewModel.reportTeacherSearchQuery.collectAsState()
    val reportSortOrder by viewModel.reportSortOrder.collectAsState()

    var showPrintPreview by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var selectedTeacherForLogDialog by remember { mutableStateOf<MonthlyTeacherReportRow?>(null) }
    var expandedTeacherId by remember { mutableStateOf<Long?>(null) }

    val selectedDeptObj = departments.find { it.id == reportDeptFilter }
    val deptDisplayName = selectedDeptObj?.fullDisplayName ?: "All Departments"
    val schoolName = admin?.schoolName ?: "Unique English School"
    val targetPct = admin?.minimumAttendanceTarget ?: 85

    // Monthly Calculation result
    val monthlyResult = viewModel.generateMonthlyAttendanceReport(selectedMonth)
    // Non-monthly fallback calculation result
    val (metrics, teacherRows) = viewModel.generateReportData()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // School & Report Header Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = schoolName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Faculty Attendance & Percentage Analytics Report",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate600
                        )
                    }
                }
            }
        }

        // Period Selection Tabs
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                TabRow(
                    selectedTabIndex = reportPeriod.ordinal,
                    containerColor = Color.Transparent,
                    modifier = Modifier.padding(4.dp)
                ) {
                    ReportPeriod.values().forEach { period ->
                        Tab(
                            selected = reportPeriod == period,
                            onClick = { viewModel.setReportPeriod(period) },
                            text = {
                                Text(
                                    period.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (reportPeriod == period) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }
            }
        }

        // Department Scope Filter
        item {
            Column {
                Text(
                    text = "Filter by Department:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate600
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = reportDeptFilter == null,
                            onClick = { viewModel.setReportDeptFilter(null) },
                            label = { Text("All Departments") }
                        )
                    }
                    items(departments) { dept ->
                        FilterChip(
                            selected = reportDeptFilter == dept.id,
                            onClick = { viewModel.setReportDeptFilter(dept.id) },
                            label = { Text(dept.code.ifBlank { dept.departmentName }) }
                        )
                    }
                }
            }
        }

        // =========================================================================
        // MONTHLY REPORT GENERATION VIEW (Calculated based on stored records)
        // =========================================================================
        if (reportPeriod == ReportPeriod.MONTHLY) {

            // 1. Month Selector & Navigation Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.selectPreviousReportMonth() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("btn_prev_month")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                                    contentDescription = "Previous Month"
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.CalendarToday,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = monthlyResult.monthLabel,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = "Monthly Register • Stored Database Records",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate600
                                )
                            }

                            IconButton(
                                onClick = { viewModel.selectNextReportMonth() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("btn_next_month")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                                    contentDescription = "Next Month"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Month Chips to jump to any month
                        Text(
                            text = "Quick Select Month:",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate600
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(monthlyResult.availableMonths) { ym ->
                                val isSelected = ym == selectedMonth
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.selectReportMonth(ym) },
                                    label = {
                                        Text(
                                            text = DateUtils.formatYearMonthShort(ym),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 2. Monthly Overall Analytics & KPI Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Monthly Faculty Attendance Rate",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$deptDisplayName • ${monthlyResult.monthLabel}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate600
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (monthlyResult.overallAttendancePercent >= targetPct) Emerald100 else if (monthlyResult.overallAttendancePercent >= 75f) Amber100 else Rose100
                            ) {
                                Text(
                                    text = String.format(Locale.US, "%.1f%%", monthlyResult.overallAttendancePercent),
                                    fontWeight = FontWeight.Bold,
                                    color = if (monthlyResult.overallAttendancePercent >= targetPct) Emerald600 else if (monthlyResult.overallAttendancePercent >= 75f) Amber600 else Rose600,
                                    style = MaterialTheme.typography.headlineSmall,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress Bar with Target Benchmark
                        LinearProgressIndicator(
                            progress = { (monthlyResult.overallAttendancePercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (monthlyResult.overallAttendancePercent >= targetPct) Emerald600 else if (monthlyResult.overallAttendancePercent >= 75f) Amber600 else Rose600
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "School Benchmark Target: $targetPct%",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate600
                            )
                            val diff = monthlyResult.overallAttendancePercent - targetPct
                            Text(
                                text = if (diff >= 0) "+${String.format(Locale.US, "%.1f", diff)}% above target" else "${String.format(Locale.US, "%.1f", diff)}% below target",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (diff >= 0) Emerald600 else Rose600
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(thickness = 0.5.dp, color = Slate600.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // 4 Analytics Tiles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${monthlyResult.totalTeachers}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text("Evaluated", style = MaterialTheme.typography.labelSmall, color = Slate600)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${monthlyResult.targetMetCount}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Emerald600
                                )
                                Text("Target Met", style = MaterialTheme.typography.labelSmall, color = Slate600)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${monthlyResult.belowTargetCount}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (monthlyResult.belowTargetCount > 0) Rose600 else Slate600
                                )
                                Text("Below 75%", style = MaterialTheme.typography.labelSmall, color = Slate600)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${String.format(Locale.US, "%.0f", monthlyResult.averagePunctualityRate)}%",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Indigo600
                                )
                                Text("Punctuality", style = MaterialTheme.typography.labelSmall, color = Slate600)
                            }
                        }
                    }
                }
            }

            // 3. Export & Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val csv = CsvExporter.generateMonthlyAttendanceReportCsv(
                                rows = monthlyResult.teacherRows,
                                monthLabel = monthlyResult.monthLabel,
                                departmentName = deptDisplayName,
                                schoolName = schoolName,
                                targetPercentage = targetPct
                            )
                            val cleanMonth = monthlyResult.monthYear.replace("-", "_")
                            CsvExporter.shareCsv(
                                context,
                                csv,
                                "UniqueEnglishSchool_Monthly_Attendance_${cleanMonth}_${deptDisplayName.replace(" ", "_")}"
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_export_monthly_csv"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Monthly CSV", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showPrintPreview = !showPrintPreview },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_toggle_print_preview"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (showPrintPreview) "Hide Sheet" else "Print Register", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 4. Printable Register Preview Table (if toggled on)
            if (showPrintPreview) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate600.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = schoolName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "OFFICIAL MONTHLY FACULTY ATTENDANCE REGISTER",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = monthlyResult.monthLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate600,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Department: $deptDisplayName • Generated On: ${DateUtils.formatIsoToShort(DateUtils.getTodayIso())}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate600
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Table Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(vertical = 4.dp, horizontal = 4.dp)
                            ) {
                                Text("ID", fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.width(44.dp))
                                Text("Faculty Name", fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.weight(1f))
                                Text("Days", fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.width(28.dp))
                                Text("P", fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.width(20.dp))
                                Text("L", fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.width(20.dp))
                                Text("A", fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.width(20.dp))
                                Text("LV", fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.width(20.dp))
                                Text("%", fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.width(36.dp))
                                Text("Rating", fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.width(55.dp))
                            }

                            monthlyResult.teacherRows.forEach { row ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(row.employeeId, fontSize = 9.sp, modifier = Modifier.width(44.dp), color = Slate600)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(row.teacherName, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, maxLines = 1)
                                        Text(row.designation, fontSize = 8.sp, color = Slate600, maxLines = 1)
                                    }
                                    Text("${row.totalDays}", fontSize = 9.sp, modifier = Modifier.width(28.dp), color = Slate600)
                                    Text("${row.presentCount}", fontSize = 9.sp, modifier = Modifier.width(20.dp), color = Emerald600, fontWeight = FontWeight.Bold)
                                    Text("${row.lateCount}", fontSize = 9.sp, modifier = Modifier.width(20.dp), color = Amber600)
                                    Text("${row.absentCount}", fontSize = 9.sp, modifier = Modifier.width(20.dp), color = Rose600)
                                    Text("${row.leaveCount}", fontSize = 9.sp, modifier = Modifier.width(20.dp), color = MaterialTheme.colorScheme.primary)
                                    Text(
                                        "${String.format(Locale.US, "%.0f", row.percentage)}%",
                                        fontSize = 9.sp,
                                        modifier = Modifier.width(36.dp),
                                        fontWeight = FontWeight.Bold,
                                        color = if (row.percentage >= targetPct) Emerald600 else if (row.percentage >= 75f) Amber600 else Rose600
                                    )
                                    Text(
                                        text = if (row.percentage >= targetPct) "Excellent" else if (row.percentage >= 75f) "Pass" else "Low",
                                        fontSize = 8.sp,
                                        modifier = Modifier.width(55.dp),
                                        color = if (row.percentage >= targetPct) Emerald600 else if (row.percentage >= 75f) Amber600 else Rose600,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                HorizontalDivider(thickness = 0.5.dp, color = Slate600.copy(alpha = 0.15f))
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text("Department Head: _____________", style = MaterialTheme.typography.labelSmall, color = Slate600)
                                Text("Principal Seal: _____________", style = MaterialTheme.typography.labelSmall, color = Slate600)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "System Created by Ritesh Kumar • Unique English School",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = Slate600,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }

            // 5. Easy Calculation Guide Banner
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Monthly Attendance % Calculation Formula",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Monthly % = (Present Days + Late Days) ÷ Total Recorded Days in Month × 100",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Emerald100,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("≥ $targetPct% Target", fontWeight = FontWeight.Bold, color = Emerald600, fontSize = 11.sp)
                                    Text("Excellent", color = Emerald600, fontSize = 9.sp)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Amber100,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("75% - 84%", fontWeight = FontWeight.Bold, color = Amber600, fontSize = 11.sp)
                                    Text("Satisfactory", color = Amber600, fontSize = 9.sp)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Rose100,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("< 75% Low", fontWeight = FontWeight.Bold, color = Rose600, fontSize = 11.sp)
                                    Text("Needs Attention", color = Rose600, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 6. Search Bar & Sort Dropdown
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = reportSearchQuery,
                        onValueChange = { viewModel.setReportTeacherSearchQuery(it) },
                        placeholder = { Text("Search teacher or ID...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (reportSearchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.setReportTeacherSearchQuery("") }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_search_monthly_teacher"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Box {
                        OutlinedButton(
                            onClick = { showSortMenu = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Sort, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sort", fontSize = 12.sp)
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            ReportSortOrder.values().forEach { order ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            order.label,
                                            fontWeight = if (reportSortOrder == order) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        viewModel.setReportSortOrder(order)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 7. Teacher Count & Sort Indicator
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Monthly Performance (${monthlyResult.teacherRows.size} Faculty)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Sorted by: ${reportSortOrder.label}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate600
                    )
                }
            }

            // 8. Individual Teacher Monthly Cards
            if (monthlyResult.teacherRows.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = Slate600,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No faculty records found for ${monthlyResult.monthLabel}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try selecting another month above or refresh the demo database.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.reloadAllDemoData() },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Refresh Demo Records")
                            }
                        }
                    }
                }
            } else {
                itemsIndexed(monthlyResult.teacherRows) { index, row ->
                    val isExpanded = expandedTeacherId == row.teacherId
                    val isMet = row.percentage >= targetPct
                    val isSatisfactory = row.percentage >= 75f && !isMet

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedTeacherId = if (isExpanded) null else row.teacherId }
                            .testTag("card_teacher_monthly_${row.employeeId}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Row 1: Rank, Teacher Info, and Large % Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Rank Circle
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "#${index + 1}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column {
                                        Text(
                                            text = row.teacherName,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = "${row.employeeId} • ${row.departmentName}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate600
                                        )
                                        Text(
                                            text = row.designation,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Slate600
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Percentage & Tier Badge
                                Column(horizontalAlignment = Alignment.End) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isMet) Emerald100 else if (isSatisfactory) Amber100 else Rose100
                                    ) {
                                        Text(
                                            text = String.format(Locale.US, "%.1f%%", row.percentage),
                                            fontWeight = FontWeight.Bold,
                                            color = if (isMet) Emerald600 else if (isSatisfactory) Amber600 else Rose600,
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isMet) "🟢 Target Met" else if (isSatisfactory) "🟡 Satisfactory" else "🔴 Low",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp,
                                        color = if (isMet) Emerald600 else if (isSatisfactory) Amber600 else Rose600
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Progress Bar relative to 100%
                            LinearProgressIndicator(
                                progress = { (row.percentage / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (isMet) Emerald600 else if (isSatisfactory) Amber600 else Rose600
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Breakdown Chips: P, L, A, LV, Working Days
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Emerald100.copy(alpha = 0.7f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("P: ${row.presentCount}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Emerald600)
                                        Text("Present", fontSize = 8.sp, color = Emerald600)
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Amber100.copy(alpha = 0.7f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("L: ${row.lateCount}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Amber600)
                                        Text("Late", fontSize = 8.sp, color = Amber600)
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Rose100.copy(alpha = 0.7f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("A: ${row.absentCount}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Rose600)
                                        Text("Absent", fontSize = 8.sp, color = Rose600)
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Indigo100.copy(alpha = 0.7f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("LV: ${row.leaveCount}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Indigo600)
                                        Text("Leave", fontSize = 8.sp, color = Indigo600)
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    Column(modifier = Modifier.padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("${row.totalDays} Days", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate600)
                                        Text("Marked", fontSize = 8.sp, color = Slate600)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Day-by-Day log toggle and details button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Punctuality: ${String.format(Locale.US, "%.0f", row.punctualityRate)}% on-time",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate600
                                )

                                TextButton(
                                    onClick = { selectedTeacherForLogDialog = row },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Full Month Log (${row.dailyRecords.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Expandable inline quick log preview
                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                    HorizontalDivider(thickness = 0.5.dp, color = Slate600.copy(alpha = 0.2f))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Stored Daily Records (${row.monthLabel}):",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))

                                    if (row.dailyRecords.isEmpty()) {
                                        Text("No individual daily records stored for this month.", style = MaterialTheme.typography.bodySmall, color = Slate600)
                                    } else {
                                        row.dailyRecords.take(5).forEach { rec ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 2.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = DateUtils.formatIsoToDayMonth(rec.dateString),
                                                    fontSize = 11.sp,
                                                    color = Slate600,
                                                    modifier = Modifier.width(100.dp)
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = when (rec.status) {
                                                        TeacherAttendanceStatus.PRESENT -> Emerald100
                                                        TeacherAttendanceStatus.LATE -> Amber100
                                                        TeacherAttendanceStatus.ABSENT -> Rose100
                                                        TeacherAttendanceStatus.ON_LEAVE -> Indigo100
                                                    }
                                                ) {
                                                    Text(
                                                        text = rec.status.name,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = when (rec.status) {
                                                            TeacherAttendanceStatus.PRESENT -> Emerald600
                                                            TeacherAttendanceStatus.LATE -> Amber600
                                                            TeacherAttendanceStatus.ABSENT -> Rose600
                                                            TeacherAttendanceStatus.ON_LEAVE -> Indigo600
                                                        },
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Text(
                                                    text = if (rec.checkInTime.isNotBlank()) rec.checkInTime else "--",
                                                    fontSize = 11.sp,
                                                    color = Slate600
                                                )
                                            }
                                        }

                                        if (row.dailyRecords.size > 5) {
                                            Text(
                                                text = "+ ${row.dailyRecords.size - 5} more dates... (Tap 'Full Month Log' to view all)",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } else {
            // =========================================================================
            // NON-MONTHLY REPORTS (Daily, Weekly, All Time)
            // =========================================================================

            // Non-monthly Summary Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Faculty Attendance Summary",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$deptDisplayName • ${reportPeriod.label}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate600
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (metrics.overallAttendancePercent >= targetPct) Emerald100 else Amber100
                            ) {
                                Text(
                                    text = String.format(Locale.US, "%.1f%%", metrics.overallAttendancePercent),
                                    fontWeight = FontWeight.Bold,
                                    color = if (metrics.overallAttendancePercent >= targetPct) Emerald600 else Amber600,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { (metrics.overallAttendancePercent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (metrics.overallAttendancePercent >= targetPct) Emerald600 else Amber600
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${metrics.totalTeachers}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(text = "Faculty", style = MaterialTheme.typography.labelSmall, color = Slate600)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${metrics.presentToday}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Emerald600)
                                Text(text = "Present", style = MaterialTheme.typography.labelSmall, color = Slate600)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${metrics.lateToday}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Amber600)
                                Text(text = "Late", style = MaterialTheme.typography.labelSmall, color = Slate600)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${metrics.absentToday}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Rose600)
                                Text(text = "Absent", style = MaterialTheme.typography.labelSmall, color = Slate600)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${metrics.onLeaveToday}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                Text(text = "Leave", style = MaterialTheme.typography.labelSmall, color = Slate600)
                            }
                        }
                    }
                }
            }

            // Export Actions: CSV & Print Register
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val csv = CsvExporter.generateTeacherSummaryCsv(
                                rows = teacherRows,
                                departmentName = deptDisplayName,
                                period = reportPeriod.label,
                                schoolName = schoolName
                            )
                            CsvExporter.shareCsv(context, csv, "Faculty_Attendance_${deptDisplayName.replace(" ", "_")}")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_export_csv"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export CSV", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showPrintPreview = !showPrintPreview },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_print_report"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (showPrintPreview) "Hide Sheet" else "Print Register", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Faculty Roster Performance List
            item {
                Text(
                    text = "Faculty Attendance Records (${teacherRows.size} Teachers)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(teacherRows) { row ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = row.teacherName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = "${row.employeeId} • ${row.departmentName} • ${row.designation}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Present: ${row.presentCount} | Late: ${row.lateCount} | Absent: ${row.absentCount} | Leave: ${row.leaveCount}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate600
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (row.percentage >= targetPct) Emerald100 else if (row.percentage >= 75f) Amber100 else Rose100
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.1f%%", row.percentage),
                                fontWeight = FontWeight.Bold,
                                color = if (row.percentage >= targetPct) Emerald600 else if (row.percentage >= 75f) Amber600 else Rose600,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // =========================================================================
    // MODAL DIALOG: Full Month Attendance Log for Specific Teacher
    // =========================================================================
    selectedTeacherForLogDialog?.let { teacherRow ->
        AlertDialog(
            onDismissRequest = { selectedTeacherForLogDialog = null },
            title = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = teacherRow.teacherName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${teacherRow.employeeId} • ${teacherRow.departmentName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate600
                            )
                        }
                    }
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Month: ${teacherRow.monthLabel}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        text = "Rate: ${String.format(Locale.US, "%.1f%%", teacherRow.percentage)}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (teacherRow.percentage >= targetPct) Emerald600 else if (teacherRow.percentage >= 75f) Amber600 else Rose600,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Marked: ${teacherRow.totalDays} days | P: ${teacherRow.presentCount}, L: ${teacherRow.lateCount}, A: ${teacherRow.absentCount}, LV: ${teacherRow.leaveCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate600
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Stored Records in Month (${teacherRow.dailyRecords.size}):",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (teacherRow.dailyRecords.isEmpty()) {
                        item {
                            Text(
                                text = "No daily attendance entries stored for this teacher in ${teacherRow.monthLabel}.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600
                            )
                        }
                    } else {
                        items(teacherRow.dailyRecords) { rec ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Slate600.copy(alpha = 0.15f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = DateUtils.formatIsoToDayMonth(rec.dateString),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                        if (rec.remarks.isNotBlank()) {
                                            Text(
                                                text = rec.remarks,
                                                fontSize = 9.sp,
                                                color = Slate600,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (rec.checkInTime.isNotBlank()) {
                                            Text(
                                                text = rec.checkInTime,
                                                fontSize = 10.sp,
                                                color = Slate600,
                                                modifier = Modifier.padding(end = 6.dp)
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = when (rec.status) {
                                                TeacherAttendanceStatus.PRESENT -> Emerald100
                                                TeacherAttendanceStatus.LATE -> Amber100
                                                TeacherAttendanceStatus.ABSENT -> Rose100
                                                TeacherAttendanceStatus.ON_LEAVE -> Indigo100
                                            }
                                        ) {
                                            Text(
                                                text = rec.status.name,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (rec.status) {
                                                    TeacherAttendanceStatus.PRESENT -> Emerald600
                                                    TeacherAttendanceStatus.LATE -> Amber600
                                                    TeacherAttendanceStatus.ABSENT -> Rose600
                                                    TeacherAttendanceStatus.ON_LEAVE -> Indigo600
                                                },
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedTeacherForLogDialog = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Close")
                }
            }
        )
    }
}
