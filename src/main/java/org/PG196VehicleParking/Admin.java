ackage org.PG196VehicleParking;

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


    public Admin() {
    }


    public Admin(String email, String password) {
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
}