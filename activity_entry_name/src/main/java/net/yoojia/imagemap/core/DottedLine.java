package net.yoojia.imagemap.core;

import net.yoojia.imagemap.support.ScaleUtility;
import net.yoojia.imagemap.util.MatrixConverHelper;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.DiscretePathEffect;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.graphics.Shader;

public class DottedLine extends Shape
{
    private float left;
    private float top;
    private float bottom;
    private float right;

    public DottedLine(Object tag, int coverColor)
    {
        super(tag, coverColor);
    }

    /**
     * set Left,Top,Right,Bottim
     * 
     * @param coords
     *            left,top,right,buttom
     */
    @Override
    public void setValues(float... coords)
    {
        if (coords == null || coords.length != 4)
        {
            throw new IllegalArgumentException(
                    "Please set values with 4 paramters: left,top,right,buttom");
        }
        left = coords[0];
        top = coords[1];
        right = coords[2];
        bottom = coords[3];
    }

    @Override
    public void onScale(float scale)
    {
        left *= scale;
        top *= scale;
        right *= scale;
        bottom *= scale;
    }

    @Override
    public void draw(Canvas canvas)
    {
        drawPaint.setStrokeWidth(8f);
        drawPaint.setAntiAlias(true);
        PointF pa = MatrixConverHelper.mapMatrixPoint(mOverMatrix, left, top);
		PointF pb = MatrixConverHelper.mapMatrixPoint(mOverMatrix, right, bottom);
        drawPaint.setPathEffect(new DashPathEffect(new float[]{10f,3f}, 8));
        canvas.drawLine(pa.x, pa.y, pb.x, pb.y, drawPaint);
    }

    @Override
    public void scaleBy(float scale, float centerX, float centerY)
    {

        PointF leftTop = ScaleUtility.scaleByPoint(left, top, centerX, centerY,
                scale);
        left = leftTop.x;
        top = leftTop.y;

        PointF righBottom = ScaleUtility.scaleByPoint(right, bottom, centerX,
                centerY, scale);
        right = righBottom.x;
        bottom = righBottom.y;
    }

    @Override
    public void translate(float deltaX, float deltaY)
    {
        left += deltaX;
        right += deltaX;
        top += deltaY;
        bottom += deltaY;
    }

    @Override
    public boolean inArea(float x, float y)
    {
        boolean ret = false;
        if ((x > left) && (x < right))
        {
            if ((y > top) && (y < bottom))
            {
                ret = true;
            }
        }
        return false;
    }

    @Override
    public PointF getCenterPoint()
    {
        float centerX = (left + right) / 2.0f;
        float centerY = (top + bottom) / 2.0f;
        return new PointF(centerX, centerY);
    }

    @Override
    public String getUrl()
    {
        return null;
    }

    @Override
    public String getPictureUrl()
    {
        return null;
    }

    @Override
    public String getContent()
    {
        return null;
    }

    @Override
    public String getTitle()
    {
        return null;
    }

    @Override
    public boolean bubbleTag()
    {
        return false;
    }

	@Override
	public float getCenterX() {
		// TODO Auto-generated method stub
		return (left+right)/2;
	}

	@Override
	public float getCenterY() {
		// TODO Auto-generated method stub
		return (top+bottom)/2;
	}
}
