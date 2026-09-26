package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SignalWifiConnectedNoInternet4
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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

/**
 * Reusable Offline & Room Database Status Banner.
 * Clearly informs teachers whether they are working offline or online,
 * that all records are persisted in local SQLite via Room,
 * and allows instant offline simulation and synchronization.
 */
@Composable
fun OfflineStatusBar(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val isOnline by viewModel.isOnline.collectAsState()
    val isSimulatingOffline by viewModel.isSimulatingOffline.collectAsState()
    val unsyncedCount by viewModel.unsyncedRecordsCount.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val totalStored by viewModel.totalStoredRecordsCount.collectAsState()

    var showDetailsDialog by remember { mutableStateOf(false) }

    if (compact) {
        // Compact Pill format for Top Bars or dense screens
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (!isOnline) Amber100 else Emerald100,
            modifier = modifier
                .clickable { showDetailsDialog = true }
                .testTag("compact_offline_status_pill")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (!isOnline) Amber600 else Emerald600)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = if (!isOnline) Icons.Default.WifiOff else Icons.Default.Storage,
                    contentDescription = null,
                    tint = if (!isOnline) Amber600 else Emerald600,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (!isOnline) "Offline • Room DB Active" else "Online • Room DB",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (!isOnline) Amber600 else Emerald600
                )
                if (unsyncedCount > 0) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        shape = CircleShape,
                        color = Rose600,
                        modifier = Modifier.size(16.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$unsyncedCount",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Full Card Banner format for MarkAttendanceScreen and StudentQrAttendanceScreen
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (!isOnline) Amber100.copy(alpha = 0.85f) else Indigo100.copy(alpha = 0.5f)
            ),
            modifier = modifier
                .fillMaxWidth()
                .testTag("full_offline_status_card")
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (!isOnline) Amber600 else Indigo600),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (!isOnline) Icons.Default.WifiOff else Icons.Default.Storage,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (!isOnline) "Offline Mode Active" else "Room Local DB Active",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (!isOnline) Amber600 else Indigo600
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = (if (!isOnline) Amber600 else Indigo600).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (!isOnline) "SQLite Stored" else "Connected",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isOnline) Amber600 else Indigo600,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = if (!isOnline) {
                                "All attendance marks & QR scans are saved directly to local Room database on this device."
                            } else {
                                "Attendance records persist locally in Room database for full offline readiness."
                            },
                            fontSize = 11.sp,
                            color = Slate600,
                            lineHeight = 14.sp
                        )
                    }

                    // Action buttons
                    IconButton(
                        onClick = { showDetailsDialog = true },
                        modifier = Modifier.testTag("btn_offline_db_info")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Room DB Info",
                            tint = if (!isOnline) Amber600 else Indigo600
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick Stats & Unsynced indicator
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Slate600
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (unsyncedCount > 0) "$unsyncedCount records queued in Room" else "All local records in sync",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (unsyncedCount > 0) Rose600 else Slate600
                        )
                    }

                    // Interactive test toggle: Simulate Offline Mode
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.toggleSimulateOffline() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("toggle_simulate_offline")
                    ) {
                        Text(
                            text = if (isSimulatingOffline) "Simulated Offline" else "Test Offline",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSimulatingOffline) Amber600 else Slate600
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = isSimulatingOffline,
                            onCheckedChange = { viewModel.toggleSimulateOffline() },
                            modifier = Modifier.size(32.dp),
                            thumbContent = {
                                Icon(
                                    imageVector = if (isSimulatingOffline) Icons.Default.WifiOff else Icons.Default.Wifi,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    // Detailed Room Database & Offline Sync Dialog
    if (showDetailsDialog) {
        OfflineDetailsDialog(
            viewModel = viewModel,
            onDismiss = { showDetailsDialog = false }
        )
    }
}

@Composable
fun OfflineDetailsDialog(
    viewModel: AttendanceViewModel,
    onDismiss: () -> Unit
) {
    val isOnline by viewModel.isOnline.collectAsState()
    val isSimulatingOffline by viewModel.isSimulatingOffline.collectAsState()
    val unsyncedCount by viewModel.unsyncedRecordsCount.collectAsState()
    val unsyncedTeachers by viewModel.unsyncedTeacherCount.collectAsState()
    val unsyncedStudents by viewModel.unsyncedStudentCount.collectAsState()
    val totalStored by viewModel.totalStoredRecordsCount.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val lastSync by viewModel.lastSyncTimeFormatted.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Room Database & Offline Store", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Smart Teacher Attendance uses an integrated Android Room database (SQLite) so faculty can mark teacher attendance and scan student IDs even when disconnected from the school WiFi or cellular network.",
                    fontSize = 12.sp,
                    color = Slate600
                )

                // Database details card
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        DetailRow("Database File", "smart_attendance_db (SQLite)")
                        DetailRow("Persistence Layer", "AndroidX Room + KSP (v4)")
                        DetailRow("Network Status", if (!isOnline) "Offline (Local Storage Active)" else "Online (Ready)")
                        DetailRow("Simulation Mode", if (isSimulatingOffline) "Active (Simulated Offline)" else "Disabled (Real Network)")
                        DetailRow("Total Persisted Records", "$totalStored attendance records")
                        DetailRow("Pending Sync Queue", "$unsyncedCount records ($unsyncedTeachers faculty, $unsyncedStudents students)")
                        DetailRow("Last Synced", lastSync)
                    }
                }

                // Simulate Offline Mode switch
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSimulatingOffline) Amber100 else MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Simulate Offline Mode",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSimulatingOffline) Amber600 else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Toggle to test marking attendance with zero internet",
                                fontSize = 11.sp,
                                color = Slate600
                            )
                        }
                        Switch(
                            checked = isSimulatingOffline,
                            onCheckedChange = { viewModel.toggleSimulateOffline() },
                            modifier = Modifier.testTag("dialog_toggle_offline_switch")
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.syncOfflineRecords()
                },
                enabled = !isSyncing,
                modifier = Modifier.testTag("btn_dialog_sync_now")
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Syncing...")
                } else {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sync Records Now")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = Slate600, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}
