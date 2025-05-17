package org.PG196VehicleParking;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.*;

@RestController
@RequestMapping("/parking")
public class ParkingController {

    // File constants
    private static final String SLOT_FILE = "parking_slots.txt";
    private static final String VEHICLE_FILE = "vehicles.txt";
    private static final String ADMIN_FILE = "admins.txt";
    private static final String USER_FILE = "users.txt";
    private static final String HISTORY_FILE = "parking_history.txt";
    private static final String FEE_HISTORY_FILE = "parking_fees.txt";

    private Stack<ParkingSlot> parkingSlots = new Stack<>();

    // ========== PERSON 1: SLOT MANAGEMENT ========== //

    /**
     * Get all parking slots
     * @return List of all parking slots
     */
    @GetMapping("/slots")
    public List<ParkingSlot> getAllSlots() {
        return new ArrayList<>(parkingSlots);
    }

    /**
     * Create a new parking slot
     * @param slot Parking slot details
     * @return Created parking slot
     */
    @PostMapping("/create")
    public ParkingSlot addSlot(@RequestBody ParkingSlot slot) {
        parkingSlots.push(slot);
        saveSlotsToFile();
        return slot;
    }

    /**
     * Update parking slot status
     * @param slotId ID of slot to update
     * @param updatedSlot Updated slot details
     * @return Updated parking slot
     */
    @PutMapping("/update/{slotId}")
    public ResponseEntity<?> updateSlot(@PathVariable int slotId, @RequestBody ParkingSlot updatedSlot) {
        for (ParkingSlot slot : parkingSlots) {
            if (slot.getSlotId() == slotId) {
                if (updatedSlot.isOccupied() != slot.isOccupied()) {
                    if (!updatedSlot.isOccupied()) {
                        // Check if slot is occupied in history
                        List<Map<String, String>> history = readAllHistory();
                        for (Map<String, String> entry : history) {
                            if (Integer.parseInt(entry.get("slotId")) == slotId && entry.get("exitTime").equals("-")) {
                                // Mark as exited and calculate fee
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
                        // Prevent marking as occupied if no active reservation
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

    /**
     * Delete a parking slot
     * @param slotId ID of slot to delete
     * @return Success message
     */
    @DeleteMapping("/delete/{slotId}")
    public ResponseEntity<?> deleteSlot(@PathVariable int slotId) {
        // Check if slot is occupied in history
        List<Map<String, String>> history = readAllHistory();
        for (Map<String, String> entry : history) {
            if (Integer.parseInt(entry.get("slotId")) == slotId && entry.get("exitTime").equals("-")) {
                // Mark as exited and calculate fee
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

        // Delete the slot
        boolean removed = parkingSlots.removeIf(slot -> slot.getSlotId() == slotId);
        if (removed) {
            saveSlotsToFile();
            return ResponseEntity.ok(Collections.singletonMap("message", "Slot " + slotId + " deleted."));
        } else {
            return ResponseEntity.badRequest().body("Slot not found");
        }
    }

    /**
     * Get sorted parking slots (available first, then by ID)
     * @return Sorted list of parking slots
     */
    @GetMapping("/sort")
    public List<ParkingSlot> sortSlots() {
        List<ParkingSlot> sorted = new ArrayList<>(parkingSlots);
        quicksort(sorted, 0, sorted.size() - 1);
        return sorted;
    }

    /**
     * Quick sort implementation for parking slots
     */
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

    /**
     * Save all parking slots to file
     */
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

    /**
     * Load parking slots from file on startup
     */
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
    // ========== PERSON 2: VEHICLE MANAGEMENT ========== //

    /**
     * Register a new vehicle
     * @param payload Vehicle details (type, plateNumber, and type-specific attributes)
     * @return Success message
     */
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

    /**
     * Get list of all registered vehicles
     * @return List of vehicles with details
     */
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

    /**
     * Get list of currently parked vehicles
     * @return List of parked vehicles with slot information
     */
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

    /**
     * Save vehicle to file
     */
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

    // ========== PERSON 5: PARKING FEE & HISTORY MANAGEMENT ========== //

    /**
     * Reserve a parking slot
     * @param payload Reservation details (email, slotId, plateNumber)
     * @return Success message
     */
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

    /**
     * Leave a parking slot and calculate fee
     * @param payload Leave request details (email, slotId)
     * @return Parking fee information
     */
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

    /**
     * Get parking history for a user
     * @param email User email
     * @return List of parking history entries
     */
    @GetMapping("/history/{email}")
    public List<Map<String, String>> getHistory(@PathVariable String email) {
        return readHistory(email);
    }

    /**
     * Get all fee transactions
     * @return List of fee payment records
     */
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

    // ========== SHARED HELPER METHODS ========== //

    private boolean checkCredentials(String file, String email, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2 &&
                        parts[0].trim().equals(email.trim()) &&
                        parts[1].trim().equals(password.trim())) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean existsInFile(String file, String email) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 1 && parts[0].equals(email)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void addToFile(String file, String email, String password) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(email + "," + password);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean removeFromFile(String file, String email) {
        boolean removed = false;
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 1 && parts[0].equals(email)) {
                    removed = true;
                    continue;
                }
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String l : lines) {
                writer.write(l);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return removed;
    }

    private Map<String, String> findParkingEntry(String email, int slotId) {
        List<Map<String, String>> history = readAllHistory();
        for (Map<String, String> entry : history) {
            if (entry.get("email").equals(email) &&
                    Integer.parseInt(entry.get("slotId")) == slotId &&
                    entry.get("exitTime").equals("-")) {
                return entry;
            }
        }
        return null;
    }

    private Map<String, String> findVehicleByPlate(String plateNumber) {
        List<Map<String, String>> vehicles = listVehicles();
        for (Map<String, String> vehicle : vehicles) {
            if (vehicle.get("plateNumber").equals(plateNumber)) {
                return vehicle;
            }
        }
        return null;
    }

    private void recordFeeTransaction(String email, int slotId, String plateNumber, double fee) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FEE_HISTORY_FILE, true))) {
            writer.write(String.format("%s,%d,%s,%.2f,%tF %<tT",
                    email, slotId, plateNumber, fee, new Date()));
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateHistoryExit(String email, int slotId, Date exit, double fee) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(HISTORY_FILE))) {
            String line;
            boolean updated = false;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (!updated && parts[0].equals(email) &&
                        Integer.parseInt(parts[1]) == slotId &&
                        parts[3].equals("-")) {
                    String plateNumber = parts.length > 4 ? parts[4] : "";
                    line = parts[0] + "," + parts[1] + "," + parts[2] + "," +
                            exit.getTime() + "," + String.format("%.2f", fee) +
                            (plateNumber.isEmpty() ? "" : "," + plateNumber);
                    updated = true;
                }
                lines.add(line);
            }
        } catch (IOException e) { e.printStackTrace(); }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORY_FILE))) {
            for (String l : lines) {
                writer.write(l);
                writer.newLine();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private List<Map<String, String>> readHistory(String email) {
        List<Map<String, String>> history = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(HISTORY_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(email)) {
                    Map<String, String> entry = new HashMap<>();
                    entry.put("slotId", parts[1]);
                    entry.put("entryTime", parts[2]);
                    entry.put("exitTime", parts[3]);
                    if (parts.length > 4) {
                        entry.put("plateNumber", parts[4]);
                    }
                    history.add(entry);
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return history;
    }

    private List<Map<String, String>> readAllHistory() {
        List<Map<String, String>> history = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(HISTORY_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    Map<String, String> entry = new HashMap<>();
                    entry.put("email", parts[0]);
                    entry.put("slotId", parts[1]);
                    entry.put("entryTime", parts[2]);
                    entry.put("exitTime", parts[3]);
                    if (parts.length > 4) {
                        entry.put("plateNumber", parts[4]);
                    }
                    history.add(entry);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return history;
    }
}