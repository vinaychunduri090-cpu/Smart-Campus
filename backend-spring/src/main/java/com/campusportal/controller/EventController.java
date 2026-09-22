package com.campusportal.controller;

import com.campusportal.dto.EventDTO;
import com.campusportal.dto.UserDTO;
import com.campusportal.model.Event;
import com.campusportal.model.User;
import com.campusportal.repository.EventRepository;
import com.campusportal.repository.RegistrationRepository;
import com.campusportal.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;

    public EventController(EventRepository eventRepository, RegistrationRepository registrationRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAllEvents() {
        List<EventDTO> events = eventRepository.findAll().stream()
                .map(event -> new EventDTO(event, registrationRepository.countByEventAndStatusNot(event, "cancelled")))
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("events", events);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEventById(@PathVariable Long id) {
        return eventRepository.findById(id)
                .map(event -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("event", new EventDTO(event, registrationRepository.countByEventAndStatusNot(event, "cancelled")));
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "error", "Event not found")));
    }

    @GetMapping("/my/created")
    public ResponseEntity<?> getMyCreatedEvents(HttpSession session) {
        UserDTO sessionUser = (UserDTO) session.getAttribute("user");
        if (sessionUser == null || (!sessionUser.getRole().equals("admin") && !sessionUser.getRole().equals("faculty"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "error", "Unauthorized"));
        }

        User creator = userRepository.findById(sessionUser.getId()).orElse(null);
        List<EventDTO> events = eventRepository.findByCreatedBy(creator).stream()
                .map(event -> new EventDTO(event, registrationRepository.countByEventAndStatusNot(event, "cancelled")))
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("events", events);
        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> createEvent(
            @RequestParam("title") String title,
            @RequestParam("category") String category,
            @RequestParam("description") String description,
            @RequestParam("venue") String venue,
            @RequestParam("event_date") String eventDateStr,
            @RequestParam("event_time") String eventTimeStr,
            @RequestParam("registration_deadline") String registrationDeadlineStr,
            @RequestParam("max_capacity") Integer maxCapacity,
            @RequestParam(value = "banner", required = false) MultipartFile banner,
            HttpSession session) {
        
        UserDTO sessionUser = (UserDTO) session.getAttribute("user");
        if (sessionUser == null || (!sessionUser.getRole().equals("admin") && !sessionUser.getRole().equals("faculty"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "error", "Unauthorized"));
        }

        User creator = userRepository.findById(sessionUser.getId()).orElse(null);
        if (creator == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "error", "Creator not found"));
        }

        Event event = new Event();
        event.setTitle(title);
        event.setCategory(category);
        event.setDescription(description);
        event.setVenue(venue);
        event.setEventDate(LocalDate.parse(eventDateStr));
        event.setEventTime(LocalTime.parse(eventTimeStr));
        event.setRegistrationDeadline(LocalDate.parse(registrationDeadlineStr));
        event.setMaxCapacity(maxCapacity);
        event.setCreatedBy(creator);
        event.setStatus("upcoming");

        if (banner != null && !banner.isEmpty()) {
            try {
                // Sanitize filename: remove spaces and special characters
                String originalFilename = banner.getOriginalFilename();
                String sanitizedName = originalFilename != null ? originalFilename.replaceAll("[^a-zA-Z0-9.\\-]", "_") : "banner.png";
                String fileName = UUID.randomUUID().toString() + "_" + sanitizedName;
                
                // Smart Discovery for saving location
                String[] locationsToTry = {
                    "uploads/",
                    "../uploads/",
                    "../../uploads/",
                    "./campusPortal-main/campusPortal-main/uploads/",
                    "./server/uploads/"
                };
                
                Path uploadPath = null;
                for (String loc : locationsToTry) {
                    Path p = Paths.get(loc).toAbsolutePath().normalize();
                    if (Files.exists(p) && Files.isDirectory(p)) {
                        uploadPath = p;
                        break;
                    }
                }

                if (uploadPath == null) {
                    // Fallback: create it in parent if nothing else found
                    uploadPath = Paths.get("../uploads").toAbsolutePath().normalize();
                }
                
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Files.copy(banner.getInputStream(), uploadPath.resolve(fileName));
                event.setBannerUrl("/uploads/" + fileName);
                System.out.println("!!! SAVED BANNER TO: " + uploadPath.resolve(fileName));
            } catch (IOException e) {
                System.err.println("Failed to save banner: " + e.getMessage());
            }
        }

        Event savedEvent = eventRepository.save(event);
        return ResponseEntity.ok(Map.of("success", true, "eventId", savedEvent.getId()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @RequestBody Map<String, Object> updates, HttpSession session) {
        UserDTO sessionUser = (UserDTO) session.getAttribute("user");
        if (sessionUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "error", "Not authenticated"));
        }

        return eventRepository.findById(id)
                .map(event -> {
                    if (!sessionUser.getRole().equals("admin") && !event.getCreatedBy().getId().equals(sessionUser.getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "error", "Not authorized"));
                    }

                    if (updates.containsKey("title")) event.setTitle((String) updates.get("title"));
                    if (updates.containsKey("description")) event.setDescription((String) updates.get("description"));
                    if (updates.containsKey("category")) event.setCategory((String) updates.get("category"));
                    if (updates.containsKey("venue")) event.setVenue((String) updates.get("venue"));
                    if (updates.containsKey("status")) event.setStatus((String) updates.get("status"));
                    if (updates.containsKey("max_capacity")) event.setMaxCapacity((Integer) updates.get("max_capacity"));
                    if (updates.containsKey("event_date")) event.setEventDate(LocalDate.parse((String) updates.get("event_date")));
                    if (updates.containsKey("event_time")) event.setEventTime(LocalTime.parse((String) updates.get("event_time")));
                    if (updates.containsKey("registration_deadline")) event.setRegistrationDeadline(LocalDate.parse((String) updates.get("registration_deadline")));

                    eventRepository.save(event);
                    return ResponseEntity.ok(Map.of("success", true));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "error", "Event not found")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id, HttpSession session) {
        UserDTO sessionUser = (UserDTO) session.getAttribute("user");
        if (sessionUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "error", "Not authenticated"));
        }

        return eventRepository.findById(id)
                .map(event -> {
                    if (!sessionUser.getRole().equals("admin") && !event.getCreatedBy().getId().equals(sessionUser.getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "error", "Not authorized"));
                    }
                    eventRepository.delete(event);
                    return ResponseEntity.ok(Map.of("success", true));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "error", "Event not found")));
    }
}
