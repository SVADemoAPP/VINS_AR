package com.xhf.hw.utils;

import android.graphics.PointF;
import android.util.Log;


public class DistanceUtil {
    public static float[] realToMap(float mapScale, float rx, float ry, int height) {
        return new float[]{rx * mapScale, height - ry * mapScale};
    }


    public static float[] mapToReal(float mapScale, float mx, float my, int height) {
        return new float[]{mx / mapScale, (height - my) / mapScale};
    }

    public static float[] getPoint(final float x, final float y, float angle) {
        final float[] point = new float[2];
        point[0] = (float) (x * Math.cos(2 * Math.PI / 360 * angle) + y * Math.sin(2 * Math.PI / 360 * angle));
        point[1] = (float) (y * Math.cos(2 * Math.PI / 360 * angle) - x * Math.sin(2 * Math.PI / 360 * angle));
        return point;

    }
}
