package com.samgol.robot.obstacle;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.samgol.robot.Inputs.InputSource;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;
import static com.samgol.robot.Inputs.InputKb.OBS_CLOSE;
import static com.samgol.robot.Inputs.InputKb.OBS_FAR;
import static com.samgol.robot.Inputs.InputKb.OBS_NEAR;
import static com.samgol.robot.obstacle.ObstacleInputDataType.FAR;

/**
 * Created by x on 1/8/17.
 */

public class ObstacleSensor extends InputSource<ObstacleInput> implements AutoCloseable {
    private ObstacleInputDataType state;
    private static final int CLOSE = 10;
    private static final int NEAR = 20;
    private Gpio trig;
    private Gpio echo;
    private Hcsr04 hcsr04;

    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private Runnable pollDist;

    public ObstacleSensor(Gpio trig, Gpio echo) throws IOException {
        state = FAR;
        this.trig = trig;
        this.echo = echo;

        hcsr04 = new Hcsr04(echo, trig);
        mHandlerThread = new HandlerThread("Distance Sensor");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        pollDist = () -> {
            int count = 3;
            float d = -1;
            while (count > 0) {
                try {
                    d = hcsr04.measureDistance();
//                    Log.d(TAG, "Distance: " + d);
                    break;
                } catch (Exception e) {
                    count--;
                    Log.w(TAG, "Distance warning: " + e.getMessage());
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (InterruptedException e) {
                }
            }
            onDistance((int) d);
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {

            }
            mHandler.post(pollDist);
        };
        mHandler.post(pollDist);
    }

    private void onDistance(int distance) {
        switch (state) {
            case CLOSE:
                if (distance > CLOSE) {
                    if (distance > NEAR) {
                        state = FAR;
                        onInput(OBS_FAR);
                    } else {
                        state = ObstacleInputDataType.NEAR;
                        onInput(OBS_NEAR);
                    }
                }
                break;
            case FAR:
                if (distance < CLOSE) {
                    state = ObstacleInputDataType.CLOSE;
                    onInput(OBS_CLOSE);
                } else if (distance < NEAR) {
                    state = ObstacleInputDataType.NEAR;
                    onInput(OBS_NEAR);
                }
                break;
            case NEAR:
                if (distance < CLOSE) {
                    state = ObstacleInputDataType.CLOSE;
                    onInput(OBS_CLOSE);
                }
                if (distance > NEAR) {
                    state = ObstacleInputDataType.FAR;
                    onInput(OBS_FAR);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void close() throws Exception {
        mHandlerThread.quitSafely();

        trig.close();
        echo.close();
    }
}
