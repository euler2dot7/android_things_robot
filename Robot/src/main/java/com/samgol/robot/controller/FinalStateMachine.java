package com.samgol.robot.controller;

import android.support.annotation.StringDef;
import android.util.Log;

import com.samgol.robot.camera.ServoCamera;
import com.samgol.robot.chassis.RobotСhassis;
import com.samgol.robot.Inputs.InputData;
import com.samgol.robot.Inputs.InputDataObserver;
import com.samgol.robot.Inputs.InputType;
import com.samgol.robot.ledrgb.LedRGB;

import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static com.samgol.robot.Inputs.InputKb.CAM_COMPLETED;
import static com.samgol.robot.Inputs.InputKb.CMD_BACKWARD;
import static com.samgol.robot.Inputs.InputKb.CMD_CAM_DOWN;
import static com.samgol.robot.Inputs.InputKb.CMD_CAM_SHOT;
import static com.samgol.robot.Inputs.InputKb.CMD_CAM_UP;
import static com.samgol.robot.Inputs.InputKb.CMD_FORWARD;
import static com.samgol.robot.Inputs.InputKb.CMD_LEFT;
import static com.samgol.robot.Inputs.InputKb.CMD_RIGHT;
import static com.samgol.robot.Inputs.InputKb.CMD_STOP;
import static com.samgol.robot.Inputs.InputKb.HALT;
import static com.samgol.robot.Inputs.InputKb.OBS_CLOSE;
import static com.samgol.robot.Inputs.InputKb.OBS_FAR;
import static com.samgol.robot.Inputs.InputKb.OBS_NEAR;
import static com.samgol.robot.Inputs.InputType.OBSTACLE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by x on 1/8/17.
 */

public class FinalStateMachine implements InputDataObserver {

    private static final int spd = 30;
    private static final String TAG = FinalStateMachine.class.getSimpleName();
    private RobotСhassis chassis;
    private ServoCamera camera;
    private LedRGB ledRGB;
    private BlockingQueue<InputData> eventsQueue = new ArrayBlockingQueue<>(1000);
    private List<InputData> eventLog = new ArrayList<>();
    private boolean run = true;

    @Retention(SOURCE)
    @StringDef({OBSTACLE_FAR, OBSTACLE_CLOSE, SHOT_IN_PROGRESS})

    private @interface RobotState {
    }

    private static final String OBSTACLE_FAR = "OBSTACLE_FAR";
    private static final String OBSTACLE_CLOSE = "OBSTACLE_CLOSE";
    private static final String SHOT_IN_PROGRESS = "SHOT_IN_PROGRESS";

    @RobotState
    private String state = OBSTACLE_FAR;

    public FinalStateMachine(RobotСhassis сhassis, ServoCamera servoCamera, LedRGB ledRGB) {
        this.chassis = сhassis;
        this.camera = servoCamera;
        this.ledRGB = ledRGB;
        if (ledRGB != null) {
            ledRGB.turnAll(false);
            ledRGB.turnGreen(true);
        }
        сhassis.stop();
        reactor.start();
    }

    @Override
    public void onInput(InputData Input) {
        Log.d(TAG, "Event: " + Input.getDefStringRep());
        try {
            eventsQueue.put(Input);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Thread reactor = new Thread(() -> {
        while (run) {
            try {
                InputData evn = eventsQueue.take();

                if (evn.getInputType() == OBSTACLE) {
                    cleanLogByType(evn.getInputType());
                    eventLog.add(evn);
                }

                if (evn.eq(HALT))
                    chassis.stop();

                switch (state) {
                    case OBSTACLE_FAR:
                        if (evn.eq(OBS_CLOSE)) {
                            chassis.stop();
                            gotoS(OBSTACLE_CLOSE);
                        } else if (evn.eq(CMD_STOP)) {
                            chassis.stop();
                        } else if (evn.eq(CMD_FORWARD)) {
                            chassis.forward(spd);
                        } else if (evn.eq(CMD_BACKWARD)) {
                            chassis.backward(spd);
                        } else if (evn.eq(CMD_LEFT)) {
                            chassis.left(spd);
                        } else if (evn.eq(CMD_RIGHT)) {
                            chassis.right(spd);
                        } else if (evn.eq(CMD_CAM_SHOT)) {
                            if (camera != null) {
                                chassis.stop();
                                camera.takeAshot();
                                gotoS(SHOT_IN_PROGRESS);
                            }
                        }
                        cameraPosition(evn);
                        break;

                    case OBSTACLE_CLOSE:
                        cameraPosition(evn);
                        if (evn.eq(CMD_BACKWARD))
                            chassis.backward(spd);
                        else if (evn.eq(OBS_FAR) || evn.eq(OBS_NEAR))
                            gotoS(OBSTACLE_FAR);
                        else
                            chassis.stop();
                        break;

                    case SHOT_IN_PROGRESS:
                        if (evn.eq(CAM_COMPLETED)) {
                            InputData inD = getLastByType(OBSTACLE);
                            if (OBS_CLOSE.eq(inD)) {
                                gotoS(OBSTACLE_CLOSE);
                            } else {
                                gotoS(OBSTACLE_FAR);
                            }
                        }
                        break;
                    default:
                        break;
                }
            } catch (InterruptedException e) {

            }
        }
    });

    private void cameraPosition(InputData evn) {
        if (camera != null) {

            if (evn.eq(CMD_CAM_DOWN)) {
                camera.down();
            } else if (evn.eq(CMD_CAM_UP)) {
                camera.up();
            }
        } else {
            if (evn.eq(CMD_CAM_DOWN)) {
                Log.d(TAG, "cameraPosition: camera is null " + evn.getDefStringRep());
            } else if (evn.eq(CMD_CAM_UP)) {
                Log.d(TAG, "cameraPosition: camera is null " + evn.getDefStringRep());
            }
        }
    }

    private void gotoS(@RobotState String state) {
        if (ledRGB != null) {
            ledRGB.turnAll(false);
            switch (state) {
                case OBSTACLE_CLOSE:
                    ledRGB.turnRed(true);
                    break;
                case OBSTACLE_FAR:
                    ledRGB.turnGreen(true);
                    break;
                default:
                    ledRGB.turnBlue(true);
                    break;
            }
        }
        this.state = state;
        Log.d(TAG, "GOTO State: " + state);
    }

    private void cleanLogByType(InputType type) {
        eventLog.removeIf(e -> e.getInputType() == type);
    }

    private InputData getLastByType(InputType type) {
        return eventLog.stream().filter(e -> e.getInputType() == type).findFirst().orElse(null);
    }

}
