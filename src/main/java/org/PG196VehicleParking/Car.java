package org.PG196VehicleParking;

public class Car extends Vehicle {
    private int doors;

    public Car(String plateNumber, int doors, String model) {
        super(plateNumber,model);
        this.doors = doors;
    }
    public Car(String plateNumber, int doors) {
        super(plateNumber);
        this.doors = doors;
    }

    @Override
    public String getType() {
        return "Car";
    }

    public int getDoors() {
        return doors;
    }
}