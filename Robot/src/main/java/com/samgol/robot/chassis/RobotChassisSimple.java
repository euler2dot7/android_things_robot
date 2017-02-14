package com.samgol.robot.chassis;

import android.support.annotation.StringDef;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.util.concurrent.TimeUnit;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by x on 2/9/17.
 */

public class RobotChassisSimple implements RobotСhassis {

    private Gpio lCw;
    private Gpio lCcw;
    private Gpio rCw;
    private Gpio rCcw;
    private boolean run = true;

    public RobotChassisSimple(String gpioLCw, String gpioLCcw, String gpioRCw, String gpioRCcw, PeripheralManagerService service) throws IOException {

        lCw = service.openGpio(gpioLCw);
        lCw.setActiveType(Gpio.ACTIVE_HIGH);
        lCw.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

        lCcw = service.openGpio(gpioLCcw);
        lCcw.setActiveType(Gpio.ACTIVE_HIGH);
        lCcw.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

        rCw = service.openGpio(gpioRCw);
        rCw.setActiveType(Gpio.ACTIVE_HIGH);
        rCw.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

        rCcw = service.openGpio(gpioRCcw);
        rCcw.setActiveType(Gpio.ACTIVE_HIGH);
        rCcw.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

        chassisThread.start();
    }

    @Retention(SOURCE)
    @StringDef({STOP, FORWARD, BACKWARD, LEFT, RIGHT})

    private @interface ICommand {
    }

    private static final String STOP = "STOP";
    private static final String FORWARD = "FORWARD";
    private static final String BACKWARD = "BACKWARD";
    private static final String LEFT = "LEFT";
    private static final String RIGHT = "RIGHT";

    @ICommand
    private String cmd = STOP;

    private int speed;
    private static final int PERIOD = 100;

    Thread chassisThread = new Thread(new Runnable() {
        @Override
        public void run() {
            int sleepTime;
            @ICommand String curCmd;
            while (run) {
                sleepTime = speed;
                curCmd = cmd;
                try {
                    if (sleepTime == 0 || curCmd.equalsIgnoreCase(STOP)) {
                        lCcw.setValue(false);
                        lCw.setValue(false);
                        rCcw.setValue(false);
                        rCw.setValue(false);
                        TimeUnit.MICROSECONDS.sleep(PERIOD);
                    } else if (sleepTime == PERIOD) {
                        switch (curCmd) {
                            case FORWARD:
                                lCcw.setValue(true);
                                lCw.setValue(false);
                                rCcw.setValue(false);
                                rCw.setValue(true);
                                break;
                            case BACKWARD:
                                lCcw.setValue(false);
                                lCw.setValue(true);
                                rCcw.setValue(true);
                                rCw.setValue(false);
                                break;
                            case LEFT:
                                lCcw.setValue(false);
                                lCw.setValue(true);
                                rCcw.setValue(false);
                                rCw.setValue(true);
                                break;
                            case RIGHT:
                                lCcw.setValue(true);
                                lCw.setValue(false);
                                rCcw.setValue(true);
                                rCw.setValue(false);
                                break;
                            default:
                                break;
                        }
                        TimeUnit.MICROSECONDS.sleep(PERIOD);
                    } else {
                        switch (curCmd) {
                            case FORWARD:
                                lCcw.setValue(true);
                                lCw.setValue(false);
                                rCcw.setValue(false);
                                rCw.setValue(true);
                                break;
                            case BACKWARD:
                                lCcw.setValue(false);
                                lCw.setValue(true);
                                rCcw.setValue(true);
                                rCw.setValue(false);
                                break;
                            case LEFT:
                                lCcw.setValue(false);
                                lCw.setValue(true);
                                rCcw.setValue(false);
                                rCw.setValue(true);
                                break;
                            case RIGHT:
                                lCcw.setValue(true);
                                lCw.setValue(false);
                                rCcw.setValue(true);
                                rCw.setValue(false);
                                break;
                            default:
                                break;
                        }
                        TimeUnit.MICROSECONDS.sleep(sleepTime);
                        lCcw.setValue(false);
                        lCw.setValue(false);
                        rCcw.setValue(false);
                        rCw.setValue(false);
                        TimeUnit.MICROSECONDS.sleep(PERIOD - sleepTime);
                    }

                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    @Override
    public synchronized void forward(int speed) {
        cmd = FORWARD;
        this.speed = speed;
    }

    @Override
    public synchronized void backward(int speed) {
        cmd = BACKWARD;
        this.speed = speed;
    }

    @Override
    public synchronized void left(int speed) {
        cmd = LEFT;
        this.speed = speed;

    }

    @Override
    public synchronized void right(int speed) {
        cmd = RIGHT;
        this.speed = speed;

    }

    @Override
    public synchronized void stop() {
        cmd = STOP;
        this.speed = 0;
    }

    @Override
    public void close() throws Exception {
        run = false;
        chassisThread.interrupt();
    }
}
