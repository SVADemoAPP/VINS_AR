package net.yoojia.imagemap.util;

import android.graphics.Bitmap;

public class BaZhanUtil {

    public float initAngle = 0;

    public float initScale = 1f;

    public BaZhanUtil(float initAngle, float initScale) {
        super();
        this.initAngle = initAngle;
        this.initScale = initScale;
    }

    public float getInitAngle() {
        return initAngle;
    }

    public void setInitAngle(float initAngle) {
        this.initAngle = initAngle;
    }

    public float getInitScale() {
        return initScale;
    }

    public void setInitScale(float initScale) {
        this.initScale = initScale;
    }

    @Override
    public String toString() {
        return "BaZhanUtil [initAngle=" + initAngle + ", initScale="
                + initScale + "]";
    }

    public static Bitmap getTransparentBitmap(Bitmap sourceImg, int number) {
        int[] argb = new int[sourceImg.getWidth() * sourceImg.getHeight()];

        sourceImg.getPixels(argb, 0, sourceImg.getWidth(), 0, 0, sourceImg

                .getWidth(), sourceImg.getHeight());// 获得图片的ARGB值

        number = number * 255 / 100;

        for (int i = 0; i < argb.length; i++) {

            argb[i] = (number << 24) | (argb[i] & 0x00FFFFFF);

        }

        sourceImg = Bitmap.createBitmap(argb, sourceImg.getWidth(), sourceImg

                .getHeight(), Bitmap.Config.ARGB_8888);
        return sourceImg;

    }
}
