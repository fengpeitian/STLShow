package com.fpt.stl_show.stl;

/**
 * <pre>
 *   @author  : tocci.feng
 *   e-mail  : fengfei0205@gmail.com
 *   time    : 2021/03/10 17:05
 *   desc    : stl数据读取器
 * </pre>
 */
public interface ISTLReader {

    /**
     * 判断stl文件格式是否是Ascii格式
     */
    static boolean isAscii(byte[] data) {
        for (byte b : data) {
            if (b == 0x0a || b == 0x0d || b == 0x09) {
                continue;
            }
            if (b < 0x20 || (0xff & b) >= 0x80) {
                return false;
            }
        }
        return true;
    }

    /**
     * 读取stl数据
     * @param data      stl数据
     * @param listener  读取监听
     */
    void read(byte[] data, OnSTLReadListener listener);

    /**
     * 解析stl
     * @return result
     */
    boolean parseStl();

}
