package com.fpt.stl_show;

import android.content.Context;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.fpt.stl_show.stl.ISTLReader;
import com.fpt.stl_show.stl.OnSTLReadListener;
import com.fpt.stl_show.stl.STLReader;

import java.io.IOException;

/**
 * <pre>
 *   @author  : tocci.feng
 *   e-mail  : fengfei0205@gmail.com
 *   time    : 2021/03/10 17:08
 *   desc    : stl读取任务
 * </pre>
 */
public class STLReaderTask extends ThreadUtils.Task<Boolean> {

    private ISTLReader reader;

    public STLReaderTask(Context context, String fileName, OnSTLReadListener listener) {
        reader = new STLReader();
        init(context, fileName, listener);
    }

    private void init(Context context, String fileName, OnSTLReadListener listener) {
        try {
            reader.read(ConvertUtils.inputStream2Bytes(context.getAssets().open(fileName)), listener);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Boolean doInBackground() throws Throwable {
        return reader.parseStl();
    }

    @Override
    public void onSuccess(Boolean result) {}

    @Override
    public void onCancel() {}

    @Override
    public void onFail(Throwable t) {}

}
