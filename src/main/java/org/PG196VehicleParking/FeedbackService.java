package org.PG196VehicleParking;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FeedbackService {

    private final File file = new File("feed.txt");
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Feedback> getAllFeedbacks() {
        try {
            if (!file.exists()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(file, new TypeReference<List<Feedback>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Optional<Feedback> getFeedbackById(String vehicleId) {
        return getAllFeedbacks().stream()
                .filter(f -> f.getVehicleId().equals(vehicleId))
                .findFirst();
    }

    public void addFeedback(Feedback feedback) {
        List<Feedback> feedbacks = getAllFeedbacks();
        feedbacks.add(feedback);
        writeToFile(feedbacks);
    }

    public boolean updateFeedback(String vehicleId, Feedback newFeedback) {
        List<Feedback> feedbacks = getAllFeedbacks();
        for (int i = 0; i < feedbacks.size(); i++) {
            if (feedbacks.get(i).getVehicleId().equals(vehicleId)) {
                feedbacks.set(i, newFeedback);
                writeToFile(feedbacks);
                return true;
            }
        }
        return false;
    }

    public boolean deleteFeedback(String vehicleId) {
        List<Feedback> feedbacks = getAllFeedbacks();
        boolean removed = feedbacks.removeIf(f -> f.getVehicleId().equals(vehicleId));
        if (removed) {
            writeToFile(feedbacks);
        }
        return removed;
    }

    private void writeToFile(List<Feedback> feedbacks) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, feedbacks);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
