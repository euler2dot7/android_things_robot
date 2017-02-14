package com.samgol.robot.chassis;

import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.util.Log;

import com.samgol.robot.compass.RotationSensor;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.util.Arrays;

import static java.lang.annotation.RetentionPolicy.SOURCE;


public class RobotChassisPid implements RobotСhassis {

    private static final String TAG = RobotChassisPid.class.getSimpleName();
    private RobotMotor leftMotor, rightMotor;
    private RotationSensor rotationSensor;

    @Retention(SOURCE)
    @IntDef({FORWARD, BACKWARD, LEFT, RIGHT, STOP})
    private @interface Cmd {
    }

    private static final int STOP = 0;
    private static final int BACKWARD = 1;
    private static final int FORWARD = 2;
    private static final int LEFT = 3;
    private static final int RIGHT = 4;

    @Cmd
    private int mCmd = 0;
    private int mSpeed;

    public RobotChassisPid(RobotMotor leftMotor, RobotMotor rightMotor, RotationSensor rotationSensor) {
        this.leftMotor = leftMotor;
        this.rightMotor = rightMotor;
        this.rotationSensor = rotationSensor;
        stop();
    }

    @Override
    public void forward(@IntRange(from = 0, to = 100) int speed) {
        mCmd = FORWARD;
        this.mSpeed = speed;
        execute(mCmd, mSpeed, 0);

    }

    @Override
    public void backward(@IntRange(from = 0, to = 100) int speed) {
        mCmd = BACKWARD;
        this.mSpeed = speed;
        execute(mCmd, mSpeed, 0);
    }

    @Override
    public void left(@IntRange(from = 0, to = 100) int speed) {
        mCmd = LEFT;
        this.mSpeed = speed;
        execute(mCmd, mSpeed, 45);

    }

    @Override
    public void right(@IntRange(from = 0, to = 100) int speed) {
        mCmd = RIGHT;
        this.mSpeed = speed;
        execute(mCmd, mSpeed, 45);
    }

    @Override
    public void stop() {
        mCmd = STOP;
        execute(mCmd, 0, -1);
    }

    public void left(@IntRange(from = 0, to = 100) int speed, int angle) {
        mCmd = LEFT;
        this.mSpeed = speed;

    }

    public void right(@IntRange(from = 0, to = 100) int speed, int angle) {
        mCmd = RIGHT;
        this.mSpeed = speed;

    }


    @Override
    public void close() throws Exception {


    }

    private void execute(@Cmd int command, int speed) {
        switch (command) {

            case BACKWARD:
                leftMotor.cw(speed);
                rightMotor.ccw(speed);
                break;
            case FORWARD:
                leftMotor.ccw(speed);
                rightMotor.cw(speed);
                break;
            case LEFT:
                leftMotor.cw(speed);
                rightMotor.cw(speed);
                break;
            case RIGHT:
                leftMotor.ccw(speed);
                rightMotor.ccw(speed);
                break;
            case STOP:
                leftMotor.halt();
                rightMotor.halt();
                break;
        }
    }

    private void execute(int[] motorsSpeed) {
        Log.d(TAG, "execute: " + Arrays.toString(motorsSpeed));
        if (motorsSpeed[0] < 0)
            leftMotor.ccw(motorsSpeed[0] < -100 ? 100 : -motorsSpeed[0]);
        else leftMotor.cw(motorsSpeed[0] > 100 ? 100 : motorsSpeed[0]);
        if (motorsSpeed[1] < 0)
            rightMotor.ccw(motorsSpeed[1] < -100 ? 100 : -motorsSpeed[1]);
        else rightMotor.cw(motorsSpeed[1] > 100 ? 100 : motorsSpeed[1]);
    }

    private Thread executeControl;
    private final int P = 2, I = 1;
    private int curLSpeed, curRightSpeed;

    public void execute(@Cmd int cmd, int cSpeed, int cAngle) {
        if (executeControl != null) {
            executeControl.interrupt();
        }
        if (cAngle >= 0 && rotationSensor != null) {
            executeControl = new Thread(new Runnable() {
                private int accomulutor = 0;

                @Override
                public void run() {
                    int angle = cAngle;
                    if (cmd == BACKWARD) {
                        angle = (180 + angle) % 360;
                    }
                    int sensorAngle = 0;
                    try {
                        sensorAngle = rotationSensor.getYaw();
                        angle = (sensorAngle + angle) % 360;
                    } catch (IOException e) {
                        Log.e(TAG, "run: ", e);
                        return;
                    }

                    int wheel_speed = cSpeed;
                    if (cmd == BACKWARD) {
                        wheel_speed = -wheel_speed;
                    } else if (cmd != FORWARD) {
                        wheel_speed = 0;
                    }
                    while (true) {
                        int cur_angle = 0;
                        try {
                            cur_angle = rotationSensor.getYaw();
                        } catch (IOException e) {
                            Log.e(TAG, "run: ", e);
                        }
                        int[] motorsSpeed = getPIDDifferentialDriveWheelsCommand(angle, cur_angle, wheel_speed, P, I);

                        if (curLSpeed != motorsSpeed[0] || curRightSpeed != motorsSpeed[1]) {
                            curLSpeed = motorsSpeed[0];
                            curRightSpeed = motorsSpeed[1];
                            execute(motorsSpeed);
                        }
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            return;
                        }
                    } // while
                }

                private int[] getPIDDifferentialDriveWheelsCommand(int angle, int cur_angle, int wheel_speed, int P, int I) {
                    int[] weelsSpeed = new int[]{0, 0};
                    // cur_angle = (cur_angle + angle) % 360;
                    cur_angle = cur_angle < angle ? 360 + (cur_angle - angle) : (cur_angle - angle);
                    if (cur_angle < 2 || cur_angle > 358) {
                        weelsSpeed[0] = wheel_speed;
                        weelsSpeed[1] = wheel_speed;
                        accomulutor = 0;
                    } else {
                        // System.out.print("angle " + cur_angle + " ");

                        if (wheel_speed < 0) {
                        }
                        int correction = ((cur_angle > 180 ? (360 - cur_angle) : cur_angle) * P);
                        if (wheel_speed == 0) {
//							correction = (int) ((cur_angle > 180 ? (360 - cur_angle) : cur_angle) * P / 2);
                            correction = ((cur_angle > 180 ? (360 - cur_angle) : cur_angle) * P);
                        }

                        accomulutor = (accomulutor / I) > 100 ? 100 : accomulutor + correction;
                        // accomulutor += correction;
                        int speed_prc;
                        if (wheel_speed < 0) {
                            speed_prc = wheel_speed + correction + accomulutor / I;
                            cur_angle = (cur_angle + 180) % 360;
                        } else {
                            speed_prc = wheel_speed - correction - accomulutor / I;
                        }
                        // System.out.println(" I " + accomulutor + " \n");
                        if (cur_angle > 180) {
                            weelsSpeed[0] = wheel_speed == 0 ? speed_prc / 2 : speed_prc;
                            weelsSpeed[1] = wheel_speed == 0 ? -speed_prc / 2 : wheel_speed;
                            // System.out.println("left correction " + speed_prc);
                        } else {
                            weelsSpeed[0] = wheel_speed == 0 ? -speed_prc / 2 : wheel_speed;
                            weelsSpeed[1] = wheel_speed == 0 ? speed_prc / 2 : speed_prc;
                            // System.out.println("right correction " +  speed_prc);
                        }
                    }

                    return weelsSpeed;
                }
            });
            executeControl.start();
        } else {
            execute(mCmd, mSpeed);
            Log.d(TAG, "execute: Normal command");
        }
    }
}
