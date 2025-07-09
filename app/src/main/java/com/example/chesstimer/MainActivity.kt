package com.example.chesstimer

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chesstimer.ui.theme.ChessTimerTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    data class Time(var minutes: Int, var seconds: Int)
    data class TimeSetting(var time: Time, var inc: Int, var btnText: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT // will be fixed later
        setContent {
            ChessTimerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    App()
                }
            }
        }
    }

    fun makeTs(str: String): TimeSetting {
        val rawData = str.split('|')
        if(rawData.size > 0) {
            return TimeSetting(Time(rawData[0].toInt(),0),rawData[1].toInt(), str)
        }
        return TimeSetting(Time(0,0),0,"0|0") // default
    }

    @Composable
    fun App() {
        var white by remember { mutableStateOf(false) }
        var black by remember { mutableStateOf(false) }
        var started by remember { mutableStateOf(false) }
        var whiteTime by remember { mutableStateOf(Time(15,0)) }
        var blackTime by remember { mutableStateOf(Time(15,0)) }
        var ogTime by remember {mutableStateOf(Time(15,0))}
        var whiteMillis by remember {mutableStateOf(0)}
        var blackMillis by remember {mutableStateOf(0)}
        var isSettings by remember {mutableStateOf(false)}
        var increment by remember {mutableStateOf(0)}

        val func: (Boolean, Boolean) -> Unit = {w,b -> white = w; black = b}
        val setStart: (Boolean) -> Unit = {s -> started = s}
        val timeToString: (Time) -> String = {t -> "%02d.%02d".format(t.minutes,t.seconds)}
        val setTime: (Time, Int) -> Unit = {time, inc -> whiteTime = time; blackTime = time; increment = inc; ogTime = time}
        val reset: () -> Unit = {
            func(false, false)
            setStart(false)
            whiteTime = ogTime
            blackTime = ogTime
            whiteMillis = 0
            blackMillis = 0
        }

        var customMinute by remember{mutableStateOf("")}
        var customInc by remember{mutableStateOf("")}

        //make screen always on
        val view = LocalView.current
        DisposableEffect(view, started) {
            view.keepScreenOn = started
            onDispose { view.keepScreenOn = false }
        }

        //white timer
        var whiteIncrement by remember {mutableStateOf(false)}
        LaunchedEffect(white) {
            if(white) {
                if (!whiteIncrement) {
                    whiteIncrement = true
                } else {
                    if(increment > 0) {
                        var sec = whiteTime.seconds + increment
                        var min = whiteTime.minutes
                        if (sec >= 60) {
                            min += sec / 60
                            sec = sec % 60
                        }
                        whiteTime = Time(min, sec)
                    }
                }
            }
            while(white){
                delay(100)
                whiteMillis-=100
                if(whiteMillis <= 0) {
                    whiteMillis = 1000
                    var seconds = whiteTime.seconds - 1
                    var minutes = whiteTime.minutes
                    if(minutes <= 0 && seconds <= 0) {
                        reset()
                        break
                    }
                    if(seconds - 1 < 0) {
                        minutes--
                        seconds = 59
                    }
                    whiteTime = Time(minutes, seconds)
                }
            }
        }

        //black timer
        var blackIncrement by remember {mutableStateOf(false)}
        LaunchedEffect(black) {
            if(black) {
                if (!blackIncrement) {
                    blackIncrement = true
                } else {
                    if (increment > 0) {
                        var sec = blackTime.seconds + increment
                        var min = blackTime.minutes

                        if (sec >= 60) {
                            min += sec / 60
                            sec = sec % 60
                        }

                        blackTime = Time(min, sec)
                    }
                }
            }
            while(black){
                delay(100)
                blackMillis-=100
                if(blackMillis <= 0) {
                    blackMillis = 1000
                    var seconds = blackTime.seconds - 1
                    var minutes = blackTime.minutes
                    if(minutes <= 0 && seconds <= 0) {
                        reset()
                        break
                    }
                    if(seconds - 1 < 0) {
                        minutes--
                        seconds = 59
                    }
                    blackTime = Time(minutes, seconds)
                }
            }
        }

        if(!isSettings) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                SideSwitch(Modifier.align(Alignment.TopCenter), white, white, black, func, 0, setStart)
                SideSwitch(Modifier.align(Alignment.BottomCenter), black, white, black, func, 1, setStart)
                Column(modifier = Modifier.align(Alignment.Center).rotate(90F)) {
                    Text(
                        timeToString(whiteTime) + " ->",
                        fontSize = 48.sp,
                        color = if (white) Color.Magenta else Color.White
                    )
                    Spacer(Modifier.height(32.dp))
                    Text(
                        "<- " + timeToString(blackTime),
                        fontSize = 48.sp,
                        color = if (black) Color.Magenta else Color.White
                    )
                }
                Row(modifier = Modifier.rotate(90F).align(Alignment.CenterStart).offset(y = 48.dp)/*.padding(16.dp)*/) {
                    Button(onClick = {
                        reset()
                    }, enabled = started) {
                        Text("RESET")
                    }
                    // TODO: Make pause feature
//                Spacer(Modifier.width(32.dp))
//                Button(onClick = {}, enabled = started) {
//                    Text("PAUSE")
//                }
                    Spacer(Modifier.width(32.dp))
                    Button(onClick = {
                        isSettings = true
                    }, enabled = !started) {
                        Text("SETTINGS")
                    }
                }
            }
        } else {
            val abd: () -> Unit = {isSettings = false}
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Text("Settings", modifier = Modifier.align(Alignment.TopCenter).padding(64.dp), fontSize = 32.sp)
                Button(onClick = {
                    isSettings = false
                }, modifier = Modifier.align(Alignment.BottomCenter).padding(80.dp)) {
                    Text("Go back")
                }
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    SettingsTimeRow("Bullet", setTime, abd, arrayOf(
                        makeTs("1|0"),
                        makeTs("1|1"),
                        makeTs("2|1")
                    ))
                    SettingsTimeRow("Blitz", setTime, abd, arrayOf(
                        makeTs("3|0"),
                        makeTs("3|2"),
                        makeTs("5|0"),
                        makeTs("5|3")
                    ))
                    SettingsTimeRow("Rapid", setTime, abd, arrayOf(
                        makeTs("10|0"),
                        makeTs("10|10"),
                        makeTs("15|0"),
                        makeTs("20|0"),
                    ))
                    SettingsTimeRow("Classical", setTime, abd, arrayOf(
                        makeTs("30|0"),
                        makeTs("40|0"),
                        makeTs("60|0")
                    ))
                    Text("Custom", fontSize = 24.sp)
                    Spacer(Modifier.height(8.dp))
                    Row {
                        TextField(
                            value = customMinute,
                            onValueChange = { input ->
                                val filtered = input.filter { it.isDigit() }
                                customMinute = filtered
                            },
                            label = { Text("min.") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(64.dp)
                        )
                        Spacer(Modifier.width(32.dp))
                        TextField(
                            value = customInc,
                            onValueChange = { input ->
                                val filtered = input.filter { it.isDigit() }
                                customInc = filtered
                            },
                            label = { Text("incr.") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(64.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Button(onClick = {
                            setTime(Time(customMinute.toInt(),0), customInc.toInt())
                            abd()
                        }) {
                            Text("Set custom")
                        }
                    }
                }
            }
        }
    }

     @Composable
     fun SettingsTimeRow(title:String, setTime: (Time, Int) -> Unit, additionalButtonAction: () -> Unit, data: Array<TimeSetting>) {
         Text(title, fontSize = 24.sp)
         Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
             for(ts in data) {
                 Button(onClick = {
                     setTime(ts.time, ts.inc)
                     additionalButtonAction()
                 }) {
                     Text(ts.btnText)
                 }
             }
         }
         Spacer(Modifier.height(32.dp))
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