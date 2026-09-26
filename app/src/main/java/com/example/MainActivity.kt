package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AppBottomNavigation
import com.example.ui.components.AppNavigationRail
import com.example.ui.components.AppTopBar
import com.example.ui.screens.AnnouncementsScreen
import com.example.ui.screens.AttendanceHistoryScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DepartmentsScreen
import com.example.ui.screens.FutureFeaturesScreen
import com.example.ui.screens.MarkAttendanceScreen
import com.example.ui.screens.ProfileSettingsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.StudentQrAttendanceScreen
import com.example.ui.screens.TeachersScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.AttendanceViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppRoot()
            }
        }
    }
}

@Composable
fun MainAppRoot(viewModel: AttendanceViewModel = viewModel()) {
    val admin by viewModel.currentAdmin.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    if (admin == null) {
        AuthScreen(
            viewModel = viewModel,
            authError = authError,
            onLoginSuccess = {
                viewModel.navigateTo(AppScreen.DASHBOARD)
            }
        )
    } else {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isExpandedScreen = maxWidth >= 600.dp

            if (isExpandedScreen) {
                // Adaptive layout for Tablets and Chromebooks: Side Navigation Rail
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    AppNavigationRail(
                        currentScreen = currentScreen,
                        onNavigate = { viewModel.navigateTo(it) }
                    )

                    Scaffold(
                        topBar = {
                            AppTopBar(
                                currentScreen = currentScreen,
                                admin = admin,
                                onNavigate = { viewModel.navigateTo(it) },
                                onLogout = { viewModel.logout() },
                                onReloadDemo = { viewModel.reloadAllDemoData() },
                                viewModel = viewModel
                            )
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        containerColor = MaterialTheme.colorScheme.background
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            AppScreenContent(
                                currentScreen = currentScreen,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            } else {
                // Mobile layout: Top App Bar + Bottom Navigation
                Scaffold(
                    topBar = {
                        AppTopBar(
                            currentScreen = currentScreen,
                            admin = admin,
                            onNavigate = { viewModel.navigateTo(it) },
                            onLogout = { viewModel.logout() },
                            onReloadDemo = { viewModel.reloadAllDemoData() },
                            viewModel = viewModel
                        )
                    },
                    bottomBar = {
                        AppBottomNavigation(
                            currentScreen = currentScreen,
                            onNavigate = { viewModel.navigateTo(it) }
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AppScreenContent(
                            currentScreen = currentScreen,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppScreenContent(
    currentScreen: AppScreen,
    viewModel: AttendanceViewModel
) {
    when (currentScreen) {
        AppScreen.DASHBOARD -> DashboardScreen(
            viewModel = viewModel,
            onNavigate = { viewModel.navigateTo(it) }
        )
        AppScreen.MARK_ATTENDANCE -> MarkAttendanceScreen(
            viewModel = viewModel
        )
        AppScreen.STUDENT_QR_SCANNER -> StudentQrAttendanceScreen(
            viewModel = viewModel
        )
        AppScreen.TEACHERS -> TeachersScreen(
            viewModel = viewModel
        )
        AppScreen.DEPARTMENTS -> DepartmentsScreen(
            viewModel = viewModel,
            onNavigate = { viewModel.navigateTo(it) }
        )
        AppScreen.HISTORY -> AttendanceHistoryScreen(
            viewModel = viewModel,
            onNavigate = { viewModel.navigateTo(it) }
        )
        AppScreen.REPORTS -> ReportsScreen(
            viewModel = viewModel
        )
        AppScreen.ANNOUNCEMENTS -> AnnouncementsScreen(
            viewModel = viewModel
        )
        AppScreen.SETTINGS -> ProfileSettingsScreen(
            viewModel = viewModel,
            onLogout = { viewModel.logout() }
        )
        AppScreen.FUTURE_FEATURES -> FutureFeaturesScreen(
            onLaunchQrScanner = { viewModel.navigateTo(AppScreen.STUDENT_QR_SCANNER) }
        )
    }
}
