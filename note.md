# Team Contributions - PAF Assignment 2026

**Group:** PAF_NU_008  
**Project:** Smart Campus Management System  
**Date:** April 23, 2026

---

## Member 1: Facilities Catalogue + Resource Management

**Name:** Vinu Shan  
**GitHub Email:** vinushan2218@gmail.com  
**Module:** Facilities & Resources Management  
**Total Commits:** 3

### Key Responsibilities:
- Facilities catalogue endpoint development
- Resource management endpoints
- Resource capacity validation and management

### Features Implemented:
- ✅ Resource search validation
- ✅ Resource capacity guard checks
- ✅ Centralized resource field sanitization
- ✅ API endpoints for resource management
- ✅ Regression tests for resource capacity validation

### Related Details:
| Commit Hash | Description |
|---|---|
| a982cd3 | refactor(facilities): centralize resource field sanitization |
| aa82c0a | feat(facilities): validate search and resource capacity inputs |
| 4a6d167 | test(facilities): add resource capacity guard regression tests |

**Branch:** origin/module-facilities  
**Status:** ✅ Merged to main

---

## Member 2: Booking Workflow + Conflict Checking

**Name:** Karunakanthan Lavan  
**GitHub Email:** karunakanthanlavan@gmail.com  
**Module:** Booking Management  
**Total Commits:** 3

### Key Responsibilities:
- Booking workflow implementation
- Conflict checking mechanisms
- Booking lifecycle management
- Academic period handling

### Features Implemented:
- ✅ Booking lifecycle service tests
- ✅ Conflict detection and validation
- ✅ Optional academic period field normalization
- ✅ Manager and staff access control
- ✅ Advanced booking query capabilities

### Related Details:
| Commit Hash | Description |
|---|---|
| 90d6275 | refactor(bookings): extract manager and staff access helper |
| 0e34798 | fix(bookings): normalize optional academic period fields |
| 01e3486 | test(bookings): add lifecycle and optional-field service tests |

**Branch:** origin/module-booking  
**Status:** ✅ Merged to main

---

## Member 3: Incident Tickets + Attachments + Technician Updates

**Name:** Pakeerathan Sinthujan  
**GitHub Email:** pakeerathansinthujan2003@gmail.com  
**Module:** Maintenance & Incident Management  
**Total Commits:** 3

### Key Responsibilities:
- Incident ticket system implementation
- Attachment management
- Technician assignment and updates
- Status transition enforcement

### Features Implemented:
- ✅ Safe ticket status transitions
- ✅ Assignment and comment ownership rules
- ✅ Identity normalization in service layer
- ✅ Permission-based ticket operations
- ✅ Status transition validation with regression tests

### Related Details:
| Commit Hash | Description |
|---|---|
| 95ca5e9 | refactor(maintenance): centralize identity normalization in service |
| cbb76e5 | feat(maintenance): enforce safe ticket status transitions |
| 8b04748 | test(maintenance): cover invalid status transition scenarios |

**Branch:** origin/module-maintenance  
**Status:** ✅ Merged to main

---

## Member 4: Notifications + Role Management + OAuth Integration

**Name:** Aruldino  
**GitHub Email:** aruldino2025@gmail.com (Primary), aruldino17@gmail.com (Secondary)  
**Module:** Notifications, Authentication & Authorization  
**Total Commits:** 19 (7 + 12)

### Key Responsibilities:
- Notification system development
- Role-based access control (RBAC)
- OAuth 2.0 integration (Google)
- Database configuration
- Project structure and architecture

### Features Implemented:
- ✅ Notification system with preferences
- ✅ Read status tracking for notifications
- ✅ Google OAuth2 integration
- ✅ OAuth2 success and failure handlers
- ✅ PostgreSQL database auto-creation
- ✅ RoleAccess utility for permission management
- ✅ Booking expiration scheduling
- ✅ Logging for notification retrieval
- ✅ Project refactoring and structure optimization
- ✅ Backend environment configuration support

### Related Details:
| Commit Hash | Description |
|---|---|
| a566022 | feat: add RoleAccess utility class for user role management and permissions |
| 854622f | feat: Enable scheduling for booking expirations |
| 2834e8d | feat: Add Notification feature with preferences management |
| 33f6239 | feat: add logging for notification retrieval in NotificationController |
| 998d063 | feat: Implement Google OAuth2 integration with success and failure handlers |
| c455986 | feat: Add PostgreSQL database auto-creation and Google OAuth2 API key support |
| 08b5341 | feat: Implement notification system with preferences and read status |
| bf0788b | chore(backend): support backend-local and root env loading |
| e9f7b88 | refactor(repo): split project into frontend/backend structure |

**Branches:** Multiple integrations and merges  
**Status:** ✅ Lead developer on multiple features, coordinated integrations

---

## Summary Statistics

| Member | Email | Commits | Module | Status |
|--------|-------|---------|--------|--------|
| Vinu Shan | vinushan2218@gmail.com | 3 | Facilities | ✅ Complete |
| Karunakanthan Lavan | karunakanthanlavan@gmail.com | 3 | Bookings | ✅ Complete |
| Pakeerathan Sinthujan | pakeerathansinthujan2003@gmail.com | 3 | Maintenance | ✅ Complete |
| Aruldino | aruldino2025@gmail.com | 19 | Notifications & Auth | ✅ Complete |
| **TOTAL** | - | **28 commits** | All Modules | ✅ **Integrated** |

---

## Project Structure Overview

```
├── backend/                          # Spring Boot application
│   └── src/main/java/com/campus/smart_campus/
│       ├── modules/
│       │   ├── facilities/           # Member 1: Resource management
│       │   ├── bookings/             # Member 2: Booking workflow
│       │   ├── maintenance/          # Member 3: Incident tickets
│       │   ├── notifications/        # Member 4: Notification system
│       │   ├── auth/                 # Member 4: OAuth & Auth
│       │   └── users/                # Role management
│       └── common/
│           ├── config/               # OAuth & DB config
│           ├── error/                # Error handling
│           └── web/                  # Web utilities
├── frontend/                         # React/TypeScript application
│   └── src/modules/
│       ├── facilities/               # Resource UI
│       ├── bookings/                 # Booking UI
│       ├── maintenance/              # Ticket UI
│       └── notifications/            # Notification UI
```

---

## Integration Status

✅ **All Modules Integrated and Tested**
- Module branches successfully merged to main
- Cross-module testing completed
- OAuth and role-based access control implemented
- Notification system fully functional

---

## Additional Notes

- **Project Lead:** Aruldino - Coordinated integration and maintained project structure
- **Development Timeline:** Sequential module development with parallel integration
- **Testing:** Comprehensive unit and integration tests implemented for each module
- **Documentation:** Inline code documentation and API specifications maintained
