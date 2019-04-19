package net.yoojia.imagemap.core;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.v4.internal.view.SupportMenu;
import net.yoojia.imagemap.support.ScaleUtility;
import net.yoojia.imagemap.util.MatrixConverHelper;

public class LineShape extends Shape {
    private float bottom;
    private float dis;
    private boolean isDraw = true;
    private float left;
    private Paint paint = new Paint();
    private Paint paint2;
    private Path path;
    private float right;
    private float top;

    public LineShape(Object tag, int coverColor) {
        super(tag, coverColor);
        this.paint.setColor(Color.parseColor("#8bc34a"));
        this.paint.setStyle(Style.STROKE);
        this.paint.setStrokeWidth(6.0f);
        this.paint.setAntiAlias(true);
        this.paint.setPathEffect(new CornerPathEffect(9.9f));
        this.paint2 = new Paint();
        this.paint2.setColor(SupportMenu.CATEGORY_MASK);
        this.paint2.setAntiAlias(true);
        this.paint2.setTextSize(30.0f);
    }

    public void setValues(float... coords) {
        if (coords == null || coords.length != 5) {
            throw new IllegalArgumentException("Please set values with 4 paramters: left,top,right,buttom");
        }
        this.left = coords[0];
        this.top = coords[1];
        this.right = coords[2];
        this.bottom = coords[3];
        this.dis = coords[4];
    }

    public void onScale(float scale) {
        this.left *= scale;
        this.top *= scale;
        this.right *= scale;
        this.bottom *= scale;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public void isDrawDis(boolean isDraw) {
        this.isDraw = isDraw;
    }

    public void draw(Canvas canvas) {
        PointF pa = MatrixConverHelper.mapMatrixPoint(this.mOverMatrix, this.left, this.top);
        PointF pb = MatrixConverHelper.mapMatrixPoint(this.mOverMatrix, this.right, this.bottom);
        canvas.drawLine(pa.x, pa.y, pb.x, pb.y, this.paint);
        if (this.isDraw) {
            canvas.drawText(this.dis + "m", (pa.x + pb.x) / 2.0f, (pa.y + pb.y) / 2.0f, this.paint2);
        }
    }

    public void scaleBy(float scale, float centerX, float centerY) {
        PointF leftTop = ScaleUtility.scaleByPoint(this.left, this.top, centerX, centerY, scale);
        this.left = leftTop.x;
        this.top = leftTop.y;
        PointF righBottom = ScaleUtility.scaleByPoint(this.right, this.bottom, centerX, centerY, scale);
        this.right = righBottom.x;
        this.bottom = righBottom.y;
    }

    public void translate(float deltaX, float deltaY) {
        this.left += deltaX;
        this.right += deltaX;
        this.top += deltaY;
        this.bottom += deltaY;
    }

    public boolean inArea(float x, float y) {
        if (x > this.left && x < this.right && y > this.top && y < this.bottom) {
        }
        return false;
    }

    public PointF getCenterPoint() {
        return new PointF((this.left + this.right) / 2.0f, (this.top + this.bottom) / 2.0f);
    }

    public String getUrl() {
        return null;
    }

    public String getPictureUrl() {
        return null;
    }

    public String getContent() {
        return null;
    }

    public String getTitle() {
        return null;
    }

    public boolean bubbleTag() {
        return false;
    }

    public float getCenterX() {
        return (this.left + this.right) / 2.0f;
    }

    public float getCenterY() {
        return (this.top + this.bottom) / 2.0f;
    }

    public float getRadius() {
        return 0.0f;
    }
}
