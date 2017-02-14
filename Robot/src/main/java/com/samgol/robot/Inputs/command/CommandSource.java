package com.samgol.robot.Inputs.command;

import android.support.annotation.StringDef;

import com.samgol.robot.Inputs.InputSource;
import com.samgol.robot.remoteControl.CommandListener;

import java.lang.annotation.Retention;

import static com.samgol.robot.Inputs.InputKb.CMD_BACKWARD;
import static com.samgol.robot.Inputs.InputKb.CMD_CAM_DOWN;
import static com.samgol.robot.Inputs.InputKb.CMD_CAM_SHOT;
import static com.samgol.robot.Inputs.InputKb.CMD_CAM_UP;
import static com.samgol.robot.Inputs.InputKb.CMD_FORWARD;
import static com.samgol.robot.Inputs.InputKb.CMD_LEFT;
import static com.samgol.robot.Inputs.InputKb.CMD_RIGHT;
import static com.samgol.robot.Inputs.InputKb.CMD_STOP;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by x on 1/8/17.
 */


public class CommandSource extends InputSource<Command> implements CommandListener {

    @Retention(SOURCE)
    @StringDef({STOP, FORWARD, BACKWARD, LEFT, RIGHT, CAM_UP, CAM_DOWN, TAKE_A_SHOT})

    public @interface CommandType {
    }

    public static final String STOP = "STOP";
    public static final String FORWARD = "FORWARD";
    public static final String BACKWARD = "BACKWARD";
    public static final String LEFT = "LEFT";
    public static final String RIGHT = "RIGHT";
    public static final String CAM_UP = "CAM_UP";
    public static final String CAM_DOWN = "CAM_DOWN";
    public static final String TAKE_A_SHOT = "TAKE_A_SHOT";

    @Override
    public void onCommand(String command) {
        command = command.toLowerCase().trim();
//        System.out.println("command = " + command);

        switch (command) {
            case "w":
                onInput(CMD_FORWARD);
                break;
            case "s":
                onInput(CMD_BACKWARD);
                break;
            case "a":
                onInput(CMD_LEFT);
                break;
            case "d":
                onInput(CMD_RIGHT);
                break;
            case "j":
                onInput(CMD_CAM_DOWN);
                break;
            case "k":
                onInput(CMD_CAM_UP);
                break;
            case "p":
                onInput(CMD_CAM_SHOT);
                break;
            default:
                onInput(CMD_STOP);
                break;
        }
    }
}
