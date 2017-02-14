package com.samgol.robot.chassis;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public class RobotChassisMotorBased implements RobotСhassis {
    private RobotMotor leftMotor, rightMotor;
    private ExecutorService cmdExec = Executors.newSingleThreadExecutor();


    @Retention(SOURCE)
    @IntDef({FORWARD, BACKWARD, LEFT, RIGHT, STOP})
    private @interface LastCommand {
    }

    private static final int STOP = 0;
    private static final int BACKWARD = 1;
    private static final int FORWARD = 2;
    private static final int LEFT = 3;
    private static final int RIGHT = 4;

    @LastCommand
    private int lastCommand = 0;

    private Runnable pause = () -> {
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };

    public RobotChassisMotorBased(RobotMotor leftMotor, RobotMotor rightMotor) {
        this.leftMotor = leftMotor;
        this.rightMotor = rightMotor;
        stop();
    }


    @Override
    public void forward(int speed) {
        lastCommand = FORWARD;
        cmdExec.execute(() -> {
            leftMotor.ccw(speed);
            rightMotor.cw(speed);
        });
    }

    @Override
    public void backward(int speed) {
        lastCommand = BACKWARD;
        cmdExec.execute(() -> {
            leftMotor.cw(speed);
            rightMotor.ccw(speed);
        });
    }

    @Override
    public void left(int speed) {
        switch (lastCommand) {
            case BACKWARD:
                simpleLeft(speed);
                cmdExec.execute(pause);
                backward(speed);
                break;
            case FORWARD:
                simpleLeft(speed);
                cmdExec.execute(pause);
                forward(speed);
                break;
            default:
                lastCommand = LEFT;
                simpleLeft(speed);
                break;
        }
    }

    private void simpleLeft(int speed) {
        cmdExec.execute(() -> {
            leftMotor.cw(speed);
            rightMotor.cw(speed);
        });

    }

    @Override
    public void right(int speed) {
        switch (lastCommand) {
            case BACKWARD:
                simpleRight(speed);
                cmdExec.execute(pause);
                backward(speed);
                break;
            case FORWARD:
                simpleRight(speed);
                cmdExec.execute(pause);
                forward(speed);
                break;
            default:
                lastCommand = RIGHT;
                simpleRight(speed);
                break;
        }
    }

    private void simpleRight(int speed) {
        cmdExec.execute(() -> {
            leftMotor.ccw(speed);
            rightMotor.ccw(speed);
        });
    }

    @Override
    public void stop() {
        lastCommand = STOP;
        cmdExec.execute(() -> {
            leftMotor.halt();
            rightMotor.halt();
        });
    }

    @Override
    public void close() throws Exception {
        stop();
        leftMotor.close();
        rightMotor.close();
        cmdExec.shutdown();
    }

}
