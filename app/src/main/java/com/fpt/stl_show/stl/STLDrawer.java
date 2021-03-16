package com.fpt.stl_show.stl;

import android.content.Context;
import android.opengl.GLES20;

import androidx.annotation.NonNull;

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
 *   time    : 2021/03/12 15:24
 *   desc    : stl绘制
 * </pre>
 */
public class STLDrawer {

    private static final String U_MVP_MATRIX = "uMVPMatrix";

    private static final String U_MATRIX = "uMMatrix";

    private static final String U_LIGHT_LOCATION = "uLightLocation";

    private static final String U_CAMERA = "uCamera";

    private static final String A_POSITION = "aPosition";

    private static final String A_NORMAL = "aNormal";

    private static final String A_COLOR = "aColor";

    /**
     * 程序id
     */
    private int mProgram;
    /**
     * 总变换矩阵引用
     */
    private int muMVPMatrixHandle;
    /**
     * 位置、旋转变换矩阵
     */
    private int muMMatrixHandle;
    /**
     * 顶点位置属性引用
     */
    private int maPositionHandle;
    /**
     * 顶点法向量属性引用
     */
    private int maNormalHandle;
    /**
     * 光源位置属性引用
     */
    private int maLightLocationHandle;
    /**
     * 摄像机位置属性引用
     */
    private int maCameraHandle;
    /**
     * 顶点颜色
     */
    private int muColorHandle;

    private VertexArray vertexData;

    private VertexArray normalData;

    private STLModel mModel;

    public STLDrawer(@NonNull Context context, @NonNull STLModel model) {
        this.mModel = model;
        // 初始化顶点数据
        vertexData = new VertexArray(mModel.getVertexArray(), 3);
        normalData = new VertexArray(mModel.getNormalArray(), 3);
        // 初始化着色器的方法
        initShader(context, R.raw.stl_vertex, R.raw.stl_fragment_color);
    }

    /**
     * 初始化基础着色器
     * @param context
     * @param vertexShaderResourceId
     * @param fragmentShaderResourceId
     */
    public void initShader(Context context, int vertexShaderResourceId, int fragmentShaderResourceId) {
        // 基于顶点着色器与片元着色器创建程序
        mProgram = ShaderHelper.buildProgram(
                TextResourceReader.readCodeFromResource(
                        context, vertexShaderResourceId),
                TextResourceReader.readCodeFromResource(
                        context, fragmentShaderResourceId));
        // 获取程序中顶点位置属性引用
        maPositionHandle = glGetAttribLocation(mProgram, A_POSITION);
        // 获取程序中顶点颜色属性引用
        maNormalHandle= glGetAttribLocation(mProgram, A_NORMAL);
        // 获取程序中总变换矩阵引用
        muMVPMatrixHandle = glGetUniformLocation(mProgram, U_MVP_MATRIX);
        // 获取位置、旋转变换矩阵引用
        muMMatrixHandle = glGetUniformLocation(mProgram, U_MATRIX);
        // 获取程序中光源位置引用
        maLightLocationHandle = glGetUniformLocation(mProgram, U_LIGHT_LOCATION);
        // 获取程序中摄像机位置引用
        maCameraHandle = glGetUniformLocation(mProgram, U_CAMERA);
        // 获取程序中物体颜色的引用
        muColorHandle = glGetUniformLocation(mProgram, A_COLOR);
    }

    public void drawSelf() {
        // matrix进栈
        MatrixState.pushMatrix();
        // 平移
        MatrixState.translate(mModel.xTranslate, mModel.yTranslate, mModel.zTranslate);
        // 旋转
        MatrixState.rotate(mModel.xRotateAngle, 0, 0, 1.0f);
        MatrixState.rotate(mModel.yRotateAngle, 0, 1.0f, 0);
        MatrixState.rotate(mModel.zRotateAngle, 1.0f, 0, 0);
        // 缩放
        MatrixState.scale(
                mModel.adjustScale * mModel.xScale,
                mModel.adjustScale * mModel.yScale,
                mModel.adjustScale * mModel.zScale);

        // 设置线宽
        GLES20.glLineWidth(1.0f);
        // 指定使用某套着色器程序
        GLES20.glUseProgram(mProgram);
        // 将最终变换矩阵传入着色器程序
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, MatrixState.getFinalMatrix(), 0);
        // 将位置、旋转变换矩阵传入着色器程序
        GLES20.glUniformMatrix4fv(muMMatrixHandle, 1, false, MatrixState.getMMatrix(), 0);
        // 将光源位置传入着色器程序
        GLES20.glUniform3fv(maLightLocationHandle, 1, MatrixState.lightPositionFB);
        // 将摄像机位置传入着色器程序
        GLES20.glUniform3fv(maCameraHandle, 1, MatrixState.cameraFB);

        // 将顶点位置数据传入渲染管线
        vertexData.setVertexAttribPointer(0, maPositionHandle);
        // 将顶点法向量数据传入渲染管线
        normalData.setVertexAttribPointer(0, maNormalHandle);

        // 传入顶点颜色数据
        GLES20.glUniform4fv(muColorHandle, 1, mModel.color, 0);
        // 绘制加载的物体
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexData.getPointCount());

        // matrix出栈
        MatrixState.popMatrix();
    }

    /**
     * 删除程序释放内存
     */
    public void release() {
        GLES20.glDeleteProgram(mProgram);
    }

}
