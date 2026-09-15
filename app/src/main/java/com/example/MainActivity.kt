package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ControlCamera
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Draw
import com.example.ui.screens.ManualJogScreen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.WriteBotViewModel
import com.example.ui.components.CompactEmergencyStopButton
import com.example.ui.components.QueuePreflightDialog
import com.example.ui.components.StatusChip
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DrawingCanvasScreen
import com.example.ui.screens.FirmwareSpecScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TextToWritingScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.flow.collectLatest

enum class AppTab(val title: String, val icon: ImageVector, val tag: String) {
    HOME("Home", Icons.Default.Home, "tab_home"),
    DRAW("Draw", Icons.Default.Draw, "tab_draw"),
    TEXT("Text", Icons.Default.TextFields, "tab_text"),
    MANUAL("Manual", Icons.Default.ControlCamera, "tab_manual"),
    SETTINGS("Settings", Icons.Default.Settings, "tab_settings")
}

class MainActivity : ComponentActivity() {
    private val viewModel: WriteBotViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                WriteBotApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteBotApp(viewModel: WriteBotViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Telemetry & settings state
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()
    val posX by viewModel.posX.collectAsStateWithLifecycle()
    val posY by viewModel.posY.collectAsStateWithLifecycle()
    val isPenDown by viewModel.isPenDown.collectAsStateWithLifecycle()
    val machineState by viewModel.machineStatus.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    // Canvas state
    val canvasStrokes by viewModel.canvasStrokes.collectAsStateWithLifecycle()

    // Text state
    val textInput by viewModel.textInput.collectAsStateWithLifecycle()
    val fontSizeMm by viewModel.fontSizeMm.collectAsStateWithLifecycle()
    val letterSpacingMm by viewModel.letterSpacingMm.collectAsStateWithLifecycle()
    val lineSpacingMm by viewModel.lineSpacingMm.collectAsStateWithLifecycle()
    val writingSpeed by viewModel.writingSpeed.collectAsStateWithLifecycle()
    val textStartX by viewModel.textStartX.collectAsStateWithLifecycle()
    val textStartY by viewModel.textStartY.collectAsStateWithLifecycle()
    val textStrokes by viewModel.textStrokes.collectAsStateWithLifecycle()
    val selectedFont by viewModel.selectedFont.collectAsStateWithLifecycle()
    val jogStepDistance by viewModel.jogStepDistance.collectAsStateWithLifecycle()
    val jogSpeed by viewModel.travelSpeed.collectAsStateWithLifecycle()

    // Preflight Queue state
    val activePreflight by viewModel.activePreflight.collectAsStateWithLifecycle()
    val isWritingActive by viewModel.isWritingActive.collectAsStateWithLifecycle()
    val writingProgress by viewModel.writingProgress.collectAsStateWithLifecycle()
    val writingStatusText by viewModel.writingStatusText.collectAsStateWithLifecycle()

    // Snackbar event collector
    LaunchedEffect(Unit) {
        viewModel.snackbarMessages.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg.message)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            WriteBotTopBar(
                machineState = machineState,
                onEmergencyStop = { viewModel.emergencyStop() }
            )
        },
        bottomBar = {
            WriteBotBottomNavigation(
                selectedTabIndex = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (AppTab.values()[selectedTab]) {
                AppTab.HOME -> DashboardScreen(
                    connectionStatus = connectionStatus,
                    posX = posX,
                    posY = posY,
                    isPenDown = isPenDown,
                    machineState = machineState,
                    settings = settings,
                    onConnect = { viewModel.connect(it) },
                    onTestConnection = { viewModel.testConnection(it) },
                    onDisconnect = { viewModel.disconnect() },
                    onHome = { viewModel.home() },
                    onEmergencyStop = { viewModel.emergencyStop() },
                    onSetPen = { viewModel.setPen(it) },
                    onCalibrate = { viewModel.calibrate() }
                )

                AppTab.DRAW -> DrawingCanvasScreen(
                    strokes = canvasStrokes,
                    settings = settings,
                    onStrokeDrawn = { viewModel.addCanvasStroke(it) },
                    onUndo = { viewModel.undoCanvas() },
                    onRedo = { viewModel.redoCanvas() },
                    onClear = { viewModel.clearCanvas() },
                    onSendToMachine = { viewModel.prepareCanvasForPlotting() }
                )

                AppTab.TEXT -> TextToWritingScreen(
                    textInput = textInput,
                    selectedFont = selectedFont,
                    fontSizeMm = fontSizeMm,
                    letterSpacingMm = letterSpacingMm,
                    lineSpacingMm = lineSpacingMm,
                    writingSpeed = writingSpeed,
                    startX = textStartX,
                    startY = textStartY,
                    textStrokes = textStrokes,
                    settings = settings,
                    onTextChanged = { viewModel.updateTextInput(it) },
                    onFontChanged = { viewModel.updateSelectedFont(it) },
                    onFontSizeChanged = { viewModel.updateFontSize(it) },
                    onLetterSpacingChanged = { viewModel.updateLetterSpacing(it) },
                    onLineSpacingChanged = { viewModel.updateLineSpacing(it) },
                    onWritingSpeedChanged = { viewModel.updateWritingSpeed(it) },
                    onPositionChanged = { x, y -> viewModel.updateTextPosition(x, y) },
                    onClearText = { viewModel.clearTextInput() },
                    onSendToMachine = { viewModel.prepareTextForPlotting() }
                )

                AppTab.MANUAL -> ManualJogScreen(
                    connectionStatus = connectionStatus,
                    posX = posX,
                    posY = posY,
                    isPenDown = isPenDown,
                    machineState = machineState,
                    settings = settings,
                    onJog = { viewModel.jog(it) },
                    onHome = { viewModel.home() },
                    onSetPen = { viewModel.setPen(it) },
                    onEmergencyStop = { viewModel.emergencyStop() },
                    onStepDistanceSelected = { viewModel.setJogStepDistance(it) },
                    onSpeedChanged = { viewModel.setTravelSpeed(it) }
                )

                AppTab.SETTINGS -> SettingsScreen(
                    currentSettings = settings,
                    onSaveSettings = { viewModel.updateSettings(it) },
                    onSyncToMachine = { viewModel.syncConfigToEsp8266() }
                )
            }

            // Pre-flight Queue Modal Dialog
            activePreflight?.let { preflight ->
                QueuePreflightDialog(
                    preflight = preflight,
                    isConnected = connectionStatus is com.example.data.model.ConnectionStatus.Connected,
                    isWritingActive = isWritingActive,
                    writingProgress = writingProgress,
                    writingStatusText = writingStatusText,
                    onStartWriting = { viewModel.startWritingActiveJob() },
                    onCancelWriting = { viewModel.cancelWritingActiveJob() },
                    onDismiss = { viewModel.dismissPreflight() }
                )
            }
        }
    }
}

@Composable
fun WriteBotTopBar(
    machineState: String,
    onEmergencyStop: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Plotter reticle logo
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, NeonCyan, RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(NeonCyan)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "WRITEBOT",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                    Text(
                        text = "ESP8266 XY PLOTTER",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NeonCyan,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Right side: Compact Emergency Stop button
            CompactEmergencyStopButton(
                onStopClicked = onEmergencyStop
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DarkOutline)
        )
    }
}

@Composable
fun WriteBotBottomNavigation(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = DarkSurface,
        tonalElevation = 0.dp,
        modifier = Modifier.border(1.dp, DarkOutline, RoundedCornerShape(0.dp))
    ) {
        AppTab.values().forEachIndexed { index, tab ->
            val isSelected = selectedTabIndex == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NeonCyan,
                    selectedTextColor = NeonCyan,
                    indicatorColor = DarkSurfaceVariant,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                ),
                modifier = Modifier.testTag(tab.tag)
            )
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Android") }
}

