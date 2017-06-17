package com.omjoonkim.skeletonloadingview

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View

class SkeletonLoadingView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    val BASE_COLOR_DEFAULT = Color.parseColor("#fbfbfb")
    val DEEP_COLOR_DEFAULT = Color.parseColor("#f2f2f2")
    val RADIUS_DEFAULT = 5.toDp(context)
    val PROGRESS_LENGTH_DEFAULT = 120.toDp(context)
    val DURATION_DEFAULT = 1500L
    val INTERVAL_DEFAULT = 0L

    val animator: ValueAnimator =
            ValueAnimator.ofFloat(0f, 1f).apply {
                addUpdateListener {
                    frame = it.animatedFraction
                    postInvalidate()
                }
            }

    var frame = 0f
    var radius = RADIUS_DEFAULT
    var progressLength = PROGRESS_LENGTH_DEFAULT
    var baseColor = BASE_COLOR_DEFAULT
    var deepColor = DEEP_COLOR_DEFAULT
    var durationOfPass = DURATION_DEFAULT
    var interval = INTERVAL_DEFAULT
    var autoStart = true

    var basePaint: Paint
    var deepPaintLeft: Paint
    var deepPaintRight: Paint

    var rect = RectF()
    var path = Path()

    private var screenHeight: Int

    private var screenWidth: Int

    private var m: Matrix = Matrix()

    init {

        with(context.obtainStyledAttributes(attrs, R.styleable.SkeletonLoadingView)) {
            if (hasValue(R.styleable.SkeletonLoadingView_radius))
                radius = getDimensionPixelOffset(R.styleable.SkeletonLoadingView_radius, RADIUS_DEFAULT.toInt()).toFloat()
            if (hasValue(R.styleable.SkeletonLoadingView_duration))
                durationOfPass = getInt(R.styleable.SkeletonLoadingView_duration, DURATION_DEFAULT.toInt()).toLong()
            if (hasValue(R.styleable.SkeletonLoadingView_interval))
                interval = getInt(R.styleable.SkeletonLoadingView_interval, INTERVAL_DEFAULT.toInt()).toLong()
            if (hasValue(R.styleable.SkeletonLoadingView_baseColor))
                baseColor = getColor(R.styleable.SkeletonLoadingView_baseColor, BASE_COLOR_DEFAULT)
            if (hasValue(R.styleable.SkeletonLoadingView_deepColor))
                deepColor = getColor(R.styleable.SkeletonLoadingView_deepColor, DEEP_COLOR_DEFAULT)
            if (hasValue(R.styleable.SkeletonLoadingView_progressLength))
                progressLength = getDimensionPixelOffset(R.styleable.SkeletonLoadingView_progressLength, PROGRESS_LENGTH_DEFAULT.toInt()).toFloat()
            if (hasValue(R.styleable.SkeletonLoadingView_autoStart))
                autoStart = getBoolean(R.styleable.SkeletonLoadingView_autoStart, true)
        }

        screenHeight = context.resources.displayMetrics.heightPixels
        screenWidth = context.resources.displayMetrics.widthPixels

        basePaint = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            color = baseColor
        }
        deepPaintLeft = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            shader = LinearGradient(0f, 0f, progressLength / 2, 0f, baseColor, deepColor, Shader.TileMode.CLAMP)
        }
        deepPaintRight = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            shader = LinearGradient(0f, 0f, progressLength / 2, 0f, deepColor, baseColor, Shader.TileMode.CLAMP)
        }
        if (autoStart)
            start()
    }

    override fun onDraw(canvas: Canvas?) {
        val width = width.toFloat()
        val height = height.toFloat()
        canvas?.clipPath(path.apply { reset(); addRoundRect(rect.apply { set(0f, 0f, width, height) }, radius, radius, Path.Direction.CW) })
        super.onDraw(canvas)
        canvas?.let { canvas ->
            canvas.drawRoundRect(rect.apply { set(0f, 0f, width, height) }, radius, radius, basePaint)

            canvas.drawRoundRect(rect.apply { set(screenWidth * frame - x, 0f, screenWidth * frame - x + progressLength / 2, height) }, 0f, 0f, deepPaintLeft.apply { shader.setLocalMatrix(m.apply { setTranslate(screenWidth * frame - x, 0f) }) })
            canvas.drawRoundRect(rect.apply { set(screenWidth * frame - x + progressLength / 2, 0f, screenWidth * frame - x + progressLength, height) }, 0f, 0f, deepPaintRight.apply { shader.setLocalMatrix(m.apply { setTranslate(screenWidth * frame - x + progressLength / 2, 0f) }) })

            if (screenWidth - (screenWidth * frame + progressLength) < 0) {
                canvas.drawRoundRect(rect.apply { set(screenWidth * frame - x - screenWidth, 0f, screenWidth * frame - x + progressLength / 2 - screenWidth, height) }, 0f, 0f, deepPaintLeft.apply { shader.setLocalMatrix(m.apply { setTranslate(screenWidth * frame - x - screenWidth, 0f) }) })
                canvas.drawRoundRect(rect.apply { set(screenWidth * frame - x + progressLength / 2 - screenWidth, 0f, screenWidth * frame - x + progressLength - screenWidth, height) }, 0f, 0f, deepPaintRight.apply { shader.setLocalMatrix(m.apply { setTranslate(screenWidth * frame - x + progressLength / 2 - screenWidth, 0f) }) })
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop()
    }

    fun start() =
            with(animator) {
                duration = durationOfPass
                startDelay = interval
                repeatCount = ObjectAnimator.INFINITE
                start()
            }

    fun stop() = with(animator) { if (isRunning) cancel() }
}