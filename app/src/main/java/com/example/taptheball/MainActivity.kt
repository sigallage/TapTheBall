package com.example.taptheball

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val gameState = remember { GameState(this) }
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (gameState.gameStarted) {
                        GameScreen(gameState)
                    }
                    TapTheBallGame()
                }
            }
        }
    }
}

@Composable
fun TapTheBallGame() {
    // Game state - using intStateOf for better performance
    var score by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(30) }
    var gameActive by remember { mutableStateOf(true) }
    var ballPosition by remember { mutableStateOf(Offset(0.5f, 0.5f)) }

    // Ball image
    val ballImage = painterResource(R.drawable.ic_launcher_foreground)

    // Timer logic
    LaunchedEffect(gameActive) {
        while (timeLeft > 0 && gameActive) {
            delay(1000)
            timeLeft--
        }
        gameActive = false
    }

    // Ball movement logic
    LaunchedEffect(gameActive) {
        while (gameActive) {
            delay(500)
            ballPosition = Offset(
                Random.nextFloat(),
                Random.nextFloat()
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                detectTapGestures { _ ->  // Using _ for unused parameter
                    if (gameActive) {
                        score++
                    }
                }
            }
    ) {
        // Score display
        Text(
            text = "Score: $score",
            fontSize = 24.sp,
            modifier = Modifier.padding(16.dp)
        )

        // Time display
        Text(
            text = "Time: $timeLeft",
            fontSize = 24.sp,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd)
        )

        // Ball image
        Image(
            painter = ballImage,
            contentDescription = "Ball to tap",
            modifier = Modifier
                .size(60.dp)
                .alignInParent(ballPosition)
        )

        // Game over message
        if (!gameActive) {
            Text(
                text = "Game Over!\nFinal Score: $score",
                fontSize = 32.sp,
                color = Color.Red,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

// Custom modifier extension for positioning
private fun Modifier.alignInParent(position: Offset) = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(constraints.maxWidth, constraints.maxHeight) {
        placeable.place(
            x = (constraints.maxWidth * position.x - placeable.width / 2).toInt(),
            y = (constraints.maxHeight * position.y - placeable.height / 2).toInt()
        )
    }

}
