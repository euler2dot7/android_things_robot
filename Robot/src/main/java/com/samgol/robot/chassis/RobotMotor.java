package com.samgol.robot.chassis;

public interface RobotMotor extends AutoCloseable {
    void cw(int speedPercentage);

    void ccw(int speedPercentage);

    void halt();

}