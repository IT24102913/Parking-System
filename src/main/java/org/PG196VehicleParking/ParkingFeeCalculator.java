package org.PG196VehicleParking;

import java.util.Date;

public class ParkingFeeCalculator {
    public static double calculateFee(Date entryTime, Date exitTime, String vehicleType, int doors, boolean hasSidecar) {
        long durationMillis = exitTime.getTime() - entryTime.getTime();
        double hours = durationMillis / (1000.0 * 60 * 60);
        return Math.ceil(hours) * 50.0;
    }
}

