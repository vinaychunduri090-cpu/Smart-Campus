package com.campusportal.dto;

import com.campusportal.model.Registration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

public class RegistrationDTO {
    private Long id; // This will hold EVENT ID so frontend buttons work
    private String title;
    private String category;
    private String venue;
    private LocalDate eventDate;
    private LocalTime eventTime;
    private LocalDate registrationDeadline;
    private Integer maxCapacity;
    private String bannerUrl;
    private String creatorName;
    private String status;
    private long registrationCount;
    
    // Registration specific fields
    private Long registrationId;
    private String registrationStatus;
    private LocalDateTime registeredAt;

    public RegistrationDTO() {}

    public RegistrationDTO(Registration reg, long regCount) {
        // We map Event fields to the top level because the frontend UI expects an 'event' object
        this.id = reg.getEvent().getId(); 
        this.title = reg.getEvent().getTitle();
        this.category = reg.getEvent().getCategory();
        this.venue = reg.getEvent().getVenue();
        this.eventDate = reg.getEvent().getEventDate();
        this.eventTime = reg.getEvent().getEventTime();
        this.registrationDeadline = reg.getEvent().getRegistrationDeadline();
        this.maxCapacity = reg.getEvent().getMaxCapacity();
        this.bannerUrl = reg.getEvent().getBannerUrl();
        this.creatorName = reg.getEvent().getCreatedBy().getFullName();
        this.status = reg.getEvent().getStatus(); // Event status
        this.registrationCount = regCount;

        this.registrationId = reg.getId();
        this.registrationStatus = reg.getStatus();
        this.registeredAt = reg.getRegisteredAt();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public LocalTime getEventTime() { return eventTime; }
    public void setEventTime(LocalTime eventTime) { this.eventTime = eventTime; }
    public LocalDate getRegistrationDeadline() { return registrationDeadline; }
    public void setRegistrationDeadline(LocalDate registrationDeadline) { this.registrationDeadline = registrationDeadline; }
    public Integer getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(Integer maxCapacity) { this.maxCapacity = maxCapacity; }
    public String getBannerUrl() { return bannerUrl; }
    public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }
    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getRegistrationCount() { return registrationCount; }
    public void setRegistrationCount(long registrationCount) { this.registrationCount = registrationCount; }
    public Long getRegistrationId() { return registrationId; }
    public void setRegistrationId(Long registrationId) { this.registrationId = registrationId; }
    public String getRegistrationStatus() { return registrationStatus; }
    public void setRegistrationStatus(String registrationStatus) { this.registrationStatus = registrationStatus; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
}
