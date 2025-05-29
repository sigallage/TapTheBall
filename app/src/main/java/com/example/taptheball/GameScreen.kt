package com.example.taptheball

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

// ... [Keep all your data classes and ScoreManager] ...
data class Particle(
    val position: Offset,
    val velocity: Offset,
    val color: Color,
    val life: Int = 60
)

class GameState(context: Context) {
    private val scoreManager = ScoreManager(context)

    var gameStarted by mutableStateOf(false)
    var gameActive by mutableStateOf(false)
    var difficulty by mutableIntStateOf(1)
    var score by mutableIntStateOf(0)
    var timeLeft by mutableIntStateOf(30)
    var highScore by mutableIntStateOf(scoreManager.getHighScore())

    fun startGame() {
        gameActive = true
        score = 0
        timeLeft = 30
    }

    fun endGame() {
        gameActive = false
        scoreManager.saveHighScore(score)
        highScore = scoreManager.getHighScore()
    }

    fun playTapSound() = Unit
}

// ... [Keep your StartScreen composable] ...
// Start Screen
@Composable
fun StartScreen(gameState: GameState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Tap The Ball",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                "High Score: ${gameState.highScore}",
                fontSize = 24.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(30.dp))

            Text("Difficulty:", fontSize = 20.sp, color = Color.Black)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = gameState.difficulty == 1,
                    onClick = { gameState.difficulty = 1 }
                )
                Text("Easy", color = Color.Black)

                RadioButton(
                    selected = gameState.difficulty == 2,
                    onClick = { gameState.difficulty = 2 }
                )
                Text("Medium", color = Color.Black)

                RadioButton(
                    selected = gameState.difficulty == 3,
                    onClick = { gameState.difficulty = 3 }
                )
                Text("Hard", color = Color.Black)
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { gameState.startGame(); gameState.gameStarted = true },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Start Game", fontSize = 20.sp)
            }
        }
    }
}


@Composable
fun GameScreen(gameState: GameState) {
    val ballRadius = 60f
    var ballPosition by remember { mutableStateOf(Offset.Zero) }
    var ballVelocity by remember { mutableStateOf(Offset.Zero) }
    var ballColor by remember { mutableStateOf(Color.Red) }
    var particles by remember { mutableStateOf<List<Particle>>(emptyList()) }
    var powerUpActive by remember { mutableStateOf(false) }
    var powerUpPosition by remember { mutableStateOf(Offset.Zero) }

    // Initialize ball position
    LaunchedEffect(Unit) {
        ballPosition = Offset(ballRadius, ballRadius)
        ballVelocity = Offset(2f, 2f)
    }

    // ... [Keep all your LaunchedEffect blocks] ...
    // Initialize ball position
    LaunchedEffect(Unit) {
        ballPosition = Offset(ballRadius, ballRadius)
        ballVelocity = Offset(2f, 2f)
    }

    // Game physics loop
    LaunchedEffect(gameState.gameActive) {
        while (gameState.gameActive) {
            delay(16)

            // Update ball position with physics
            ballPosition += ballVelocity

            // Bounce off walls
            if (ballPosition.x <= ballRadius || ballPosition.x >= 1080f - ballRadius) {
                ballVelocity = ballVelocity.copy(x = -ballVelocity.x * 0.95f)
            }
            if (ballPosition.y <= ballRadius || ballPosition.y >= 1920f - ballRadius) {
                ballVelocity = ballVelocity.copy(y = -ballVelocity.y * 0.95f)
            }

            // Apply gravity
            ballVelocity += Offset(0f, 0.1f)

            // Random nudges
            if (Random.nextInt(100) < 5) {
                ballVelocity += Offset(
                    (Random.nextFloat() - 0.5f) * 2f,
                    (Random.nextFloat() - 0.5f) * 2f
                )
            }
        }
    }

    // Power-up spawner
    LaunchedEffect(gameState.gameActive) {
        while (gameState.gameActive) {
            delay(10000) // Every 10 seconds
            if (!powerUpActive) {
                powerUpPosition = Offset(
                    Random.nextFloat() * 1080f,
                    Random.nextFloat() * 1920f
                )
                powerUpActive = true
            }
            delay(5000) // Disappear after 5 seconds
            powerUpActive = false
        }
    }

    // Particle updater
    LaunchedEffect(gameState.gameActive) {
        while (gameState.gameActive) {
            delay(16)
            particles = particles
                .map { it.copy(
                    position = it.position + it.velocity,
                    life = it.life - 1
                )}
                .filter { it.life > 0 }
        }
    }

    // Timer
    LaunchedEffect(gameState.gameActive) {
        while (gameState.timeLeft > 0 && gameState.gameActive) {
            delay(1000)
            gameState.timeLeft--
        }
        if (gameState.gameActive) {
            gameState.endGame()
        }
    }


    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    if (gameState.gameActive) {
                        // Check power-up collection
                        if (powerUpActive && sqrt(
                                (tapOffset.x - powerUpPosition.x).pow(2) +
                                        (tapOffset.y - powerUpPosition.y).pow(2)
                            ) <= 30f
                        ) {
                            gameState.score += 10
                            powerUpActive = false
                            gameState.playTapSound()
                        }
                        // Check ball tap
                        val distance = sqrt(
                            (tapOffset.x - ballPosition.x).pow(2) +
                                    (tapOffset.y - ballPosition.y).pow(2)
                        )
                        if (distance <= ballRadius) {
                            gameState.score++
                            particles = (0..15).map {
                                Particle(
                                    position = ballPosition,
                                    velocity = Offset(
                                        (Random.nextFloat() - 0.5f) * 8f,
                                        (Random.nextFloat() - 0.5f) * 8f
                                    ),
                                    color = ballColor
                                )
                            }
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
        // ... [Keep all your drawing code] ...
        // Draw background
        drawRect(Color.White)

        // Draw power-up
        if (powerUpActive) {
            drawCircle(
                color = Color.Yellow,
                radius = 30f,
                center = powerUpPosition,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
            )
        }

        // Draw particles
        particles.forEach { particle ->
            drawCircle(
                color = particle.color.copy(alpha = particle.life / 60f),
                radius = 5f,
                center = particle.position
            )
        }

        // Draw ball
        drawCircle(
            color = ballColor,
            radius = ballRadius,
            center = ballPosition
        )

        // Draw score and time
        drawText(
            text = "Score: ${gameState.score}",
            x = 50f,
            y = 100f,
            color = Color.Black,
            fontSize = 24.sp
        )
        drawText(
            text = "Time: ${gameState.timeLeft}",
            x = size.width - 150f,
            y = 100f,
            color = Color.Black,
            fontSize = 24.sp
        )

        // Game over text
        if (!gameState.gameActive && gameState.gameStarted) {
            drawText(
                text = "GAME OVER",
                x = size.width / 2 - 100f,
                y = size.height / 2,
                color = Color.Red,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
            drawText(
                text = "Final Score: ${gameState.score}",
                x = size.width / 2 - 120f,
                y = size.height / 2 + 60f,
                color = Color.Black,
                fontSize = 30.sp
            )
        }
    }
}

// Helper function to draw text on Canvas
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawText(
    text: String,
    x: Float,
    y: Float,
    color: Color,
    fontSize: Float = 24f,  // Note: Using Float directly instead of TextUnit
    fontWeight: FontWeight = FontWeight.Normal
) {
    drawContext.canvas.nativeCanvas.drawText(
        text,
        x,
        y,
        android.graphics.Paint().apply {
            this.color = android.graphics.Color.parseColor(color.toHex())
            this.textSize = fontSize
            this.typeface = android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT,
                fontWeight.weight
            )
        }
    )
}


// Color to hex extension
private fun Color.toHex(): String {
    val alpha = (alpha * 255).toInt()
    val red = (red * 255).toInt()
    val green = (green * 255).toInt()
    val blue = (blue * 255).toInt()
    return String.format("#%02X%02X%02X%02X", alpha, red, green, blue)
}