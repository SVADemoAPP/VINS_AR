package com.xhf.hw.utils;

import android.graphics.Bitmap;

public class Constant {
    public static final String PROJECT_NAME = "VINS_AR";
    public static final String MAP_NAME = "ar.png";
    public static String MAP;
    public static final String SELECT_POINT = "select_point";
    public static final String SELECT_ANGLE = "select_angle";
    public static Bitmap mSelectBitmap; //全局选择图片
    public static String FilePath;      //文件存储路径
    public static float Scale = 20f; //全局地图比例尺
    public static final int MIN_SCALE = 5; //最小缩放比例
    public static final int MAX_SCALE = 30; //最大缩放比例
}
