package com.samgol.robot.obstacle;

import com.samgol.robot.Inputs.InputType;
import com.samgol.robot.Inputs.InputData;

/**
 * Created by x on 1/8/17.
 */

public class ObstacleInput extends InputData {
    public ObstacleInput(ObstacleInputDataType type) {
        super(InputType.OBSTACLE);
        defStringRep += "_" + type.name();
    }
}
