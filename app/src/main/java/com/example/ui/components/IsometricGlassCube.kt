package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.viewmodel.EcosystemType
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun IsometricGlassCube(
    modifier: Modifier = Modifier,
    activeType: EcosystemType,
    pillarHeight: Float,
    auraIntensity: Float,
    climateIndex: Float,
    rotationAngle: Float
) {
    // Generate organic pulse and sway animations for ecosystem dynamics
    val infiniteTransition = rememberInfiniteTransition(label = "ecosystem_dynamics")
    
    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_pillar_1"
    )

    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_pillar_2"
    )

    val sway by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "grass_sway"
    )

    val hologramFloat by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hologram_float"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        // Define Center and Scales
        val xc = width / 2f
        val yc = height * 0.58f // lower slightly to accommodate 3D extrusion upward
        
        val baseSize = minOf(width, height) * 0.43f
        val maxCubeHeight = baseSize * 0.72f
        val currentCubeHeight = maxCubeHeight

        // Setup theme colors
        val primaryColor = Color(activeType.primaryColorHex)
        val secondaryColor = Color(activeType.secondaryColorHex)
        val glowColor = primaryColor.copy(alpha = 0.5f * auraIntensity)

        // 1. NEON BASE GLOW (Radial Gradient Shadow at the base of the tray)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glowColor, Color.Transparent),
                center = Offset(xc, yc + baseSize * 0.15f),
                radius = baseSize * 1.3f
            ),
            center = Offset(xc, yc + baseSize * 0.15f),
            radius = baseSize * 1.3f
        )

        // Math Constants
        val cos30 = 0.8660254f
        val sin30 = 0.5f
        val rad = Math.toRadians(rotationAngle.toDouble())
        val cosRot = cos(rad).toFloat()
        val sinRot = sin(rad).toFloat()

        // Projection Helper Function
        fun project(x: Float, y: Float, z: Float): Offset {
            // Rotate around Y-axis (vertical)
            val xr = x * cosRot - z * sinRot
            val zr = x * sinRot + z * cosRot
            
            // Isometric screen projection
            val u = xc + (xr - zr) * cos30
            val v = yc - y + (xr + zr) * sin30
            return Offset(u, v)
        }

        // --- DRAW BASE PLATFORM TRAY (Solid Extruded Base) ---
        // Top surface of base tray is at y = 0
        // Bottom of base tray is at y = -16dp (thickness)
        val thickness = 35f
        val tPoints = listOf(
            project(baseSize / 2f, 0f, baseSize / 2f),  // B0 (Bottom Center)
            project(-baseSize / 2f, 0f, baseSize / 2f), // B1 (Left corner)
            project(-baseSize / 2f, 0f, -baseSize / 2f),// B2 (Top corner)
            project(baseSize / 2f, 0f, -baseSize / 2f)  // B3 (Right corner)
        )

        val bPoints = listOf(
            project(baseSize / 2f, -thickness, baseSize / 2f),
            project(-baseSize / 2f, -thickness, baseSize / 2f),
            project(-baseSize / 2f, -thickness, -baseSize / 2f),
            project(baseSize / 2f, -thickness, -baseSize / 2f)
        )

        // Draw under tray base faces (dark metal/slate extrusions)
        // Left extruded side of tray
        val leftTrayPath = Path().apply {
            moveTo(bPoints[1].x, bPoints[1].y)
            lineTo(bPoints[0].x, bPoints[0].y)
            lineTo(tPoints[0].x, tPoints[0].y)
            lineTo(tPoints[1].x, tPoints[1].y)
            close()
        }
        drawPath(
            path = leftTrayPath,
            color = Color(0xFF16152B)
        )

        // Right extruded side of tray
        val rightTrayPath = Path().apply {
            moveTo(bPoints[0].x, bPoints[0].y)
            lineTo(bPoints[3].x, bPoints[3].y)
            lineTo(tPoints[3].x, tPoints[3].y)
            lineTo(tPoints[0].x, tPoints[0].y)
            close()
        }
        drawPath(
            path = rightTrayPath,
            color = Color(0xFF1F1E38)
        )

        // Draw top surface of the tray base (rhombus, dark purple slate texture)
        val topTrayPath = Path().apply {
            moveTo(tPoints[0].x, tPoints[0].y)
            lineTo(tPoints[1].x, tPoints[1].y)
            lineTo(tPoints[2].x, tPoints[2].y)
            lineTo(tPoints[3].x, tPoints[3].y)
            close()
        }
        drawPath(
            path = topTrayPath,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF100F21), Color(0xFF232240)),
                start = tPoints[2],
                end = tPoints[0]
            )
        )

        // Draw tray border highlight line (glowing cyan neon trim around tray top)
        val trayStrokePath = Path().apply {
            moveTo(tPoints[1].x, tPoints[1].y)
            lineTo(tPoints[0].x, tPoints[0].y)
            lineTo(tPoints[3].x, tPoints[3].y)
        }
        drawPath(
            path = trayStrokePath,
            color = primaryColor.copy(alpha = 0.7f),
            style = Stroke(width = 4f, cap = StrokeCap.Round)
        )

        // Draw circular grid patterns or details inside the tray
        val steps = 4
        for (i in 1..steps) {
            val factor = i.toFloat() / steps
            val gp0 = project(baseSize / 2f * factor, 0f, baseSize / 2f * factor)
            val gp1 = project(-baseSize / 2f * factor, 0f, baseSize / 2f * factor)
            val gp2 = project(-baseSize / 2f * factor, 0f, -baseSize / 2f * factor)
            val gp3 = project(baseSize / 2f * factor, 0f, -baseSize / 2f * factor)
            
            val ringPath = Path().apply {
                moveTo(gp0.x, gp0.y)
                lineTo(gp1.x, gp1.y)
                lineTo(gp2.x, gp2.y)
                lineTo(gp3.x, gp3.y)
                close()
            }
            drawPath(
                path = ringPath,
                color = primaryColor.copy(alpha = 0.08f),
                style = Stroke(width = 1.5f)
            )
        }

        // --- DRAW INTERIOR ECOSYSTEM ELEMENTS (Z-sorted from back to front) ---
        // Sorting items based on their rotated Z coordinate to draw back-to-front!
        // Rotated Z position of a point is: zr = x * sinRot + z * cosRot
        // Since we want to draw back-to-front: larger zr means closer to the camera, smaller zr means further back.
        // We sort objects from SMALLEST rotated Z to LARGEST rotated Z!

        data class DrawableItem(
            val rotatedZ: Float,
            val drawAction: DrawScope.() -> Unit
        )

        val drawables = mutableListOf<DrawableItem>()

        // A. NEON CYLINDER PILLARS
        // We define three pillars in local coordinates (X, Z)
        val pillarConfigs = listOf(
            Triple(-baseSize * 0.22f, -baseSize * 0.22f, pulse1 * 0.85f), // Back
            Triple(baseSize * 0.25f, baseSize * 0.20f, pulse2 * 0.6f),   // Front Right
            Triple(-baseSize * 0.26f, baseSize * 0.15f, pulse1 * 0.75f)  // Front Left
        )

        pillarConfigs.forEachIndexed { idx, config ->
            val lx = config.first
            val lz = config.second
            val heightMultiplier = config.third
            
            val localHeight = maxCubeHeight * 0.7f * pillarHeight * heightMultiplier
            val rPillar = 20f

            // Calculate rotated Z for sorting
            val rotZ = lx * sinRot + lz * cosRot

            drawables.add(
                DrawableItem(rotZ) {
                    val pBase = project(lx, 0f, lz)
                    val pTop = project(lx, localHeight, lz)

                    // Draw pillar glow underlay
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(primaryColor.copy(alpha = 0.35f * auraIntensity), Color.Transparent),
                            center = pBase,
                            radius = rPillar * 4f
                        ),
                        center = pBase,
                        radius = rPillar * 4f
                    )

                    // Compute pillar cylindrical body outline path
                    // The 2D width of the isometric ellipse is 2*R, height is R.
                    val baseEllipseL = pBase + Offset(-rPillar, 0f)
                    val baseEllipseR = pBase + Offset(rPillar, 0f)
                    val topEllipseL = pTop + Offset(-rPillar, 0f)
                    val topEllipseR = pTop + Offset(rPillar, 0f)

                    val bodyPath = Path().apply {
                        moveTo(baseEllipseL.x, baseEllipseL.y)
                        lineTo(topEllipseL.x, topEllipseL.y)
                        
                        // Arcs at top and bottom
                        quadraticTo(pTop.x, pTop.y + rPillar * 0.5f, topEllipseR.x, topEllipseR.y)
                        lineTo(baseEllipseR.x, baseEllipseR.y)
                        quadraticTo(pBase.x, pBase.y + rPillar * 0.5f, baseEllipseL.x, baseEllipseL.y)
                        close()
                    }

                    // Fill body with vertical neon gradient
                    drawPath(
                        path = bodyPath,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                secondaryColor.copy(alpha = 0.5f),
                                primaryColor.copy(alpha = 0.85f)
                            ),
                            start = pBase,
                            end = pTop
                        )
                    )

                    // Draw Top Cap of Cylinder
                    val capPath = Path().apply {
                        moveTo(topEllipseL.x, topEllipseL.y)
                        quadraticTo(pTop.x, pTop.y - rPillar * 0.5f, topEllipseR.x, topEllipseR.y)
                        quadraticTo(pTop.x, pTop.y + rPillar * 0.5f, topEllipseL.x, topEllipseL.y)
                        close()
                    }
                    drawPath(
                        path = capPath,
                        color = Color.White.copy(alpha = 0.95f)
                    )
                    drawPath(
                        path = capPath,
                        color = primaryColor,
                        style = Stroke(width = 3f)
                    )

                    // Glowing point on top of column
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = pTop
                    )
                }
            )
        }

        // B. GRASS AND VEGETATION
        // Draw little neon-green glowing weeds or filaments on the surface
        val grassPositions = listOf(
            Pair(-baseSize * 0.35f, -baseSize * 0.1f),
            Pair(-baseSize * 0.1f, -baseSize * 0.35f),
            Pair(baseSize * 0.1f, -baseSize * 0.1f),
            Pair(baseSize * 0.3f, -baseSize * 0.15f),
            Pair(-baseSize * 0.15f, baseSize * 0.3f),
            Pair(0f, baseSize * 0.25f),
            Pair(baseSize * 0.15f, baseSize * 0.1f)
        )

        // We map grass color based on climate and theme
        val grassColor = if (activeType == EcosystemType.SOLAR_FLARE) {
            Color(0xFFFFB700) // Amber/Yellow
        } else if (activeType == EcosystemType.AURORA_VALLEY) {
            Color(0xFF00FFCC) // Cyan
        } else {
            Color(0xFF39FF14) // Electric Green
        }

        grassPositions.forEach { pos ->
            val lx = pos.first
            val lz = pos.second
            val rotZ = lx * sinRot + lz * cosRot

            drawables.add(
                DrawableItem(rotZ) {
                    val pBase = project(lx, 0f, lz)
                    
                    // Draw grass blade 1
                    val pTop1 = pBase + Offset(-8f + sway, -20f - climateIndex * 15f)
                    val pTop2 = pBase + Offset(8f + sway * 0.5f, -16f - climateIndex * 10f)
                    
                    drawLine(
                        color = grassColor.copy(alpha = 0.75f),
                        start = pBase,
                        end = pTop1,
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = grassColor.copy(alpha = 0.75f),
                        start = pBase,
                        end = pTop2,
                        strokeWidth = 3.5f,
                        cap = StrokeCap.Round
                    )
                }
            )
        }

        // C. FLOATING HOLOGRAM DISPLAY
        // Let's place a holographic display screen near the center, slightly elevated
        val holoZ = 0f
        drawables.add(
            DrawableItem(holoZ) {
                val holoBase = project(0f, maxCubeHeight * 0.2f + hologramFloat, 0f)
                val hWidth = 85f
                val hHeight = 65f

                // Draw floating glass hologram
                val holoPath = Path().apply {
                    val p1 = holoBase + Offset(-hWidth / 2f, -hHeight / 2f)
                    val p2 = holoBase + Offset(hWidth / 2f, -hHeight / 2f + 10f)
                    val p3 = holoBase + Offset(hWidth / 2f, hHeight / 2f + 10f)
                    val p4 = holoBase + Offset(-hWidth / 2f, hHeight / 2f)
                    moveTo(p1.x, p1.y)
                    lineTo(p2.x, p2.y)
                    lineTo(p3.x, p3.y)
                    lineTo(p4.x, p4.y)
                    close()
                }

                // Transparent glass screen fill
                drawPath(
                    path = holoPath,
                    brush = Brush.linearGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.35f), Color.Transparent),
                        start = holoBase + Offset(-hWidth / 2f, -hHeight / 2f),
                        end = holoBase + Offset(hWidth / 2f, hHeight / 2f)
                    )
                )

                // Hologram Border
                drawPath(
                    path = holoPath,
                    color = primaryColor.copy(alpha = 0.7f),
                    style = Stroke(width = 2f)
                )

                // Inside the hologram draw a glowing wave/line
                val wavePath = Path().apply {
                    val startX = holoBase.x - hWidth * 0.35f
                    val baseOffsetY = holoBase.y + 10f
                    moveTo(startX, baseOffsetY)
                    for (i in 0..10) {
                        val pct = i / 10f
                        val x = startX + pct * hWidth * 0.7f
                        val y = baseOffsetY + sin(pct * Math.PI.toFloat() * 3f + (sway * 0.5f)) * 12f
                        lineTo(x, y)
                    }
                }
                drawPath(
                    path = wavePath,
                    color = Color.White.copy(alpha = 0.9f),
                    style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                )

                // Draw digital markers/dots on the hologram
                drawCircle(
                    color = Color.White,
                    radius = 3f,
                    center = holoBase + Offset(-hWidth * 0.35f, -hHeight * 0.25f)
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.6f),
                    start = holoBase + Offset(-hWidth * 0.2f, -hHeight * 0.25f),
                    end = holoBase + Offset(hWidth * 0.3f, -hHeight * 0.25f),
                    strokeWidth = 2f
                )
            }
        )

        // D. DIGITAL PARTICLES / SPARKLES
        // Tiny specs of light drifting upward inside the cube
        val particleSeed = listOf(
            Triple(-baseSize * 0.15f, maxCubeHeight * 0.15f, baseSize * 0.2f),
            Triple(baseSize * 0.1f, maxCubeHeight * 0.4f, -baseSize * 0.15f),
            Triple(-baseSize * 0.25f, maxCubeHeight * 0.65f, -baseSize * 0.25f),
            Triple(baseSize * 0.3f, maxCubeHeight * 0.35f, baseSize * 0.3f),
            Triple(-baseSize * 0.05f, maxCubeHeight * 0.8f, baseSize * 0.1f),
            Triple(baseSize * 0.2f, maxCubeHeight * 0.75f, -baseSize * 0.2f)
        )

        particleSeed.forEachIndexed { pIdx, particle ->
            val lx = particle.first
            // Drift the particle upwards based on pulse/sway
            val driftY = (particle.second + (sway * 5f + pIdx * 12f) * climateIndex) % maxCubeHeight
            val lz = particle.third
            val rotZ = lx * sinRot + lz * cosRot

            drawables.add(
                DrawableItem(rotZ) {
                    val pLoc = project(lx, driftY, lz)
                    drawCircle(
                        color = Color.White.copy(alpha = 0.75f),
                        radius = 2.5f,
                        center = pLoc
                    )
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.4f),
                        radius = 6f,
                        center = pLoc
                    )
                }
            )
        }

        // Sort drawables list back-to-front by Rotated Z and execute drawing
        drawables.sortBy { it.rotatedZ }
        drawables.forEach { it.drawAction(this) }


        // --- DRAW FROSTED GLASS COVER (CUBE ENCLOSURE) ---
        // Sits on top of the tray. Floor corners at y = 0 (tPoints).
        // Roof corners at y = currentCubeHeight (rPoints).
        val rPoints = listOf(
            project(baseSize / 2f, currentCubeHeight, baseSize / 2f),  // R0
            project(-baseSize / 2f, currentCubeHeight, baseSize / 2f), // R1
            project(-baseSize / 2f, currentCubeHeight, -baseSize / 2f),// R2
            project(baseSize / 2f, currentCubeHeight, -baseSize / 2f)  // R3
        )

        // Glass Face 1: Left Front Glass Panel (defined by B1, B0, R0, R1)
        val leftGlassPath = Path().apply {
            moveTo(tPoints[1].x, tPoints[1].y)
            lineTo(tPoints[0].x, tPoints[0].y)
            lineTo(rPoints[0].x, rPoints[0].y)
            lineTo(rPoints[1].x, rPoints[1].y)
            close()
        }

        // Glass Face 2: Right Front Glass Panel (defined by B0, B3, R3, R0)
        val rightGlassPath = Path().apply {
            moveTo(tPoints[0].x, tPoints[0].y)
            lineTo(tPoints[3].x, tPoints[3].y)
            lineTo(rPoints[3].x, rPoints[3].y)
            lineTo(rPoints[0].x, rPoints[0].y)
            close()
        }

        // Glass Face 3: Top Glass Panel (defined by R0, R1, R2, R3)
        val topGlassPath = Path().apply {
            moveTo(rPoints[0].x, rPoints[0].y)
            lineTo(rPoints[1].x, rPoints[1].y)
            lineTo(rPoints[2].x, rPoints[2].y)
            lineTo(rPoints[3].x, rPoints[3].y)
            close()
        }

        // Draw Left Front Glass Panel with translucent white-to-transparent linear gradient
        drawPath(
            path = leftGlassPath,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.16f),
                    Color.White.copy(alpha = 0.02f)
                ),
                start = rPoints[1],
                end = tPoints[0]
            )
        )

        // Draw Right Front Glass Panel with similar gradient
        drawPath(
            path = rightGlassPath,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.12f),
                    Color.White.copy(alpha = 0.01f)
                ),
                start = rPoints[0],
                end = tPoints[3]
            )
        )

        // Draw Top Glass panel with slight frosted reflection
        drawPath(
            path = topGlassPath,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.22f),
                    Color.White.copy(alpha = 0.03f)
                ),
                start = rPoints[2],
                end = rPoints[0]
            )
        )

        // --- GLASS HIGHLIGHTS AND OUTLINES (Adds high-fidelity premium glassy realism) ---
        // Crisp white/cyan borders on front-facing glass seams
        // Draw vertical center corner (R0 to B0)
        drawLine(
            color = Color.White.copy(alpha = 0.5f),
            start = rPoints[0],
            end = tPoints[0],
            strokeWidth = 2.5f
        )
        // Draw left side edge (R1 to B1)
        drawLine(
            color = Color.White.copy(alpha = 0.3f),
            start = rPoints[1],
            end = tPoints[1],
            strokeWidth = 1.5f
        )
        // Draw right side edge (R3 to B3)
        drawLine(
            color = Color.White.copy(alpha = 0.3f),
            start = rPoints[3],
            end = tPoints[3],
            strokeWidth = 1.5f
        )

        // Draw Top Seam Outline (R1 to R0 to R3)
        val topGlassBorders = Path().apply {
            moveTo(rPoints[1].x, rPoints[1].y)
            lineTo(rPoints[0].x, rPoints[0].y)
            lineTo(rPoints[3].x, rPoints[3].y)
        }
        drawPath(
            path = topGlassBorders,
            color = Color.White.copy(alpha = 0.6f),
            style = Stroke(width = 3f)
        )

        // Draw Back Seam Outline (highly faded, R1 to R2 to R3)
        val backGlassBorders = Path().apply {
            moveTo(rPoints[1].x, rPoints[1].y)
            lineTo(rPoints[2].x, rPoints[2].y)
            lineTo(rPoints[3].x, rPoints[3].y)
        }
        drawPath(
            path = backGlassBorders,
            color = Color.White.copy(alpha = 0.15f),
            style = Stroke(width = 1.5f)
        )

        // Draw a diagonal "glass reflection" sheen across the entire cube
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.25f), Color.Transparent),
                start = rPoints[1] + Offset(50f, -50f),
                end = tPoints[3] + Offset(-50f, 50f)
            ),
            start = rPoints[1] + Offset(50f, -50f),
            end = tPoints[3] + Offset(-50f, 50f),
            strokeWidth = 40f
        )
    }
}
