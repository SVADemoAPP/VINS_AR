package net.yoojia.imagemap.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;
import android.view.View.MeasureSpec;
import net.yoojia.imagemap.R;
import net.yoojia.imagemap.support.ScaleUtility;
import net.yoojia.imagemap.util.MatrixConverHelper;

public class PrruGkcShape extends Shape {
    private Bitmap bmp_show;
    private PointF center;
    private PointF centers;
    private Context context;
    private String neCode = "";
    private String pictureUrl;
    private float radius = 13.0f;
    private String rsrp = "";
    private Object tag;
    private String url;
    private View view_inArea;
    private View view_outArea;

    public float getCenterX() {
        return this.center.x;
    }

    public float getCenterY() {
        return this.center.y;
    }

    public PrruGkcShape(Object tag, int coverColor, Context context) {
        super(tag, coverColor);
        this.context = context;
        this.tag = tag;
        this.drawPaint.setTextSize(28.0f);
        if(coverColor == Color.YELLOW){
            this.view_inArea = View.inflate(context, R.layout.prru_in_area, null);
        }else {
            this.view_inArea = View.inflate(context, R.layout.prru_red, null);
        }

        this.bmp_show = convertViewToBitmap(this.view_inArea);
    }

    public Object getTag() {
        return this.tag;
    }

    public void setPaintColor(int color) {
        this.drawPaint.setColor(color);
    }

    public void setValues(float... coords) {
        this.center = new PointF(coords[0], coords[1]);
        if (coords.length > 2) {
            this.radius = coords[2];
        }
    }

    public PointF getCenterPoint() {
        return MatrixConverHelper.mapMatrixPoint(this.mOverMatrix, this.center.x, this.center.y);
    }

    public void draw(Canvas canvas) {
        PointF f = MatrixConverHelper.mapMatrixPoint(this.mOverMatrix, this.center.x, this.center.y);
        canvas.drawBitmap(this.bmp_show, f.x - ((float) (this.bmp_show.getWidth() / 2)), f.y - ((float) (this.bmp_show.getHeight() / 2)), this.drawPaint);
        canvas.drawText(this.neCode, (f.x + ((float) (this.bmp_show.getWidth() / 2))) + 2.0f, f.y, this.drawPaint);
        canvas.drawText(this.rsrp, (f.x + ((float) (this.bmp_show.getWidth() / 2))) + 2.0f, f.y + 20.0f, this.drawPaint);
    }

    public void setNecodeText(String neCode) {
        this.neCode = neCode;
    }

    public void setRsrpText(String rsrp) {
        this.rsrp = rsrp;
    }

    public static Bitmap convertViewToBitmap(View view) {
        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        return view.getDrawingCache();
    }

    public void scaleBy(float scale, float centerX, float centerY) {
        PointF newCenter = ScaleUtility.scaleByPoint(this.center.x, this.center.y, centerX, centerY, scale);
        this.center.set(newCenter.x, newCenter.y);
    }

    public void onScale(float scale) {
        PointF pointF = this.center;
        PointF pointF2 = this.center;
        float f = pointF2.x * scale;
        pointF2.x = f;
        pointF2 = this.center;
        float f2 = pointF2.y * scale;
        pointF2.y = f2;
        pointF.set(f, f2);
    }

    public void translate(float deltaX, float deltaY) {
        PointF pointF = this.center;
        pointF.x += deltaX;
        pointF = this.center;
        pointF.y += deltaY;
    }

    public boolean inArea(float x, float y) {
        float r = this.radius;
        if (null != null) {
            return false;
        }
        PointF p = this.center;
        float dx = p.x - x;
        float dy = p.y - y;
        if (new Float(Math.sqrt((double) ((dx * dx) + (dy * dy)))).floatValue() < r) {
            return true;
        }
        return false;
    }

    public String getUrl() {
        return this.url;
    }

    public String getPictureUrl() {
        return this.pictureUrl;
    }

    public String getContent() {
        return this.neCode + "_" + this.rsrp;
    }

    public String getTitle() {
        return null;
    }

    public boolean bubbleTag() {
        return true;
    }

    public float getRadius() {
        return this.radius;
    }
}
