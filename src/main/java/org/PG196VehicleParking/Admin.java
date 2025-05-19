//IT24102760

package org.PG196VehicleParking;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@RestController  // Marks this class as a REST controller for handling HTTP requests
@RequestMapping("/parking/admin")  // Base URL path for all APIs in this controller
public class Admin {

    // File paths for storing admin and user credentials
    private static final String ADMIN_FILE = "admins.txt";
    private static final String USER_FILE = "users.txt";

    // Admin object fields for email and password
    private String email;
    private String password;

    // Default constructor
    public Admin() {}

    // Parameterized constructor for easier object creation
    public Admin(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters and setters for email and password
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // Endpoint to add a new admin, Check if admin already exists, // Save new admin credentials to file
    @PostMapping("/add")
    public ResponseEntity<String> addAdmin(@RequestBody Admin admin) {

        if (admin.getEmail() == null || admin.getPassword() == null) {
            return ResponseEntity.badRequest().body("Email and password are required");
        }


        if (existsInFile(ADMIN_FILE, admin.getEmail())) {
            return ResponseEntity.badRequest().body("Admin already exists");
        }


        writeToFile(ADMIN_FILE, admin.getEmail(), admin.getPassword());
        return ResponseEntity.ok("Admin added successfully");
    }

    // Endpoint to list all registered admins
    @GetMapping("/list")
    public ResponseEntity<List<Map<String, String>>> listAdmins() {
        List<Admin> admins = readFromFile(ADMIN_FILE);  // Read all admins from file
        List<Map<String, String>> result = new ArrayList<>();

        // Convert each Admin object to a map of email and password
        for (int i = 0; i < admins.size(); i++) {
            Admin a = admins.get(i);
            Map<String, String> map = new HashMap<>();
            map.put("email", a.getEmail());
            map.put("password", a.getPassword());
            result.add(map);
        }

        return ResponseEntity.ok(result);  // Return list of maps as response
    }

    // Endpoint to delete an admin by email
    @DeleteMapping("/delete/{email}")
    public ResponseEntity<String> deleteAdmin(@PathVariable String email) {
        boolean removed = deleteFromFile(ADMIN_FILE, email);
        if (removed) {
            return ResponseEntity.ok("Admin deleted successfully");
        } else {
            return ResponseEntity.badRequest().body("Admin not found");
        }
    }

    // Endpoint to list all users (not admins)
    @GetMapping("/user/list")
    public ResponseEntity<List<Map<String, String>>> listUsers() {
        List<Admin> users = readFromFile(USER_FILE);  // Read all users from file
        List<Map<String, String>> result = new ArrayList<>();

        // Convert each user to a map of email and password
        for (int i = 0; i < users.size(); i++) {
            Admin u = users.get(i);
            Map<String, String> map = new HashMap<>();
            map.put("email", u.getEmail());
            map.put("password", u.getPassword());
            result.add(map);
        }

        return ResponseEntity.ok(result);  // Return list of users
    }

    // Endpoint to delete a user by email
    @DeleteMapping("/user/delete/{email}")
    public ResponseEntity<String> deleteUser(@PathVariable String email) {
        boolean removed = deleteFromFile(USER_FILE, email);  // Try to delete from file
        if (removed) {
            return ResponseEntity.ok("User deleted successfully");
        } else {
            return ResponseEntity.badRequest().body("User not found");
        }
    }

    // Admin login API to check credentials
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");

        // Validation
        if (email == null || password == null) {
            return ResponseEntity.badRequest().body("Email and password are required");
        }

        // Check if credentials are valid
        boolean isValid = existsInFile(ADMIN_FILE, email) &&
                readFromFile(ADMIN_FILE).stream().anyMatch(a -> a.getEmail().equals(email) && a.getPassword().equals(password));

        if (isValid) {
            return ResponseEntity.ok(Collections.singletonMap("message", "Login successful"));
        } else {
            return ResponseEntity.status(401).body(Collections.singletonMap("message", "Invalid credentials"));
        }
    }

    // Helper method to write email and password to a file
    private void writeToFile(String filename, String email, String password) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)))) {
            out.println(email + "," + password);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper method to read all Admin/User data from file
    private List<Admin> readFromFile(String filename) {
        List<Admin> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;

            // Read line by line
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    list.add(new Admin(parts[0], parts[1]));
                }
            }
        } catch (IOException e) {

        }
        return list;
    }

    // Helper method to check if an email exists in the file
    private boolean existsInFile(String filename, String email) {
        return readFromFile(filename).stream().anyMatch(a -> a.getEmail().equals(email));
    }

    // Helper method to delete a user/admin by email from the file
    private boolean deleteFromFile(String filename, String email) {
        try {
            File file = new File(filename);
            if (!file.exists()) return false;


            List<String> lines = Files.readAllLines(file.toPath());
            List<String> updated = new ArrayList<>();
            boolean found = false;

            // Keep only lines that do not match the email
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (!line.startsWith(email + ",")) {
                    updated.add(line);
                } else {
                    found = true;
                }
            }

            // If found, rewrite the file without the deleted line
            if (found) {
                Files.write(file.toPath(), updated);
            }

            return found;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
