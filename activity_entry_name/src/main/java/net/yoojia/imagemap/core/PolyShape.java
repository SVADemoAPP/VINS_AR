package net.yoojia.imagemap.core;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import net.yoojia.imagemap.support.ScaleUtility;
import net.yoojia.imagemap.util.MatrixConverHelper;

import java.util.ArrayList;

/**
 * author : 桥下一粒砂 (chenyoca@gmail.com) date : 13-5-21 TODO
 */
public class PolyShape extends Shape
{

    private ArrayList<Float> xPoints = new ArrayList<Float>(0);
    private ArrayList<Float> yPoints = new ArrayList<Float>(0);
    private int pointCount;
    // bounding box
    private float top = -1;
    private float bottom = -1;
    private float left = -1;
    private float right = -1;
    

	@Override
	public float getCenterX() {
		return (left + right) / 2;
	}

	@Override
	public float getCenterY() {
		return (top + bottom) / 2;
	}

    public PolyShape(Object tag, int coverColor)
    {
        super(tag, coverColor);
    }

    @Override
    public void setValues(float... coords)
    {
        int i = 0;
        float x;
        float y;
        while ((i + 1) < coords.length)
        {
            x = coords[i];
            y = coords[i + 1];
            xPoints.add(x);
            yPoints.add(y);
            top = (top == -1) ? y : Math.min(top, y);
            bottom = (bottom == -1) ? y : Math.max(bottom, y);
            left = (left == -1) ? x : Math.min(left, x);
            right = (right == -1) ? x : Math.max(right, x);
            i += 2;
        }
        pointCount = xPoints.size();
        xPoints.add(xPoints.get(0));
        yPoints.add(yPoints.get(0));
        computeCentroid();
    }

    public void computeCentroid()
    {
        double cx = 0.0;
        double cy = 0.0;
        for (int i = 0; i < pointCount; i++)
        {
            cx = cx
                    + (xPoints.get(i) + xPoints.get(i + 1))
                    * (yPoints.get(i) * xPoints.get(i + 1) - xPoints.get(i)
                            * yPoints.get(i + 1));
            cy = cy
                    + (yPoints.get(i) + yPoints.get(i + 1))
                    * (yPoints.get(i) * xPoints.get(i + 1) - xPoints.get(i)
                            * yPoints.get(i + 1));
        }
        cx /= (6 * area());
        cy /= (6 * area());
    }

    public double area()
    {
        double sum = 0.0;
        for (int i = 0; i < pointCount; i++)
        {
            sum = sum + (xPoints.get(i) * yPoints.get(i + 1))
                    - (yPoints.get(i) * xPoints.get(i + 1));
        }
        sum = 0.5 * sum;
        return Math.abs(sum);
    }

    @Override
    public void draw(Canvas canvas)
    {
    	PointF pa = MatrixConverHelper.mapMatrixPoint(mOverMatrix, left, top);
		PointF pb = MatrixConverHelper.mapMatrixPoint(mOverMatrix, right, bottom);
        drawPaint.setAlpha(alaph);
        Path path = new Path();
        float startX = xPoints.get(0);
        float startY = yPoints.get(0);
        path.moveTo(startX, startY);
        float pointX;
        float pointY;
        for (int i = 0; i < pointCount; i++)
        {
            pointX = xPoints.get(i);
            pointY = yPoints.get(i);
            path.lineTo(pointX, pointY);
        }
        path.close();
        canvas.drawPath(path, drawPaint);
    }

    @Override
    public void onScale(float scale)
    {
        float newX;
        float newY;
        for (int i = 0; i < pointCount; i++)
        {
            newX = xPoints.get(i) * scale;
            newY = yPoints.get(i) * scale;
            xPoints.set(i, newX);
            yPoints.set(i, newY);
        }
    }

    @Override
    public void scaleBy(float scale, float centerX, float centerY)
    {
        PointF newPoint;
        for (int i = 0; i < pointCount; i++)
        {
            newPoint = ScaleUtility.scaleByPoint(xPoints.get(i),
                    yPoints.get(i), centerX, centerY, scale);
            xPoints.set(i, newPoint.x);
            yPoints.set(i, newPoint.y);
        }
    }

    @Override
    public void translate(float deltaX, float deltaY)
    {
        float x;
        float y;
        for (int i = 0; i < pointCount; i++)
        {
            x = xPoints.get(i) + deltaX;
            y = yPoints.get(i) + deltaY;
            xPoints.set(i, x);
            yPoints.set(i, y);
        }
    }

    @Override
    public boolean inArea(float x, float y)
    {
        int i = 0;
        int j = 0;
        boolean c = false;
        for (i = 0, j = pointCount - 1; i < pointCount; j = i++)
        {
            if (((yPoints.get(i) > y) != (yPoints.get(j) > y))
                    && (x < (xPoints.get(j) - xPoints.get(i))
                            * (y - yPoints.get(i))
                            / (yPoints.get(j) - yPoints.get(i))
                            + xPoints.get(i)))
                c = !c;
        }
        return c;
    }

    @Override
    public PointF getCenterPoint()
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < pointCount; i++)
        {
            builder.append(xPoints.get(i)).append(",").append(yPoints.get(i));
            builder.append(" ");
        }
        String[] point = getCenterCoord(builder.toString()).split(",");
        return new PointF(Float.parseFloat(point[0]),
                Float.parseFloat(point[1]));
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
}
