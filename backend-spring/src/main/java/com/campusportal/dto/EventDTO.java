package com.campusportal.dto;

import com.campusportal.model.Event;
import java.time.LocalDate;
import java.time.LocalTime;

public class EventDTO {
    private Long id;
    private String title;
    private String description;
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

    public EventDTO() {}

    public EventDTO(Event event, long registrationCount) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.description = event.getDescription();
        this.category = event.getCategory();
        this.venue = event.getVenue();
        this.eventDate = event.getEventDate();
        this.eventTime = event.getEventTime();
        this.registrationDeadline = event.getRegistrationDeadline();
        this.maxCapacity = event.getMaxCapacity();
        this.bannerUrl = event.getBannerUrl();
        this.creatorName = event.getCreatedBy().getFullName();
        this.status = event.getStatus();
        this.registrationCount = registrationCount;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
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
}
