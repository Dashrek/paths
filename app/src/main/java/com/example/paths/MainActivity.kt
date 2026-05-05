package com.example.paths

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



/*class StoperViewModel: ViewModel(){
    private val _sekundy = MutableStateFlow(0)
    private val _minuty = MutableStateFlow(0)
    private val _godziny = MutableStateFlow(0)
    private val _daneZPliku=MutableStateFlow<List<String>>(empty)
}*/
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val Padding : Modifier = Modifier.padding(horizontal = 5.dp, vertical = 5.dp);
            PathsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Row {
                        ButtonField("Trasy\nPiesze", modifier=Modifier.padding(innerPadding));
                        ButtonField("Trasy\nRowerowe", modifier = Modifier.padding(innerPadding));
                    }
                }
            }
        }
    }
}

//@Preview(showBackground = true)
@Composable
fun ButtonField(name: String, modifier: Modifier = Modifier, lambdacon: () -> Unit = {}) {
    var expanded by remember { mutableStateOf(false) }
    val items : List<String> = List(50) { "Element $name ${it + 1}" }
    Column(modifier = modifier.fillMaxHeight().padding(16.dp)) {
        Button(onClick = { expanded = !expanded }) {
            Text( if (expanded) "Zwiń: $name"
            else "Rozwiń: $name")
        }
        AnimatedVisibility(visible=expanded) {
            LazyColumn (
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(top = 8.dp)
                    .border(1.dp, Color.Gray)
            ) {
                items(items) { item ->
                    Text(item)
                    HorizontalDivider()
                }
            }
        }
    }

}

