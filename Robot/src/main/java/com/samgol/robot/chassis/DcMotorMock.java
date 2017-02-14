package com.samgol.robot.chassis;

import android.util.Log;

/**
 * Created by x on 2/7/17.
 */

public class DcMotorMock implements RobotMotor {
    private final String TAG;
    private String name;

    public DcMotorMock(String name) {
        this.name = name;
        TAG = name + " " + DcMotorMock.class.getSimpleName();
    }

    @Override
    public void cw(int speedPercentage) {
        Log.d(TAG, "cw() called with: speedPercentage = [" + speedPercentage + "]");
    }

    @Override
    public void ccw(int speedPercentage) {
        Log.d(TAG, "ccw() called with: speedPercentage = [" + speedPercentage + "]");
    }

    @Override
    public void halt() {
        Log.d(TAG, "halt() called");
    }

    @Override
    public void close() throws Exception {
        Log.d(TAG, "close() called");
    }
}
