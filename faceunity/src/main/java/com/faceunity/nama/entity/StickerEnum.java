package com.faceunity.nama.entity;

import com.faceunity.nama.R;

import java.util.ArrayList;

/**
 * 道具贴纸列表
 *
 * @author Richie on 2019.12.20
 */
public enum StickerEnum {
    /**
     * 道具贴纸
     */
    STICKER_none(R.drawable.ic_delete_all, "", "none"),
    STICKER_sdlu(R.drawable.sdlu, "effect/sdlu.bundle", "sdlu"),
    STICKER_daisypig(R.drawable.daisypig, "effect/daisypig.bundle", "daisypig"),
    STICKER_fashi(R.drawable.fashi, "effect/fashi.bundle", "fashi"),
    STICKER_xueqiu_lm_fu(R.drawable.xueqiu_lm_fu, "effect/xueqiu_lm_fu.bundle", "xueqiu_lm_fu"),
    STICKER_wobushi(R.drawable.wobushi, "effect/wobushi.bundle", "wobushi"),
    STICKER_gaoshiqing(R.drawable.gaoshiqing, "effect/gaoshiqing.bundle", "gaoshiqing");

    private int iconId;
    private String filePath;
    private String description;

    StickerEnum(int iconId, String filePath, String description) {
        this.iconId = iconId;
        this.filePath = filePath;
        this.description = description;
    }

    public Sticker create() {
        return new Sticker(iconId, filePath, description);
    }

    public static ArrayList<Sticker> getEffects() {
        StickerEnum[] effectEnums = StickerEnum.values();
        ArrayList<Sticker> effects = new ArrayList<>(effectEnums.length);
        for (StickerEnum e : effectEnums) {
            effects.add(e.create());
        }
        return effects;
    }
}
