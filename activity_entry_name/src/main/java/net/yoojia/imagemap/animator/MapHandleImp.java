package net.yoojia.imagemap.animator;

import net.yoojia.imagemap.util.MatrixConverHelper;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation.AnimationListener;

public class MapHandleImp {

	private MapHandle mHandle;
	private boolean isAniming = false;
	private Object lock = new Object();

	private float mOffsetY = 0;
	private float mOffsetX = 0;
	
	public void reset(){
		mOffsetX = 0;
		mOffsetY = 0;
	}

	public MapHandleImp(MapHandle handle) {
		this.mHandle = handle;
	}

	public void setAniming(boolean animing) {
		isAniming = animing;
	}

	public boolean isAniming() {
		return isAniming;
	}
	
	public AnimatorSet getAnimatorSet(final AnimatorListener l){
		AnimatorSet set = new AnimatorSet();
		set.addListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
				setAniming(true);
				l.onAnimationStart(animation);
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				setAniming(false);
				l.onAnimationEnd(animation);
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				setAniming(false);
				l.onAnimationCancel(animation);
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
				l.onAnimationRepeat(animation);
			}
		});
		set.setDuration(500);
		set.setInterpolator(new AccelerateDecelerateInterpolator());
		return set;
	}
	
	public AnimatorSet getAnimatorSet(){
		AnimatorSet set = new AnimatorSet();
		set.addListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
				setAniming(true);
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				setAniming(false);
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				setAniming(false);
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				
			}
		});
		set.setDuration(500);
		set.setInterpolator(new AccelerateDecelerateInterpolator());
		return set;
	}

	public void toRotateAnimator(float rotate, PointF mid) {
		RotateWapper wapper = new RotateWapper();
		wapper.mid.set(mid);
		wapper.rotate = rotate;
		ObjectAnimator animator = ObjectAnimator.ofFloat(wapper, "rotate",
				rotate, 0);
		animator.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
				setAniming(true);
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				setAniming(false);
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				setAniming(false);
			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}
		});
		animator.setInterpolator(new AccelerateDecelerateInterpolator());
		animator.setDuration(400);
		animator.start();
	}

	/**
	 * 提交移动动画
	 * 
	 * @param offsetX
	 * @param offsetY
	 * @return
	 */
	public void toTranslateAnimAtor(float offsetX, float offsetY) {
		TranslateViewWapper wapper = new TranslateViewWapper();
		ObjectAnimator animatorTranslateX = ObjectAnimator.ofFloat(wapper,
				"translateX", 0f, offsetX);
		ObjectAnimator animatorTranslateY = ObjectAnimator.ofFloat(wapper,
				"translateY", 0f, offsetY);
		AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
				setAniming(true);
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				setAniming(false);
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				setAniming(false);
			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}
		});
		animatorSet.play(animatorTranslateX).with(animatorTranslateY);
		animatorSet.setDuration(400);
		animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
		animatorSet.start();
	}

	public Animator getRotateAnimator(float rotate, PointF mid) {
		reset();
		RotateWapper wapper = new RotateWapper();
		wapper.mid.set(mid);
		wapper.rotate = rotate;
		wapper.isChangeMatrix = true;
		ObjectAnimator animator = ObjectAnimator.ofFloat(wapper, "rotate",
				rotate, 0);
		return animator;
	}
	
	public Animator getRotateAnimator(float rotate, Float toRotate, PointF mid) {
		reset();
		RotateWapper wapper = new RotateWapper();
		wapper.mid.set(mid);
		wapper.rotate = rotate;
		wapper.isChangeMatrix = true;
		ObjectAnimator animator = ObjectAnimator.ofFloat(wapper, "rotate",
				rotate, toRotate);
		return animator;
	}

	/**
	 * 提交移动动画
	 * 
	 * @param offsetX
	 * @param offsetY
	 * @return
	 */
	public Animator getTranslateAnimAtor(float offsetX, float offsetY) {
		reset();
		TranslateViewWapper wapper = new TranslateViewWapper();
		ObjectAnimator animatorTranslateX = ObjectAnimator.ofFloat(wapper,
				"translateX", 0f, offsetX);
		ObjectAnimator animatorTranslateY = ObjectAnimator.ofFloat(wapper,
				"translateY", 0f, offsetY);
		AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.play(animatorTranslateX).with(animatorTranslateY);
		return animatorSet;
	}

	/**
	 * 执行动画伸缩
	 * 
	 * @param scale
	 * @param mid
	 */
	public Animator getScaleAnimator(float from, float to, PointF mid) {
		reset();
		ScaleViewWapper wapper = new ScaleViewWapper();
		Log.e("scale = ", "var = " + from + ", " + to);
		wapper.midPoint.set(mid);
		wapper.curScale = from;
		wapper.isChangeMatrix = true;
		ObjectAnimator animator = ObjectAnimator.ofFloat(wapper, "scale", from,
				to);
		animator.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
				setAniming(true);
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				setAniming(false);
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				setAniming(false);
			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}
		});
		animator.setInterpolator(new AccelerateDecelerateInterpolator());
		animator.setDuration(400);
		return animator;
	}
	
	/**
	 * 执行动画伸缩
	 * 
	 * @param scale
	 * @param mid
	 */
	public void toScaleAnimator(float from, float to, PointF mid) {
		ScaleViewWapper wapper = new ScaleViewWapper();
		Log.e("scale = ", "var = " + from + ", " + to);
		wapper.midPoint.set(mid);
		wapper.curScale = from;
		ObjectAnimator animator = ObjectAnimator.ofFloat(wapper, "scale", from,
				to);
		animator.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
				setAniming(true);
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				setAniming(false);
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				setAniming(false);
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				
			}
		});
		animator.setInterpolator(new AccelerateDecelerateInterpolator());
		animator.setDuration(400);
		animator.start();
	}

	/**
	 * 伸缩动画封装类
	 */
	private class ScaleViewWapper {
		private final PointF midPoint = new PointF();
		private float curScale = 1f;
		private boolean isChangeMatrix = false;

		public float getScale() {
			return curScale;
		}

		public void setScale(float scale) {
			if (curScale == scale) {
				return;
			}
			if (isChangeMatrix) {
				Matrix matrix = new Matrix();
				matrix.postTranslate(mOffsetX, mOffsetY);
				PointF p = MatrixConverHelper.mapMatrixPoint(matrix, midPoint.x,
						midPoint.y);
				mHandle.postMatrixScale(scale / curScale, p);
			} else {
				mHandle.postMatrixScale(scale / curScale, midPoint);
			}
			mHandle.postImageMatrix();
			curScale = scale;
		}
	}

	/**
	 * 移动动画封装类
	 */
	private class TranslateViewWapper {
		private float lastTransX = 0f;
		private float lastTransY = 0f;

		public void setTranslateX(float offsetX) {
			float dx = offsetX - lastTransX;
			if (dx == 0)
				return;
			mOffsetX = offsetX;
			mHandle.postMatrixTranslate(dx, 0);
			mHandle.postImageMatrix();
			lastTransX = offsetX;
		}

		public void setTranslateY(float offsetY) {
			float dy = offsetY - lastTransY;
			if (dy == 0)
				return;
			mOffsetY = offsetY;
			mHandle.postMatrixTranslate(0, dy);
			mHandle.postImageMatrix();
			lastTransY = offsetY;
		}

		public float getTranslateX() {
			return lastTransX;
		}

		public float getTranslateY() {
			return lastTransY;
		}
	}

	private class RotateWapper {
		private final PointF mid = new PointF();
		private float rotate = 0;
		private boolean isChangeMatrix = false;

		public void setMid(PointF mid) {
			this.mid.set(mid);
		}

		public float getRotate() {
			return rotate;
		}

		public void setRotate(float rotate) {
			float r = this.rotate - rotate;
			if (r == 0) {
				return;
			}
			if (isChangeMatrix) {
				Matrix matrix = new Matrix();
				matrix.postTranslate(mOffsetX, mOffsetY);
				PointF p = MatrixConverHelper.mapMatrixPoint(matrix, mid.x,
						mid.y);
				mHandle.postMatrixRotate(r, p);
			} else {
				mHandle.postMatrixRotate(r, mid);
			}
			mHandle.postImageMatrix();
			this.rotate = rotate;
		}
	}
}
