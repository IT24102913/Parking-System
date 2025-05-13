package org.PG196VehicleParking;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/parking")
public class ParkingController {

    private Stack<ParkingSlot> parkingSlots = new Stack<>();
    private static final String FILE_NAME = "parking_slots.txt";


    @GetMapping("/slots")
    public List<ParkingSlot> getAllSlots() {
        return new ArrayList<>(parkingSlots);
    }


    @PostMapping("/create")
    public ParkingSlot addSlot(@RequestBody ParkingSlot slot) {
        System.out.println("Slot ID: " + slot.getSlotId());
        System.out.println("Occupied: " + slot.isOccupied());
        parkingSlots.push(slot);

        return slot;
    }
}