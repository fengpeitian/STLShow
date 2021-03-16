package com.fpt.stl_show;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.fpt.stl_show.stl.OnSTLReadListener;
import com.fpt.stl_show.stl.STLModel;


public class DisplayActivity extends AppCompatActivity {

    private static final String stlFileName = "beizi.stl";

    private DisplayView display_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        display_view = new DisplayView(this);
        setContentView(display_view);

        STLReaderTask task = new STLReaderTask(this, stlFileName, new OnSTLReadListener() {

            @Override
            public void onStart() {
                LogUtils.d("onStart");
            }

            @Override
            public void onProgress(int progress) {
                LogUtils.d("onProgress", progress);
            }

            @Override
            public void onCompleted(STLModel model) {
                display_view.setNewSTLObject(model);

                LogUtils.d("onCompleted");
            }

            @Override
            public void onFailure(Exception e) {
                LogUtils.d("onFailure", e.getMessage());
            }
        });

        ThreadUtils.executeByIo(task);
    }

}