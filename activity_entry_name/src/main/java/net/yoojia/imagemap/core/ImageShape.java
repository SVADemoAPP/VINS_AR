package net.yoojia.imagemap.core;

import net.yoojia.imagemap.support.ScaleUtility;
import net.yoojia.imagemap.util.MatrixConverHelper;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.view.View;
import android.view.View.MeasureSpec;

public class ImageShape extends Shape
{

    private PointF center;
    @SuppressWarnings("unused")
    private float radius = 5f;
    private View view;
    @SuppressWarnings("unused")
    private Context context;
    private String url;
    private String pictureUrl;
    private String content;
    private String title;

    public ImageShape(Object tag, int coverColor, View view, Context context,
            String url, String pictureUrl, String content, String title)
    {
        super(tag, coverColor);
        this.context = context;
        this.view = view;
        this.url = url;
        this.pictureUrl = pictureUrl;
        this.content = content;
        this.title = title;
    }

    /**
     * Set Center,radius
     * 
     * @param coords
     *            centerX,CenterY,radius
     */
    @Override
    public void setValues(float... coords)
    {

        final float centerX = coords[0];
        final float centerY = coords[1];

        this.center = new PointF(centerX, centerY);

        if (coords.length > 2)
        {
            this.radius = coords[2];
        }

    }

    public void setRadius(float radius)
    {
        this.radius = radius;
    }

    @Override
    public PointF getCenterPoint()
    {
        return MatrixConverHelper.mapMatrixPoint(mOverMatrix, center.x, center.y);
    }

    @Override
    public void draw(Canvas canvas)
    {
    	PointF f = MatrixConverHelper.mapMatrixPoint(mOverMatrix, center.x, center.y);
        drawPaint.setAlpha(alaph);
        Bitmap bitmap = convertViewToBitmap(view);
        canvas.drawBitmap(bitmap, f.x - bitmap.getWidth() / 2f, f.y
                - bitmap.getHeight(), drawPaint);
    }

    public static Bitmap convertViewToBitmap(View view)
    {
        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    @Override
    public void scaleBy(float scale, float centerX, float centerY)
    {
        PointF newCenter = ScaleUtility.scaleByPoint(center.x, center.y,
                centerX, centerY, scale);
        radius *= scale;
        center.set(newCenter.x, newCenter.y);
    }

    @Override
    public void onScale(float scale)
    {
        // scaleBy = (float)Math.sqrt(scaleBy);
        radius *= scale;
        center.set(center.x *= scale, center.y *= scale);
    }

    @Override
    public void translate(float deltaX, float deltaY)
    {
        center.x += deltaX;
        center.y += deltaY;
    }

    @Override
    public boolean inArea(float x, float y)
    {
        boolean b = false;
        System.out.println(x + "---" + y);
        System.out.println(center.x + "++++" + center.y);
        if (x > center.x - 50 && x < center.x + 50 && y > center.y - 120
                && y < center.y)
        {
            b = true;
        } else
        {
            b = false;
        }

        return b;
    }

    @Override
    public String getUrl()
    {
        return url;
    }

    @Override
    public String getPictureUrl()
    {
        return pictureUrl;
    }

    @Override
    public String getContent()
    {
        return content;
    }

    @Override
    public String getTitle()
    {
        return title;
    }

    @Override
    public boolean bubbleTag()
    {
        return false;
    }

	@Override
	public float getCenterX() {
		return center.x;
	}

	@Override
	public float getCenterY() {
		return center.y;
	}

}
