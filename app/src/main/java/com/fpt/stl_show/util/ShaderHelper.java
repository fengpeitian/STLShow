package com.fpt.stl_show.util;

import android.opengl.GLES20;
import android.util.Log;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;

/**
 * <pre>
 *   @author  : tocci.feng
 *   e-mail  : fengfei0205@gmail.com
 *   time    : 2020/11/17 09:58
 *   desc    : shader程序
 * </pre>
 */
public class ShaderHelper {

    private static final String TAG = "ShaderHelper";

    private static int compileVertexShader(String shaderCode){
        return compileShader(GL_VERTEX_SHADER, shaderCode);
    }

    private static int compileFragmentShader(String shaderCode){
        return compileShader(GL_FRAGMENT_SHADER, shaderCode);
    }

    private static int compileShader(int shaderType, String shaderCode) {
        //创建shader
        final int shaderObjectId = glCreateShader(shaderType);
        //检验shader
        if (shaderObjectId == 0){
            Log.e(TAG,"Could not create new shader.");

            return 0;
        }
        //向openGL里上传源代码
        glShaderSource(shaderObjectId,shaderCode);
        //编译shader
        glCompileShader(shaderObjectId);
        //取出编译状态
        final int[] compileStatus = new int[1];
        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0);
        //检验编译状态
        if (compileStatus[0] == 0){
            //编译失败时，及时删掉shader
            GLES20.glDeleteShader(shaderObjectId);
            Log.e(TAG, "Compilation of shader failed.");

            return 0;
        }
        //返回有效的shader
        return shaderObjectId;
    }

    /**
     * 获得一个关联好着色器的program
     * @param vertexShader
     * @param fragmentShader
     * @return
     */
    private static int linkProgram(int vertexShader, int fragmentShader) {
        final int programObjectId = glCreateProgram();

        if (programObjectId == 0){
            Log.e(TAG, "Could not create new program.");

            return 0;
        }

        //为项目附上着色器
        glAttachShader(programObjectId,vertexShader);
        glAttachShader(programObjectId,fragmentShader);
        //联合着色器
        glLinkProgram(programObjectId);
        //取出联合状态
        final int[] linkStatus = new int[1];
        glGetProgramiv(programObjectId, GL_LINK_STATUS, linkStatus, 0);

        if (linkStatus[0] == 0) {
            GLES20.glDeleteProgram(programObjectId);

            Log.e(TAG, "Linking of program failed.");
            return 0;
        }

        return programObjectId;
    }


    public static int buildProgram(String vertexShaderCode, String fragmentShaderCode){
        int vertexShader = compileVertexShader(vertexShaderCode);
        int fragmentShader = compileFragmentShader(fragmentShaderCode);

        return linkProgram(vertexShader,fragmentShader);
    }

}
