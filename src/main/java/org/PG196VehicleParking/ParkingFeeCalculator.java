package org.PG196VehicleParking;

import java.util.Date;

public class ParkingFeeCalculator {
    public static double calculateFee(Date entryTime, Date exitTime, String vehicleType, int doors, boolean hasSidecar) {
        long durationMillis = exitTime.getTime() - entryTime.getTime();
        double hours = durationMillis / (1000.0 * 60 * 60);
        double totalFee;

    if ("Car".equalsIgnoreCase(vehicleType)) {
        if (doors == 4) {
            totalFee = 100.0; // First hour
            if (hours > 1) {
                totalFee += 80.0 * (Math.ceil(hours) - 1);
            }
        } else if (doors == 2) {
            totalFee = 90.0; // First hour
            if (hours > 1) {
                totalFee += 70.0 * (Math.ceil(hours) - 1);
            }
        } else {
            totalFee = 80.0; // Default for unknown Cars
            if (hours > 1) {
                totalFee += 70.0 * (Math.ceil(hours) - 1);
            }
        }
    } else if ("Motorbike".equalsIgnoreCase(vehicleType)) {
        if (hasSidecar) {
            totalFee = 60.0; // First hour
            if (hours > 1) {
                totalFee += 55.0 * (Math.ceil(hours) - 1);
            }
        } else {
            totalFee = 50.0; // First hour
            if (hours > 1) {
                totalFee += 40.0 * (Math.ceil(hours) - 1);
            }
        }
    } else {
        totalFee = 40.0; // Default for unknown vehicle types
        if (hours > 1) {
            totalFee += 30.0 * (Math.ceil(hours) - 1);
        }
    }

    return totalFee < 1.0 ? 1.0 : totalFee; // Apply a minimum fee
}
}

