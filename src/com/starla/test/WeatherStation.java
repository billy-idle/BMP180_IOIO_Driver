package com.starla.test;

/**
 * Created by guillesupremacy on 12/2/2015.
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
    private final boolean ledOn = false;
    private TwiMaster twi;
    private final int TWI_MODULE = 2;
    private final boolean I2C_VOLTAGE_LEVELS = false;
    private BMP180 bmp180;
    private double temperature;
    private double pressure;
    private double seaLevelPressure;
    private double computedAltitude;
    // Altitude (in meters) took from latitude 4°26.996'N longitude 75°11.317'W - Ibagué - Tolima - Colombia, using a MotoG's GPS.
    private final double ALTITUDE = 1111.0;

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

                twi = ioio_.openTwiMaster(TWI_MODULE, TwiMaster.Rate.RATE_100KHz, I2C_VOLTAGE_LEVELS);
                bmp180 = new BMP180(twi);

                System.out.println("Temp.(°C)\tPress.(atm)\t\tAlt.(m)");
            }

            @Override
            public void loop() throws ConnectionLostException, InterruptedException {
                led.write(ledOn);

                temperature = bmp180.getTemperature();
                pressure = bmp180.getPressure(temperature, BMP180.Oversampling.HIGH_RES);
                seaLevelPressure = bmp180.seaLevel(pressure, ALTITUDE);
                computedAltitude = bmp180.altitude(pressure,seaLevelPressure);

                System.out.print(Math.round(temperature * 100.0) / 100.0 + "\t\t");
                System.out.print(Math.round(bmp180.mbToAtm(seaLevelPressure) * 100.0) / 100.0 + "\t\t\t");
                System.out.print(Math.round(computedAltitude * 100.0) / 100.0 + "\n");

                Thread.sleep(1000);
            }
        };
    }
}



