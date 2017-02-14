package com.samgol.robot.chassis;

import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

public class DcMotorOnOff implements RobotMotor {

    private static final String TAG = DcMotorOnOff.class.getSimpleName();
    private Gpio cwPin;
    private Gpio ccwPin;

    public DcMotorOnOff(Gpio gpioCw, Gpio gpioCcw) throws IOException {
        this.ccwPin = gpioCcw;
        this.cwPin = gpioCw;
    }

    public DcMotorOnOff(String gpioCw, String gpioCcw, PeripheralManagerService peripheralManagerService) throws IOException {
        cwPin = peripheralManagerService.openGpio(gpioCw);
        cwPin.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
        cwPin.setActiveType(Gpio.ACTIVE_HIGH);
        cwPin.setValue(true);
        ccwPin = peripheralManagerService.openGpio(gpioCcw);
        ccwPin.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
        ccwPin.setActiveType(Gpio.ACTIVE_HIGH);
        ccwPin.setValue(true);
        halt();
    }

    @Override
    public void cw(int speedPercentage) {
        try {
            ccwPin.setValue(false);
            cwPin.setValue(true);
            Log.d(TAG, "cw cw: " + cwPin.getValue() + " ccw: " + ccwPin.getValue());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void ccw(int speedPercentage) {
        try {
            ccwPin.setValue(true);
            cwPin.setValue(false);
            Log.d(TAG, "cw: " + cwPin.getValue());
            Log.d(TAG, "ccw cw: " + cwPin.getValue() + " ccw: " + ccwPin.getValue());
        } catch (IOException e) {

        }
    }

    @Override
    public void halt() {
        try {
            cwPin.setValue(false);
            ccwPin.setValue(false);
            Log.d(TAG, "halt cw: " + cwPin.getValue() + " ccw: " + ccwPin.getValue());
        } catch (IOException e) {
        }
    }

    @Override
    public void close() throws Exception {
        halt();
        cwPin.close();
        ccwPin.close();

    }
}
