package com.faceunity.nama;

import com.faceunity.nama.utils.FuDeviceUtils;

import java.io.File;

public class FUConfig {
    public static final String BLACK_LIST = "config" + File.separator + "blackList.json";;
    //设备等级默认为中级
    public static int DEVICE_LEVEL = FuDeviceUtils.DEVICE_LEVEL_TWO;
}
