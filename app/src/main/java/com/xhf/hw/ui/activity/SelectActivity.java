package com.xhf.hw.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.thkoeln.jmoeller.vins_mobile_androidport.R;
import com.xhf.hw.ui.base.BaseActivity;
import com.xhf.hw.ui.view.popup.SelectPopupWindow;
import com.xhf.hw.utils.Constant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class SelectActivity extends BaseActivity {
    private SelectPopupWindow mSelecPopupWindow;

    private Bitmap mBitmap;
    private float mAngle = -1;
    private PointF mSelectPointF;
    private Context mContext;

    @Override
    public void setLayout() {
        mContext = this;
        setContentView(R.layout.activity_select);
    }

    @Override
    public void initView() {

    }

    @Override
    public void initData() {
        initMap();
        initPop();
    }

    private void initMap() {
        String mapPath = Constant.MAP;
        File file = new File(mapPath);
        if (!file.exists()) {
            file.mkdirs();
            Toast.makeText(mContext, "加载图片不存在，请手动在/sdcard/VINS_AR/map/目录下加入ar.png", Toast.LENGTH_SHORT).show();
            initOtherMap();
        } else {
            mapPath = mapPath + File.separator + "ar.png";
            File arMap = new File(mapPath);
            if (arMap.exists()) {
                Constant.mSelectBitmap = BitmapFactory.decodeFile(mapPath);
            } else {
                Toast.makeText(mContext, "加载图片不存在，请手动在/sdcard/VINS_AR/map/目录下加入ar.png", Toast.LENGTH_SHORT).show();
                initOtherMap();
            }

        }


    }

    private void initOtherMap() {
        InputStream is = null;
        try {
            is = getAssets().open("U5.png");
            BitmapDrawable bd = (BitmapDrawable) Drawable.createFromStream(is, null);
            Constant.mSelectBitmap = bd.getBitmap();
            is.close();
        } catch (IOException e) {
            Toast.makeText(this, "加载地图失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        Toast.makeText(this, "加载默认图片", Toast.LENGTH_SHORT).show();
    }

    private void initPop() {
        mSelecPopupWindow = new SelectPopupWindow(mContext, Constant.mSelectBitmap);
        mSelecPopupWindow.setSelectAngleListener(new SelectPopupWindow.AngeleListener() {
            @Override
            public void getAngle(float angle) {
                mAngle = angle;
            }
        });
        mSelecPopupWindow.setSelectListener(new SelectPopupWindow.SelectPointListener() {
            @Override
            public void getPoint(PointF pointF) {
                mSelectPointF = pointF;
                gotoMainAcitvity();
            }

            @Override
            public void cancel() {
                finish();
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (mSelecPopupWindow != null) {
                mSelecPopupWindow.showPopupWindow();
            }
        }
    }

    private void gotoMainAcitvity() {
        Intent intent = new Intent();
        intent.setClass(mContext, MainActivity.class);
        intent.putExtra(Constant.SELECT_POINT, mSelectPointF);
        intent.putExtra(Constant.SELECT_ANGLE, mAngle);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSelecPopupWindow != null) {
            mSelecPopupWindow.hidePopupWindow();
            mSelecPopupWindow = null;
        }
    }
}
