package com.campusportal.controller;

import com.campusportal.dto.LoginRequest;
import com.campusportal.dto.UserDTO;
import com.campusportal.model.User;
import com.campusportal.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        Optional<User> userOpt = userRepository.findByEmail(loginRequest.getEmail());
        
        if (userOpt.isPresent() && passwordEncoder.matches(loginRequest.getPassword(), userOpt.get().getPassword())) {
            User user = userOpt.get();
            UserDTO userDTO = new UserDTO(user);
            session.setAttribute("user", userDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", userDTO);
            return ResponseEntity.ok(response);
        }
        
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", "Invalid credentials");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User userRequest, HttpSession session) {
        if (userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Email already in use");
            return ResponseEntity.badRequest().body(error);
        }

        userRequest.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        if (userRequest.getRole() == null || !userRequest.getRole().equals("faculty")) {
            userRequest.setRole("student");
        }
        
        User savedUser = userRepository.save(userRequest);
        UserDTO userDTO = new UserDTO(savedUser);
        session.setAttribute("user", userDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("user", userDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", user);
            return ResponseEntity.ok(response);
        }
        
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", "Not authenticated");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
}
