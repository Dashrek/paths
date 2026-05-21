package com.example.paths

import android.annotation.SuppressLint
import android.content.ClipData
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import com.example.paths.ui.theme.PathsTheme
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.items
import android.os.SystemClock
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val Padding : Modifier = Modifier.padding(horizontal = 5.dp, vertical = 5.dp);
            var rower by remember { mutableStateOf(false) }
            var pieszy by remember { mutableStateOf(false) }
            val piesi : List<String> = List(50) { "Element pieszy ${it + 1}" }
            val rowery : List<String> = List(50) { "Element rower ${it + 1}" }
            val configuration = LocalConfiguration.current

            val isLandscape =
                configuration.orientation ==
                        Configuration.ORIENTATION_LANDSCAPE
            PathsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column {
                    Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        //przycisk rowerzysta
                        Button(onClick={rower=!rower; pieszy=false},
                            modifier = Modifier.padding(innerPadding).weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (rower) Color.Red else Color.Green,
                                contentColor = Color.White
                            )) {
                            Text(if (rower) "\uD83D\uDEB4" else "\uD83D\uDEB4\u200D➡\uFE0F",
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis)
                        }
                        Stopwatch(modifier=Modifier.padding(innerPadding).weight(if(!isLandscape)1f else 2F).fillMaxWidth());
                        //Przycisk pieszy
                        Button(onClick={pieszy=!pieszy; rower=false},
                            modifier=Modifier.padding(innerPadding).weight(1f),
                            colors=ButtonDefaults.buttonColors(
                                containerColor = if (pieszy) Color.Red else Color.Green,
                                contentColor = Color.White
                            )) {
                            Text(if (pieszy) "\uD83C\uDFC3" else "\uD83C\uDFC3\u200D➡\uFE0F",maxLines = 2,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Column(modifier = Modifier.weight(4f)) {
                        AnimatedVisibility(rower) {
                            ButtonField(
                                rowery,
                                modifier = Modifier.padding(innerPadding).fillMaxWidth().weight(1f),rower
                            );
                        }
                        AnimatedVisibility(pieszy) {
                            ButtonField(
                                piesi,
                                modifier = Modifier.padding(innerPadding).fillMaxWidth().weight(1f),pieszy
                            );
                        }
                        AnimatedVisibility(!rower && !pieszy) {
                            Text("Reklama", modifier=Modifier.padding(innerPadding).fillMaxHeight())
                        }
                    }
                    }
                }
            }
        }
    }
}

//@Preview(showBackground = true)
@Composable
fun ButtonField(lista: List<String>, modifier: Modifier, wizja: Boolean) {
    AnimatedVisibility(visible=wizja, modifier=modifier) {
        LazyColumn(modifier = Modifier.fillMaxWidth()
            .padding(top = 8.dp)
            .border(1.dp, Color.Gray)) {
            items(lista) { item ->
                Text(item)
                //HorizontalDivider()
            }
        }
    }

}

@Composable
fun Stopwatch(modifier:Modifier, viewModel: StoperViewModel = viewModel()
) {
        val configuration = LocalConfiguration.current

        val isLandscape =
            configuration.orientation ==
                    Configuration.ORIENTATION_LANDSCAPE
        val elapsedTime by
        viewModel.elapsedTime.collectAsStateWithLifecycle()
        var raise by remember{ mutableStateOf(viewModel.is_Start())}
    if(!isLandscape){
        Column(modifier=modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth()) {

                Button(
                    onClick = {
                        if (!raise)
                            viewModel.start();
                        else
                            viewModel.stop();
                        raise = viewModel.is_Start();
                    }, modifier = Modifier.weight(1F)
                ) {
                    //Play
                    AnimatedVisibility(!raise) {
                        Text(
                            "\u25B6\uFE0E", textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    //Pause
                    AnimatedVisibility(raise) {
                        Text(
                            "\u2161\uFE0E", textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }


                Button(
                    onClick = {
                        viewModel.reset(); raise =
                        viewModel.is_Start();//usunięcie ikony pause na play
                    }, modifier = Modifier.weight(1f)
                ) {
                    //stop
                    Text(
                        "■\uFE0E", textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
                Text(
                    text = viewModel.formatTime(elapsedTime),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().weight(1f).border(
                        width = 2.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    )
                        .padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        )
                )
            }
        }
    else{
        Row(modifier=modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    if (!raise)
                        viewModel.start();
                    else
                        viewModel.stop();
                    raise = viewModel.is_Start();
                }, modifier = Modifier.weight(1F)
            ) {
                //Play
                AnimatedVisibility(!raise) {
                    Text(
                        "\u25B6\uFE0E", textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                //Pause
                AnimatedVisibility(raise) {
                    Text(
                        "\u2161\uFE0E", textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }


            Button(
                onClick = {
                    viewModel.reset(); raise =
                    viewModel.is_Start();//usunięcie ikony pause na play
                }, modifier = Modifier.weight(1f)
            ) {
                //stop
                Text(
                    "■\uFE0E", textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Text(
                text = viewModel.formatTime(elapsedTime),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().border(
                    width = 2.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp)
                ).weight(2.5f)
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    )
            )
        }

        }
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
