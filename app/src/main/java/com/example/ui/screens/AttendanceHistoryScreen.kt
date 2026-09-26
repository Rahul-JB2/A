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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TeacherAttendanceRecord
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
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.AttendanceViewModel
import com.example.util.DateUtils

@Composable
fun AttendanceHistoryScreen(
    viewModel: AttendanceViewModel,
    onNavigate: (AppScreen) -> Unit
) {
    val records by viewModel.allAttendanceRecords.collectAsState()
    val departments by viewModel.departments.collectAsState()
    val dateFilter by viewModel.historyDateFilter.collectAsState()
    val deptFilter by viewModel.historyDeptFilter.collectAsState()
    val statusFilter by viewModel.historyStatusFilter.collectAsState()
    val searchQuery by viewModel.historySearchQuery.collectAsState()

    var showDatePickerDialog by remember { mutableStateOf(false) }

    // Filtered records
    val filteredRecords = records.filter { rec ->
        val matchesDate = dateFilter == null || rec.dateString == dateFilter
        val matchesDept = deptFilter == null || rec.departmentId == deptFilter
        val matchesStatus = statusFilter == null || rec.status == statusFilter
        val matchesSearch = searchQuery.isBlank() ||
                rec.teacherName.contains(searchQuery, ignoreCase = true) ||
                rec.employeeId.contains(searchQuery, ignoreCase = true) ||
                rec.departmentName.contains(searchQuery, ignoreCase = true) ||
                rec.designation.contains(searchQuery, ignoreCase = true)

        matchesDate && matchesDept && matchesStatus && matchesSearch
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("attendance_history_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search field
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setHistorySearchQuery(it) },
                placeholder = { Text("Search by Teacher Name, Employee ID...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setHistorySearchQuery("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_search_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // Date Filter
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (dateFilter != null) DateUtils.formatIsoToDisplay(dateFilter!!) else "All Recorded Dates",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    if (dateFilter != null) {
                        TextButton(onClick = { viewModel.setHistoryDateFilter(null) }) {
                            Text("Show All Dates", fontSize = 12.sp)
                        }
                    }
                    OutlinedButton(
                        onClick = { showDatePickerDialog = true },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Filter Date", fontSize = 12.sp)
                    }
                }
            }
        }

        // Department Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = deptFilter == null,
                        onClick = { viewModel.setHistoryDeptFilter(null) },
                        label = { Text("All Departments") }
                    )
                }
                items(departments) { dept ->
                    FilterChip(
                        selected = deptFilter == dept.id,
                        onClick = { viewModel.setHistoryDeptFilter(dept.id) },
                        label = { Text(dept.code.ifBlank { dept.departmentName }) }
                    )
                }
            }
        }

        // Status Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = statusFilter == null,
                        onClick = { viewModel.setHistoryStatusFilter(null) },
                        label = { Text("All Statuses") }
                    )
                }
                item {
                    FilterChip(
                        selected = statusFilter == TeacherAttendanceStatus.PRESENT,
                        onClick = { viewModel.setHistoryStatusFilter(TeacherAttendanceStatus.PRESENT) },
                        label = { Text("Present") }
                    )
                }
                item {
                    FilterChip(
                        selected = statusFilter == TeacherAttendanceStatus.LATE,
                        onClick = { viewModel.setHistoryStatusFilter(TeacherAttendanceStatus.LATE) },
                        label = { Text("Late") }
                    )
                }
                item {
                    FilterChip(
                        selected = statusFilter == TeacherAttendanceStatus.ABSENT,
                        onClick = { viewModel.setHistoryStatusFilter(TeacherAttendanceStatus.ABSENT) },
                        label = { Text("Absent") }
                    )
                }
                item {
                    FilterChip(
                        selected = statusFilter == TeacherAttendanceStatus.ON_LEAVE,
                        onClick = { viewModel.setHistoryStatusFilter(TeacherAttendanceStatus.ON_LEAVE) },
                        label = { Text("On Leave") }
                    )
                }
            }
        }

        // Count Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Attendance Records (${filteredRecords.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (filteredRecords.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = Slate600, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No attendance logs found matching these filters.", fontWeight = FontWeight.Medium)
                        Text("Try resetting filters or mark today's attendance.", style = MaterialTheme.typography.bodySmall, color = Slate600)
                    }
                }
            }
        }

        // History Log Cards
        items(filteredRecords) { record ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_record_${record.id}")
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
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (record.status) {
                                            TeacherAttendanceStatus.PRESENT -> Emerald100
                                            TeacherAttendanceStatus.ABSENT -> Rose100
                                            TeacherAttendanceStatus.LATE -> Amber100
                                            TeacherAttendanceStatus.ON_LEAVE -> Indigo100
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (record.status) {
                                        TeacherAttendanceStatus.PRESENT -> "P"
                                        TeacherAttendanceStatus.ABSENT -> "A"
                                        TeacherAttendanceStatus.LATE -> "L"
                                        TeacherAttendanceStatus.ON_LEAVE -> "OL"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    color = when (record.status) {
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
                                    text = record.teacherName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${record.employeeId} • ${record.departmentName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate600
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (record.status) {
                                TeacherAttendanceStatus.PRESENT -> Emerald100
                                TeacherAttendanceStatus.ABSENT -> Rose100
                                TeacherAttendanceStatus.LATE -> Amber100
                                TeacherAttendanceStatus.ON_LEAVE -> Indigo100
                            }
                        ) {
                            Text(
                                text = record.status.name,
                                color = when (record.status) {
                                    TeacherAttendanceStatus.PRESENT -> Emerald600
                                    TeacherAttendanceStatus.ABSENT -> Rose600
                                    TeacherAttendanceStatus.LATE -> Amber600
                                    TeacherAttendanceStatus.ON_LEAVE -> Indigo600
                                },
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = Slate600, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = DateUtils.formatIsoToShort(record.dateString),
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate600
                            )
                            if (record.checkInTime.isNotBlank()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Filled.Schedule, contentDescription = null, tint = Slate600, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(record.checkInTime, style = MaterialTheme.typography.labelSmall, color = Slate600)
                            }
                        }

                        // Edit session shortcut
                        Text(
                            text = "Edit Session",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                viewModel.onSelectDeptForAttendance(record.departmentId)
                                viewModel.onSelectDateForAttendance(record.dateString)
                                onNavigate(AppScreen.MARK_ATTENDANCE)
                            }
                        )
                    }

                    if (record.remarks.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Note: ${record.remarks}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Date Filter Dialog
    if (showDatePickerDialog) {
        PastDateDialog(
            currentDate = dateFilter ?: DateUtils.getTodayIso(),
            onDismiss = { showDatePickerDialog = false },
            onSelectDate = { d ->
                viewModel.setHistoryDateFilter(d)
                showDatePickerDialog = false
            }
        )
    }
}
