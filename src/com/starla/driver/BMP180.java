package com.starla.driver;

/**
 * Created by guillesupremacy on 12/2/2015.
 */
import ioio.lib.api.TwiMaster;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.IOIOConnectionManager.Thread;

public class BMP180 {
    private static final int BMP180_ADDRESS = 0x77; // 7-bit address
    private static final int BMP180_REG_CONTROL = 0xF4;
    private static final int BMP180_REG_RESULT = 0xF6;
    private static final int BMP180_COMMAND_TEMPERATURE = 0x2E;
    private static final int BMP180_COMMAND_PRESSURE_0 = 0x34;
    private static final int BMP180_COMMAND_PRESSURE_1 = 0x74;
    private static final int BMP180_COMMAND_PRESSURE_2 = 0xB4;
    private static final int BMP180_COMMAND_PRESSURE_3 = 0xF4;
    private static final byte[] AC1_REG_ADDRESS = {(byte) 0xAA, (byte) 0xAB}; // {MSB, LSB}
    private static final byte[] AC2_REG_ADDRESS = {(byte) 0xAC, (byte) 0xAD};
    private static final byte[] AC3_REG_ADDRESS = {(byte) 0xAE, (byte) 0xAF};
    private static final byte[] AC4_REG_ADDRESS = {(byte) 0xB0, (byte) 0xB1};
    private static final byte[] AC5_REG_ADDRESS = {(byte) 0xB2, (byte) 0xB3};
    private static final byte[] AC6_REG_ADDRESS = {(byte) 0xB4, (byte) 0xB5};
    private static final byte[] B1_REG_ADDRESS = {(byte) 0xB6, (byte) 0xB7};
    private static final byte[] B2_REG_ADDRESS = {(byte) 0xB8, (byte) 0xB9};
    private static final byte[] MB_REG_ADDRESS = {(byte) 0xBA, (byte) 0xBB};
    private static final byte[] MC_REG_ADDRESS = {(byte) 0xBC, (byte) 0xBD};
    private static final byte[] MD_REG_ADDRESS = {(byte) 0xBE, (byte) 0xBF};
    private final TwiMaster twi;
    private final boolean SEVEN_BIT_ADDRESS = false;
    private double c5;
    private double c6;
    private double mc;
    private double md;
    private double x0;
    private double x1;
    private double x2;
    private double y0;
    private double y1;
    private double y2;
    private double p0;
    private double p1;
    private double p2;

    public BMP180(TwiMaster twi) throws ConnectionLostException, InterruptedException {
        this.twi = twi;
        if (begin()) {
            System.out.println("BMP_180 is alive");
        } else {
            System.out.println("BMP_180 is dead");
        }
    }

    private boolean begin() throws ConnectionLostException, InterruptedException {
        if(readInt(AC1_REG_ADDRESS) && readInt(AC2_REG_ADDRESS) && readInt(AC3_REG_ADDRESS) &&
                readUInt(AC4_REG_ADDRESS) && readUInt(AC5_REG_ADDRESS) &&  readUInt(AC6_REG_ADDRESS) &&
                readInt(B1_REG_ADDRESS) && readInt(B2_REG_ADDRESS) && readInt(MB_REG_ADDRESS) && readInt(MC_REG_ADDRESS) && readInt(MD_REG_ADDRESS)) {
            computePolynomials();
            return true;
        }
        return false;
    }

    private void computePolynomials() throws ConnectionLostException,
            InterruptedException {
        double c3, c4, b1;

        int AC1 = getIntCoefficient(AC1_REG_ADDRESS);
        int AC2 = getIntCoefficient(AC2_REG_ADDRESS);
        int AC3 = getIntCoefficient(AC3_REG_ADDRESS);
        int AC4 = getUIntCoefficient(AC4_REG_ADDRESS);
        int AC5 = getUIntCoefficient(AC5_REG_ADDRESS);
        int AC6 = getUIntCoefficient(AC6_REG_ADDRESS);
        int VB1 = getIntCoefficient(B1_REG_ADDRESS);
        int VB2 = getIntCoefficient(B2_REG_ADDRESS);
        int MB = getIntCoefficient(MB_REG_ADDRESS);
        int MC = getIntCoefficient(MC_REG_ADDRESS);
        int MD = getIntCoefficient(MD_REG_ADDRESS);
        /*
		System.out.println("AC1 = " + AC1);
		System.out.println("AC2 = " + AC2);
		System.out.println("AC3 = " + AC3);
		System.out.println("AC4 = " + AC4);
		System.out.println("AC5 = " + AC5);
		System.out.println("AC6 = " + AC6);
		System.out.println("VB1 = " + VB1);
		System.out.println("VB2 = " + VB2);
		System.out.println("MB = " + MB);
		System.out.println("MC = " + MC);
		System.out.println("MD = " + MD);
		*/
        // Compute floating-point polynomials:
        c3 = 160.0 * Math.pow(2, -15) * AC3;
        c4 = Math.pow(10, -3) * Math.pow(2, -15) * AC4;
        b1 = Math.pow(160, 2) * Math.pow(2, -30) * VB1;
        c5 = (Math.pow(2, -15) / 160) * AC5;
        c6 = AC6;
        mc = (Math.pow(2, 11) / Math.pow(160, 2)) * MC;
        md = MD / 160.0;
        x0 = AC1;
        x1 = 160.0 * Math.pow(2, -13) * AC2;
        x2 = Math.pow(160, 2) * Math.pow(2, -25) * VB2;
        y0 = c4 * Math.pow(2, 15);
        y1 = c4 * c3;
        y2 = c4 * b1;
        p0 = (3791.0 - 8.0) / 1600.0;
        p1 = 1.0 - 7357.0 * Math.pow(2, -20);
        p2 = 3038.0 * 100.0 * Math.pow(2, -36);
		/*
		System.out.println("");
		System.out.println("c3 = " + c3);
		System.out.println("c4 = " + c4);
		System.out.println("c5 = " + c5);
		System.out.println("c6 = " + c6);
		System.out.println("b1 = " + b1);
		System.out.println("mc = " + mc);
		System.out.println("md = " + md);
		System.out.println("x0 = " + x0);
		System.out.println("x1 = " + x1);
		System.out.println("x2 = " + x2);
		System.out.println("y0 = " + y0);
		System.out.println("y1 = " + y1);
		System.out.println("y2 = " + y2);
		System.out.println("p0 = " + p0);
		System.out.println("p1 = " + p1);
		System.out.println("p2 = " + p2);
		*/
    }

    int startTemperature() throws ConnectionLostException, InterruptedException {
        int delay = 5;
        if(writeBytes(new byte[]{(byte) BMP180_REG_CONTROL, (byte) BMP180_COMMAND_TEMPERATURE})){ //write 0x2E int reg 0xF4
            return delay;
        }
        return 0;
    }

    public double getTemperature() throws ConnectionLostException, InterruptedException {
        double tu, a, t;
        Thread.sleep(startTemperature());

        byte[] response = readBytes(new byte[]{(byte) BMP180_REG_RESULT, (byte) 0xF7}); // read reg 0xF6 (MSB), 0xF7 (LSB)

        tu = ((int) response[0] * 256) + (int) response[1];
        a = c5 * (tu - c6);
        t = a + (mc / (a + md));

        return t;
    }

    int startPressure(Oversampling oversampling) throws ConnectionLostException, InterruptedException {
        byte[] request = new byte[2];
        request[0] = (byte) BMP180_REG_CONTROL;
        int delay;

        switch (oversampling) {
            case VERY_LOW_RES:
                request[1] = BMP180_COMMAND_PRESSURE_0;
                delay = 5; // delay
                break;
            case LOW_RES:
                request[1] = BMP180_COMMAND_PRESSURE_1;
                delay = 8;
                break;
            case MEDIUM_RES:
                request[1] = (byte) BMP180_COMMAND_PRESSURE_2;
                delay = 14;
                break;
            case HIGH_RES:
                request[1] = (byte) BMP180_COMMAND_PRESSURE_3;
                delay = 26;
                break;
            default:
                request[1] = BMP180_COMMAND_PRESSURE_1;
                delay = 8;
                break;
        }

        if(writeBytes(request)){ // write BMP180_COMMAND_PRESSURE_X into reg 0xF4
            return delay;
        }

        return 0;
    }

    public double getPressure(double temperature, Oversampling oversampling) throws ConnectionLostException, InterruptedException {
        double pu, s, x, y, z, pressure;
        Thread.sleep(startPressure(oversampling)); // delay

        byte[] response = readBytes(new byte[]{(byte) BMP180_REG_RESULT, (byte) 0xF7, (byte) 0xF8});

        pu = Byte.toUnsignedInt(response[0])*256.0 + Byte.toUnsignedInt(response[1]) + Byte.toUnsignedInt(response[2])/256.0;
        s = temperature - 25.0;
        x = x2*Math.pow(s, 2) + x1*s + x0;
        y = y2*Math.pow(s, 2) + y1*s + y0;
        z = (pu-x) / y;
        pressure = p2* Math.pow(z, 2) + p1*z + p0;

        return pressure;
    }

    public double seaLevel(double pressure, double altitude) {
        return (pressure / Math.pow(1 - (altitude / 44330.0), 5.255));
    }

    public double altitude(double pressure, double pressureAtABaseline) {
        return (44330.0 * (1 - Math.pow(pressure / pressureAtABaseline, 1 / 5.255)));
    }

    boolean readInt(byte[] request) throws ConnectionLostException, InterruptedException {
        return twi.writeRead(BMP180_ADDRESS, SEVEN_BIT_ADDRESS, request, request.length, null, 0);
    }

    boolean readUInt(byte[] request) throws ConnectionLostException, InterruptedException {
        return twi.writeRead(BMP180_ADDRESS, SEVEN_BIT_ADDRESS, request, request.length, null, 0);
    }

    int getIntCoefficient(byte[] request) throws ConnectionLostException, InterruptedException {
        byte[] msb = new byte[1];
        byte[] lsb = new byte[1];

        if (twi.writeRead(BMP180_ADDRESS, SEVEN_BIT_ADDRESS, new byte[]{request[0]}, 1, msb, 1) && twi.writeRead(BMP180_ADDRESS, SEVEN_BIT_ADDRESS, new byte[]{request[1]}, 1, lsb, 1)) {
            return (((int) msb[0] << 8) + (int) lsb[0]);
        }
        return 0;
    }

    int getUIntCoefficient(byte[] request) throws ConnectionLostException, InterruptedException {
        byte[] msb = new byte[1];
        byte[] lsb = new byte[1];

        if (twi.writeRead(BMP180_ADDRESS, SEVEN_BIT_ADDRESS, new byte[]{request[0]}, 1, msb, 1) && twi.writeRead(BMP180_ADDRESS, SEVEN_BIT_ADDRESS, new byte[]{request[1]}, 1, lsb, 1)) {
            return ((Byte.toUnsignedInt(msb[0]) << 8) + Byte.toUnsignedInt(lsb[0]));
        }
        return 0;
    }

    boolean writeBytes(byte[] request) throws ConnectionLostException, InterruptedException {
        return twi.writeRead(BMP180_ADDRESS, SEVEN_BIT_ADDRESS, request, request.length, null, 0);
    }

    byte[] readBytes(byte[] request) throws ConnectionLostException, InterruptedException {
        byte[] singleResponse = new byte[1];
        byte[] totalResponse = new byte[request.length];

        for (int i = 0; i < request.length; i++) {
            if (twi.writeRead(BMP180_ADDRESS, SEVEN_BIT_ADDRESS, new byte[]{request[i]}, 1, singleResponse, 1)) { // because i'm reading, i must send the request one reg at a time.
                totalResponse[i] = (byte) Byte.toUnsignedInt(singleResponse[0]);
            }
        }
        return totalResponse;
    }

    public double mbToAtm(double pressure) {
        return pressure * 0.000986923267;
    }

    public enum Oversampling {
        VERY_LOW_RES, LOW_RES, MEDIUM_RES, HIGH_RES
    }
}
