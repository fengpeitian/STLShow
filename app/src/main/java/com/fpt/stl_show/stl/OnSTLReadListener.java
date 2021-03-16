package com.fpt.stl_show.stl;

/**
 * <pre>
 *   @author  : tocci.feng
 *   e-mail  : fengfei0205@gmail.com
 *   time    : 2021/03/11 08:52
 *   desc    : stl文件读取监听(全部为子线程回调)
 * </pre>
 */
public interface OnSTLReadListener {

    /**
     * 开始
     */
    void onStart();

    /**
     * 进度
     * @param progress 进度
     */
    void onProgress(int progress);

    /**
     * 完成
     * @param model  stl模型
     */
    void onCompleted(STLModel model);

    /**
     * 失败
     * @param e  异常
     */
    void onFailure(Exception e);

}
