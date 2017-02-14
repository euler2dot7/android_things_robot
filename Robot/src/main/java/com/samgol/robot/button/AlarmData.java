package com.samgol.robot.button;

import com.samgol.robot.Inputs.InputData;

import static com.samgol.robot.Inputs.InputType.HALT;

/**
 * Created by x on 1/30/17.
 */

public class AlarmData extends InputData {
    public AlarmData() {
        super(HALT);
        defStringRep += "_" + HALT.name();
    }
}
