package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Observes network connectivity status and provides offline simulation capability
 * so teachers can verify offline Room attendance logging in any environment.
 * Uses lightweight querying to prevent emulator kernel audit and SELinux rate limiting.
 */
class NetworkConnectivityObserver(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    // Allows user to manually simulate offline mode for testing
    private val _isSimulatingOffline = MutableStateFlow(false)
    val isSimulatingOffline = _isSimulatingOffline.asStateFlow()

    private val _realNetworkStatus = MutableStateFlow(checkConnected())
    val realNetworkStatus: Flow<Boolean> = _realNetworkStatus.asStateFlow()

    /**
     * Effective online status, taking into account simulated offline mode.
     * True if device is connected AND not simulating offline mode.
     */
    val isOnline: Flow<Boolean> = combine(_realNetworkStatus, _isSimulatingOffline) { realConnected, simulatingOffline ->
        if (simulatingOffline) false else realConnected
    }.distinctUntilChanged()

    fun checkConnected(): Boolean {
        return try {
            val manager = connectivityManager ?: return true
            val activeNetwork = manager.activeNetwork ?: return true
            val capabilities = manager.getNetworkCapabilities(activeNetwork) ?: return true
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            true // default to connected if permission/inspection restricted
        }
    }

    fun refreshNetworkStatus() {
        _realNetworkStatus.value = checkConnected()
    }

    fun setSimulateOffline(simulate: Boolean) {
        _isSimulatingOffline.value = simulate
    }

    fun toggleSimulateOffline(): Boolean {
        _isSimulatingOffline.value = !_isSimulatingOffline.value
        return _isSimulatingOffline.value
    }
}
