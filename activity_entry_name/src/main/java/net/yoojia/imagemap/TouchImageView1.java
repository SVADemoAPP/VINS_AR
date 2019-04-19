package net.yoojia.imagemap;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.nineoldandroids.animation.AnimatorSet;

import net.yoojia.imagemap.animator.MapHandle;
import net.yoojia.imagemap.animator.MapHandleImp;
import net.yoojia.imagemap.core.PrruInfoShape;
import net.yoojia.imagemap.core.Shape;
import net.yoojia.imagemap.event.RotateGestureDetector;
import net.yoojia.imagemap.event.RotateGestureDetector.SimpleOnRotateGestureListener;
import net.yoojia.imagemap.util.BaZhanUtil;
import net.yoojia.imagemap.util.ImageViewHelper;

public class TouchImageView1 extends android.support.v7.widget.AppCompatImageView implements OnGlobalLayoutListener, MapHandle {
    private static final float FRICTION = 0.0f;
    private boolean bLongClick;
    private GestureMode curGestureMode;
    private PointF downPoint;
    private float dragVelocity;
    private final Matrix imageUsingMatrix;
    private boolean isAllowRotate;
    protected boolean isAllowTranslate;
    private boolean isNewImageSVG;
    private float lastOffsetX;
    private float lastOffsetY;
    private Shape longClickShape;
    private BaZhanUtil mBaZhanUtil;
    private GestureDetector mGestureDetector;
    private final ImageViewHelper mImageViewHelper;
    protected MapHandleImp mMapHandle;
    private OnLongClickListener1 mOnLongClickListener;
    private OnRotateListener mOnRotateListener;
    private RotateGestureDetector mRotateGestureDetector;
    private ScaleGestureDetector mScaleDetector;
    public OnSingleClickListener mSingleClickListener;
    protected int mView_height;
    protected int mView_width;
    private final float[] matrixValues;
    private PointF mid;
    private MyOnTouchListener myOnTouchListener;
    private float saveRotate;
    private OnTouchListener touchListener;
    private PrruModifyListener pRUUlistener;
    private MoveAllRsRpListener mMoveAllRsRpListener;

    private boolean tranlateFlag = true;
    public  void setTranlateFlag(boolean flag){
        tranlateFlag=flag;
    }

    public void setMoveAllRsRpListener(MoveAllRsRpListener moveAllRsRpListener) {
        mMoveAllRsRpListener = moveAllRsRpListener;
    }

    public void setPRRUMoveListener(PrruModifyListener moveListener) {
        pRUUlistener = moveListener;
    }

    private enum GestureMode {
        toMove,
        doubleTap,
        drag,
        rotate_scale,
        none
    }

    private static class MyBitmapDrawable extends BitmapDrawable {
        public MyBitmapDrawable(Resources res, Bitmap bitmap) {
            super(res, bitmap);
        }
    }

    private class MyGestureDetectorListener extends SimpleOnGestureListener {
        private MyGestureDetectorListener() {
        }

        /* synthetic */ MyGestureDetectorListener(TouchImageView1 touchImageView1, MyGestureDetectorListener myGestureDetectorListener) {
            this();
        }

        public boolean onDoubleTap(MotionEvent e) {
            if (TouchImageView1.this.mImageViewHelper.havePullScale) {
                float centerX = e.getX();
                float centerY = e.getY();
                if (TouchImageView1.this.mImageViewHelper.pointInArea(TouchImageView1.this.imageUsingMatrix, TouchImageView1.this.saveRotate, new PointF(centerX, centerY))) {
                    TouchImageView1.this.mid.set(centerX, centerY);
                    TouchImageView1.this.pullScale(TouchImageView1.this.mid);
                }
            }
            return true;
        }

        public boolean onSingleTapUp(MotionEvent e) {
            PointF cur = new PointF(e.getX(), e.getY());
            if (TouchImageView1.this.mImageViewHelper.pointInArea(TouchImageView1.this.imageUsingMatrix, TouchImageView1.this.saveRotate, cur)) {
                PointF point = TouchImageView1.this.mImageViewHelper.getSinglePoint(TouchImageView1.this.imageUsingMatrix, TouchImageView1.this.saveRotate, cur);
                if (TouchImageView1.this.mSingleClickListener != null) {
                    TouchImageView1.this.mSingleClickListener.onSingle(point);
                }
                TouchImageView1.this.onViewClick(point.x, point.y);
            }
            return true;
        }

        public void onLongPress(MotionEvent e) {
            if (TouchImageView1.this.mImageViewHelper.pointInArea(TouchImageView1.this.imageUsingMatrix, TouchImageView1.this.saveRotate, new PointF(e.getX(), e.getY())) && TouchImageView1.this.mOnLongClickListener != null) {
                TouchImageView1.this.bLongClick = true;
                TouchImageView1.this.longClickShape = TouchImageView1.this.onViewLongClickShape(TouchImageView1.this.downPoint.x, TouchImageView1.this.downPoint.y);
            }
        }
    }

    public interface MyOnTouchListener {
        void onTouch(boolean z);
    }

    public interface OnLongClickListener1 {
        void onLongClick(Shape shape);
    }

    public interface OnRotateListener {
        void onRotate(float f);
    }

    public interface OnSingleClickListener {
        void onSingle(PointF pointF);
    }

    private class ScaleListener extends SimpleOnScaleGestureListener {
        private ScaleListener() {
        }

        /* synthetic */ ScaleListener(TouchImageView1 touchImageView1, ScaleListener scaleListener) {
            this();
        }

        public boolean onScale(ScaleGestureDetector detector) {
            TouchImageView1.this.postMatrixScale(detector.getScaleFactor(), TouchImageView1.this.mid);
            return true;
        }
    }

    private class MyRotateGesture extends SimpleOnRotateGestureListener {
        private MyRotateGesture() {
        }

        /* synthetic */ MyRotateGesture(TouchImageView1 touchImageView1, MyRotateGesture myRotateGesture) {
            this();
        }

        public boolean onRotate(RotateGestureDetector detector) {
            float toRotate = detector.getRotationDegreesDelta();
            if (toRotate > 360.0f || toRotate < -360.0f) {
                toRotate %= 360.0f;
            }
            if (toRotate == 0.0f) {
                return false;
            }
            TouchImageView1.this.postMatrixRotate(-toRotate, new PointF(TouchImageView1.this.mid.x, TouchImageView1.this.mid.y));
            return true;
        }
    }

    public TouchImageView1(Context context) {
        this(context, null);
    }

    public TouchImageView1(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.imageUsingMatrix = new Matrix();
        this.mImageViewHelper = new ImageViewHelper();
        this.matrixValues = new float[9];
        this.mid = new PointF();
        this.isAllowRotate = true;
        this.isAllowTranslate = false;
        this.isNewImageSVG = true;
        this.curGestureMode = GestureMode.none;
        this.touchListener = new OnTouchListener() {
            private PointF singlePoint;
            private PointF rlPoint;
            static final float MAX_VELOCITY = 1.2f;
            private PointF curPoint = new PointF();
            private long dragTime;
            private boolean isTouch;
            private long lastDragTime;
            private PointF mDownPoint;
            private int mLastPointerCount = -1;
            private PointF mMovePoint = new PointF();
            private PointF point1;
            long timeTag = 0;

            public boolean onTouch(View v, MotionEvent event) {
                int pointerCount = event.getPointerCount();
                if (pointerCount > 2) {
                    return false;
                }
                if (pointerCount != this.mLastPointerCount) {
                    if (pointerCount == 1) {
                        this.mDownPoint = new PointF(event.getX(), event.getY());
                        TouchImageView1.this.curGestureMode = GestureMode.drag;
                        this.lastDragTime = System.currentTimeMillis();
                    } else {
                        TouchImageView1.this.curGestureMode = GestureMode.rotate_scale;
                    }
                    this.mLastPointerCount = pointerCount;
                }
                if (TouchImageView1.this.curGestureMode == GestureMode.rotate_scale) {
                    TouchImageView1.this.midPoint(TouchImageView1.this.mid, event);
                }
                TouchImageView1.this.mScaleDetector.onTouchEvent(event);
                TouchImageView1.this.mGestureDetector.onTouchEvent(event);
                if (TouchImageView1.this.isAllowRotate) {
                    TouchImageView1.this.mRotateGestureDetector.onTouchEvent(event);
                }
                switch (event.getAction() & 255) {
                    case MotionEvent.ACTION_DOWN:
                        this.mDownPoint = new PointF(event.getX(), event.getY());
                        TouchImageView1.this.downPoint = TouchImageView1.this.mImageViewHelper.getSinglePoint(TouchImageView1.this.imageUsingMatrix, TouchImageView1.this.saveRotate, this.mDownPoint);
                        TouchImageView1.this.bLongClick = false;
                        if (TouchImageView1.this.myOnTouchListener != null) {
                            TouchImageView1.this.myOnTouchListener.onTouch(true);
                            break;
                        }
                        rlPoint = new PointF(event.getX(), event.getY());
                        singlePoint = mImageViewHelper.getSinglePoint(imageUsingMatrix, saveRotate, rlPoint);
                        if (pRUUlistener != null) {
                            if (mImageViewHelper.pointInArea(imageUsingMatrix, saveRotate, rlPoint)) {

                                pRUUlistener.startTranslate(singlePoint.x, singlePoint.y);

                            }
                            pRUUlistener.touchOutside(singlePoint.x, singlePoint.y);
                        }

                        if (mMoveAllRsRpListener != null) {
                            mMoveAllRsRpListener.startTranslate(singlePoint.x, singlePoint.y); //xhf
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        rlPoint = new PointF(event.getX(), event.getY());
                        singlePoint = mImageViewHelper.getSinglePoint(imageUsingMatrix, saveRotate, rlPoint);

                        if (mMoveAllRsRpListener != null) {
                            mMoveAllRsRpListener.endTranslate(singlePoint.x, singlePoint.y);
                        }

                        if (pRUUlistener != null) {
                            if (mImageViewHelper.pointInArea(imageUsingMatrix, saveRotate, rlPoint)) {

                                pRUUlistener.endTranslate(singlePoint.x, singlePoint.y); //xhf 获取长按移动xy
                            }
                        }
                        if (TouchImageView1.this.mOnLongClickListener != null && TouchImageView1.this.bLongClick) {
                            TouchImageView1.this.mOnLongClickListener.onLongClick(TouchImageView1.this.longClickShape);
                        }
                        if (TouchImageView1.this.myOnTouchListener != null) {
                            TouchImageView1.this.myOnTouchListener.onTouch(false);
                            break;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:

                        if (TouchImageView1.this.curGestureMode == GestureMode.drag) {
                            this.mMovePoint.x = event.getX();
                            this.mMovePoint.y = event.getY();
                            if (Math.abs(this.mMovePoint.x - this.mDownPoint.x) > 8.0f || Math.abs(this.mMovePoint.y - this.mDownPoint.y) > 8.0f) {
                                this.dragTime = System.currentTimeMillis();
                                this.timeTag = this.dragTime - this.lastDragTime;
                                if (this.timeTag == 0) {
                                    this.timeTag = 100;
                                }
                                TouchImageView1.this.dragVelocity = new Float((TouchImageView1.this.distanceBetween(this.mMovePoint, this.mDownPoint) / ((double) this.timeTag)) * 0.0d).floatValue();
                                TouchImageView1.this.dragVelocity = Math.min(MAX_VELOCITY, TouchImageView1.this.dragVelocity);
                                this.lastDragTime = this.dragTime;
                                TouchImageView1.this.lastOffsetX = this.mMovePoint.x - this.mDownPoint.x;
                                TouchImageView1.this.lastOffsetY = this.mMovePoint.y - this.mDownPoint.y;
                                rlPoint = new PointF(event.getX(), event.getY());
                                singlePoint = mImageViewHelper.getSinglePoint(imageUsingMatrix, saveRotate, rlPoint);

                                //xhf
                                if (mMoveAllRsRpListener != null) {
                                    mMoveAllRsRpListener.moveTranslate(singlePoint.x, singlePoint.y);
                                }

                                if (TouchImageView1.this.bLongClick) {
                                    this.curPoint.x = event.getX();
                                    this.curPoint.y = event.getY();
                                    if (pRUUlistener != null) {
                                        if (mImageViewHelper.pointInArea(imageUsingMatrix, saveRotate, rlPoint)) {
                                            pRUUlistener.moveTranslate(singlePoint.x, singlePoint.y); //xhf 获取长按移动xy
                                        }
                                    }
                                    if (TouchImageView1.this.mImageViewHelper.pointInArea(TouchImageView1.this.imageUsingMatrix, TouchImageView1.this.saveRotate, this.curPoint)) {
                                        this.point1 = TouchImageView1.this.mImageViewHelper.getSinglePoint(TouchImageView1.this.imageUsingMatrix, TouchImageView1.this.saveRotate, this.curPoint);
                                        TouchImageView1.this.onViewLongClickShapeMove(this.point1.x, this.point1.y);
                                    }
                                } else {
                                    if (tranlateFlag) { //是否可以移动
                                        TouchImageView1.this.postMatrixTranslate(TouchImageView1.this.lastOffsetX, TouchImageView1.this.lastOffsetY);
                                    }
                                }
                                this.mDownPoint.set(this.mMovePoint);
                                break;
                            }
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_POINTER_UP:
                        if (TouchImageView1.this.curGestureMode == GestureMode.drag) {
                            TouchImageView1.this.curGestureMode = GestureMode.toMove;
                            TouchImageView1.this.inertiaMove();
                        } else {
                            TouchImageView1.this.curGestureMode = GestureMode.none;
                        }
                        this.mLastPointerCount = -1;
                        break;
                    case 5:
                        TouchImageView1.this.midPoint(TouchImageView1.this.mid, event);
                        break;
                }
                TouchImageView1.this.postImageMatrix();
                return true;
            }
        };
        this.saveRotate = 0.0f;
        initialized();
    }

    protected void initialized() {
        super.setClickable(true);
        setLayerType(1, null);
        setScaleType(ScaleType.MATRIX);
        setImageMatrix(this.imageUsingMatrix);
        setOnTouchListener(this.touchListener);
        this.mMapHandle = new MapHandleImp(this);
        this.mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        this.mRotateGestureDetector = new RotateGestureDetector(getContext(), new MyRotateGesture());
        this.mGestureDetector = new GestureDetector(getContext(), new MyGestureDetectorListener());
    }

    public void pullScale() {
        if (!this.mMapHandle.isAniming() && this.mImageViewHelper.havePullScale) {
            this.mMapHandle.toScaleAnimator(getCurrentScale(), getCurrentScale() * 1.6f, new PointF((float) (this.mView_width / 2), (float) (this.mView_height / 2)));
        }
    }

    public void pullScale(PointF mid) {
        if (!this.mMapHandle.isAniming() && this.mImageViewHelper.havePullScale) {
            float offsetX = (((float) this.mView_width) / 2.0f) - mid.x;
            float offsetY = (((float) this.mView_height) / 2.0f) - mid.y;
            if (offsetX < -8.0f || offsetX > 8.0f || offsetY < -8.0f || offsetY > 8.0f) {
                AnimatorSet set = this.mMapHandle.getAnimatorSet();
                set.play(this.mMapHandle.getTranslateAnimAtor(offsetX, offsetY)).with(this.mMapHandle.getScaleAnimator(getCurrentScale(), getCurrentScale() * 1.6f, mid));
                set.start();
                return;
            }
            postMatrixTranslate(offsetX, offsetY);
            postImageMatrix();
            this.mMapHandle.toScaleAnimator(getCurrentScale(), getCurrentScale() * 1.6f, mid);
        }
    }

    public void zoomScale() {
        if (!this.mMapHandle.isAniming() && this.mImageViewHelper.haveZoomScale) {
            this.mMapHandle.toScaleAnimator(getCurrentScale(), getCurrentScale() * 0.625f, this.mImageViewHelper.getCenterPoint(this.imageUsingMatrix));
        }
    }

    private void rotateToZero() {
        if (!this.mMapHandle.isAniming()) {
            PointF center = this.mImageViewHelper.getCenterPoint(this.imageUsingMatrix);
            float offsetX = (((float) this.mView_width) / 2.0f) - center.x;
            float offsetY = (((float) this.mView_height) / 2.0f) - center.y;
            AnimatorSet animatorSet = this.mMapHandle.getAnimatorSet();
            animatorSet.play(this.mMapHandle.getTranslateAnimAtor(offsetX, offsetY)).with(this.mMapHandle.getScaleAnimator(getScale(), this.mBaZhanUtil.initScale, center)).with(this.mMapHandle.getRotateAnimator(this.saveRotate, Float.valueOf(this.mBaZhanUtil.initAngle), center));
            animatorSet.start();
        }
    }

    public void releaseImageShow() {
        rotateToZero();
    }

    public void translateOffset(float offsetX, float offsetY) {
        this.mMapHandle.toTranslateAnimAtor(offsetX, offsetY);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    public void onGlobalLayout() {
        this.mView_width = getWidth();
        this.mView_height = getHeight();
        this.mImageViewHelper.setViewSize(this.mView_width, this.mView_height);
        Drawable d = getDrawable();
        if (d != null && this.isNewImageSVG) {
            this.isNewImageSVG = false;
            this.saveRotate = 0.0f;
            if (this.mOnRotateListener != null) {
                this.mOnRotateListener.onRotate(this.saveRotate);
            }
            int dw = d.getIntrinsicWidth();
            int dh = d.getIntrinsicHeight();

            this.mImageViewHelper.setDrawableSize(dw, d.getIntrinsicHeight());
            this.mImageViewHelper.initImageMatrix(this.imageUsingMatrix);

            //这里涉及到缩放问题 。图片的宽高和容器的宽高之间的处理
            //--------------------------xhf 处理图片和容器适配问题---------------------------------
            float scale = 0;
            float c1 = dh * 1f / dw;
            float c2 = mView_height * 1f / mView_width;
            if (c2 > c1) {  //如果容器比例大于图片   相等其实哪个都可以
                if (mView_width < mView_height) {
                    scale = mView_width * 1f / dw;
                } else {
                    scale = mView_height * 1f / dh;
                }
            } else {
                scale = mView_height * 1f / dh;
            }
            //--------------------------xhf 处理图片和容器适配问题---------------------------------
            this.mBaZhanUtil = new BaZhanUtil(0.0f, scale);
            PointF mid = new PointF(((float) this.mView_width) / 2.0f, ((float) this.mView_height) / 2.0f);
            postMatrixScale(this.mBaZhanUtil.initScale / getScale(), mid);
            postMatrixRotate(this.mBaZhanUtil.initAngle, mid);
            this.saveRotate -= this.mBaZhanUtil.initAngle;
            this.mImageViewHelper.haveZoomScale = true;
            if (this.mImageViewHelper.mOnMinZoomCallback != null) {
                this.mImageViewHelper.mOnMinZoomCallback.onMinZoom(true);
            }
            postImageMatrix();
        }
    }

    public boolean isAllowTranslate() {
        return this.isAllowTranslate;
    }

    public void setAllowTranslate(boolean isAllowTranslate) {
        this.isAllowTranslate = isAllowTranslate;
    }

    public boolean isAllowRotate() {
        return this.isAllowRotate;
    }

    public void setAllowRotate(boolean isAllowRotate) {
        this.isAllowRotate = isAllowRotate;
        if (!isAllowRotate && this.saveRotate != 0.0f) {
            rotateToZero();
        }
    }

    public float getScale() {
        return this.mImageViewHelper.getCurrentScale(this.imageUsingMatrix);
    }

    public ImageViewHelper getHelper() {
        return this.mImageViewHelper;
    }

    public boolean onViewClick(float xOnView, float yOnView) {
        return false;
    }

    protected Shape onViewLongClickShape(float xOnView, float yOnView) {
        return null;
    }

    protected boolean onViewLongClickShapeMove(float xOnView, float yOnView) {
        return false;
    }

    public float getLastOffsetX() {
        return this.lastOffsetX;
    }

    public void setLastOffsetX(float lastOffsetX) {
        this.lastOffsetX = lastOffsetX;
    }

    public float getLastOffsetY() {
        return this.lastOffsetY;
    }

    public void setLastOffsetY(float lastOffsetY) {
        this.lastOffsetY = lastOffsetY;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.curGestureMode == GestureMode.toMove) {
            inertiaMove();
        }
    }

    public PointF getCenterByImage() {
        return this.mImageViewHelper.getSinglePoint(this.imageUsingMatrix, this.saveRotate, new PointF(((float) this.mView_width) / 2.0f, ((float) this.mView_height) / 2.0f));
    }

    protected void inertiaMove() {
        float deltaX = this.lastOffsetX * this.dragVelocity;
        float deltaY = this.lastOffsetY * this.dragVelocity;
        this.dragVelocity *= 0.0f;
        if (((double) Math.abs(deltaX)) >= 0.01d || ((double) Math.abs(deltaY)) >= 0.01d) {
            postMatrixTranslate(deltaX, deltaY);
            postImageMatrix();
            return;
        }
        this.curGestureMode = GestureMode.none;
    }

    public void setImageDrawable(Drawable drawable) {
        if (getDrawable() != drawable) {
            this.imageUsingMatrix.reset();
            this.isNewImageSVG = true;
            super.setImageDrawable(drawable);
        }
    }

    public void setImageBitmap(Bitmap bm) {
        setImageDrawable(new MyBitmapDrawable(getResources(), bm));
    }

    private double distanceBetween(PointF left, PointF right) {
        return Math.sqrt(Math.pow((double) (left.x - right.x), 2.0d) + Math.pow((double) (left.y - right.y), 2.0d));
    }

    private void midPoint(PointF point, MotionEvent event) {
        point.set((event.getX(0) + event.getX(1)) / 2.0f, (event.getY(0) + event.getY(1)) / 2.0f);
    }

    private float getCurrentScale() {
        this.imageUsingMatrix.getValues(this.matrixValues);
        return this.matrixValues[0];
    }

    public void postMatrixRotate(float rotate, PointF midPoint) {
        this.saveRotate -= rotate;
        if (this.saveRotate > 360.0f || this.saveRotate < -360.0f) {
            this.saveRotate %= 360.0f;
        }
        Log.e(" saveRotate ", new StringBuilder(String.valueOf(this.saveRotate)).toString());
        if (this.mOnRotateListener != null) {
            this.mOnRotateListener.onRotate(this.saveRotate);
        }
        Log.e("rotate", new StringBuilder(String.valueOf(rotate)).append("_").append(midPoint.toString()).toString());
        this.imageUsingMatrix.postRotate(rotate, midPoint.x, midPoint.y);
    }

    public void postMatrixScale(float scale, PointF midpPoint) {
        if (scale < 1.0f && !this.mImageViewHelper.haveZoomScale) {
            return;
        }
        if (scale <= 1.0f || this.mImageViewHelper.havePullScale) {
            Log.e("scale", new StringBuilder(String.valueOf(scale)).append("_").append(midpPoint.toString()).toString());
            this.imageUsingMatrix.postScale(scale, scale, midpPoint.x, midpPoint.y);
        }
    }

    public final void postImageMatrix() {
        checkScale();
        checkTranslate();
        setImageMatrix(this.imageUsingMatrix);
    }

    public void postMatrixTranslate(float deltaX, float deltaY) {
        if (deltaX != 0.0f || deltaY != 0.0f) {
            this.imageUsingMatrix.postTranslate(deltaX, deltaY);
        }
    }

    public void checkScale() {
        this.mImageViewHelper.checkScale(this.imageUsingMatrix, this.mid);
    }

    public void checkTranslate() {
        this.mImageViewHelper.checkTranslate(this.imageUsingMatrix);
    }

    public void setOnSingleClickListener(OnSingleClickListener listener) {
        this.mSingleClickListener = listener;
    }

    public void setOnRotateListener(OnRotateListener listener) {
        this.mOnRotateListener = listener;
    }

    public void setOnMyTouchListener(MyOnTouchListener listener) {
        this.myOnTouchListener = listener;
    }

    public void setOnLongClickListener(OnLongClickListener1 listener) {
        this.mOnLongClickListener = listener;
    }

    public void setOnTouchListener(MyOnTouchListener listener) {
        this.myOnTouchListener = listener;
    }

    public interface PrruModifyListener {
        void startTranslate(float x, float y);

        void moveTranslate(float x, float y);

        void endTranslate(float x, float y);

        void touchOutside(float x, float y);

    }

    /***
     * 移动所有的rsrp点
     */
    public interface MoveAllRsRpListener {
        void startTranslate(float x, float y);

        void moveTranslate(float x, float y);

        void endTranslate(float x, float y);
    }
}
