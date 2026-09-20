package com.example.noxis.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.ElectricBlue
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun JarvisArcReactorView(
  hudState: JarvisHudState,
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {}
) {
  val infiniteTransition = rememberInfiniteTransition(label = "arc_reactor")

  // Rotation animations
  val rotationSlow by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = if (hudState == JarvisHudState.PROCESSING) 4000 else 12000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "rotation_slow"
  )

  val rotationCounter by infiniteTransition.animateFloat(
    initialValue = 360f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = if (hudState == JarvisHudState.PROCESSING) 3000 else 9000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "rotation_counter"
  )

  // Energy pulse animation
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.88f,
    targetValue = 1.08f,
    animationSpec = infiniteRepeatable(
      animation = tween(
        durationMillis = when (hudState) {
          JarvisHudState.SPEAKING -> 500
          JarvisHudState.PROCESSING -> 700
          JarvisHudState.EXECUTING -> 600
          JarvisHudState.LISTENING -> 800
          JarvisHudState.STANDBY -> 1800
        },
        easing = FastOutSlowInEasing
      ),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
  )

  // Waveform height multipliers
  val waveAnim1 by infiniteTransition.animateFloat(
    initialValue = 0.2f, targetValue = 1.0f,
    animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse),
    label = "wave1"
  )
  val waveAnim2 by infiniteTransition.animateFloat(
    initialValue = 0.8f, targetValue = 0.3f,
    animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse),
    label = "wave2"
  )
  val waveAnim3 by infiniteTransition.animateFloat(
    initialValue = 0.4f, targetValue = 0.9f,
    animationSpec = infiniteRepeatable(tween(280, easing = LinearEasing), RepeatMode.Reverse),
    label = "wave3"
  )

  val stateColor = when (hudState) {
    JarvisHudState.STANDBY -> CyanPrimary
    JarvisHudState.LISTENING -> AccentGold
    JarvisHudState.PROCESSING -> CyanGlow
    JarvisHudState.EXECUTING -> ElectricBlue
    JarvisHudState.SPEAKING -> CyanPrimary
  }

  val stateLabel = when (hudState) {
    JarvisHudState.STANDBY -> "ONLINE // READY"
    JarvisHudState.LISTENING -> "AWAITING AUDIO"
    JarvisHudState.PROCESSING -> "COMPUTING MATRIX"
    JarvisHudState.EXECUTING -> "OVERRIDING PHONE SUBSYSTEM"
    JarvisHudState.SPEAKING -> "TRANSMITTING SPEECH"
  }

  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .size(175.dp)
        .testTag("arc_reactor_core")
        .clickable { onClick() },
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val baseRadius = (size.minDimension / 2f) * 0.85f

        // Outer glow halo
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(stateColor.copy(alpha = 0.25f * pulseScale), Color.Transparent),
            center = center,
            radius = baseRadius * 1.3f
          ),
          center = center,
          radius = baseRadius * 1.3f
        )

        // Outer segmented ring with clockwise rotation
        rotate(rotationSlow, pivot = center) {
          drawCircle(
            color = stateColor.copy(alpha = 0.2f),
            radius = baseRadius,
            center = center,
            style = Stroke(width = 1.5f)
          )

          // 8 outer arc segments
          val numSegments = 8
          val arcSweep = 24f
          for (i in 0 until numSegments) {
            val startAngle = i * (360f / numSegments)
            drawArc(
              color = stateColor.copy(alpha = 0.85f),
              startAngle = startAngle,
              sweepAngle = arcSweep,
              useCenter = false,
              topLeft = Offset(center.x - baseRadius, center.y - baseRadius),
              size = androidx.compose.ui.geometry.Size(baseRadius * 2, baseRadius * 2),
              style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )
          }
        }

        // Middle counter-rotating ring with notches
        rotate(rotationCounter, pivot = center) {
          val midRadius = baseRadius * 0.72f
          drawCircle(
            color = stateColor.copy(alpha = 0.35f),
            radius = midRadius,
            center = center,
            style = Stroke(width = 2f)
          )

          // Tick markers
          val tickCount = 16
          for (i in 0 until tickCount) {
            val angle = Math.toRadians((i * (360.0 / tickCount)))
            val innerX = center.x + (midRadius - 6f) * cos(angle).toFloat()
            val innerY = center.y + (midRadius - 6f) * sin(angle).toFloat()
            val outerX = center.x + (midRadius + 6f) * cos(angle).toFloat()
            val outerY = center.y + (midRadius + 6f) * sin(angle).toFloat()

            drawLine(
              color = stateColor.copy(alpha = 0.7f),
              start = Offset(innerX, innerY),
              end = Offset(outerX, outerY),
              strokeWidth = 2f,
              cap = StrokeCap.Round
            )
          }
        }

        // Inner core pulsing ring
        val innerRadius = baseRadius * 0.44f * pulseScale
        drawCircle(
          color = stateColor.copy(alpha = 0.5f),
          radius = innerRadius,
          center = center,
          style = Stroke(width = 2.5f)
        )

        // Center glowing nexus point
        drawCircle(
          color = stateColor.copy(alpha = 0.9f),
          radius = baseRadius * 0.20f * pulseScale,
          center = center
        )
        drawCircle(
          color = Color.White,
          radius = baseRadius * 0.10f,
          center = center
        )
      }

      // Center Diamond icon / Jarvis crest
      Text(
        text = "J",
        color = Color.Black,
        fontWeight = FontWeight.Black,
        fontSize = 17.sp,
        fontFamily = FontFamily.Monospace
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Waveform audio reactive bars (active during speaking/listening/processing)
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      val isBusy = hudState != JarvisHudState.STANDBY
      val heights = if (isBusy) {
        listOf(
          10.dp * waveAnim1,
          18.dp * waveAnim3,
          24.dp * waveAnim2,
          16.dp * waveAnim1,
          22.dp * waveAnim3,
          12.dp * waveAnim2
        )
      } else {
        listOf(6.dp, 8.dp, 10.dp, 8.dp, 6.dp, 4.dp)
      }

      heights.forEach { h ->
        Box(
          modifier = Modifier
            .padding(horizontal = 2.dp)
            .width(3.dp)
            .height(h)
            .then(
              Modifier.padding(0.dp)
            )
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
              color = stateColor.copy(alpha = if (isBusy) 0.9f else 0.4f),
              cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Subsystem state readout
    Text(
      text = "NOXIS PHONE CORE // $stateLabel",
      color = stateColor,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace,
      letterSpacing = 1.2.sp
    )
  }
}
