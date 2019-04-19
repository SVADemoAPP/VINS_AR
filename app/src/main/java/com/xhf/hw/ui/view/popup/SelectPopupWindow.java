package com.xhf.hw.ui.view.popup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.thkoeln.jmoeller.vins_mobile_androidport.R;
import com.xhf.hw.ui.view.PieView;

import net.yoojia.imagemap.ImageMap1;
import net.yoojia.imagemap.TouchImageView1;
import net.yoojia.imagemap.core.CircleShape;


public class SelectPopupWindow {
    private static final String SELECT_ADDRESS = "select";
    private SuperPopupWindow mSelectPopupWindow;
    private Bitmap mMapBitmap;
    private ImageMap1 mAmap;
    private CircleShape mSelectShape;
    private int mCircleRadius = 10;
    private boolean firstSelect = true;
    private TextView mTvCancel;
    private PointF mSelectPointF;
    private View mTvConfirm;
    private SelectPointListener mSelectPointListener;
    private Context mContext;
    private RelativeLayout mRl;
    private AngeleListener mListener;
    private PieView mPieView;
    private float mAngle = -1;

    public SelectPopupWindow(Context context, Bitmap mapBitmap) {
        mContext = context;
        mMapBitmap = mapBitmap;
        mSelectPopupWindow = new SuperPopupWindow(context, R.layout.popupwindow_select_map);
        View popupView = mSelectPopupWindow.getPopupView();
        initPopupWindow(popupView);
        initData();
        mSelectPopupWindow.setBlack(0.1f);
    }

    public void setSelectListener(SelectPointListener selectPointListener) {
        mSelectPointListener = selectPointListener;
    }


    /**
     * 初始化prru
     */
    public void initPopupWindow(View view) {
        mRl = view.findViewById(R.id.content);
        mPieView = view.findViewById(R.id.select_pv);
        mAmap = view.findViewById(R.id.pop_select_map);
        mAmap.setAllowRotate(false);
        mAmap.setAllowRequestTranslate(false);
        mTvCancel = view.findViewById(R.id.pop_cancel);
        mTvConfirm = view.findViewById(R.id.pop_confirm);
        mAmap.setOnSingleClickListener(new TouchImageView1.OnSingleClickListener() {
            @Override
            public void onSingle(PointF pointF) {
                mSelectShape.setValues(pointF.x, pointF.y);
                mSelectPointF = pointF;
                if (firstSelect) {
                    firstSelect = false;
                    mAmap.addShape(mSelectShape, false);
                }
            }
        });
        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePopupWindow();
                if (mSelectPointListener != null) {
                    mSelectPointListener.cancel();
                }

            }
        });
        mTvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectPointListener != null) {
                    if (mAngle < 0) {
                        Toast.makeText(mContext, "请选择方向，然后点击确定", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (mSelectPointF != null) {
                        mListener.getAngle(mAngle);
                        mSelectPointListener.getPoint(mSelectPointF); //获取选中点
                        hidePopupWindow();
                    } else {
                        Toast.makeText(mContext, "请在地图上选择当前位置", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        mPieView.setOnPieViewTouchListener(new PieView.OnPieViewTouchListener() {


            @Override
            public void onTouch(View v, MotionEvent e, PieView.ClickedDirection d) {
                mAngle = -1f;
                switch (d) {
                    case UP:
                        mAngle = 270;
                        break;
                    case DOWN:
                        mAngle = 90;
                        break;
                    case LEFT:
                        mAngle = 180;
                        break;
                    case RIGHT:
                        mAngle = 0;
                        break;
                    case CENTER:
                        mAngle = -1;
                        break;
                    case UP_LEFT:
                        mAngle = 225;
                        break;
                    case UP_RIGHT:
                        mAngle = 315;
                        break;
                    case DOWN_LEFT:
                        mAngle = 135;
                        break;
                    case DOWN_RIGHT:
                        mAngle = 45;
                        break;

                }

            }
        });
    }

    /**
     * 初始化prru
     */
    public void initData() {
        if (mMapBitmap != null) {
            mAmap.setMapBitmap(mMapBitmap);
        }
        mSelectShape = new CircleShape(SELECT_ADDRESS, Color.RED, mCircleRadius);
    }

    /**
     * 显示
     */
    public void showPopupWindow() {
        mSelectPopupWindow.showPopupWindow();
    }

    public void setSelectAngleListener(AngeleListener listener) {
        mListener = listener;
    }

    public interface AngeleListener {
        void getAngle(float angle);
    }

    /**
     * 隐藏
     */
    public void hidePopupWindow() {
        if (mSelectPopupWindow.isShowing()) {
            mSelectPopupWindow.hidePopupWindow();
        }
    }

    public interface SelectPointListener {
        void getPoint(PointF pointF);

        void cancel();
    }


    public void setmSelectShape(float x, float y) {
        mSelectShape.setValues(x, y);
        mSelectPointF.set(x, y);
    }
}
