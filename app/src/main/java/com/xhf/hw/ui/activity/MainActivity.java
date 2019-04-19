package com.xhf.hw.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Range;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.thkoeln.jmoeller.vins_mobile_androidport.R;
import com.xhf.hw.bean.CollectPose;
import com.xhf.hw.ui.base.BaseActivity;
import com.xhf.hw.utils.Constant;
import com.xhf.hw.utils.DistanceUtil;
import com.xhf.hw.utils.LogUtils;
import com.xhf.hw.utils.UpdateCommunityInfo;
import com.xhf.hw.vins.VinsJNI;

import net.yoojia.imagemap.HighlightImageView1;
import net.yoojia.imagemap.ImageMap1;
import net.yoojia.imagemap.core.CircleRangeShape;
import net.yoojia.imagemap.core.CircleShape;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link MainActivity} only activity
 * manages camera input, texture output
 * textViews and buttons
 */
public class MainActivity extends BaseActivity implements TextureView.SurfaceTextureListener, SensorEventListener {

    private static final String TAG = "MainActivity";
    private static final boolean GET_DATA = false;       //是否用来采集校准数据

    private static Rect sRect = new Rect(0, 0, 640, 480);

    // needed for permission request callback
    private static final int PERMISSIONS_REQUEST_CODE = 12345;

    // camera2 API Camera
    private CameraDevice camera;
    // Back cam, 1 would be the front facing one
    private String cameraID = "0";

    // Texture View to display the camera picture, or the vins output
    private TextureView textureView;
    private Size previewSize;
    private CaptureRequest.Builder previewBuilder;
    private ImageReader imageReader;

    // Handler for Camera Thread
    private Handler handler;
    private HandlerThread threadHandler;
    private SensorManager mSensorManager;

    // Cam parameters
    private final int imageWidth = 640;
    private final int imageHeight = 480;

    private final int framesPerSecond = 30;

    /**
     * Adjustment to auto-exposure (AE) target image brightness in EV
     */
    private final int aeCompensation = 0;
//    private final int aeCompensation = -1;

    private Surface surface;


    // TextViews
    private TextView tvX;
    private TextView tvY;
    private TextView tvZ;
    private TextView tvTotal;
    private TextView tvLoop;
    private TextView tvFeature;
    private TextView tvBuf;

    // ImageView for initialization instructions
    private ImageView ivInit;

    // directory path for BRIEF config files
    private final String directoryPathBriefFiles = "/storage/emulated/0/VINS";

    // Distance of virtual Cam from Center
    // could be easily manipulated in UI later
    private float virtualCamDistance = 70;
    private final float minVirtualCamDistance = 4;
    private final float maxVirtualCamDistance = 40;
    private long mTimestamp;
    private String mPath;
    private CameraManager cameraManager;
    private VinsJNI mVinsJNI;
    private long mLastExitTime;
    private SeekBar mSbAngle;
    private SeekBar mSbScale;
    private TextView mTvAngle;
    private TextView mTvScale;

    //----改--//
    private static final long TIME = 1000;
    private int mScale = 0;
    private float mAngle = 0f;
    private PointF mSelectPointF;
    private PointF mTempSelectPointF = new PointF();
    private float mScaleProgress = 0f;
    private float mAngleProgress = 0f;
    private float mChangeAngle = 0f;
    private List<Integer> mRsrpList = new ArrayList<>();  //收集Rsrp数值；
    private List<PointF> mPointList = new ArrayList<>();  //需要在地图上显示的坐标点
    private List<CollectPose> mOldPoseList = new ArrayList<>(); //上一次的getAllPose（）方法获取的
    private UpdateCommunityInfo mUpdateCommunityInfo;
    private ImageMap1 mAMap;
    private Handler mPoseHandler = new Handler();
    private Runnable mPoseRunnable = new Runnable() {
        @Override
        public void run() {
            LogUtils.e(TAG, "开始调用--------每次调用时间间隔1s--------------");
            if (!moveFlag) {
                getAllPose();
            }
            mPoseHandler.postDelayed(this, 2000);
        }
    };
    private int mMapHight;
    private CircleRangeShape mCircleRangeShape;


    private float mMoveX; //手指长按拖动的数值x
    private float mMoveY; //手指长按拖动的数值y
    private boolean moveFlag = false;
    private PointF mTempPointF = new PointF();
    private ImageView mIvReset;

    private void initMap() {
        if (Constant.mSelectBitmap != null) {
            mAMap.setMapBitmap(Constant.mSelectBitmap);
            mMapHight = Constant.mSelectBitmap.getHeight();
            mAMap.setRsrpMoveListener(new HighlightImageView1.AllRsrpMoveListener() {
                @Override
                public void move(float x, float y) {
                    LogUtils.e("Main测试拖动Move", "x =" + x + " , y =" + y);
                    mAMap.setTranlateFlag(false);
                    moveFlag = true;
                    mMoveX = x;
                    mMoveY = y;
                    mTempPointF.x = mSelectPointF.x + mMoveX;
                    mTempPointF.y = mSelectPointF.y + mMoveY;
                    drawOldRsrp();
                }

                @Override
                public void end(float x, float y) {
                    LogUtils.e("Main测试拖动end", "x =" + x + " , y =" + y);
                    mMoveX = x;
                    mMoveY = y;
                    moveFlag = false;
                    mTempPointF.x = mSelectPointF.x + mMoveX;
                    mTempPointF.y = mSelectPointF.y + mMoveY;
                    drawOldRsrp();
                    mSelectPointF.x = mTempPointF.x;
                    mSelectPointF.y = mTempPointF.y;
                    mAMap.setTranlateFlag(true);
                }
            });
        }
    }

    private void resetPointF() {
        mSelectPointF.x = mTempSelectPointF.x;
        mSelectPointF.y = mTempSelectPointF.y;
        mTempPointF.x = mTempSelectPointF.x;
        mTempPointF.y = mTempSelectPointF.y;
    }

    @Override
    public void setLayout() {
        setContentView(R.layout.activity_main);
    }

    @Override
    public void initView() {
        getIntentData(); //获取上个页面传递的数据
        mScale = (int) Constant.Scale;
        mPath = Environment.getExternalStorageDirectory() + File.separator + "VINS" + File.separator + "img_imu_data";
        File file = new File(mPath);
        if (!file.exists()) file.mkdirs();

        File file1 = new File(Environment.getExternalStorageDirectory() + File.separator + "VINS" +
                File.separator + "brief_k10L6.bin");
        File file2 = new File(Environment.getExternalStorageDirectory() + File.separator + "VINS" +
                File.separator + "brief_pattern.yml");

        if (!file1.exists()) copyFile(file1);
        if (!file2.exists()) copyFile(file2);

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        initViews();
        initMap();
        mUpdateCommunityInfo = new UpdateCommunityInfo(this, (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE), new Handler());
        mUpdateCommunityInfo.startUpdateData();
        mPoseHandler.postDelayed(mPoseRunnable, TIME);//完成初始化之后，每次间隔TIME秒调用一次
    }

    @Override
    public void initData() {

    }

    private void copyFile(final File file) {
        try {
            InputStream inputStream = getAssets().open(file.getName());
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] bytes = new byte[10240];
            int count = 0;
            while ((count = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, count);
            }

            outputStream.flush();
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starting separate thread to handle camera input
     */
    private void initLooper() {
        threadHandler = new HandlerThread("Camera2Thread");
        threadHandler.start();
        handler = new Handler(threadHandler.getLooper());
    }

    /**
     * initializes an new VinsJNI Object
     */
    private void initVINS() {
        if (mVinsJNI == null) mVinsJNI = new VinsJNI();
        mVinsJNI.init();
    }

    /**
     * Finding all UI Elements,
     * Setting TextureView Listener to this object.
     */
    private void initViews() {
        mAMap = findViewById(R.id.map);
        mSbAngle = findViewById(R.id.angle_zoom);
        mSbScale = findViewById(R.id.scale_zoom);
        mTvAngle = findViewById(R.id.angle_tv);
        mTvScale = findViewById(R.id.scale_tv);
        mIvReset = findViewById(R.id.reset);
        mSbAngle.setProgress(50);
        mTvAngle.setText("" + 0);

        mSbScale.setProgress(mScale);
        mTvScale.setText(60 + "");
        mSbAngle.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                mAngleProgress = progress;
                mChangeAngle = (int) (((float) (progress - 50)) / 50 * 50);
                LogUtils.e(TAG, "mChangeAngle = " + mChangeAngle);
                mTvAngle.setText("" + (int) mChangeAngle);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mSbScale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                mScale = (int) (Constant.MIN_SCALE + (Constant.MAX_SCALE - Constant.MIN_SCALE) * ((float) progress / 100));
                mTvScale.setText("" + mScale);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mIvReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPointF();
            }
        });
        tvX = (TextView) findViewById(R.id.x_Label);
        tvY = (TextView) findViewById(R.id.y_Label);
        tvZ = (TextView) findViewById(R.id.z_Label);
        tvTotal = (TextView) findViewById(R.id.total_odom_Label);
        tvLoop = (TextView) findViewById(R.id.loop_Label);
        tvFeature = (TextView) findViewById(R.id.feature_Label);
        tvBuf = (TextView) findViewById(R.id.buf_Label);

        ivInit = (ImageView) findViewById(R.id.init_image_view);
        ivInit.setVisibility(View.VISIBLE);

        textureView = (TextureView) findViewById(R.id.texture_view);
        textureView.setSurfaceTextureListener(this);

        // Define the Switch listeners
        Switch arSwitch = (Switch) findViewById(R.id.ar_switch);
        arSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "arSwitch State = " + isChecked);
                mVinsJNI.onARSwitch(isChecked);
            }
        });

        Switch loopSwitch = (Switch) findViewById(R.id.loop_switch);
        loopSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "loopSwitch State = " + isChecked);
                mVinsJNI.onLoopSwitch(isChecked);
            }
        });

        SeekBar zoomSlider = (SeekBar) findViewById(R.id.zoom_slider);
        zoomSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                virtualCamDistance = minVirtualCamDistance + ((float) progress / 100) * (maxVirtualCamDistance - minVirtualCamDistance);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    /**
     * SurfaceTextureListener interface function
     * used to set configuration of the camera and start it
     */
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
                                          int height) {
        try {
            // check permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                checkPermissionsIfNeccessary();
                return;
            }

            // start up Camera (not the recording)
            cameraManager.openCamera(cameraID, cameraDeviceStateCallback, handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    private CameraDevice.StateCallback cameraDeviceStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice cameraDevice) {
            try {
                camera = cameraDevice;
                startCameraView(camera);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
        }

        @Override
        public void onError(CameraDevice camera, int error) {
        }
    };

    /**
     * starts CameraView
     */
    private void startCameraView(CameraDevice camera) throws CameraAccessException {
        SurfaceTexture texture = textureView.getSurfaceTexture();

        // to set CameraView size
        texture.setDefaultBufferSize(textureView.getWidth(), textureView.getHeight());
        surface = new Surface(texture);

        try {
            // to set request for CameraView
            previewBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        // to set the format of captured images and the maximum number of images that can be accessed in mImageReader
        imageReader = ImageReader.newInstance(imageWidth, imageHeight, ImageFormat.YUV_420_888, 1);

        imageReader.setOnImageAvailableListener(onImageAvailableListener, handler);


        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraID);
        // get the StepSize of the auto exposure compensation
        Rational aeCompStepSize = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP);
        if (aeCompStepSize == null) {
            Log.e(TAG, "Camera doesn't support setting Auto-Exposure Compensation");
            finish();
        }
        Log.d(TAG, "AE Compensation StepSize: " + aeCompStepSize);

        int aeCompensationInSteps = aeCompensation * aeCompStepSize.getDenominator() / aeCompStepSize.getNumerator();
        Log.d(TAG, "aeCompensationInSteps: " + aeCompensationInSteps);
        previewBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, aeCompensationInSteps);

        if (GET_DATA) {
            // set the camera output frequency to 20Hz
            previewBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, new Range<Integer>(20, 20));
        } else {
            // set the camera output frequency to 30Hz
            previewBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, new Range<Integer>(framesPerSecond, framesPerSecond));
        }


        // the first added target surface is for CameraView display
        // the second added target mImageReader.getSurface()
        // is for ImageReader Callback where it can be access EACH frame
        //previewBuilder.addTarget(surface);
        previewBuilder.addTarget(imageReader.getSurface());

        //output Surface
        List<Surface> outputSurfaces = new ArrayList<>();
        outputSurfaces.add(imageReader.getSurface());

        camera.createCaptureSession(outputSurfaces, sessionStateCallback, handler);
    }

    private CameraCaptureSession.StateCallback sessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            try {
                updateCameraView(session);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {

        }
    };

    /**
     * Starts the RepeatingRequest for
     */
    private void updateCameraView(CameraCaptureSession session)
            throws CameraAccessException {
//        previewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
        previewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);

        session.setRepeatingRequest(previewBuilder.build(), null, handler);
    }

    /**
     * At last the actual function with access to the image
     */
    private ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {

        private int mOffset2 = 640 * 480 * 5 / 4;
        private int mOffset1 = 640 * 480;
        private long mLastTime;

        /*
         *  The following method will be called every time an image is ready
         *  be sure to use method acquireNextImage() and then close(), otherwise, the display may STOP
         */
        @Override
        public void onImageAvailable(ImageReader reader) {
            // get the newest frame
            Image image = reader.acquireNextImage();

            if (image == null) {
                return;
            }


            if (image.getFormat() != ImageFormat.YUV_420_888) {
                Log.e(TAG, "camera image is in wrong format");
            }

            //RGBA output
            Image.Plane Y_plane = image.getPlanes()[0];
            int Y_rowStride = Y_plane.getRowStride();
            Image.Plane U_plane = image.getPlanes()[1];
            int UV_rowStride = U_plane.getRowStride();
            Image.Plane V_plane = image.getPlanes()[2];

            // pass the current device's screen orientation to the c++ part
            int currentRotation = getWindowManager().getDefaultDisplay().getRotation();
            boolean isScreenRotated = currentRotation != Surface.ROTATION_90;

            // pass image to c++ part
            //这里时间要调用image.getTimestamp()，间隔是标准的33ms
            mVinsJNI.onImageAvailable(image.getWidth(), image.getHeight(),
                    Y_rowStride, Y_plane.getBuffer(),
                    UV_rowStride, U_plane.getBuffer(), V_plane.getBuffer(),
                    surface, image.getTimestamp(), isScreenRotated,
                    virtualCamDistance,
                    null, GET_DATA);

            // run the updateViewInfo function on the UI Thread so it has permission to modify it
            runOnUiThread(new Runnable() {
                public void run() {
                    mVinsJNI.updateViewInfo(tvX, tvY, tvZ, tvTotal, tvLoop, tvFeature, tvBuf, ivInit);
                }
            });

            image.close();
        }
    };

    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();

        initLooper();

        if (textureView.isAvailable()) {
            try {
                cameraManager.openCamera(cameraID, cameraDeviceStateCallback, handler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else {
            textureView.setSurfaceTextureListener(this);
        }

        initVINS();
    }

    /**
     * shutting down onPause
     */
    protected void onPause() {
        if (null != camera) {
            camera.close();
            camera = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }

        if (null != threadHandler) {
            threadHandler.quitSafely();
            threadHandler = null;
        }

        mVinsJNI.onPause();

        super.onPause();
    }

    /**
     * @return true if permissions where given
     */
    private boolean checkPermissionsIfNeccessary() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            if (info.requestedPermissions != null) {
                List<String> permissionsNotGrantedYet = new ArrayList<>(info.requestedPermissions.length);
                for (String p : info.requestedPermissions) {
                    if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                        permissionsNotGrantedYet.add(p);
                    }
                }
                if (permissionsNotGrantedYet.size() > 0) {
                    ActivityCompat.requestPermissions(this, permissionsNotGrantedYet.toArray(new String[permissionsNotGrantedYet.size()]),
                            PERMISSIONS_REQUEST_CODE);
                    return false;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            boolean hasAllPermissions = true;
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length == 0)
                hasAllPermissions = false;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED)
                    hasAllPermissions = false;
            }

            if (!hasAllPermissions) {
                finish();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values;
//        Log.d("liuping", ":" + values[0] + " " + values[1] + " " + values[2]);
        mTimestamp = event.timestamp;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onStart() {
        super.onStart();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onStop() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);

        super.onStop();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        long time = System.currentTimeMillis();
        if (time - mLastExitTime < 2000) {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        } else {
            Toast.makeText(this, "click again", Toast.LENGTH_SHORT).show();
        }

        mLastExitTime = time;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUpdateCommunityInfo.endUpdateData();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        mSelectPointF = intent.getParcelableExtra(Constant.SELECT_POINT);
        mTempPointF.x = mSelectPointF.x;
        mTempPointF.y = mSelectPointF.y;
        mTempSelectPointF.x = mSelectPointF.x;
        mTempSelectPointF.y = mSelectPointF.y;
        mAngle = intent.getFloatExtra(Constant.SELECT_ANGLE, 0f);
        LogUtils.e(TAG, "pointF=" + mSelectPointF.toString() + " , angle = " + mAngle);
    }

    private void drawOldRsrp() {
        if (mOldPoseList.size() > 0) {
            mPointList.clear();
            LogUtils.e(TAG, "加载存在的点--" + mOldPoseList.size());
            for (CollectPose collectPose : mOldPoseList) { //遍历之前保存的
                float poseX = collectPose.getPoseX();
                float poseY = collectPose.getPoseY();
                LogUtils.e(TAG, "加载存在的——x = " + poseX + " , y = " + poseY);
                float[] realPoint = DistanceUtil.mapToReal(mScale, mTempPointF.x, mTempPointF.y, mMapHight);
                float[] point = DistanceUtil.getPoint(poseX, poseY, (mAngle + mChangeAngle));
                float[] pixPoint = DistanceUtil.realToMap(mScale, (point[0] + realPoint[0]), (point[1] + realPoint[1]), mMapHight);
                mPointList.add(new PointF(pixPoint[0], pixPoint[1]));
            }
            drawCircle(mPointList);
            mCircleRangeShape = new CircleRangeShape("loc", Color.RED);
            mCircleRangeShape.setRadius(10);
            mCircleRangeShape.setRange(20);
            mCircleRangeShape.setValues(mPointList.get(mPointList.size() - 1).x, mPointList.get(mPointList.size() - 1).y);
            mAMap.addShape(mCircleRangeShape, false);
        }
    }

    private void getAllPose() {
        List<CollectPose> allPose = mVinsJNI.getAllPose();
        if (ivInit.getVisibility() == View.VISIBLE) {
            LogUtils.e(TAG, "初始化未完成----------------------------------------等待");
            drawOldRsrp();
        } else {
            LogUtils.e(TAG, "初始化完成----------------------------------------获取中");
            int oldSize = mOldPoseList.size();
            int newSize = allPose.size();
            int num = newSize - oldSize;
            int rsrpNum = Integer.valueOf(mUpdateCommunityInfo.RSRP);

            for (int i = 0; i < num; i++) {
                mRsrpList.add(rsrpNum);

            }

            mOldPoseList.clear();
            mPointList.clear();
            mOldPoseList.addAll(allPose);

            for (CollectPose collectPose : allPose) {
                float poseX = collectPose.getPoseX();
                float poseY = collectPose.getPoseY();
                LogUtils.e(TAG, "x = " + poseX + " , y = " + poseY);
                float[] realPoint = DistanceUtil.mapToReal(mScale, mTempPointF.x, mTempPointF.y, mMapHight);
                float[] point = DistanceUtil.getPoint(poseX, poseY, (mAngle + mChangeAngle));
                float[] pixPoint = DistanceUtil.realToMap(mScale, (point[0] + realPoint[0]), (point[1] + realPoint[1]), mMapHight);
                mPointList.add(new PointF(pixPoint[0], pixPoint[1]));
            }
            drawCircle(mPointList);

            mCircleRangeShape = new CircleRangeShape("loc", Color.RED);
            mCircleRangeShape.setRadius(10);
            mCircleRangeShape.setRange(20);
            mCircleRangeShape.setValues(mPointList.get(newSize - 1).x, mPointList.get(newSize - 1).y);
            mAMap.addShape(mCircleRangeShape, false);
        }
    }

    private void drawCircle(List<PointF> pointFList) {
        mAMap.clearShapes();
        int rsrpColor;
        for (int i = 0; i < mRsrpList.size(); i++) {
            int rsrp = mRsrpList.get(i);
            if (-75 < rsrp && rsrp <= 0) {  //1e8449
                rsrpColor = Color.parseColor("#1e8449");
            } else if (-95 < rsrp && rsrp <= -75) { //浅绿色
                rsrpColor = Color.GREEN;
            } else if (-105 < rsrp && rsrp <= -95) {  //黄色
                rsrpColor = Color.YELLOW;
            } else if (-120 < rsrp && rsrp <= -105) { //红色
                rsrpColor = Color.RED;
            } else {
                rsrpColor = Color.BLACK;
            }
            CircleShape circleShape = new CircleShape("tag" + i, rsrpColor, 6);
            circleShape.setValues(pointFList.get(i).x, pointFList.get(i).y);
            mAMap.addShape(circleShape, false);
        }
    }

    /**
     * 通过seekbar修改scale
     */
    private void setScale() {

    }
}
