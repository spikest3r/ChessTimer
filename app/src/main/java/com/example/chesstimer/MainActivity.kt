package com.example.chesstimer

import android.content.pm.ActivityInfo
import android.media.SoundPool
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
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
        var player1 by remember { mutableStateOf(false) }
        var player2 by remember { mutableStateOf(false) }
        var started by remember { mutableStateOf(false) }
        var allowStart by remember { mutableStateOf(false) }
        var runTimers by remember {mutableStateOf(true)}
        var p1Time by remember { mutableStateOf(Time(15,0)) }
        var p2Time by remember { mutableStateOf(Time(15,0)) }
        var ogTime by remember {mutableStateOf(Time(15,0))}
        var whiteMillis by remember {mutableStateOf(0)}
        var blackMillis by remember {mutableStateOf(0)}
        var isSettings by remember {mutableStateOf(false)}
        var increment by remember {mutableStateOf(0)}

        var p1SwitchText by remember { mutableStateOf("") }
        var p2SwitchText by remember { mutableStateOf("") }

        var p1Increment by remember {mutableStateOf(false)}
        var p2Increment by remember {mutableStateOf(false)}

        val func: (Boolean, Boolean) -> Unit = {w,b -> player1 = w; player2 = b}
        val setStart: (Boolean) -> Unit = {s -> started = s}
        val setLabels: (String, String) -> Unit = {a,b -> p1SwitchText = a; p2SwitchText = b}
        val timeToString: (Time) -> String = {t -> "%02d.%02d".format(t.minutes,t.seconds)}
        val setTime: (Time, Int) -> Unit = {time, inc -> p1Time = time; p2Time = time; increment = inc; ogTime = time}
        val reset: () -> Unit = {
            func(false, false)
            setStart(false)
            p1Increment = false
            p2Increment = false
            p1Time = ogTime
            p2Time = ogTime
            whiteMillis = 0
            blackMillis = 0
            runTimers = true
            allowStart = false
            setLabels("","")
        }

        var customMinute by remember{mutableStateOf("")}
        var customInc by remember{mutableStateOf("")}

        val context = LocalContext.current

        val soundPool = remember {
            SoundPool.Builder()
                .setMaxStreams(1)
                .build()
        }

        val beepId = remember {
            soundPool.load(context,R.raw.beep,1)
        }

        //make screen always on
        val view = LocalView.current
        DisposableEffect(view, started) {
            view.keepScreenOn = started
            onDispose { view.keepScreenOn = false }
        }

        //p1 timer
        LaunchedEffect(player1, runTimers) {
            if(player1) {
                if (!p1Increment) {
                    p1Increment = true
                } else {
                    if(increment > 0) {
                        var sec = p1Time.seconds + increment
                        var min = p1Time.minutes
                        if (sec >= 60) {
                            min += sec / 60
                            sec = sec % 60
                        }
                        p1Time = Time(min, sec)
                    }
                }
            }
            while(player1){
                if(!runTimers) break
                delay(100)
                whiteMillis-=100
                if(whiteMillis <= 0) {
                    whiteMillis = 1000
                    var seconds = p1Time.seconds - 1
                    var minutes = p1Time.minutes
                    if(minutes <= 0 && seconds <= 0) {
                        reset()
                        soundPool.play(beepId,1f,1f,1,0,1f)
                        break
                    }
                    if(seconds - 1 < 0) {
                        minutes--
                        seconds = 59
                    }
                    p1Time = Time(minutes, seconds)
                }
            }
        }

        //p2 timer
        LaunchedEffect(player2, runTimers) {
            if(player2) {
                if (!p2Increment) {
                    p2Increment = true
                } else {
                    if (increment > 0) {
                        var sec = p2Time.seconds + increment
                        var min = p2Time.minutes

                        if (sec >= 60) {
                            min += sec / 60
                            sec = sec % 60
                        }

                        p2Time = Time(min, sec)
                    }
                }
            }
            while(player2){
                if(!runTimers) break
                delay(100)
                blackMillis-=100
                if(blackMillis <= 0) {
                    blackMillis = 1000
                    var seconds = p2Time.seconds - 1
                    var minutes = p2Time.minutes
                    if(minutes <= 0 && seconds <= 0) {
                        reset()
                        soundPool.play(beepId,1f,1f,1,0,1f)
                        break
                    }
                    if(seconds - 1 < 0) {
                        minutes--
                        seconds = 59
                    }
                    p2Time = Time(minutes, seconds)
                }
            }
        }

        if(!isSettings) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                SideSwitch(Modifier.align(Alignment.TopCenter), player1, player1, player2, func, 0, setStart, runTimers,allowStart,p1SwitchText, setLabels)
                SideSwitch(Modifier.align(Alignment.BottomCenter), player2, player1, player2, func, 1, setStart, runTimers,allowStart,p2SwitchText, setLabels)
                Column(modifier = Modifier.align(Alignment.Center).rotate(90F)) {
                    Text(
                        timeToString(p1Time) + " ->",
                        fontSize = 48.sp,
                        color = if (player1) Color.Magenta else MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(32.dp))
                    Text(
                        "<- " + timeToString(p2Time),
                        fontSize = 48.sp,
                        color = if (player2) Color.Magenta else MaterialTheme.colorScheme.onBackground
                    )
                }
                Button(modifier = Modifier.rotate(90F).align(Alignment.CenterEnd), onClick = {
                    if(!allowStart) {
                        allowStart = true
                    } else {
                        runTimers = !runTimers
                        p1Increment = !runTimers
                        p2Increment = !runTimers
                    }
                }, enabled = !allowStart || started) {
                    Text(if(allowStart) if (runTimers) "PAUSE" else "RESUME" else "START")
                }
                Row(modifier = Modifier.rotate(90F).align(Alignment.CenterStart).offset(y = 64.dp)/*.padding(16.dp)*/) {
                    Button(onClick = {
                        reset()
                    }, enabled = allowStart) {
                        Text("RESET")
                    }
                    Spacer(Modifier.width(32.dp))
                    Button(onClick = {
                        isSettings = true
                    }, enabled = !allowStart) {
                        Text("SETTINGS")
                    }
                }
            }
        } else {
            val aba: () -> Unit = {isSettings = false}
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
                    SettingsTimeRow("Bullet", setTime, aba, arrayOf(
                        makeTs("1|0"),
                        makeTs("1|1"),
                        makeTs("2|1")
                    ))
                    SettingsTimeRow("Blitz", setTime, aba, arrayOf(
                        makeTs("3|0"),
                        makeTs("3|2"),
                        makeTs("5|0"),
                        makeTs("5|3")
                    ))
                    SettingsTimeRow("Rapid", setTime, aba, arrayOf(
                        makeTs("10|0"),
                        makeTs("10|10"),
                        makeTs("15|0"),
                        makeTs("20|0"),
                    ))
                    SettingsTimeRow("Classical", setTime, aba, arrayOf(
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
                            aba()
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
    fun SideSwitch(modifier: Modifier, myState: Boolean, p1: Boolean, p2: Boolean, stateChange: (Boolean, Boolean) -> Unit,
                   id: Int, setStarted: (Boolean) -> Unit, runTimers: Boolean, allowStart: Boolean, switchText: String,
                   setLabels: (String, String) -> Unit) {
        val context = LocalContext.current

        val soundPool = remember {
            SoundPool.Builder()
                .setMaxStreams(1)
                .build()
        }

        val soundId = remember {
            soundPool.load(context,R.raw.click1,1)
        }

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
                if(!allowStart) return@Button
                if(runTimers) {
                    if (!p1 && !p2) {
                        stateChange(id == 0, id == 1)
                        soundPool.play(soundId,1f,1f,1,0,1f);
                        setStarted(true)
                        setLabels(if(id == 1) "White" else "Black",
                            if(id == 0) "White" else "Black")
                    } else {
                        if (!myState) {
                            stateChange(p2, p1)
                            soundPool.play(soundId,1f,1f,1,0,1f);
                        }
                    }
                }
            }) {
            Text(switchText,modifier=Modifier.rotate(90f))
        }
    }
}