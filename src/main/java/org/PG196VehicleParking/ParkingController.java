package org.PG196VehicleParking;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.*;

@RestController
@RequestMapping("/parking")
public class ParkingController {

    private static final String SLOT_FILE = "parking_slots.txt";
    private static final String VEHICLE_FILE = "vehicles.txt";
    private static final String HISTORY_FILE = "parking_history.txt";
    private static final String FEE_HISTORY_FILE = "parking_fees.txt";

    private Stack<ParkingSlot> parkingSlots = new Stack<>();

    // ========== IT24102913: SLOT MANAGEMENT ========== //


    @GetMapping("/slots")
    public List<ParkingSlot> getAllSlots() {
        return new ArrayList<>(parkingSlots);
    }

    @PostMapping("/create")
    public ParkingSlot addSlot(@RequestBody ParkingSlot slot) {
        parkingSlots.push(slot);
        saveSlotsToFile();
        return slot;
    }


    @PutMapping("/update/{slotId}")
    public ResponseEntity<?> updateSlot(@PathVariable int slotId, @RequestBody ParkingSlot updatedSlot) {
        for (ParkingSlot slot : parkingSlots) {
            if (slot.getSlotId() == slotId) {
                if (updatedSlot.isOccupied() != slot.isOccupied()) {
                    if (!updatedSlot.isOccupied()) {
                        List<Map<String, String>> history = readAllHistory();
                        for (Map<String, String> entry : history) {
                            if (Integer.parseInt(entry.get("slotId")) == slotId && entry.get("exitTime").equals("-")) {
                                String email = entry.get("email");
                                String plateNumber = entry.get("plateNumber");
                                Date now = new Date();
                                Date entryTime = new Date(Long.parseLong(entry.get("entryTime")));
                                Map<String, String> vehicle = findVehicleByPlate(plateNumber);
                                if (vehicle != null) {
                                    int doors = vehicle.containsKey("doors") ? Integer.parseInt(vehicle.get("doors")) : 0;
                                    boolean hasSidecar = vehicle.containsKey("hasSidecar") && Boolean.parseBoolean(vehicle.get("hasSidecar"));
                                    double fee = ParkingFeeCalculator.calculateFee(
                                            entryTime, now,
                                            vehicle.get("type"),
                                            doors,
                                            hasSidecar
                                    );
                                    updateHistoryExit(email, slotId, now, fee);
                                    recordFeeTransaction(email, slotId, plateNumber, fee);
                                }
                            }
                        }
                    } else {
                        List<Map<String, String>> history = readAllHistory();
                        boolean isReserved = history.stream()
                                .anyMatch(entry -> Integer.parseInt(entry.get("slotId")) == slotId && entry.get("exitTime").equals("-"));
                        if (isReserved) {
                            return ResponseEntity.badRequest().body("Cannot mark slot as occupied; it is already reserved.");
                        }
                    }
                    slot.setIsOccupied(updatedSlot.isOccupied());
                    saveSlotsToFile();
                    return ResponseEntity.ok(slot);
                }
                return ResponseEntity.ok(slot); // No change needed
            }
        }
        return ResponseEntity.badRequest().body("Slot not found");
    }

    @DeleteMapping("/delete/{slotId}")
    public ResponseEntity<?> deleteSlot(@PathVariable int slotId) {
        List<Map<String, String>> history = readAllHistory();
        for (Map<String, String> entry : history) {
            if (Integer.parseInt(entry.get("slotId")) == slotId && entry.get("exitTime").equals("-")) {
                String email = entry.get("email");
                String plateNumber = entry.get("plateNumber");
                Date now = new Date();
                Date entryTime = new Date(Long.parseLong(entry.get("entryTime")));
                Map<String, String> vehicle = findVehicleByPlate(plateNumber);
                if (vehicle != null) {
                    int doors = vehicle.containsKey("doors") ? Integer.parseInt(vehicle.get("doors")) : 0;
                    boolean hasSidecar = vehicle.containsKey("hasSidecar") && Boolean.parseBoolean(vehicle.get("hasSidecar"));
                    double fee = ParkingFeeCalculator.calculateFee(
                            entryTime, now,
                            vehicle.get("type"),
                            doors,
                            hasSidecar
                    );
                    updateHistoryExit(email, slotId, now, fee);
                    recordFeeTransaction(email, slotId, plateNumber, fee);
                }
            }
        }

        boolean removed = parkingSlots.removeIf(slot -> slot.getSlotId() == slotId);
        if (removed) {
            saveSlotsToFile();
            return ResponseEntity.ok(Collections.singletonMap("message", "Slot " + slotId + " deleted."));
        } else {
            return ResponseEntity.badRequest().body("Slot not found");
        }
    }

    @GetMapping("/sort")
    public List<ParkingSlot> sortSlots() {
        List<ParkingSlot> sorted = new ArrayList<>(parkingSlots);
        quicksort(sorted, 0, sorted.size() - 1);
        return sorted;
    }


    private void quicksort(List<ParkingSlot> list, int low, int high) {
        if (low < high) {
            int pi = partition(list, low, high);
            quicksort(list, low, pi - 1);
            quicksort(list, pi + 1, high);
        }
    }

    private int partition(List<ParkingSlot> list, int low, int high) {
        ParkingSlot pivot = list.get(high);
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (!list.get(j).isOccupied() && pivot.isOccupied()) {
                i++;
                Collections.swap(list, i, j);
            } else if (list.get(j).isOccupied() == pivot.isOccupied()) {
                if (list.get(j).getSlotId() < pivot.getSlotId()) {
                    i++;
                    Collections.swap(list, i, j);
                }
            }
        }
        Collections.swap(list, i + 1, high);
        return i + 1;
    }

    private void saveSlotsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SLOT_FILE))) {
            for (ParkingSlot slot : parkingSlots) {
                writer.write(slot.getSlotId() + "," + slot.isOccupied());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostConstruct
    public void loadSlotsFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(SLOT_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                ParkingSlot slot = new ParkingSlot();
                slot.setSlotId(Integer.parseInt(parts[0]));
                slot.setIsOccupied(Boolean.parseBoolean(parts[1]));
                parkingSlots.push(slot);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}