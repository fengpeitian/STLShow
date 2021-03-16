package com.fpt.stl_show.stl;

/**
 * <pre>
 *   @author  : tocci.feng
 *   e-mail  : fengfei0205@gmail.com
 *   time    : 2021/03/10 17:24
 *   desc    : stl数据读取器
 * </pre>
 */
public class STLReader implements ISTLReader {

    private float maxX = Float.MIN_VALUE;

    private float minX = Float.MAX_VALUE;

    private float maxY = Float.MIN_VALUE;

    private float minY = Float.MAX_VALUE;

    private float maxZ = Float.MIN_VALUE;

    private float minZ = Float.MAX_VALUE;

    private int vertex_size = 0;

    private float[] normal_array;

    private float[] vertex_array;

    private OnSTLReadListener listener;

    private byte[] data;

    public STLReader() {}

    @Override
    public void read(byte[] data, OnSTLReadListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @Override
    public boolean parseStl() {
        if (data == null || data.length == 0){
            onFailed(new NullPointerException("stl data is empty."));
            return false;
        }
        return parseStl(data);
    }

    /**
     * 解析stl
     * @param stl  stl数据
     */
    private boolean parseStl(byte[] stl) {
        boolean result = false;
        try {
            onStart();
            if (ISTLReader.isAscii(stl)){
                parserAsciiStl(stl);
            }else {
                parserBinaryStl(stl);
            }
            result = true;
        }catch (Exception e){
            onFailed(e);
        }
        return result;
    }

    /**
     * 解析ASCII格式的STL文件
     * @param bytes  stl数据
     */
    private void parserAsciiStl(byte[] bytes) {
        // ascii转文本
        String stlText = new String(bytes);
        String[] stlLines = stlText.split("\n");

        // 顶点数
        vertex_size = (stlLines.length - 2) / 7;

        // 创建顶点数组
        vertex_array = new float[vertex_size * 9];
        normal_array = new float[vertex_size * 9];

        // 顶点数组赋值
        int vertex_num = 0;
        int normal_num = 0;
        for (int i = 0; i < stlLines.length; i++) {
            // normal
            String string = stlLines[i].trim();
            if (string.startsWith("facet normal ")) {
                string = string.replaceFirst("facet normal ", "");
                String[] normalValue = string.split(" ");

                for (int n = 0; n < 3; n++) {
                    normal_array[normal_num++] = Float.parseFloat(normalValue[0]);
                    normal_array[normal_num++] = Float.parseFloat(normalValue[1]);
                    normal_array[normal_num++] = Float.parseFloat(normalValue[2]);
                }
            }

            // vertex
            if (string.startsWith("vertex ")) {
                string = string.replaceFirst("vertex ", "");
                String[] vertexValue = string.split(" ");

                float x = Float.parseFloat(vertexValue[0]);
                float y = Float.parseFloat(vertexValue[1]);
                float z = Float.parseFloat(vertexValue[2]);
                adjustMaxMin(x, y, z);
                vertex_array[vertex_num++] = x;
                vertex_array[vertex_num++] = y;
                vertex_array[vertex_num++] = z;
            }

            // 进度回调
            if (i % (stlLines.length / 50) == 0) {
                onLoading(i, stlLines.length);
            }
        }

        // 中心点坐标
        float center_x = (maxX + minX) / 2;
        float center_y = (maxY + minY) / 2;
        float center_z = (maxZ + minZ) / 2;

        // 矫正中心点坐标
        for (int i = 0; i < vertex_size * 3; i++) {
            adjustCoordinate(i * 3, center_x);
            adjustCoordinate(i * 3 + 1, center_y);
            adjustCoordinate(i * 3 + 2, center_z);
        }

        //将读取的数据设置到STLModel对象中
        STLModel model = new STLModel();
        model.setMax(maxX, maxY, maxZ);
        model.setMin(minX, minY, minZ);
        model.setVertexArray(vertex_array);
        model.setNormalArray(normal_array);

        // 完成状态回调
        onFinished(model);
    }

    /**
     * 解析二进制格式的STL文件
     * @param bytes  stl数据
     */
    private void parserBinaryStl(byte[] bytes) {
        // 顶点数
        vertex_size = getIntWithLittleEndian(bytes, 80);

        // 创建顶点数组
        vertex_array = new float[vertex_size * 9];
        normal_array = new float[vertex_size * 9];

        // 顶点数组赋值
        for (int i = 0; i < vertex_size; i++) {
            // normal
            for (int n = 0; n < 3; n++) {
                normal_array[i * 9 + n * 3] =
                        Float.intBitsToFloat(getIntWithLittleEndian(bytes, 84 + i * 50));
                normal_array[i * 9 + n * 3 + 1] =
                        Float.intBitsToFloat(getIntWithLittleEndian(bytes, 84 + i * 50 + 4));
                normal_array[i * 9 + n * 3 + 2] =
                        Float.intBitsToFloat(getIntWithLittleEndian(bytes, 84 + i * 50 + 8));
            }

            // vertex
            float x = Float.intBitsToFloat(getIntWithLittleEndian(bytes, 84 + i * 50 + 12));
            float y = Float.intBitsToFloat(getIntWithLittleEndian(bytes, 84 + i * 50 + 16));
            float z = Float.intBitsToFloat(getIntWithLittleEndian(bytes, 84 + i * 50 + 20));
            adjustMaxMin(x, y, z);
            vertex_array[i * 9] = x;
            vertex_array[i * 9 + 1] = y;
            vertex_array[i * 9 + 2] = z;

            x = Float.intBitsToFloat(getIntWithLittleEndian(bytes, 84 + i * 50 + 24));
            y = Float.intBitsToFloat(getIntWithLittleEndian(bytes, 84 + i * 50 + 28));
            z = Float.intBitsToFloat(getIntWithLittleEndian(bytes, 84 + i * 50 + 32));
            adjustMaxMin(x, y, z);
            vertex_array[i * 9 + 3] = x;
            vertex_array[i * 9 + 4] = y;
            vertex_array[i * 9 + 5] = z;

            x = Float.intBitsToFloat(getIntWithLittleEndian(bytes, 84 + i * 50 + 36));
            y = Float.intBitsToFloat(getIntWithLittleEndian(bytes, 84 + i * 50 + 40));
            z = Float.intBitsToFloat(getIntWithLittleEndian(bytes, 84 + i * 50 + 44));
            adjustMaxMin(x, y, z);
            vertex_array[i * 9 + 6] = x;
            vertex_array[i * 9 + 7] = y;
            vertex_array[i * 9 + 8] = z;

            // 进度回调
            if (i % (vertex_size / 50) == 0) {
                onLoading(i, vertex_size);
            }
        }

        // 中心点坐标
        float center_x = (maxX + minX) / 2;
        float center_y = (maxY + minY) / 2;
        float center_z = (maxZ + minZ) / 2;

        // 矫正中心点坐标
        for (int i = 0; i < vertex_size * 3; i++) {
            adjustCoordinate(i * 3, center_x);
            adjustCoordinate(i * 3 + 1, center_y);
            adjustCoordinate(i * 3 + 2, center_z);
        }

        //将读取的数据设置到STLModel对象中
        STLModel model = new STLModel();
        model.setMax(maxX, maxY, maxZ);
        model.setMin(minX, minY, minZ);
        model.setVertexArray(vertex_array);
        model.setNormalArray(normal_array);

        // 完成状态回调
        onFinished(model);
    }

    /**
     * 开始解析的回调
     */
    private void onStart() {
        if (listener != null){
            listener.onStart();
        }
    }

    /**
     * 回调load进度
     * @param current   当前
     * @param max       总量
     */
    private void onLoading(int current, int max) {
        if (listener != null){
            float result = (float)current/max;
            listener.onProgress((int)(result * 100));
        }
    }

    /**
     * 回调完成状态
     * @param model     stl模型
     */
    private void onFinished(STLModel model) {
        if (listener != null){
            listener.onCompleted(model);
        }
    }

    /**
     * 回调读取stl失败
     * @param e  错误msg
     */
    private void onFailed(Exception e) {
        if (listener != null){
            listener.onFailure(e);
        }
    }

    /**
     * 调整大小值（检查是否超出float范围）
     * @param x         坐标x
     * @param y         坐标y
     * @param z         坐标z
     */
    private void adjustMaxMin(float x, float y, float z) {
        if (x > maxX) {
            maxX = x;
        }
        if (y > maxY) {
            maxY = y;
        }
        if (z > maxZ) {
            maxZ = z;
        }
        if (x < minX) {
            minX = x;
        }
        if (y < minY) {
            minY = y;
        }
        if (z < minZ) {
            minZ = z;
        }
    }

    /**
     * 矫正坐标坐标中心移动
     * @param index   调整的顶点index
     * @param adjust  需要调整的值
     */
    private void adjustCoordinate(int index, float adjust) {
        this.vertex_array[index] -= adjust;
    }

    /**
     * 小尾字节序求size
     * @param bytes     stl数据
     * @param offset    偏移量
     * @return
     */
    private static int getIntWithLittleEndian(byte[] bytes, int offset) {
        return (0xff & bytes[offset])
                | ((0xff & bytes[offset + 1]) << 8)
                | ((0xff & bytes[offset + 2]) << 16)
                | ((0xff & bytes[offset + 3]) << 24);
    }

}
