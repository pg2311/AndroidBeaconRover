package com.scsa.abr.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Virtual joystick control for vehicle steering.
 *
 * @param modifier Modifier for the joystick container
 * @param joystickSize Size of the joystick (default 200.dp)
 * @param baseColor Color of the outer circle (base)
 * @param stickColor Color of the inner circle (stick)
 * @param deadZone Percentage of radius where input is ignored (0.0 to 1.0, default 0.1)
 * @param onMove Called when joystick moves, provides normalized x,y from -1.0 to 1.0
 * @param onRelease Called when joystick is released
 */
@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    joystickSize: Dp = 200.dp,
    baseColor: Color = Color.Gray.copy(alpha = 0.3f),
    stickColor: Color = Color.Blue,
    deadZone: Float = 0.1f,
    onMove: (x: Float, y: Float) -> Unit,
    onRelease: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .size(joystickSize)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        offsetX = 0f
                        offsetY = 0f
                        onRelease()
                    },
                    onDragCancel = {
                        offsetX = 0f
                        offsetY = 0f
                        onRelease()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()

                        offsetX += dragAmount.x
                        offsetY += dragAmount.y

                        val radius = this.size.width / 2f
                        val maxOffset = radius * 0.7f // Joystick travel range (70% of radius)

                        // Constrain to circular boundary
                        val distance = sqrt(offsetX * offsetX + offsetY * offsetY)
                        if (distance > maxOffset) {
                            val scale = maxOffset / distance
                            offsetX *= scale
                            offsetY *= scale
                        }

                        // Calculate distance from center (0.0 to 1.0)
                        val normalizedDistance = distance / maxOffset

                        // Apply dead zone
                        if (normalizedDistance < deadZone) {
                            // Within dead zone - treat as center (stop)
                            onMove(0f, 0f)
                        } else {
                            // Normalize to -1.0 to 1.0
                            val normalizedX = offsetX / maxOffset
                            val normalizedY =
                                -offsetY / maxOffset // Invert Y (screen Y increases downward)

                            onMove(normalizedX, normalizedY)
                        }
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = min(size.width, size.height) / 2

            // Draw outer circle (base)
            drawCircle(
                color = baseColor,
                radius = radius,
                center = Offset(centerX, centerY)
            )

            // Draw center crosshair for reference
            val crosshairSize = radius * 0.15f
            drawLine(
                color = Color.Gray.copy(alpha = 0.5f),
                start = Offset(centerX - crosshairSize, centerY),
                end = Offset(centerX + crosshairSize, centerY),
                strokeWidth = 2f
            )
            drawLine(
                color = Color.Gray.copy(alpha = 0.5f),
                start = Offset(centerX, centerY - crosshairSize),
                end = Offset(centerX, centerY + crosshairSize),
                strokeWidth = 2f
            )

            // Draw inner circle (stick)
            drawCircle(
                color = stickColor,
                radius = radius * 0.3f,
                center = Offset(centerX + offsetX, centerY + offsetY)
            )

            // Draw stick border for better visibility
            drawCircle(
                color = Color.White,
                radius = radius * 0.3f,
                center = Offset(centerX + offsetX, centerY + offsetY),
                style = Stroke(width = 3f)
            )
        }
    }
}
