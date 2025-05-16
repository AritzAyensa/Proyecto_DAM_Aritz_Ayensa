package com.example.proyecto_dam_aritz_ayensa.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.sqrt
/**
 * Clase: ZoomableImageView
 *
 * ImageView personalizada que permite hacer zoom y desplazamiento mediante gestos multitáctiles.
 */
@SuppressLint("ClickableViewAccessibility")
class ZoomableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs), ScaleGestureDetector.OnScaleGestureListener {

    private val matrix = Matrix()
    private val savedMatrix = Matrix()
    private val scaleDetector = ScaleGestureDetector(context, this)

    private var mode = NONE
    private var start = PointF()
    private var mid = PointF()
    private var oldDist = 1f

    private var minScale = 1f
    private var maxScale = 5f
    private var currentScale = 1f

    companion object {
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
    }

    init {
        scaleType = ScaleType.MATRIX
        imageMatrix = matrix
        post {
            // Establecer zoom inicial centrado
            val scale = 1.5f // Ajusta este valor según tus necesidades
            val dx = (width - drawable.intrinsicWidth * scale) / 2
            val dy = (height - drawable.intrinsicHeight * scale) / 2
            matrix.postScale(scale, scale)
            matrix.postTranslate(dx, dy)
            imageMatrix = matrix
            currentScale = scale
        }
    }
    /**
     * Método: onTouchEvent
     *
     * Maneja los eventos táctiles para arrastrar y hacer zoom sobre la imagen.
     *
     * @param event Evento de toque detectado por el usuario.
     * @return true si el evento fue manejado correctamente.
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                savedMatrix.set(matrix)
                start.set(event.x, event.y)
                mode = DRAG
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = spacing(event)
                if (oldDist > 10f) {
                    savedMatrix.set(matrix)
                    midPoint(mid, event)
                    mode = ZOOM
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (mode == DRAG) {
                    matrix.set(savedMatrix)
                    val dx = event.x - start.x
                    val dy = event.y - start.y
                    matrix.postTranslate(dx, dy)
                    fixTranslation()
                } else if (mode == ZOOM) {
                    // El manejo del zoom se realiza en onScale
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                mode = NONE
            }
        }

        imageMatrix = matrix
        return true
    }
    /**
     * Método: onScale
     *
     * Aplica el zoom basado en el gesto detectado.
     *
     * @param detector Detector de escala que proporciona el factor y foco del gesto.
     * @return true si el escalado fue manejado correctamente.
     */
    override fun onScale(detector: ScaleGestureDetector): Boolean {
        val scaleFactor = detector.scaleFactor
        val newScale = currentScale * scaleFactor
        if (newScale in minScale..maxScale) {
            matrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
            currentScale = newScale
            fixTranslation()
        }
        return true
    }
    /**
     * Método: onScaleBegin
     *
     * Indica el inicio de un gesto de escala.
     *
     * @param detector Detector del gesto de escala.
     * @return true para aceptar el gesto.
     */
    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean = true
    /**
     * Método: onScaleEnd
     *
     * Indica el final del gesto de escala.
     *
     * @param detector Detector del gesto de escala.
     */
    override fun onScaleEnd(detector: ScaleGestureDetector) {}
    /**
     * Método: spacing
     *
     * Calcula la distancia entre dos puntos táctiles.
     *
     * @param event Evento multitáctil.
     * @return Distancia entre los dedos.
     */
    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return kotlin.math.sqrt(x * x + y * y)
    }
    /**
     * Método: midPoint
     *
     * Calcula el punto medio entre dos dedos.
     *
     * @param point Objeto donde se guarda el punto medio.
     * @param event Evento multitáctil.
     */
    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point.set(x / 2, y / 2)
    }
    /**
     * Método: fixTranslation
     *
     * Ajusta la posición de la imagen para mantenerla dentro de los límites de la vista.
     */
    private fun fixTranslation() {
        val values = FloatArray(9)
        matrix.getValues(values)
        val transX = values[Matrix.MTRANS_X]
        val transY = values[Matrix.MTRANS_Y]
        val scaleX = values[Matrix.MSCALE_X]
        val scaleY = values[Matrix.MSCALE_Y]

        val drawable = drawable ?: return

        val imageWidth = drawable.intrinsicWidth * scaleX
        val imageHeight = drawable.intrinsicHeight * scaleY

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        var deltaX = 0f
        var deltaY = 0f

        if (imageWidth > viewWidth) {
            if (transX > 0) {
                deltaX = -transX
            } else if (transX + imageWidth < viewWidth) {
                deltaX = viewWidth - (transX + imageWidth)
            }
        } else {
            deltaX = (viewWidth - imageWidth) / 2 - transX
        }

        if (imageHeight > viewHeight) {
            if (transY > 0) {
                deltaY = -transY
            } else if (transY + imageHeight < viewHeight) {
                deltaY = viewHeight - (transY + imageHeight)
            }
        } else {
            deltaY = (viewHeight - imageHeight) / 2 - transY
        }

        matrix.postTranslate(deltaX, deltaY)
    }
}
