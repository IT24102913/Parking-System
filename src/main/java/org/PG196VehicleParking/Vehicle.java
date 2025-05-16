package org.PG196VehicleParking;

public abstract class Vehicle {
    protected String plateNumber;

    public Vehicle(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public abstract String getType();
}


