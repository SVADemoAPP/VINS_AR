package com.xhf.hw.vins;

import android.view.Surface;
import android.widget.ImageView;
import android.widget.TextView;

import com.xhf.hw.bean.CollectPose;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * JNI Java Part
 */
public class VinsJNI implements Serializable {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("NativeLib");
    }

    public native void init();

    public native void onImageAvailable(int width, int height, int rowStrideY, ByteBuffer bufferY,
                                        int rowStrideUV, ByteBuffer bufferU, ByteBuffer bufferV,
                                        Surface surface, long timeStamp, boolean isScreenRotated,
                                        float virtualCamDistance,
                                        byte[] bytes, boolean getData);

    public native void updateViewInfo(TextView tvX, TextView tvY, TextView tvZ,
                                      TextView tvTotal, TextView tvLoop, TextView tvFeature,
                                      TextView tvBuf, ImageView initImage);

    public native void onPause();

    public native void onARSwitch(boolean isChecked);

    public native void onLoopSwitch(boolean isChecked);

    public native List<CollectPose> getAllPose();
}
