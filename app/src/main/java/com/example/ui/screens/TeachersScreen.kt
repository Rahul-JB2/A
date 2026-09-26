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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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

@Composable
fun TeachersScreen(
    viewModel: AttendanceViewModel
) {
    val teachers by viewModel.teachers.collectAsState()
    val departments by viewModel.departments.collectAsState()
    val searchQuery by viewModel.teacherSearchQuery.collectAsState()
    val selectedDeptFilter by viewModel.selectedDeptFilter.collectAsState()
    val activeFilter by viewModel.statusActiveFilter.collectAsState()
    val selectedTeacherForDetail by viewModel.selectedTeacherForDetail.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var teacherToEdit by remember { mutableStateOf<TeacherMember?>(null) }

    // Filter teachers
    val filteredTeachers = teachers.filter { t ->
        val matchesQuery = searchQuery.isBlank() ||
                t.name.contains(searchQuery, ignoreCase = true) ||
                t.employeeId.contains(searchQuery, ignoreCase = true) ||
                t.designation.contains(searchQuery, ignoreCase = true) ||
                t.departmentName.contains(searchQuery, ignoreCase = true)

        val matchesDept = selectedDeptFilter == null || t.departmentId == selectedDeptFilter
        val matchesActive = activeFilter == null || t.isActive == activeFilter

        matchesQuery && matchesDept && matchesActive
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("teachers_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Input
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setTeacherSearchQuery(it) },
                    placeholder = { Text("Search by Teacher Name, Employee ID, Designation...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setTeacherSearchQuery("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_teachers_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Department Filters Horizontal Scroll
            item {
                Column {
                    Text(text = "Filter by Department:", style = MaterialTheme.typography.labelSmall, color = Slate600)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedDeptFilter == null,
                                onClick = { viewModel.setSelectedDeptFilter(null) },
                                label = { Text("All Departments (${teachers.size})") }
                            )
                        }
                        items(departments) { dept ->
                            val count = teachers.count { it.departmentId == dept.id }
                            FilterChip(
                                selected = selectedDeptFilter == dept.id,
                                onClick = { viewModel.setSelectedDeptFilter(dept.id) },
                                label = { Text("${dept.code.ifBlank { dept.departmentName }} ($count)") }
                            )
                        }
                    }
                }
            }

            // Status Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Status:", style = MaterialTheme.typography.labelSmall, color = Slate600)

                    FilterChip(
                        selected = activeFilter == null,
                        onClick = { viewModel.setStatusActiveFilter(null) },
                        label = { Text("All") }
                    )
                    FilterChip(
                        selected = activeFilter == true,
                        onClick = { viewModel.setStatusActiveFilter(true) },
                        label = { Text("Active Duty") }
                    )
                    FilterChip(
                        selected = activeFilter == false,
                        onClick = { viewModel.setStatusActiveFilter(false) },
                        label = { Text("On Leave / Inactive") }
                    )
                }
            }

            // Result Count Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Faculty Members (${filteredTeachers.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (filteredTeachers.isEmpty()) {
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
                            Icon(Icons.Filled.Person, contentDescription = null, tint = Slate600, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No teachers found matching your filter criteria.", fontWeight = FontWeight.Medium)
                            Text("Try clearing filters or add a new faculty member.", style = MaterialTheme.typography.bodySmall, color = Slate600)
                        }
                    }
                }
            }

            // Teacher Cards
            items(filteredTeachers) { teacher ->
                val attendanceRate = viewModel.calculateTeacherAttendanceRate(teacher.id)

                TeacherRosterCard(
                    teacher = teacher,
                    attendanceRate = attendanceRate,
                    onViewProfile = { viewModel.selectTeacherForDetail(teacher) },
                    onEdit = { teacherToEdit = teacher },
                    onToggleActive = { viewModel.toggleTeacherActive(teacher) },
                    onDelete = { viewModel.deleteTeacher(teacher) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // FAB to Add Teacher
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("fab_add_teacher")
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Add, contentDescription = "Add Faculty")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Teacher", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Add Teacher Dialog
    if (showAddDialog) {
        TeacherFormDialog(
            departments = departments,
            teacherToEdit = null,
            onDismiss = { showAddDialog = false },
            onSave = { deptId, name, empId, designation, deptName, phone, email, qualification, gender ->
                viewModel.addTeacher(deptId, name, empId, designation, deptName, phone, email, qualification, gender)
                showAddDialog = false
            }
        )
    }

    // Edit Teacher Dialog
    if (teacherToEdit != null) {
        TeacherFormDialog(
            departments = departments,
            teacherToEdit = teacherToEdit,
            onDismiss = { teacherToEdit = null },
            onSave = { deptId, name, empId, designation, deptName, phone, email, qualification, gender ->
                teacherToEdit?.let { existing ->
                    viewModel.updateTeacher(
                        existing.copy(
                            departmentId = deptId,
                            name = name,
                            employeeId = empId,
                            designation = designation,
                            departmentName = deptName,
                            phone = phone,
                            email = email,
                            qualification = qualification,
                            gender = gender
                        )
                    )
                }
                teacherToEdit = null
            }
        )
    }

    // Individual Teacher Attendance Profile Modal
    if (selectedTeacherForDetail != null) {
        TeacherAttendanceProfileDialog(
            teacher = selectedTeacherForDetail!!,
            viewModel = viewModel,
            onDismiss = { viewModel.selectTeacherForDetail(null) }
        )
    }
}

@Composable
fun TeacherRosterCard(
    teacher: TeacherMember,
    attendanceRate: Float,
    onViewProfile: () -> Unit,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("teacher_item_${teacher.id}")
            .clickable { onViewProfile() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Indigo100),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = teacher.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                            fontWeight = FontWeight.Bold,
                            color = Indigo600,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = teacher.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (!teacher.isActive) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Rose100
                                ) {
                                    Text(
                                        text = "On Leave",
                                        color = Rose600,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "${teacher.employeeId} • ${teacher.designation}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )
                    }
                }

                // Attendance Rate Badge & Menu
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (attendanceRate >= 85f) Emerald100 else if (attendanceRate >= 75f) Amber100 else Rose100
                    ) {
                        Text(
                            text = String.format("%.1f%%", attendanceRate),
                            fontWeight = FontWeight.Bold,
                            color = if (attendanceRate >= 85f) Emerald600 else if (attendanceRate >= 75f) Amber600 else Rose600,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Options")
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("View Attendance History") },
                                leadingIcon = { Icon(Icons.Filled.Visibility, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onViewProfile()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit Teacher Info") },
                                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (teacher.isActive) "Mark On Leave / Inactive" else "Reactivate Teacher") },
                                onClick = {
                                    menuExpanded = false
                                    onToggleActive()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Remove from Faculty") },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metadata Row: Department, Qualification, Phone
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = teacher.departmentName,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (teacher.phone.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Phone, contentDescription = null, tint = Slate600, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(teacher.phone, style = MaterialTheme.typography.labelSmall, color = Slate600)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherFormDialog(
    departments: List<Department>,
    teacherToEdit: TeacherMember?,
    onDismiss: () -> Unit,
    onSave: (
        departmentId: Long,
        name: String,
        employeeId: String,
        designation: String,
        departmentName: String,
        phone: String,
        email: String,
        qualification: String,
        gender: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(teacherToEdit?.name ?: "") }
    var employeeId by remember { mutableStateOf(teacherToEdit?.employeeId ?: "TCH-") }
    var designation by remember { mutableStateOf(teacherToEdit?.designation ?: "Senior Lecturer") }
    var phone by remember { mutableStateOf(teacherToEdit?.phone ?: "") }
    var email by remember { mutableStateOf(teacherToEdit?.email ?: "") }
    var qualification by remember { mutableStateOf(teacherToEdit?.qualification ?: "M.Sc., B.Ed.") }
    var gender by remember { mutableStateOf(teacherToEdit?.gender ?: "Female") }

    var selectedDeptId by remember {
        mutableLongStateOf(teacherToEdit?.departmentId ?: departments.firstOrNull()?.id ?: 1L)
    }
    var deptDropdownExpanded by remember { mutableStateOf(false) }

    val selectedDept = departments.find { it.id == selectedDeptId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (teacherToEdit == null) "Add Faculty Teacher" else "Edit Teacher Profile",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Department Dropdown
                ExposedDropdownMenuBox(
                    expanded = deptDropdownExpanded,
                    onExpandedChange = { deptDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedDept?.fullDisplayName ?: "Select Department",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Department *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deptDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = deptDropdownExpanded,
                        onDismissRequest = { deptDropdownExpanded = false }
                    ) {
                        departments.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(d.fullDisplayName) },
                                onClick = {
                                    selectedDeptId = d.id
                                    deptDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name * (e.g. Dr. Robert Miller)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_teacher_name"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = employeeId,
                        onValueChange = { employeeId = it },
                        label = { Text("Employee ID *") },
                        modifier = Modifier.weight(1f).testTag("input_teacher_empid"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = designation,
                        onValueChange = { designation = it },
                        label = { Text("Designation *") },
                        modifier = Modifier.weight(1f).testTag("input_teacher_designation"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = qualification,
                        onValueChange = { qualification = it },
                        label = { Text("Qualification") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        selectedDeptId,
                        name,
                        employeeId,
                        designation,
                        selectedDept?.departmentName ?: "Department",
                        phone,
                        email,
                        qualification,
                        gender
                    )
                },
                enabled = name.isNotBlank() && employeeId.isNotBlank() && designation.isNotBlank(),
                modifier = Modifier.testTag("submit_teacher_form_btn")
            ) {
                Text(if (teacherToEdit == null) "Add to Faculty" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TeacherAttendanceProfileDialog(
    teacher: TeacherMember,
    viewModel: AttendanceViewModel,
    onDismiss: () -> Unit
) {
    val allRecords by viewModel.allAttendanceRecords.collectAsState()
    val teacherRecords = allRecords.filter { it.teacherMemberId == teacher.id }.sortedByDescending { it.dateString }
    val (present, absent, late, leave) = viewModel.getTeacherAttendanceBreakdown(teacher.id)
    val attendanceRate = viewModel.calculateTeacherAttendanceRate(teacher.id)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = teacher.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "${teacher.employeeId} • ${teacher.designation} • ${teacher.departmentName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Attendance Summary Banner
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Lifetime Attendance Rate:", fontWeight = FontWeight.SemiBold)
                            Text(
                                text = String.format("%.1f%%", attendanceRate),
                                fontWeight = FontWeight.Bold,
                                color = if (attendanceRate >= 85f) Emerald600 else if (attendanceRate >= 75f) Amber600 else Rose600,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { (attendanceRate / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = if (attendanceRate >= 85f) Emerald600 else if (attendanceRate >= 75f) Amber600 else Rose600
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Present: $present", color = Emerald600, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Late: $late", color = Amber600, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Absent: $absent", color = Rose600, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Leave: $leave", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Attendance History Log (${teacherRecords.size} sessions):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (teacherRecords.isEmpty()) {
                        item {
                            Text("No attendance records logged for this teacher yet.", style = MaterialTheme.typography.bodySmall, color = Slate600)
                        }
                    }

                    items(teacherRecords) { rec ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = DateUtils.formatIsoToDisplay(rec.dateString),
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    if (rec.remarks.isNotBlank()) {
                                        Text(text = "Note: ${rec.remarks}", style = MaterialTheme.typography.labelSmall, color = Slate600)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (rec.status) {
                                        TeacherAttendanceStatus.PRESENT -> Emerald100
                                        TeacherAttendanceStatus.ABSENT -> Rose100
                                        TeacherAttendanceStatus.LATE -> Amber100
                                        TeacherAttendanceStatus.ON_LEAVE -> Indigo100
                                    }
                                ) {
                                    Text(
                                        text = rec.status.name,
                                        color = when (rec.status) {
                                            TeacherAttendanceStatus.PRESENT -> Emerald600
                                            TeacherAttendanceStatus.ABSENT -> Rose600
                                            TeacherAttendanceStatus.LATE -> Amber600
                                            TeacherAttendanceStatus.ON_LEAVE -> Indigo600
                                        },
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
