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

    // ========== IT24102678: VEHICLE MANAGEMENT ========== //


    @PostMapping("/vehicle/register")
    public ResponseEntity<String> registerVehicle(@RequestBody Map<String, String> payload) {
        String type = payload.get("type");
        String plate = payload.get("plateNumber");

        Vehicle vehicle;
        try {
            if ("Car".equalsIgnoreCase(type)) {
                int doors = Integer.parseInt(payload.get("doors"));
                if (doors != 2 && doors != 4) {
                    return ResponseEntity.badRequest().body("Car must have either 2 or 4 doors.");
                }
                vehicle = new Car(plate, doors);
            } else if ("Motorbike".equalsIgnoreCase(type)) {
                boolean sidecar = Boolean.parseBoolean(payload.get("hasSidecar"));
                vehicle = new Motorbike(plate, sidecar);
            } else {
                return ResponseEntity.badRequest().body("Invalid vehicle type.");
            }

            saveVehicleToFile(vehicle);
            return ResponseEntity.ok(vehicle.getType() + " with plate " + vehicle.getPlateNumber() + " registered.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }


    @GetMapping("/vehicle/list")
    public List<Map<String, String>> listVehicles() {
        List<Map<String, String>> vehicles = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(VEHICLE_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    Map<String, String> vehicle = new HashMap<>();
                    vehicle.put("type", parts[0]);
                    vehicle.put("plateNumber", parts[1]);
                    if (parts.length > 2) {
                        if (parts[0].equals("Car")) {
                            vehicle.put("doors", parts[2]);
                        } else if (parts[0].equals("Motorbike")) {
                            vehicle.put("hasSidecar", parts[2]);
                        }
                    }
                    vehicles.add(vehicle);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return vehicles;
    }

    @GetMapping("/vehicle/parked")
    public List<Map<String, String>> getParkedVehicles() {
        List<Map<String, String>> parkedVehicles = new ArrayList<>();
        List<Map<String, String>> history = readAllHistory();

        for (Map<String, String> entry : history) {
            if ("-".equals(entry.get("exitTime"))) {
                List<Map<String, String>> vehicles = listVehicles();
                for (Map<String, String> vehicle : vehicles) {
                    if (vehicle.get("plateNumber").equals(entry.get("plateNumber"))) {
                        Map<String, String> parkedVehicle = new HashMap<>(vehicle);
                        parkedVehicle.put("slotId", entry.get("slotId"));
                        parkedVehicles.add(parkedVehicle);
                        break;
                    }
                }
            }
        }
        return parkedVehicles;
    }

    private void saveVehicleToFile(Vehicle vehicle) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(VEHICLE_FILE, true))) {
            String details = "";
            if (vehicle instanceof Car) {
                details = "," + ((Car) vehicle).getDoors();
            } else if (vehicle instanceof Motorbike) {
                details = "," + ((Motorbike) vehicle).hasSidecar();
            }
            writer.write(vehicle.getType() + "," + vehicle.getPlateNumber() + details);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ========== IT24104385: PARKING FEE & HISTORY MANAGEMENT ========== //



    @PostMapping("/reserve")
    public ResponseEntity<?> reserveSlot(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        int slotId = Integer.parseInt(payload.get("slotId"));
        String plateNumber = payload.get("plateNumber");
        Date now = new Date();

        for (ParkingSlot slot : parkingSlots) {
            if (slot.getSlotId() == slotId && !slot.isOccupied()) {
                slot.setIsOccupied(true);
                saveSlotsToFile();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORY_FILE, true))) {
                    writer.write(email + "," + slotId + "," + now.getTime() + ",-," + plateNumber);
                    writer.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return ResponseEntity.ok("Slot reserved");
            }
        }
        return ResponseEntity.badRequest().body("Slot not available");
    }


    @PostMapping("/leave")
    public ResponseEntity<?> leaveSlot(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        int slotId = Integer.parseInt(payload.get("slotId"));
        Date now = new Date();

        for (ParkingSlot slot : parkingSlots) {
            if (slot.getSlotId() == slotId && slot.isOccupied()) {
                slot.setIsOccupied(false);
                saveSlotsToFile();

                Map<String, String> entry = findParkingEntry(email, slotId);
                if (entry != null) {
                    Date entryTime = new Date(Long.parseLong(entry.get("entryTime")));
                    Map<String, String> vehicle = findVehicleByPlate(entry.get("plateNumber"));
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
                        recordFeeTransaction(email, slotId, entry.get("plateNumber"), fee);
                        return ResponseEntity.ok(Collections.singletonMap("fee", fee));
                    }
                }
                return ResponseEntity.ok("Slot left (fee not calculated)");
            }
        }
        return ResponseEntity.badRequest().body("Slot not occupied");
    }


    @GetMapping("/history/{email}")
    public List<Map<String, String>> getHistory(@PathVariable String email) {
        return readHistory(email);
    }


    @GetMapping("/fee/history")
    public List<Map<String, String>> getFeeHistory() {
        List<Map<String, String>> fees = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FEE_HISTORY_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    Map<String, String> feeRecord = new HashMap<>();
                    feeRecord.put("email", parts[0]);
                    feeRecord.put("slotId", parts[1]);
                    feeRecord.put("plateNumber", parts[2]);
                    feeRecord.put("fee", parts[3]);
                    feeRecord.put("timestamp", parts[4] + (parts.length > 5 ? "," + parts[5] : ""));
                    fees.add(feeRecord);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fees;
    }

}