package com.campus.smart_campus.repository;

import com.campus.smart_campus.model.AppUser;
import com.campus.smart_campus.model.UserRole;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    long countByRole(UserRole role);
}
