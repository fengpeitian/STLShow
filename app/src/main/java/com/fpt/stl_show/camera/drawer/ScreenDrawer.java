package com.fpt.stl_show.camera.drawer;

import android.content.Context;
import android.opengl.GLES20;

import com.fpt.stl_show.R;
import com.fpt.stl_show.util.MatrixState;
import com.fpt.stl_show.util.ShaderHelper;
import com.fpt.stl_show.util.TextResourceReader;
import com.fpt.stl_show.util.VertexArray;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;

/**
 * <pre>
 *   @author  : tocci.feng
 *   e-mail  : fengfei0205@gmail.com
 *   time    : 2021/03/11 15:14
 *   desc    :
 * </pre>
 */
public class ScreenDrawer {

    private int mProgram;

    private VertexArray vertexData;
    private VertexArray fragmentData;

    private static final String U_MATRIX = "u_Matrix";
    private static final String U_TEXTURE_UNIT = "u_TextureUnit";

    private static final String A_POSITION = "a_Position";
    private static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";

    private int uMatrixLocation;
    private int uTextureUnitLocation;

    private int aPositionLocation;
    private int aTextureCoordinatesLocation;

    public ScreenDrawer(Context context) {
        //初始化顶点数据
        initVertexData();
        //初始化着色器的方法
        initShader(context, R.raw.screen_vert, R.raw.screen_frag);
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

    /**
     * 画纹理
     * @param textureId     纹理id
     */
    public void drawTexture(int textureId) {
        drawTexture(textureId,
                0, 0, 0,
                0, 0, 0,
                1.0f, 1.0f, 1.0f);
    }

    /**
     * 画纹理
     * @param textureId     纹理id
     */
    public void drawTexture(int textureId,
                            float xTranslate, float yTranslate, float zTranslate,
                            float xRotateAngle, float yRotateAngle, float zRotateAngle,
                            float xScale, float yScale, float zScale) {
        // 指定使用shader程序
        GLES20.glUseProgram(mProgram);

        // matrix进栈
        MatrixState.pushMatrix();
        // 平移
        MatrixState.translate(xTranslate, yTranslate, zTranslate);
        // 旋转
        MatrixState.rotate(xRotateAngle, 0, 0, 1.0f);
        MatrixState.rotate(yRotateAngle, 0, 1.0f, 0);
        MatrixState.rotate(zRotateAngle, 1.0f, 0, 0);
        // 缩放
        MatrixState.scale(xScale, yScale, zScale);

        // matrix变换
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, MatrixState.getFinalMatrix(), 0);

        // 设置指针(x,y,z)
        vertexData.setVertexAttribPointer(0,aPositionLocation);
        // 设置指针(s,t)
        fragmentData.setVertexAttribPointer(0,aTextureCoordinatesLocation);

        // 把活动的纹理单元设置为纹理单元0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // 把纹理绑定到这个纹理单元
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        // 把选定的纹理单元传递给片段着色器中的u_TextureUnit
        GLES20.glUniform1i(uTextureUnitLocation, 0);

        // 填充内容
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        // matrix出栈
        MatrixState.popMatrix();
    }

    /**
     * 释放内存
     */
    public void release() {
        GLES20.glDeleteProgram(mProgram);
    }

}
