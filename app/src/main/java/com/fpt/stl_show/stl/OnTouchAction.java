package com.fpt.stl_show.stl;

import android.view.MotionEvent;

/**
 * <pre>
 *   @author  : tocci.feng
 *   e-mail  : fengfei0205@gmail.com
 *   time    : 2020/11/23 09:17
 *   desc    : 触屏动作
 * </pre>
 */
public interface OnTouchAction {
    /**
     * 按下
     */
    int ACTION_DOWN = 1;
    /**
     * 多指按下
     */
    int ACTION_POINTER_DOWN = 2;
    /**
     * 移动
     */
    int ACTION_MOVE = 0;
    /**
     * 抬起
     */
    int ACTION_UP = -1;
    /**
     * 多指抬起
     */
    int ACTION_POINTER_UP = -2;

    /**
     * 触摸动作
     * @param action    动作flag
     * @param event     触摸事件
     */
    void onTouchAction(int action, MotionEvent event);

}
