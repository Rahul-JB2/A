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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

data class PlannedModule(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val status: String,
    val technicalReadiness: String,
    val isLive: Boolean = false
)

@Composable
fun FutureFeaturesScreen(
    onLaunchQrScanner: () -> Unit = {}
) {
    val modules = listOf(
        PlannedModule(
            title = "QR-Code Student Attendance",
            description = "CameraX powered live QR code scanner allowing teachers to quickly log student attendance by scanning student ID cards with real-time audio/haptic feedback and classroom roster syncing.",
            icon = Icons.Filled.QrCodeScanner,
            status = "LIVE ACTIVE MODULE",
            technicalReadiness = "CameraX + ML Kit Barcode Analyzer + Room Student Attendance Database",
            isLive = true
        ),
        PlannedModule(
            title = "GPS Geofenced Campus Verification",
            description = "Prevent off-campus attendance proxy marking by validating student device geolocation within school boundaries (50m radius).",
            icon = Icons.Filled.LocationOn,
            status = "Architecture Prepared",
            technicalReadiness = "Uses Android Location Services; zero disruption to core attendance logic"
        ),
        PlannedModule(
            title = "Class Timetable & Period Scheduler",
            description = "Multi-period weekly schedule with subject rooms, bell timings, and automated daily class attendance reminders.",
            icon = Icons.Filled.Schedule,
            status = "Schema Ready",
            technicalReadiness = "Directly binds to ClassBatch entity and academic year definitions"
        ),
        PlannedModule(
            title = "Homework & Assignment Tracker",
            description = "Assign homework per class, set submission due dates, and track student submission status with parent alerts.",
            icon = Icons.Filled.Assignment,
            status = "Draft Planned",
            technicalReadiness = "Extends Announcement & Class relationship models"
        ),
        PlannedModule(
            title = "Student Marks & Gradebook",
            description = "Grade recording for quizzes, midterms, and finals with automatic grade point averages and comprehensive progress cards.",
            icon = Icons.Filled.Grade,
            status = "Draft Planned",
            technicalReadiness = "Integrates with existing Student roster and report generation engines"
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("future_features_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Next-Gen Extensibility Hub",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Smart Teacher Attendance has been constructed with an expandable, modular Clean Architecture database ready to accommodate advanced features in future releases.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(modules) { module ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Indigo100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = module.icon,
                            contentDescription = null,
                            tint = Indigo600,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = module.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Emerald100
                            ) {
                                Text(
                                    text = module.status,
                                    color = Emerald600,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = module.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Technical Blueprint: ${module.technicalReadiness}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (module.isLive) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = onLaunchQrScanner,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                                modifier = Modifier.testTag("btn_launch_live_qr_scanner")
                            ) {
                                Icon(Icons.Filled.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open CameraX QR Scanner", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
