package com.fpt.stl_show;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.fpt.stl_show.stl.OnTouchAction;
import com.fpt.stl_show.stl.STLModel;

/**
 * <pre>
 *   @author  : tocci.feng
 *   e-mail  : fengfei0205@gmail.com
 *   time    : 2020/11/17 09:42
 *   desc    : 显示
 * </pre>
 */
public class DisplayView extends GLSurfaceView {

    private DisplayRender mRender;

    /**
     * render初始化是/否成功
     */
    private boolean renderSet = false;

    /**
     * 触摸动作
     */
    private OnTouchAction action;

    public DisplayView(Context context) {
        super(context);
        init(context, null);
    }

    public DisplayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setEGLContextClientVersion2(context);
    }

    private void setEGLContextClientVersion2(Context context){
        // Check if the system supports OpenGL ES 2.0.
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager
                .getDeviceConfigurationInfo();
        // Even though the latest emulator supports OpenGL ES 2.0,
        // it has a bug where it doesn't set the reqGlEsVersion so
        // the above check doesn't work. The below will detect if the
        // app is running on an emulator, and assume that it supports
        // OpenGL ES 2.0.
        final boolean supportsEs2 =
                configurationInfo.reqGlEsVersion >= 0x20000
                        || Build.FINGERPRINT.startsWith("generic")
                        || Build.FINGERPRINT.startsWith("unknown")
                        || Build.MODEL.contains("google_sdk")
                        || Build.MODEL.contains("Emulator")
                        || Build.MODEL.contains("Android SDK built for x86");

        if (supportsEs2) {
            // Request an OpenGL ES 2.0 compatible context.
            setEGLContextClientVersion(2);

            mRender = new DisplayRender(this);
            setRenderer(mRender);

            renderSet = true;
        } else {
            /*
             * This is where you could create an OpenGL ES 1.x compatible
             * renderer if you wanted to support both ES 1 and ES 2. Since
             * we're not doing anything, the app will crash if the device
             * doesn't support OpenGL ES 2.0. If we publish on the market, we
             * should also add the following to AndroidManifest.xml:
             *
             * <uses-feature android:glEsVersion="0x00020000"
             * android:required="true" />
             *
             * This hides our app from those devices which don't support OpenGL
             * ES 2.0.
             */
            Toast.makeText(context, "This device does not support OpenGL ES 2.0.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return handleTouch(event);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);

        mRender.unregisterTouchEvent();
        mRender.onSurfaceDestroyed();
    }

    public void pause() {
        if (renderSet){
            onPause();
        }
    }

    public void resume() {
        if (renderSet){
            onResume();
        }
    }

    /**
     * GLSurfaceView的触控处理
     * @param event   触摸事件
     * @return        事件消费状态
     */
    private boolean handleTouch(MotionEvent event) {
        if (event != null) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    onActionDown(event);
                case MotionEvent.ACTION_POINTER_DOWN:
                    onActionPointerDown(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    onActionMove(event);
                    break;
                case MotionEvent.ACTION_UP:
                    onActionUp(event);
                case MotionEvent.ACTION_POINTER_UP:
                    onActionPointerUp(event);
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    private void onActionPointerUp(MotionEvent event) {
        if (action == null){
            return;
        }
        queueEvent(() -> {
            try {
                action.onTouchAction(OnTouchAction.ACTION_POINTER_UP, event);
            }catch (IllegalArgumentException e){
                e.printStackTrace();
            }
        });
    }

    private void onActionPointerDown(MotionEvent event) {
        if (action == null){
            return;
        }
        queueEvent(() -> {
            try {
                action.onTouchAction(OnTouchAction.ACTION_POINTER_DOWN, event);
            }catch (IllegalArgumentException e){
                e.printStackTrace();
            }
        });
    }

    private void onActionDown(MotionEvent event) {
        if (action == null){
            return;
        }
        queueEvent(() -> {
            try {
                action.onTouchAction(OnTouchAction.ACTION_DOWN, event);
            }catch (IllegalArgumentException e){
                e.printStackTrace();
            }
        });
    }

    private void onActionMove(MotionEvent event) {
        if (action == null){
            return;
        }
        queueEvent(() -> {
            try {
                action.onTouchAction(OnTouchAction.ACTION_MOVE, event);
            }catch (IllegalArgumentException e){
                e.printStackTrace();
            }
        });
    }

    private void onActionUp(MotionEvent event) {
        if (action == null){
            return;
        }
        queueEvent(() -> {
            try {
                action.onTouchAction(OnTouchAction.ACTION_UP, event);
            }catch (IllegalArgumentException e){
                e.printStackTrace();
            }
        });
    }

    public void setNewSTLObject(STLModel model) {
        if (!renderSet){
            return;
        }
        queueEvent(() -> {
            try {
                mRender.setNewSTLObject(model);
                action = mRender.registerTouchEvent();
            }catch (IllegalArgumentException e){
                e.printStackTrace();
            }
        });
    }
}
