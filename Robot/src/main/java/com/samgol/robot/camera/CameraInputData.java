package com.samgol.robot.camera;

import com.samgol.robot.Inputs.InputData;

import static com.samgol.robot.Inputs.InputType.CAMERA;

/**
 * Created by x on 1/30/17.
 */

public class CameraInputData extends InputData {
    public CameraInputData(CamInputType type) {
        super(CAMERA);
        defStringRep += "_" + type.name();
    }
}
