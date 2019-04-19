package net.yoojia.imagemap.core;

import net.yoojia.imagemap.support.CompassView;
import net.yoojia.imagemap.support.ScaleUtility;
import net.yoojia.imagemap.util.MatrixConverHelper;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.view.View;
import android.view.View.MeasureSpec;

public class RequestShape extends Shape {
	private PointF center;
	private float radius = 5f;
	Bitmap bitmap;

	@SuppressWarnings("unused")
	private Context context;
	private View view;


	@Override
	public float getCenterX() {
		return center.x;
	}

	@Override
	public float getCenterY() {
		return center.y;
	}

	public RequestShape(Object tag, int coverColor, View view, Context context) {
		super(tag, coverColor);
		this.view = view;
		this.context = context;
		bitmap = convertViewToBitmap(view);
	}

	public void setView(View view){
		this.view = view;
		bitmap = convertViewToBitmap(view);
	}

	/**
	 * Set Center,radius
	 * 
	 * @param coords
	 *            centerX,CenterY,radius
	 */
	@Override
	public void setValues(float... coords) {

		final float centerX = coords[0];
		final float centerY = coords[1];

		this.center = new PointF(centerX, centerY);

		if (coords.length > 2) {
			this.radius = coords[2];
		}

	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

	@Override
	public PointF getCenterPoint() {
		return MatrixConverHelper.mapMatrixPoint(mOverMatrix, center.x,
				center.y);
	}

	@Override
	public void draw(Canvas canvas) {
		PointF f = MatrixConverHelper.mapMatrixPoint(mOverMatrix, center.x,
				center.y);
		drawPaint.setAlpha(25);
		canvas.drawCircle(f.x, f.y, radius * mScale, drawPaint);
		canvas.drawBitmap(bitmap, f.x - bitmap.getWidth() / 2,
				f.y - bitmap.getHeight() / 2, drawPaints);
	}

	public static Bitmap convertViewToBitmap(View view) {
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.buildDrawingCache();
		Bitmap bitmap = view.getDrawingCache();
		return bitmap;
	}

	@Override
	public void scaleBy(float scale, float centerX, float centerY) {
		PointF newCenter = ScaleUtility.scaleByPoint(center.x, center.y,
				centerX, centerY, scale);
		radius *= scale;
		center.set(newCenter.x, newCenter.y);
	}

	@Override
	public void onScale(float scale) {
		radius *= scale;
		center.set(center.x *= scale, center.y *= scale);
	}

	@Override
	public void translate(float deltaX, float deltaY) {
		center.x += deltaX;
		center.y += deltaY;
	}

	@Override
	public boolean inArea(float x, float y) {
		boolean b = false;
		if (x > center.x - 30 && x < center.x + 30 && y > center.y - 100
				&& y < center.y) {
			b = false;
		} else {
			b = false;
		}
		return b;
	}

	@Override
	public String getUrl() {
		return null;
	}

	@Override
	public String getPictureUrl() {
		return null;
	}

	@Override
	public String getContent() {
		return null;
	}

	@Override
	public String getTitle() {
		return null;
	}

	@Override
	public boolean bubbleTag() {
		return false;
	}
}
