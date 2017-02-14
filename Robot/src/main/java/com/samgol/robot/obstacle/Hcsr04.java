package com.samgol.robot.obstacle;


import com.google.android.things.pio.Gpio;

import java.io.IOException;

/**
 * DistanceMonitor class to monitor distance measured by sensor
 *
 * @author Rutger Claes <rutger.claes@cs.kuleuven.be>
 */
public class Hcsr04 {

    private final static float SOUND_SPEED = 340.29f; // speed of sound in m/s
    private final static int TRIG_DURATION_IN_MICROS = 10; // trigger duration
    private final static int TIMEOUT = 2100;

    private final Gpio echoPin;
    private final Gpio trigPin;

    public Hcsr04(Gpio echoPin, Gpio trigPin) throws IOException {
        this.echoPin = echoPin;
        this.trigPin = trigPin;
        trigPin.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        echoPin.setDirection(Gpio.DIRECTION_IN);
    }

    /*
     * This method returns the distance measured by the sensor in cm
     *
     * @throws TimeoutException if a timeout occurs
     */
    public synchronized float measureDistance() throws Exception {
        triggerSensor();
        waitForSignal();
        long duration = measureSignal();
        return duration * SOUND_SPEED / 20000F;
    }

    /**
     * Put a high on the trig pin for TRIG_DURATION_IN_MICROS
     */
    private void triggerSensor() {
        try {
            trigPin.setValue(true);
            Thread.sleep(0, TRIG_DURATION_IN_MICROS * 1000);
            trigPin.setValue(false);
        } catch (InterruptedException | IOException ex) {

        }
    }

    /**
     * Wait for a high on the echo pin
     */
    private void waitForSignal() throws Exception {
        int countdown = 10;

        while (!echoPin.getValue() && countdown > 0) {
            countdown--;
        }

        if (countdown <= 0) {
            throw new Exception("Timeout waiting for signal start");
        }
    }

    /**
     * @return the duration of the signal in micro seconds
     * @throws Exception
     */
    private long measureSignal() throws Exception {
        int countdown = TIMEOUT*10;
        long start = System.nanoTime();
        while (echoPin.getValue() && countdown > 0) {
            countdown--;
        }
        long end = System.nanoTime();

        if (countdown <= 0) {
            throw new Exception("Timeout waiting for signal end");
        }

        return (long) Math.ceil((end - start) / 1000.0); // Return micro seconds
    }

}
