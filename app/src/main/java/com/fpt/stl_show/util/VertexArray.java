package com.fpt.stl_show.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * <pre>
 *   @author  : tocci.feng
 *   e-mail  : fengfei0205@gmail.com
 *   time    : 2020/11/17 09:57
 *   desc    : 顶点数组
 * </pre>
 */
public class VertexArray {

    private static final int BYTES_PER_FLOAT = 4;

    private final FloatBuffer floatBuffer;

    private final int componentCount;

    private final int count;

    public VertexArray(float[] vertexData, int componentCount) {
        this.componentCount = componentCount;
        this.count = vertexData.length / componentCount;

        floatBuffer = ByteBuffer
                //申请内存
                .allocateDirect(vertexData.length * BYTES_PER_FLOAT)
                //一个平台要使用同样的顺序
                .order(ByteOrder.nativeOrder())
                //申请类型
                .asFloatBuffer()
                //放入数据
                .put(vertexData);

    }

    public void setVertexAttribPointer(int dataOffset, int attributeLocation){
        //指针到固定位置
        floatBuffer.position(dataOffset);
        //赋值(需要加跨距)
        glVertexAttribPointer(attributeLocation,componentCount
                ,GL_FLOAT,false,componentCount*BYTES_PER_FLOAT,floatBuffer);
        //使能顶点数组
        glEnableVertexAttribArray(attributeLocation);
    }

    public int getPointCount() {
        return count;
    }

}
