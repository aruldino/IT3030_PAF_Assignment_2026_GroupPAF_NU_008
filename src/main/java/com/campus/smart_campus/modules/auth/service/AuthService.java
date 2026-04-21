package com.campus.smart_campus.modules.auth.service;

import com.campus.smart_campus.modules.auth.dto.LoginRequest;
import com.campus.smart_campus.modules.auth.dto.RegisterRequest;
import com.campus.smart_campus.modules.users.dto.UserProfileResponse;
import com.campus.smart_campus.common.error.BusinessException;
import com.campus.smart_campus.common.error.NotFoundException;
import com.campus.smart_campus.common.error.UnauthorizedException;
import com.campus.smart_campus.modules.users.model.AppUser;
import com.campus.smart_campus.modules.users.model.UserRole;
import com.campus.smart_campus.modules.users.repository.AppUserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AuthService {

    public static final String SESSION_USER_ID = "SMART_CAMPUS_USER_ID";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$"
    );

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserProfileResponse register(RegisterRequest request, HttpSession session) {
        String email = request.email().trim().toLowerCase();
        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("An account already exists with this email.");
        }

        if (request.role() == UserRole.SUPER_ADMIN) {
            throw new BusinessException("Super administrator accounts cannot be created through registration.");
        }

        validateFullName(request.fullName());
        validatePasswordStrength(request.password());
        AppUser user = new AppUser();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setActive(true);

        AppUser savedUser = appUserRepository.save(user);
        session.setAttribute(SESSION_USER_ID, savedUser.getId());
        return toProfile(savedUser);
    }

    public UserProfileResponse login(LoginRequest request, HttpSession session) {
        AppUser user = appUserRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

        if (!user.isActive()) {
            throw new UnauthorizedException("Your account is inactive. Please contact the administrator.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        session.setAttribute(SESSION_USER_ID, user.getId());
        return toProfile(user);
    }

    public UserProfileResponse loginOrCreateGoogleUser(String fullName, String email, HttpSession session) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail.isBlank()) {
            throw new UnauthorizedException("Google login did not return an email address.");
        }

        AppUser user = appUserRepository.findByEmailIgnoreCase(normalizedEmail)
                .map(existing -> {
                    if (!existing.isActive()) {
                        throw new UnauthorizedException("Your account is inactive. Please contact the administrator.");
                    }
                    if (needsGoogleDisplayName(existing.getFullName(), fullName)) {
                        existing.setFullName(fullName.trim());
                    }
                    return appUserRepository.save(existing);
                })
                .orElseGet(() -> {
                    AppUser created = new AppUser();
                    created.setFullName(resolveDisplayName(fullName, normalizedEmail));
                    created.setEmail(normalizedEmail);
                    created.setPasswordHash(passwordEncoder.encode(generateOAuthPlaceholderPassword()));
                    created.setRole(UserRole.USER);
                    created.setActive(true);
                    return appUserRepository.save(created);
                });

        session.setAttribute(SESSION_USER_ID, user.getId());
        return toProfile(user);
    }

    public UserProfileResponse loginAsAdminShortcut(String fullName, String email, HttpSession session) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail.isBlank()) {
            throw new UnauthorizedException("Admin shortcut email is not configured.");
        }

        AppUser user = appUserRepository.findByEmailIgnoreCase(normalizedEmail)
                .map(existing -> {
                    existing.setFullName(resolveDisplayName(fullName, normalizedEmail));
                    existing.setRole(UserRole.ADMIN);
                    existing.setActive(true);
                    if (existing.getPasswordHash() == null || existing.getPasswordHash().isBlank()) {
                        existing.setPasswordHash(passwordEncoder.encode(generateOAuthPlaceholderPassword()));
                    }
                    return appUserRepository.save(existing);
                })
                .orElseGet(() -> {
                    AppUser created = new AppUser();
                    created.setFullName(resolveDisplayName(fullName, normalizedEmail));
                    created.setEmail(normalizedEmail);
                    created.setPasswordHash(passwordEncoder.encode(generateOAuthPlaceholderPassword()));
                    created.setRole(UserRole.ADMIN);
                    created.setActive(true);
                    return appUserRepository.save(created);
                });

        session.setAttribute(SESSION_USER_ID, user.getId());
        return toProfile(user);
    }

    public UserProfileResponse getCurrentUser(HttpSession session) {
        Object userId = session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            throw new UnauthorizedException("Please log in to continue.");
        }

        AppUser user = appUserRepository.findById((Long) userId)
                .orElseThrow(() -> new NotFoundException("Authenticated user was not found."));

        if (!user.isActive()) {
            session.invalidate();
            throw new UnauthorizedException("Your account is inactive.");
        }

        return toProfile(user);
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }

    public void deleteCurrentUser(HttpSession session) {
        AppUser currentUser = getCurrentAppUser(session);
        ensureAtLeastOneSuperAdminRemains(currentUser.getId(), currentUser.getRole());
        appUserRepository.delete(currentUser);
        session.invalidate();
    }

    public List<UserProfileResponse> listUsers(HttpSession session) {
        requireViewUsers(session);
        return appUserRepository.findAll().stream()
                .map(this::toProfile)
                .toList();
    }

    public UserProfileResponse updateUserRole(Long userId, UserRole role, HttpSession session) {
        AppUser currentUser = requireManageRoles(session);
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User account was not found."));

        if (RoleAccess.normalize(currentUser.getRole()) == UserRole.ADMIN && user.getRole() == UserRole.SUPER_ADMIN) {
            throw new UnauthorizedException("Administrators cannot modify super administrator accounts.");
        }

        if (RoleAccess.normalize(currentUser.getRole()) == UserRole.ADMIN && RoleAccess.normalize(role) == UserRole.SUPER_ADMIN) {
            throw new UnauthorizedException("Administrators cannot promote accounts to super administrator.");
        }

        user.setRole(role);
        return toProfile(appUserRepository.save(user));
    }

    public void deleteUserById(Long id, HttpSession session) {
        AppUser currentUser = getCurrentAppUser(session);
        AppUser targetUser = appUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User account was not found."));

        if (RoleAccess.normalize(currentUser.getRole()) == UserRole.ADMIN
                && RoleAccess.normalize(targetUser.getRole()) == UserRole.SUPER_ADMIN) {
            throw new UnauthorizedException("Administrators cannot delete super administrator accounts.");
        }

        if (!RoleAccess.canDeleteAnyAccount(currentUser.getRole()) && RoleAccess.normalize(currentUser.getRole()) != UserRole.ADMIN) {
            throw new UnauthorizedException("You do not have permission to delete user accounts.");
        }

        ensureAtLeastOneSuperAdminRemains(targetUser.getId(), targetUser.getRole());
        appUserRepository.delete(targetUser);

        if (currentUser.getId().equals(targetUser.getId())) {
            session.invalidate();
        }
    }

    public AppUser createSeedUser(String fullName, String email, String rawPassword, com.campus.smart_campus.modules.users.model.UserRole role) {
        return appUserRepository.findByEmailIgnoreCase(email)
                .map(existing -> {
                    existing.setFullName(fullName);
                    existing.setEmail(email.toLowerCase());
                    existing.setPasswordHash(passwordEncoder.encode(rawPassword));
                    existing.setRole(role);
                    existing.setActive(true);
                    return appUserRepository.save(existing);
                })
                .orElseGet(() -> {
                    AppUser user = new AppUser();
                    user.setFullName(fullName);
                    user.setEmail(email.toLowerCase());
                    user.setPasswordHash(passwordEncoder.encode(rawPassword));
                    user.setRole(role);
                    user.setActive(true);
                    return appUserRepository.save(user);
                });
    }

    private UserProfileResponse toProfile(AppUser user) {
        return new UserProfileResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }

    private AppUser getCurrentAppUser(HttpSession session) {
        Object userId = session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            throw new UnauthorizedException("Please log in to continue.");
        }

        AppUser user = appUserRepository.findById((Long) userId)
                .orElseThrow(() -> new NotFoundException("Authenticated user was not found."));

        if (!user.isActive()) {
            session.invalidate();
            throw new UnauthorizedException("Your account is inactive.");
        }

        return user;
    }

    private UserProfileResponse requireSuperAdmin(HttpSession session) {
        AppUser currentUser = getCurrentAppUser(session);
        if (RoleAccess.normalize(currentUser.getRole()) != UserRole.SUPER_ADMIN) {
            throw new UnauthorizedException("Only a super administrator can access user management.");
        }
        return toProfile(currentUser);
    }

    private AppUser requireManageRoles(HttpSession session) {
        AppUser currentUser = getCurrentAppUser(session);
        if (!RoleAccess.canManageRoles(currentUser.getRole())) {
            throw new UnauthorizedException("Only administrators can manage roles.");
        }
        return currentUser;
    }

    private void requireViewUsers(HttpSession session) {
        AppUser currentUser = getCurrentAppUser(session);
        if (!RoleAccess.canViewUsers(currentUser.getRole())) {
            throw new UnauthorizedException("Only administrators can view user data.");
        }
    }

    private void ensureAtLeastOneSuperAdminRemains(Long targetUserId, UserRole targetRole) {
        if (targetRole == UserRole.SUPER_ADMIN && appUserRepository.countByRole(UserRole.SUPER_ADMIN) <= 1) {
            throw new BusinessException("At least one super administrator must remain in the system.");
        }
    }

    private void validateFullName(String fullName) {
        String normalized = fullName.trim();
        if (normalized.length() < 3) {
            throw new BusinessException("Full name must be at least 3 letters.");
        }
        if (!normalized.matches("^[A-Za-z ]+$")) {
            throw new BusinessException("Full name can contain only letters and spaces.");
        }
        long letterCount = normalized.chars().filter(Character::isLetter).count();
        if (letterCount < 3) {
            throw new BusinessException("Full name must contain at least 3 letters.");
        }
    }

    private void validatePasswordStrength(String password) {
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new BusinessException("Password must be at least 8 characters and include an uppercase letter, a lowercase letter, a number, and a symbol.");
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String resolveDisplayName(String fullName, String email) {
        String candidate = fullName == null ? "" : fullName.trim();
        if (!candidate.isBlank()) {
            return candidate;
        }

        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(0, atIndex) : email;
    }

    private boolean needsGoogleDisplayName(String currentName, String incomingName) {
        String existing = currentName == null ? "" : currentName.trim();
        String candidate = incomingName == null ? "" : incomingName.trim();
        return existing.isBlank() && !candidate.isBlank();
    }

    private String generateOAuthPlaceholderPassword() {
        return "oauth-" + UUID.randomUUID().toString().replace("-", "");
    }
}


