package com.example.taptheball

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val ballPaint = Paint().apply {
        color = Color.RED
        isAntiAlias = true
    }
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 60f
        isAntiAlias = true
    }

    private var ballX = 100f
    private var ballY = 100f
    private val ballRadius = 60f
    private var score = 0
    private var timeLeft = 30 // 30 seconds
    private var gameActive = true

    private val handler = Handler(Looper.getMainLooper())
    private val gameLoop = object : Runnable {
        override fun run() {
            if (gameActive) {
                update()
                invalidate()
                handler.postDelayed(this, 16) // ~60 FPS
            }
        }
    }

    init {
        startGame()
    }

    private fun startGame() {
        score = 0
        timeLeft = 30
        gameActive = true

        // Start countdown timer
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (timeLeft > 0 && gameActive) {
                    timeLeft--
                    handler.postDelayed(this, 1000)
                } else {
                    gameActive = false
                    invalidate()
                }
            }
        }, 1000)

        // Start game loop
        handler.post(gameLoop)
    }

    private fun update() {
        // Move the ball randomly
        ballX += Random.nextInt(-10, 10).toFloat()
        ballY += Random.nextInt(-10, 10).toFloat()

        // Keep ball within screen bounds
        ballX = ballX.coerceIn(ballRadius, width - ballRadius)
        ballY = ballY.coerceIn(ballRadius, height - ballRadius)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw background
        canvas.drawColor(Color.WHITE)

        // Draw ball
        canvas.drawCircle(ballX, ballY, ballRadius, ballPaint)

        // Draw score and time
        canvas.drawText("Score: $score", 50f, 100f, textPaint)
        canvas.drawText("Time: $timeLeft", width - 250f, 100f, textPaint)

        // Draw game over if needed
        if (!gameActive) {
            textPaint.color = Color.RED
            textPaint.textSize = 100f
            canvas.drawText("GAME OVER", width / 2f - 200f, height / 2f, textPaint)
            textPaint.textSize = 60f
            canvas.drawText("Final Score: $score", width / 2f - 150f, height / 2f + 100f, textPaint)
            textPaint.color = Color.BLACK
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && gameActive) {
            val touchX = event.x
            val touchY = event.y

            // Check if touch is inside the ball
            val distance = sqrt(
                (touchX - ballX).toDouble().pow(2) +
                        (touchY - ballY).toDouble().pow(2)
            )

            if (distance <= ballRadius) {
                score++
                // Change ball position after tap
                ballX = Random.nextInt(ballRadius.toInt(), width - ballRadius.toInt()).toFloat()
                ballY = Random.nextInt(ballRadius.toInt(), height - ballRadius.toInt()).toFloat()
                // Change ball color randomly
                ballPaint.color = Color.rgb(
                    Random.nextInt(256),
                    Random.nextInt(256),
                    Random.nextInt(256)
                )
                invalidate()
            }
            performClick()
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacksAndMessages(null)
    }
}