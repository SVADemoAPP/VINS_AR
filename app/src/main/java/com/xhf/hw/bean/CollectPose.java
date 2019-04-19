package com.xhf.hw.bean;

import java.io.Serializable;

public class CollectPose implements Serializable {
    private float poseX;
    private float poseY;
    private float poseZ;

    public CollectPose() {
    }

    public CollectPose(float poseX, float poseY, float poseZ) {
        this.poseX = poseX;
        this.poseY = poseY;
        this.poseZ = poseZ;
    }

    public float getPoseX() {
        return poseX;
    }

    public void setPoseX(float poseX) {
        this.poseX = poseX;
    }

    public float getPoseY() {
        return poseY;
    }

    public void setPoseY(float poseY) {
        this.poseY = poseY;
    }

    public float getPoseZ() {
        return poseZ;
    }

    public void setPoseZ(float poseZ) {
        this.poseZ = poseZ;
    }

    @Override
    public String toString() {
        return "CollectPose{" +
                "poseX=" + poseX +
                ", poseY=" + poseY +
                ", poseZ=" + poseZ +
                '}';
    }
}
