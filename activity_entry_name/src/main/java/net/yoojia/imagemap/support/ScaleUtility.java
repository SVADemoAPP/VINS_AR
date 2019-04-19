package net.yoojia.imagemap.support;

import android.graphics.Matrix;
import android.graphics.PointF;

/**
 * author : chenyoca@gmail.com date : 13-5-19 A utility for scaleBy options.
 */
public class ScaleUtility
{

    /**
     * Get the new position object when scaleBy with a given point.
     * 
     * @param targetPointX
     *            the x position before scaleBy
     * @param targetPointY
     *            the y position before scaleBy
     * @param scaleCenterX
     *            the scaleBy point ,x position
     * @param scaleCenterY
     *            the scaleBy point ,y position
     * @param scale
     *            scaleBy
     * @return the new position after scaleBy
     */
    public static PointF scaleByPoint(float targetPointX, float targetPointY,
            float scaleCenterX, float scaleCenterY, float scale)
    {
        Matrix matrix = new Matrix();
        // move the matrix to target position
        // then, scaleBy with the given point and scaleBy.
        matrix.preTranslate(targetPointX, targetPointY);
        matrix.postScale(scale, scale, scaleCenterX, scaleCenterY);
        float[] values = new float[9];
        matrix.getValues(values);
        return new PointF(values[Matrix.MTRANS_X], values[Matrix.MTRANS_Y]);
    }
}
