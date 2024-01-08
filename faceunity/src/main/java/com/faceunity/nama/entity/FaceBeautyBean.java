package com.faceunity.nama.entity;

/**
 * DESC：美颜
 * Created on 2021/4/26
 */
public class FaceBeautyBean {

    private String key;//名称标识
    private int desRes;//描述
    private int closeRes;//图片
    private int openRes;//图片
    private boolean canUseFunction = true;

    private String relevanceKey;//关联的名称标识
    private boolean showRadioButton = false;
    private boolean enableRadioButton;
    private int leftRadioButtonDesRes;
    private int rightRadioButtonDesRes;
    private int enableRadioButtonDesRes;

    public FaceBeautyBean(String key, int desRes, int closeRes, int openRes) {
        this.key = key;
        this.desRes = desRes;
        this.closeRes = closeRes;
        this.openRes = openRes;
        this.canUseFunction = true;
    }

    public FaceBeautyBean(String key, int desRes, int closeRes, int openRes, boolean canUseFunction) {
        this.key = key;
        this.desRes = desRes;
        this.closeRes = closeRes;
        this.openRes = openRes;
        this.canUseFunction = canUseFunction;
    }

    public FaceBeautyBean(String key, int desRes, int closeRes, int openRes, boolean canUseFunction, String relevanceKey, boolean showRadioButton, boolean enableRadioButton, int leftRadioButtonDesRes, int rightRadioButtonDesRes, int enableRadioButtonDesRes) {
        this.key = key;
        this.desRes = desRes;
        this.closeRes = closeRes;
        this.openRes = openRes;
        this.canUseFunction = canUseFunction;
        this.relevanceKey = relevanceKey;
        this.showRadioButton = showRadioButton;
        this.enableRadioButton = enableRadioButton;
        this.leftRadioButtonDesRes = leftRadioButtonDesRes;
        this.rightRadioButtonDesRes = rightRadioButtonDesRes;
        this.enableRadioButtonDesRes = enableRadioButtonDesRes;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getDesRes() {
        return desRes;
    }

    public void setDesRes(int desRes) {
        this.desRes = desRes;
    }

    public int getCloseRes() {
        return closeRes;
    }

    public void setCloseRes(int closeRes) {
        this.closeRes = closeRes;
    }

    public int getOpenRes() {
        return openRes;
    }

    public void setOpenRes(int openRes) {
        this.openRes = openRes;
    }

    public boolean isCanUseFunction() {
        return canUseFunction;
    }

    public void setCanUseFunction(boolean canUseFunction) {
        this.canUseFunction = canUseFunction;
    }

    public boolean isShowRadioButton() {
        return showRadioButton;
    }

    public void setShowRadioButton(boolean showRadioButton) {
        this.showRadioButton = showRadioButton;
    }

    public boolean isEnableRadioButton() {
        return enableRadioButton;
    }

    public void setEnableRadioButton(boolean enableRadioButton) {
        this.enableRadioButton = enableRadioButton;
    }

    public int getLeftRadioButtonDesRes() {
        return leftRadioButtonDesRes;
    }

    public void setLeftRadioButtonDesRes(int leftRadioButtonDesRes) {
        this.leftRadioButtonDesRes = leftRadioButtonDesRes;
    }

    public int getRightRadioButtonDesRes() {
        return rightRadioButtonDesRes;
    }

    public void setRightRadioButtonDesRes(int rightRadioButtonDesRes) {
        this.rightRadioButtonDesRes = rightRadioButtonDesRes;
    }

    public int getEnableRadioButtonDesRes() {
        return enableRadioButtonDesRes;
    }

    public void setEnableRadioButtonDesRes(int enableRadioButtonDesRes) {
        this.enableRadioButtonDesRes = enableRadioButtonDesRes;
    }

    public String getRelevanceKey() {
        return relevanceKey;
    }

    public void setRelevanceKey(String relevanceKey) {
        this.relevanceKey = relevanceKey;
    }
}
