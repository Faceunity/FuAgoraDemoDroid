package com.faceunity.utils;

import com.faceunity.entity.Makeup;

import java.util.ArrayList;

/**
 * Created by tujh on 2018/1/30.
 */

public enum MakeupEnum {
    MakeupNone("", 0, "", Makeup.MAKEUP_TYPE_NONE, 0);


    private String bundleName;
    private int resId;
    private String path;
    private int makeupType;
    private int description;

    MakeupEnum(String name, int resId, String path, int makeupType, int description) {
        this.bundleName = name;
        this.resId = resId;
        this.path = path;
        this.makeupType = makeupType;
        this.description = description;
    }

    public String bundleName() {
        return bundleName;
    }

    public int resId() {
        return resId;
    }

    public String path() {
        return path;
    }

    public int makeupType() {
        return makeupType;
    }

    public int description() {
        return description;
    }

    public Makeup makeup() {
        return new Makeup(bundleName, resId, path, makeupType, description);
    }

    public static ArrayList<Makeup> getMakeupsByMakeupType(int makeupType) {
        ArrayList<Makeup> Makeups = new ArrayList<>();
        for (MakeupEnum e : MakeupEnum.values()) {
            if (e.makeupType == makeupType) {
                Makeups.add(e.makeup());
            }
        }
        return Makeups;
    }
}
