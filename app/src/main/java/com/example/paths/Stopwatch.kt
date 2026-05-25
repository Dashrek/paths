package com.example.paths

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun Stopwatch(
    modifier: Modifier,
    isLandscape: Boolean,
    viewModel: StoperViewModel = viewModel()
) {
    val elapsedTime by viewModel.elapsedTime.collectAsStateWithLifecycle()
    var raise by remember { mutableStateOf(viewModel.is_Start()) }

    if (!isLandscape) {
        Column(modifier = modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        if (!raise) viewModel.start() else viewModel.stop()
                        raise = viewModel.is_Start()
                    },
                    modifier = Modifier.weight(1F)
                ) {
                    AnimatedVisibility(!raise) {
                        Text("\u25B6\uFE0E", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                    AnimatedVisibility(raise) {
                        Text("\u2161\uFE0E", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }

                Button(
                    onClick = {
                        viewModel.reset()
                        raise = viewModel.is_Start()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("■\uFE0E", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }
            Text(
                text = formatTime(elapsedTime),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(width = 2.dp, color = Color.White, shape = RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    } else {
        Row(modifier = modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    if (!raise) viewModel.start() else viewModel.stop()
                    raise = viewModel.is_Start()
                },
                modifier = Modifier.weight(1F)
            ) {
                AnimatedVisibility(!raise) {
                    Text("\u25B6\uFE0E", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
                AnimatedVisibility(raise) {
                    Text("\u2161\uFE0E", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }

            Button(
                onClick = {
                    viewModel.reset()
                    raise = viewModel.is_Start()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("■\uFE0E", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
            Text(
                text = formatTime(elapsedTime),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 2.dp, color = Color.White, shape = RoundedCornerShape(12.dp))
                    .weight(2.5f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}
