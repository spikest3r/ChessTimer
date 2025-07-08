 package com.example.chesstimer

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chesstimer.ui.theme.ChessTimerTheme
import kotlinx.coroutines.delay

 class MainActivity : ComponentActivity() {
    data class Time(var minutes: Int, var seconds: Int)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChessTimerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    App()
                }
            }
        }
    }

    @Composable
    fun App() {
        var white by remember { mutableStateOf(false) }
        var black by remember { mutableStateOf(false) }
        var started by remember { mutableStateOf(false) }
        var whiteTime by remember { mutableStateOf(Time(15,0)) }
        var blackTime by remember { mutableStateOf(Time(15,0)) }
        var whiteMillis by remember {mutableStateOf(0)}
        var blackMillis by remember {mutableStateOf(0)}

        val func: (Boolean, Boolean) -> Unit = {w,b -> white = w; black = b}
        val setStart: (Boolean) -> Unit = {s -> started = s}
        val timeToString: (Time) -> String = {t -> "%02d.%02d".format(t.minutes,t.seconds)}

        //white timer
        LaunchedEffect(white) {
            while(white){
                delay(100)
                whiteMillis-=100
                if(whiteMillis <= 0) {
                    whiteMillis = 1000
                    var seconds = whiteTime.seconds - 1
                    var minutes = whiteTime.minutes
                    if(seconds - 1 < 0) {
                        minutes--
                        seconds = 59
                    }
                    whiteTime = Time(minutes, seconds)
                }
            }
        }

        //black timer
        LaunchedEffect(black) {
            while(black){
                delay(100)
                blackMillis-=100
                if(blackMillis <= 0) {
                    blackMillis = 1000
                    var seconds = blackTime.seconds - 1
                    var minutes = blackTime.minutes
                    if(seconds - 1 < 0) {
                        minutes--
                        seconds = 59
                    }
                    blackTime = Time(minutes, seconds)
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            SideSwitch(Modifier.align(Alignment.TopCenter), white, white, black, func, 0, setStart)
            SideSwitch(Modifier.align(Alignment.BottomCenter), black, white, black, func, 1, setStart)
            Column(modifier=Modifier.align(Alignment.Center).rotate(90F)) {
                Text(timeToString(whiteTime) + " ->", fontSize = 48.sp, color = if(white) Color.Magenta else Color.White)
                Spacer(Modifier.height(32.dp))
                Text("<- " + timeToString(blackTime), fontSize = 48.sp, color = if(black) Color.Magenta else Color.White)
            }
            Row(modifier = Modifier.rotate(90F).align(Alignment.CenterStart)/*.offset(y = 32.dp)*/.padding(16.dp)) {
                Button(onClick = {
                    func(false,false)
                    setStart(false)
                    whiteTime = Time(15,0)
                    blackTime = Time(15,0)
                    whiteMillis = 0
                    blackMillis = 0
                }, enabled = started) {
                    Text("RESET")
                }
                // TODO: Make pause feature
//                Spacer(Modifier.width(32.dp))
//                Button(onClick = {}, enabled = started) {
//                    Text("PAUSE")
//                }
            }
        }
    }

    @Composable
    fun SideSwitch(modifier: Modifier, myState: Boolean, white: Boolean, black: Boolean, stateChange: (Boolean, Boolean) -> Unit,
                   id: Int, setStarted: (Boolean) -> Unit) {
        Button(
            modifier = modifier
                .width(300.dp)
                .height(300.dp)
                .padding(64.dp),
            shape = RectangleShape,
            colors = ButtonColors(
                if (myState) Color.DarkGray else Color.Gray,
                Color.White,
                Color.White,
                Color.White
            ),
            onClick = {
                if(!white && !black) {
                    stateChange(id == 0, id == 1)
                    setStarted(true)
                } else
                if (!myState) {
                    stateChange(black, white)
                }
            }) {}
    }

}