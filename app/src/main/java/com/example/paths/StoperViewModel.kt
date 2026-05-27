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

    fun start() {
        if (_isRunning.value) return
        _isRunning.value = true
        startTime = SystemClock.elapsedRealtime() - _elapsedTime.value
        timerJob?.cancel() // Zabezpieczenie przed zdublowaniem korutyny
        timerJob = viewModelScope.launch {
            while (_isRunning.value) {
                _elapsedTime.value = SystemClock.elapsedRealtime() - startTime
                delay(100) // Zwiększono opóźnienie z 10ms na 100ms dla lepszej wydajności
            }
        }
    }

    fun stop() {
        _isRunning.value = false
        timerJob?.cancel()
        timerJob = null
    }

    fun reset() {
        stop()
        _elapsedTime.value = 0L
    }
    
    fun is_Start() : Boolean {
        return _isRunning.value
    }
}
