package ru.sportclub.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TrainingRegistration {
    public enum RegistrationStatus {
        PLANNED,
        CONFIRMED,
        COMPLETED,
        CANCELLED
    }

    private long id;
    private long memberId;
    private String memberName;
    private String trainingName;
    private String trainerName;
    private LocalDate trainingDate;
    private LocalTime startTime;
    private int durationMinutes;
    private RegistrationStatus status;
    private String category;
    private LocalDateTime createdAt;

    public TrainingRegistration(long id, long memberId, String memberName, String trainingName,
                                String trainerName, LocalDate trainingDate, LocalTime startTime,
                                int durationMinutes, RegistrationStatus status, String category,
                                LocalDateTime createdAt) {
        this.id = id;
        this.memberId = memberId;
        this.memberName = memberName;
        this.trainingName = trainingName;
        this.trainerName = trainerName;
        this.trainingDate = trainingDate;
        this.startTime = startTime;
        this.durationMinutes = durationMinutes;
        this.status = status;
        this.category = category;
        this.createdAt = createdAt;
    }

    public TrainingRegistration(long memberId, String trainingName, String trainerName,
                                LocalDate trainingDate, LocalTime startTime, int durationMinutes,
                                RegistrationStatus status, String category) {
        this(0, memberId, "", trainingName, trainerName, trainingDate, startTime,
                durationMinutes, status, category, null);
    }

    public long getId() { return id; }
    public long getMemberId() { return memberId; }
    public String getMemberName() { return memberName; }
    public String getTrainingName() { return trainingName; }
    public String getTrainerName() { return trainerName; }
    public LocalDate getTrainingDate() { return trainingDate; }
    public LocalTime getStartTime() { return startTime; }
    public int getDurationMinutes() { return durationMinutes; }
    public RegistrationStatus getStatus() { return status; }
    public String getCategory() { return category; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setId(long id) { this.id = id; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public void setStatus(RegistrationStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "%d | %s | участник: %s | %s | %s %s | %d мин | %s | %s".formatted(
                id, trainingName, memberName, trainerName, trainingDate, startTime,
                durationMinutes, status, category);
    }
}
