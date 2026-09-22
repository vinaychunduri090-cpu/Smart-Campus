package com.campusportal.repository;

import com.campusportal.model.Event;
import com.campusportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatus(String status);
    List<Event> findByCreatedBy(User createdBy);
}
