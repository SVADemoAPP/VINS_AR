package net.yoojia.imagemap;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

/***
 *  日志工具类
 *  Create by XHF 2019/3/20
 */

public class LogUtils {
    //等级开关 等级以上才会输出显示
    public static int VERBOSE_LEVEL = 0;
    public static int DEBUG_LEVEL = 1;
    public static int INFO_LEVEL = 2;
    public static int WARN_LEVEL = 3;
    public static int ERROR_LEVEL = 4;
    public static int CLOSE_LEVEL = 100;

    private static boolean writeToDiskFlag = false;  //是否输出日志文件到sd卡
    private static int mLevel = VERBOSE_LEVEL; //输出日志等级
    private static String mLogPath = Environment.getExternalStorageDirectory().getPath() + File.separator;
    private static LogUtils mLogUtils;

    public static LogUtils getInstance() {
        if (mLogUtils == null) {
            mLogUtils = new LogUtils();
        }
        return mLogUtils;
    }

    /**
     * @param path 存储文件夹
     */
    public LogUtils setDiskPath(String path) {
        mLogPath = path;
        return mLogUtils;
    }

    /**
     * @param level 当前日志等级
     */
    public LogUtils setLevel(int level) {
        mLevel = level;
        return mLogUtils;
    }

    /**
     * 是否写入sdk卡
     *
     * @param flag
     */
    public LogUtils setWriteFlag(boolean flag) {
        writeToDiskFlag = flag;
        return mLogUtils;
    }


    public static void v(String tag, String message) {
        if (mLevel <= VERBOSE_LEVEL) {
            Log.v(tag, message);
            if (writeToDiskFlag) {
                writeToDisk("Verbose:", tag, message);
            }
        }
    }

    public static void i(String tag, String message) {
        if (mLevel <= INFO_LEVEL) {
            Log.i(tag, message);
            if (writeToDiskFlag) {
                writeToDisk("Info:", tag, message);
            }
        }
    }

    public static void d(String tag, String message) {
        if (mLevel <= DEBUG_LEVEL) {
            Log.d(tag, message);
            if (writeToDiskFlag) {
                writeToDisk("Debug:", tag, message);
            }
        }
    }


    public static void w(String tag, String message) {
        if (mLevel <= WARN_LEVEL) {
            Log.w(tag, message);
            if (writeToDiskFlag) {
                writeToDisk("Warning:", tag, message);
            }
        }
    }

    public static void e(String tag, String message) {
        if (mLevel <= ERROR_LEVEL) {
            Log.e(tag, message);
            if (writeToDiskFlag) {
                writeToDisk("Error:", tag, message);
            }
        }
    }

    /**
     * 写入文件
     *
     * @param tag
     * @param message
     */
    private static void writeToDisk(String name, String tag, String message) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String format = simpleDateFormat.format(System.currentTimeMillis());
        File pathFile = new File(mLogPath);
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }
        File targetFile = new File(pathFile, "Log_" + format+".txt");
        FileOutputStream fileOutputStream = null;
        try {
            if (!targetFile.exists()) {
                targetFile.createNewFile();
            }
            fileOutputStream = new FileOutputStream(targetFile,true);  //追加写入
            String newLine = System.getProperty("line.separator"); //跨系统换行
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(name).append(tag).append(message).append(newLine);
            fileOutputStream.write(stringBuffer.toString().getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
