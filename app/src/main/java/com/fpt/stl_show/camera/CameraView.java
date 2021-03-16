package com.fpt.stl_show.camera;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * <pre>
 *   @author  : tocci.feng
 *   e-mail  : fengfei0205@gmail.com
 *   time    : 2021/03/11 14:07
 *   desc    : openGL相机预览
 * </pre>
 */
public class CameraView extends GLSurfaceView {

    private CameraViewRender mRender;

    public CameraView(Context context) {
        super(context);
        init(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (checkSupported(context)) {
            // 设置EGL版本
            setEGLContextClientVersion(2);
            // 设置render
            mRender = new CameraViewRender(this);
            // 设置render
            setRenderer(mRender);
            // 设置渲染模式(当需要重绘时，调用GLSurfaceView.requestRender())
            // GLSurfaceView.RENDERMODE_CONTINUOUSLY  自动模式
            // GLSurfaceView.RENDERMODE_WHEN_DIRTY    手动模式
            setRenderMode(RENDERMODE_WHEN_DIRTY);
        }else{
            System.out.println("this phone can't support OpenGl ES 2.0!");
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);

        mRender.onSurfaceDestroyed();
    }

    /**
     * 检验机器是否支持OpenGl ES2
     * @param context    上下文
     */
    private static boolean checkSupported(Context context) {
        boolean supportsEs2;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        supportsEs2 = configurationInfo.reqGlEsVersion >= 0x2000;

        boolean isEmulator = Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86");

        supportsEs2 = supportsEs2 || isEmulator;
        return supportsEs2;
    }

}
