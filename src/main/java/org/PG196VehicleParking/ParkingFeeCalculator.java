package org.PG196VehicleParking;

import java.util.Date;

public class ParkingFeeCalculator {
    public static double calculateFee(Date entryTime, Date exitTime, String vehicleType, int doors, boolean hasSidecar) {
        long durationMillis = exitTime.getTime() - entryTime.getTime();
        double hours = durationMillis / (1000.0 * 60 * 60);
        return Math.ceil(hours) * 50.0;
    }

    double rate;
    if ("Car".equalsIgnoreCase(vehicleType)) {
        if (doors == 4) {
            rate = 100.0;
        } else if (doors == 2) {
            rate = 90.0;
        } else {
            rate = 80.0;
        }
    } else if ("Motorbike".equalsIgnoreCase(vehicleType)) {
        if (hasSidecar) {
            rate = 60.0;
        } else {
            rate = 50.0;
        }
    } else {
        rate = 40.0;
    }

    return Math.ceil(hours) * rate;
}

