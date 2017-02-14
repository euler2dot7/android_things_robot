package com.samgol.robot.remoteControl;

/**
 * Created by x on 1/31/17.
 */

public interface ImageConsumer {
    void onBase64Image(String msg);

    void onMsg(String msg);
}
