package org.PG196VehicleParking;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/parking/admin")
public class Admin {


    private static final String ADMIN_FILE = "admins.txt";
    private static final String USER_FILE = "users.txt";


    private String email;
    private String password;

    public Admin() {}

    public Admin(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

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


    @GetMapping("/list")
    public ResponseEntity<List<Map<String, String>>> listAdmins() {
        List<Admin> admins = readFromFile(ADMIN_FILE);
        List<Map<String, String>> result = new ArrayList<>();
        for (Admin a : admins) {
            Map<String, String> map = new HashMap<>();
            map.put("email", a.getEmail());
            map.put("password", a.getPassword());
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/delete/{email}")
    public ResponseEntity<String> deleteAdmin(@PathVariable String email) {
        boolean removed = deleteFromFile(ADMIN_FILE, email);
        if (removed) {
            return ResponseEntity.ok("Admin deleted successfully");
        } else {
            return ResponseEntity.badRequest().body("Admin not found");
        }
    }

    @GetMapping("/user/list")
    public ResponseEntity<List<Map<String, String>>> listUsers() {
        List<Admin> users = readFromFile(USER_FILE);
        List<Map<String, String>> result = new ArrayList<>();
        for (Admin u : users) {
            Map<String, String> map = new HashMap<>();
            map.put("email", u.getEmail());
            map.put("password", u.getPassword());
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }
    @DeleteMapping("/user/delete/{email}")
    public ResponseEntity<String> deleteUser(@PathVariable String email) {
        boolean removed = deleteFromFile(USER_FILE, email);
        if (removed) {
            return ResponseEntity.ok("User deleted successfully");
        } else {
            return ResponseEntity.badRequest().body("User not found");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");
        if (email == null || password == null) {
            return ResponseEntity.badRequest().body("Email and password are required");
        }
        boolean isValid = existsInFile(ADMIN_FILE, email) &&
                readFromFile(ADMIN_FILE).stream().anyMatch(a -> a.getEmail().equals(email) && a.getPassword().equals(password));
        if (isValid) {
            return ResponseEntity.ok(Collections.singletonMap("message", "Login successful"));
        } else {
            return ResponseEntity.status(401).body(Collections.singletonMap("message", "Invalid credentials"));
        }
    }


    private void writeToFile(String filename, String email, String password) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)))) {
            out.println(email + "," + password);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Admin> readFromFile(String filename) {
        List<Admin> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
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


    private boolean existsInFile(String filename, String email) {
        return readFromFile(filename).stream().anyMatch(a -> a.getEmail().equals(email));
    }

    private boolean deleteFromFile(String filename, String email) {
        try {
            File file = new File(filename);
            if (!file.exists()) return false;

            List<String> lines = Files.readAllLines(file.toPath());
            List<String> updated = new ArrayList<>();
            boolean found = false;

            for (String line : lines) {
                if (!line.startsWith(email + ",")) {
                    updated.add(line);
                } else {
                    found = true;
                }
            }

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
