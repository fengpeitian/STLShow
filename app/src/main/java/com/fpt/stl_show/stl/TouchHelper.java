package com.fpt.stl_show.stl;

import android.graphics.PointF;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

/**
 * <pre>
 *   @author  : tocci.feng
 *   e-mail  : fengfei0205@gmail.com
 *   time    : 2021/03/12 17:05
 *   desc    :
 * </pre>
 */
public class TouchHelper implements OnTouchAction {
    /**
     * 移动/缩放因子
     */
    private final float TOUCH_SCALE_FACTOR = 180.0f / 1080 / 2;
    /**
     * null模式
     */
    private static final int TOUCH_NONE = 0;
    /**
     * 拖拽
     */
    private static final int TOUCH_DRAG = 1;
    /**
     * 缩放
     */
    private static final int TOUCH_ZOOM = 2;
    /**
     * 模式
     */
    private int mTouchMode = TOUCH_NONE;
    /**
     * 触摸
     */
    private boolean canTouch = true;
    /**
     * 缩放
     */
    private boolean canScale = true;
    /**
     * 旋转
     */
    private boolean canRotate = true;
    /**
     * 缩放距离
     */
    private float mPinchStartDistance = 0.0f;
    /**
     * 缩放中心点坐标
     */
    private PointF mPinchStartPoint = new PointF();
    /**
     * 记录上个点X
     */
    private float mPreviousX;
    /**
     * 记录上个点Y
     */
    private float mPreviousY;
    /**
     * X缩放偏移量
     */
    private float mPinchMoveX = 0.0f;
    /**
     * Y缩放偏移量
     */
    private float mPinchMoveY = 0.0f;
    /**
     * 缩放率(larger > 1.0f > smaller)
     */
    private float mPinchScale = 1.0f;

    private STLModel stlModel;

    public TouchHelper(@NonNull STLModel stlModel) {
        this.stlModel = stlModel;
    }

    @Override
    public void onTouchAction(int action, MotionEvent event) {
        // 不可触摸
        if (!canTouch) {
            return;
        }
        // 双指缩放
        if (canScale) {
            zoomScale(action, event);
        }
        // 单指旋转
        if (canRotate) {
            rotate(action, event);
        }
    }

    public void setTouch(boolean touch) {
        canTouch = touch;
    }

    public void setRotate(boolean rotate) {
        canTouch = true;
        canRotate = rotate;
    }

    public void setScale(boolean scale) {
        canTouch = true;
        canScale = scale;
    }

    /**
     * 双指缩放大小
     */
    private void zoomScale(int action, MotionEvent event) {
        switch (action) {
            case OnTouchAction.ACTION_POINTER_DOWN:
                // 多指操作
                if (event.getPointerCount() >= 2) {
                    // 获取缩放距离
                    mPinchStartDistance = getPinchDistance(event);
                    // 超过误差距离
                    if (mPinchStartDistance > 50.0f) {
                        // 获取中心点
                        getPinchCenterPoint(event, mPinchStartPoint);
                        // 记录中心点
                        mPreviousX = mPinchStartPoint.x;
                        mPreviousY = mPinchStartPoint.y;
                        // 触摸模式为缩放
                        mTouchMode = TOUCH_ZOOM;
                    }
                }
                break;
            case OnTouchAction.ACTION_MOVE:
                // 缩放模式&缩放距离大于0
                if (mTouchMode == TOUCH_ZOOM && mPinchStartDistance > 0) {
                    // 当前点
                    PointF pt = new PointF();
                    // 获取缩放中心点
                    getPinchCenterPoint(event, pt);
                    // 计算偏移量
                    mPinchMoveX = pt.x - mPreviousX;
                    mPinchMoveY = pt.y - mPreviousY;
                    // 缓存偏移量至dx，dy
                    float dx = mPinchMoveX;
                    float dy = mPinchMoveY;
                    // 重置记录中心点
                    mPreviousX = pt.x;
                    mPreviousY = pt.y;
                    // 判断同时是否开启旋转功能
                    if (canRotate) {
                        // 设置render的angleX
                        stlModel.xRotateAngle += dx * TOUCH_SCALE_FACTOR;
                        // 设置render的angleY
                        stlModel.yRotateAngle += dy * TOUCH_SCALE_FACTOR;
                    } else {
                        // 设置render的positionX
                        stlModel.xTranslate += dx * TOUCH_SCALE_FACTOR / 5;
                        // 设置render的positionY
                        stlModel.yTranslate += dy * TOUCH_SCALE_FACTOR / 5;
                    }
                    // 获取缩放比例
                    mPinchScale = getPinchDistance(event) / mPinchStartDistance;
                    // 设置render的scale
                    stlModel.zScale = mPinchScale;
                    stlModel.yScale = mPinchScale;
                    stlModel.zScale = mPinchScale;
                }
                break;
            case OnTouchAction.ACTION_UP:
            case OnTouchAction.ACTION_POINTER_UP:
                // 缩放模式
                if (mTouchMode == TOUCH_ZOOM) {
                    // 重置缩放过程中所有状态
                    mTouchMode = TOUCH_NONE;
                    mPinchMoveX = 0.0f;
                    mPinchMoveY = 0.0f;
                    mPinchScale = 1.0f;
                    mPinchStartPoint.x = 0.0f;
                    mPinchStartPoint.y = 0.0f;
                }
                break;
            default:
                break;
        }
    }

    /**
     * 单指旋转model
     */
    private void rotate(int action, MotionEvent event) {
        switch (action){
            case OnTouchAction.ACTION_DOWN:
                // 单点操作
                if (mTouchMode == TOUCH_NONE && event.getPointerCount() == 1) {
                    // 设置模式为拖拽
                    mTouchMode = TOUCH_DRAG;
                    // 记录点击点
                    mPreviousX = event.getX();
                    mPreviousY = event.getY();
                }
                break;
            case OnTouchAction.ACTION_MOVE:
                if (mTouchMode == TOUCH_DRAG) {
                    // 获取当前点
                    float x = event.getX();
                    float y = event.getY();
                    // 计算偏移量
                    float dx = x - mPreviousX;
                    float dy = y - mPreviousY;
                    // 记录当前触摸点
                    mPreviousX = x;
                    mPreviousY = y;
                    // 旋转功能
                    if (Math.abs(dx) > Math.abs(dy)) {
                        // 设置render的angleX
                        stlModel.xRotateAngle = (stlModel.xRotateAngle - dx * TOUCH_SCALE_FACTOR) % 360.0f;
                    } else {
                        // 设置render的angleY
                        stlModel.yRotateAngle = (stlModel.yRotateAngle + dy * TOUCH_SCALE_FACTOR) % 360.0f;
                    }
                }
                break;
            case OnTouchAction.ACTION_UP:
                if (mTouchMode == TOUCH_DRAG) {
                    mTouchMode = TOUCH_NONE;
                    break;
                }
                break;
            default:
                break;
        }
    }

    /**
     * 获取缩放距离
     * @param event  点击事件
     * @return       距离
     */
    private float getPinchDistance(MotionEvent event) {
        float x = 0;
        float y = 0;
        try {
            x = event.getX(0) - event.getX(1);
            y = event.getY(0) - event.getY(1);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 获取缩放中心点
     * @param event   点击事件
     * @param pt      中心点
     */
    private void getPinchCenterPoint(MotionEvent event, PointF pt) {
        pt.x = (event.getX(0) + event.getX(1)) * 0.5f;
        pt.y = (event.getY(0) + event.getY(1)) * 0.5f;
    }

}
