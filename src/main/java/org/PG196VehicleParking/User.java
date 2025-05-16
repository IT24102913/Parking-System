package org.PG196VehicleParking;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.*;

@RestController
@RequestMapping("/parking/user")
public class User {


    private static final String USER_FILE = "users.txt";


    private String email;
    private String password;




    public User() {}

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }



    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");


        if (email == null || password == null) {
            return ResponseEntity.badRequest().body("Email and password are required");
        }

        boolean isValid = checkCredentials(USER_FILE, email, password);

        if (isValid) {
            return ResponseEntity.ok(Collections.singletonMap("message", "Login successful"));
        } else {
            return ResponseEntity.status(401).body(Collections.singletonMap("message", "Invalid credentials"));
        }
    }


    @PostMapping("/add")
    public ResponseEntity<?> addUser(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");


        if (email == null || password == null) {
            return ResponseEntity.badRequest().body("Email and password are required");
        }


        if (existsInFile(USER_FILE, email)) {
            return ResponseEntity.badRequest().body("User already exists");
        }


        addToFile(USER_FILE, email, password);
        return ResponseEntity.ok("User added successfully");
    }


    @GetMapping("/profile/{email}")
    public Map<String, String> getProfile(@PathVariable String email) {
        List<Map<String, String>> users = readUsersFromFile(USER_FILE);
        for (Map<String, String> user : users) {
            if (user.get("email").equals(email)) {
                user.remove("password");
                return user;
            }
        }
        return new HashMap<>();
    }


    @PostMapping("/profile/update")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String password = payload.get("password");
        if (email == null || password == null) {
            return ResponseEntity.badRequest().body("Email and password required");
        }
        removeFromFile(USER_FILE, email);
        addToFile(USER_FILE, email, password);
        return ResponseEntity.ok("Profile updated");
    }


    private boolean checkCredentials(String filename, String email, String password) {
        List<Map<String, String>> users = readUsersFromFile(filename);
        return users.stream()
                .anyMatch(user -> email.equals(user.get("email")) && password.equals(user.get("password")));
    }


    private void addToFile(String filename, String email, String password) {
        try {
            File file = new File(filename);
            if (!file.exists()) {
                file.createNewFile();
            }

            try (FileWriter fw = new FileWriter(file, true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)) {
                out.println(email + "," + password);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static List<Map<String, String>> readUsersFromFile(String filename) {
        List<Map<String, String>> users = new ArrayList<>();
        try {
            File file = new File(filename);
            if (!file.exists()) {
                return users;
            }

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        Map<String, String> user = new HashMap<>();
                        user.put("email", parts[0]);
                        user.put("password", parts[1]);
                        users.add(user);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }


    private boolean existsInFile(String filename, String email) {
        List<Map<String, String>> users = readUsersFromFile(filename);
        return users.stream().anyMatch(user -> email.equals(user.get("email")));
    }


    private void removeFromFile(String filename, String email) {
        try {
            File file = new File(filename);
            if (!file.exists()) {
                return;
            }

            List<Map<String, String>> users = readUsersFromFile(filename);
            users.removeIf(user -> email.equals(user.get("email")));

            try (FileWriter fw = new FileWriter(file);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)) {
                for (Map<String, String> user : users) {
                    out.println(user.get("email") + "," + user.get("password"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
