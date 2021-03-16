package com.fpt.stl_show.camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.fpt.stl_show.camera.drawer.CameraDrawer;
import com.fpt.stl_show.camera.drawer.ScreenDrawer;
import com.fpt.stl_show.util.MatrixState;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * <pre>
 *   @author  : tocci.feng
 *   e-mail  : fengfei0205@gmail.com
 *   time    : 2021/03/11 14:13
 *   desc    : 相机预览render
 * </pre>
 */
public class CameraViewRender implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener, Camera2Helper.OnPreviewSizeListener {

    private final GLSurfaceView glSurfaceView;

    private Context context;

    private int[] texturesId;

    private Camera2Helper camera2Helper;

    private SurfaceTexture surfaceTexture;

    private CameraDrawer cameraDrawer;

    private ScreenDrawer screenDrawer;

    private int mPreviewWidth;

    private int mPreviewHeight;

    private int screenSurfaceWid;

    private int screenSurfaceHeight;

    private int screenX;

    private int screenY;

    private float[] matrix = new float[16];

    public CameraViewRender(CameraView glSurfaceView) {
        this.glSurfaceView = glSurfaceView;
        this.context = glSurfaceView.getContext();

        camera2Helper = new Camera2Helper((Activity)context);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        // 创建一个纹理
        texturesId = new int[1];
        GLES20.glGenTextures(texturesId.length, texturesId, 0);

        // 将纹理和离屏buffer绑定
        surfaceTexture = new SurfaceTexture(texturesId[0]);

        // 设置FrameAvailable监听
        surfaceTexture.setOnFrameAvailableListener(this);

        // 使用fbo将samplerExternalOES 输入到sampler2D中
        cameraDrawer = new CameraDrawer(context);

        // 负责将图像绘制到屏幕上
        screenDrawer = new ScreenDrawer(context);

        // 初始化变换矩阵
        MatrixState.setInitStack();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        // 设置视窗大小及位置
        GLES20.glViewport(0, 0, width, height);

        // 预览尺寸监听
        camera2Helper.setPreviewSizeListener(this);
        // 打开相机
        camera2Helper.openCamera(width, height, surfaceTexture);

        float scaleX = (float) mPreviewHeight / (float) width;
        float scaleY = (float) mPreviewWidth / (float) height;

        float max = Math.max(scaleX, scaleY);

        screenSurfaceWid = (int) (mPreviewHeight / max);
        screenSurfaceHeight = (int) (mPreviewWidth / max);
        screenX = width - (int) (mPreviewHeight / max);
        screenY = height - (int) (mPreviewWidth / max);

        cameraDrawer.surfaceChanged(screenSurfaceWid, screenSurfaceHeight);
        //screenDrawer.surfaceChanged(screenX, screenY, screenSurfaceWid, screenSurfaceHeight);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        // 清理屏幕颜色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // 更新获取一张图
        surfaceTexture.updateTexImage();
        // 获取矩阵
        surfaceTexture.getTransformMatrix(matrix);
        // 映射到cameraDrawer中
        cameraDrawer.setMatrix(matrix);

        // 画camera内容
        int textureId = cameraDrawer.drawTexture(texturesId[0]);
        // 画screen内容
        screenDrawer.drawTexture(textureId);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        // 手动调用渲染
        glSurfaceView.requestRender();
    }

    @Override
    public void onSize(int width, int height) {
        mPreviewWidth = width;
        mPreviewHeight = height;
    }

    public void onSurfaceDestroyed() {
        if (camera2Helper != null) {
            camera2Helper.closeCamera();
            camera2Helper.setPreviewSizeListener(null);
        }

        if (cameraDrawer != null) {
            cameraDrawer.release();
        }

        if (screenDrawer != null) {
            screenDrawer.release();
        }
    }

}
