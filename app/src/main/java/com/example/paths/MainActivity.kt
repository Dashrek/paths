package com.example.paths

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.paths.ui.theme.PathsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var rower by remember { mutableStateOf(false) }
            var pieszy by remember { mutableStateOf(false) }

            val roweryVM: ItemViewModel = viewModel(key = "rowery")
            val piesiVM: ItemViewModel = viewModel(key = "piesi")

            val roweryItems by roweryVM.items.collectAsStateWithLifecycle()
            val piesiItems by piesiVM.items.collectAsStateWithLifecycle()

            val configuration = LocalConfiguration.current
            val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            
            PathsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            Button(
                                onClick = { rower = !rower; pieszy = false },
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (rower) Color.Red else Color.Green,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    if (rower) "\uD83D\uDEB4" else "\uD83D\uDEB4\u200D➡\uFE0F",
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            
                            Stopwatch(
                                modifier = Modifier
                                    .weight(if (!isLandscape) 1f else 2F)
                                    .fillMaxWidth(),
                                isLandscape = isLandscape
                            )
                            
                            Button(
                                onClick = { pieszy = !pieszy; rower = false },
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (pieszy) Color.Red else Color.Green,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    if (pieszy) "\uD83C\uDFC3" else "\uD83C\uDFC3\u200D➡\uFE0F",
                                    maxLines = 2,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        
                        Column(modifier = Modifier.weight(7.5f)) {
                            AnimatedVisibility(rower) {
                                ButtonField(
                                    roweryItems,
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    isLandscape = isLandscape
                                )
                            }
                            AnimatedVisibility(pieszy) {
                                ButtonField(
                                    piesiItems,
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    isLandscape = isLandscape
                                )
                            }
                            AnimatedVisibility(!rower && !pieszy) {
                                Text("Reklama", modifier = Modifier.fillMaxHeight())
                            }
                        }
                    }
                }
            }
        }
    }
}
