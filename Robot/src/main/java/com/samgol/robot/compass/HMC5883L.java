/*
 * Copyright 2016 Cagdas Caglak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.samgol.robot.compass;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

/**
 * Created by cagdas on 24.12.2016.
 */

public class HMC5883L implements AutoCloseable {
    private static final int HMC5883L_DEV_ADD = 0x1E;
    private static final int CONFIG_A = 0x00;
    private static final int CONFIG_B = 0x01;
    private static final int MODE_REG = 0x02;
    private static final int X_HIGH = 0x03;
    private static final int X_LOW = 0x04;
    private static final int Z_HIGH = 0x05;
    private static final int Z_LOW = 0x06;
    private static final int Y_HIGH = 0x07;
    private static final int Y_LOW = 0x08;
    private static final int STATUS_REG = 0x09;
    private static final int ID_REG_A = 0x0A;
    private static final int ID_REG_B = 0x0B;
    private static final int ID_REG_C = 0x0C;

    private static final int SAMPLES_AVARAGE_START = 0x05;
    private static final int SAMPLES_AVARAGE_LENGTH = 0x02;
    private static final int SAMPLES_AVARAGE_1 = 0x00;
    private static final int SAMPLES_AVARAGE_2 = 0x01;
    private static final int SAMPLES_AVARAGE_4 = 0x02;
    private static final int SAMPLES_AVARAGE_8 = 0x03;

    private static final int OUTPUT_RATE_START = 0x02;
    private static final int OUTPUT_RATE_LENGTH = 0x03;
    private static final int OUTPUT_RATE_0 = 0x00;
    private static final int OUTPUT_RATE_1 = 0x01;
    private static final int OUTPUT_RATE_2 = 0x02;
    private static final int OUTPUT_RATE_3 = 0x03;
    private static final int OUTPUT_RATE_4 = 0x04;
    private static final int OUTPUT_RATE_5 = 0x05;
    private static final int OUTPUT_RATE_6 = 0x06;
    private static final int OUTPUT_RATE_7 = 0x07;

    private static final int MEASUREMENT_START = 0x00;
    private static final int MEASUREMENT_LENGTH = 0x02;
    private static final int MEASUREMENT_NORMAL = 0x00;
    private static final int MEASUREMENT_POSITIVE = 0x01;
    private static final int MEASUREMENT_NEGATIVE = 0x02;

    private static final int GAIN_START = 0x05;
    private static final int GAIN_LENGTH = 0x03;
    private static final int GAIN_1370 = 0x00;
    private static final int GAIN_1090 = 0x01;
    private static final int GAIN_820 = 0x02;
    private static final int GAIN_660 = 0x03;
    private static final int GAIN_440 = 0x04;
    private static final int GAIN_390 = 0x05;
    private static final int GAIN_330 = 0x06;
    private static final int GAIN_230 = 0x07;

    private static final int OPERATION_MODE_START = 0x00;
    private static final int OPERATION_MODE_LENGTH = 0x02;
    private static final int OPERATION_MODE_CONT = 0x00;
    private static final int OPERATION_MODE_SINGLE = 0x01;
    private static final int OPERATION_MODE_IDLE = 0x02;

    private static final int RDY_BIT = 0x00;
    private static final int LOCK_BIT = 0x01;

    private I2cDevice mDevice;

    public HMC5883L(String bus) throws IOException {
        PeripheralManagerService peripheralManagerService = new PeripheralManagerService();
        I2cDevice device = peripheralManagerService.openI2cDevice(bus, HMC5883L_DEV_ADD);
        try {
            connect(device);
        } catch (IOException | RuntimeException e) {
            try {
                close();
            } catch (IOException | RuntimeException ignored) {
            }
            throw e;
        }
    }

    @Override
    public void close() throws IOException {
        if (this.mDevice != null) {
            this.mDevice.close();
            this.mDevice = null;
        }
    }

    private void connect(I2cDevice device) throws IOException {
        this.mDevice = device;
        setSamplesAvarage(SAMPLES_AVARAGE_8);
        setOutputRate(OUTPUT_RATE_4);
        setMeasurementMode(MEASUREMENT_NORMAL);
        setMeasurementGain(GAIN_1090);
        setOperationMode(OPERATION_MODE_CONT);
    }

    public void setSamplesAvarage(int avarage) throws IOException {
        int value = this.mDevice.readRegByte(CONFIG_A) & 0xff;
        int bits = 1;
        int i = 0;
        while (i < SAMPLES_AVARAGE_LENGTH - 1) {
            bits = (bits << 1);
            ++bits;
            ++i;
        }
        value &= ~(bits << SAMPLES_AVARAGE_START);
        if (avarage == 0) {
            value |= (SAMPLES_AVARAGE_1 << SAMPLES_AVARAGE_START);
        } else if (avarage == 1) {
            value |= (SAMPLES_AVARAGE_2 << SAMPLES_AVARAGE_START);
        } else if (avarage == 2) {
            value |= (SAMPLES_AVARAGE_4 << SAMPLES_AVARAGE_START);
        } else if (avarage == 3) {
            value |= (SAMPLES_AVARAGE_8 << SAMPLES_AVARAGE_START);
        }
        this.mDevice.writeRegByte(CONFIG_A, (byte) value);
    }

    public int getSamplesAvarage() throws IOException {
        int value = this.mDevice.readRegByte(CONFIG_A) & 0xff;
        return (int) ((value >> SAMPLES_AVARAGE_START) % Math.pow(2, SAMPLES_AVARAGE_LENGTH));
    }

    public void setOutputRate(int rate) throws IOException {
        int value = this.mDevice.readRegByte(CONFIG_A) & 0xff;
        int bits = 1;
        int i = 0;
        while (i < OUTPUT_RATE_LENGTH - 1) {
            bits = (bits << 1);
            ++bits;
            ++i;
        }
        value &= ~(bits << OUTPUT_RATE_START);
        if (rate == 0) {
            value |= (SAMPLES_AVARAGE_8 << SAMPLES_AVARAGE_START);
        } else if (rate == 1) {
            value |= (OUTPUT_RATE_1 << OUTPUT_RATE_START);
        } else if (rate == 2) {
            value |= (OUTPUT_RATE_2 << OUTPUT_RATE_START);
        } else if (rate == 3) {
            value |= (OUTPUT_RATE_3 << OUTPUT_RATE_START);
        } else if (rate == 4) {
            value |= (OUTPUT_RATE_4 << OUTPUT_RATE_START);
        } else if (rate == 5) {
            value |= (OUTPUT_RATE_5 << OUTPUT_RATE_START);
        } else if (rate == 6) {
            value |= (OUTPUT_RATE_6 << OUTPUT_RATE_START);
        } else if (rate == 7) {
            System.out.println("Reserved");
        }
        this.mDevice.writeRegByte(CONFIG_A, (byte) value);
    }

    public int getOutputRate() throws IOException {
        int value = this.mDevice.readRegByte(CONFIG_A) & 0xff;
        return (int) ((value >> OUTPUT_RATE_START) % Math.pow(2, OUTPUT_RATE_LENGTH));
    }

    public void setMeasurementMode(int mode) throws IOException {
        int value = this.mDevice.readRegByte(CONFIG_A) & 0xff;
        int bits = 1;
        int i = 0;
        while (i < MEASUREMENT_LENGTH - 1) {
            bits = (bits << 1);
            ++bits;
            ++i;
        }
        value &= ~(bits << MEASUREMENT_START);
        if (mode == 0) {
            value |= (MEASUREMENT_NORMAL << MEASUREMENT_START);
        } else if (mode == 2) {
            value |= (MEASUREMENT_POSITIVE << MEASUREMENT_START);
        } else if (mode == 3) {
            value |= (MEASUREMENT_NEGATIVE << MEASUREMENT_START);
        }
        this.mDevice.writeRegByte(CONFIG_A, (byte) value);
    }

    public int getMeasurementMode() throws IOException {
        int value = this.mDevice.readRegByte(CONFIG_A) & 0xff;
        return (int) ((value >> MEASUREMENT_START) % Math.pow(2, MEASUREMENT_LENGTH));
    }

    public void setMeasurementGain(int gain) throws IOException {
        if (gain == 0) {
            this.mDevice.writeRegByte(CONFIG_B, (byte) (GAIN_1370 << 5));
        } else if (gain == 1) {
            this.mDevice.writeRegByte(CONFIG_B, (byte) (GAIN_1090 << 5));
        } else if (gain == 2) {
            this.mDevice.writeRegByte(CONFIG_B, (byte) (GAIN_820 << 5));
        } else if (gain == 3) {
            this.mDevice.writeRegByte(CONFIG_B, (byte) (GAIN_660 << 5));
        } else if (gain == 4) {
            this.mDevice.writeRegByte(CONFIG_B, (byte) (GAIN_440 << 5));
        } else if (gain == 5) {
            this.mDevice.writeRegByte(CONFIG_B, (byte) (GAIN_390 << 5));
        } else if (gain == 6) {
            this.mDevice.writeRegByte(CONFIG_B, (byte) (GAIN_330 << 5));
        } else if (gain == 7) {
            this.mDevice.writeRegByte(CONFIG_B, (byte) (GAIN_230 << 5));
        }
    }

    public int getMeasurementGain() throws IOException {
        int value = this.mDevice.readRegByte(CONFIG_B) & 0xff;
        return (int) ((value >> GAIN_START) % Math.pow(2, GAIN_LENGTH));
    }

    public void setOperationMode(int mode) throws IOException {
        if (mode == 0) {
            this.mDevice.writeRegByte(MODE_REG, (byte) OPERATION_MODE_CONT);
        } else if (mode == 1) {
            this.mDevice.writeRegByte(MODE_REG, (byte) OPERATION_MODE_SINGLE);
        } else if (mode == 2) {
            this.mDevice.writeRegByte(MODE_REG, (byte) OPERATION_MODE_IDLE);
        }
    }

    public int getOperationMode() throws IOException {
        int value = this.mDevice.readRegByte(MODE_REG) & 0xff;
        return (int) ((value >> OPERATION_MODE_START) % Math.pow(2, OPERATION_MODE_LENGTH));
    }


    public int getMagnitudeX() throws IOException {
        int lsb = this.mDevice.readRegByte(X_LOW) & 0xff;
        int msb = this.mDevice.readRegByte(X_HIGH);

        return (msb << 8 | lsb);
    }
    
    public int getMagnitudeY() throws IOException {
        int lsb = this.mDevice.readRegByte(Y_LOW) & 0xff;
        int msb = this.mDevice.readRegByte(Y_HIGH);

        return (msb << 8 | lsb);
    }
    
    public int getMagnitudeZ() throws IOException {
        int lsb = this.mDevice.readRegByte(Z_LOW) & 0xff;
        int msb = this.mDevice.readRegByte(Z_HIGH);

        return (msb << 8 | lsb);
    }

    public float[] getMagnitudes() throws IOException {
        float[] values = new float[3];
        values[0] = getMagnitudeX();
        values[1] = getMagnitudeY();
        values[2] = getMagnitudeZ();
        return values;
    }
}
