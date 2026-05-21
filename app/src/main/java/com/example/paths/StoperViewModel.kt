package com.example.paths

import android.annotation.SuppressLint
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StoperViewModel : ViewModel() {

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime = _elapsedTime.asStateFlow()

    private var startTime = 0L

    private var timerJob: Job? = null

    private var isRunning = false

    fun start() {

        if (isRunning) return

        isRunning = true

        startTime =
            SystemClock.elapsedRealtime() - _elapsedTime.value

        timerJob = viewModelScope.launch {

            while (isRunning) {

                _elapsedTime.value =
                    SystemClock.elapsedRealtime() - startTime

                delay(10)
            }
        }
    }

    fun stop() {

        isRunning = false

        timerJob?.cancel()
    }

    fun reset() {

        stop()

        _elapsedTime.value = 0L
    }
    fun is_Start() : Boolean{
        return isRunning;
    }

    @SuppressLint("DefaultLocale")
    fun formatTime(timeMillis: Long): String {

        val hours = (timeMillis / 1000) / 3600
        val minutes = ((timeMillis / 1000) % 3600) / 60
        val seconds = (timeMillis / 1000) % 60
        val centiseconds = (timeMillis % 1000) / 10

        return String.format(
            "%02d:%02d:%02d,%02d",
            hours,
            minutes,
            seconds,
            centiseconds
        )
    }
}