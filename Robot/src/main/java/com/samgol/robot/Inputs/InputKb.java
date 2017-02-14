package com.samgol.robot.Inputs;

import com.samgol.robot.Inputs.command.Command;
import com.samgol.robot.button.AlarmData;
import com.samgol.robot.camera.CameraInputData;
import com.samgol.robot.obstacle.ObstacleInput;

import static com.samgol.robot.Inputs.command.CommandSource.BACKWARD;
import static com.samgol.robot.Inputs.command.CommandSource.CAM_DOWN;
import static com.samgol.robot.Inputs.command.CommandSource.CAM_UP;
import static com.samgol.robot.Inputs.command.CommandSource.FORWARD;
import static com.samgol.robot.Inputs.command.CommandSource.LEFT;
import static com.samgol.robot.Inputs.command.CommandSource.RIGHT;
import static com.samgol.robot.Inputs.command.CommandSource.STOP;
import static com.samgol.robot.Inputs.command.CommandSource.TAKE_A_SHOT;
import static com.samgol.robot.camera.CamInputType.SHOT_COMPLETED;
import static com.samgol.robot.obstacle.ObstacleInputDataType.CLOSE;
import static com.samgol.robot.obstacle.ObstacleInputDataType.FAR;
import static com.samgol.robot.obstacle.ObstacleInputDataType.NEAR;

/**
 * Created by x on 1/8/17.
 */

public class InputKb {

    public static final ObstacleInput OBS_CLOSE = new ObstacleInput(CLOSE);
    public static final ObstacleInput OBS_NEAR = new ObstacleInput(NEAR);
    public static final ObstacleInput OBS_FAR = new ObstacleInput(FAR);

    public static final Command CMD_STOP = new Command(STOP);
    public static final Command CMD_FORWARD = new Command(FORWARD);
    public static final Command CMD_BACKWARD = new Command(BACKWARD);
    public static final Command CMD_LEFT = new Command(LEFT);
    public static final Command CMD_RIGHT = new Command(RIGHT);
    public static final Command CMD_CAM_UP = new Command(CAM_UP);
    public static final Command CMD_CAM_DOWN = new Command(CAM_DOWN);
    public static final Command CMD_CAM_SHOT = new Command(TAKE_A_SHOT);

    public static final CameraInputData CAM_COMPLETED = new CameraInputData(SHOT_COMPLETED);
    public static final AlarmData HALT = new AlarmData();
}
