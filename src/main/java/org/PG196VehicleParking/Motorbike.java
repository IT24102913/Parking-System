
// Motorbike.java
package org.example;

public class Motorbike extends Vehicle {
    private boolean hasSidecar;

    public Motorbike(String plateNumber, boolean hasSidecar, String model) {
        super(plateNumber, model);
        this.hasSidecar = hasSidecar;
    }

    @Override
    public String getType() {
        return "Motorbike";
    }

    public boolean hasSidecar() {
        return hasSidecar;
    }
}