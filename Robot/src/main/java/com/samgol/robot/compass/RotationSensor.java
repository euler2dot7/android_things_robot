package com.samgol.robot.compass;

import java.io.IOException;

/**
 * Created by x on 2/11/17.
 */

public class RotationSensor {
    private HMC5883L hmc5883L;

    private String i2cBus;
    private int offsetX, offsetY;
    private static final double DEFAULT_SCALE = 0.92;

    private RotationSensor(String i2cBus, int offsetX, int getOffsetY) throws IOException {
        this.i2cBus = i2cBus;
        this.offsetX = offsetX;
        this.offsetY = getOffsetY;
        hmc5883L = new HMC5883L(this.i2cBus);
    }

    public RotationSensor(String i2cBus) throws IOException {
        this(i2cBus, 0, 0);
    }


    public int getYaw() throws IOException {
        double magnitudeX = hmc5883L.getMagnitudeX() * DEFAULT_SCALE + offsetX;
        double magnitudeY = hmc5883L.getMagnitudeY() * DEFAULT_SCALE + offsetY;
        double atan = Math.atan2(magnitudeX, magnitudeY);
        return (int) Math.toDegrees(atan < 0 ? atan + 2 * Math.PI : atan);
    }

    public int[] getMagnitudeXY() throws IOException {
        double magnitudeX = hmc5883L.getMagnitudeX() * DEFAULT_SCALE + offsetX;
        double magnitudeY = hmc5883L.getMagnitudeY() * DEFAULT_SCALE + offsetY;
        return new int[]{(int) magnitudeX, (int) magnitudeY};
    }

}
