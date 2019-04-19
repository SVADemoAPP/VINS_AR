package net.yoojia.imagemap.animator;

import android.graphics.Matrix;
import android.graphics.PointF;

public interface MapHandle {
	
	void postMatrixScale(float scale, PointF midpPoint);
	
	void postImageMatrix();
	
	void postMatrixTranslate(float deltaX, float deltaY);
	
	void postMatrixRotate(float rotate, PointF midPoint);
	
	void checkScale();
	
	void checkTranslate();
	
	Matrix getImageMatrix();
}
