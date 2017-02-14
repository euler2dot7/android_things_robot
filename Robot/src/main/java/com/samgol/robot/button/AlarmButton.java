package com.samgol.robot.button;

import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;
import com.samgol.robot.Inputs.InputSource;

import java.io.IOException;

import static com.samgol.robot.Inputs.InputKb.HALT;

/**
 * Created by x on 1/30/17.
 */

public class AlarmButton extends InputSource<AlarmData> implements AutoCloseable {

    private static final String TAG = AlarmButton.class.getSimpleName();
    private Gpio mButtonGpio;
    private Gpio mVccGpio;
    private PeripheralManagerService service;
    private long last_click;

    public AlarmButton(String btnGpio, String vccGpio, PeripheralManagerService service) throws IOException {
        this(btnGpio, service);
        mVccGpio = service.openGpio(vccGpio);
        mVccGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_HIGH);
    }

    public AlarmButton(String btnGpio, PeripheralManagerService service) throws IOException {
        this.service = service;
        mButtonGpio = service.openGpio(btnGpio);
        mButtonGpio.setDirection(Gpio.DIRECTION_IN);
        mButtonGpio.setEdgeTriggerType(Gpio.EDGE_RISING);
        mButtonGpio.registerGpioCallback(mCallback);
    }

    private void halt() {
        onInput(HALT);
    }

    private GpioCallback mCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            long click_time = System.currentTimeMillis();
            if (click_time - last_click > 500) {
                Log.i(TAG, "GPIO changed, button pressed");
                halt();
            }
            last_click = click_time;
            return true;
        }
    };

    @Override
    public void close() throws Exception {
        mButtonGpio.close();
        mVccGpio.close();
    }
}

