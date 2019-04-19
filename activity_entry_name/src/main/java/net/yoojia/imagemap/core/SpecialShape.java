package net.yoojia.imagemap.core;

import net.yoojia.imagemap.R;
import net.yoojia.imagemap.support.MResource;
import net.yoojia.imagemap.support.ScaleUtility;
import net.yoojia.imagemap.util.MatrixConverHelper;
import net.yoojia.imagemap.util.SvgHelper.SvgPath;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ImageView;

public class SpecialShape extends Shape
{

    private PointF center;
    private PointF centers;
    private float radius = 13f;
    private View view;
    private String text;
    private String url;
    private Context context;
    private String pictureUrl;
    private Object tag;
    private int level;// 级别
    ImageView imageView;
    
    Bitmap bitmap;
    private SvgPath mSvgPath;

    public SvgPath getSvgPath()
    {
        return mSvgPath;
    }

    public void setSvgPath(SvgPath mSvgPath)
    {
        this.mSvgPath = mSvgPath;
    }

    @Override
    public float getCenterX()
    {
        return center.x;
    }

    @Override
    public float getCenterY()
    {
        return center.y;
    }

    public SpecialShape(Object tag, String url, String pictureUrl,
            int coverColor, ImageView image, String text, Context context,
            int level)
    {
        super(tag, coverColor);
        this.context = context;
        this.url = url;
        this.text = text;
        this.tag = tag;
        this.level = level;
        this.pictureUrl = pictureUrl;
        drawPaint.setTextSize(30);
        view = View.inflate(context, R.layout.special_shape, null);
        imageView = (ImageView) view.findViewById(R.id.special_logo);
        bitmap = convertViewToBitmap(view);
    }

    public Object getTag()
    {
        return tag;
    }

    public int getLevel()
    {
        return level;
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
        return MatrixConverHelper.mapMatrixPoint(mOverMatrix, center.x,
                center.y);
    }

    @Override
    public void draw(Canvas canvas)
    {

        PointF f = MatrixConverHelper.mapMatrixPoint(mOverMatrix, center.x,
                center.y);
        
        // canvas.drawCircle(center.x, center.y, 18, drawPaint);
        canvas.drawBitmap(bitmap, f.x - imageView.getWidth() / 2, f.y
                - imageView.getHeight() / 2, drawPaint);
       // canvas.drawCircle(f.x, f.y, 3, drawPaint);
        canvas.drawText(text, f.x + imageView.getWidth() / 2 + 3, f.y
                + imageView.getHeight() / 2 - 4, drawPaint);

        /*Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.parseColor("#881204"));
        if (mSvgPath != null)
        {
            canvas.save();
            canvas.setMatrix(mOverMatrix);
            switch (mSvgPath.getType())
            {
            case path:
                canvas.drawPath(mSvgPath.getPath(), paint);
                break;
            case rect:
                canvas.drawRect(mSvgPath.getRect(), paint);
                break;
            }
            canvas.restore();
        }*/
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
        center.set(newCenter.x, newCenter.y);
    }

    @Override
    public void onScale(float scale)
    {
        // scaleBy = (float)Math.sqrt(scaleBy);
        // radius *= scale;
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
        float r = radius;
        if (!ret && mSvgPath != null)
        {
            r = 12;
            ret = mSvgPath.isArea(x, y);
        }
        if (!ret)
        {
            PointF p = center;
            float dx = p.x - x;
            float dy = p.y - y;
            float d = new Float(Math.sqrt((dx * dx) + (dy * dy))).floatValue();
            if (d < r)
            {
                ret = true;
            }
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
