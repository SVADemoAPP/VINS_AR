package net.yoojia.imagemap.util;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;

/**
 * libo
 */
public class MatrixConverHelper {

	/**
	 * 此方法会将一个RectF(0, 0, width, height)(区域), 按照matrix变换, 用于计算一个区域按照
	 * Matrix变换后该区域的位置
	 * 
	 * @param matrix
	 * @param width
	 * @param height
	 * @return 返回变换后的Rect
	 */
	public static RectF mapMatrixRect(Matrix matrix, float width, float height) {
		RectF rectF = new RectF(0, 0, width, height);
		if (matrix == null)
			return rectF;
		matrix.mapRect(rectF);
		return rectF;
	}

	/**
	 * 此方法会将一个RectF(left, top, right, bottom)(区域), 按照matrix变换, 用于计算一个区域按照
	 * Matrix变换后该区域的位置
	 * 
	 * @param matrix
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @return
	 */
	public static RectF mapMatrixRect(Matrix matrix, float left, float top,
			float right, float bottom) {
		RectF rectF = new RectF(left, top, right, bottom);
		if (matrix == null)
			return rectF;
		matrix.mapRect(rectF);
		return rectF;
	}

	/**
	 * 此方法会将一个RectF(区域), 按照matrix变换, 用于计算一个区域按照 Matrix变换后该区域的位置
	 * 
	 * @param matrix
	 * @param rect
	 *            注:当前方法会改变传入rect的原有值
	 * @return
	 */
	public static RectF mapMatrixRect(Matrix matrix, RectF rect) {
		RectF to = new RectF(rect);
		if (matrix == null)
			return rect;
		matrix.mapRect(to);
		return to;
	}

	/**
	 * 此方法会将一个点, 按照matrix变换, 用于计算该点按照 Matrix变换后的位置
	 * 
	 * @param matrix
	 * @param x
	 * @param y
	 * @return
	 */
	public static PointF mapMatrixPoint(Matrix matrix, float x, float y) {
		float[] point = new float[] { x, y };
		if (matrix == null)
			return new PointF(point[0], point[1]);
		matrix.mapPoints(point);
		return new PointF(point[0], point[1]);
	}

	/**
	 * 计算两点之间的距离
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public static float distance(PointF x, PointF y) {
		return (float) Math.sqrt(Math.pow(x.x - y.x, 2)
				+ Math.pow(x.y - y.y, 2));
	}

	/**
	 * 计算缩放scale倍后两点的距离
	 * 
	 * @param scale
	 * @param x
	 * @param y
	 * @return
	 */
	public static float distance(float scale, PointF x, PointF y) {
		return (float) Math.sqrt(Math.pow((x.x - y.x) * scale, 2)
				+ Math.pow((x.y - y.y) * scale, 2));
	}

	/**
	 * 当前点是否在path区域内
	 * 
	 * @param path
	 * @param eventX
	 * @param eventY
	 * @return
	 */
	public static boolean isArea(Path path, int eventX, int eventY) {
		// 构造一个区域对象，左闭右开的。
		RectF r = new RectF();
		Region re = new Region();
		// 计算控制点的边界
		path.computeBounds(r, true);
		// 设置区域路径和剪辑描述的区域
		re.setPath(path, new Region((int) r.left, (int) r.top,
				(int) r.right, (int) r.bottom));
		// 在封闭的path内返回true 不在返回falses
		return re.contains(eventX, eventY);
	}
}
