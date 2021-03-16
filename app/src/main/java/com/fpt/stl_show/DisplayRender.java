package com.fpt.stl_show;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.fpt.stl_show.camera.drawer.CameraDrawer;
import com.fpt.stl_show.camera.drawer.ScreenDrawer;
import com.fpt.stl_show.stl.OnTouchAction;
import com.fpt.stl_show.stl.STLDrawer;
import com.fpt.stl_show.stl.STLModel;
import com.fpt.stl_show.stl.TouchHelper;
import com.fpt.stl_show.camera.Camera2Helper;
import com.fpt.stl_show.util.MatrixState;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * <pre>
 *   @author : tocci.feng
 *   e-mail  : fengfei0205@gmail.com
 *   time    : 2021/03/12 10:03
 *   desc    :
 * </pre>
 */
public class DisplayRender implements GLSurfaceView.Renderer {

    private Context mContext;

    private TouchHelper mTouchHelper;

    private STLModel stlModel;

    private STLDrawer stlDrawer;

    //-----------------------------camera----------------------------

    private GLSurfaceView glSurfaceView;

    private Camera2Helper camera2Helper;

    private int[] texturesId;

    private SurfaceTexture surfaceTexture;

    private CameraDrawer cameraDrawer;

    private ScreenDrawer screenDrawer;

    private int mPreviewWidth;

    private int mPreviewHeight;

    private int screenSurfaceWid;

    private int screenSurfaceHeight;

    private float[] matrix = new float[16];

    public DisplayRender(GLSurfaceView glSurfaceView) {
        this.glSurfaceView = glSurfaceView;
        this.mContext = glSurfaceView.getContext();

        camera2Helper = new Camera2Helper((Activity)mContext);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        // 设置平模背景色RGBA
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        // 打开深度检测
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        // 禁用背面剪裁
        GLES20.glDisable(GLES20.GL_CULL_FACE);

        // 初始化变换矩阵
        MatrixState.setInitStack();
        // 初始化光源位置
        MatrixState.setLightLocation(60.0f, 15.0f ,30.0f);

        //----------------------------camera-------------------------------

        // 创建一个纹理
        texturesId = new int[1];
        GLES20.glGenTextures(texturesId.length, texturesId, 0);

        // 将纹理和离屏buffer绑定
        surfaceTexture = new SurfaceTexture(texturesId[0]);

        // 设置FrameAvailable监听
        surfaceTexture.setOnFrameAvailableListener(surfaceTexture -> {
            // 手动调用渲染
            glSurfaceView.requestRender();
        });

        // 使用fbo将samplerExternalOES 输入到sampler2D中
        cameraDrawer = new CameraDrawer(mContext);

        // 负责将图像绘制到屏幕上
        screenDrawer = new ScreenDrawer(mContext);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        // 设置视窗大小及位置
        GLES20.glViewport(0, 0, width, height);
        // 计算GLSurfaceView的宽高比
        float ratio = (float) width/height;
        // 调用此方法计算产生透视投影矩阵
        MatrixState.setProjectFrustum(-ratio, ratio, -1.0f, 1.0f, 2.0f, 100.0f);
        // 调用此方法产生摄像机9参数位置矩阵
        MatrixState.setCamera(0,0,0, 0f,0f,-1.0f, 0f,1.0f,0.0f);

        //-----------------------camera-------------------------------------

        // 预览尺寸监听
        camera2Helper.setPreviewSizeListener((width1, height1) -> {
            mPreviewWidth = width1;
            mPreviewHeight = height1;
        });
        // 打开相机
        camera2Helper.openCamera(width, height, surfaceTexture);

        float scaleX = (float) mPreviewHeight / (float) width;
        float scaleY = (float) mPreviewWidth / (float) height;
        float max = Math.max(scaleX, scaleY);
        screenSurfaceWid = (int) (mPreviewHeight / max);
        screenSurfaceHeight = (int) (mPreviewWidth / max);

        cameraDrawer.surfaceChanged(screenSurfaceWid, screenSurfaceHeight);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        // 清除深度缓冲与颜色缓冲
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        // 更新获取一张图
        surfaceTexture.updateTexImage();
        // 获取矩阵
        surfaceTexture.getTransformMatrix(matrix);
        // 映射到cameraDrawer中
        cameraDrawer.setMatrix(matrix);

        // 画camera内容
        int textureId = cameraDrawer.drawTexture(texturesId[0]);
        // 画screen内容
        screenDrawer.drawTexture(textureId,
                  0, -2.0f, -50.0f,
                0,   0,    0,
                  26.0f,   26.0f,      1.0f);

        // 绘制stl模型
        if (stlDrawer != null) {
            stlDrawer.drawSelf();
        }

    }

    public OnTouchAction registerTouchEvent() {
        return mTouchHelper;
    }

    public void unregisterTouchEvent() {
        mTouchHelper = null;
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

    public void setNewSTLObject(STLModel model) {
        this.stlModel = model;
        stlModel.adjustScale();

        mTouchHelper = new TouchHelper(stlModel);

        if (!stlModel.isDataEmpty()) {
            stlDrawer = new STLDrawer(mContext, stlModel);
        }
    }

}
