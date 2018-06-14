package com.sothree.slidinguppanel.demo;

import android.app.Application;
import android.util.Log;

import com.facebook.stetho.Stetho;
import com.jzb.common.ILogDelegate;
import com.jzb.common.LogTool;

public class MainApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        LogTool.getInstance().setDelegate(new ILogDelegate() {
            @Override
            public void executeLog(String tag, String message) {
                Log.e(tag, message);
            }

            @Override
            public String getProcessName() {
                return null;
            }
        });
        Stetho.initializeWithDefaults(this);
    }
}
