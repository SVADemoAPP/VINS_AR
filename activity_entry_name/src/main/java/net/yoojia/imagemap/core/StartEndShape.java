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

public class StartEndShape extends Shape {

	private PointF center;
	@SuppressWarnings("unused")
	private float radius = 5f;
	private View view;
	@SuppressWarnings("unused")
	private Context context;

	@Override
	public float getCenterX() {
		return center.x;
	}

	@Override
	public float getCenterY() {
		return center.y;
	}

	public StartEndShape(Object tag, int coverColor, View view, Context context) {
		super(tag, coverColor);
		this.context = context;
		this.view = view;

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
		drawPaint.setAlpha(alaph); // 關機重啟一下有關係么 是不是這次又延遲一樣的旋轉縮放都會有問題

		Bitmap bitmap = convertViewToBitmap(view);
		// Bitmap bitmap=BitmapFactory.decodeResource(context.getResources(),
		// R.drawable.point);
		// canvas.drawCircle(center.x, center.y, 18, drawPaint);
		// bitmap=Bitmap.createScaledBitmap(bitmap, 36,44, true);
		// drawPaint.setTextSize(40);
		// drawPaint.setColor(Color.RED);
		// canvas.drawText(this.message, center.x-bitmap.getWidth()/2,
		// center.y-bitmap.getHeight(), drawPaint);
		canvas.drawBitmap(bitmap, f.x - bitmap.getWidth() / 2,
				f.y - bitmap.getHeight(), drawPaint);
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
		// scaleBy = (float)Math.sqrt(scaleBy);
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
		return false;
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
