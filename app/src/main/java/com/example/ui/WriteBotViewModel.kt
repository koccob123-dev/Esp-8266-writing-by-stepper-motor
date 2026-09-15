package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.SettingsEntity
import com.example.data.db.WriteBotDatabase
import com.example.data.model.ConnectionStatus
import com.example.data.model.DrawBatch
import com.example.data.model.DrawCommand
import com.example.data.model.JobPreflightInfo
import com.example.data.network.PlotterRepository
import com.example.data.repository.SettingsRepository
import com.example.engine.PathOptimizer
import com.example.engine.StrokeFontEngine
import com.example.engine.StrokePath
import com.example.engine.StrokePoint
import com.example.engine.VectorFont
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class JogDirection {
    UP, DOWN, LEFT, RIGHT, HOME
}

data class UiSnackbarMessage(
    val message: String,
    val isError: Boolean = false
)

class WriteBotViewModel(application: Application) : AndroidViewModel(application) {

    private val database = WriteBotDatabase.getInstance(application)
    private val settingsRepository = SettingsRepository(database.settingsDao())
    private val plotterRepository = PlotterRepository()

    // Settings state
    private val _settings = MutableStateFlow(SettingsEntity())
    val settings: StateFlow<SettingsEntity> = _settings.asStateFlow()

    // Connection & machine telemetry
    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _posX = MutableStateFlow(0.0f)
    val posX: StateFlow<Float> = _posX.asStateFlow()

    private val _posY = MutableStateFlow(0.0f)
    val posY: StateFlow<Float> = _posY.asStateFlow()

    private val _isPenDown = MutableStateFlow(false)
    val isPenDown: StateFlow<Boolean> = _isPenDown.asStateFlow()

    private val _machineStatus = MutableStateFlow("IDLE")
    val machineStatus: StateFlow<String> = _machineStatus.asStateFlow()

    // Manual Jog Controls state
    private val _jogStepDistance = MutableStateFlow(10f)
    val jogStepDistance: StateFlow<Float> = _jogStepDistance.asStateFlow()

    private val _travelSpeed = MutableStateFlow(60)
    val travelSpeed: StateFlow<Int> = _travelSpeed.asStateFlow()

    fun setJogStepDistance(distance: Float) {
        _jogStepDistance.value = distance
        updateSettings(_settings.value.copy(jogStepDistance = distance))
    }

    fun setTravelSpeed(speed: Int) {
        _travelSpeed.value = speed
        updateSettings(_settings.value.copy(travelSpeed = speed))
    }

    // Feedback messages
    private val _snackbarMessages = MutableSharedFlow<UiSnackbarMessage>()
    val snackbarMessages: SharedFlow<UiSnackbarMessage> = _snackbarMessages.asSharedFlow()

    // Telemetry polling job
    private var pollingJob: Job? = null

    // Freehand Drawing Canvas State
    private val _canvasStrokes = MutableStateFlow<List<StrokePath>>(emptyList())
    val canvasStrokes: StateFlow<List<StrokePath>> = _canvasStrokes.asStateFlow()

    private val canvasUndoStack = mutableListOf<List<StrokePath>>()
    private val canvasRedoStack = mutableListOf<List<StrokePath>>()

    // Text-to-Writing State
    private val _textInput = MutableStateFlow("HELLO WORLD")
    val textInput: StateFlow<String> = _textInput.asStateFlow()

    private val _selectedFont = MutableStateFlow(VectorFont.HERSHEY_SANS)
    val selectedFont: StateFlow<VectorFont> = _selectedFont.asStateFlow()

    private val _fontSizeMm = MutableStateFlow(12f)
    val fontSizeMm: StateFlow<Float> = _fontSizeMm.asStateFlow()

    private val _letterSpacingMm = MutableStateFlow(2f)
    val letterSpacingMm: StateFlow<Float> = _letterSpacingMm.asStateFlow()

    private val _lineSpacingMm = MutableStateFlow(4f)
    val lineSpacingMm: StateFlow<Float> = _lineSpacingMm.asStateFlow()

    private val _writingSpeed = MutableStateFlow(40)
    val writingSpeed: StateFlow<Int> = _writingSpeed.asStateFlow()

    private val _textStartX = MutableStateFlow(10f)
    val textStartX: StateFlow<Float> = _textStartX.asStateFlow()

    private val _textStartY = MutableStateFlow(20f)
    val textStartY: StateFlow<Float> = _textStartY.asStateFlow()

    private val _textStrokes = MutableStateFlow<List<StrokePath>>(emptyList())
    val textStrokes: StateFlow<List<StrokePath>> = _textStrokes.asStateFlow()

    // Pre-flight Queue & Writing Execution
    private val _activePreflight = MutableStateFlow<JobPreflightInfo?>(null)
    val activePreflight: StateFlow<JobPreflightInfo?> = _activePreflight.asStateFlow()

    private val _isWritingActive = MutableStateFlow(false)
    val isWritingActive: StateFlow<Boolean> = _isWritingActive.asStateFlow()

    private val _writingProgress = MutableStateFlow(0f)
    val writingProgress: StateFlow<Float> = _writingProgress.asStateFlow()

    private val _writingStatusText = MutableStateFlow("")
    val writingStatusText: StateFlow<String> = _writingStatusText.asStateFlow()

    private var activeWritingJob: Job? = null

    init {
        // Collect saved settings from Room
        viewModelScope.launch {
            settingsRepository.settingsFlow.collectLatest { savedSettings ->
                _settings.value = savedSettings
                _writingSpeed.value = savedSettings.drawSpeed
                recalculateTextStrokes()
            }
        }
        recalculateTextStrokes()
    }

    // --- Connection Management ---

    fun connect(ip: String = _settings.value.ipAddress) {
        viewModelScope.launch {
            _connectionStatus.value = ConnectionStatus.Connecting
            val result = plotterRepository.pingAndGetStatus(ip)
            result.onSuccess { (statusResp, latency) ->
                _connectionStatus.value = ConnectionStatus.Connected(ip, latency)
                _posX.value = statusResp.x ?: 0f
                _posY.value = statusResp.y ?: 0f
                _isPenDown.value = statusResp.pen?.equals("down", ignoreCase = true) == true
                _machineStatus.value = statusResp.status?.uppercase() ?: "IDLE"
                _snackbarMessages.emit(UiSnackbarMessage("Connected to ESP8266 ($latency ms)"))
                startTelemetryPolling(ip)
            }.onFailure { err ->
                _connectionStatus.value = ConnectionStatus.Error(err.message ?: "Connection failed")
                _snackbarMessages.emit(UiSnackbarMessage("Connection failed: ${err.message}", isError = true))
                stopTelemetryPolling()
            }
        }
    }

    fun testConnection(ip: String) {
        viewModelScope.launch {
            _snackbarMessages.emit(UiSnackbarMessage("Pinging $ip..."))
            val result = plotterRepository.pingAndGetStatus(ip)
            result.onSuccess { (statusResp, latency) ->
                _snackbarMessages.emit(UiSnackbarMessage("Success: ESP8266 responded in ${latency}ms (Status: ${statusResp.status})"))
            }.onFailure { err ->
                _snackbarMessages.emit(UiSnackbarMessage("Ping failed: ${err.localizedMessage}", isError = true))
            }
        }
    }

    fun disconnect() {
        stopTelemetryPolling()
        _connectionStatus.value = ConnectionStatus.Disconnected
        _machineStatus.value = "DISCONNECTED"
    }

    private fun startTelemetryPolling(ip: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(3000)
                if (_isWritingActive.value) continue
                val result = plotterRepository.pingAndGetStatus(ip)
                result.onSuccess { (statusResp, latency) ->
                    _connectionStatus.value = ConnectionStatus.Connected(ip, latency)
                    statusResp.x?.let { _posX.value = it }
                    statusResp.y?.let { _posY.value = it }
                    statusResp.pen?.let { _isPenDown.value = it.equals("down", ignoreCase = true) }
                    statusResp.status?.let { _machineStatus.value = it.uppercase() }
                }.onFailure {
                    // Do not immediately disconnect on one transient poll drop, but notify if consecutive
                }
            }
        }
    }

    private fun stopTelemetryPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    // --- Machine Controls ---

    fun home() {
        val ip = _settings.value.ipAddress
        viewModelScope.launch {
            _machineStatus.value = "HOMING"
            val result = plotterRepository.home(ip)
            result.onSuccess {
                _posX.value = 0f
                _posY.value = 0f
                _isPenDown.value = false
                _machineStatus.value = "IDLE"
                _snackbarMessages.emit(UiSnackbarMessage("Machine homed successfully"))
            }.onFailure { err ->
                _machineStatus.value = "ERROR"
                _snackbarMessages.emit(UiSnackbarMessage("Home failed: ${err.message}", isError = true))
            }
        }
    }

    fun emergencyStop() {
        val ip = _settings.value.ipAddress
        activeWritingJob?.cancel()
        activeWritingJob = null
        _isWritingActive.value = false
        _writingStatusText.value = "EMERGENCY STOP TRIGGERED"

        viewModelScope.launch {
            _machineStatus.value = "STOPPED"
            val result = plotterRepository.stop(ip)
            result.onSuccess {
                _isPenDown.value = false
                _snackbarMessages.emit(UiSnackbarMessage("EMERGENCY STOP EXECUTED", isError = true))
            }.onFailure { err ->
                _snackbarMessages.emit(UiSnackbarMessage("Stop signal error: ${err.message}", isError = true))
            }
        }
    }

    fun setPen(down: Boolean) {
        val ip = _settings.value.ipAddress
        viewModelScope.launch {
            val result = plotterRepository.setPen(ip, down)
            result.onSuccess {
                _isPenDown.value = down
                _snackbarMessages.emit(UiSnackbarMessage("Pen ${if (down) "DOWN" else "UP"}"))
            }.onFailure { err ->
                _snackbarMessages.emit(UiSnackbarMessage("Pen toggle failed: ${err.message}", isError = true))
            }
        }
    }

    fun togglePen() {
        setPen(!_isPenDown.value)
    }

    fun jog(direction: JogDirection) {
        val ip = _settings.value.ipAddress
        val stepMm = _settings.value.jogStepDistance
        val speed = _settings.value.travelSpeed

        var deltaX = 0f
        var deltaY = 0f

        when (direction) {
            JogDirection.UP -> deltaY = -stepMm // Cartesian or plotter direction
            JogDirection.DOWN -> deltaY = stepMm
            JogDirection.LEFT -> deltaX = -stepMm
            JogDirection.RIGHT -> deltaX = stepMm
            JogDirection.HOME -> {
                home()
                return
            }
        }

        viewModelScope.launch {
            val result = plotterRepository.move(ip, deltaX, deltaY, speed)
            result.onSuccess {
                _posX.value = (_posX.value + deltaX).coerceIn(0f, _settings.value.machineWidth)
                _posY.value = (_posY.value + deltaY).coerceIn(0f, _settings.value.machineHeight)
            }.onFailure { err ->
                _snackbarMessages.emit(UiSnackbarMessage("Jog move error: ${err.message}", isError = true))
            }
        }
    }

    fun calibrate() {
        val ip = _settings.value.ipAddress
        viewModelScope.launch {
            _machineStatus.value = "CALIBRATING"
            val result = plotterRepository.calibrate(ip)
            result.onSuccess {
                _machineStatus.value = "IDLE"
                _posX.value = 0f
                _posY.value = 0f
                _snackbarMessages.emit(UiSnackbarMessage("Calibration completed. Steppers synchronized."))
            }.onFailure { err ->
                _machineStatus.value = "IDLE"
                _snackbarMessages.emit(UiSnackbarMessage("Calibration note: ${err.message}"))
            }
        }
    }

    // --- Settings Persistence ---

    fun updateSettings(newSettings: SettingsEntity) {
        viewModelScope.launch {
            settingsRepository.saveSettings(newSettings)
            _settings.value = newSettings
            recalculateTextStrokes()
            _snackbarMessages.emit(UiSnackbarMessage("Settings saved successfully"))
        }
    }

    fun syncConfigToEsp8266() {
        val s = _settings.value
        viewModelScope.launch {
            _snackbarMessages.emit(UiSnackbarMessage("Syncing configuration to ESP8266..."))
            val configMap = mapOf(
                "steps_per_mm_x" to s.stepsPerMmX,
                "steps_per_mm_y" to s.stepsPerMmY,
                "max_speed" to s.maxSpeed,
                "pen_up_angle" to s.penUpAngle,
                "pen_down_angle" to s.penDownAngle,
                "width" to s.machineWidth,
                "height" to s.machineHeight
            )
            val res = plotterRepository.sendConfig(s.ipAddress, configMap)
            res.onSuccess {
                _snackbarMessages.emit(UiSnackbarMessage("Config successfully applied to ESP8266!"))
            }.onFailure { err ->
                _snackbarMessages.emit(UiSnackbarMessage("Config saved locally. (ESP8266 returned: ${err.message})"))
            }
        }
    }

    // --- Canvas Freehand Drawing ---

    fun addCanvasStroke(rawPoints: List<StrokePoint>) {
        if (rawPoints.size < 2) return
        // Simplify stroke using RDP algorithm to eliminate high-frequency touch noise
        val simplified = PathOptimizer.simplifyPath(rawPoints, epsilonMm = 0.35f)
        if (simplified.size < 2) return

        canvasUndoStack.add(_canvasStrokes.value)
        canvasRedoStack.clear()
        _canvasStrokes.value = _canvasStrokes.value + listOf(simplified)
    }

    fun undoCanvas() {
        if (canvasUndoStack.isNotEmpty()) {
            canvasRedoStack.add(_canvasStrokes.value)
            _canvasStrokes.value = canvasUndoStack.removeAt(canvasUndoStack.size - 1)
        }
    }

    fun redoCanvas() {
        if (canvasRedoStack.isNotEmpty()) {
            canvasUndoStack.add(_canvasStrokes.value)
            _canvasStrokes.value = canvasRedoStack.removeAt(canvasRedoStack.size - 1)
        }
    }

    fun clearCanvas() {
        if (_canvasStrokes.value.isNotEmpty()) {
            canvasUndoStack.add(_canvasStrokes.value)
            canvasRedoStack.clear()
            _canvasStrokes.value = emptyList()
        }
    }

    fun prepareCanvasForPlotting() {
        val strokes = _canvasStrokes.value
        if (strokes.isEmpty()) {
            viewModelScope.launch {
                _snackbarMessages.emit(UiSnackbarMessage("Canvas is empty. Draw something first!", isError = true))
            }
            return
        }

        // Optimize stroke order to reduce pen-up rapid moves
        val optimized = PathOptimizer.optimizeStrokeOrder(strokes)
        val s = _settings.value

        val preflight = PathOptimizer.calculatePreflight(
            title = "Freehand Drawing",
            strokes = optimized,
            machineWidthMm = s.machineWidth,
            machineHeightMm = s.machineHeight,
            drawSpeedMmS = s.drawSpeed.toFloat(),
            travelSpeedMmS = s.travelSpeed.toFloat()
        )
        _activePreflight.value = preflight
    }

    // --- Text-to-Writing Engine ---

    fun updateTextInput(newText: String) {
        _textInput.value = newText
        recalculateTextStrokes()
    }

    fun updateFontSize(sizeMm: Float) {
        _fontSizeMm.value = sizeMm
        recalculateTextStrokes()
    }

    fun updateLetterSpacing(spacingMm: Float) {
        _letterSpacingMm.value = spacingMm
        recalculateTextStrokes()
    }

    fun updateLineSpacing(spacingMm: Float) {
        _lineSpacingMm.value = spacingMm
        recalculateTextStrokes()
    }

    fun updateTextPosition(xMm: Float, yMm: Float) {
        _textStartX.value = xMm.coerceIn(0f, _settings.value.machineWidth - 10f)
        _textStartY.value = yMm.coerceIn(0f, _settings.value.machineHeight - 10f)
        recalculateTextStrokes()
    }

    fun updateWritingSpeed(speed: Int) {
        _writingSpeed.value = speed
    }

    fun updateSelectedFont(font: VectorFont) {
        _selectedFont.value = font
        recalculateTextStrokes()
    }

    fun clearTextInput() {
        _textInput.value = ""
        recalculateTextStrokes()
    }

    private fun recalculateTextStrokes() {
        val s = _settings.value
        val strokes = StrokeFontEngine.vectorizeText(
            text = _textInput.value,
            fontSizeMm = _fontSizeMm.value,
            letterSpacingMm = _letterSpacingMm.value,
            lineSpacingMm = _lineSpacingMm.value,
            startX = _textStartX.value,
            startY = _textStartY.value,
            maxBedWidth = s.machineWidth,
            maxBedHeight = s.machineHeight,
            font = _selectedFont.value
        )
        _textStrokes.value = strokes
    }

    fun prepareTextForPlotting() {
        val strokes = _textStrokes.value
        if (strokes.isEmpty()) {
            viewModelScope.launch {
                _snackbarMessages.emit(UiSnackbarMessage("No text strokes generated.", isError = true))
            }
            return
        }

        val optimized = PathOptimizer.optimizeStrokeOrder(strokes)
        val s = _settings.value

        val preflight = PathOptimizer.calculatePreflight(
            title = "Text Writing: \"${_textInput.value.take(20)}\"",
            strokes = optimized,
            machineWidthMm = s.machineWidth,
            machineHeightMm = s.machineHeight,
            drawSpeedMmS = _writingSpeed.value.toFloat(),
            travelSpeedMmS = s.travelSpeed.toFloat()
        )
        _activePreflight.value = preflight
    }

    // --- Job Queue & Machine Execution ---

    fun dismissPreflight() {
        _activePreflight.value = null
    }

    fun startWritingActiveJob() {
        val preflight = _activePreflight.value ?: return
        val ip = _settings.value.ipAddress

        activeWritingJob?.cancel()
        _isWritingActive.value = true
        _writingProgress.value = 0f
        _writingStatusText.value = "Initiating transmission..."
        _machineStatus.value = "DRAWING"

        activeWritingJob = viewModelScope.launch {
            val result = plotterRepository.sendDrawingInBatches(
                ip = ip,
                commands = preflight.commands,
                chunkSize = 35
            ) { sent, total ->
                val fraction = sent.toFloat() / total.toFloat()
                _writingProgress.value = fraction
                _writingStatusText.value = "Plotting... $sent / $total points (${(fraction * 100).toInt()}%)"
            }

            result.onSuccess {
                _writingProgress.value = 1.0f
                _writingStatusText.value = "Writing finished successfully!"
                _machineStatus.value = "IDLE"
                _isPenDown.value = false
                _isWritingActive.value = false
                _snackbarMessages.emit(UiSnackbarMessage("Job \"${preflight.title}\" complete!"))
                delay(1500)
                _activePreflight.value = null
            }.onFailure { err ->
                _isWritingActive.value = false
                _machineStatus.value = "ERROR"
                _writingStatusText.value = "Transmission halted: ${err.message}"
                _snackbarMessages.emit(UiSnackbarMessage("Plotting error: ${err.message}", isError = true))
            }
        }
    }

    fun cancelWritingActiveJob() {
        activeWritingJob?.cancel()
        activeWritingJob = null
        _isWritingActive.value = false
        _writingStatusText.value = "Canceled by user."
        _activePreflight.value = null
        emergencyStop()
    }
}
