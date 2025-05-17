package org.PG196VehicleParking;

import java.util.Date;

public class ParkingFeeCalculator {
    // Rates for Cars (LKR)
    private static final double CAR_4_DOORS_FIRST_HOUR = 100.0;
    private static final double CAR_4_DOORS_ADDITIONAL_HOUR = 80.0;
    private static final double CAR_2_DOORS_FIRST_HOUR = 90.0;
    private static final double CAR_2_DOORS_ADDITIONAL_HOUR = 70.0;

    // Rates for Motorbikes (LKR)
    private static final double MOTORBIKE_SIDECAR_FIRST_HOUR = 60.0;
    private static final double MOTORBIKE_SIDECAR_ADDITIONAL_HOUR = 55.0;
    private static final double MOTORBIKE_NO_SIDECAR_FIRST_HOUR = 50.0;
    private static final double MOTORBIKE_NO_SIDECAR_ADDITIONAL_HOUR = 40.0;

    // Minimum charge (LKR)
    private static final double MINIMUM_CHARGE = 1.0;

    public static double calculateFee(Date entryTime, Date exitTime, String vehicleType, int doors, boolean hasSidecar) {
        long durationMillis = exitTime.getTime() - entryTime.getTime();
        double hours = durationMillis / (1000.0 * 60 * 60);
        double totalFee;

        if ("Car".equalsIgnoreCase(vehicleType)) {
            if (doors == 4) {
                totalFee = CAR_4_DOORS_FIRST_HOUR;
                if (hours > 1) {
                    totalFee += CAR_4_DOORS_ADDITIONAL_HOUR * (Math.ceil(hours) - 1);
                }
            } else if (doors == 2) {
                totalFee = CAR_2_DOORS_FIRST_HOUR;
                if (hours > 1) {
                    totalFee += CAR_2_DOORS_ADDITIONAL_HOUR * (Math.ceil(hours) - 1);
                }
            } else {
                // Default to 2 doors for invalid door count
                totalFee = CAR_2_DOORS_FIRST_HOUR;
                if (hours > 1) {
                    totalFee += CAR_2_DOORS_ADDITIONAL_HOUR * (Math.ceil(hours) - 1);
                }
            }
        } else if ("Motorbike".equalsIgnoreCase(vehicleType)) {
            if (hasSidecar) {
                totalFee = MOTORBIKE_SIDECAR_FIRST_HOUR;
                if (hours > 1) {
                    totalFee += MOTORBIKE_SIDECAR_ADDITIONAL_HOUR * (Math.ceil(hours) - 1);
                }
            } else {
                totalFee = MOTORBIKE_NO_SIDECAR_FIRST_HOUR;
                if (hours > 1) {
                    totalFee += MOTORBIKE_NO_SIDECAR_ADDITIONAL_HOUR * (Math.ceil(hours) - 1);
                }
            }
        } else {
            // Default to motorbike without sidecar for unknown types
            totalFee = MOTORBIKE_NO_SIDECAR_FIRST_HOUR;
            if (hours > 1) {
                totalFee += MOTORBIKE_NO_SIDECAR_ADDITIONAL_HOUR * (Math.ceil(hours) - 1);
            }
        }

        // Apply minimum charge
        if (totalFee < MINIMUM_CHARGE) {
            totalFee = MINIMUM_CHARGE;
        }

        return totalFee;
    }
}

