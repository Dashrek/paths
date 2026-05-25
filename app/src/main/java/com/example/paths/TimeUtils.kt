package com.example.paths

import android.annotation.SuppressLint

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
