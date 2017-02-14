package vaf.vishal.hcsr04;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by vishal on 18/12/16.
 * Driver for HC-SRC04 Ultrasonic sensor.
 */

public class Hcsr04 implements AutoCloseable {

    private static final String TAG = Hcsr04.class.getSimpleName();
    private Gpio trigGpio, echoGpio;
    private Handler handler = new Handler();
    private static final int pauseInMicro = 10;

    private long startTime, ellapsedTime;
    private float distanceInCm;

    public Hcsr04(String trigPin, String echoPin) throws IOException {
        try {
            PeripheralManagerService service = new PeripheralManagerService();
            trigGpio = service.openGpio(trigPin);
            echoGpio = service.openGpio(echoPin);
            configureGpio(trigGpio, echoGpio);
        } catch (IOException e) {
            throw e;
        }
    }

    @Override
    public void close() throws Exception {
        handler.removeCallbacks(startTrigger);
        try {
            trigGpio.close();
            echoGpio.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void configureGpio(Gpio trigGpio, Gpio echoGpio) throws IOException {
        try {
            trigGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            echoGpio.setDirection(Gpio.DIRECTION_IN);

            trigGpio.setActiveType(Gpio.ACTIVE_HIGH);
            echoGpio.setActiveType(Gpio.ACTIVE_HIGH);
            echoGpio.setEdgeTriggerType(Gpio.EDGE_BOTH);
            handler.post(startTrigger);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Runnable startTrigger = new Runnable() {
        @Override
        public void run() {
            try {
                trigGpio.setValue(!trigGpio.getValue());
                busyWaitMicros(pauseInMicro);
                trigGpio.setValue(!trigGpio.getValue());
                while (!echoGpio.getValue())
                    startTime = System.nanoTime();
                while (echoGpio.getValue())
                    ellapsedTime = System.nanoTime() - startTime;
                ellapsedTime = TimeUnit.NANOSECONDS.toMicros(ellapsedTime);
                distanceInCm = ellapsedTime / 58;
                handler.postDelayed(startTrigger, 60);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };


    public float[] getProximityDistance() {
        return new float[]{distanceInCm};
    }

//    private GpioCallback gpioCallback = new GpioCallback() {
//        @Override
//        public boolean onGpioEdge(Gpio gpio) {
//            try {
//                if (!gpio.getValue()) {
//                    startTime = SystemClock.elapsedRealtime();
//                } else {
//                    ellapsedTime = SystemClock.elapsedRealtime() - startTime;
//                    getProximityDistance();
//                }
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            return true;
//        }
//
//        @Override
//        public void onGpioError(Gpio gpio, int error) {
//            Log.d("ERROR", "GPIO CALLBACK ERROR");
//        }
//    };

    public static void busyWaitMicros(long micros) {
        long waitUntil = System.nanoTime() + (micros * 1_000);
        while (waitUntil > System.nanoTime()) {
            try {
                TimeUnit.MICROSECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
