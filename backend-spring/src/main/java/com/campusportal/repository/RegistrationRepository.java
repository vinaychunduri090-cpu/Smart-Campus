package com.campusportal.repository;

import com.campusportal.model.Registration;
import com.campusportal.model.Event;
import com.campusportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByUserAndStatusNot(User user, String status);
    List<Registration> findByEventAndStatusNot(Event event, String status);
    Optional<Registration> findByEventAndUser(Event event, User user);
    long countByEventAndStatusNot(Event event, String status);
}
