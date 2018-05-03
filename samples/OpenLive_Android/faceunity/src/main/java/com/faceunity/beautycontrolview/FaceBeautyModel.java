package com.faceunity.beautycontrolview;

import java.util.HashMap;
import java.util.Map;

/**
 * 美颜参数SharedPreferences记录
 * Created by tujh on 2018/3/7.
 */

public class FaceBeautyModel {
    private static final String TAG = FaceBeautyModel.class.getSimpleName();

    private static final String FaceBeautyFilterLevel = "FaceBeautyFilterLevel_";

    private float mFaceBeautyALLBlurLevel = 1.0f;//精准磨皮
    private float mFaceBeautyType = 0.0f;//美肤类型
    private float mFaceBeautyBlurLevel = 0.7f;//磨皮
    private float mFaceBeautyColorLevel = 0.5f;//美白
    private float mFaceBeautyRedLevel = 0.5f;//红润
    private float mBrightEyesLevel = 1000.7f;//亮眼
    private float mBeautyTeethLevel = 1000.7f;//美牙

    private float mOpenFaceShape = 1.0f;
    private float mFaceBeautyFaceShape = 3.0f;//脸型
    private float mFaceShapeLevel = 1.0f;//程度
    private float mFaceBeautyEnlargeEye_old = 0.4f;//大眼
    private float mFaceBeautyCheekThin_old = 0.4f;//瘦脸
    private float mFaceBeautyEnlargeEye = 0.4f;//大眼
    private float mFaceBeautyCheekThin = 0.4f;//瘦脸
    private float mChinLevel = 0.3f;//下巴
    private float mForeheadLevel = 0.3f;//额头
    private float mThinNoseLevel = 0.5f;//瘦鼻
    private float mMouthShape = 0.4f;//嘴形

    private Map<String, Float> mFilterLevelIntegerMap = new HashMap<>();

    private static FaceBeautyModel sMFaceBeautyModel;

    public static FaceBeautyModel getInstance() {
        if (sMFaceBeautyModel == null) {
            sMFaceBeautyModel = new FaceBeautyModel();
        }
        return sMFaceBeautyModel;
    }

    private FaceBeautyModel() {
    }

    public float getFaceBeautyFilterLevel(String filterName) {
        Float level = mFilterLevelIntegerMap.get(FaceBeautyFilterLevel + filterName);
        return level == null ? 1.0f : level;
    }

    public void setFaceBeautyFilterLevel(String filterName, float faceBeautyFilterLevel) {
        mFilterLevelIntegerMap.put(FaceBeautyFilterLevel + filterName, faceBeautyFilterLevel);
    }

    public float getFaceBeautyALLBlurLevel() {
        return mFaceBeautyALLBlurLevel;
    }

    public void setFaceBeautyALLBlurLevel(float faceBeautyALLBlurLevel) {
        mFaceBeautyALLBlurLevel = faceBeautyALLBlurLevel;
    }

    public float getFaceBeautyBlurLevel() {
        return mFaceBeautyBlurLevel;
    }

    public void setFaceBeautyBlurLevel(float faceBeautyBlurLevel) {
        mFaceBeautyBlurLevel = faceBeautyBlurLevel;
    }

    public float getFaceBeautyType() {
        return mFaceBeautyType;
    }

    public void setFaceBeautyType(float faceBeautyType) {
        mFaceBeautyType = faceBeautyType;
    }

    public float getFaceBeautyColorLevel() {
        return mFaceBeautyColorLevel;
    }

    public void setFaceBeautyColorLevel(float faceBeautyColorLevel) {
        mFaceBeautyColorLevel = faceBeautyColorLevel;
    }

    public float getFaceBeautyRedLevel() {
        return mFaceBeautyRedLevel;
    }

    public void setFaceBeautyRedLevel(float faceBeautyRedLevel) {
        mFaceBeautyRedLevel = faceBeautyRedLevel;
    }

    public float getBrightEyesLevel() {
        return mBrightEyesLevel;
    }

    public void setBrightEyesLevel(float brightEyesLevel) {
        mBrightEyesLevel = brightEyesLevel;
    }

    public float getBeautyTeethLevel() {
        return mBeautyTeethLevel;
    }

    public void setBeautyTeethLevel(float beautyTeethLevel) {
        mBeautyTeethLevel = beautyTeethLevel;
    }

    public float getOpenFaceShape() {
        return mOpenFaceShape;
    }

    public void setOpenFaceShape(float openFaceShape) {
        mOpenFaceShape = openFaceShape;
    }

    public float getFaceBeautyFaceShape() {
        return mFaceBeautyFaceShape;
    }

    public void setFaceBeautyFaceShape(float faceBeautyFaceShape) {
        mFaceBeautyFaceShape = faceBeautyFaceShape;
    }

    public float getFaceShapeLevel() {
        return mFaceShapeLevel;
    }

    public void setFaceShapeLevel(float faceShapeLevel) {
        mFaceShapeLevel = faceShapeLevel;
    }

    public float getFaceBeautyEnlargeEye_old() {
        return mFaceBeautyEnlargeEye_old;
    }

    public void setFaceBeautyEnlargeEye_old(float faceBeautyEnlargeEye_old) {
        mFaceBeautyEnlargeEye_old = faceBeautyEnlargeEye_old;

    }

    public float getFaceBeautyCheekThin_old() {
        return mFaceBeautyCheekThin_old;
    }

    public void setFaceBeautyCheekThin_old(float faceBeautyCheekThin_old) {
        mFaceBeautyCheekThin_old = faceBeautyCheekThin_old;

    }

    public float getFaceBeautyEnlargeEye() {
        return mFaceBeautyEnlargeEye;
    }

    public void setFaceBeautyEnlargeEye(float faceBeautyEnlargeEye) {
        mFaceBeautyEnlargeEye = faceBeautyEnlargeEye;
    }

    public float getFaceBeautyCheekThin() {
        return mFaceBeautyCheekThin;
    }

    public void setFaceBeautyCheekThin(float faceBeautyCheekThin) {
        mFaceBeautyCheekThin = faceBeautyCheekThin;
    }

    public float getChinLevel() {
        return mChinLevel;
    }

    public void setChinLevel(float chinLevel) {
        mChinLevel = chinLevel;
    }

    public float getForeheadLevel() {
        return mForeheadLevel;
    }

    public void setForeheadLevel(float foreheadLevel) {
        mForeheadLevel = foreheadLevel;
    }

    public float getThinNoseLevel() {
        return mThinNoseLevel;
    }

    public void setThinNoseLevel(float thinNoseLevel) {
        mThinNoseLevel = thinNoseLevel;
    }

    public float getMouthShape() {
        return mMouthShape;
    }

    public void setMouthShape(float mouthShape) {
        mMouthShape = mouthShape;
    }

}
