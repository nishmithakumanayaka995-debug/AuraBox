package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AuraDatabase
import com.example.data.AuraRepository
import com.example.data.AuraSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class EcosystemType(val displayName: String, val primaryColorHex: Long, val secondaryColorHex: Long, val label: String) {
    NEON_OASIS("Neon Oasis", 0xFF00F0FF, 0xFF0044FF, "Cyan Grid"),
    SOLAR_FLARE("Solar Flare", 0xFFFF4500, 0xFFFF007F, "Thermal Core"),
    AURORA_VALLEY("Aurora Valley", 0xFFBD00FF, 0xFF5000FF, "Ethereal Wave"),
    CYBER_STORM("Cyber Storm", 0xFF39FF14, 0xFF00FFCC, "Matrix Pulse")
}

data class DiagnosticLog(
    val timestamp: Long = System.currentTimeMillis(),
    val source: String,
    val message: String,
    val level: String = "INFO"
)

class AuraViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AuraRepository
    val snapshots: StateFlow<List<AuraSnapshot>>

    // Live Ecosystem UI parameters
    var activeType by mutableStateOf(EcosystemType.NEON_OASIS)
        private set

    var pillarHeight by mutableFloatStateOf(0.6f)
        private set

    var auraIntensity by mutableFloatStateOf(0.75f)
        private set

    var climateIndex by mutableFloatStateOf(0.45f)
        private set

    var rotationAngle by mutableFloatStateOf(15f) // Rotation slider (0 - 360)
        private set

    // Diagnostics / logs
    var logs = mutableStateOf<List<DiagnosticLog>>(emptyList())
        private set

    init {
        val database = AuraDatabase.getDatabase(application)
        repository = AuraRepository(database.auraSnapshotDao())
        snapshots = repository.allSnapshots.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed initial diagnostic logs
        addLog("SYS_INIT", "AuraBox Matrix Engine initialized successfully.")
        addLog("LINK_STAT", "Neural sync channel secure at 128-bit quantum band.")
        addLog("ECO_MGR", "Micro-world stabilizer calibrated. Running at 100% capacity.")
    }

    fun updateEcosystemType(type: EcosystemType) {
        activeType = type
        addLog("THEME_CHG", "Ecosystem morphed to ${type.displayName}. Accent frequency set.", "CORE")
    }

    fun updatePillarHeight(height: Float) {
        pillarHeight = height
        if (Random.nextFloat() < 0.25f) {
            addLog("P_HEIGHT", "Pillar height calibrated: ${(height * 100).toInt()}%", "CALIB")
        }
    }

    fun updateAuraIntensity(intensity: Float) {
        auraIntensity = intensity
        if (Random.nextFloat() < 0.25f) {
            addLog("A_INTENS", "Luminescent pressure flux: ${(intensity * 100).toInt()}%", "GRID")
        }
    }

    fun updateClimateIndex(index: Float) {
        climateIndex = index
        if (Random.nextFloat() < 0.25f) {
            addLog("C_CLIM", "Ecosystem ambient thermal index set to ${(index * 50 + 10).toInt()}°C", "ENV")
        }
    }

    fun updateRotationAngle(angle: Float) {
        rotationAngle = angle
    }

    fun saveSnapshot(name: String) {
        viewModelScope.launch {
            val snapshot = AuraSnapshot(
                name = name.ifEmpty { "Snapshot #${Random.nextInt(100, 999)}" },
                ecosystemType = activeType.name,
                pillarHeight = pillarHeight,
                auraIntensity = auraIntensity,
                climateIndex = climateIndex,
                rotationAngle = rotationAngle
            )
            repository.insert(snapshot)
            addLog("SNAPSHOT", "Saved current core snapshot: '${snapshot.name}'", "SECURE")
        }
    }

    fun loadSnapshot(snapshot: AuraSnapshot) {
        viewModelScope.launch {
            try {
                val matchedType = EcosystemType.valueOf(snapshot.ecosystemType)
                activeType = matchedType
            } catch (e: Exception) {
                // Fallback if mismatch
            }
            pillarHeight = snapshot.pillarHeight
            auraIntensity = snapshot.auraIntensity
            climateIndex = snapshot.climateIndex
            rotationAngle = snapshot.rotationAngle
            addLog("SNAP_LOAD", "Restored matrix parameters from '${snapshot.name}'", "RESTORE")
        }
    }

    fun deleteSnapshot(id: Int, name: String) {
        viewModelScope.launch {
            repository.deleteById(id)
            addLog("SNAP_DEL", "Purged core snapshot: '$name'", "PURGE")
        }
    }

    fun addLog(source: String, message: String, level: String = "INFO") {
        val newLog = DiagnosticLog(source = source, message = message, level = level)
        val currentList = logs.value.toMutableList()
        currentList.add(0, newLog)
        if (currentList.size > 25) {
            currentList.removeAt(currentList.size - 1)
        }
        logs.value = currentList
    }
}
