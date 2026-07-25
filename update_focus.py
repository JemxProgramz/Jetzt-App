import re

with open('app/src/main/java/com/example/ui/FocusScreen.kt', 'r') as f:
    content = f.read()

replacement_1 = """    val stats by viewModel.stats.collectAsState()
    val currentStats = stats ?: Stats()
    
    var mode by remember { mutableStateOf("pomodoro") }
    var isActive by remember { mutableStateOf(false) }
    
    var pomodoroMinutes by remember { mutableIntStateOf(25) }
    var shortBreakMinutes by remember { mutableIntStateOf(5) }
    var longBreakMinutes by remember { mutableIntStateOf(15) }
    var showSettings by remember { mutableStateOf(false) }
    
    val timers = mapOf(
        "pomodoro" to pomodoroMinutes * 60,
        "shortBreak" to shortBreakMinutes * 60,
        "longBreak" to longBreakMinutes * 60
    )
    
    var timeLeft by remember(mode, pomodoroMinutes, shortBreakMinutes, longBreakMinutes) { mutableIntStateOf(timers[mode] ?: (25 * 60)) }
    
    LaunchedEffect(isActive, timeLeft) {
        if (isActive && timeLeft > 0) {
            delay(1000L)
            timeLeft -= 1
        } else if (isActive && timeLeft == 0) {
            isActive = false
            if (mode == "pomodoro") {
                viewModel.addFocusSession((timers["pomodoro"] ?: 0) / 3600f)
                mode = "shortBreak"
            } else {
                mode = "pomodoro"
            }
        }
    }
"""

content = re.sub(r'    val stats by viewModel.stats.collectAsState().*?    LaunchedEffect\(isActive, timeLeft\) \{.*?\n        \}\n    \}', replacement_1, content, flags=re.DOTALL)

replacement_2 = """            IconButton(
                onClick = { showSettings = true },
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Slate800)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Slate400)
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
    }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("Timer Settings", color = Color.White) },
            text = {
                Column {
                    Text("Pomodoro (minutes)", color = Slate300)
                    Slider(
                        value = pomodoroMinutes.toFloat(),
                        onValueChange = { pomodoroMinutes = it.toInt() },
                        valueRange = 1f..60f,
                        steps = 59
                    )
                    Text("${pomodoroMinutes}m", color = Color.White)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Short Break (minutes)", color = Slate300)
                    Slider(
                        value = shortBreakMinutes.toFloat(),
                        onValueChange = { shortBreakMinutes = it.toInt() },
                        valueRange = 1f..30f,
                        steps = 29
                    )
                    Text("${shortBreakMinutes}m", color = Color.White)
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettings = false }) {
                    Text("Done", color = Indigo400)
                }
            },
            containerColor = Slate900,
            titleContentColor = Color.White,
            textContentColor = Slate300
        )
    }
}"""

content = re.sub(r'            IconButton\(\n                onClick = \{ /\* Open Settings \*/ \},.*?    \}\n\}', replacement_2, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/FocusScreen.kt', 'w') as f:
    f.write(content)
