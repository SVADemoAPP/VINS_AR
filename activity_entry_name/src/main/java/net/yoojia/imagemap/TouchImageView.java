package net.yoojia.imagemap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import net.yoojia.imagemap.event.RotateGestureDetector;
import net.yoojia.imagemap.event.RotateGestureDetector.OnRotateGestureListener;

public class TouchImageView extends android.support.v7.widget.AppCompatImageView {
    private static final int DRAG = 1;
    private static final float FRICTION = 0.6f;
    private static final int NONE = 0;
    private static final int ZOOM = 2;
    private float absoluteOffsetX;
    private float absoluteOffsetY;
    private boolean bb;
    private float bmHeight;
    private float bmWidth;
    private float bottom;
    private final Matrix imageSavedMatrix;
    private final Matrix imageUsingMatrix;
    private boolean isAllowRotate;
    private PointF last;
    private PointF lastDelta;
    private long lastDragTime;
    private Context mContext;
    private RotateGestureDetector mRotateGestureDetector;
    private ScaleGestureDetector mScaleDetector;
    private float[] matrixValues;
    private float maxScale;
    private PointF mid;
    private float minScale;
    private int mode;
    private float oldDist;
    public boolean onBottomSide;
    public boolean onLeftSide;
    public boolean onRightSide;
    public boolean onTopSide;
    private float origHeight;
    private float origWidth;
    private final Matrix overLayerMatrix;
    private float redundantXSpace;
    private float redundantYSpace;
    private float right;
    private float saveRotate;
    private float saveScale;
    private float scale;
    private PointF start;
    private OnTouchListener touchListener;
    private float velocity;
    private float viewHeight;
    private float viewWidth;
    private float x;
    private float y;

    private class ScaleListener extends SimpleOnScaleGestureListener {
        private ScaleListener() {
        }

        /* synthetic */ ScaleListener(TouchImageView touchImageView, ScaleListener scaleListener) {
            this();
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            TouchImageView.this.mode = 2;
            return true;
        }

        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            Log.i("scaleFactor", new StringBuilder(String.valueOf(scaleFactor)).toString());
            TouchImageView.this.scale = scaleFactor;
            TouchImageView.this.postScaleToImage(scaleFactor, detector.getFocusX(), detector.getFocusY());
            return true;
        }
    }

    private class MyRotateGesture implements OnRotateGestureListener {
        private MyRotateGesture() {
        }

        /* synthetic */ MyRotateGesture(TouchImageView touchImageView, MyRotateGesture myRotateGesture) {
            this();
        }

        public boolean onRotate(RotateGestureDetector detector) {
            float toRotate = detector.getRotationDegreesDelta();
            if (toRotate == 0.0f) {
                return false;
            }
            float rotate = -toRotate;
            TouchImageView touchImageView = TouchImageView.this;
            touchImageView.saveRotate = touchImageView.saveRotate - rotate;
            if (TouchImageView.this.saveRotate > 360.0f || TouchImageView.this.saveRotate < -360.0f) {
                touchImageView = TouchImageView.this;
                touchImageView.saveRotate = touchImageView.saveRotate % 360.0f;
            }
            TouchImageView.this.postMatrixRotate(rotate, new PointF(TouchImageView.this.mid.x, TouchImageView.this.mid.y));
            return true;
        }

        public boolean onRotateBegin(RotateGestureDetector detector) {
            TouchImageView.this.mode = 2;
            return true;
        }

        public void onRotateEnd(RotateGestureDetector detector) {
        }
    }

    public TouchImageView(Context context) {
        this(context, null);
    }

    public TouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.imageUsingMatrix = new Matrix();
        this.imageSavedMatrix = new Matrix();
        this.overLayerMatrix = new Matrix();
        this.mode = 0;
        this.last = new PointF();
        this.mid = new PointF();
        this.start = new PointF();
        this.saveScale = 1.0f;
        this.minScale = 0.5f;
        this.maxScale = 10.0f;
        this.oldDist = 1.0f;
        this.lastDelta = new PointF(0.0f, 0.0f);
        this.velocity = 0.0f;
        this.lastDragTime = 0;
        this.onLeftSide = false;
        this.onTopSide = false;
        this.onRightSide = false;
        this.onBottomSide = false;
        this.x = 0.0f;
        this.y = 0.0f;
        this.scale = 0.0f;
        this.isAllowRotate = true;
        this.touchListener = new OnTouchListener() {
            static final float MAX_VELOCITY = 1.2f;
            int a = 0;
            private long dragTime;
            private float dragVelocity;
            private float eventX = 0.0f;
            private float eventY = 0.0f;

            public boolean onTouch(View v, MotionEvent event) {
                TouchImageView.this.mScaleDetector.onTouchEvent(event);
                if (TouchImageView.this.isAllowRotate) {
                    TouchImageView.this.mRotateGestureDetector.onTouchEvent(event);
                }
                TouchImageView.this.fillAbsoluteOffset();
                PointF curr = new PointF(event.getX(), event.getY());
                switch (event.getAction() & 255) {
                    case MotionEvent.ACTION_DOWN:
                        Log.i("down", "down");
                        this.eventX = event.getX();
                        this.eventY = event.getY();
                        TouchImageView.this.fillAbsoluteOffset();
                        if (TouchImageView.this.absoluteOffsetX > 0.0f && TouchImageView.this.absoluteOffsetY > 0.0f) {
                            if (event.getX() <= TouchImageView.this.absoluteOffsetX) {
                                TouchImageView.this.x = 0.0f;
                            } else if (event.getX() - TouchImageView.this.absoluteOffsetX > TouchImageView.this.bmWidth * TouchImageView.this.getScale()) {
                                TouchImageView.this.x = TouchImageView.this.bmWidth * TouchImageView.this.getScale();
                            } else {
                                TouchImageView.this.x = event.getX() - TouchImageView.this.absoluteOffsetX;
                            }
                            if (event.getY() <= TouchImageView.this.absoluteOffsetY) {
                                TouchImageView.this.y = 0.0f;
                            } else if (event.getY() - TouchImageView.this.absoluteOffsetY > TouchImageView.this.bmHeight * TouchImageView.this.getScale()) {
                                TouchImageView.this.y = TouchImageView.this.bmHeight * TouchImageView.this.getScale();
                            } else {
                                TouchImageView.this.y = event.getY() - TouchImageView.this.absoluteOffsetY;
                            }
                        } else if (TouchImageView.this.absoluteOffsetX > 0.0f && TouchImageView.this.absoluteOffsetY < 0.0f) {
                            if (event.getX() <= TouchImageView.this.absoluteOffsetX) {
                                TouchImageView.this.x = 0.0f;
                            } else if (event.getX() - TouchImageView.this.absoluteOffsetX > TouchImageView.this.bmWidth * TouchImageView.this.getScale()) {
                                TouchImageView.this.x = TouchImageView.this.bmWidth * TouchImageView.this.getScale();
                            } else {
                                TouchImageView.this.x = event.getX() - TouchImageView.this.absoluteOffsetX;
                            }
                            if (event.getY() + Math.abs(TouchImageView.this.absoluteOffsetY) > TouchImageView.this.bmHeight * TouchImageView.this.getScale()) {
                                TouchImageView.this.y = TouchImageView.this.bmHeight * TouchImageView.this.getScale();
                            } else {
                                TouchImageView.this.y = event.getY() + Math.abs(TouchImageView.this.absoluteOffsetY);
                            }
                        } else if (TouchImageView.this.absoluteOffsetX >= 0.0f || TouchImageView.this.absoluteOffsetY <= 0.0f) {
                            if (event.getX() + Math.abs(TouchImageView.this.absoluteOffsetX) > TouchImageView.this.bmWidth * TouchImageView.this.getScale()) {
                                TouchImageView.this.x = TouchImageView.this.bmWidth * TouchImageView.this.getScale();
                            } else {
                                TouchImageView.this.x = event.getX() + Math.abs(TouchImageView.this.absoluteOffsetX);
                            }
                            if (event.getY() + Math.abs(TouchImageView.this.absoluteOffsetY) > TouchImageView.this.bmHeight * TouchImageView.this.getScale()) {
                                TouchImageView.this.y = TouchImageView.this.bmHeight * TouchImageView.this.getScale();
                            } else {
                                TouchImageView.this.y = event.getY() + Math.abs(TouchImageView.this.absoluteOffsetY);
                            }
                        } else {
                            if (event.getX() + Math.abs(TouchImageView.this.absoluteOffsetX) > TouchImageView.this.bmWidth * TouchImageView.this.getScale()) {
                                TouchImageView.this.x = TouchImageView.this.bmWidth * TouchImageView.this.getScale();
                            } else {
                                TouchImageView.this.x = event.getX() + Math.abs(TouchImageView.this.absoluteOffsetX);
                            }
                            if (event.getY() <= TouchImageView.this.absoluteOffsetY) {
                                TouchImageView.this.y = 0.0f;
                            } else if (event.getY() - TouchImageView.this.absoluteOffsetY > TouchImageView.this.bmHeight * TouchImageView.this.getScale()) {
                                TouchImageView.this.y = TouchImageView.this.bmHeight * TouchImageView.this.getScale();
                            } else {
                                TouchImageView.this.y = event.getY() - TouchImageView.this.absoluteOffsetY;
                            }
                        }
                        TouchImageView.this.mode = 1;
                        this.a = 1;
                        TouchImageView.this.imageSavedMatrix.set(TouchImageView.this.imageUsingMatrix);
                        TouchImageView.this.last.set(event.getX(), event.getY());
                        TouchImageView.this.start.set(TouchImageView.this.last);
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.i("up", "up");
                        if (TouchImageView.this.mode == 1) {
                            TouchImageView.this.velocity = this.dragVelocity;
                        }
                        TouchImageView.this.mode = 0;
                        if (this.a == 1 && !TouchImageView.this.bb) {
                            TouchImageView.this.onViewClick(event.getX(0), event.getY(0));
                            break;
                        }
                    case MotionEvent.ACTION_MOVE:
                        Log.i("move", "move");
                        if (TouchImageView.this.mode == 1) {
                            this.dragTime = System.currentTimeMillis();
                            this.dragVelocity = new Float((TouchImageView.this.distanceBetween(curr, TouchImageView.this.last) / ((double) (this.dragTime - TouchImageView.this.lastDragTime))) * 0.6000000238418579d).floatValue();
                            this.dragVelocity = Math.min(MAX_VELOCITY, this.dragVelocity);
                            TouchImageView.this.lastDragTime = this.dragTime;
                            float deltaX = curr.x - TouchImageView.this.last.x;
                            float deltaY = curr.y - TouchImageView.this.last.y;
                            TouchImageView.this.checkAndSetTranslate(deltaX, deltaY);
                            TouchImageView.this.lastDelta.set(deltaX, deltaY);
                            TouchImageView.this.last.set(curr.x, curr.y);
                            if (!(this.eventX == event.getX() && this.eventY == event.getY())) {
                                this.a = 0;
                                break;
                            }
                        }
                        break;
                    case 5:
                        TouchImageView.this.oldDist = TouchImageView.this.spacing(event);
                        TouchImageView.this.midPoint(TouchImageView.this.mid, event);
                        if (TouchImageView.this.oldDist > 10.0f) {
                            TouchImageView.this.imageSavedMatrix.set(TouchImageView.this.imageUsingMatrix);
                            TouchImageView.this.mode = 2;
                            TouchImageView.this.getMesure();
                            break;
                        }
                        break;
                    case 6:
                        TouchImageView.this.mode = 0;
                        TouchImageView.this.velocity = 0.0f;
                        TouchImageView.this.imageSavedMatrix.set(TouchImageView.this.imageUsingMatrix);
                        TouchImageView.this.oldDist = TouchImageView.this.spacing(event);
                        break;
                }
                TouchImageView.this.setImageMatrix(TouchImageView.this.imageUsingMatrix);
                TouchImageView.this.invalidate();
                return false;
            }
        };
        super.setClickable(true);
        this.mContext = context;
        setLayerType(1, null);
        initialized();
    }

    public boolean isAllowRotate() {
        return this.isAllowRotate;
    }

    public void setAllowRotate(boolean isAllowRotate) {
        this.isAllowRotate = isAllowRotate;
    }

    public PointF getPoints() {
        return new PointF(this.x, this.y);
    }

    public void setBoolean(boolean b) {
        this.bb = b;
    }

    public float getMesure() {
        return this.scale;
    }

    protected void initialized() {
        this.matrixValues = new float[9];
        setScaleType(ScaleType.MATRIX);
        setImageMatrix(this.imageUsingMatrix);
        this.mScaleDetector = new ScaleGestureDetector(this.mContext, new ScaleListener());
        setOnTouchListener(this.touchListener);
        float scale = this.saveScale;
        this.saveScale = 1.0f;
        this.imageUsingMatrix.setScale(scale, scale);
        this.overLayerMatrix.setScale(scale, scale);
        this.mRotateGestureDetector = new RotateGestureDetector(getContext(), new MyRotateGesture());
    }

    protected void onViewClick(float xOnView, float yOnView) {
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        scrolling();
    }

    public PointF getAbsoluteCenter() {
        fillAbsoluteOffset();
        if (this.absoluteOffsetY >= 0.0f && this.absoluteOffsetX <= 0.0f) {
            return new PointF(Math.abs(this.absoluteOffsetX) + (this.viewWidth / 2.0f), (this.viewHeight / 2.0f) - Math.abs(this.absoluteOffsetY));
        }
        if (this.absoluteOffsetY >= 0.0f && this.absoluteOffsetX > 0.0f) {
            return new PointF((this.viewWidth / 2.0f) - Math.abs(this.absoluteOffsetX), (this.viewHeight / 2.0f) - Math.abs(this.absoluteOffsetY));
        }
        if (this.absoluteOffsetY >= 0.0f || this.absoluteOffsetX <= 0.0f) {
            return new PointF(Math.abs(this.absoluteOffsetX) + (this.viewWidth / 2.0f), Math.abs(this.absoluteOffsetY) + (this.viewHeight / 2.0f));
        }
        return new PointF((this.viewWidth / 2.0f) - Math.abs(this.absoluteOffsetX), Math.abs(this.absoluteOffsetY) + (this.viewHeight / 2.0f));
    }

    public void moveBy(float deltaX, float deltaY) {
        checkAndSetTranslate(deltaX, deltaY);
        setImageMatrix(this.imageUsingMatrix);
    }

    private void scrolling() {
        float deltaX = this.lastDelta.x * this.velocity;
        float deltaY = this.lastDelta.y * this.velocity;
        if (deltaX <= this.viewWidth && deltaY <= this.viewHeight) {
            if (((double) Math.abs(deltaX)) >= 0.1d || ((double) Math.abs(deltaY)) >= 0.1d) {
                moveBy(deltaX, deltaY);
            }
        }
    }

    protected void postTranslate(float deltaX, float deltaY) {
        this.imageUsingMatrix.postTranslate(deltaX, deltaY);
        this.overLayerMatrix.postTranslate(deltaX, deltaY);
        fillAbsoluteOffset();
    }

    protected void postScale(float scaleFactor, float scaleCenterX, float scaleCenterY) {
        this.imageUsingMatrix.postScale(scaleFactor, scaleFactor, scaleCenterX, scaleCenterY);
        this.overLayerMatrix.postScale(scaleFactor, scaleFactor, scaleCenterX, scaleCenterY);
        fillAbsoluteOffset();
    }

    private void checkAndSetTranslate(float deltaX, float deltaY) {
        float scaleWidth = (float) Math.round(this.origWidth * this.saveScale);
        float scaleHeight = (float) Math.round(this.origHeight * this.saveScale);
        fillAbsoluteOffset();
        float x = this.absoluteOffsetX;
        float y = this.absoluteOffsetY;
        if (scaleWidth > this.viewWidth) {
            if (x + deltaX > this.viewWidth / 3.0f) {
                deltaX = 0.0f;
            }
            if (x + deltaX < (-(scaleWidth - ((this.viewWidth * 2.0f) / 3.0f)))) {
                deltaX = 0.0f;
            }
        } else {
            if (x + deltaX > this.viewWidth - ((2.0f * scaleWidth) / 3.0f)) {
                deltaX = 0.0f;
            }
            if (x + deltaX < (-scaleWidth) / 3.0f) {
                deltaX = 0.0f;
            }
        }
        if (scaleHeight > this.viewHeight) {
            if (y + deltaY > this.viewHeight / 3.0f) {
                deltaY = 0.0f;
            }
            if (y + deltaY < (-(scaleHeight - ((this.viewHeight * 2.0f) / 3.0f)))) {
                deltaY = 0.0f;
            }
        } else {
            if (y + deltaY > this.viewHeight - ((2.0f * scaleHeight) / 3.0f)) {
                deltaY = 0.0f;
            }
            if (y + deltaY < (-scaleHeight) / 3.0f) {
                deltaY = 0.0f;
            }
        }
        postTranslate(deltaX, deltaY);
        checkSiding();
    }

    public PointF getAbsoluteOffset() {
        fillAbsoluteOffset();
        return new PointF(this.absoluteOffsetX, this.absoluteOffsetY);
    }

    public float getScale() {
        return this.saveScale;
    }

    private void checkSiding() {
        fillAbsoluteOffset();
        float x = this.absoluteOffsetX;
        float y = this.absoluteOffsetY;
        float scaleWidth = (float) Math.round(this.origWidth * this.saveScale);
        float scaleHeight = (float) Math.round(this.origHeight * this.saveScale);
        this.onBottomSide = false;
        this.onTopSide = false;
        this.onRightSide = false;
        this.onLeftSide = false;
        if ((-x) < 10.0f) {
            this.onLeftSide = true;
        }
        if ((scaleWidth >= this.viewWidth && (x + scaleWidth) - this.viewWidth < 10.0f) || (scaleWidth <= this.viewWidth && (-x) + scaleWidth <= this.viewWidth)) {
            this.onRightSide = true;
        }
        if ((-y) < 10.0f) {
            this.onTopSide = true;
        }
        if (Math.abs(((-y) + this.viewHeight) - scaleHeight) < 10.0f) {
            this.onBottomSide = true;
        }
    }

    private void calcPadding() {
        this.right = ((this.viewWidth * this.saveScale) - this.viewWidth) - ((this.redundantXSpace * 2.0f) * this.saveScale);
        this.bottom = ((this.viewHeight * this.saveScale) - this.viewHeight) - ((this.redundantYSpace * 2.0f) * this.saveScale);
    }

    private void fillAbsoluteOffset() {
        this.imageUsingMatrix.getValues(this.matrixValues);
        this.absoluteOffsetX = this.matrixValues[2];
        this.absoluteOffsetY = this.matrixValues[5];
    }

    public float getMinScale() {
        return this.minScale;
    }

    public void setImageDrawable(Drawable drawable) {
        this.bmWidth = (float) drawable.getIntrinsicWidth();
        this.bmHeight = (float) drawable.getIntrinsicHeight();
        this.saveScale = 1.0f;
        this.imageUsingMatrix.setScale(1.0f, 1.0f);
        this.overLayerMatrix.setScale(1.0f, 1.0f);
        initSize();
        calcPadding();
        super.setImageDrawable(drawable);
    }

    public void setImageBitmap(Bitmap bm) {
        this.bmWidth = (float) bm.getWidth();
        this.bmHeight = (float) bm.getHeight();
        this.saveScale = 1.0f;
        this.imageUsingMatrix.setScale(1.0f, 1.0f);
        this.overLayerMatrix.setScale(1.0f, 1.0f);
        initSize();
        calcPadding();
        super.setImageBitmap(bm);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float newHeight = (float) MeasureSpec.getSize(heightMeasureSpec);
        this.viewWidth = (float) MeasureSpec.getSize(widthMeasureSpec);
        this.viewHeight = newHeight;
        if (this.bmWidth / this.bmHeight > this.viewWidth / this.viewHeight) {
            this.minScale = this.viewWidth / this.bmWidth;
        }
        if (this.bmWidth / this.bmHeight < this.viewWidth / this.viewHeight) {
            this.minScale = this.viewHeight / this.bmHeight;
        }
    }

    private void initSize() {
        this.redundantYSpace = this.viewHeight - (this.saveScale * this.bmHeight);
        this.redundantXSpace = this.viewWidth - (this.saveScale * this.bmWidth);
        this.redundantYSpace = (float) (((double) this.redundantYSpace) / 2.0d);
        this.redundantXSpace = (float) (((double) this.redundantXSpace) / 2.0d);
        this.origWidth = this.viewWidth - (this.redundantXSpace * 2.0f);
        this.origHeight = this.viewHeight - (this.redundantYSpace * 2.0f);
    }

    private double distanceBetween(PointF left, PointF right) {
        return Math.sqrt(Math.pow((double) (left.x - right.x), 2.0d) + Math.pow((double) (left.y - right.y), 2.0d));
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return new Float(Math.sqrt((double) ((x * x) + (y * y)))).floatValue();
    }

    private void midPoint(PointF point, MotionEvent event) {
        point.set((event.getX(0) + event.getX(1)) / 2.0f, (event.getY(0) + event.getY(1)) / 2.0f);
    }

    public void setZoom(float scale, float x, float y) {
        postScaleToImage(scale, x, y);
    }

    public void postScaleToImage(float scaleFactor, float scaleFocusX, float scaleFocusY) {
        float origScale = this.saveScale;
        this.saveScale *= scaleFactor;
        if (this.saveScale > this.maxScale) {
            this.saveScale = this.maxScale;
            scaleFactor = this.maxScale / origScale;
        } else if (this.saveScale < this.minScale) {
            this.saveScale = this.minScale;
            scaleFactor = this.minScale / origScale;
        }
        this.right = ((this.viewWidth * this.saveScale) - this.viewWidth) - ((2.0f * this.redundantXSpace) * this.saveScale);
        this.bottom = ((this.viewHeight * this.saveScale) - this.viewHeight) - ((2.0f * this.redundantYSpace) * this.saveScale);
        float x;
        float y;
        if (this.origWidth * this.saveScale <= this.viewWidth || this.origHeight * this.saveScale <= this.viewHeight) {
            postScale(scaleFactor, this.viewWidth / 2.0f, this.viewHeight / 2.0f);
            if (scaleFactor < 1.0f) {
                fillAbsoluteOffset();
                x = this.absoluteOffsetX;
                y = this.absoluteOffsetY;
                if (scaleFactor < 1.0f) {
                    if (((float) Math.round(this.origWidth * this.saveScale)) < this.viewWidth) {
                        if (y < (-this.bottom)) {
                            postTranslate(0.0f, -(this.bottom + y));
                        } else if (y > 0.0f) {
                            postTranslate(0.0f, -y);
                        }
                    } else if (x < (-this.right)) {
                        postTranslate(-(this.right + x), 0.0f);
                    } else if (x > 0.0f) {
                        postTranslate(-x, 0.0f);
                    }
                }
            }
        } else {
            postScale(scaleFactor, scaleFocusX, scaleFocusY);
            fillAbsoluteOffset();
            x = this.absoluteOffsetX;
            y = this.absoluteOffsetY;
            if (scaleFactor < 1.0f) {
                if (x < (-this.right)) {
                    postTranslate(-(this.right + x), 0.0f);
                } else if (x > 0.0f) {
                    postTranslate(-x, 0.0f);
                }
                if (y < (-this.bottom)) {
                    postTranslate(0.0f, -(this.bottom + y));
                } else if (y > 0.0f) {
                    postTranslate(0.0f, -y);
                }
            }
        }
        postInvalidate();
    }

    public void postMatrixRotate(float rotate, PointF pointF) {
        this.imageUsingMatrix.postRotate(rotate, this.mid.x, this.mid.y);
        this.overLayerMatrix.postRotate(rotate, this.mid.x, this.mid.y);
        this.imageSavedMatrix.set(this.imageUsingMatrix);
    }
}
