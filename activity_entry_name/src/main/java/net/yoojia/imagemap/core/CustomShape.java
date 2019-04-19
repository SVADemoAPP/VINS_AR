package net.yoojia.imagemap.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ImageView;

import net.yoojia.imagemap.R;
import net.yoojia.imagemap.support.ScaleUtility;
import net.yoojia.imagemap.util.MatrixConverHelper;

public class CustomShape extends Shape{

	private PointF center;
    @SuppressWarnings("unused")
    private float radius = 5f;
    private Context context;
    private String url;
    private String pictureUrl;
    private String content;
    private String title;
    Bitmap bitmap;
    public CustomShape(Object tag, int coverColor, Context context, String content,int imageResource)
    {
        super(tag, coverColor);
        this.context = context;
        this.content = content;
        View view = View.inflate(context, R.layout.collect_shape_pop, null);
        ImageView imageView = (ImageView)view.findViewById(R.id.iv_coll_point);
        imageView.setImageResource(imageResource);
        bitmap = convertViewToBitmap(view);
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
        canvas.drawBitmap(bitmap, f.x - bitmap.getWidth() / 2f, f.y- bitmap.getHeight()/2f, drawPaint);
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
        return true;
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
