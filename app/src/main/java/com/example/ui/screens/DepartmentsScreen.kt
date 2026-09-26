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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Room
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Department
import com.example.ui.theme.Indigo100
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Slate600
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.AttendanceViewModel
import com.example.util.DateUtils

@Composable
fun DepartmentsScreen(
    viewModel: AttendanceViewModel,
    onNavigate: (AppScreen) -> Unit
) {
    val departments by viewModel.departments.collectAsState()
    val teachers by viewModel.teachers.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var deptToEdit by remember { mutableStateOf<Department?>(null) }
    var deptToDelete by remember { mutableStateOf<Department?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("departments_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Academic Departments & Wings (${departments.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Organize faculty by subject departments or school wings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )
                    }
                }
            }

            items(departments) { dept ->
                val deptTeachers = teachers.filter { it.departmentId == dept.id }

                DepartmentCard(
                    department = dept,
                    teacherCount = deptTeachers.size,
                    onMarkAttendance = {
                        viewModel.onSelectDeptForAttendance(dept.id)
                        viewModel.onSelectDateForAttendance(DateUtils.getTodayIso())
                        onNavigate(AppScreen.MARK_ATTENDANCE)
                    },
                    onViewFaculty = {
                        viewModel.setSelectedDeptFilter(dept.id)
                        onNavigate(AppScreen.TEACHERS)
                    },
                    onEdit = { deptToEdit = dept },
                    onDelete = { deptToDelete = dept }
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // FAB to add Department
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("fab_add_department")
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Add, contentDescription = "Add Department")
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Department", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Add / Edit Department Dialog
    if (showAddDialog || deptToEdit != null) {
        DepartmentFormDialog(
            deptToEdit = deptToEdit,
            onDismiss = {
                showAddDialog = false
                deptToEdit = null
            },
            onSave = { name, code, head, room, year ->
                if (deptToEdit == null) {
                    viewModel.addDepartment(name, code, head, room, year)
                } else {
                    viewModel.updateDepartment(
                        deptToEdit!!.copy(
                            departmentName = name,
                            code = code,
                            headOfDepartment = head,
                            roomOrOffice = room,
                            academicYear = year
                        )
                    )
                }
                showAddDialog = false
                deptToEdit = null
            }
        )
    }

    // Delete Confirmation
    if (deptToDelete != null) {
        AlertDialog(
            onDismissRequest = { deptToDelete = null },
            title = { Text("Delete Department") },
            text = {
                Text("Are you sure you want to remove ${deptToDelete?.fullDisplayName}? Teachers assigned to this department will not be deleted but can be reassigned.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        deptToDelete?.let { viewModel.deleteDepartment(it) }
                        deptToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deptToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DepartmentCard(
    department: Department,
    teacherCount: Int,
    onMarkAttendance: () -> Unit,
    onViewFaculty: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dept_item_${department.id}")
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
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Indigo100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Business,
                            contentDescription = null,
                            tint = Indigo600,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = department.fullDisplayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (department.headOfDepartment.isNotBlank()) {
                            Text(
                                text = "HOD: ${department.headOfDepartment}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600
                            )
                        }
                    }
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Details") },
                            leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Remove Department") },
                            leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Room, contentDescription = null, tint = Slate600, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = department.roomOrOffice.ifBlank { "Main Campus" },
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "$teacherCount Teachers",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onMarkAttendance,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.HowToReg, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mark Attendance", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onViewFaculty,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.People, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View Faculty", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun DepartmentFormDialog(
    deptToEdit: Department?,
    onDismiss: () -> Unit,
    onSave: (name: String, code: String, head: String, room: String, year: String) -> Unit
) {
    var name by remember { mutableStateOf(deptToEdit?.departmentName ?: "") }
    var code by remember { mutableStateOf(deptToEdit?.code ?: "") }
    var head by remember { mutableStateOf(deptToEdit?.headOfDepartment ?: "") }
    var room by remember { mutableStateOf(deptToEdit?.roomOrOffice ?: "") }
    var year by remember { mutableStateOf(deptToEdit?.academicYear ?: "2026-2027") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (deptToEdit == null) "Create Department" else "Edit Department",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Department Name * (e.g. Science & Tech)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_dept_name"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Code (e.g. SCI)") },
                        modifier = Modifier.weight(1f).testTag("input_dept_code"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = year,
                        onValueChange = { year = it },
                        label = { Text("Academic Year") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = head,
                    onValueChange = { head = it },
                    label = { Text("Head of Department (HOD)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_dept_hod"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Office Location / Room (e.g. Block B 201)") },
                    modifier = Modifier.fillMaxWidth().testTag("input_dept_room"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, code, head, room, year) },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("btn_save_dept")
            ) {
                Text(if (deptToEdit == null) "Create Department" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
