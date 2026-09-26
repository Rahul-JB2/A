package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.WifiOff
import com.example.ui.viewmodel.AttendanceViewModel
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdminUser
import com.example.ui.theme.Indigo600
import com.example.ui.viewmodel.AppScreen

data class NavItem(
    val screen: AppScreen,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

val mobileNavItems = listOf(
    NavItem(AppScreen.DASHBOARD, "Dashboard", Icons.Filled.Dashboard, "nav_dashboard"),
    NavItem(AppScreen.MARK_ATTENDANCE, "Faculty", Icons.Filled.HowToReg, "nav_mark_attendance"),
    NavItem(AppScreen.STUDENT_QR_SCANNER, "QR Scan", Icons.Filled.QrCodeScanner, "nav_student_qr"),
    NavItem(AppScreen.TEACHERS, "Directory", Icons.Filled.People, "nav_teachers"),
    NavItem(AppScreen.REPORTS, "Reports", Icons.Filled.Assessment, "nav_reports")
)

val railNavItems = listOf(
    NavItem(AppScreen.DASHBOARD, "Dashboard", Icons.Filled.Dashboard, "rail_dashboard"),
    NavItem(AppScreen.MARK_ATTENDANCE, "Mark", Icons.Filled.HowToReg, "rail_mark"),
    NavItem(AppScreen.STUDENT_QR_SCANNER, "QR Scan", Icons.Filled.QrCodeScanner, "rail_student_qr"),
    NavItem(AppScreen.TEACHERS, "Faculty", Icons.Filled.People, "rail_teachers"),
    NavItem(AppScreen.DEPARTMENTS, "Departments", Icons.Filled.Business, "rail_depts"),
    NavItem(AppScreen.HISTORY, "History", Icons.Filled.History, "rail_history"),
    NavItem(AppScreen.REPORTS, "Reports", Icons.Filled.Assessment, "rail_reports"),
    NavItem(AppScreen.ANNOUNCEMENTS, "Notices", Icons.Filled.Campaign, "rail_notices"),
    NavItem(AppScreen.SETTINGS, "Settings", Icons.Filled.Settings, "rail_settings"),
    NavItem(AppScreen.FUTURE_FEATURES, "Upcoming", Icons.Filled.RocketLaunch, "rail_upcoming")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    currentScreen: AppScreen,
    admin: AdminUser?,
    onNavigate: (AppScreen) -> Unit,
    onLogout: () -> Unit,
    onReloadDemo: () -> Unit,
    viewModel: AttendanceViewModel? = null
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.School,
                        contentDescription = "Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                androidx.compose.foundation.layout.Column {
                    Text(
                        text = currentScreen.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "${admin?.schoolName ?: "Unique English School"} • ${admin?.name ?: "Principal"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        },
        actions = {
            // Compact Room DB & Offline Status pill
            viewModel?.let { vm ->
                OfflineStatusBar(viewModel = vm, compact = true)
                Spacer(modifier = Modifier.width(4.dp))
            }

            IconButton(
                onClick = { onNavigate(AppScreen.STUDENT_QR_SCANNER) },
                modifier = Modifier.testTag("action_top_qr_scanner")
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCodeScanner,
                    contentDescription = "CameraX Student QR Scanner",
                    tint = Indigo600
                )
            }

            IconButton(
                onClick = { showHelpDialog = true },
                modifier = Modifier.testTag("action_help_guide")
            ) {
                Icon(Icons.AutoMirrored.Filled.Help, contentDescription = "User Guide & Help")
            }

            IconButton(
                onClick = { onNavigate(AppScreen.ANNOUNCEMENTS) },
                modifier = Modifier.testTag("action_notices")
            ) {
                Icon(Icons.Filled.Campaign, contentDescription = "Staff Notices")
            }

            IconButton(
                onClick = { onNavigate(AppScreen.HISTORY) },
                modifier = Modifier.testTag("action_history")
            ) {
                Icon(Icons.Filled.History, contentDescription = "Attendance Register")
            }

            IconButton(
                onClick = { onNavigate(AppScreen.SETTINGS) },
                modifier = Modifier.testTag("action_settings")
            ) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings")
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.testTag("action_more_menu")
                ) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More")
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    viewModel?.let { vm ->
                        DropdownMenuItem(
                            text = { Text("Toggle Offline Simulation") },
                            leadingIcon = { Icon(Icons.Filled.WifiOff, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                vm.toggleSimulateOffline()
                            },
                            modifier = Modifier.testTag("menu_toggle_offline")
                        )
                        DropdownMenuItem(
                            text = { Text("Sync Offline Records") },
                            leadingIcon = { Icon(Icons.Filled.Storage, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                vm.syncOfflineRecords()
                            },
                            modifier = Modifier.testTag("menu_sync_offline")
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Reset & Reload Demo Data") },
                        leadingIcon = { Icon(Icons.Filled.Refresh, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onReloadDemo()
                        },
                        modifier = Modifier.testTag("menu_reload_demo")
                    )
                    DropdownMenuItem(
                        text = { Text("Log Out") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onLogout()
                        },
                        modifier = Modifier.testTag("menu_logout")
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )

    if (showHelpDialog) {
        val helpScrollState = rememberScrollState()
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unique English School - User Guide", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier
                        .verticalScroll(helpScrollState)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "How to Use Faculty Attendance Easily:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("1️⃣ Mark Daily Attendance", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        "• Go to 'Attendance' tab or tap 'Mark Faculty' on Dashboard.\n" +
                        "• Pick your academic department and attendance date (defaults to Today).\n" +
                        "• Use 'All Present' to instantly mark all staff in the department.\n" +
                        "• Tap Late (L), Absent (A), or Leave (LV) for any exceptions.\n" +
                        "• Tap 'Save Faculty Attendance' at the bottom to record.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("2️⃣ Attendance Status Guide", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        "• 🟢 P (Present): In school and on time.\n" +
                        "• 🟡 L (Late): Arrived past official time (e.g. 8:00 AM). In-time is recorded.\n" +
                        "• 🔴 A (Absent): Unexcused absence.\n" +
                        "• 🟣 LV (On Leave): Approved casual, medical, or official duty leave.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("3️⃣ Faculty Directory & Departments", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        "• Faculty: View all teachers, designations, phone numbers, and qualifications. Tap any teacher to view individual history.\n" +
                        "• Depts: Organize faculty into Science, Mathematics, Languages, Social Sciences, etc., with designated HODs.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("4️⃣ Reports & Official CSV Export", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        "• View Daily, Weekly, Monthly, and All-Time attendance metrics.\n" +
                        "• Color indicators: 🟢 85%+ Target Met | 🟡 75%-84% Moderate | 🔴 <75% Low Attendance Alert.\n" +
                        "• Tap 'Export CSV' to share official spreadsheets ready for management or printing.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("5️⃣ School Settings & Rules", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        "• Edit school name, Principal profile, and check-in time.\n" +
                        "• Configure whether Late counts towards attended percentage.\n" +
                        "• Adjust minimum target attendance threshold (default 85%).",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "👨‍💻 Application Creator",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Created by Ritesh Kumar for Unique English School faculty administration.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Got It, Thanks!", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun AppBottomNavigation(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        mobileNavItems.forEach { item ->
            val isSelected = currentScreen == item.screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Indigo600,
                    selectedTextColor = Indigo600,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}

@Composable
fun AppNavigationRail(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit
) {
    NavigationRail(
        modifier = Modifier.fillMaxHeight(),
        containerColor = MaterialTheme.colorScheme.surface,
        header = {
            Box(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.School,
                    contentDescription = "Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) {
        railNavItems.forEach { item ->
            val isSelected = currentScreen == item.screen
            NavigationRailItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, fontSize = 11.sp) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = Indigo600,
                    selectedTextColor = Indigo600,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}
