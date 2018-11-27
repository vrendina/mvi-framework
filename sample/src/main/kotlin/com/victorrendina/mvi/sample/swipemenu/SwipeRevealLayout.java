package com.victorrendina.mvi.sample.swipemenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.victorrendina.mvi.sample.R;

/**
 * Modified from SwipeRevealLayout
 * https://github.com/chthai64/SwipeRevealLayout
 */
public class SwipeRevealLayout extends ViewGroup {

    protected static final int STATE_CLOSED = 0;
    protected static final int STATE_CLOSING = 1;
    protected static final int STATE_OPEN = 2;
    protected static final int STATE_OPENING = 3;
    protected static final int STATE_DRAGGING = 4;

    private static final int DEFAULT_MIN_FLING_VELOCITY = 300; // dp per second
    private static final int DEFAULT_MIN_DIST_REQUEST_DISALLOW_PARENT = 1; // dp

    public static final int DRAG_EDGE_LEFT = 0x1;
    public static final int DRAG_EDGE_RIGHT = 0x1 << 1;

    /**
     * The secondary view will stick the edge of the main view. This is the default behavior.
     */
    public static final int MODE_EDGE = 0;

    /**
     * The secondary view will be under the main view.
     */
    public static final int MODE_UNDER = 1;

    /**
     * Main view is the view which is shown when the layout is closed.
     */
    private View mainView;

    /**
     * Secondary view is the view which is shown when the layout is opened.
     */
    private View secondaryView;

    /**
     * The rectangle position of the main view when the layout is closed.
     */
    private Rect rectMainClosed = new Rect();

    /**
     * The rectangle position of the main view when the layout is opened.
     */
    private Rect rectMainOpen = new Rect();

    /**
     * The rectangle position of the secondary view when the layout is closed.
     */
    private Rect rectSecClosed = new Rect();

    /**
     * The rectangle position of the secondary view when the layout is opened.
     */
    private Rect rectSecOpen = new Rect();

    /**
     * The minimum distance the view must be dragged before the parent will be told to
     * disallow intercepting touch events.
     */
    private int minDistRequestDisallowParent = 0;

    private boolean isOpenBeforeInit = false;
    private volatile boolean aborted = false;
    private volatile boolean isScrolling = false;
    private volatile boolean lockDrag = false;

    private int minFlingVelocity = DEFAULT_MIN_FLING_VELOCITY;
    private int state = STATE_CLOSED;
    private int mode = MODE_EDGE;

    private int dragEdge = DRAG_EDGE_RIGHT;

    private float dragDistance = 0;
    private float prevX = -1;

    private ViewDragHelper dragHelper;
    private GestureDetectorCompat gestureDetector;

    private DragStateChangeListener dragStateChangeListener;

    interface DragStateChangeListener {
        void onDragStateChanged(int state);
    }

    public SwipeRevealLayout(Context context) {
        super(context);
        init(context, null);
    }

    public SwipeRevealLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SwipeRevealLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        dragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isDragLocked()) {
            return super.onInterceptTouchEvent(ev);
        }

        dragHelper.processTouchEvent(ev);
        gestureDetector.onTouchEvent(ev);
        accumulateDragDist(ev);

        boolean couldBecomeClick = couldBecomeClick(ev);
        boolean settling = dragHelper.getViewDragState() == ViewDragHelper.STATE_SETTLING;
        boolean idleAfterScrolled = dragHelper.getViewDragState() == ViewDragHelper.STATE_IDLE
                && isScrolling;

        // must be placed as the last statement
        prevX = ev.getX();

        // return true => intercept, cannot trigger onClick event
        return !couldBecomeClick && (settling || idleAfterScrolled);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // get views
        if (getChildCount() >= 2) {
            secondaryView = getChildAt(0);
            mainView = getChildAt(1);
        } else if (getChildCount() == 1) {
            mainView = getChildAt(0);
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        aborted = false;

        for (int index = 0; index < getChildCount(); index++) {
            final View child = getChildAt(index);

            int left, right, top, bottom;
            left = right = top = bottom = 0;

            final int minLeft = getPaddingLeft();
            final int maxRight = Math.max(r - getPaddingRight() - l, 0);
            final int minTop = getPaddingTop();
            final int maxBottom = Math.max(b - getPaddingBottom() - t, 0);

            int measuredChildHeight = child.getMeasuredHeight();
            int measuredChildWidth = child.getMeasuredWidth();

            // need to take account if child size is match_parent
            final LayoutParams childParams = child.getLayoutParams();
            boolean matchParentHeight = false;
            boolean matchParentWidth = false;

            if (childParams != null) {
                matchParentHeight = (childParams.height == LayoutParams.MATCH_PARENT);
                matchParentWidth = (childParams.width == LayoutParams.MATCH_PARENT);
            }

            if (matchParentHeight) {
                measuredChildHeight = maxBottom - minTop;
                childParams.height = measuredChildHeight;
            }

            if (matchParentWidth) {
                measuredChildWidth = maxRight - minLeft;
                childParams.width = measuredChildWidth;
            }

            switch (dragEdge) {
                case DRAG_EDGE_RIGHT:
                    left = Math.max(r - measuredChildWidth - getPaddingRight() - l, minLeft);
                    top = Math.min(getPaddingTop(), maxBottom);
                    right = Math.max(r - getPaddingRight() - l, minLeft);
                    bottom = Math.min(measuredChildHeight + getPaddingTop(), maxBottom);
                    break;

                case DRAG_EDGE_LEFT:
                    left = Math.min(getPaddingLeft(), maxRight);
                    top = Math.min(getPaddingTop(), maxBottom);
                    right = Math.min(measuredChildWidth + getPaddingLeft(), maxRight);
                    bottom = Math.min(measuredChildHeight + getPaddingTop(), maxBottom);
                    break;
            }

            child.layout(left, top, right, bottom);
        }

        // taking account offset when mode is MODE_EDGE
        if (mode == MODE_EDGE) {
            switch (dragEdge) {
                case DRAG_EDGE_LEFT:
                    secondaryView.offsetLeftAndRight(-secondaryView.getWidth());
                    break;

                case DRAG_EDGE_RIGHT:
                    secondaryView.offsetLeftAndRight(secondaryView.getWidth());
                    break;
            }
        }

        initRects();

        if (isOpenBeforeInit) {
            open(false);
        } else {
            close(false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getChildCount() < 2) {
            throw new RuntimeException("Layout must have two children");
        }

        final LayoutParams params = getLayoutParams();

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int desiredWidth = 0;
        int desiredHeight = 0;

        // first find the largest child
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            desiredWidth = Math.max(child.getMeasuredWidth(), desiredWidth);
            desiredHeight = Math.max(child.getMeasuredHeight(), desiredHeight);
        }
        // create new measure spec using the largest child width
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(desiredWidth, widthMode);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(desiredHeight, heightMode);

        final int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);

        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            final LayoutParams childParams = child.getLayoutParams();

            if (childParams != null) {
                if (childParams.height == LayoutParams.MATCH_PARENT) {
                    child.setMinimumHeight(measuredHeight);
                }

                if (childParams.width == LayoutParams.MATCH_PARENT) {
                    child.setMinimumWidth(measuredWidth);
                }
            }

            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            desiredWidth = Math.max(child.getMeasuredWidth(), desiredWidth);
            desiredHeight = Math.max(child.getMeasuredHeight(), desiredHeight);
        }

        // taking accounts of padding
        desiredWidth += getPaddingLeft() + getPaddingRight();
        desiredHeight += getPaddingTop() + getPaddingBottom();

        // adjust desired width
        if (widthMode == MeasureSpec.EXACTLY) {
            desiredWidth = measuredWidth;
        } else {
            if (params.width == LayoutParams.MATCH_PARENT) {
                desiredWidth = measuredWidth;
            }

            if (widthMode == MeasureSpec.AT_MOST) {
                desiredWidth = (desiredWidth > measuredWidth) ? measuredWidth : desiredWidth;
            }
        }

        // adjust desired height
        if (heightMode == MeasureSpec.EXACTLY) {
            desiredHeight = measuredHeight;
        } else {
            if (params.height == LayoutParams.MATCH_PARENT) {
                desiredHeight = measuredHeight;
            }

            if (heightMode == MeasureSpec.AT_MOST) {
                desiredHeight = (desiredHeight > measuredHeight) ? measuredHeight : desiredHeight;
            }
        }

        setMeasuredDimension(desiredWidth, desiredHeight);
    }

    @Override
    public void computeScroll() {
        if (dragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * Open the panel to show the secondary view
     *
     * @param animation true to animate the open motion.
     */
    public void open(boolean animation) {
        isOpenBeforeInit = true;
        aborted = false;

        if (animation) {
            state = STATE_OPENING;
            dragHelper.smoothSlideViewTo(mainView, rectMainOpen.left, rectMainOpen.top);

            if (dragStateChangeListener != null) {
                dragStateChangeListener.onDragStateChanged(state);
            }
        } else {
            state = STATE_OPEN;
            dragHelper.abort();

            mainView.layout(
                    rectMainOpen.left,
                    rectMainOpen.top,
                    rectMainOpen.right,
                    rectMainOpen.bottom
            );

            secondaryView.layout(
                    rectSecOpen.left,
                    rectSecOpen.top,
                    rectSecOpen.right,
                    rectSecOpen.bottom
            );
        }

        ViewCompat.postInvalidateOnAnimation(this);
    }

    /**
     * Close the panel to hide the secondary view
     *
     * @param animation true to animate the close motion.
     */
    public void close(boolean animation) {
        isOpenBeforeInit = false;
        aborted = false;

        if (animation) {
            state = STATE_CLOSING;
            dragHelper.smoothSlideViewTo(mainView, rectMainClosed.left, rectMainClosed.top);

            if (dragStateChangeListener != null) {
                dragStateChangeListener.onDragStateChanged(state);
            }

        } else {
            state = STATE_CLOSED;
            dragHelper.abort();

            mainView.layout(
                    rectMainClosed.left,
                    rectMainClosed.top,
                    rectMainClosed.right,
                    rectMainClosed.bottom
            );

            secondaryView.layout(
                    rectSecClosed.left,
                    rectSecClosed.top,
                    rectSecClosed.right,
                    rectSecClosed.bottom
            );
        }

        ViewCompat.postInvalidateOnAnimation(this);
    }

    /**
     * Set the minimum fling velocity to cause the layout to open/close.
     *
     * @param velocity dp per second
     */
    public void setMinFlingVelocity(int velocity) {
        minFlingVelocity = velocity;
    }

    /**
     * Get the minimum fling velocity to cause the layout to open/close.
     *
     * @return dp per second
     */
    public int getMinFlingVelocity() {
        return minFlingVelocity;
    }

    /**
     * @param lock if set to true, the user cannot drag/swipe the layout.
     */
    public void setLockDrag(boolean lock) {
        lockDrag = lock;
    }

    /**
     * @return true if the drag/swipe motion is currently locked.
     */
    public boolean isDragLocked() {
        return lockDrag;
    }

    /**
     * @return true if layout is fully opened, false otherwise.
     */
    public boolean isOpen() {
        return (state == STATE_OPEN);
    }

    /**
     * @return true if layout is fully closed, false otherwise.
     */
    public boolean isClosed() {
        return (state == STATE_CLOSED);
    }

    /**
     * @param listener Listener for drag state changes.
     */
    public void setDragStateChangeListener(DragStateChangeListener listener) {
        dragStateChangeListener = listener;
    }

    private int getMainOpenLeft() {
        switch (dragEdge) {
            case DRAG_EDGE_LEFT:
                return rectMainClosed.left + secondaryView.getWidth();

            case DRAG_EDGE_RIGHT:
                return rectMainClosed.left - secondaryView.getWidth();

            default:
                return 0;
        }
    }

    private int getMainOpenTop() {
        switch (dragEdge) {
            case DRAG_EDGE_LEFT:
                return rectMainClosed.top;

            case DRAG_EDGE_RIGHT:
                return rectMainClosed.top;

            default:
                return 0;
        }
    }

    private int getSecOpenLeft() {
        if (mode == MODE_UNDER) {
            return rectSecClosed.left;
        }

        if (dragEdge == DRAG_EDGE_LEFT) {
            return rectSecClosed.left + secondaryView.getWidth();
        } else {
            return rectSecClosed.left - secondaryView.getWidth();
        }
    }

    private int getSecOpenTop() {
        return rectSecClosed.top;
    }

    private void initRects() {
        // close position of main view
        rectMainClosed.set(
                mainView.getLeft(),
                mainView.getTop(),
                mainView.getRight(),
                mainView.getBottom()
        );

        // close position of secondary view
        rectSecClosed.set(
                secondaryView.getLeft(),
                secondaryView.getTop(),
                secondaryView.getRight(),
                secondaryView.getBottom()
        );

        // open position of the main view
        rectMainOpen.set(
                getMainOpenLeft(),
                getMainOpenTop(),
                getMainOpenLeft() + mainView.getWidth(),
                getMainOpenTop() + mainView.getHeight()
        );

        // open position of the secondary view
        rectSecOpen.set(
                getSecOpenLeft(),
                getSecOpenTop(),
                getSecOpenLeft() + secondaryView.getWidth(),
                getSecOpenTop() + secondaryView.getHeight()
        );
    }

    private boolean couldBecomeClick(MotionEvent ev) {
        return isInMainView(ev) && !shouldInitiateADrag();
    }

    private boolean isInMainView(MotionEvent ev) {
        float x = ev.getX();
        float y = ev.getY();

        boolean withinVertical = mainView.getTop() <= y && y <= mainView.getBottom();
        boolean withinHorizontal = mainView.getLeft() <= x && x <= mainView.getRight();

        return withinVertical && withinHorizontal;
    }

    private boolean shouldInitiateADrag() {
        float minDistToInitiateDrag = dragHelper.getTouchSlop();
        return dragDistance >= minDistToInitiateDrag;
    }

    private void accumulateDragDist(MotionEvent ev) {
        final int action = ev.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            dragDistance = 0;
            return;
        }

        dragDistance += Math.abs(ev.getX() - prevX);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null && context != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.SwipeRevealLayout,
                    0, 0
            );

            dragEdge = a.getInteger(R.styleable.SwipeRevealLayout_srl_dragEdge, DRAG_EDGE_RIGHT);
            mode = a.getInteger(R.styleable.SwipeRevealLayout_srl_mode, MODE_EDGE);
            minFlingVelocity = a.getInteger(R.styleable.SwipeRevealLayout_srl_flingVelocity, DEFAULT_MIN_FLING_VELOCITY);

            minDistRequestDisallowParent = dpToPx(DEFAULT_MIN_DIST_REQUEST_DISALLOW_PARENT);
        }

        dragHelper = ViewDragHelper.create(this, 1.0f, mDragHelperCallback);
        dragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_ALL);

        gestureDetector = new GestureDetectorCompat(context, mGestureListener);
    }

    private final GestureDetector.OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        boolean hasDisallowed = false;

        @Override
        public boolean onDown(MotionEvent e) {
            isScrolling = false;
            hasDisallowed = false;
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            isScrolling = true;
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            isScrolling = true;

            if (getParent() != null) {
                boolean shouldDisallow;

                if (!hasDisallowed) {
                    shouldDisallow = getDistToClosestEdge() >= minDistRequestDisallowParent;
                    if (shouldDisallow) {
                        hasDisallowed = true;
                    }
                } else {
                    shouldDisallow = true;
                }

                // disallow parent to intercept touch event so that the layout will work
                // properly on RecyclerView or view that handles scroll gesture.
                getParent().requestDisallowInterceptTouchEvent(shouldDisallow);
            }

            return false;
        }
    };

    private int getDistToClosestEdge() {
        switch (dragEdge) {
            case DRAG_EDGE_LEFT:
                final int pivotRight = rectMainClosed.left + secondaryView.getWidth();

                return Math.min(
                        mainView.getLeft() - rectMainClosed.left,
                        pivotRight - mainView.getLeft()
                );

            case DRAG_EDGE_RIGHT:
                final int pivotLeft = rectMainClosed.right - secondaryView.getWidth();

                return Math.min(
                        mainView.getRight() - pivotLeft,
                        rectMainClosed.right - mainView.getRight()
                );
        }

        return 0;
    }

    private int getHalfwayPivotHorizontal() {
        if (dragEdge == DRAG_EDGE_LEFT) {
            return rectMainClosed.left + secondaryView.getWidth() / 2;
        } else {
            return rectMainClosed.right - secondaryView.getWidth() / 2;
        }
    }

    private final ViewDragHelper.Callback mDragHelperCallback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            aborted = false;

            if (lockDrag)
                return false;

            dragHelper.captureChildView(mainView, pointerId);
            return false;
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            return child.getTop();
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            switch (dragEdge) {
                case DRAG_EDGE_RIGHT:
                    return Math.max(
                            Math.min(left, rectMainClosed.left),
                            rectMainClosed.left - secondaryView.getWidth()
                    );

                case DRAG_EDGE_LEFT:
                    return Math.max(
                            Math.min(left, rectMainClosed.left + secondaryView.getWidth()),
                            rectMainClosed.left
                    );

                default:
                    return child.getLeft();
            }
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            final boolean velRightExceeded = pxToDp((int) xvel) >= minFlingVelocity;
            final boolean velLeftExceeded = pxToDp((int) xvel) <= -minFlingVelocity;
            final int pivotHorizontal = getHalfwayPivotHorizontal();

            switch (dragEdge) {
                case DRAG_EDGE_RIGHT:
                    if (velRightExceeded) {
                        close(true);
                    } else if (velLeftExceeded) {
                        open(true);
                    } else {
                        if (mainView.getRight() < pivotHorizontal) {
                            open(true);
                        } else {
                            close(true);
                        }
                    }
                    break;

                case DRAG_EDGE_LEFT:
                    if (velRightExceeded) {
                        open(true);
                    } else if (velLeftExceeded) {
                        close(true);
                    } else {
                        if (mainView.getLeft() < pivotHorizontal) {
                            close(true);
                        } else {
                            open(true);
                        }
                    }
                    break;
            }
        }

        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            super.onEdgeDragStarted(edgeFlags, pointerId);

            if (lockDrag) {
                return;
            }

            boolean edgeStartLeft = (dragEdge == DRAG_EDGE_RIGHT)
                    && edgeFlags == ViewDragHelper.EDGE_LEFT;

            boolean edgeStartRight = (dragEdge == DRAG_EDGE_LEFT)
                    && edgeFlags == ViewDragHelper.EDGE_RIGHT;

            if (edgeStartLeft || edgeStartRight) {
                dragHelper.captureChildView(mainView, pointerId);
            }
        }

        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            if (mode == MODE_EDGE) {
                if (dragEdge == DRAG_EDGE_LEFT || dragEdge == DRAG_EDGE_RIGHT) {
                    secondaryView.offsetLeftAndRight(dx);
                } else {
                    secondaryView.offsetTopAndBottom(dy);
                }
            }

            ViewCompat.postInvalidateOnAnimation(SwipeRevealLayout.this);
        }

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
            final int prevState = SwipeRevealLayout.this.state;

            switch (state) {
                case ViewDragHelper.STATE_DRAGGING:
                    SwipeRevealLayout.this.state = STATE_DRAGGING;
                    break;

                case ViewDragHelper.STATE_IDLE:

                    // drag edge is left or right
                    if (dragEdge == DRAG_EDGE_LEFT || dragEdge == DRAG_EDGE_RIGHT) {
                        if (mainView.getLeft() == rectMainClosed.left) {
                            SwipeRevealLayout.this.state = STATE_CLOSED;
                        } else {
                            SwipeRevealLayout.this.state = STATE_OPEN;
                        }
                    }

                    // drag edge is top or bottom
                    else {
                        if (mainView.getTop() == rectMainClosed.top) {
                            SwipeRevealLayout.this.state = STATE_CLOSED;
                        } else {
                            SwipeRevealLayout.this.state = STATE_OPEN;
                        }
                    }
                    break;
            }

            if (dragStateChangeListener != null && !aborted && prevState != SwipeRevealLayout.this.state) {
                dragStateChangeListener.onDragStateChanged(SwipeRevealLayout.this.state);
            }
        }
    };

    private int pxToDp(int px) {
        Resources resources = getContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int) (px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private int dpToPx(int dp) {
        Resources resources = getContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int) (dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}