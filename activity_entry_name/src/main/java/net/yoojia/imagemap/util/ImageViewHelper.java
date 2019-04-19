package net.yoojia.imagemap.util;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;

/**
 * Created by yipenga on 2016/1/11.
 */
public class ImageViewHelper {
	private final PointF startPoint = new PointF();
	private final PointF endPoint = new PointF();
	private final RectF mDrawableRect = new RectF();

	private int mInitDis = 0;

	private float mMaxPullScale = 1f; // 最大拉伸倍数
	private float mMinZoomScale = 1f; // 最大压缩倍数
	private float maxSize = 10; // 最大與最小的比例

	private int mView_width;
	private int mView_height;

	private int mDrawable_width;
	private int mDrawable_height;

	public OnMaxZoomCallback mOnMaxZoomCallback;
	public OnMinZoomCallback mOnMinZoomCallback;

	public boolean havePullScale = true;
	public boolean haveZoomScale = true;

	public ImageViewHelper() {

	}

	/**
	 * 獲取最大拉伸比例
	 * 
	 * @return
	 */
	public float getMaxPullScale() {
		return mMaxPullScale;
	}

	/**
	 * 獲取最小縮放比例
	 * 
	 * @return
	 */
	public float getMinZoomScale() {
		return mMinZoomScale;
	}

	/**
	 * 設置View的宽高
	 * 
	 * @param width
	 * @param height
	 */
	public void setViewSize(int width, int height) {
		this.mView_height = height;
		this.mView_width = width;
	}

	/**
	 * 设置图片宽高
	 * 
	 * @param width
	 * @param height
	 */
	public void setDrawableSize(int width, int height) {
		this.mDrawable_width = width;
		this.mDrawable_height = height;
		this.mDrawableRect.set(new RectF(0, 0, width, height));
		this.startPoint.set(0, 0);
		this.endPoint.set(0, height);
	}

	public float getMaxSize() {
		return maxSize;
	}

	/**
	 * 設置最大倍數
	 * 
	 * @param maxSize
	 */
	public void setMaxSize(float maxSize) {
		if (maxSize <= 1) {
			throw new RuntimeException(" maxSize 必须必1大 ");
		}
		this.maxSize = maxSize;
	}

	/**
	 * 獲取當前圖片的中心點
	 * 
	 * @param matrix
	 * @return
	 */
	public PointF getCenterPoint(Matrix matrix) {
		return MatrixConverHelper.mapMatrixPoint(matrix, mDrawable_width / 2,
				mDrawable_height / 2f);
	}

	/**
	 * 获取当前缩放倍数
	 * 
	 * @param matrix
	 * @return
	 */
	public float getCurrentScale(Matrix matrix) {
		PointF point_x = MatrixConverHelper.mapMatrixPoint(matrix,
				this.startPoint.x, this.startPoint.y);
		PointF point_y = MatrixConverHelper.mapMatrixPoint(matrix,
				this.endPoint.x, this.endPoint.y);
		float curDis = Math
				.round(MatrixConverHelper.distance(point_x, point_y));

		return curDis / mInitDis;
	}

	public void initImageMatrix(Matrix matrix) {
		float scale = SVGSizeHelper.getBitmapMatchScale(mView_width // 获取适配屏幕的比例值
				, mView_height, mDrawable_width, mDrawable_height);

		mMinZoomScale = scale;
		mMaxPullScale = maxSize * scale;

		int offsetX = mView_width / 2 - mDrawable_width / 2;
		int offsetY = mView_height / 2 - mDrawable_height / 2;

		this.mInitDis = (int) MatrixConverHelper.distance(startPoint, endPoint);

		matrix.postTranslate(offsetX, offsetY);
		matrix.postScale(scale, scale, mView_width / 2, mView_height / 2);
		haveZoomScale = false;
		if (mOnMinZoomCallback != null) {
			mOnMinZoomCallback.onMinZoom(haveZoomScale);
		}
	}

	/**
	 * 用于检查svg是否还能伸缩, 当返回-1时, 表示当前操作不能执行, 即不能伸缩
	 * 
	 * @param matrix
	 * @param midPoint
	 */
	public void checkScale(Matrix matrix, PointF midPoint) {
		PointF point_x = MatrixConverHelper.mapMatrixPoint(matrix,
				this.startPoint.x, this.startPoint.y);
		PointF point_y = MatrixConverHelper.mapMatrixPoint(matrix,
				this.endPoint.x, this.endPoint.y);
		int curDis = Math.round(MatrixConverHelper.distance(point_x, point_y));

		int toDis = 0;

		int maxDis = Math.round(mInitDis * mMaxPullScale);
		int minDis = Math.round(mInitDis * mMinZoomScale);
		havePullScale = true;
		haveZoomScale = true;

		if (curDis > mInitDis * mMaxPullScale) {
			toDis = maxDis;
			havePullScale = false;
		} else if (curDis < mInitDis * mMinZoomScale) {
			toDis = minDis;
			haveZoomScale = false;
		} else {
			havePullScale = !(maxDis == curDis);
			haveZoomScale = !(minDis == curDis);
			if (mOnMaxZoomCallback != null) {
				mOnMaxZoomCallback.onMaxZoom(havePullScale);
			}
			if (mOnMinZoomCallback != null) {
				mOnMinZoomCallback.onMinZoom(haveZoomScale);
			}
			return;
		}
		if (mOnMaxZoomCallback != null) {
			mOnMaxZoomCallback.onMaxZoom(havePullScale);
		}
		if (mOnMinZoomCallback != null) {
			mOnMinZoomCallback.onMinZoom(haveZoomScale);
		}
		float scale = toDis * 1f / curDis;
		matrix.postScale(scale, scale, midPoint.x, midPoint.y);
	}

	/**
	 * 检查Matrix移动边界
	 * 
	 * @param matrix
	 */
	public void checkTranslate(Matrix matrix) {
		RectF rectF = new RectF(mDrawableRect);
		matrix.mapRect(rectF);

		int maxTop = mView_height / 2;
		int maxLeft = mView_width / 2;

		int minBottom = maxTop;
		int minRight = maxLeft;

		float offsetX = 0;
		float offsetY = 0;

		if (rectF.left > maxLeft) {
			offsetX -= rectF.left - maxLeft;
		}
		if (rectF.right < minRight) {
			offsetX += minRight - rectF.right;
		}
		if (rectF.top > maxTop) {
			offsetY -= rectF.top - maxTop;
		}
		if (rectF.bottom < minBottom) {
			offsetY += minBottom - rectF.bottom;
		}
		if (offsetX == 0 && offsetY == 0)
			return;
		matrix.postTranslate(offsetX, offsetY);
	}

	/**
	 * 判斷點是否在區域內
	 * 
	 * @param matrix
	 * @param point
	 * @return
	 */
	public boolean pointInArea(Matrix m, float saveRotate, PointF point) {
		if (saveRotate == 0) {
			RectF rectF = MatrixConverHelper.mapMatrixRect(m, mDrawableRect);
			if (rectF.left <= point.x && rectF.right >= point.x
					&& rectF.top <= point.y && rectF.bottom >= point.y) {
				return true;
			}
		} else {
			PointF center = getCenterPoint(m);
			Matrix matrix = new Matrix();
			matrix.postRotate(saveRotate, center.x, center.y);
			PointF toPoint = MatrixConverHelper.mapMatrixPoint(matrix, point.x,
					point.y);
			matrix.set(m);
			matrix.postRotate(saveRotate, center.x, center.y);
			RectF rectF = MatrixConverHelper.mapMatrixRect(matrix,
					mDrawableRect);

			if (rectF.left <= toPoint.x && rectF.right >= toPoint.x
					&& rectF.top <= toPoint.y && rectF.bottom >= toPoint.y) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 獲取點在矩陣變換前的對應點
	 * 
	 * @param imageUsingMatrix
	 * @param saveRotate
	 * @param point
	 * @return
	 */
	public PointF getSinglePoint(Matrix curMatrix, float saveRotate,
			PointF point) {
		PointF to = new PointF();
		if (saveRotate == 0) {
			float[] values = new float[9];
			curMatrix.getValues(values);
			float dx = values[Matrix.MTRANS_X];
			float dy = values[Matrix.MTRANS_Y];
			float scale = values[Matrix.MSCALE_X];
			
			float x = (point.x - dx) / scale; 
			float y = (point.y - dy) / scale; 
			
			to.set(x, y);
		} else {
			PointF center = getCenterPoint(curMatrix);
			Matrix matrix = new Matrix();
			matrix.postRotate(saveRotate, center.x, center.y);
			PointF toPoint = MatrixConverHelper.mapMatrixPoint(matrix, point.x,
					point.y);
			matrix.set(curMatrix);
			matrix.postRotate(saveRotate, center.x, center.y);
			
			float[] values = new float[9];
			matrix.getValues(values);
			float dx = values[Matrix.MTRANS_X];
			float dy = values[Matrix.MTRANS_Y];
			float scale = values[Matrix.MSCALE_X];
			
			float x = (toPoint.x - dx) / scale; 
			float y = (toPoint.y - dy) / scale; 
			
			to.set(x, y);
		}
		return to;
	}

	/**
	 * 设置缩放到最小的监听
	 * 
	 * @param callback
	 */
	public void setOnMinZoomScaleCallback(OnMinZoomCallback callback) {
		this.mOnMinZoomCallback = callback;
		if (mOnMinZoomCallback != null) {
			mOnMinZoomCallback.onMinZoom(haveZoomScale);
		}
	}

	/**
	 * 设置拉伸到最大的监听
	 * 
	 * @param callback
	 */
	public void setOnMaxZoomScaleCallback(OnMaxZoomCallback callback) {
		this.mOnMaxZoomCallback = callback;
		if (mOnMaxZoomCallback != null) {
			mOnMaxZoomCallback.onMaxZoom(havePullScale);
		}
	}

	/**
	 * 用于回调图片缩放到最小
	 */
	public interface OnMinZoomCallback {
		void onMinZoom(boolean haveZoom);
	}

	/**
	 * 用于回调图片拉伸到最大
	 */
	public interface OnMaxZoomCallback {
		void onMaxZoom(boolean havePull);
	}
}
