package com.faceunity.nama.module;

import com.faceunity.nama.entity.Sticker;

/**
 * @author Richie on 2020.07.07
 */
public interface IStickerModule {
    /**
     * 选择贴纸
     *
     * @param sticker
     */
    void selectSticker(Sticker sticker);

    /**
     * 设置参数
     *
     * @param key
     * @param value
     */
    void setItemParam(String key, Object value);
}
