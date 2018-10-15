package com.faceunity.utils;

import com.faceunity.R;
import com.faceunity.entity.Filter;
import com.faceunity.entity.Makeup;

import java.util.HashMap;
import java.util.Map;

/**
 * 美颜参数SharedPreferences记录,目前仅以保存数据，可改造为以SharedPreferences保存数据
 * Created by tujh on 2018/3/7.
 */

public abstract class BeautyParameterModel {
    public static final String TAG = BeautyParameterModel.class.getSimpleName();


    public static final String sStrFilterLevel = "FilterLevel_";
    public static Map<String, Float> sFilterLevel = new HashMap<>();
    public static Filter sFilterName = FilterEnum.nature_beauty.filter();

    public static Map<String, Float> sMakeupLevel = new HashMap<>();
    public static Makeup[] sMakeups = {MakeupEnum.MakeupNone.makeup(), MakeupEnum.MakeupNone.makeup(), MakeupEnum.MakeupNone.makeup(), MakeupEnum.MakeupNone.makeup(), MakeupEnum.MakeupNone.makeup(), MakeupEnum.MakeupNone.makeup(), MakeupEnum.MakeupNone.makeup()};

    public static float sSkinDetect = 1.0f;//精准磨皮
    public static float sHeavyBlur = 0.0f;//美肤类型
    public static float sHeavyBlurLevel = 0.7f;//磨皮
    public static float sBlurLevel = 0.7f;//磨皮
    public static float sColorLevel = 0.5f;//美白
    public static float sRedLevel = 0.5f;//红润
    public static float sEyeBright = 0.0f;//亮眼
    public static float sToothWhiten = 0.0f;//美牙

    public static float sFaceShape = 4.0f;//脸型
    public static float sFaceShapeLevel = 1.0f;//程度
    public static float sEyeEnlarging = 0.4f;//大眼
    public static float sEyeEnlargingOld = 0.4f;//大眼
    public static float sCheekThinning = 0.4f;//瘦脸
    public static float sCheekThinningOld = 0.4f;//瘦脸
    public static float sIntensityChin = 0.3f;//下巴
    public static float sIntensityForehead = 0.3f;//额头
    public static float sIntensityNose = 0.5f;//瘦鼻
    public static float sIntensityMouth = 0.4f;//嘴形


    public static boolean isOpen(int checkId) {
        if (checkId == R.id.beauty_box_skin_detect) {
            return sSkinDetect == 1;
        } else if (checkId == R.id.beauty_box_heavy_blur) {
            return sHeavyBlur == 1 ? sHeavyBlurLevel > 0 : sBlurLevel > 0;
        } else if (checkId == R.id.beauty_box_blur_level) {
            return sHeavyBlurLevel > 0;
        } else if (checkId == R.id.beauty_box_color_level) {
            return sColorLevel > 0;
        } else if (checkId == R.id.beauty_box_red_level) {
            return sRedLevel > 0;
        } else if (checkId == R.id.beauty_box_eye_bright) {
            return sEyeBright > 0;
        } else if (checkId == R.id.beauty_box_tooth_whiten) {
            return sToothWhiten != 0;
        } else if (checkId == R.id.beauty_box_face_shape) {
            return sFaceShape != 3;
        } else if (checkId == R.id.beauty_box_eye_enlarge) {
            if (sFaceShape == 4)
                return sEyeEnlarging > 0;
            else
                return sEyeEnlargingOld > 0;
        } else if (checkId == R.id.beauty_box_cheek_thinning) {
            if (sFaceShape == 4)
                return sCheekThinning > 0;
            else
                return sCheekThinningOld > 0;
        } else if (checkId == R.id.beauty_box_intensity_chin) {
            return sIntensityChin != 0.5;
        } else if (checkId == R.id.beauty_box_intensity_forehead) {
            return sIntensityForehead != 0.5;
        } else if (checkId == R.id.beauty_box_intensity_nose) {
            return sIntensityNose > 0;
        } else if (checkId == R.id.beauty_box_intensity_mouth) {
            return sIntensityMouth != 0.5;
        } else {
            return true;
        }
    }

    public static float getValue(int checkId) {
        if (checkId == R.id.beauty_box_skin_detect) {
            return sSkinDetect;
        } else if (checkId == R.id.beauty_box_heavy_blur) {
            return sHeavyBlur == 1 ? sHeavyBlurLevel : sBlurLevel;
        } else if (checkId == R.id.beauty_box_blur_level) {
            return sHeavyBlurLevel;
        } else if (checkId == R.id.beauty_box_color_level) {
            return sColorLevel;
        } else if (checkId == R.id.beauty_box_red_level) {
            return sRedLevel;
        } else if (checkId == R.id.beauty_box_eye_bright) {
            return sEyeBright;
        } else if (checkId == R.id.beauty_box_tooth_whiten) {
            return sToothWhiten;
        } else if (checkId == R.id.beauty_box_face_shape) {
            return sFaceShape;
        } else if (checkId == R.id.beauty_box_eye_enlarge) {
            if (sFaceShape == 4)
                return sEyeEnlarging;
            else
                return sEyeEnlargingOld;
        } else if (checkId == R.id.beauty_box_cheek_thinning) {
            if (sFaceShape == 4)
                return sCheekThinning;
            else
                return sCheekThinningOld;
        } else if (checkId == R.id.beauty_box_intensity_chin) {
            return sIntensityChin;
        } else if (checkId == R.id.beauty_box_intensity_forehead) {
            return sIntensityForehead;
        } else if (checkId == R.id.beauty_box_intensity_nose) {
            return sIntensityNose;
        } else if (checkId == R.id.beauty_box_intensity_mouth) {
            return sIntensityMouth;
        } else {
            return 0;
        }
    }

    public static void setValue(int checkId, float value) {
        if (checkId == R.id.beauty_box_skin_detect) {
            sSkinDetect = value;

        } else if (checkId == R.id.beauty_box_heavy_blur) {
            if (sHeavyBlur == 1) {
                sHeavyBlurLevel = value;
            } else {
                sBlurLevel = value;
            }

        } else if (checkId == R.id.beauty_box_blur_level) {
            sHeavyBlurLevel = value;

        } else if (checkId == R.id.beauty_box_color_level) {
            sColorLevel = value;

        } else if (checkId == R.id.beauty_box_red_level) {
            sRedLevel = value;

        } else if (checkId == R.id.beauty_box_eye_bright) {
            sEyeBright = value;

        } else if (checkId == R.id.beauty_box_tooth_whiten) {
            sToothWhiten = value;

        } else if (checkId == R.id.beauty_box_face_shape) {
            sFaceShape = value;

        } else if (checkId == R.id.beauty_box_eye_enlarge) {
            if (sFaceShape == 4)
                sEyeEnlarging = value;
            else
                sEyeEnlargingOld = value;

        } else if (checkId == R.id.beauty_box_cheek_thinning) {
            if (sFaceShape == 4)
                sCheekThinning = value;
            else
                sCheekThinningOld = value;

        } else if (checkId == R.id.beauty_box_intensity_chin) {
            sIntensityChin = value;

        } else if (checkId == R.id.beauty_box_intensity_forehead) {
            sIntensityForehead = value;

        } else if (checkId == R.id.beauty_box_intensity_nose) {
            sIntensityNose = value;

        } else if (checkId == R.id.beauty_box_intensity_mouth) {
            sIntensityMouth = value;

        }
    }

    public static void setHeavyBlur(boolean isHeavy) {
        sHeavyBlur = isHeavy ? 1.0f : 0;
    }
}
