package com.samgol.robot;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.android.things.pio.PeripheralManagerService;
import com.samgol.robot.Inputs.command.CommandSource;
import com.samgol.robot.button.AlarmButton;
import com.samgol.robot.camera.ServoCamera;
import com.samgol.robot.chassis.DcMotorWithSpeed;
import com.samgol.robot.chassis.RobotChassisMotorBased;
import com.samgol.robot.chassis.RobotMotor;
import com.samgol.robot.chassis.RobotСhassis;
import com.samgol.robot.compass.RotationSensor;
import com.samgol.robot.controller.FinalStateMachine;
import com.samgol.robot.lcd.RobotLcd;
import com.samgol.robot.ledrgb.LedRGB;
import com.samgol.robot.obstacle.ObstacleSensor;
import com.samgol.robot.remoteControl.Server;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class RobotActivity extends Activity {
    private static final String TAG = RobotActivity.class.getSimpleName();
    //TODO check pins
    //Right motor
    private static final String GPIO_R_CW = "BCM27";
    private static final String GPIO_R_CCW = "BCM23";

    //Left motor
    private static final String GPIO_L_CW = "BCM17";
    private static final String GPIO_L_CCW = "BCM25";

    //HC-SR04
    private static final String GPIO_TRIG = "BCM26";
    private static final String GPIO_ECHO = "BCM19";

    //RGB Led
    private static final String GPIO_R = "BCM20";
    private static final String GPIO_G = "BCM16";
    private static final String GPIO_B = "BCM21";

    //Alarm button
    private static final String GPIO_HALT = "BCM5";
    private static final String GPIO_HALT_VCC = "BCM6";

    //Servo camera
    private static final String GPIO_PWM_SERVO = "PWM0";

    private static final String ROBOT_I2C = "I2C1";

    private PeripheralManagerService service;
    private RobotСhassis robotСhassis;

    private ServoCamera servoCamera;
    private Server server;
    private ObstacleSensor obstacleDetector;
    private AlarmButton alarmButton;
    private LedRGB ledRGB;
    private RobotLcd mRobotLcd;

    private Handler mHandler;
    private HandlerThread mHandlerThread;

    private void initHandler() {
        mHandlerThread = new HandlerThread("test thread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    Runnable pause = () -> {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };
    RotationSensor rotationSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot);
        initHandler();

        try {
            mRobotLcd = new RobotLcd(this, ROBOT_I2C);
        } catch (IOException e) {
            e.printStackTrace();
        }

        service = new PeripheralManagerService();
//        System.out.println(service.getGpioList());
        try {
            RobotMotor leftMotor = new DcMotorWithSpeed(GPIO_L_CW, GPIO_L_CCW, service, 17);
            RobotMotor rightMotor = new DcMotorWithSpeed(GPIO_R_CW, GPIO_R_CCW, service);
            robotСhassis = new RobotChassisMotorBased(leftMotor, rightMotor);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            ledRGB = new LedRGB(GPIO_R, GPIO_G, GPIO_B, service);
        } catch (IOException e) {
            e.printStackTrace();
            ledRGB = null;
        }
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "No permission");
            return;
        }

        try {
            servoCamera = new ServoCamera(GPIO_PWM_SERVO, this);

        } catch (IOException e) {
            e.printStackTrace();
        }

        FinalStateMachine robotBrain = new FinalStateMachine(robotСhassis, servoCamera, ledRGB);

        CommandSource commandSource = new CommandSource();
        commandSource.addObserver(robotBrain);
        server = new Server(8080, commandSource, getAssets());

        if (servoCamera != null) {
            servoCamera.setImageConsumer(server);
            servoCamera.addObserver(robotBrain);
        }

        try {
            server.start();
            Log.d(TAG, "onCreate: Http server started");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            obstacleDetector = new ObstacleSensor(service.openGpio(GPIO_ECHO), service.openGpio(GPIO_TRIG));
            obstacleDetector.addObserver(robotBrain);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            alarmButton = new AlarmButton(GPIO_HALT, GPIO_HALT_VCC, service);
            alarmButton.addObserver(robotBrain);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        server.stop();
        try {
            robotСhassis.close();
        } catch (Exception e) {
            Log.w(TAG, "Exception: " + e.getMessage());
        }

        try {
            ledRGB.close();
        } catch (Exception e) {
            Log.w(TAG, "Exception: " + e.getMessage());
        }
        try {
            servoCamera.close();
        } catch (Exception e) {
            Log.w(TAG, "Exception: " + e.getMessage());
        }
        try {
            obstacleDetector.close();
        } catch (Exception e) {
            Log.w(TAG, "Exception: " + e.getMessage());
        }

        try {
            alarmButton.close();
        } catch (Exception e) {
            Log.w(TAG, "Exception: " + e.getMessage());
        }
        try {
            mRobotLcd.close();
        } catch (Exception e) {
            Log.w(TAG, "Exception: " + e.getMessage());
        }

        mHandlerThread.quitSafely();

        super.onDestroy();
    }

//    private void  setUpWifi(String ssid, String key){
//        WifiConfiguration wifiConfig = new WifiConfiguration();
//        wifiConfig.SSID = String.format("\"%s\"", ssid);
//        wifiConfig.preSharedKey = String.format("\"%s\"", key);
//
//        WifiManager wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);
////remember id
//        int netId = wifiManager.addNetwork(wifiConfig);
//        wifiManager.disconnect();
//        wifiManager.enableNetwork(netId, true);
//        wifiManager.reconnect();
//    }
//    private String getWifiAddress() {
//        WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
//        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
//        int ip = wifiInfo.getIpAddress();
//        String ipAddress = Formatter.formatIpAddress(ip);
//        return ipAddress;
//    }


}
//        try {
//            rotationSensor = new RotationSensor(ROBOT_I2C);
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    for (int i = 0; i < 1000; i++) {
//                        try {
//                            int[] vals = rotationSensor.getMagnitudeXY();
//                            Log.d(TAG, "run: " + Arrays.toString(vals));
//                            JSONObject object = new JSONObject();
//                            object.put("x", vals[0]);
//                            object.put("y", vals[1]);
//                            server.onMsg(object.toString());
//                            TimeUnit.SECONDS.sleep(1);
//                        } catch (Exception e) {
//                            Log.e(TAG, "Rotation: " + e.getMessage());
//                        }
//                    }
//                }
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
