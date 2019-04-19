package net.yoojia.imagemap.core;

import net.yoojia.imagemap.R;
import net.yoojia.imagemap.support.ScaleUtility;
import net.yoojia.imagemap.util.MatrixConverHelper;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ImageView;

public class PushMessageShape extends Shape
{

    private PointF center;
    private float radius = 8f;
    private View view;
    private String text;
    private String url;
    private Context context;
    private String pictureUrl;
    private Object tag;
    
	@Override
	public float getCenterX() {
		return center.x;
	}

	@Override
	public float getCenterY() {
		return center.y;
	}

    public PushMessageShape(Object tag, String url, String pictureUrl,
            int coverColor, ImageView image, String text, Context context)
    {
        super(tag, coverColor);
        this.context = context;
        this.url = url;
        this.text = text;
        this.tag = tag;
        this.pictureUrl = pictureUrl;
        view = View.inflate(context, R.layout.message_shape, null);
    }

    public Object getTag()
    {
        return tag;
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
        ImageView imageView = (ImageView) view.findViewById(R.id.special_logo2);
        Bitmap bitmap = convertViewToBitmap(view);
        // canvas.drawCircle(center.x, center.y, 18, drawPaint);
        canvas.drawBitmap(bitmap, f.x - imageView.getWidth() / 2, f.y
                - imageView.getHeight()/2, drawPaint);
//        canvas.drawText(text, center.x + imageView.getWidth() / 2+3,
//                center.y + imageView.getHeight() / 2-4, drawPaint);
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
        // radius *= scale;
//        center.set(newCenter.x, newCenter.y);
    }

    @Override
    public void onScale(float scale)
    {
        // scaleBy = (float)Math.sqrt(scaleBy);
        // radius *= scale;
//        center.set(center.x *= scale, center.y *= scale);
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
        boolean ret = false;
        float dx = center.x - Math.abs(x);
        float dy = center.y - Math.abs(y);
        float d = new Float(Math.sqrt((dx * dx) + (dy * dy))).floatValue();
        if (d < radius)
        {
            ret = true;
        }
        return ret;
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
        return text;
    }

    @Override
    public String getTitle()
    {
        return null;
    }

    @Override
    public boolean bubbleTag()
    {
        return true;
    }
}
