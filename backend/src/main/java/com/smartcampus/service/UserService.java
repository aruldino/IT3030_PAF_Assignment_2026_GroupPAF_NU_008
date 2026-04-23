package com.smartcampus.service;

import com.smartcampus.enums.UserRole;
import com.smartcampus.enums.UserStatus;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.model.User;
import com.smartcampus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * User management service — Member 4 responsibility.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /** Get all users — ADMIN only */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /** Get user by ID */
    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    /** Change a user's role — ADMIN only */
    public User updateUserRole(String id, UserRole newRole) {
        User user = getUserById(id);
        user.setRole(newRole);
        return userRepository.save(user);
    }

    /** Update logged-in user's own name */
    public User updateMyName(String email, String name) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setName(name);
        return userRepository.save(user);
    }

    /** Approve a user — ADMIN only */
    public User approveUser(String id) {
        User user = getUserById(id);
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    /** Suspend a user — ADMIN only */
    public User suspendUser(String id) {
        User user = getUserById(id);
        user.setStatus(UserStatus.SUSPENDED);
        return userRepository.save(user);
    }

    /** Reactivate a suspended user — ADMIN only */
    public User reactivateUser(String id) {
        User user = getUserById(id);
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    /** Get count of pending users */
    public long getPendingCount() {
        return userRepository.findAll().stream()
                .filter(u -> u.getStatus() == UserStatus.PENDING)
                .count();
    }

    /** Deactivate/delete a user — ADMIN only */
    public void deleteUser(String id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }
}