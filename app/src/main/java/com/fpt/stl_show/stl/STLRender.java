package com.fpt.stl_show.stl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

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
public class STLRender implements GLSurfaceView.Renderer {

    private Context mContext;

    private TouchHelper mTouchHelper;

    private STLModel stlModel;

    private STLDrawer stlDrawer;

    public STLRender(Context context) {
        this.mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        // 设置平模背景色RGBA
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        // 打开深度检测
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        // 打开背面剪裁
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        // 初始化变换矩阵
        MatrixState.setInitStack();
        // 初始化光源位置
        MatrixState.setLightLocation(60.0f, 15.0f ,30.0f);
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
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        // 清除深度缓冲与颜色缓冲
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

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
