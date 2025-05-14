// Vehicle.java (abstract class)
package org.PG196VehicleParking;

public abstract class Vehicle {
    protected String plateNumber;
    protected String model;

    public Vehicle(String plateNumber, String model) {
        this.plateNumber = plateNumber;
        this.model = model;
    }
    public Vehicle(String plateNumber) {

        this.plateNumber = plateNumber;
    }

    public String getPlateNumber() {
        return plateNumber;
    }
    public String getModel() {return model;}

    public abstract String getType();
}


