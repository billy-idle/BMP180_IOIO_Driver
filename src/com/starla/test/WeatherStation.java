package com.starla.test;

/**
 * Created by asus on 12/2/2015.
 */

import com.starla.driver.BMP180;

import ioio.lib.api.*;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOConnectionManager.Thread;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.pc.IOIOConsoleApp;

import java.io.IOException;

public final class WeatherStation extends IOIOConsoleApp {
    private DigitalOutput led;
    private boolean ledOn = false;
    private TwiMaster twi;
    private final int TWI_MODULE = 2;
    private BMP180 bmp180;
    private double temperature;
    private double relativePressure;

    // Boilerplate main(). Copy-paste this code into any IOIO application.
    public static void main(String[] args) throws Exception {
        new WeatherStation().go(args);
    }


    @Override
    protected void run(String[] args) throws IOException {
        while (true) {}
    }

    @Override
    public IOIOLooper createIOIOLooper(String connectionType, Object extra) {
        return new BaseIOIOLooper() {
            @Override
            protected void setup() throws ConnectionLostException, InterruptedException {
                led = ioio_.openDigitalOutput(IOIO.LED_PIN, true);

                twi = ioio_.openTwiMaster(TWI_MODULE, TwiMaster.Rate.RATE_400KHz, false);
                bmp180 = new BMP180(twi);

                System.out.println("Temp.(°C)\tPress.(atm)");
            }

            @Override
            public void loop() throws ConnectionLostException, InterruptedException {
                led.write(ledOn);

                temperature = bmp180.getTemperature();
                relativePressure = bmp180.mbToAtm(bmp180.seaLevel(bmp180.getPressure(temperature, BMP180.Oversampling.HIGH_RES), BMP180.ALTITUDE));

                System.out.print(Math.round(temperature * 100.0) / 100.0 + "\t\t");
                System.out.print(Math.round(relativePressure * 100.0) / 100.0 + "\n");

                Thread.sleep(1000);
            }
        };
    }
}



