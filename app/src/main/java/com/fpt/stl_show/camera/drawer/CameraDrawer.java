package com.fpt.stl_show.camera.drawer;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.fpt.stl_show.R;
import com.fpt.stl_show.util.ShaderHelper;
import com.fpt.stl_show.util.TextResourceReader;
import com.fpt.stl_show.util.VertexArray;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;

/**
 * <pre>
 *   @author  : tocci.feng
 *   e-mail  : fengfei0205@gmail.com
 *   time    : 2021/03/11 15:01
 *   desc    :
 * </pre>
 */
public class CameraDrawer {

    private int mProgram;

    private static final String U_MATRIX = "u_Matrix";
    private static final String U_TEXTURE_UNIT = "u_TextureUnit";

    private static final String A_POSITION = "a_Position";
    private static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";

    private int uMatrixLocation;
    private int uTextureUnitLocation;

    private int aPositionLocation;
    private int aTextureCoordinatesLocation;

    private VertexArray vertexData;
    private VertexArray fragmentData;

    private int[] mFrameBufferId;

    private int[] mFBOTextureId;

    private float[] matrix;

    public CameraDrawer(Context context) {
        //初始化顶点数据
        initVertexData();
        //初始化着色器的方法
        initShader(context, R.raw.camera_vertex, R.raw.camera_frag);
    }

    /**
     * 一般纹理初始化顶点
     */
    private void initVertexData() {
        // 顶点坐标(x,y)
        float[] vertices = new float[] {
                -1.0f,   1.0f,
                1.0f,   1.0f,
                -1.0f,  -1.0f,
                1.0f,  -1.0f
        };

        // 纹理坐标(s,t)
        float[] texCoor = new float[]{
                0.0f, 0.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f
        };

        initVertexData(vertices, texCoor);
    }

    /**
     * 自定义纹理初始化顶点
     */
    private void initVertexData(float[] vertices, float[] texCoor) {
        vertexData = new VertexArray(vertices, 2);
        fragmentData = new VertexArray(texCoor, 2);
    }

    /**
     * 初始化shader
     */
    private void initShader(Context context, int vertexShaderResourceId, int fragmentShaderResourceId) {
        //基于顶点着色器与片元着色器创建程序
        mProgram = ShaderHelper.buildProgram(
                TextResourceReader.readCodeFromResource(
                        context, vertexShaderResourceId),
                TextResourceReader.readCodeFromResource(
                        context, fragmentShaderResourceId));

        //获取程序中总变换矩阵引用
        uMatrixLocation = glGetUniformLocation(mProgram, U_MATRIX);
        //获取程序中纹理属性引用
        uTextureUnitLocation = glGetUniformLocation(mProgram, U_TEXTURE_UNIT);
        //获取程序中顶点位置属性引用
        aPositionLocation = glGetAttribLocation(mProgram, A_POSITION);
        //获取程序中顶点纹理坐标属性引用
        aTextureCoordinatesLocation = glGetAttribLocation(mProgram, A_TEXTURE_COORDINATES);
    }

    public void surfaceChanged(int width, int height){
        loadFbo(width, height);
    }

    public int drawTexture(int textureId) {
        // 绑定FBO，在FBO上操作
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferId[0]);

        // 使用着色器
        GLES20.glUseProgram(mProgram);

        // Matrix变换
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);

        // 设置指针(x,y)
        vertexData.setVertexAttribPointer(0, aPositionLocation);
        // 设置指针(s,t)
        fragmentData.setVertexAttribPointer(0, aTextureCoordinatesLocation);

        // 把活动的纹理单元设置为纹理单元0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        // SurfaceTexture 对应 GL_TEXTURE_EXTERNAL_OES 类型
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        // 把选定的纹理单元传递给片段着色器中的u_TextureUnit
        GLES20.glUniform1i(uTextureUnitLocation, 0);
        // 填充内容
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        // 清空
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);

        return mFBOTextureId[0];
    }

    /**
     * 释放内存
     */
    public void release() {
        GLES20.glDeleteProgram(mProgram);
    }

    public void setMatrix(float[] matrix) {
        this.matrix = matrix;
    }

    private void loadFbo(int width, int height) {

        if (mFrameBufferId != null) {
            deleteFrameBuffers();
        }

        // 创建FrameBuffer
        mFrameBufferId = new int[1];
        GLES20.glGenFramebuffers(mFrameBufferId.length, mFrameBufferId, 0);
        // 创建FBO中的纹理
        mFBOTextureId = new int[1];
        GLES20.glGenTextures(mFBOTextureId.length, mFBOTextureId, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFBOTextureId[0]);
        // 设置参数
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        // 清空
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        // 绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFBOTextureId[0]);

        // 指定FBO纹理的输出图像的格式 RGBA
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,
                0,
                GLES20.GL_RGBA,
                width, height,
                0,
                GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE,
                null);

        // 绑定frameBuffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferId[0]);
        // 将fbo绑定到2d的纹理上
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
                GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D,
                mFBOTextureId[0],
                0);

        // 清空
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    private void deleteFrameBuffers() {
        // 删除fbo的纹理
        if (mFBOTextureId != null) {
            GLES20.glDeleteTextures(1, mFBOTextureId, 0);
            mFBOTextureId = null;
        }
        // 删除fbo
        if (mFrameBufferId != null) {
            GLES20.glDeleteFramebuffers(1, mFrameBufferId, 0);
            mFrameBufferId = null;
        }
    }

}
