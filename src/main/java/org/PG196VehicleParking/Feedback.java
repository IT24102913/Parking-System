package org.PG196VehicleParking;

public class Feedback {

    private String vehicleId;
    private int mileage;
    private String name;
    private String email;
    private String phone;
    private String department;
    private String feedbackType;
    private String priority;
    private String feedbackDetails;
    public Feedback(){

    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getFeedbackType() {
        return feedbackType;
    }

    public void setFeedbackType(String feedbackType) {
        this.feedbackType = feedbackType;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getFeedbackDetails() {
        return feedbackDetails;
    }

    public void setFeedbackDetails(String feedbackDetails) {
        this.feedbackDetails = feedbackDetails;
    }
}
