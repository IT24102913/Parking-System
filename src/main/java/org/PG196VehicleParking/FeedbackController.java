package org.PG196VehicleParking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "http://localhost:63342")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    // Add feedback
    @PostMapping
    public ResponseEntity<String> addFeedback(@RequestBody Feedback feedback) {
        feedbackService.addFeedback(feedback);
        return new ResponseEntity<>("Feedback added successfully", HttpStatus.CREATED);
    }

    // Get all feedbacks
    @GetMapping
    public ResponseEntity<List<Feedback>> getAllFeedback() {
        return new ResponseEntity<>(feedbackService.getAllFeedbacks(), HttpStatus.OK);
    }

//    // Get feedback by vehicleId
//    @GetMapping("/{vehicleId}")
//    public ResponseEntity<?> getFeedbackById(@PathVariable String vehicleId) {
//        return feedbackService.getFeedbackById(vehicleId)
//                .map(feedback -> new ResponseEntity<>(feedback, HttpStatus.OK))
//                .orElse(new ResponseEntity<>("Feedback not found", HttpStatus.NOT_FOUND));
//    }

    // Update feedback by id
    @PutMapping("/{id}")
    public ResponseEntity<String> updateFeedback(@PathVariable String id, @RequestBody Feedback feedback) {
        boolean updated = feedbackService.updateFeedback(id, feedback);
        if (updated) {
            return new ResponseEntity<>("Feedback updated successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Feedback not found", HttpStatus.NOT_FOUND);
        }
    }

    // Delete feedback by id
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFeedback(@PathVariable String id) {
        boolean deleted = feedbackService.deleteFeedback(id);
        if (deleted) {
            return new ResponseEntity<>("Feedback deleted successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Feedback not found", HttpStatus.NOT_FOUND);
        }
    }
}
