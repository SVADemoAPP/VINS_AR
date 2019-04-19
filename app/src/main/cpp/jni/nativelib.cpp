#include <jni.h>
#include <string>
#include <android/log.h>
#include <time.h>
#include <ViewController.hpp>

#define LOG_TAG "nativelip.cpp"

// debug logging
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#include <android/native_window.h>
#include <android/native_window_jni.h>

// global variable to viewController 
// because its much less of a hassle 
// than to pass pointers to Java and back
std::unique_ptr<ViewController> viewControllerGlobal;

//校准数据保存文件夹
const string path = "/storage/emulated/0/VINS/img_imu_data/";

extern "C"
JNIEXPORT void JNICALL
Java_com_xhf_hw_vins_VinsJNI_init(JNIEnv *env, jobject instance) {

    viewControllerGlobal = std::unique_ptr<ViewController>(new ViewController);
    LOGI("Successfully created Viewcontroller Object");

    viewControllerGlobal->testMethod();

    // startup method of ViewController
    viewControllerGlobal->viewDidLoad();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_xhf_hw_vins_VinsJNI_onImageAvailable(JNIEnv *env,
                                                                                 jclass type,
                                                                                 jint width,
                                                                                 jint height,
                                                                                 jint rowStrideY,
                                                                                 jobject bufferY,
                                                                                 jint rowStrideUV,
                                                                                 jobject bufferU,
                                                                                 jobject bufferV,
                                                                                 jobject surface,
                                                                                 jlong timeStamp,
                                                                                 jboolean isScreenRotated,
                                                                                 jfloat virtualCamDistance,
                                                                                 jbyteArray byteArray,
                                                                                 jboolean isGetData) {


    long long img_time = timeStamp;

    double timeStampSec = ViewController::timeStampToSec(timeStamp);
    // IMU Meassurements are momentary meassurements.
    // Camera over an interval. so the mid of the interval is chosen as the timestamp
    // Half the maximum exposure time - half senor time delta
//    const double timeStampOffset = 1.0 / 30.0 / 2.0 - 1.0 / 100.0 / 2.0;
//    timeStampSec += timeStampOffset;
    LOGI("Current ImageTimestamp: %lf ,IMUTimeStamp: %lf",
         timeStampSec, viewControllerGlobal->getLateast_imu_time());

//    //camera
//    jbyte *srcLumaPtr = env->GetByteArrayElements(byteArray,NULL);

    //camrea2
    uint8_t *srcLumaPtr = reinterpret_cast<uint8_t *>(env->GetDirectBufferAddress(bufferY));

    if (srcLumaPtr == nullptr) {
        LOGE("blit NULL pointer ERROR");
        return;
    }
    cv::Mat mYuv(height + height / 2, width, CV_8UC1, srcLumaPtr);

//    env->ReleaseByteArrayElements(byteArray,srcLumaPtr,0);


    ANativeWindow *win = ANativeWindow_fromSurface(env, surface);

    ANativeWindow_acquire(win);
    ANativeWindow_Buffer buf;

    int rotatedWidth = height; // 480
    int rotatedHeight = width; // 640

    ANativeWindow_setBuffersGeometry(win, width, height, 0);

    if (int32_t err = ANativeWindow_lock(win, &buf, NULL)) {
        LOGE("ANativeWindow_lock failed with error code %d\n", err);
        ANativeWindow_release(win);
        return;
    }


    uint8_t *dstPtr = reinterpret_cast<uint8_t *>(buf.bits);
    Mat dstRgba(height, buf.stride, CV_8UC4, dstPtr); // TextureView buffer, use stride as width
    Mat srcRgba(height, width, CV_8UC4);
    Mat rotatedRgba(rotatedHeight, rotatedWidth, CV_8UC4);

    TS(actual_onImageAvailable);

    // convert YUV to RGBA
    cv::cvtColor(mYuv, srcRgba, CV_YUV2RGBA_NV21);

    // Rotate 90 degree
    cv::rotate(srcRgba, rotatedRgba, cv::ROTATE_90_CLOCKWISE);
    if (!isGetData) {
        assert(rotatedRgba.size().width == 480);
        assert(rotatedRgba.size().height == 640);

        viewControllerGlobal->virtualCamDistance = (float) virtualCamDistance;
        viewControllerGlobal->processImage(rotatedRgba, timeStampSec, isScreenRotated);

    } else {
        //保存相片到本地，这种效率太低，需要耗时33ms左右
        ostringstream oss;
        oss << path << img_time << ".png";

        imwrite(oss.str(), rotatedRgba);

//        LOGI("aaa: %lld",img_time);
    }
    cv::rotate(rotatedRgba, srcRgba, cv::ROTATE_90_COUNTERCLOCKWISE);

    // copy to TextureView surface
    uchar *dbuf = dstRgba.data;
    uchar *sbuf = srcRgba.data;
    int i;

    for (i = 0; i < srcRgba.rows; i++) {
        dbuf = dstRgba.data + i * buf.stride * 4;
        memcpy(dbuf, sbuf, srcRgba.cols * 4); //TODO: threw a SIGSEGV SEGV_ACCERR once
        sbuf += srcRgba.cols * 4;
    }
    TE(actual_onImageAvailable);

    mYuv.release();
    srcRgba.release();
    dstRgba.release();
    rotatedRgba.release();

    ANativeWindow_unlockAndPost(win);
    ANativeWindow_release(win);

}

extern "C"
JNIEXPORT void JNICALL
Java_com_xhf_hw_vins_VinsJNI_onPause(JNIEnv *env, jclass type) {
    LOGI("Pause triggered, stopping SensorEvents");
    viewControllerGlobal->imuStopUpdate();
    viewControllerGlobal->onStop();
}

// Constants for ImageView visibility coming from Java
const int VISIBLE = 0x00000000;
const int INVISIBLE = 0x00000004;

extern "C"
JNIEXPORT void JNICALL
Java_com_xhf_hw_vins_VinsJNI_updateViewInfo(JNIEnv *env, jclass type,
                                                                           jobject tvX, jobject tvY,
                                                                           jobject tvZ,
                                                                           jobject tvTotal,
                                                                           jobject tvLoop,
                                                                           jobject tvFeature,
                                                                           jobject tvBuf,
                                                                           jobject ivInit) {

    // Get the method handles
    jclass tvClass = env->FindClass("android/widget/TextView");
    jmethodID setTextID = env->GetMethodID(tvClass, "setText", "(Ljava/lang/CharSequence;)V");

    jclass ivClass = env->FindClass("android/widget/ImageView");
    jmethodID setVisibilityID = env->GetMethodID(ivClass, "setVisibility", "(I)V");

    viewControllerGlobal->viewUpdateMutex.lock();
    if (!viewControllerGlobal->tvXText.empty()) {
        jstring pJstring = env->NewStringUTF(viewControllerGlobal->tvXText.c_str());
        env->CallVoidMethod(tvX, setTextID, pJstring);
        env->DeleteLocalRef(pJstring);

        jstring utf = env->NewStringUTF(viewControllerGlobal->tvYText.c_str());
        env->CallVoidMethod(tvY, setTextID, utf);
        env->DeleteLocalRef(utf);

        jstring stringUTF = env->NewStringUTF(viewControllerGlobal->tvZText.c_str());
        env->CallVoidMethod(tvZ, setTextID, stringUTF);
        env->DeleteLocalRef(stringUTF);
        jstring newStringUTF = env->NewStringUTF(viewControllerGlobal->tvTotalText.c_str());
        env->CallVoidMethod(tvTotal, setTextID, newStringUTF);
        env->DeleteLocalRef(newStringUTF);

        jstring pJstring1 = env->NewStringUTF(viewControllerGlobal->tvLoopText.c_str());
        env->CallVoidMethod(tvLoop, setTextID, pJstring1);
        env->DeleteLocalRef(pJstring1);

        jstring pJstring2 = env->NewStringUTF(viewControllerGlobal->tvFeatureText.c_str());
        env->CallVoidMethod(tvFeature, setTextID, pJstring2);
        env->DeleteLocalRef(pJstring2);

        jstring pJstring3 = env->NewStringUTF(viewControllerGlobal->tvBufText.c_str());
        env->CallVoidMethod(tvBuf, setTextID, pJstring3);
        env->DeleteLocalRef(pJstring3);

        jint visibility = viewControllerGlobal->initImageVisible ? VISIBLE : INVISIBLE;
        env->CallVoidMethod(ivInit, setVisibilityID, visibility);
    }
    viewControllerGlobal->viewUpdateMutex.unlock();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_xhf_hw_vins_VinsJNI_onARSwitch(JNIEnv *env, jclass type,
                                                                       jboolean isChecked) {

    if (viewControllerGlobal)
        viewControllerGlobal->switchUI(isChecked);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_xhf_hw_vins_VinsJNI_onLoopSwitch(JNIEnv *env, jclass type,
                                                                         jboolean isChecked) {

    if (viewControllerGlobal)
        viewControllerGlobal->loopButtonPressed(isChecked);
}
extern "C"
JNIEXPORT jobject JNICALL
Java_com_xhf_hw_vins_VinsJNI_getAllPose(JNIEnv *env, jclass type) {
    std::vector<std::vector<float>> path;
    jclass list_cls = env->FindClass("java/util/ArrayList"); //获取ArrayList 对象引用
    jmethodID list_costruct = env->GetMethodID(list_cls, "<init>", "()V"); //获取构造函数id
    jobject list_obj = env->NewObject(list_cls, list_costruct);//构造ArrayList集合对象
    jmethodID list_add = env->GetMethodID(list_cls, "add", "(Ljava/lang/Object;)Z");
    jclass stucls = env->FindClass("com/xhf/hw/bean/CollectPose"); //获得mappoi类引用
    jmethodID stu_costruct = env->GetMethodID(stucls, "<init>", "(FFF)V");

    if (viewControllerGlobal)
        viewControllerGlobal->getRefinePath(path);
    for (int i = 0; i < path.size(); ++i) {
        float x = path[i][0];
        float y = path[i][1];
        float z = path[i][2];
        jobject stu_obj = env->NewObject(stucls, stu_costruct, x, y, z);
        env->CallBooleanMethod(list_obj, list_add, stu_obj);
        env->DeleteLocalRef(stu_obj);
    }
    env->DeleteLocalRef(list_cls);
    env->DeleteLocalRef(stucls);
    return list_obj;
}