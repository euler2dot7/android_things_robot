package com.samgol.robot.chassis;

import android.support.annotation.IntRange;

public interface RobotСhassis extends AutoCloseable {

    void forward(@IntRange(from = 0, to = 100) int speed);

    void backward(@IntRange(from = 0, to = 100) int speed);

    void left(@IntRange(from = 0, to = 100) int speed);

    void right(@IntRange(from = 0, to = 100) int speed);

    void stop();

}