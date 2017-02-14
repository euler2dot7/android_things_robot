package com.samgol.robot.lcd;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.android.things.contrib.driver.ssd1306.Ssd1306;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by x on 1/31/17.
 */

public class RobotLcd implements AutoCloseable {
    private static final String TAG = RobotLcd.class.getSimpleName();
    private Handler mLcdHandler;
    private HandlerThread mLcdHandlerThread;
    private Ssd1306 mScreen;
    private Context context;
    private String cur_ip = " ";

    public RobotLcd(Context context, String i2cBusAddress) throws IOException {
        Log.d(TAG, "RobotLcd() called with: context, i2cBusAddress = [" + i2cBusAddress + "]");
        mLcdHandlerThread = new HandlerThread("Lcd thread");
        mLcdHandlerThread.start();
        mLcdHandler = new Handler(mLcdHandlerThread.getLooper());
        mScreen = new Ssd1306(i2cBusAddress);
        this.context = context.getApplicationContext();

        drawStringCentered("STARTED", Font.FONT_5X8, 10, true);
        try {
            mScreen.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        showWifiInfo();
    }


    @Override
    public void close() throws Exception {
        mLcdHandlerThread.quitSafely();
        mScreen.close();
    }

    private void showWifiInfo() {
        mLcdHandler.post(
                new Runnable() {
                    @Override
                    public void run() {
                        String ip = wifiIpAddress(context);

                        if (ip != null && !cur_ip.equalsIgnoreCase(ip)) {
                            cur_ip = ip;
                            Log.d(TAG, "run() called ip: " + ip);
                            mScreen.clearPixels();
                            drawStringCentered("ANDROID THINGS", Font.FONT_5X8, 0, true);
                            drawStringCentered("ROBOT", Font.FONT_5X8, 10, true);
//                            Fonts.drawString(mScreen, 0, 0, "Android Things", Fonts.Type.fontAcme5Outlines);
//                            Fonts.drawString(mScreen, 0, 20, "ROBOT " + ip, Fonts.Type.fontAcme5Outlines);
//                            drawString("ip: ", Font.FONT_5X8, 10, 30, true);
//                            drawString(ip, Font.FONT_4X5, 35, 32, true);
                            Fonts.drawString(mScreen, 10, 30, "IP: " + ip, Fonts.Type.font5x5);
                            Fonts.drawString(mScreen, 25, 50, " :(){ :|:& };:", Fonts.Type.font5x5);
//                            drawStringCentered(ip, Font.FONT_5X8, 40, true);
                            try {
                                mScreen.show();
//                                mScreen.startScroll(0, 50, Ssd1306.ScrollMode.RightHorizontal);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else if (ip == null) {
                            Log.d(TAG, "run() called ip is null");
                            mScreen.clearPixels();

                            drawStringCentered("ANDROID THINGS", Font.FONT_5X8, 0, true);
                            drawStringCentered("ROBOT", Font.FONT_5X8, 10, true);
                            drawStringCentered("no ip address", Font.FONT_5X8, 30, true);
                            try {
                                mScreen.show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        mLcdHandler.postDelayed(this, 10000);
                    }
                });
    }


    private String wifiIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            Log.e(TAG, "Unable to get host address.");
            ipAddressString = null;
        }

        return ipAddressString;
    }

    private synchronized void drawString(String string, Font font, int x, int y, boolean on) {
        int posX = x;
        int posY = y;
        for (char c : string.toCharArray()) {
            if (c == '\n') {
                posY += font.getOutterHeight();
                posX = x;
            } else {
                if (posX >= 0 && posX + font.getWidth() < mScreen.getLcdWidth()
                        && posY >= 0 && posY + font.getHeight() < mScreen.getLcdHeight()) {
                    font.drawChar(mScreen, c, posX, posY, on);
                }
                posX += font.getOutterWidth();
            }
        }
    }

    private synchronized void drawStringCentered(String string, Font font, int y, boolean on) {
        final int strSizeX = string.length() * font.getOutterWidth();
        final int x = (mScreen.getLcdWidth() - strSizeX) / 2;
        drawString(string, font, x, y, on);

    }
}
