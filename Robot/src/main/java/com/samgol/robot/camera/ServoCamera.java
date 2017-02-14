package com.samgol.robot.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Base64;
import android.util.Log;

import com.google.android.things.contrib.driver.pwmservo.Servo;
import com.samgol.robot.Inputs.InputSource;
import com.samgol.robot.remoteControl.ImageConsumer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static com.samgol.robot.Inputs.InputKb.CAM_COMPLETED;

/**
 * Created by x on 1/30/17.
 */

public class ServoCamera extends InputSource<CameraInputData> implements AutoCloseable {
    private static final String TAG = ServoCamera.class.getSimpleName();
    private final String PWM_PIN;
    private String imageStr;

    private Servo mServo;
    private RobotCamera mCamera;
    private Handler mWebHandler;
    private HandlerThread mWebThread;
    private ImageConsumer imageConsumer;
    private Handler mCameraHandler;
    private HandlerThread mCameraThread;

    private static final int INC_ANGLE = 5;

    public ServoCamera(String PWM_PIN, Context context) throws IOException {
        this.PWM_PIN = PWM_PIN;
        mServo = new Servo("PWM0");
        mServo.setPulseDurationRange(0.6, 2.5); // according to your servo's specifications
        mServo.setAngleRange(0, 180);       // according to your servo's specifications
        mServo.setEnabled(true);
        mServo.setAngle(90);

        mWebThread = new HandlerThread("WebThread");
        mWebThread.start();
        mWebHandler = new Handler(mWebThread.getLooper());

        mCameraThread = new HandlerThread("Camera Thread");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());

        RobotCamera robotCamera = RobotCamera.getInstance();
        robotCamera.initializeCamera(context, mCameraHandler, mOnImageAvailableListener);
        this.mCamera = robotCamera;
    }

    public void setImageConsumer(ImageConsumer imageConsumer) {
        this.imageConsumer = imageConsumer;
    }

    public void up() {
        try {
            mServo.setAngle(mServo.getAngle() >= 175 ? mServo.getAngle() : mServo.getAngle() + INC_ANGLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void center() {
        try {
            mServo.setAngle(90);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void down() {
        try {
            mServo.setAngle(mServo.getAngle() <= 5 ? mServo.getAngle() : mServo.getAngle() - INC_ANGLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void takeAshot() {
        mWebHandler.post(mCamera::takePicture);
//        mCamera.takePicture();
    }

    private ImageReader.OnImageAvailableListener mOnImageAvailableListener =
            reader -> {
                Image image = reader.acquireLatestImage();
                // get image bytes
                ByteBuffer imageBuf = image.getPlanes()[0].getBuffer();
                final byte[] imageBytes = new byte[imageBuf.remaining()];
                imageBuf.get(imageBytes);
                image.close();

                onPictureTaken(imageBytes);
            };


    private void onPictureTaken(final byte[] imageBytes) {
        Log.d(TAG, "onPictureTaken: imageBytes: " + imageBytes.length);
        if (imageBytes != null) {
            Bitmap shot = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            shot.compress(Bitmap.CompressFormat.JPEG, 70, bos);
            imageStr = Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP);

            mWebHandler.post(() -> {
                Log.d(TAG, "sending image " + imageStr.length() + " bytes ");
                // annotate image by uploading to Cloud Vision API
                try {
                    Log.d(TAG, "run: " + imageStr);
                    Log.d(TAG, "start_sending");
                    imageConsumer.onBase64Image(imageStr);
                } catch (Exception e) {
                }
                Log.d(TAG, "finish_sending");
                onInput(CAM_COMPLETED);
            });
        }
    }

    @Override
    public void close() throws Exception {
        mCamera.shutDown();
//        mCameraThread.quitSafely();
        mWebThread.quitSafely();
        try {
            mServo.close();
        } catch (IOException e) {
            Log.e(TAG, "button driver error", e);
        }
    }
}
