package com.yang.dragview

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.LinearLayout
import androidx.annotation.IntDef

class DragLinearLayout : LinearLayout {

    //拖拽的方向
    @DragOrientation
    private var mDragOrientation = DRAG_ORIENTATION_ALL

    private var mTouchSlop: Int = 0

    private var mDragPointerId = -1
    private var mInitialTouchX = 0
    private var mInitialTouchY = 0

    private var mIsDragging: Boolean = false

    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle){
        val vc = ViewConfiguration.get(context)
        mTouchSlop = vc.scaledTouchSlop
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val action: Int = ev.actionMasked
        val actionIndex: Int = ev.actionIndex
        when(action){

            MotionEvent.ACTION_DOWN -> {
                onPointerDown(ev, 0)
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                onPointerDown(ev, actionIndex)
            }

            MotionEvent.ACTION_MOVE -> {
                val index: Int = ev.findPointerIndex(mDragPointerId)
                if (index < 0) {
                    return false
                }
                val x = (ev.getX(index) + 0.5f).toInt()
                val y = (ev.getY(index) + 0.5f).toInt()
                if (!mIsDragging){
                    val dx = x - mInitialTouchX
                    val dy = y - mInitialTouchY
                    mIsDragging = (canDragHorizontal() && Math.abs(dx) > mTouchSlop) ||
                            (canDragVertical() && Math.abs(dy) > mTouchSlop)
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                onPointerUp(ev)
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                mIsDragging = false
            }
        }
        return mIsDragging
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val action: Int = ev.actionMasked
        val actionIndex: Int = ev.actionIndex

        var dispatchSuper = true

        when(action){

            MotionEvent.ACTION_DOWN -> {
                onPointerDown(ev, 0)
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                onPointerDown(ev, actionIndex)
            }

            MotionEvent.ACTION_MOVE -> {
                val index: Int = ev.findPointerIndex(mDragPointerId)
                if (index < 0) {
                    return false
                }

                val x = (ev.getX(index) + 0.5f).toInt()
                val y = (ev.getY(index) + 0.5f).toInt()

                //view跟随手指，就是保持最初点击的点，在原始位置。坐标系一直在变
                val dx = x - mInitialTouchX
                val dy = y - mInitialTouchY

                if (!mIsDragging) {
                    mIsDragging = (canDragHorizontal() && Math.abs(dx) > mTouchSlop) ||
                            (canDragVertical() && Math.abs(dy) > mTouchSlop)
                }

                if(mIsDragging){
                    parent?.requestDisallowInterceptTouchEvent(true)
                    drag(dx, dy)
                    dispatchSuper = true
                }

                Log.i(TAG, "onTouchEvent:  xy = [$x, $y]  initXY = [$mInitialTouchX, $mInitialTouchY]   dxy = [$dx, $dy]   translationXY = [${translationX}, ${translationY}]")
            }

            MotionEvent.ACTION_POINTER_UP -> {
                onPointerUp(ev)
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                if (mIsDragging) {
                    //取消super的点击事件的监听
                    dispatchSuper = false
                    val cancelEvent = MotionEvent.obtain(ev)
                    cancelEvent.action = MotionEvent.ACTION_CANCEL
                    super.onTouchEvent(cancelEvent)
                }
                mIsDragging = false
            }
        }

        Log.i(TAG, "onTouchEvent: $mIsDragging")
        if (dispatchSuper){
            super.onTouchEvent(ev)
        }
        return true
    }

    private fun onPointerDown(ev: MotionEvent, actionIndex: Int) {
        mDragPointerId = ev.getPointerId(actionIndex)
        mInitialTouchX = (ev.getX(actionIndex) + 0.5f).toInt()
        mInitialTouchY = (ev.getY(actionIndex) + 0.5f).toInt()
    }

    private fun onPointerUp(ev: MotionEvent) {
        val aIndex: Int = ev.actionIndex
        if (ev.getPointerId(aIndex) == mDragPointerId) {
            // Pick a new pointer to pick up the slack.
            val newIndex = if (aIndex == 0) 1 else 0
            mDragPointerId = ev.getPointerId(newIndex)
            //给定一个初始点击坐标
            mInitialTouchX = (ev.getX(newIndex) + 0.5f).toInt()
            mInitialTouchY = (ev.getY(newIndex) + 0.5f).toInt()
        }
    }

    private fun drag(dx: Int, dy: Int) {
        if (canDragHorizontal()){
            translationX += dx
        }

        if (canDragVertical()){
            translationY += dy
        }
    }

    open fun canDragHorizontal(): Boolean{
        return mDragOrientation.and(DRAG_ORIENTATION_HORIZONTAL) != 0
    }

    open fun canDragVertical(): Boolean{
        return mDragOrientation.and(DRAG_ORIENTATION_VERTICAL) != 0
    }

    fun setDragOrientation(@DragOrientation dragOrientation: Int){
        this.mDragOrientation = dragOrientation
    }

    companion object {
        const val DRAG_ORIENTATION_NONE: Int                = 0
        const val DRAG_ORIENTATION_HORIZONTAL: Int          = 2
        const val DRAG_ORIENTATION_VERTICAL: Int            = 4
        const val DRAG_ORIENTATION_ALL: Int                 = 6

        const val TAG = "DragLinearLayout"
    }

    @IntDef(DRAG_ORIENTATION_NONE, DRAG_ORIENTATION_HORIZONTAL, DRAG_ORIENTATION_VERTICAL, DRAG_ORIENTATION_ALL)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class DragOrientation
}