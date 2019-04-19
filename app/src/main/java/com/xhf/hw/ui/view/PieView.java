package com.xhf.hw.ui.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.thkoeln.jmoeller.vins_mobile_androidport.R;


public class PieView extends View {
    private static final String TAG = "PieView";
    private Paint mPaint;
    private Context mContext;
    private OnPieViewTouchListener mOnPieViewTouchListener;
    /**
     * 保存控件中心点位置
     */
    private Point mCenterPoint;
    /**
     * 暂存当前点击区域的方位
     */
    private ClickedDirection mDirection; // 点击的区域方位
    /**
     * View默认颜色
     */
    private int mDefaultColor;
    /**
     * View点击后的颜色
     */
    private int mPressedColor;
    /**
     * 分割线的颜色
     */
    private int mDividerColor;
    /**
     * 间隙宽度
     */
    private float mGapWidth;
    /**
     * 箭头在每一块区域相对于mOuterRadius的距离
     */
    private float mArrowLocation;
    /**
     * 箭头分支长度
     */
    private float mArrowBranchLength;
    /**
     * View的内编剧
     */
    private int mPadding;
    /**
     * 是否点击在可点区域内
     */
    private boolean isClicked = false;
    /**
     * 外圆半径
     */
    private int mOuterRadius;
    /**
     * 内圆半径
     */
    private int mInnerRadius;
    /**
     * View的宽
     */
    private int mWidth = 0;
    /**
     * View的高
     */
    private int mHeight = 0;
    /**
     * 暂存当前内圆颜色
     */
    private int mInnerCircleColor;
    /**
     * 枚举类型，代表8个方向
     */
    public enum ClickedDirection {
        LEFT, RIGHT, UP, DOWN, UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT, CENTER;
    }

    public PieView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initAttrs(attrs);
        init();
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.PieView);
        if (typedArray != null) {
            mDefaultColor = typedArray.getColor(R.styleable.PieView_defaultColor, 0xFFFFFFFF);
//            mPressedColor = typedArray.getColor(R.styleable.PieView_pressedColor, 0xFF000000);
            mPressedColor = Color.RED;
            mDividerColor = typedArray.getColor(R.styleable.PieView_dividerColor, 0xFFFFFFFF);
            mGapWidth = typedArray.getDimension(R.styleable.PieView_gapWidth, 3);
            mArrowLocation = typedArray.getDimension(R.styleable.PieView_arrowLocation, 25);
            mArrowBranchLength = typedArray.getDimension(R.styleable.PieView_arrowBranchLength, 8);
            typedArray.recycle();
        }
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true); // 抗锯齿
        mPadding = getPaddingLeft();
        mInnerCircleColor = mDefaultColor;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = measuredActualValue(widthMeasureSpec);
        mHeight = measuredActualValue(heightMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);
    }

    private int measuredActualValue(int measureSpec) {
        int result = 0; // 最终尺寸大小
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            // 已经给定明确的值
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            // 给定最大值，暂时直接赋值
            result = specSize;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mOuterRadius = Math.min(getHeight(), getWidth()) / 2 - mPadding;
        mInnerRadius = mOuterRadius / 3;

        calCenterPoint();
        drawOuterCircle(canvas);
        drawLine(canvas);
        if (isClicked) {
            drawOnClickColor(canvas);
        }
        drawInnerCircleBackground(canvas);
        drawInnerCircle(canvas);
        drawArrow(canvas);
    }

    /**
     * 画出每个方向箭头
     *
     * @param canvas
     */
    private void drawArrow(Canvas canvas) {
        Path arrowPath = new Path();
        int arrowRadius = (int) (mOuterRadius - mArrowLocation);

        for (int i = 0; i < 8; i++) {
            // 计算箭头顶点的坐标
            double topX = mCenterPoint.x + arrowRadius * Math.cos(Math.toRadians(45 * i));
            double topY = mCenterPoint.y - arrowRadius * Math.sin(Math.toRadians(45 * i));

            arrowPath.moveTo((float) topX, (float) topY);
            arrowPath.lineTo((float) (topX - mArrowBranchLength * Math.cos(Math.toRadians(45 * (i + 1)))),
                    (float) (topY + mArrowBranchLength * Math.sin(Math.toRadians(45 * (i + 1)))));
            arrowPath.moveTo((float) topX, (float) topY);
            arrowPath.lineTo((float) (topX - mArrowBranchLength * Math.cos(Math.toRadians(45 * (i - 1)))),
                    (float) (topY + mArrowBranchLength * Math.sin(Math.toRadians(45 * (i - 1)))));
        }
        arrowPath.close();
        mPaint.setColor(mDividerColor);
        mPaint.setStyle(Style.STROKE);
        mPaint.setStrokeWidth(2);
        canvas.drawPath(arrowPath, mPaint);

        // 绘制中间按钮的箭头
        int smallCircleRadius = mInnerRadius * 2 / 5; // 五分之二倍的内圆半径
        float centerTopX = mCenterPoint.x + smallCircleRadius;
        float centerTopY = mCenterPoint.y;
        float smallArrowBranchLength = mArrowBranchLength / 2;

        canvas.drawArc(new RectF(mCenterPoint.x - smallCircleRadius, mCenterPoint.y - smallCircleRadius,
                mCenterPoint.x + smallCircleRadius, mCenterPoint.y + smallCircleRadius), 0F, 315F, false, mPaint);

        Path centerArrowPath = new Path();
        centerArrowPath.moveTo(centerTopX, centerTopY);
        centerArrowPath.lineTo((float) (centerTopX - smallArrowBranchLength * Math.sqrt(2) / 2),
                (float) (centerTopY + smallArrowBranchLength * Math.sqrt(2) / 2));
        centerArrowPath.moveTo(centerTopX, mCenterPoint.y);
        centerArrowPath.lineTo((float) (centerTopX + smallArrowBranchLength * Math.sqrt(2) / 2),
                (float) (centerTopY + smallArrowBranchLength * Math.sqrt(2) / 2));
        centerArrowPath.close();
        canvas.drawPath(centerArrowPath, mPaint);
    }

    /**
     * 绘制内圆背景色
     * @param canvas
     */
    private void drawInnerCircleBackground(Canvas canvas) {
        mPaint.setColor(mDividerColor);
        mPaint.setStyle(Style.FILL);
        canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mInnerRadius + mGapWidth, mPaint);
    }

    /**
     * 计算view的中心位置
     */
    private void calCenterPoint() {
        if (mCenterPoint == null) {
            mCenterPoint = new Point();
            mCenterPoint.x = mWidth / 2;
            mCenterPoint.y = mHeight / 2;
        }
    }

    /**
     * 绘制大圆
     *
     * @param canvas
     */
    private void drawOuterCircle(Canvas canvas) {
        mPaint.setColor(mDefaultColor);
        mPaint.setStyle(Style.FILL);
        canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mOuterRadius, mPaint);
    }

    /**
     * 绘制分割线
     *
     * @param canvas
     */
    private void drawLine(Canvas canvas) {
        mPaint.setColor(mDividerColor);
        mPaint.setStyle(Style.STROKE);
        mPaint.setStrokeWidth(mGapWidth);
        Path linePath = new Path();
        for (int i = 0; i < 8; i++) {
            linePath.moveTo(mCenterPoint.x, mCenterPoint.y);
            double degree = Math.toRadians(22.5 + 45 * i);// 计算度数
            linePath.lineTo(mCenterPoint.x + (float) (mOuterRadius * Math.cos(degree)),
                    mCenterPoint.y - (float) (mOuterRadius * Math.sin(degree)));
        }
        canvas.drawPath(linePath, mPaint);
    }

    /**
     * 绘制内圆
     *
     * @param canvas
     */
    private void drawInnerCircle(Canvas canvas) {
        mPaint.setColor(mInnerCircleColor);
        mPaint.setStyle(Style.FILL);
        canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mInnerRadius, mPaint);
    }

    /**
     * 判断(x, y)点是否在外圆上
     *
     * @param x
     * @param y
     * @return
     */
    private boolean isInOuterCircle(double x, double y) {
        double radius = Math.sqrt(x * x + y * y);
        return radius <= mOuterRadius;
    }

    /**
     * 判断(x, y)点
     *
     * @param x
     * @param y
     * @return
     */
    private boolean isInInnerCircle(double x, double y) {
        double radius = Math.sqrt((x * x + y * y));
        return radius <= mInnerRadius;
    }

    /**
     * 绘制点击后的效果
     *
     * @param canvas
     */
    private void drawOnClickColor(Canvas canvas) {
        mInnerCircleColor = mDefaultColor;
        mPaint.setStyle(Style.FILL);
        mPaint.setColor(mPressedColor);
        RectF rectF = new RectF(mCenterPoint.x - mOuterRadius, mCenterPoint.y - mOuterRadius,
                mCenterPoint.x + mOuterRadius, mCenterPoint.y + mOuterRadius);
        switch (mDirection) {
            case RIGHT:
                canvas.drawArc(rectF, 22.5f + 45 * 7, 45, true, mPaint);
                break;
            case UP_RIGHT:
                canvas.drawArc(rectF, 22.5f + 45 * 6, 45, true, mPaint);
                break;
            case UP:
                canvas.drawArc(rectF, 22.5f + 45 * 5, 45, true, mPaint);
                break;
            case UP_LEFT:
                canvas.drawArc(rectF, 22.5f + 45 * 4, 45, true, mPaint);
                break;
            case LEFT:
                canvas.drawArc(rectF, 22.5f + 45 * 3, 45, true, mPaint);
                break;
            case DOWN_LEFT:
                canvas.drawArc(rectF, 22.5f + 45 * 2, 45, true, mPaint);
                break;
            case DOWN:
                canvas.drawArc(rectF, 22.5f + 45, 45, true, mPaint);
                break;
            case DOWN_RIGHT:
                canvas.drawArc(rectF, 22.5f, 45, true, mPaint);
                break;
            case CENTER:
                mInnerCircleColor = mPressedColor;
                break;
            default:
                break;
        }
    }

    /**
     * 判断点击的区域
     */
    private ClickedDirection getClickedDirection(double x, double y) {
        if (isInInnerCircle(x, y)) {
            return ClickedDirection.CENTER;
        }

        double degree = Math.toDegrees(Math.acos(x / (Math.sqrt(x * x + y * y))));
        Log.e(TAG, "" + degree);
        if (y < 0) {    // 若在第三第四象限，将角度折算为[180, 360]度
            degree = 360 - degree;
        }

        if (degree >= 0 && degree < 22.5 || degree > 22.5 + 45 * 7 && degree < 45 * 8) {
            return ClickedDirection.RIGHT;
        } else if (degree >= 22.5 && degree < 22.5 + 45) {
            return ClickedDirection.UP_RIGHT;
        } else if (degree >= 22.5 + 45 && degree < 22.5 + 45 * 2) {
            return ClickedDirection.UP;
        } else if (degree >= 22.5 + 45 * 2 && degree < 22.5 + 45 * 3) {
            return ClickedDirection.UP_LEFT;
        } else if (degree >= 22.5 + 45 * 3 && degree < 22.5 + 45 * 4) {
            return ClickedDirection.LEFT;
        } else if (degree >= 22.5 + 45 * 4 && degree < 22.5 + 45 * 5) {
            return ClickedDirection.DOWN_LEFT;
        } else if (degree >= 22.5 + 45 * 5 && degree < 22.5 + 45 * 6) {
            return ClickedDirection.DOWN;
        } else if (degree >= 22.5 + 45 * 6 && degree < 22.5 + 45 * 7) {
            return ClickedDirection.DOWN_RIGHT;
        }
        return null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                double x = event.getX() - mCenterPoint.x;
                double y = mCenterPoint.y - event.getY();
                if (isInOuterCircle(x, y)) {
                    mDirection = getClickedDirection(x, y);
                    isClicked = true;
                } else {
                    isClicked = false;
                }
                break;
            case MotionEvent.ACTION_UP:

                mInnerCircleColor = mDefaultColor;
                if (mOnPieViewTouchListener != null) {
                    mOnPieViewTouchListener.onTouch(this, event, mDirection);
                    //改变颜色
                }
                break;
            default:
                break;
        }
        invalidate();
        return true;
    }

    // 自定义回调接口
    public interface OnPieViewTouchListener {
        void onTouch(View v, MotionEvent e, ClickedDirection d);
    }

    public void setOnPieViewTouchListener(OnPieViewTouchListener onPieViewTouchListener) {
        mOnPieViewTouchListener = onPieViewTouchListener;
    }
}