package net.yoojia.imagemap.core;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

public abstract class Shape {
    public final int color;
    public final Object tag;
    protected Bubble displayBubble;
    protected int alaph = 255;
    protected Paint drawPaint;
    protected Paint drawPaints;
    protected final static Paint cleanPaint;
    protected final Matrix mOverMatrix = new Matrix();
    protected float mScale = 1f;

    static {
        cleanPaint = new Paint();
        cleanPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }


    public float getScale() {
        return mScale;
    }

    public void setScale(float mScale) {
        this.mScale = mScale;
    }

    public Shape(Object tag, int coverColor) {
        this.tag = tag;
        this.color = coverColor;
        drawPaint = new Paint();
        drawPaint.setColor(coverColor);
        drawPaint.setStyle(Paint.Style.FILL);
        drawPaint.setAntiAlias(true);
        drawPaint.setFilterBitmap(true);
        drawPaint.setTextSize(20);
        drawPaints = new Paint();
        drawPaints.setColor(coverColor);
        drawPaints.setStyle(Paint.Style.FILL);
        drawPaints.setAntiAlias(true);
        drawPaints.setFilterBitmap(true);
    }

    public void setAlaph(int alaph) {
        this.alaph = alaph;
    }

    public void createBubbleRelation(Bubble displayBubble) {
        this.displayBubble = displayBubble;
    }

    public void cleanBubbleRelation() {
        this.displayBubble = null;
    }

    public abstract void setValues(float... coords);

    /**
     * Set coords. Split by char ',' .
     *
     * @param coords coords
     */
//	public void setValues(String coords) {
//		String[] parametrs = coords.split(",");
//		final int size = parametrs.length;
//		final float[] args = new float[size];
//		for (int i = 0; i < size; i++) {
//			args[i] = Float.valueOf(parametrs[i].trim());
//		}
//		setValues(args);
//	}
    public void setValues(String coords) {
        String[] parametrs = coords.split(":");
        final int size = parametrs.length;
        final float[] args = new float[size];
        for (int i = 0; i < size; i++) {
            args[i] = Float.valueOf(parametrs[i].replace(",", ".").trim());
        }
        setValues(args);
    }

    /**
     * 由HightlightImageView调度
     *
     * @param scale   缩放量
     * @param centerX 缩放中心 x
     * @param centerY 缩放中心 y
     */
    public final void onScale(float scale, float centerX, float centerY) {
        // scaleBy(scale, centerX, centerY);
        if (displayBubble != null) {
            displayBubble.showAtShape(this);
        }
    }

    public abstract void onScale(float scale);

    /**
     * 由HightlightImageView调度
     *
     * @param deltaX 移动量 x
     * @param deltaY 移动量 y
     */
    public final void onTranslate(float deltaX, float deltaY) {
        // translate(deltaX, deltaY);
        if (displayBubble != null) {
            displayBubble.showAtShape(this);
        }
    }

    /**
     * 由HightlightImageView调度。
     *
     * @param canvas 绘制画布
     */
    public final void onDraw(Canvas canvas) {
        draw(canvas);
        // 如果当前Shape与Bubble有关联，则将Bubble也显示出来
        if (displayBubble != null) {
            displayBubble.showAtShape(this);
        }
    }

    public abstract void draw(Canvas canvas);

    public abstract boolean bubbleTag();

    public abstract void scaleBy(float scale, float centerX, float centerY);

    public abstract void translate(float deltaX, float deltaY);

    public abstract boolean inArea(float x, float y);

    public abstract PointF getCenterPoint();

    public abstract String getUrl();

    public abstract String getPictureUrl();

    public abstract String getContent();

    public abstract String getTitle();

    public abstract float getCenterX();

    public abstract float getCenterY();

    public static String getCenterCoord(final String _coords) {
        if (_coords == null || _coords.trim().length() == 0)
            return null;

        try {
            String coords = _coords.trim();
            String[] pts = coords.split(" ");
            int nPts = pts.length;
            float x = 0;
            float y = 0;
            float f;
            int j = nPts - 1;
            String p1;
            String p2;
            float p1_x;
            float p1_y;
            float p2_x;
            float p2_y;
            for (int i = 0; i < nPts; j = i++) {
                p1 = pts[i].trim();
                if (p1.length() == 0)
                    continue;

                p1_x = Float.parseFloat(p1.split(",")[0]);
                p1_y = Float.parseFloat(p1.split(",")[1]);

                p2 = pts[j].trim();
                if (p2.length() == 0)
                    continue;

                p2_x = Float.parseFloat(p2.split(",")[0]);
                p2_y = Float.parseFloat(p2.split(",")[1]);

                f = p1_x * p2_y - p2_x * p1_y;
                x += (p1_x + p2_x) * f;
                y += (p1_y + p2_y) * f;
            }

            f = area(pts) * 6;

            return x / f + "," + y / f;
        } catch (Throwable e) {
            return null;
        }
    }

    private static float area(String[] pts) {
        float area = 0;
        int nPts = pts.length;
        int j = nPts - 1;
        String p1 = null;
        String p2 = null;

        float p1_x;
        float p1_y;
        float p2_x;
        float p2_y;
        for (int i = 0; i < nPts; j = i++) {
            p1 = pts[i].trim();
            if (p1.length() == 0)
                continue;

            p1_x = Float.parseFloat(p1.split(",")[0]);
            p1_y = Float.parseFloat(p1.split(",")[1]);

            p2 = pts[j].trim();
            if (p2.length() == 0)
                continue;

            p2_x = Float.parseFloat(p2.split(",")[0]);
            p2_y = Float.parseFloat(p2.split(",")[1]);

            area += p1_x * p2_y;
            area -= p1_y * p2_x;
        }

        area /= 2;
        return area;
    }

    public void postMatrixCahnge(Matrix matrix) {
        mOverMatrix.set(matrix);
    }

}
