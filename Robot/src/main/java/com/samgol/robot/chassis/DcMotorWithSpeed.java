package com.samgol.robot.chassis;

import android.support.annotation.StringDef;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.util.concurrent.TimeUnit;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public class DcMotorWithSpeed implements RobotMotor, AutoCloseable {


    private static final String TAG = DcMotorWithSpeed.class.getSimpleName();
    private Gpio cw;
    private Gpio ccw;
    private boolean run = true;
    private int speedInc;

    public DcMotorWithSpeed(String gpioCw, String gpioCcw, PeripheralManagerService service, int speedDec) throws IOException {
        this(gpioCw, gpioCcw, service);
        this.speedInc = speedDec;
    }

    public DcMotorWithSpeed(String gpioCw, String gpioCcw, PeripheralManagerService service) throws IOException {
        cw = service.openGpio(gpioCw);
        cw.setActiveType(Gpio.ACTIVE_HIGH);
        cw.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

        ccw = service.openGpio(gpioCcw);
        ccw.setActiveType(Gpio.ACTIVE_HIGH);
        ccw.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        motorThread.start();
    }

    @Retention(SOURCE)
    @StringDef({HALT, CW, CCW})

    private @interface ICommand {
    }

    private static final String HALT = "HALT";
    private static final String CW = "CW";
    private static final String CCW = "CCW";

    @ICommand
    private String cmd = HALT;

    private int speed;
    private static final int PERIOD = 10;

    private Thread motorThread = new Thread(new Runnable() {
        @Override
        public void run() {
            int sleepTime;
            @ICommand String curCmd;
            while (run) {
                sleepTime = (speed + speedInc) / 10;
                curCmd = cmd;
                try {
                    if (sleepTime == 0 || curCmd.equalsIgnoreCase(HALT)) {
                        ccw.setValue(false);
                        cw.setValue(false);
                        TimeUnit.MILLISECONDS.sleep(PERIOD);
                    } else if (sleepTime == PERIOD) {
                        switch (curCmd) {
                            case CW:
                                ccw.setValue(false);
                                cw.setValue(true);
                                break;
                            case CCW:
                                ccw.setValue(true);
                                cw.setValue(false);
                                break;
                            default:
                                break;
                        }
                        TimeUnit.MILLISECONDS.sleep(PERIOD);
                    } else {
                        switch (curCmd) {
                            case CW:
                                ccw.setValue(false);
                                cw.setValue(true);
                                break;
                            case CCW:
                                ccw.setValue(true);
                                cw.setValue(false);
                                break;
                            default:
                                break;
                        }
                        TimeUnit.MILLISECONDS.sleep(sleepTime);
                        ccw.setValue(false);
                        cw.setValue(false);
                        TimeUnit.MILLISECONDS.sleep(PERIOD - sleepTime);
                    }

                } catch (IOException | InterruptedException e) {
                    Log.e(TAG, "motorThread " + e.getMessage());
                }
            }
        }
    });

    @Override
    public synchronized void cw(int speedPercentage) {
        cmd = CW;
        speed = speedPercentage;
    }

    @Override
    public synchronized void ccw(int speedPercentage) {
        cmd = CCW;
        speed = speedPercentage;
    }

    @Override
    public synchronized void halt() {
        cmd = HALT;
        speed = 0;
    }

    @Override
    public synchronized void close() throws Exception {
        halt();
        run = false;
        TimeUnit.MILLISECONDS.sleep(PERIOD);
        cw.close();
        ccw.close();

    }
}
