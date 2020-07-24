package com.faceunity.nama.entity;

import com.faceunity.nama.R;

import java.util.ArrayList;

/**
 * 美妆列表
 *
 * @author Richie on 2020.06.20
 */
public enum MakeupEnum {
    /**
     * 减龄，暖冬，红枫，Rose，少女
     */
    MAKEUP_NONE(R.drawable.makeup_none_normal, "卸妆", "", false),
    MAKEUP_JIANLING(R.drawable.demo_combination_age, "减龄", "makeup/jianling.bundle", false),
    MAKEUP_NUANDONG(R.drawable.demo_combination_warm_winter, "暖冬", "makeup/nuandong.bundle", false),
    MAKEUP_HONGFENG(R.drawable.demo_combination_red_maple, "红枫", "makeup/hongfeng.bundle", false),
    MAKEUP_ROSE(R.drawable.demo_combination_rose, "Rose", "makeup/rose.bundle", true),
    MAKEUP_SHAONV(R.drawable.demo_combination_girl, "少女", "makeup/shaonv.bundle", false);

    private int iconId;
    private String name;
    private String filePath;
    private boolean isNeedFlipPoints;

    MakeupEnum(int iconId, String name, String filePath, boolean isNeedFlipPoints) {
        this.iconId = iconId;
        this.name = name;
        this.filePath = filePath;
        this.isNeedFlipPoints = isNeedFlipPoints;
    }

    public Makeup create() {
        return new Makeup(iconId, name, filePath, isNeedFlipPoints);
    }

    public static ArrayList<Makeup> getMakeupEntities() {
        MakeupEnum[] values = MakeupEnum.values();
        ArrayList<Makeup> makeupEntities = new ArrayList<>(values.length);
        for (MakeupEnum value : values) {
            makeupEntities.add(value.create());
        }
        return makeupEntities;
    }

}
