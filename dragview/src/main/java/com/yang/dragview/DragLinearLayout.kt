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
    private var mLastTouchX = 0
    private var mLastTouchY = 0

    //拖拽的偏移量
    private val mDragOffsets = IntArray(2)

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
                mDragPointerId = ev.getPointerId(0)
                mInitialTouchX = (ev.x + 0.5f).toInt()
                mInitialTouchY = (ev.y + 0.5f).toInt()
                mLastTouchX = mInitialTouchX
                mLastTouchY = mInitialTouchY
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                mDragPointerId = ev.getPointerId(actionIndex)
                mInitialTouchX = (ev.getX(actionIndex) + 0.5f).toInt()
                mInitialTouchY = (ev.getY(actionIndex) + 0.5f).toInt()
                mLastTouchX = mInitialTouchX
                mLastTouchY = mInitialTouchY
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
                val aIndex: Int = ev.actionIndex
                if (ev.getPointerId(aIndex) == mDragPointerId) {
                    // Pick a new pointer to pick up the slack.
                    val newIndex = if (aIndex == 0) 1 else 0
                    mDragPointerId = ev.getPointerId(newIndex)
                    mLastTouchX = (ev.getX(newIndex) + 0.5f).toInt()
                    mLastTouchY = (ev.getY(newIndex) + 0.5f).toInt()
                    mInitialTouchX = mLastTouchX
                    mInitialTouchY = mLastTouchY
                }
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                mIsDragging = false
            }
        }
        Log.i(TAG, "onInterceptTouchEvent: $mIsDragging")
        return mIsDragging
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val action: Int = ev.actionMasked
        val actionIndex: Int = ev.actionIndex

        val vtev = MotionEvent.obtain(ev)
        if (action == MotionEvent.ACTION_DOWN) {
            mDragOffsets[0] = 0
            mDragOffsets[1] = 0
        }
        vtev.offsetLocation(mDragOffsets[0].toFloat(), mDragOffsets[1].toFloat())

        var isDragging = mIsDragging

        when(action){

            MotionEvent.ACTION_DOWN -> {
                mDragPointerId = ev.getPointerId(0)
                mInitialTouchX = (ev.x + 0.5f).toInt()
                mInitialTouchY = (ev.y + 0.5f).toInt()
                mLastTouchX = mInitialTouchX
                mLastTouchY = mInitialTouchY
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                mDragPointerId = ev.getPointerId(actionIndex)
                mInitialTouchX = (ev.getX(actionIndex) + 0.5f).toInt()
                mInitialTouchY = (ev.getY(actionIndex) + 0.5f).toInt()
                mLastTouchX = mInitialTouchX
                mLastTouchY = mInitialTouchY
            }

            MotionEvent.ACTION_MOVE -> {
                val index: Int = ev.findPointerIndex(mDragPointerId)
                if (index < 0) {
                    return false
                }
                val x = (ev.getX(index) + 0.5f).toInt()
                val y = (ev.getY(index) + 0.5f).toInt()
                val dx = mLastTouchX - x
                val dy = mLastTouchY - y

                if (!isDragging){
                    isDragging = (canDragHorizontal() && Math.abs(dx) > mTouchSlop) ||
                            (canDragVertical() && Math.abs(dy) > mTouchSlop)
                }

                if(isDragging){
                    parent?.requestDisallowInterceptTouchEvent(true)
//                    dragOffsets[0] = 0
//                    dragOffsets[1] = 0
                    drag(dx, dy)
                    mLastTouchX = x
                    mLastTouchY = y
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                val aIndex: Int = ev.actionIndex
                if (ev.getPointerId(aIndex) == mDragPointerId) {
                    // Pick a new pointer to pick up the slack.
                    val newIndex = if (aIndex == 0) 1 else 0
                    mDragPointerId = ev.getPointerId(newIndex)
                    mLastTouchX = (ev.getX(newIndex) + 0.5f).toInt()
                    mInitialTouchX = mLastTouchX
                    mLastTouchY = (ev.getY(newIndex) + 0.5f).toInt()
                    mInitialTouchY = mLastTouchY
                }
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                isDragging = false
            }
        }

        mIsDragging = isDragging
        Log.i(TAG, "onTouchEvent: $isDragging")
        if (mIsDragging){
            mIsDragging
        } else {
            super.onTouchEvent(ev)
        }
        return true
    }

    private fun drag(dx: Int, dy: Int) {
        translationX -= dx
        translationY -= dy
    }


    fun canDragHorizontal(): Boolean{
        return mDragOrientation.and(DRAG_ORIENTATION_HORIZONTAL) != 0
    }

    fun canDragVertical(): Boolean{
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