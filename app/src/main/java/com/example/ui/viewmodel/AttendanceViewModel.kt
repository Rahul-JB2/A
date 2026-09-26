package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AdminUser
import com.example.data.model.Department
import com.example.data.model.StaffAnnouncement
import com.example.data.model.StudentAttendanceRecord
import com.example.data.model.StudentAttendanceStatus
import com.example.data.model.StudentMember
import com.example.data.model.TeacherAttendanceRecord
import com.example.data.model.TeacherAttendanceStatus
import com.example.data.model.TeacherMember
import com.example.data.repository.AttendanceRepository
import com.example.data.repository.QrScanResult
import com.example.util.DateUtils
import com.example.util.NetworkConnectivityObserver
import com.example.util.TeacherReportRow
import com.example.util.MonthlyTeacherReportRow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen(val title: String) {
    DASHBOARD("Faculty Dashboard"),
    MARK_ATTENDANCE("Mark Faculty Attendance"),
    STUDENT_QR_SCANNER("Student QR Scanner"),
    TEACHERS("Teachers Directory"),
    DEPARTMENTS("Departments & Wings"),
    HISTORY("Attendance History"),
    REPORTS("Staff Reports & Analytics"),
    ANNOUNCEMENTS("Staff Room Notices"),
    SETTINGS("Admin & Attendance Rules"),
    FUTURE_FEATURES("Upcoming Modules")
}

enum class ReportPeriod(val label: String) {
    DAILY("Today"),
    WEEKLY("This Week (7 Days)"),
    MONTHLY("Monthly Attendance Register"),
    ALL_TIME("All Time")
}

enum class ReportSortOrder(val label: String) {
    PERCENTAGE_DESC("Highest Attendance %"),
    PERCENTAGE_ASC("Lowest Attendance %"),
    NAME_ASC("Teacher Name (A-Z)"),
    ABSENT_DESC("Most Absences"),
    LATE_DESC("Most Late")
}

data class MonthlyReportResult(
    val monthYear: String,
    val monthLabel: String,
    val teacherRows: List<MonthlyTeacherReportRow>,
    val totalTeachers: Int,
    val totalMarkedDays: Int,
    val overallAttendancePercent: Float,
    val averagePunctualityRate: Float,
    val targetMetCount: Int,
    val satisfactoryCount: Int,
    val belowTargetCount: Int,
    val totalPresentCount: Int,
    val totalLateCount: Int,
    val totalAbsentCount: Int,
    val totalLeaveCount: Int,
    val topDepartmentName: String,
    val availableMonths: List<String>
)

data class DashboardMetrics(
    val totalTeachers: Int = 0,
    val totalDepartments: Int = 0,
    val presentToday: Int = 0,
    val absentToday: Int = 0,
    val lateToday: Int = 0,
    val onLeaveToday: Int = 0,
    val totalMarkedToday: Int = 0,
    val overallAttendancePercent: Float = 0f
)

data class MarkAttendanceUiState(
    val selectedDepartmentId: Long = 1,
    val selectedDate: String = DateUtils.getTodayIso(),
    val attendanceMap: Map<Long, TeacherAttendanceStatus> = emptyMap(),
    val checkInTimeMap: Map<Long, String> = emptyMap(),
    val remarksMap: Map<Long, String> = emptyMap(),
    val isAlreadySaved: Boolean = false,
    val isSaving: Boolean = false,
    val confirmationMessage: String? = null
)

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    val repository = AttendanceRepository(db)
    val networkObserver = NetworkConnectivityObserver(application)

    // Online / Offline state flows
    val isOnline: StateFlow<Boolean> = networkObserver.isOnline
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val isSimulatingOffline: StateFlow<Boolean> = networkObserver.isSimulatingOffline

    val unsyncedRecordsCount: StateFlow<Int> = repository.totalUnsyncedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val unsyncedTeacherCount: StateFlow<Int> = repository.unsyncedTeacherCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val unsyncedStudentCount: StateFlow<Int> = repository.unsyncedStudentCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalStoredRecordsCount: StateFlow<Int> = repository.totalStoredRecordsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTimeFormatted = MutableStateFlow("All up-to-date")
    val lastSyncTimeFormatted: StateFlow<String> = _lastSyncTimeFormatted.asStateFlow()

    // Current Admin / Principal
    private val _currentAdmin = MutableStateFlow<AdminUser?>(null)
    val currentAdmin: StateFlow<AdminUser?> = _currentAdmin.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // Navigation State
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Toast / Snackbars
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Data streams
    val departments: StateFlow<List<Department>> = repository.allDepartments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teachers: StateFlow<List<TeacherMember>> = repository.allTeachers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val announcements: StateFlow<List<StaffAnnouncement>> = repository.allAnnouncements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttendanceRecords: StateFlow<List<TeacherAttendanceRecord>> = repository.allAttendanceRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudents: StateFlow<List<StudentMember>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allClasses: StateFlow<List<String>> = repository.allClasses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Student QR Scanner State
    private val _studentQrScannerState = MutableStateFlow(StudentQrScannerUiState())
    val studentQrScannerState: StateFlow<StudentQrScannerUiState> = _studentQrScannerState.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentClassStudents: StateFlow<List<StudentMember>> = _studentQrScannerState
        .flatMapLatest { state ->
            repository.getStudentsByClass(state.selectedClass)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentClassStudentAttendance: StateFlow<List<StudentAttendanceRecord>> = _studentQrScannerState
        .flatMapLatest { state ->
            repository.getStudentAttendanceForClassAndDate(state.selectedClass, state.selectedDate)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Mark Attendance Form State
    private val _markAttendanceState = MutableStateFlow(MarkAttendanceUiState())
    val markAttendanceState: StateFlow<MarkAttendanceUiState> = _markAttendanceState.asStateFlow()

    // Teachers List Filtering
    private val _teacherSearchQuery = MutableStateFlow("")
    val teacherSearchQuery: StateFlow<String> = _teacherSearchQuery.asStateFlow()

    private val _selectedDeptFilter = MutableStateFlow<Long?>(null) // null = all
    val selectedDeptFilter: StateFlow<Long?> = _selectedDeptFilter.asStateFlow()

    private val _statusActiveFilter = MutableStateFlow<Boolean?>(null) // null = all
    val statusActiveFilter: StateFlow<Boolean?> = _statusActiveFilter.asStateFlow()

    // Attendance History Filtering
    private val _historyDateFilter = MutableStateFlow<String?>(null) // null = all
    val historyDateFilter: StateFlow<String?> = _historyDateFilter.asStateFlow()

    private val _historyDeptFilter = MutableStateFlow<Long?>(null)
    val historyDeptFilter: StateFlow<Long?> = _historyDeptFilter.asStateFlow()

    private val _historyStatusFilter = MutableStateFlow<TeacherAttendanceStatus?>(null)
    val historyStatusFilter: StateFlow<TeacherAttendanceStatus?> = _historyStatusFilter.asStateFlow()

    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    // Reports State
    private val _reportPeriod = MutableStateFlow(ReportPeriod.MONTHLY)
    val reportPeriod: StateFlow<ReportPeriod> = _reportPeriod.asStateFlow()

    private val _reportDeptFilter = MutableStateFlow<Long?>(null)
    val reportDeptFilter: StateFlow<Long?> = _reportDeptFilter.asStateFlow()

    private val _reportSelectedMonth = MutableStateFlow(DateUtils.getCurrentYearMonth())
    val reportSelectedMonth: StateFlow<String> = _reportSelectedMonth.asStateFlow()

    private val _reportTeacherSearchQuery = MutableStateFlow("")
    val reportTeacherSearchQuery: StateFlow<String> = _reportTeacherSearchQuery.asStateFlow()

    private val _reportSortOrder = MutableStateFlow(ReportSortOrder.PERCENTAGE_DESC)
    val reportSortOrder: StateFlow<ReportSortOrder> = _reportSortOrder.asStateFlow()

    // Selected teacher for individual attendance detail modal
    private val _selectedTeacherForDetail = MutableStateFlow<TeacherMember?>(null)
    val selectedTeacherForDetail: StateFlow<TeacherMember?> = _selectedTeacherForDetail.asStateFlow()

    init {
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
            // Auto login with default demo Principal
            val admin = repository.getAdminById(1)
            _currentAdmin.value = admin
            // Load initial attendance state
            loadAttendanceForSelection(1, DateUtils.getTodayIso())
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    // Admin Auth
    fun login(email: String, pass: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _authError.value = null
            val result = repository.login(email, pass)
            result.onSuccess { admin ->
                _currentAdmin.value = admin
                _userMessage.emit("Welcome back, ${admin.name}!")
                onSuccess()
            }.onFailure { err ->
                _authError.value = err.message ?: "Login failed"
            }
        }
    }

    fun register(
        name: String,
        email: String,
        pass: String,
        schoolName: String,
        role: String,
        phone: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _authError.value = null
            val result = repository.register(name, email, pass, schoolName, role, phone)
            result.onSuccess { admin ->
                _currentAdmin.value = admin
                _userMessage.emit("Admin account registered! Welcome ${admin.name}.")
                onSuccess()
            }.onFailure { err ->
                _authError.value = err.message ?: "Registration failed"
            }
        }
    }

    fun logout() {
        _currentAdmin.value = null
        _currentScreen.value = AppScreen.DASHBOARD
    }

    fun loadDemoAdmin() {
        viewModelScope.launch {
            val admin = repository.getAdminById(1) ?: return@launch
            _currentAdmin.value = admin
            _userMessage.emit("Logged in as ${admin.name} (${admin.role})")
        }
    }

    fun reloadAllDemoData() {
        viewModelScope.launch {
            repository.seedDemoData()
            val admin = repository.getAdminById(1)
            _currentAdmin.value = admin
            loadAttendanceForSelection(_markAttendanceState.value.selectedDepartmentId, _markAttendanceState.value.selectedDate)
            _userMessage.emit("Demo data successfully refreshed!")
        }
    }

    // Mark Attendance Actions
    fun onSelectDeptForAttendance(deptId: Long) {
        loadAttendanceForSelection(deptId, _markAttendanceState.value.selectedDate)
    }

    fun onSelectDateForAttendance(dateIso: String) {
        loadAttendanceForSelection(_markAttendanceState.value.selectedDepartmentId, dateIso)
    }

    fun loadAttendanceForSelection(deptId: Long, dateString: String) {
        viewModelScope.launch {
            val existingRecords = repository.getAttendanceForDeptAndDateOnce(deptId, dateString)
            val deptTeachers = repository.getTeachersForDepartmentOnce(deptId)

            val newMap = mutableMapOf<Long, TeacherAttendanceStatus>()
            val checkInMap = mutableMapOf<Long, String>()
            val remarksMap = mutableMapOf<Long, String>()

            if (existingRecords.isNotEmpty()) {
                existingRecords.forEach { rec ->
                    newMap[rec.teacherMemberId] = rec.status
                    if (rec.checkInTime.isNotBlank()) checkInMap[rec.teacherMemberId] = rec.checkInTime
                    if (rec.remarks.isNotBlank()) remarksMap[rec.teacherMemberId] = rec.remarks
                }
            } else {
                // By default mark all active teachers as PRESENT with default check-in time
                val defaultTime = _currentAdmin.value?.standardCheckInTime ?: "08:00 AM"
                deptTeachers.filter { it.isActive }.forEach { t ->
                    newMap[t.id] = TeacherAttendanceStatus.PRESENT
                    checkInMap[t.id] = defaultTime
                }
            }

            _markAttendanceState.value = _markAttendanceState.value.copy(
                selectedDepartmentId = deptId,
                selectedDate = dateString,
                attendanceMap = newMap,
                checkInTimeMap = checkInMap,
                remarksMap = remarksMap,
                isAlreadySaved = existingRecords.isNotEmpty(),
                confirmationMessage = null
            )
        }
    }

    fun setTeacherStatus(teacherId: Long, status: TeacherAttendanceStatus) {
        val updated = _markAttendanceState.value.attendanceMap.toMutableMap()
        updated[teacherId] = status
        _markAttendanceState.value = _markAttendanceState.value.copy(attendanceMap = updated)
    }

    fun setTeacherCheckInTime(teacherId: Long, time: String) {
        val updated = _markAttendanceState.value.checkInTimeMap.toMutableMap()
        updated[teacherId] = time
        _markAttendanceState.value = _markAttendanceState.value.copy(checkInTimeMap = updated)
    }

    fun setTeacherRemark(teacherId: Long, remark: String) {
        val updated = _markAttendanceState.value.remarksMap.toMutableMap()
        updated[teacherId] = remark
        _markAttendanceState.value = _markAttendanceState.value.copy(remarksMap = updated)
    }

    fun markAllAs(status: TeacherAttendanceStatus) {
        val currentDeptId = _markAttendanceState.value.selectedDepartmentId
        viewModelScope.launch {
            val deptTeachers = repository.getTeachersForDepartmentOnce(currentDeptId)
            val updated = _markAttendanceState.value.attendanceMap.toMutableMap()
            deptTeachers.filter { it.isActive }.forEach { t ->
                updated[t.id] = status
            }
            _markAttendanceState.value = _markAttendanceState.value.copy(attendanceMap = updated)
        }
    }

    fun toggleSimulateOffline() {
        val simulated = networkObserver.toggleSimulateOffline()
        viewModelScope.launch {
            if (simulated) {
                _userMessage.emit("📶 Offline Mode: Working without internet. Attendance records are saved locally in Room database.")
            } else {
                _userMessage.emit("🌐 Back Online: Connected. Local Room database records are ready for sync.")
            }
        }
    }

    fun syncOfflineRecords() {
        viewModelScope.launch {
            _isSyncing.value = true
            val result = repository.syncOfflineAttendanceRecords()
            _isSyncing.value = false
            result.onSuccess { syncedCount ->
                _lastSyncTimeFormatted.value = DateUtils.getCurrentTimeFormatted()
                if (syncedCount > 0) {
                    _userMessage.emit("✓ Synced $syncedCount offline attendance records from Room database!")
                } else {
                    _userMessage.emit("All Room database attendance records are already up-to-date.")
                }
            }.onFailure { err ->
                _userMessage.emit("Sync failed: ${err.message ?: "Database error"}")
            }
        }
    }

    fun saveAttendance() {
        val currentState = _markAttendanceState.value
        val admin = _currentAdmin.value
        val deptId = currentState.selectedDepartmentId
        val dateString = currentState.selectedDate
        val offline = !isOnline.value

        viewModelScope.launch {
            _markAttendanceState.value = currentState.copy(isSaving = true)
            val deptObj = repository.getDepartmentById(deptId)
            val deptTeachers = repository.getTeachersForDepartmentOnce(deptId)

            val recordsToSave = deptTeachers.filter { it.isActive }.map { t ->
                val status = currentState.attendanceMap[t.id] ?: TeacherAttendanceStatus.PRESENT
                val checkIn = currentState.checkInTimeMap[t.id] ?: (if (status == TeacherAttendanceStatus.PRESENT) "08:00 AM" else "")
                val remarks = currentState.remarksMap[t.id] ?: ""
                TeacherAttendanceRecord(
                    teacherMemberId = t.id,
                    employeeId = t.employeeId,
                    teacherName = t.name,
                    departmentId = deptId,
                    departmentName = deptObj?.departmentName ?: t.departmentName,
                    designation = t.designation,
                    dateString = dateString,
                    status = status,
                    checkInTime = checkIn,
                    remarks = remarks,
                    markedByAdminId = admin?.id ?: 1,
                    markedByAdminName = admin?.name ?: "Principal",
                    isSynced = !offline,
                    syncTimestamp = if (!offline) System.currentTimeMillis() else 0L,
                    isOfflineCreated = offline
                )
            }

            repository.saveAttendanceRecords(recordsToSave)
            _markAttendanceState.value = currentState.copy(
                isSaving = false,
                isAlreadySaved = true,
                confirmationMessage = if (offline) {
                    "✓ Offline Mode: Faculty attendance for ${deptObj?.fullDisplayName ?: "Department"} saved locally in Room SQLite database (${recordsToSave.size} teachers recorded). Will sync automatically when reconnected."
                } else {
                    "Faculty attendance for ${deptObj?.fullDisplayName ?: "Department"} on ${DateUtils.formatIsoToShort(dateString)} saved to Room database (${recordsToSave.size} teachers recorded)!"
                }
            )
            val toastMsg = if (offline) {
                "✓ Saved ${recordsToSave.size} faculty records locally to Room database (Offline mode)"
            } else {
                "Teacher attendance saved successfully to Room database!"
            }
            _userMessage.emit(toastMsg)
        }
    }

    fun clearConfirmationMessage() {
        _markAttendanceState.value = _markAttendanceState.value.copy(confirmationMessage = null)
    }

    // Teacher CRUD
    fun addTeacher(
        departmentId: Long,
        name: String,
        employeeId: String,
        designation: String,
        departmentName: String,
        phone: String,
        email: String,
        qualification: String,
        gender: String
    ) {
        viewModelScope.launch {
            val teacher = TeacherMember(
                departmentId = departmentId,
                name = name.trim(),
                employeeId = employeeId.trim(),
                designation = designation.trim(),
                departmentName = departmentName.trim(),
                phone = phone.trim(),
                email = email.trim(),
                qualification = qualification.trim(),
                gender = gender
            )
            repository.insertTeacher(teacher)
            _userMessage.emit("Teacher ${teacher.name} added to faculty roster!")
        }
    }

    fun updateTeacher(teacher: TeacherMember) {
        viewModelScope.launch {
            repository.updateTeacher(teacher)
            _userMessage.emit("Teacher profile for ${teacher.name} updated!")
        }
    }

    fun toggleTeacherActive(teacher: TeacherMember) {
        viewModelScope.launch {
            repository.setTeacherActiveState(teacher.id, !teacher.isActive)
            val action = if (teacher.isActive) "marked On Leave / Inactive" else "reactivated"
            _userMessage.emit("Teacher ${teacher.name} $action!")
        }
    }

    fun deleteTeacher(teacher: TeacherMember) {
        viewModelScope.launch {
            repository.deleteTeacher(teacher)
            _userMessage.emit("Teacher ${teacher.name} removed.")
        }
    }

    fun selectTeacherForDetail(teacher: TeacherMember?) {
        _selectedTeacherForDetail.value = teacher
    }

    // Department CRUD
    fun addDepartment(
        name: String,
        code: String,
        headOfDept: String,
        room: String,
        year: String
    ) {
        viewModelScope.launch {
            val admin = _currentAdmin.value
            val dept = Department(
                adminId = admin?.id ?: 1,
                departmentName = name.trim(),
                code = code.trim().uppercase(),
                headOfDepartment = headOfDept.trim(),
                roomOrOffice = room.trim(),
                academicYear = year.trim()
            )
            repository.insertDepartment(dept)
            _userMessage.emit("Department ${dept.fullDisplayName} created!")
        }
    }

    fun updateDepartment(dept: Department) {
        viewModelScope.launch {
            repository.updateDepartment(dept)
            _userMessage.emit("Department details updated!")
        }
    }

    fun deleteDepartment(dept: Department) {
        viewModelScope.launch {
            repository.deleteDepartment(dept)
            _userMessage.emit("Department removed.")
        }
    }

    // Staff Announcements
    fun addAnnouncement(
        deptId: Long,
        targetAudience: String,
        title: String,
        message: String,
        priority: String
    ) {
        viewModelScope.launch {
            val admin = _currentAdmin.value
            val ann = StaffAnnouncement(
                adminId = admin?.id ?: 1,
                authorName = admin?.name ?: "Principal",
                departmentId = deptId,
                targetAudience = if (deptId == 0L) "All Faculty" else targetAudience,
                title = title.trim(),
                message = message.trim(),
                priority = priority,
                dateString = DateUtils.getTodayIso()
            )
            repository.insertAnnouncement(ann)
            _userMessage.emit("Staff notice published!")
        }
    }

    fun deleteAnnouncement(id: Long) {
        viewModelScope.launch {
            repository.deleteAnnouncement(id)
            _userMessage.emit("Notice removed.")
        }
    }

    // Profile & Settings
    fun updateAdminProfile(name: String, schoolName: String, role: String, phone: String, standardCheckIn: String) {
        val current = _currentAdmin.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                name = name.trim(),
                schoolName = schoolName.trim(),
                role = role.trim(),
                phone = phone.trim(),
                standardCheckInTime = standardCheckIn.trim()
            )
            repository.updateAdmin(updated)
            _currentAdmin.value = updated
            _userMessage.emit("School & Admin profile updated!")
        }
    }

    fun updateAttendanceRules(countLateAsAttended: Boolean, minimumTarget: Int) {
        val current = _currentAdmin.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                countLateAsAttended = countLateAsAttended,
                minimumAttendanceTarget = minimumTarget
            )
            repository.updateAdmin(updated)
            _currentAdmin.value = updated
            _userMessage.emit("Faculty attendance rules updated!")
        }
    }

    fun changePassword(oldPass: String, newPass: String, onDone: (Boolean, String) -> Unit) {
        val current = _currentAdmin.value ?: return
        viewModelScope.launch {
            val result = repository.changePassword(current.id, oldPass, newPass)
            result.onSuccess {
                onDone(true, "Password changed successfully!")
            }.onFailure { err ->
                onDone(false, err.message ?: "Failed to change password")
            }
        }
    }

    // Filter setters
    fun setTeacherSearchQuery(q: String) { _teacherSearchQuery.value = q }
    fun setSelectedDeptFilter(deptId: Long?) { _selectedDeptFilter.value = deptId }
    fun setStatusActiveFilter(active: Boolean?) { _statusActiveFilter.value = active }

    fun setHistoryDateFilter(d: String?) { _historyDateFilter.value = d }
    fun setHistoryDeptFilter(deptId: Long?) { _historyDeptFilter.value = deptId }
    fun setHistoryStatusFilter(s: TeacherAttendanceStatus?) { _historyStatusFilter.value = s }
    fun setHistorySearchQuery(q: String) { _historySearchQuery.value = q }

    fun setReportPeriod(p: ReportPeriod) { _reportPeriod.value = p }
    fun setReportDeptFilter(deptId: Long?) { _reportDeptFilter.value = deptId }
    fun selectReportMonth(yearMonth: String) { _reportSelectedMonth.value = yearMonth }
    fun selectPreviousReportMonth() { _reportSelectedMonth.value = DateUtils.getPreviousMonth(_reportSelectedMonth.value) }
    fun selectNextReportMonth() { _reportSelectedMonth.value = DateUtils.getNextMonth(_reportSelectedMonth.value) }
    fun setReportTeacherSearchQuery(q: String) { _reportTeacherSearchQuery.value = q }
    fun setReportSortOrder(order: ReportSortOrder) { _reportSortOrder.value = order }

    fun getAvailableReportMonths(): List<String> {
        val records = allAttendanceRecords.value
        val monthsFromDb = records.mapNotNull {
            if (it.dateString.length >= 7) it.dateString.substring(0, 7) else null
        }.distinct()
        val recentMonths = DateUtils.getPastMonthsList(6)
        return (monthsFromDb + recentMonths).distinct().sortedDescending()
    }

    // Computations
    fun calculateTeacherAttendanceRate(teacherId: Long): Float {
        val records = allAttendanceRecords.value.filter { it.teacherMemberId == teacherId }
        if (records.isEmpty()) return 100f
        val countLate = _currentAdmin.value?.countLateAsAttended ?: true
        val attendedCount = records.count {
            it.status == TeacherAttendanceStatus.PRESENT || (countLate && it.status == TeacherAttendanceStatus.LATE)
        }
        return (attendedCount.toFloat() / records.size) * 100f
    }

    fun getTeacherAttendanceBreakdown(teacherId: Long): Quadruple<Int, Int, Int, Int> {
        val records = allAttendanceRecords.value.filter { it.teacherMemberId == teacherId }
        val present = records.count { it.status == TeacherAttendanceStatus.PRESENT }
        val absent = records.count { it.status == TeacherAttendanceStatus.ABSENT }
        val late = records.count { it.status == TeacherAttendanceStatus.LATE }
        val leave = records.count { it.status == TeacherAttendanceStatus.ON_LEAVE }
        return Quadruple(present, absent, late, leave)
    }

    // Reports Generation
    fun generateReportData(): Pair<DashboardMetrics, List<TeacherReportRow>> {
        val period = _reportPeriod.value
        val deptFilter = _reportDeptFilter.value
        val allRecords = allAttendanceRecords.value
        val allTchs = teachers.value
        val countLate = _currentAdmin.value?.countLateAsAttended ?: true

        val filteredRecords = when (period) {
            ReportPeriod.DAILY -> allRecords.filter { it.dateString == DateUtils.getTodayIso() }
            ReportPeriod.WEEKLY -> {
                val sevenDays = DateUtils.getDaysList(7)
                allRecords.filter { it.dateString in sevenDays }
            }
            ReportPeriod.MONTHLY -> {
                val thirtyDays = DateUtils.getDaysList(30)
                allRecords.filter { it.dateString in thirtyDays }
            }
            ReportPeriod.ALL_TIME -> allRecords
        }.let { list ->
            if (deptFilter != null) list.filter { it.departmentId == deptFilter } else list
        }

        val targetTeachers = if (deptFilter != null) {
            allTchs.filter { it.departmentId == deptFilter }
        } else {
            allTchs
        }

        val rows = targetTeachers.map { t ->
            val tRecs = filteredRecords.filter { it.teacherMemberId == t.id }
            val present = tRecs.count { it.status == TeacherAttendanceStatus.PRESENT }
            val absent = tRecs.count { it.status == TeacherAttendanceStatus.ABSENT }
            val late = tRecs.count { it.status == TeacherAttendanceStatus.LATE }
            val leave = tRecs.count { it.status == TeacherAttendanceStatus.ON_LEAVE }
            val total = tRecs.size
            val attended = present + (if (countLate) late else 0)
            val pct = if (total > 0) (attended.toFloat() / total) * 100f else 100f

            TeacherReportRow(
                employeeId = t.employeeId,
                teacherName = t.name,
                departmentName = t.departmentName,
                designation = t.designation,
                presentCount = present,
                absentCount = absent,
                lateCount = late,
                leaveCount = leave,
                totalDays = total,
                percentage = pct
            )
        }

        val totalPresent = filteredRecords.count { it.status == TeacherAttendanceStatus.PRESENT }
        val totalAbsent = filteredRecords.count { it.status == TeacherAttendanceStatus.ABSENT }
        val totalLate = filteredRecords.count { it.status == TeacherAttendanceStatus.LATE }
        val totalLeave = filteredRecords.count { it.status == TeacherAttendanceStatus.ON_LEAVE }
        val totalRecords = filteredRecords.size
        val attendedTotal = totalPresent + (if (countLate) totalLate else 0)
        val overallPct = if (totalRecords > 0) (attendedTotal.toFloat() / totalRecords) * 100f else 0f

        val metrics = DashboardMetrics(
            totalTeachers = targetTeachers.size,
            totalDepartments = departments.value.size,
            presentToday = totalPresent,
            absentToday = totalAbsent,
            lateToday = totalLate,
            onLeaveToday = totalLeave,
            totalMarkedToday = totalRecords,
            overallAttendancePercent = overallPct
        )

        return Pair(metrics, rows)
    }

    // Dedicated Monthly Attendance Report Calculation
    fun generateMonthlyAttendanceReport(
        targetMonthYear: String = _reportSelectedMonth.value
    ): MonthlyReportResult {
        val allRecords = allAttendanceRecords.value
        val allTchs = teachers.value.filter { it.isActive }
        val deptFilter = _reportDeptFilter.value
        val searchQuery = _reportTeacherSearchQuery.value.trim().lowercase()
        val sortOrder = _reportSortOrder.value
        val countLate = _currentAdmin.value?.countLateAsAttended ?: true
        val targetBenchmark = _currentAdmin.value?.minimumAttendanceTarget ?: 85

        val targetTeachers = if (deptFilter != null) {
            allTchs.filter { it.departmentId == deptFilter }
        } else {
            allTchs
        }

        // All records matching this month prefix (e.g. "2026-09")
        val monthRecords = allRecords.filter { it.dateString.startsWith(targetMonthYear) }
        val filteredDeptRecords = if (deptFilter != null) {
            monthRecords.filter { it.departmentId == deptFilter }
        } else {
            monthRecords
        }

        val rows = targetTeachers.map { teacher ->
            val teacherRecords = monthRecords.filter { it.teacherMemberId == teacher.id }
                .sortedByDescending { it.dateString }
            val present = teacherRecords.count { it.status == TeacherAttendanceStatus.PRESENT }
            val late = teacherRecords.count { it.status == TeacherAttendanceStatus.LATE }
            val absent = teacherRecords.count { it.status == TeacherAttendanceStatus.ABSENT }
            val leave = teacherRecords.count { it.status == TeacherAttendanceStatus.ON_LEAVE }
            val total = teacherRecords.size
            val attended = present + (if (countLate) late else 0)
            val pct = if (total > 0) (attended.toFloat() / total) * 100f else 0f

            val punctuality = if (present + late > 0) {
                (present.toFloat() / (present + late)) * 100f
            } else 100f

            val tier = when {
                pct >= targetBenchmark -> "Excellent (Target Met)"
                pct >= 75f -> "Satisfactory"
                else -> "Needs Attention"
            }

            MonthlyTeacherReportRow(
                teacherId = teacher.id,
                employeeId = teacher.employeeId,
                teacherName = teacher.name,
                departmentName = teacher.departmentName,
                designation = teacher.designation,
                monthYear = targetMonthYear,
                monthLabel = DateUtils.formatYearMonth(targetMonthYear),
                presentCount = present,
                absentCount = absent,
                lateCount = late,
                leaveCount = leave,
                totalDays = total,
                attendedDays = attended,
                percentage = pct,
                punctualityRate = punctuality,
                performanceTier = tier,
                dailyRecords = teacherRecords
            )
        }

        // Apply Search Query filter
        val searchFiltered = if (searchQuery.isNotEmpty()) {
            rows.filter {
                it.teacherName.lowercase().contains(searchQuery) ||
                it.employeeId.lowercase().contains(searchQuery) ||
                it.departmentName.lowercase().contains(searchQuery)
            }
        } else rows

        // Apply Sorting
        val sortedRows = when (sortOrder) {
            ReportSortOrder.PERCENTAGE_DESC -> searchFiltered.sortedByDescending { it.percentage }
            ReportSortOrder.PERCENTAGE_ASC -> searchFiltered.sortedBy { it.percentage }
            ReportSortOrder.NAME_ASC -> searchFiltered.sortedBy { it.teacherName }
            ReportSortOrder.ABSENT_DESC -> searchFiltered.sortedByDescending { it.absentCount }
            ReportSortOrder.LATE_DESC -> searchFiltered.sortedByDescending { it.lateCount }
        }

        // Overall Monthly Metrics
        val totalMarked = filteredDeptRecords.size
        val totalPresent = filteredDeptRecords.count { it.status == TeacherAttendanceStatus.PRESENT }
        val totalLate = filteredDeptRecords.count { it.status == TeacherAttendanceStatus.LATE }
        val totalAbsent = filteredDeptRecords.count { it.status == TeacherAttendanceStatus.ABSENT }
        val totalLeave = filteredDeptRecords.count { it.status == TeacherAttendanceStatus.ON_LEAVE }
        val attendedTotal = totalPresent + (if (countLate) totalLate else 0)
        val overallPct = if (totalMarked > 0) (attendedTotal.toFloat() / totalMarked) * 100f else 0f
        val avgPunctuality = if (totalPresent + totalLate > 0) {
            (totalPresent.toFloat() / (totalPresent + totalLate)) * 100f
        } else 100f

        val targetMet = rows.count { it.percentage >= targetBenchmark }
        val satisfactory = rows.count { it.percentage in 75f..targetBenchmark.toFloat() - 0.01f }
        val belowTarget = rows.count { it.percentage < 75f }

        // Top performing department
        val topDept = departments.value.maxByOrNull { dept ->
            val dRecs = monthRecords.filter { it.departmentId == dept.id }
            if (dRecs.isNotEmpty()) {
                dRecs.count { it.status == TeacherAttendanceStatus.PRESENT || (countLate && it.status == TeacherAttendanceStatus.LATE) }.toFloat() / dRecs.size
            } else 0f
        }?.departmentName ?: "All Departments"

        return MonthlyReportResult(
            monthYear = targetMonthYear,
            monthLabel = DateUtils.formatYearMonth(targetMonthYear),
            teacherRows = sortedRows,
            totalTeachers = rows.size,
            totalMarkedDays = totalMarked,
            overallAttendancePercent = overallPct,
            averagePunctualityRate = avgPunctuality,
            targetMetCount = targetMet,
            satisfactoryCount = satisfactory,
            belowTargetCount = belowTarget,
            totalPresentCount = totalPresent,
            totalLateCount = totalLate,
            totalAbsentCount = totalAbsent,
            totalLeaveCount = totalLeave,
            topDepartmentName = topDept,
            availableMonths = getAvailableReportMonths()
        )
    }

    // Dashboard metrics for today
    val todayMetrics: StateFlow<DashboardMetrics> = combine(
        allAttendanceRecords,
        teachers,
        departments,
        _currentAdmin
    ) { records, tchs, depts, admin ->
        val todayIso = DateUtils.getTodayIso()
        val todayRecords = records.filter { it.dateString == todayIso }
        val present = todayRecords.count { it.status == TeacherAttendanceStatus.PRESENT }
        val absent = todayRecords.count { it.status == TeacherAttendanceStatus.ABSENT }
        val late = todayRecords.count { it.status == TeacherAttendanceStatus.LATE }
        val leave = todayRecords.count { it.status == TeacherAttendanceStatus.ON_LEAVE }
        val total = todayRecords.size
        val countLate = admin?.countLateAsAttended ?: true
        val attended = present + (if (countLate) late else 0)
        val overall = if (total > 0) (attended.toFloat() / total) * 100f else 0f

        DashboardMetrics(
            totalTeachers = tchs.count { it.isActive },
            totalDepartments = depts.size,
            presentToday = present,
            absentToday = absent,
            lateToday = late,
            onLeaveToday = leave,
            totalMarkedToday = total,
            overallAttendancePercent = overall
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    // Student QR Scanner Actions
    fun selectClassForQrScanner(className: String) {
        _studentQrScannerState.value = _studentQrScannerState.value.copy(selectedClass = className)
    }

    fun setStudentScannerDate(dateIso: String) {
        _studentQrScannerState.value = _studentQrScannerState.value.copy(selectedDate = dateIso)
    }

    fun setScanningActive(active: Boolean) {
        _studentQrScannerState.value = _studentQrScannerState.value.copy(isScanningActive = active)
    }

    fun clearLastScanResult() {
        _studentQrScannerState.value = _studentQrScannerState.value.copy(lastScanResult = null)
    }

    fun setManualScanInput(input: String) {
        _studentQrScannerState.value = _studentQrScannerState.value.copy(manualInputId = input)
    }

    fun setStudentSearchQuery(query: String) {
        _studentQrScannerState.value = _studentQrScannerState.value.copy(searchQuery = query)
    }

    fun openStudentBadgeDialog(student: StudentMember? = null) {
        _studentQrScannerState.value = _studentQrScannerState.value.copy(
            showQrBadgesDialog = true,
            selectedStudentForBadge = student
        )
    }

    fun closeStudentBadgeDialog() {
        _studentQrScannerState.value = _studentQrScannerState.value.copy(
            showQrBadgesDialog = false,
            selectedStudentForBadge = null
        )
    }

    fun onQrCodeScanned(rawCode: String) {
        viewModelScope.launch {
            val state = _studentQrScannerState.value
            val teacherName = _currentAdmin.value?.name ?: "Attendance Teacher"
            val currentTime = DateUtils.getCurrentTimeFormatted()
            val offline = !isOnline.value

            val result = repository.processScannedQrCode(
                rawScannedPayload = rawCode,
                dateString = state.selectedDate,
                timeString = currentTime,
                markedByTeacherName = teacherName,
                preferredClass = state.selectedClass,
                isOffline = offline
            )

            _studentQrScannerState.value = _studentQrScannerState.value.copy(
                lastScanResult = result
            )

            when (result) {
                is QrScanResult.StudentSuccess -> {
                    val offlineBadge = if (offline) " [Offline: Saved in Room DB]" else ""
                    _userMessage.emit("✓ Marked ${result.student.name} (Roll #${result.student.rollNumber}) Present at ${result.record.checkInTime}$offlineBadge")
                }
                is QrScanResult.FacultySuccess -> {
                    val offlineBadge = if (offline) " [Offline: Saved in Room DB]" else ""
                    _userMessage.emit("✓ Faculty ${result.teacher.name} marked Present at ${result.record.checkInTime}$offlineBadge")
                }
                is QrScanResult.AlreadyMarked -> {
                    _userMessage.emit("ℹ ${result.name} already marked Present at ${result.time}")
                }
                is QrScanResult.NotFound -> {
                    _userMessage.emit("⚠ Unrecognized QR Code: '$rawCode'")
                }
                is QrScanResult.Error -> {
                    _userMessage.emit("Error: ${result.message}")
                }
            }
        }
    }

    fun submitManualScan() {
        val code = _studentQrScannerState.value.manualInputId.trim()
        if (code.isBlank()) return
        _studentQrScannerState.value = _studentQrScannerState.value.copy(manualInputId = "")
        onQrCodeScanned(code)
    }

    fun simulateScanForStudent(student: StudentMember) {
        onQrCodeScanned(student.qrCodeData)
    }

    fun toggleStudentAttendanceStatus(student: StudentMember, newStatus: StudentAttendanceStatus) {
        viewModelScope.launch {
            val state = _studentQrScannerState.value
            val teacherName = _currentAdmin.value?.name ?: "Attendance Teacher"
            val currentTime = DateUtils.getCurrentTimeFormatted()
            val offline = !isOnline.value

            repository.recordStudentAttendance(
                student = student,
                dateString = state.selectedDate,
                status = newStatus,
                checkInTime = if (newStatus == StudentAttendanceStatus.PRESENT || newStatus == StudentAttendanceStatus.LATE) currentTime else "",
                scanMethod = "MANUAL",
                teacherName = teacherName,
                remarks = if (offline) "Teacher Manual Override (Room DB Offline)" else "Teacher Manual Override",
                isOffline = offline
            )
            val offlineNote = if (offline) " [Room DB Offline]" else ""
            _userMessage.emit("Updated ${student.name} to ${newStatus.name}$offlineNote")
        }
    }

    fun quickMarkAllStudentsPresent(gradeClass: String) {
        viewModelScope.launch {
            val state = _studentQrScannerState.value
            val students = repository.getStudentsByClassOnce(gradeClass)
            val teacherName = _currentAdmin.value?.name ?: "Attendance Teacher"
            val currentTime = DateUtils.getCurrentTimeFormatted()
            val offline = !isOnline.value

            val records = students.map { student ->
                StudentAttendanceRecord(
                    studentMemberId = student.id,
                    studentId = student.studentId,
                    studentName = student.name,
                    rollNumber = student.rollNumber,
                    gradeClass = student.gradeClass,
                    dateString = state.selectedDate,
                    status = StudentAttendanceStatus.PRESENT,
                    checkInTime = currentTime,
                    scanMethod = "MANUAL_ALL",
                    markedByTeacherName = teacherName,
                    remarks = if (offline) "All Marked Present (Room DB Offline)" else "All Marked Present by Faculty",
                    isSynced = !offline,
                    syncTimestamp = if (!offline) System.currentTimeMillis() else 0L,
                    isOfflineCreated = offline
                )
            }
            repository.saveStudentAttendanceBatch(records, isOffline = offline)
            val offlineNote = if (offline) " (Stored locally in Room DB)" else ""
            _userMessage.emit("Marked all ${students.size} students in $gradeClass Present$offlineNote")
        }
    }

    fun clearClassStudentAttendanceToday(gradeClass: String) {
        viewModelScope.launch {
            val state = _studentQrScannerState.value
            repository.deleteStudentAttendanceForClassAndDate(gradeClass, state.selectedDate)
            _userMessage.emit("Cleared attendance for $gradeClass on ${state.selectedDate}")
        }
    }
}

data class StudentQrScannerUiState(
    val selectedClass: String = "Class 10-A",
    val selectedDate: String = DateUtils.getTodayIso(),
    val isScanningActive: Boolean = true,
    val lastScanResult: QrScanResult? = null,
    val manualInputId: String = "",
    val showQrBadgesDialog: Boolean = false,
    val selectedStudentForBadge: StudentMember? = null,
    val searchQuery: String = ""
)

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
