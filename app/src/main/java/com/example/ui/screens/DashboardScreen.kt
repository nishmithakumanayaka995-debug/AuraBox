package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AuraSnapshot
import com.example.ui.components.IsometricGlassCube
import com.example.viewmodel.DiagnosticLog
import com.example.viewmodel.EcosystemType
import com.example.viewmodel.AuraViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AuraViewModel,
    modifier: Modifier = Modifier
) {
    val snapshots by viewModel.snapshots.collectAsStateWithLifecycle()
    var showSaveDialog by remember { mutableStateOf(false) }
    var snapshotNameInput by remember { mutableStateOf("") }
    
    // Core state links
    val activeType = viewModel.activeType
    val primaryColor = Color(activeType.primaryColorHex)
    val secondaryColor = Color(activeType.secondaryColorHex)

    // Soft animated glow intensity
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_glow")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_glow_pulse"
    )

    // Save snapshot dialog
    if (showSaveDialog) {
        SaveSnapshotDialog(
            initialName = "Snapshot ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}",
            onDismiss = { showSaveDialog = false },
            onConfirm = { name ->
                viewModel.saveSnapshot(name)
                showSaveDialog = false
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF0C0B18) // deep dark purple-black space canvas
    ) { innerPadding ->
        // Starfield / Grid overlay background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .drawBehind {
                    // Draw futuristic ambient dark-purple radial glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(primaryColor.copy(alpha = 0.15f * pulseGlow), Color.Transparent),
                            center = center,
                            radius = size.maxDimension * 0.7f
                        ),
                        center = center,
                        radius = size.maxDimension * 0.7f
                    )
                }
        ) {
            // Main layout in a scrollable column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. TOP HEADER BAR
                HeaderBar(activeType = activeType)

                // 2. 3D GLASS CUBE CONTAINER
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(310.dp)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IsometricGlassCube(
                        modifier = Modifier.fillMaxSize(),
                        activeType = activeType,
                        pillarHeight = viewModel.pillarHeight,
                        auraIntensity = viewModel.auraIntensity,
                        climateIndex = viewModel.climateIndex,
                        rotationAngle = viewModel.rotationAngle
                    )
                    
                    // Core Synchronizer label
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x33000000))
                            .border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 4.dp)
                    ) {
                        Text(
                            text = "PERSPECTIVE ROTATION: ${viewModel.rotationAngle.toInt()}°",
                            color = primaryColor.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 3. ECOSYSTEM THEME SELECTION TABS
                ThemeSelectionSection(
                    activeType = activeType,
                    onTypeSelected = { viewModel.updateEcosystemType(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4. INTERACTIVE MATRIX CONTROLS
                MatrixControlSliders(viewModel = viewModel, primaryColor = primaryColor)

                Spacer(modifier = Modifier.height(24.dp))

                // 5. BOTTOM DIAGNOSTICS & PERSISTED SNAPSHOTS (WHITE/BLUE FROSTED GRADIENT PANEL)
                FrostedGradientDiagnosticsPanel(
                    snapshots = snapshots,
                    logs = viewModel.logs.value,
                    primaryColor = primaryColor,
                    onSaveClicked = { showSaveDialog = true },
                    onLoadClicked = { viewModel.loadSnapshot(it) },
                    onDeleteClicked = { snapshot -> viewModel.deleteSnapshot(snapshot.id, snapshot.name) }
                )
            }
        }
    }
}

@Composable
fun HeaderBar(
    activeType: EcosystemType,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(activeType.primaryColorHex)
    
    // Heartbeat anim for status LED
    val infiniteTransition = rememberInfiniteTransition(label = "heartbeat")
    val ledAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "led_alpha"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = ledAlpha))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AURABOX // PROTO-LINK_ACTIVE",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
            Text(
                text = "NEURAL COGNITION STATE: ${activeType.label.uppercase()}",
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
        }

        // Circular dynamic gauge
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFF151429))
                .border(1.dp, primaryColor.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "94%",
                    color = primaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "SYNC",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 7.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun ThemeSelectionSection(
    activeType: EcosystemType,
    onTypeSelected: (EcosystemType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "ECOSYSTEM MODULE SELECTOR",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            EcosystemType.values().forEach { type ->
                val isSelected = type == activeType
                val typeColor = Color(type.primaryColorHex)
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) typeColor.copy(alpha = 0.15f)
                            else Color(0xFF131226)
                        )
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) typeColor else Color(0x33FFFFFF),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onTypeSelected(type) }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Soft glowing backdrop for custom icons
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(typeColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (type) {
                                    EcosystemType.NEON_OASIS -> Icons.Default.Forest
                                    EcosystemType.SOLAR_FLARE -> Icons.Default.LocalFireDepartment
                                    EcosystemType.AURORA_VALLEY -> Icons.Default.Air
                                    EcosystemType.CYBER_STORM -> Icons.Default.ElectricBolt
                                },
                                contentDescription = type.displayName,
                                tint = typeColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            text = type.displayName,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MatrixControlSliders(
    viewModel: AuraViewModel,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF111024))
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(24.dp))
            .padding(18.dp)
    ) {
        Text(
            text = "MATRIX QUANTUM FLUX CONTROLS",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 1. Pillar Height (Energy Core)
        MatrixSliderItem(
            label = "CORE ENERGY OUTPUT (PILLARS)",
            value = viewModel.pillarHeight,
            onValueChange = { viewModel.updatePillarHeight(it) },
            icon = Icons.Default.Bolt,
            color = primaryColor,
            valueString = "${(viewModel.pillarHeight * 100).toInt()}%"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Aura Intensity (Luminescence)
        MatrixSliderItem(
            label = "LUMINESCENT AMBIENT PRESS (AURA)",
            value = viewModel.auraIntensity,
            onValueChange = { viewModel.updateAuraIntensity(it) },
            icon = Icons.Default.WbSunny,
            color = primaryColor,
            valueString = "${(viewModel.auraIntensity * 100).toInt()}%"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Climate Sync (Thermal Index)
        MatrixSliderItem(
            label = "ENVIRONMENT THERMAL FLUCT (TEMP)",
            value = viewModel.climateIndex,
            onValueChange = { viewModel.updateClimateIndex(it) },
            icon = Icons.Default.Thermostat,
            color = primaryColor,
            valueString = "${(viewModel.climateIndex * 50 + 10).toInt()}°C"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Rotation Angle (Perspective Sweep)
        MatrixSliderItem(
            label = "3D ISOMETRIC PERSPECTIVE SWEEP",
            value = viewModel.rotationAngle,
            valueRange = 0f..360f,
            onValueChange = { viewModel.updateRotationAngle(it) },
            icon = Icons.AutoMirrored.Filled.RotateRight,
            color = primaryColor,
            valueString = "${viewModel.rotationAngle.toInt()}°"
        )
    }
}

@Composable
fun MatrixSliderItem(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChange: (Float) -> Unit,
    icon: ImageVector,
    color: Color,
    valueString: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = valueString,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = Color(0xFF1B1A35)
            ),
            modifier = Modifier
                .height(32.dp)
                .fillMaxWidth()
        )
    }
}

@Composable
fun FrostedGradientDiagnosticsPanel(
    snapshots: List<AuraSnapshot>,
    logs: List<DiagnosticLog>,
    primaryColor: Color,
    onSaveClicked: () -> Unit,
    onLoadClicked: (AuraSnapshot) -> Unit,
    onDeleteClicked: (AuraSnapshot) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTabIsLogs by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF), // pure white at top
                        Color(0xFFE2F0FF)  // soft light-blue at bottom
                    )
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(28.dp))
            .padding(18.dp)
    ) {
        // Frosted Panel Header and Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0x1A0044FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = null,
                        tint = Color(0xFF0C0B18),
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AURA STORAGE DIAGS",
                    color = Color(0xFF0C0B18),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Save snapshot quick button
            Button(
                onClick = onSaveClicked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0C0B18),
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(30.dp)
                    .testTag("save_snapshot_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save Snapshot",
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "SNAPSHOT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Custom segment slider tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x0F000000))
                .padding(2.dp)
        ) {
            Button(
                onClick = { activeTabIsLogs = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTabIsLogs) Color.White else Color.Transparent,
                    contentColor = Color(0xFF0C0B18)
                ),
                elevation = null,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "LIVE SYSTEM TELEMETRY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Button(
                onClick = { activeTabIsLogs = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!activeTabIsLogs) Color.White else Color.Transparent,
                    contentColor = Color(0xFF0C0B18)
                ),
                elevation = null,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "RECORDED SNAPSHOTS (${snapshots.size})",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content Area inside gradient panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x0D000000))
                .border(1.dp, Color(0x12000000), RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            if (activeTabIsLogs) {
                // Render custom monospace logs
                if (logs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "NO DIAGNOSTIC LOGS",
                            color = Color(0x660C0B18),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(logs) { log ->
                            val timeStr = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(log.timestamp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = timeStr,
                                    color = Color(0x800C0B18),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "[${log.source}]",
                                    color = primaryColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = log.message,
                                    color = Color(0xFF0C0B18),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            } else {
                // Render persisted DB states
                if (snapshots.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CloudQueue,
                                contentDescription = null,
                                tint = Color(0x400C0B18),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "NO SAVED SNAPSHOTS",
                                color = Color(0x660C0B18),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Record matrix configs to Room storage",
                                color = Color(0x400C0B18),
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(snapshots, key = { it.id }) { snapshot ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x0B000000))
                                    .clickable { onLoadClicked(snapshot) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = snapshot.name,
                                        color = Color(0xFF0C0B18),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "Ecosystem: ${snapshot.ecosystemType} | Energy: ${(snapshot.pillarHeight * 100).toInt()}%",
                                        color = Color(0x800C0B18),
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // Load icon indicator
                                    IconButton(
                                        onClick = { onLoadClicked(snapshot) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudDownload,
                                            contentDescription = "Load",
                                            tint = Color(0xFF0066FF),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    
                                    // Delete snapshot icon
                                    IconButton(
                                        onClick = { onDeleteClicked(snapshot) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color(0xFFFF3355),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveSnapshotDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111024)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    tint = Color(0xFF00F0FF),
                    modifier = Modifier.size(36.dp)
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = "RECORD ECO SYSTEM SNAPSHOT",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "This will persist current parameters into the local Room SQL storage.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00F0FF),
                        unfocusedBorderColor = Color(0x33FFFFFF),
                        cursorColor = Color(0xFF00F0FF),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    placeholder = { Text("Snapshot Name", color = Color.White.copy(alpha = 0.3f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("snapshot_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CANCEL", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onConfirm(name) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("PERSIST", color = Color(0xFF0C0B18), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
