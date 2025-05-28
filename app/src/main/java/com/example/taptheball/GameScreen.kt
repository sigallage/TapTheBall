package com.example.taptheball

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random
import kotlinx.coroutines.delay
import java.lang.StrictMath.pow

@Composable
fun GameScreen(modifier: Modifier = Modifier) {
    var ballPosition by remember { mutableStateOf(Offset(100f, 100f)) }
    var score by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(30) }
    var gameActive by remember { mutableStateOf(true) }
    var ballColor by remember { mutableStateOf(Color.Red) }
    val ballRadius = 60f

    // Game loop
    LaunchedEffect(gameActive) {
        while (gameActive) {
            delay(16) // ~60 FPS
            ballPosition = Offset(
                x = (ballPosition.x + Random.nextInt(-10, 10)).coerceIn(ballRadius, 1080f - ballRadius),
                y = (ballPosition.y + Random.nextInt(-10, 10)).coerceIn(ballRadius, 1920f - ballRadius)
            )
        }
    }

    // Timer
    LaunchedEffect(gameActive) {
        while (timeLeft > 0 && gameActive) {
            delay(1000)
            timeLeft--
        }
        gameActive = false
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    if (gameActive) {
                        val distance = sqrt(
                            pow((tapOffset.x - ballPosition.x).toDouble(), 2.0) +
                                    pow((tapOffset.y - ballPosition.y).toDouble(), 2.0)
                        )
                        if (distance <= ballRadius) {
                            score++
                            ballPosition = Offset(
                                Random.nextInt(ballRadius.toInt(), 1080 - ballRadius.toInt()).toFloat(),
                                Random.nextInt(ballRadius.toInt(), 1920 - ballRadius.toInt()).toFloat()
                            )
                            ballColor = Color(
                                Random.nextInt(256),
                                Random.nextInt(256),
                                Random.nextInt(256)
                            )
                        }
                    }
                }
            }
    ) {
        // Draw background
        drawRect(Color.White, size = size)

        // Draw ball
        drawCircle(
            color = ballColor,
            radius = ballRadius,
            center = ballPosition
        )

        // Draw score and time
        drawContext.canvas.nativeCanvas.apply {
            drawText(
                "Score: $score",
                50f,
                100f,
                Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 60f
                }
            )
            drawText(
                "Time: $timeLeft",
                size.width - 250f,
                100f,
                Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 60f
                }
            )

            // Game over text
            if (!gameActive) {
                drawText(
                    "GAME OVER",
                    size.width / 2 - 200f,
                    size.height / 2,
                    Paint().apply {
                        color = android.graphics.Color.RED
                        textSize = 100f
                    }
                )
                drawText(
                    "Final Score: $score",
                    size.width / 2 - 150f,
                    size.height / 2 + 100f,
                    Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 60f
                    }
                )
            }
        }
    }
}