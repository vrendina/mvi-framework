package com.victorrendina.mvi.sample.framework.slider

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList

/**
 * SlideDownLayout is a container that holds a single child that can be dragged down within the layout.
 * TODO This view is currently a work in progress.
 */
class SlideDownLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    enum class SlideState {
        DRAGGING,
        EXPANDED,
        COLLAPSED
    }

    private val listeners = CopyOnWriteArrayList<SlideListener>()

    private val dragHelper = ViewDragHelper.create(this, 1f, DragHelperCallback())

    private var scrimColor = DEFAULT_SCRIM_COLOR

    private lateinit var panel: View

    private var slideRange = 0
    private var slideState = SlideState.EXPANDED
    private val slideOffset: Float
        get() {
            return (slideRange - panel.top) / slideRange.toFloat()
        }

    private var dragView: WeakReference<View> = WeakReference<View>(null)
    private val dragViewVisibleRect = Rect()

    private var initialMotionX = -1f
    private var initialMotionY = -1f

    private var dragEnabled = true

    init {
        dragHelper.minVelocity = 400.dpToPx()
    }

    fun setDragView(view: View) {
        dragView = WeakReference(view)
    }

    fun setDragEnabled(enabled: Boolean) {
        dragEnabled = enabled
    }

    fun isDragEnabled() = dragEnabled && dragView.get() != null

    fun addSlideListener(listener: SlideListener) {
        synchronized(listeners) {
            listeners.add(listener)
        }
    }

    fun removeSlideListener(listener: SlideListener) {
        synchronized(listeners) {
            listeners.remove(listener)
        }
    }

    private fun getTopPositionForOffset(offset: Float): Int {
        return (measuredHeight - slideRange * offset).toInt()
    }

    private fun dispatchOnSlide(panel: View, offset: Float) {
        synchronized(listeners) {
            listeners.forEach {
                it.onSlide(panel, offset)
            }
        }
    }

    private fun dispatchOnStateChanged(panel: View, state: SlideState) {
        synchronized(listeners) {
            listeners.forEach {
                it.onStateChanged(panel, state)
            }
        }
    }

    private fun setSlideStateInternal(state: SlideState) {
        if (slideState != state) {
            slideState = state
            dispatchOnStateChanged(panel, state)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (childCount != 1) {
            throw IllegalStateException("Sliding layout should contain exactly one child view that slides within this layout.")
        }
        panel = getChildAt(0)
        slideRange = measuredHeight
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!isDragEnabled()) {
            dragHelper.cancel()
            return super.onInterceptTouchEvent(ev)
        }

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                initialMotionX = ev.x
                initialMotionY = ev.y
            }
        }
        return dragHelper.shouldInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (!isDragEnabled()) {
            dragHelper.cancel()
            return super.onTouchEvent(ev)
        }
        dragHelper.processTouchEvent(ev)
        return true
    }

    private fun updateBackgroundColor() {
        if (slideOffset < 1f) {
            val baseAlpha = (scrimColor and 0xFF000000.toInt()) ushr 24
            val color = (baseAlpha * slideOffset).toInt() shl 24 or (scrimColor and 0xFFFFFF)
            setBackgroundColor(color)
        } else {
            background = null
        }
    }

    override fun computeScroll() {
        if (dragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    inner class DragHelperCallback : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            val view = dragView.get()
            if (view != null) {
                view.getGlobalVisibleRect(dragViewVisibleRect)
                return dragViewVisibleRect.contains(initialMotionX.toInt(), initialMotionY.toInt())
            }
            return false
        }

        override fun onViewDragStateChanged(state: Int) {
            when (state) {
                ViewDragHelper.STATE_DRAGGING -> setSlideStateInternal(SlideState.DRAGGING)
                ViewDragHelper.STATE_IDLE -> {
                    val slideState = if (slideOffset == 1f) SlideState.EXPANDED else SlideState.COLLAPSED
                    setSlideStateInternal(slideState)
                }
            }
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            val direction = -yvel
            val offsetTarget = when {
                direction > 0 -> 1f  // moving up
                direction < 0 -> 0f // moving down
                slideOffset >= 0.5f -> 1f // < min velocity, keep open unless below 50%
                else -> 0f
            }

            dragHelper.settleCapturedViewAt(releasedChild.left, getTopPositionForOffset(offsetTarget))
            invalidate()
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            dispatchOnSlide(panel, slideOffset)
            updateBackgroundColor()
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            val topBound = paddingTop
            val bottomBound = height

            return Math.min(Math.max(top, topBound), bottomBound)
        }

        override fun getViewVerticalDragRange(child: View): Int {
            return slideRange
        }
    }

    private fun Int.dpToPx() = context.resources.displayMetrics.density * this

    companion object {
        private val TAG = SlideDownLayout::class.java.simpleName

        private const val DEFAULT_SCRIM_COLOR = 0x99000000.toInt()
    }

    interface SlideListener {
        fun onSlide(view: View, offset: Float)

        fun onStateChanged(view: View, state: SlideState)
    }
}