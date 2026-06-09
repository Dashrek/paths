package com.example.paths

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.paths.ui.theme.PathsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val authVM: AuthViewModel = viewModel()
            val isDarkModeState by authVM.isDarkMode.collectAsStateWithLifecycle()
            val colorSchemeIndex by authVM.colorSchemeIndex.collectAsStateWithLifecycle()
            
            PathsTheme(
                darkTheme = isDarkModeState ?: isSystemInDarkTheme(),
                colorSchemeIndex = colorSchemeIndex
            ) {
                AppNavigation(stoperVM = viewModel())
            }
        }
    }
}
