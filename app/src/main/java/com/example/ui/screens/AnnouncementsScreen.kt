package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.model.Department
import com.example.data.model.StaffAnnouncement
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber600
import com.example.ui.theme.Indigo100
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Rose100
import com.example.ui.theme.Rose600
import com.example.ui.theme.Slate600
import com.example.ui.viewmodel.AttendanceViewModel
import com.example.util.DateUtils

@Composable
fun AnnouncementsScreen(
    viewModel: AttendanceViewModel
) {
    val announcements by viewModel.announcements.collectAsState()
    val departments by viewModel.departments.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("announcements_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Staff Room Circulars & Notices (${announcements.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (announcements.isEmpty()) {
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
                            Icon(Icons.Filled.Campaign, contentDescription = null, tint = Slate600, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No staff circulars posted yet.", fontWeight = FontWeight.Medium)
                            Text("Tap the + button to broadcast a notice to teachers.", style = MaterialTheme.typography.bodySmall, color = Slate600)
                        }
                    }
                }
            }

            items(announcements) { ann ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("announcement_card_${ann.id}")
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
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (ann.priority == "Urgent") Rose100
                                            else if (ann.priority == "Important") Amber100
                                            else Indigo100
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Campaign,
                                        contentDescription = null,
                                        tint = if (ann.priority == "Urgent") Rose600
                                        else if (ann.priority == "Important") Amber600
                                        else Indigo600,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (ann.priority == "Urgent") Rose100
                                    else if (ann.priority == "Important") Amber100
                                    else Indigo100
                                ) {
                                    Text(
                                        text = ann.priority,
                                        color = if (ann.priority == "Urgent") Rose600
                                        else if (ann.priority == "Important") Amber600
                                        else Indigo600,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = ann.targetAudience,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { viewModel.deleteAnnouncement(ann.id) },
                                modifier = Modifier.testTag("delete_announcement_${ann.id}")
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Slate600, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = ann.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = ann.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "By: ${ann.authorName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate600
                            )
                            Text(
                                text = DateUtils.formatIsoToShort(ann.dateString),
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate600
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("fab_add_announcement")
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Add, contentDescription = "Post Circular")
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Circular", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showCreateDialog) {
        CreateStaffNoticeDialog(
            departments = departments,
            onDismiss = { showCreateDialog = false },
            onPost = { deptId, audience, title, message, priority ->
                viewModel.addAnnouncement(deptId, audience, title, message, priority)
                showCreateDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStaffNoticeDialog(
    departments: List<Department>,
    onDismiss: () -> Unit,
    onPost: (deptId: Long, audience: String, title: String, message: String, priority: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("Important") }
    var selectedDeptId by remember { mutableLongStateOf(0L) } // 0 = All Faculty
    var deptDropdownExpanded by remember { mutableStateOf(false) }

    val selectedDept = departments.find { it.id == selectedDeptId }
    val audienceLabel = if (selectedDeptId == 0L) "All Faculty" else (selectedDept?.fullDisplayName ?: "Select Department")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Issue Staff Room Circular", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Target Audience Dropdown
                ExposedDropdownMenuBox(
                    expanded = deptDropdownExpanded,
                    onExpandedChange = { deptDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = audienceLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Target Faculty") },
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
                        DropdownMenuItem(
                            text = { Text("All Faculty (School-wide Staff Notice)") },
                            onClick = {
                                selectedDeptId = 0L
                                deptDropdownExpanded = false
                            }
                        )
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

                Spacer(modifier = Modifier.height(10.dp))

                // Priority
                Text(text = "Priority Level:", style = MaterialTheme.typography.labelSmall, color = Slate600)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Normal", "Important", "Urgent").forEach { prio ->
                        FilterChip(
                            selected = selectedPriority == prio,
                            onClick = { selectedPriority = prio },
                            label = { Text(prio) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Notice Title * (e.g. Faculty Meeting)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_announcement_title"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message Body *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_announcement_message"),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onPost(selectedDeptId, audienceLabel, title, message, selectedPriority)
                },
                enabled = title.isNotBlank() && message.isNotBlank(),
                modifier = Modifier.testTag("submit_announcement_btn")
            ) {
                Text("Broadcast Notice")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
