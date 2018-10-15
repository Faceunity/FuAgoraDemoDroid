package com.faceunity.utils;

import com.faceunity.R;
import com.faceunity.entity.Effect;

import java.util.ArrayList;

/**
 * Created by tujh on 2018/1/30.
 */

public enum EffectEnum {

    EffectNone("none", R.drawable.ic_delete_all, "none", 4, Effect.EFFECT_TYPE_NONE, 0),

    Effect_fengya_ztt_fu("fengya_ztt_fu", R.drawable.fengya_ztt_fu, "normal/fengya_ztt_fu.bundle", 4, Effect.EFFECT_TYPE_NORMAL, 0),
    Effect_hudie_lm_fu("hudie_lm_fu", R.drawable.hudie_lm_fu, "normal/hudie_lm_fu.bundle", 4, Effect.EFFECT_TYPE_NORMAL, 0),
    Effect_touhua_ztt_fu("touhua_ztt_fu", R.drawable.touhua_ztt_fu, "normal/touhua_ztt_fu.bundle", 4, Effect.EFFECT_TYPE_NORMAL, 0),
    Effect_juanhuzi_lm_fu("juanhuzi_lm_fu", R.drawable.juanhuzi_lm_fu, "normal/juanhuzi_lm_fu.bundle", 4, Effect.EFFECT_TYPE_NORMAL, 0),
    Effect_mask_hat("mask_hat", R.drawable.mask_hat, "normal/mask_hat.bundle", 4, Effect.EFFECT_TYPE_NORMAL, 0),
    Effect_yazui("yazui", R.drawable.yazui, "normal/yazui.bundle", 4, Effect.EFFECT_TYPE_NORMAL, 0),
    Effect_yuguan("yuguan", R.drawable.yuguan, "normal/yuguan.bundle", 4, Effect.EFFECT_TYPE_NORMAL, 0);

    private String bundleName;
    private int resId;
    private String path;
    private int maxFace;
    private int effectType;
    private int description;

    EffectEnum(String name, int resId, String path, int maxFace, int effectType, int description) {
        this.bundleName = name;
        this.resId = resId;
        this.path = path;
        this.maxFace = maxFace;
        this.effectType = effectType;
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

    public int maxFace() {
        return maxFace;
    }

    public int effectType() {
        return effectType;
    }

    public int description() {
        return description;
    }

    public Effect effect() {
        return new Effect(bundleName, resId, path, maxFace, effectType, description);
    }

    public static ArrayList<Effect> getEffectsByEffectType() {
        ArrayList<Effect> effects = new ArrayList<>();
        for (EffectEnum e : EffectEnum.values()) {
            effects.add(e.effect());
        }
        return effects;
    }
}
