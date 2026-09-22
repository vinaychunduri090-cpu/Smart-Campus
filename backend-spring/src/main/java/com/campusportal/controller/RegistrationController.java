package com.campusportal.controller;

import com.campusportal.dto.RegistrationDTO;
import com.campusportal.dto.UserDTO;
import com.campusportal.model.Event;
import com.campusportal.model.Registration;
import com.campusportal.model.User;
import com.campusportal.repository.EventRepository;
import com.campusportal.repository.RegistrationRepository;
import com.campusportal.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public RegistrationController(RegistrationRepository registrationRepository, EventRepository eventRepository, UserRepository userRepository) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/{eventId}")
    public ResponseEntity<?> registerForEvent(@PathVariable Long eventId, HttpSession session) {
        UserDTO sessionUser = (UserDTO) session.getAttribute("user");
        if (sessionUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "error", "Not authenticated"));
        }

        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "error", "Event not found"));
        }

        Event event = eventOpt.get();
        if (event.getRegistrationDeadline().isBefore(LocalDate.now())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Registration deadline has passed"));
        }

        long currentCount = registrationRepository.countByEventAndStatusNot(event, "cancelled");
        if (currentCount >= event.getMaxCapacity()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Event is at maximum capacity"));
        }

        User user = userRepository.findById(sessionUser.getId()).orElse(null);
        Optional<Registration> existing = registrationRepository.findByEventAndUser(event, user);
        
        if (existing.isPresent()) {
            Registration reg = existing.get();
            if (reg.getStatus().equals("cancelled")) {
                reg.setStatus("confirmed");
                registrationRepository.save(reg);
                return ResponseEntity.ok(Map.of("success", true, "message", "Re-registered successfully"));
            }
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Already registered"));
        }

        Registration registration = new Registration();
        registration.setEvent(event);
        registration.setUser(user);
        registration.setStatus("confirmed");
        registrationRepository.save(registration);

        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyRegistrations(HttpSession session) {
        UserDTO sessionUser = (UserDTO) session.getAttribute("user");
        if (sessionUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "error", "Not authenticated"));
        }

        User user = userRepository.findById(sessionUser.getId()).orElse(null);
        List<RegistrationDTO> registrations = registrationRepository.findByUserAndStatusNot(user, "cancelled").stream()
                .map(reg -> new RegistrationDTO(reg, registrationRepository.countByEventAndStatusNot(reg.getEvent(), "cancelled")))
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("registrations", registrations);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> getEventRegistrants(@PathVariable Long eventId, HttpSession session) {
        UserDTO sessionUser = (UserDTO) session.getAttribute("user");
        if (sessionUser == null || (!sessionUser.getRole().equals("admin") && !sessionUser.getRole().equals("faculty"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "error", "Unauthorized"));
        }

        return eventRepository.findById(eventId)
                .map(event -> {
                    if (!sessionUser.getRole().equals("admin") && !event.getCreatedBy().getId().equals(sessionUser.getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "error", "Not authorized"));
                    }

                    List<RegistrationDTO> registrants = registrationRepository.findByEventAndStatusNot(event, "cancelled").stream()
                            .map(reg -> new RegistrationDTO(reg, registrationRepository.countByEventAndStatusNot(event, "cancelled")))
                            .collect(Collectors.toList());
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("registrants", registrants);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "error", "Event not found")));
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<?> cancelRegistration(@PathVariable Long eventId, HttpSession session) {
        UserDTO sessionUser = (UserDTO) session.getAttribute("user");
        if (sessionUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "error", "Not authenticated"));
        }

        Optional<Event> eventOpt = eventRepository.findById(eventId);
        User user = userRepository.findById(sessionUser.getId()).orElse(null);
        
        if (eventOpt.isPresent() && user != null) {
            Optional<Registration> regOpt = registrationRepository.findByEventAndUser(eventOpt.get(), user);
            if (regOpt.isPresent()) {
                Registration reg = regOpt.get();
                reg.setStatus("cancelled");
                registrationRepository.save(reg);
                return ResponseEntity.ok(Map.of("success", true));
            }
        }
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "error", "Registration not found"));
    }
}
