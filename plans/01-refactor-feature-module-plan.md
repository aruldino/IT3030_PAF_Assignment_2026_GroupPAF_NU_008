# Plan 01: Feature-Module Refactor (File-by-File)

## Goal

Refactor the backend and frontend from a layered structure into feature modules while keeping all current API contracts and URLs stable.

## Non-Breaking Constraints

- Keep existing endpoint paths and HTTP methods unchanged.
- Keep DTO field names and response JSON unchanged.
- Keep session-based authentication behavior unchanged.
- Keep frontend route/hash behavior unchanged during migration.
- Move files in small slices and compile/test after each slice.

## Target Backend Structure

```
src/main/java/com/campus/smart_campus/
  common/
    config/
    error/
    web/
  modules/
    resources/
    bookings/
    maintenance/
    notifications/
    announcements/
    auth/
    users/
    dashboard/
```

## Backend File-by-File Mapping

| Current File                                                                      | Target File                                                                                           |
| --------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------- |
| src/main/java/com/campus/smart_campus/SmartCampusApplication.java                 | src/main/java/com/campus/smart_campus/SmartCampusApplication.java                                     |
| src/main/java/com/campus/smart_campus/config/AuthConfig.java                      | src/main/java/com/campus/smart_campus/common/config/AuthConfig.java                                   |
| src/main/java/com/campus/smart_campus/config/AuthInterceptor.java                 | src/main/java/com/campus/smart_campus/common/config/AuthInterceptor.java                              |
| src/main/java/com/campus/smart_campus/config/DataInitializer.java                 | src/main/java/com/campus/smart_campus/common/config/DataInitializer.java                              |
| src/main/java/com/campus/smart_campus/config/DataSourceBootstrapConfig.java       | src/main/java/com/campus/smart_campus/common/config/DataSourceBootstrapConfig.java                    |
| src/main/java/com/campus/smart_campus/config/GoogleOAuth2ClientConfig.java        | src/main/java/com/campus/smart_campus/modules/auth/config/GoogleOAuth2ClientConfig.java               |
| src/main/java/com/campus/smart_campus/config/GoogleOAuth2FailureHandler.java      | src/main/java/com/campus/smart_campus/modules/auth/config/GoogleOAuth2FailureHandler.java             |
| src/main/java/com/campus/smart_campus/config/GoogleOAuth2Properties.java          | src/main/java/com/campus/smart_campus/modules/auth/config/GoogleOAuth2Properties.java                 |
| src/main/java/com/campus/smart_campus/config/GoogleOAuth2SuccessHandler.java      | src/main/java/com/campus/smart_campus/modules/auth/config/GoogleOAuth2SuccessHandler.java             |
| src/main/java/com/campus/smart_campus/config/SecurityConfig.java                  | src/main/java/com/campus/smart_campus/modules/auth/config/SecurityConfig.java                         |
| src/main/java/com/campus/smart_campus/config/WebConfig.java                       | src/main/java/com/campus/smart_campus/common/config/WebConfig.java                                    |
| src/main/java/com/campus/smart_campus/controller/AnnouncementController.java      | src/main/java/com/campus/smart_campus/modules/announcements/controller/AnnouncementController.java    |
| src/main/java/com/campus/smart_campus/controller/AuthController.java              | src/main/java/com/campus/smart_campus/modules/auth/controller/AuthController.java                     |
| src/main/java/com/campus/smart_campus/controller/BookingController.java           | src/main/java/com/campus/smart_campus/modules/bookings/controller/BookingController.java              |
| src/main/java/com/campus/smart_campus/controller/DashboardController.java         | src/main/java/com/campus/smart_campus/modules/dashboard/controller/DashboardController.java           |
| src/main/java/com/campus/smart_campus/controller/FaviconController.java           | src/main/java/com/campus/smart_campus/common/web/FaviconController.java                               |
| src/main/java/com/campus/smart_campus/controller/GlobalExceptionHandler.java      | src/main/java/com/campus/smart_campus/common/error/GlobalExceptionHandler.java                        |
| src/main/java/com/campus/smart_campus/controller/HealthController.java            | src/main/java/com/campus/smart_campus/common/web/HealthController.java                                |
| src/main/java/com/campus/smart_campus/controller/MaintenanceController.java       | src/main/java/com/campus/smart_campus/modules/maintenance/controller/MaintenanceController.java       |
| src/main/java/com/campus/smart_campus/controller/NotificationController.java      | src/main/java/com/campus/smart_campus/modules/notifications/controller/NotificationController.java    |
| src/main/java/com/campus/smart_campus/controller/ResourceController.java          | src/main/java/com/campus/smart_campus/modules/resources/controller/ResourceController.java            |
| src/main/java/com/campus/smart_campus/controller/RootController.java              | src/main/java/com/campus/smart_campus/common/web/RootController.java                                  |
| src/main/java/com/campus/smart_campus/controller/UserManagementController.java    | src/main/java/com/campus/smart_campus/modules/users/controller/UserManagementController.java          |
| src/main/java/com/campus/smart_campus/dto/AnnouncementRequest.java                | src/main/java/com/campus/smart_campus/modules/announcements/dto/AnnouncementRequest.java              |
| src/main/java/com/campus/smart_campus/dto/ApiError.java                           | src/main/java/com/campus/smart_campus/common/error/ApiError.java                                      |
| src/main/java/com/campus/smart_campus/dto/BookingRequest.java                     | src/main/java/com/campus/smart_campus/modules/bookings/dto/BookingRequest.java                        |
| src/main/java/com/campus/smart_campus/dto/BookingSlotResponse.java                | src/main/java/com/campus/smart_campus/modules/bookings/dto/BookingSlotResponse.java                   |
| src/main/java/com/campus/smart_campus/dto/DashboardSummary.java                   | src/main/java/com/campus/smart_campus/modules/dashboard/dto/DashboardSummary.java                     |
| src/main/java/com/campus/smart_campus/dto/LoginRequest.java                       | src/main/java/com/campus/smart_campus/modules/auth/dto/LoginRequest.java                              |
| src/main/java/com/campus/smart_campus/dto/MaintenanceAttachmentRequest.java       | src/main/java/com/campus/smart_campus/modules/maintenance/dto/MaintenanceAttachmentRequest.java       |
| src/main/java/com/campus/smart_campus/dto/MaintenanceCommentEditRequest.java      | src/main/java/com/campus/smart_campus/modules/maintenance/dto/MaintenanceCommentEditRequest.java      |
| src/main/java/com/campus/smart_campus/dto/MaintenanceCommentRequest.java          | src/main/java/com/campus/smart_campus/modules/maintenance/dto/MaintenanceCommentRequest.java          |
| src/main/java/com/campus/smart_campus/dto/MaintenanceCommentResponse.java         | src/main/java/com/campus/smart_campus/modules/maintenance/dto/MaintenanceCommentResponse.java         |
| src/main/java/com/campus/smart_campus/dto/MaintenanceRequest.java                 | src/main/java/com/campus/smart_campus/modules/maintenance/dto/MaintenanceRequest.java                 |
| src/main/java/com/campus/smart_campus/dto/MaintenanceSlaResponse.java             | src/main/java/com/campus/smart_campus/modules/maintenance/dto/MaintenanceSlaResponse.java             |
| src/main/java/com/campus/smart_campus/dto/NotificationPreferenceRequest.java      | src/main/java/com/campus/smart_campus/modules/notifications/dto/NotificationPreferenceRequest.java    |
| src/main/java/com/campus/smart_campus/dto/NotificationResponse.java               | src/main/java/com/campus/smart_campus/modules/notifications/dto/NotificationResponse.java             |
| src/main/java/com/campus/smart_campus/dto/RegisterRequest.java                    | src/main/java/com/campus/smart_campus/modules/auth/dto/RegisterRequest.java                           |
| src/main/java/com/campus/smart_campus/dto/ResourceAvailabilityResponse.java       | src/main/java/com/campus/smart_campus/modules/resources/dto/ResourceAvailabilityResponse.java         |
| src/main/java/com/campus/smart_campus/dto/ResourceRequest.java                    | src/main/java/com/campus/smart_campus/modules/resources/dto/ResourceRequest.java                      |
| src/main/java/com/campus/smart_campus/dto/ResourceUsageResponse.java              | src/main/java/com/campus/smart_campus/modules/resources/dto/ResourceUsageResponse.java                |
| src/main/java/com/campus/smart_campus/dto/RoleUpdateRequest.java                  | src/main/java/com/campus/smart_campus/modules/users/dto/RoleUpdateRequest.java                        |
| src/main/java/com/campus/smart_campus/dto/UserProfileResponse.java                | src/main/java/com/campus/smart_campus/modules/users/dto/UserProfileResponse.java                      |
| src/main/java/com/campus/smart_campus/exception/BusinessException.java            | src/main/java/com/campus/smart_campus/common/error/BusinessException.java                             |
| src/main/java/com/campus/smart_campus/exception/ForbiddenException.java           | src/main/java/com/campus/smart_campus/common/error/ForbiddenException.java                            |
| src/main/java/com/campus/smart_campus/exception/NotFoundException.java            | src/main/java/com/campus/smart_campus/common/error/NotFoundException.java                             |
| src/main/java/com/campus/smart_campus/exception/UnauthorizedException.java        | src/main/java/com/campus/smart_campus/common/error/UnauthorizedException.java                         |
| src/main/java/com/campus/smart_campus/model/Announcement.java                     | src/main/java/com/campus/smart_campus/modules/announcements/model/Announcement.java                   |
| src/main/java/com/campus/smart_campus/model/AppUser.java                          | src/main/java/com/campus/smart_campus/modules/users/model/AppUser.java                                |
| src/main/java/com/campus/smart_campus/model/Booking.java                          | src/main/java/com/campus/smart_campus/modules/bookings/model/Booking.java                             |
| src/main/java/com/campus/smart_campus/model/BookingStatus.java                    | src/main/java/com/campus/smart_campus/modules/bookings/model/BookingStatus.java                       |
| src/main/java/com/campus/smart_campus/model/MaintenancePriority.java              | src/main/java/com/campus/smart_campus/modules/maintenance/model/MaintenancePriority.java              |
| src/main/java/com/campus/smart_campus/model/MaintenanceStatus.java                | src/main/java/com/campus/smart_campus/modules/maintenance/model/MaintenanceStatus.java                |
| src/main/java/com/campus/smart_campus/model/MaintenanceTicket.java                | src/main/java/com/campus/smart_campus/modules/maintenance/model/MaintenanceTicket.java                |
| src/main/java/com/campus/smart_campus/model/Resource.java                         | src/main/java/com/campus/smart_campus/modules/resources/model/Resource.java                           |
| src/main/java/com/campus/smart_campus/model/ResourceStatus.java                   | src/main/java/com/campus/smart_campus/modules/resources/model/ResourceStatus.java                     |
| src/main/java/com/campus/smart_campus/model/ResourceType.java                     | src/main/java/com/campus/smart_campus/modules/resources/model/ResourceType.java                       |
| src/main/java/com/campus/smart_campus/model/UserRole.java                         | src/main/java/com/campus/smart_campus/modules/users/model/UserRole.java                               |
| src/main/java/com/campus/smart_campus/repository/AnnouncementRepository.java      | src/main/java/com/campus/smart_campus/modules/announcements/repository/AnnouncementRepository.java    |
| src/main/java/com/campus/smart_campus/repository/AppUserRepository.java           | src/main/java/com/campus/smart_campus/modules/users/repository/AppUserRepository.java                 |
| src/main/java/com/campus/smart_campus/repository/BookingRepository.java           | src/main/java/com/campus/smart_campus/modules/bookings/repository/BookingRepository.java              |
| src/main/java/com/campus/smart_campus/repository/MaintenanceTicketRepository.java | src/main/java/com/campus/smart_campus/modules/maintenance/repository/MaintenanceTicketRepository.java |
| src/main/java/com/campus/smart_campus/repository/ResourceRepository.java          | src/main/java/com/campus/smart_campus/modules/resources/repository/ResourceRepository.java            |
| src/main/java/com/campus/smart_campus/service/AnnouncementService.java            | src/main/java/com/campus/smart_campus/modules/announcements/service/AnnouncementService.java          |
| src/main/java/com/campus/smart_campus/service/AuthService.java                    | src/main/java/com/campus/smart_campus/modules/auth/service/AuthService.java                           |
| src/main/java/com/campus/smart_campus/service/BookingExpiryScheduler.java         | src/main/java/com/campus/smart_campus/modules/bookings/service/BookingExpiryScheduler.java            |
| src/main/java/com/campus/smart_campus/service/BookingService.java                 | src/main/java/com/campus/smart_campus/modules/bookings/service/BookingService.java                    |
| src/main/java/com/campus/smart_campus/service/DashboardService.java               | src/main/java/com/campus/smart_campus/modules/dashboard/service/DashboardService.java                 |
| src/main/java/com/campus/smart_campus/service/MaintenanceService.java             | src/main/java/com/campus/smart_campus/modules/maintenance/service/MaintenanceService.java             |
| src/main/java/com/campus/smart_campus/service/NotificationService.java            | src/main/java/com/campus/smart_campus/modules/notifications/service/NotificationService.java          |
| src/main/java/com/campus/smart_campus/service/ResourceService.java                | src/main/java/com/campus/smart_campus/modules/resources/service/ResourceService.java                  |
| src/main/java/com/campus/smart_campus/service/RoleAccess.java                     | src/main/java/com/campus/smart_campus/modules/auth/service/RoleAccess.java                            |

## Target Frontend Structure

```
frontend/src/
  app/
  modules/
    facilities/
    bookings/
    maintenance/
    notifications/
    auth/
    users/
    dashboard/
  shared/
    components/
    styles/
```

## Frontend File-by-File Mapping

| Current File                                   | Target File                                                                       |
| ---------------------------------------------- | --------------------------------------------------------------------------------- |
| frontend/src/main.tsx                          | frontend/src/main.tsx                                                             |
| frontend/src/app/App.tsx                       | frontend/src/app/App.tsx                                                          |
| frontend/src/pages/PortalPage.tsx              | frontend/src/app/PortalPage.tsx                                                   |
| frontend/src/PortalApp.tsx                     | frontend/src/app/PortalApp.tsx                                                    |
| frontend/src/components/ResourcesList.jsx      | frontend/src/modules/facilities/components/ResourcesList.jsx                      |
| frontend/src/components/ResourceForm.jsx       | frontend/src/modules/facilities/components/ResourceForm.jsx                       |
| frontend/src/components/BookingForm.jsx        | frontend/src/modules/bookings/components/BookingForm.jsx                          |
| frontend/src/components/MaintenanceList.jsx    | frontend/src/modules/maintenance/components/MaintenanceList.jsx                   |
| frontend/src/components/AnnouncementsList.jsx  | frontend/src/modules/notifications/components/AnnouncementsList.jsx               |
| frontend/src/components/Header.tsx             | frontend/src/shared/components/Header.tsx                                         |
| frontend/src/components/PanelHeader.tsx        | frontend/src/shared/components/PanelHeader.tsx                                    |
| frontend/src/components/StatCard.tsx           | frontend/src/shared/components/StatCard.tsx                                       |
| frontend/src/shared/components/PanelHeader.tsx | frontend/src/shared/components/PanelHeader.tsx (keep canonical, remove duplicate) |
| frontend/src/shared/components/StatCard.tsx    | frontend/src/shared/components/StatCard.tsx (keep canonical, remove duplicate)    |
| frontend/src/portal.css                        | frontend/src/shared/styles/portal.css                                             |
| frontend/src/index.css                         | frontend/src/shared/styles/index.css                                              |
| frontend/src/App.css                           | frontend/src/shared/styles/legacy-app.css                                         |
| frontend/src/App.jsx                           | remove after migration                                                            |
| frontend/src/PortalApp.jsx                     | remove after migration                                                            |
| frontend/src/main.jsx                          | keep stub or remove after migration validation                                    |

## Execution Sequence

1. Create the new package/module folders in backend and frontend.
2. Move only one backend module at a time, starting with resources.
3. Fix imports, compile, and run backend tests after each module move.
4. Move frontend components one module at a time and fix imports.
5. Keep endpoint strings and request payload shapes unchanged.
6. Remove duplicates and legacy files only after all references are migrated.
7. Run full verification (backend tests, frontend build, manual smoke checks).

## Validation Gate Per Slice

- Backend: `./mvnw -Dmaven.repo.local=.m2repo test`
- Frontend: `npm --prefix frontend run build`
- Manual: login, resources list/create, booking create/approve, maintenance status update, notification list.
