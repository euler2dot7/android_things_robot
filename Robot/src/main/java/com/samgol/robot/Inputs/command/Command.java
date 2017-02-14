package com.samgol.robot.Inputs.command;

import com.samgol.robot.Inputs.InputData;
import com.samgol.robot.Inputs.InputType;

/**
 * Created by x on 1/8/17.
 */

public class Command extends InputData {
    public Command(@CommandSource.CommandType String commandType) {
        super(InputType.COMMAND);
        defStringRep += "_" + commandType;
    }
}
