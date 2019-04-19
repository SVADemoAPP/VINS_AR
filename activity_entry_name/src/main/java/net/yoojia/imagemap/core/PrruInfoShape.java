package net.yoojia.imagemap.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.view.View;
import android.view.View.MeasureSpec;

import net.yoojia.imagemap.R;
import net.yoojia.imagemap.support.ScaleUtility;
import net.yoojia.imagemap.util.BaZhanUtil;
import net.yoojia.imagemap.util.MatrixConverHelper;

public class PrruInfoShape extends Shape {
    private static /* synthetic */ int[] $SWITCH_TABLE$net$yoojia$imagemap$core$pRRUInfoShape$pRRUType;
    private Bitmap bmp_inArea;
    private Bitmap bmp_outArea;
    private Bitmap bmp_show;
    private PointF center;
    private PointF centers;
    private Context context;
    private String id;
    private String pictureUrl;
    private float radius = 20.0f;
    private Object tag;
    private String text = "";
    private String url;
    private View view_inArea;
    private View view_outArea;
    private boolean isBind = false;
    private boolean isMove = false;

    public boolean getMove() {
        return isMove;
    }

    public void setMove(boolean move) {
        isMove = move;
    }

    public boolean isBind() {
        return isBind;
    }

    public void setBind(boolean bind) {
        isBind = bind;
    }

    public PointF getCenter() {
        return center;
    }

    public enum pRRUType {
        outArea,
        inArea,
        temple
    }

    static /* synthetic */ int[] $SWITCH_TABLE$net$yoojia$imagemap$core$pRRUInfoShape$pRRUType() {
        int[] iArr = $SWITCH_TABLE$net$yoojia$imagemap$core$pRRUInfoShape$pRRUType;
        if (iArr == null) {
            iArr = new int[pRRUType.values().length];
            try {
                iArr[pRRUType.temple.ordinal()] = 3;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[pRRUType.inArea.ordinal()] = 2;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[pRRUType.outArea.ordinal()] = 1;
            } catch (NoSuchFieldError e2) {
            }

            $SWITCH_TABLE$net$yoojia$imagemap$core$pRRUInfoShape$pRRUType = iArr;
        }
        return iArr;
    }

    public float getCenterX() {
        return this.center.x;
    }

    public float getCenterY() {
        return this.center.y;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public PrruInfoShape(Object tag, int coverColor, Context context) {
        super(tag, coverColor);
        this.context = context;
        this.tag = tag;
        this.drawPaint.setTextSize(30.0f);
        this.drawPaint.setColor(Color.GRAY);
//        Xfermode xFermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);
//        this.drawPaint.setXfermode(xFermode);
        setPrruShowType(pRRUType.outArea);
    }

    public Object getTag() {
        return this.tag;
    }

    public void setTransport() {
        bmp_show = BaZhanUtil.getTransparentBitmap(bmp_show, 50);
    }

    public void setPrruShowType(pRRUType type) {
        switch ($SWITCH_TABLE$net$yoojia$imagemap$core$pRRUInfoShape$pRRUType()[type.ordinal()]) {
            case 1:
                if (this.view_outArea == null) {
                    this.view_outArea = View.inflate(this.context, R.layout.prru_out_area, null);
                    this.bmp_outArea = convertViewToBitmap(this.view_outArea);
                }
                setRadius((float) ((this.bmp_outArea.getWidth() / 2) + 90));
                this.bmp_show = this.bmp_outArea;
                return;
            case 2:
                if (this.view_inArea == null) {
                    this.view_inArea = View.inflate(this.context, R.layout.prru_in_area, null);
                    this.bmp_inArea = convertViewToBitmap(this.view_inArea);
                }
                setRadius((float) ((this.bmp_inArea.getWidth() / 2) + 90));
                this.bmp_show = this.bmp_inArea;
                return;
            case 3:
                if (this.view_inArea == null) {
                    this.view_inArea = View.inflate(this.context, R.layout.prru_red, null);
                    this.bmp_inArea = convertViewToBitmap(this.view_inArea);
                }
                setRadius((float) ((this.bmp_inArea.getWidth() / 2) + 90));
                this.bmp_show = this.bmp_inArea;
                return;
            default:
                return;
        }
    }

    public void setValues(float... coords) {
        this.center = new PointF(coords[0], coords[1]);
        if (coords.length > 2) {
            this.radius = coords[2];
        }
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public PointF getCenterPoint() {
        return MatrixConverHelper.mapMatrixPoint(this.mOverMatrix, this.center.x, this.center.y);
    }

    public void draw(Canvas canvas) {
        PointF f = MatrixConverHelper.mapMatrixPoint(this.mOverMatrix, this.center.x, this.center.y);
        canvas.drawBitmap(this.bmp_show, f.x - ((float) (this.bmp_show.getWidth() / 2)), f.y - ((float) (this.bmp_show.getHeight() / 2)), this.drawPaint);
        canvas.drawText(this.text, f.x + ((float) (this.bmp_show.getWidth() / 2)), f.y, this.drawPaint);
    }

    public void setText(String text) {
        this.text = text;
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
        float sqrt = (float) Math.sqrt((double) ((dx * dx) + (dy * dy)));
        if (sqrt < r) {
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
        return this.text;
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
