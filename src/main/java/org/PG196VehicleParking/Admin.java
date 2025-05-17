package org.PG196VehicleParking;

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
    public String addAdmin(@RequestBody Admin admin) {
        // Check for empty fields
        if (admin.getEmail() == null || admin.getPassword() == null) {
            return "Email and password are required";
        }


        if (existsInFile(ADMIN_FILE, admin.getEmail())) {
            return "Admin already exists";
        }


        writeToFile(ADMIN_FILE, admin.getEmail(), admin.getPassword());
        return "Admin added successfully";
    }


    @GetMapping("/list")
    public List<Admin> listAdmins() {
        return readFromFile(ADMIN_FILE);
    }


    @DeleteMapping("/delete/{email}")
    public String deleteAdmin(@PathVariable String email) {
        boolean removed = deleteFromFile(ADMIN_FILE, email);
        if (removed) {
            return "Admin deleted successfully";
        } else {
            return "Admin not found";
        }
    }


    @GetMapping("/user/list")
    public List<Admin> listUsers() {
        return readFromFile(USER_FILE);
    }


    @DeleteMapping("/user/delete/{email}")
    public String deleteUser(@PathVariable String email) {
        boolean removed = deleteFromFile(USER_FILE, email);
        if (removed) {
            return "User deleted successfully";
        } else {
            return "User not found";
        }
    }


    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");

        if (email == null || password == null) {
            return "Email and password are required";
        }

        List<Admin> list = readFromFile(ADMIN_FILE);
        boolean valid = false;


        for (int i = 0; i < list.size(); i++) {
            Admin a = list.get(i);
            if (a.getEmail().equals(email) && a.getPassword().equals(password)) {
                valid = true;
                break;
            }
        }

        if (valid) {
            return "Login successful";
        } else {
            return "Invalid email or password";
        }
    }


    private void writeToFile(String filename, String email, String password) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write(email + "," + password);
            writer.newLine(); // move to next line
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private List<Admin> readFromFile(String filename) {
        List<Admin> list = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2); // Split by comma
                if (parts.length == 2) {
                    list.add(new Admin(parts[0].trim(), parts[1].trim()));
                }
            }
        } catch (IOException e) {

        }

        return list;
    }


    private boolean existsInFile(String filename, String email) {
        List<Admin> list = readFromFile(filename);

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getEmail().equals(email)) {
                return true;
            }
        }

        return false;
    }


    private boolean deleteFromFile(String filename, String email) {
        Path path = Paths.get(filename);
        if (!Files.exists(path)) return false;

        try {
            List<String> lines = Files.readAllLines(path);
            List<String> updated = new ArrayList<>();
            boolean found = false;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (!line.startsWith(email + ",")) {
                    updated.add(line);
                } else {
                    found = true;
                }
            }

            if (found) {
                Files.write(path, updated);
            }

            return found;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
