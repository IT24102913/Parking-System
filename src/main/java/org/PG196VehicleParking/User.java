package org.PG196VehicleParking;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class User {


    private static final String USER_FILE = "users.txt";


    private String email;
    private String password;



    public User() {
    }


    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    @PostMapping("/user/login")
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
}