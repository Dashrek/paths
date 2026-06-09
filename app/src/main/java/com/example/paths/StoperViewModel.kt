package com.example.paths

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class StoperViewModel : ViewModel() {

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime = _elapsedTime.asStateFlow()

    private val _currentTime = MutableStateFlow("")
    val currentTime = _currentTime.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    // Przechowuje ID trasy, dla której stoper jest uruchomiony
    private val _activeRouteId = MutableStateFlow<String?>(null)
    val activeRouteId = _activeRouteId.asStateFlow()

    private var startTime = 0L
    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            while (true) {
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                val second = calendar.get(Calendar.SECOND)
                _currentTime.value = String.format("%02d:%02d:%02d", hour, minute, second)
                delay(1000)
            }
        }
    }

    fun start(routeId: String? = null) {
        if (_isRunning.value) return
        _isRunning.value = true
        if (routeId != null) {
            _activeRouteId.value = routeId
        }
        startTime = SystemClock.elapsedRealtime() - _elapsedTime.value
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isRunning.value) {
                _elapsedTime.value = SystemClock.elapsedRealtime() - startTime
                delay(100)
            }
        }
    }

    fun pause() {
        _isRunning.value = false
        timerJob?.cancel()
        timerJob = null
    }

    private val _pendingRecord = MutableStateFlow<Long?>(null)
    val pendingRecord = _pendingRecord.asStateFlow()

    fun clearPendingRecord() {
        _pendingRecord.value = null
    }

    fun stop() {
        if (_activeRouteId.value != null && _elapsedTime.value > 0) {
            _pendingRecord.value = _elapsedTime.value
        }
        // Zawsze resetujemy stoper po kliknięciu stop
        _isRunning.value = false
        timerJob?.cancel()
        timerJob = null
        _elapsedTime.value = 0L
        
        // Jeśli nie ma rekordu do zapisania, czyścimy też ID trasy
        if (_pendingRecord.value == null) {
            _activeRouteId.value = null
        }
    }

    fun reset() {
        _isRunning.value = false
        timerJob?.cancel()
        timerJob = null
        _elapsedTime.value = 0L
        _activeRouteId.value = null
        _pendingRecord.value = null
    }

    fun saveRecord(dao: StopwatchDao, userId: String) {
        val routeId = _activeRouteId.value ?: "unknown"
        val time = _pendingRecord.value ?: _elapsedTime.value
        viewModelScope.launch {
            dao.insertRecord(
                LocalStopwatchRecord(
                    routeFirebaseId = routeId,
                    userId = userId,
                    timeElapsed = time,
                    timestamp = System.currentTimeMillis()
                )
            )
            reset()
        }
    }
    
    fun is_Start() : Boolean {
        return _isRunning.value
    }
}
