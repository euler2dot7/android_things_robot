package com.samgol.robot.ledrgb;

import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

import static com.google.android.things.pio.Gpio.DIRECTION_OUT_INITIALLY_LOW;

/**
 * Created by x on 1/31/17.
 */

public class LedRGB implements AutoCloseable {
    private static final String TAG = LedRGB.class.getSimpleName();
    private Gpio mLedRed;
    private Gpio mLedGreen;
    private Gpio mLedBlue;
    private PeripheralManagerService service;

    public LedRGB(String gpioR, String gpioG, String gpioB, PeripheralManagerService service) throws IOException {
        this.service = service;

        mLedRed = service.openGpio(gpioR);
        mLedRed.setDirection(DIRECTION_OUT_INITIALLY_LOW);
        mLedGreen = service.openGpio(gpioG);
        mLedGreen.setDirection(DIRECTION_OUT_INITIALLY_LOW);
        mLedBlue = service.openGpio(gpioB);
        mLedBlue.setDirection(DIRECTION_OUT_INITIALLY_LOW);
    }

    public void turnRed(boolean on) {
        try {
            mLedRed.setValue(on);
        } catch (IOException e) {
            Log.e(TAG, "turnRed: ", e);
        }
    }

    public void turnBlue(boolean on) {
        try {
            mLedBlue.setValue(on);
        } catch (IOException e) {
            Log.e(TAG, "turnBlue: ", e);
        }
    }

    public void turnGreen(boolean on) {
        try {
            mLedGreen.setValue(on);
        } catch (IOException e) {
            Log.e(TAG, "turnGreen: ", e);
        }
    }

    public void turnAll(boolean on) {
        try {
            mLedRed.setValue(on);
            mLedGreen.setValue(on);
            mLedBlue.setValue(on);
        } catch (IOException e) {
            Log.e(TAG, "turnAll: ", e);
        }
    }

    @Override
    public void close() throws Exception {
        mLedRed.close();
        mLedBlue.close();
        mLedGreen.close();
    }
}
