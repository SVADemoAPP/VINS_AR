package com.xhf.hw.ui;

import android.app.Application;
import android.os.Environment;

import com.xhf.hw.utils.Constant;
import com.xhf.hw.utils.LogUtils;
import com.xhf.hw.utils.MyCrashHandler;

import java.io.File;

public class VINSApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Constant.FilePath = Environment.getExternalStorageDirectory().getPath() + File.separator + Constant.PROJECT_NAME;
        Constant.MAP = Constant.FilePath + File.separator + "map";
        LogUtils.getInstance()
                .setDiskPath(Constant.FilePath + File.separator + "Log")
                .setLevel(LogUtils.VERBOSE_LEVEL)
                .setWriteFlag(true);
        MyCrashHandler crashHandler = MyCrashHandler.instance();
        crashHandler.init(getApplicationContext());
    }
}
