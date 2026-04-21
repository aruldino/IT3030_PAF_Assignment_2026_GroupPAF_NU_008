package com.campus.smart_campus.modules.auth.service;

import com.campus.smart_campus.modules.users.model.UserRole;

public final class RoleAccess {

    private RoleAccess() {
    }

    public static UserRole normalize(UserRole role) {
        if (role == null) {
            return UserRole.STUDENT;
        }
        return role == UserRole.USER ? UserRole.STUDENT : role;
    }

    public static int rank(UserRole role) {
        return switch (normalize(role)) {
            case STUDENT -> 1;
            case TECHNICIAN -> 2;
            case STAFF -> 3;
            case ADMIN -> 4;
            case SUPER_ADMIN -> 5;
            case USER -> 1;
        };
    }

    public static boolean atLeast(UserRole actual, UserRole minimum) {
        return rank(actual) >= rank(minimum);
    }

    public static boolean canViewResources(UserRole role) {
        return true;
    }

    public static boolean canCreateBookings(UserRole role) {
        UserRole normalized = normalize(role);
        return normalized == UserRole.STUDENT
                || normalized == UserRole.STAFF
                || normalized == UserRole.ADMIN
                || normalized == UserRole.SUPER_ADMIN;
    }

    public static boolean canViewBookings(UserRole role) {
        UserRole normalized = normalize(role);
        return normalized == UserRole.STUDENT
                || normalized == UserRole.STAFF
                || normalized == UserRole.ADMIN
                || normalized == UserRole.SUPER_ADMIN;
    }

    public static boolean canManageBookings(UserRole role) {
        UserRole normalized = normalize(role);
        return normalized == UserRole.ADMIN || normalized == UserRole.SUPER_ADMIN;
    }

    public static boolean canCreateTickets(UserRole role) {
        UserRole normalized = normalize(role);
        return normalized == UserRole.STUDENT
                || normalized == UserRole.STAFF
                || normalized == UserRole.ADMIN
                || normalized == UserRole.SUPER_ADMIN;
    }

    public static boolean canManageTickets(UserRole role) {
        UserRole normalized = normalize(role);
        return normalized == UserRole.TECHNICIAN
                || normalized == UserRole.ADMIN
                || normalized == UserRole.SUPER_ADMIN;
    }

    public static boolean canManageResources(UserRole role) {
        UserRole normalized = normalize(role);
        return normalized == UserRole.ADMIN || normalized == UserRole.SUPER_ADMIN;
    }

    public static boolean canManageAnnouncements(UserRole role) {
        UserRole normalized = normalize(role);
        return normalized == UserRole.ADMIN || normalized == UserRole.SUPER_ADMIN;
    }

    public static boolean canViewUsers(UserRole role) {
        UserRole normalized = normalize(role);
        return normalized == UserRole.ADMIN || normalized == UserRole.SUPER_ADMIN;
    }

    public static boolean canManageRoles(UserRole role) {
        UserRole normalized = normalize(role);
        return normalized == UserRole.ADMIN || normalized == UserRole.SUPER_ADMIN;
    }

    public static boolean canDeleteAnyAccount(UserRole role) {
        return normalize(role) == UserRole.SUPER_ADMIN;
    }
}


