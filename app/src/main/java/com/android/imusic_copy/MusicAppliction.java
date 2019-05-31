package com.android.imusic_copy;

import android.app.Application;
import android.content.Context;

import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by 77167 on 2019/5/31.
 */

public class MusicAppliction extends Application {
    private static Context sContext;
    @Override
    public void onCreate() {
        super.onCreate();
        sContext=getApplicationContext();

        initApplication();
    }


    public static Context getContext() {
        return sContext;
    }

    private void initApplication() {
        // 全局异常拦截
        CrashReport.initCrashReport(getApplicationContext(), "da36e5e1da", false);

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // 这里是在内存不足时会调用
//        ForegroundManager.getInstance().onDestroy(this); // 这个是应用在 music 模块上的后台监测机制
        sContext=null;
    }
}
