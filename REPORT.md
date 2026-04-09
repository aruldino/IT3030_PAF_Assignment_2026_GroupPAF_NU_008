# Smart Campus Operations Hub Report

## 1. Project Title
Smart Campus Operations Hub

## 2. Project Overview
The Smart Campus Operations Hub is a modular web application built using Spring Boot for the backend and React for the frontend. The system centralizes campus resource management, booking approvals, maintenance ticket handling, notification delivery, and role-based access control into one platform.

This project is designed to reflect a real-world enterprise workflow with layered architecture, reusable DTOs, validation, global exception handling, RESTful APIs, and a responsive user interface.

## 3. Objectives
- Manage campus facilities and assets efficiently
- Handle booking requests with conflict detection and approvals
- Track maintenance incidents with technician assignment and status updates
- Provide notification support for operational updates
- Restrict access using role-based authorization
- Present a modern, dashboard-driven UI for users and administrators

## 4. Technology Stack
- Backend: Spring Boot, Spring Web, Spring Data JPA, Validation
- Frontend: React, TypeScript, Vite
- Database: JPA-based persistent relational database
- Security: Session-based authentication, role checks, interceptor-based endpoint protection
- CI/CD: GitHub Actions workflow for backend test and frontend build

## 5. System Architecture
The project follows a layered architecture:
- Controller layer for API endpoints
- Service layer for business rules and workflow logic
- Repository layer for database access
- DTO layer for request/response validation
- Global exception handler for clean API error responses

The frontend is structured as a modular React application with reusable components for the dashboard, headers, forms, lists, alerts, and notifications.

## 6. Module Breakdown

### Member 1: Facilities Catalogue + Resource Management
Responsibilities:
- Create, update, delete, and view resources
- Store metadata such as type, capacity, location, availability window, and status
- Search and filter resources
- Check resource availability
- Track resource status lifecycle
- Support CSV import
- Show most-used resource analytics

Implemented features:
- Resource CRUD
- Filtering by type, location, capacity, and status
- Smart suggestions
- Availability endpoint
- Analytics endpoint
- Bulk CSV import

### Member 2: Booking Workflow + Conflict Checking
Responsibilities:
- Create booking requests
- Review booking requests
- Approve, reject, or cancel bookings
- Prevent overlapping bookings
- Track user booking history
- Expire stale pending bookings

Implemented features:
- Booking creation
- Conflict checking
- Approve/reject/cancel workflow
- Booking history endpoint
- Auto-expiry job for pending bookings

Workflow:
- `PENDING -> APPROVED / REJECTED -> CANCELLED`

### Member 3: Incident Tickets + Attachments + Technician Updates
Responsibilities:
- Create incident tickets
- Manage ticket lifecycle
- Assign technicians
- Support comments and attachments
- Track SLA timing

Implemented features:
- Ticket creation
- Priority detection and priority selection
- Technician assignment
- Status updates
- Comment add/edit/delete rules
- Attachment limit of 3
- SLA tracking endpoint

Workflow:
- `OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED`

### Member 4: Notifications + Role Management + OAuth Integration Improvements
Responsibilities:
- Display notifications in the UI
- Manage notification preferences
- Support role-based access
- Handle login and user profile endpoints
- Provide OAuth-compatible login route naming

Implemented features:
- Notification panel
- Notification badge in header
- Mark-as-read support
- Notification preferences
- User role management
- Session-based authentication with protected endpoints
- Notification polling in frontend

Note:
- The current implementation provides secure session-based login and an OAuth-compatible route naming style. If full Google Sign-In is required, it can be integrated as a future enhancement.

## 7. Database Design Summary

### Main Entities
- `AppUser`
- `Resource`
- `Booking`
- `MaintenanceTicket`
- `Announcement`
- `Notification`

### Relationships
- A `Booking` belongs to one `Resource`
- A `MaintenanceTicket` belongs to one `Resource`
- A user session identifies the current authenticated `AppUser`
- Notifications are maintained for system events and UI display

## 8. REST API Summary

### Resources
- `GET /resources`
- `GET /resources/{id}`
- `GET /resources/{id}/availability`
- `POST /resources`
- `PUT /resources/{id}`
- `DELETE /resources/{id}`
- `GET /resources/suggestions?q=...`
- `GET /resources/analytics/most-booked`
- `POST /resources/import/csv`

### Bookings
- `GET /bookings`
- `GET /bookings/my`
- `GET /bookings/conflicts`
- `GET /bookings/history`
- `POST /bookings`
- `PUT /bookings/{id}/approve`
- `PUT /bookings/{id}/reject`
- `PUT /bookings/{id}/cancel`
- `PUT /bookings/expire-pending`

### Maintenance
- `GET /tickets`
- `GET /tickets/{id}/comments`
- `GET /tickets/{id}/sla`
- `POST /tickets`
- `POST /tickets/{id}/comments`
- `POST /tickets/{id}/attachments`
- `PUT /tickets/{id}/assign`
- `PUT /tickets/{id}/status`
- `PUT /tickets/{id}/comments/{commentId}`
- `DELETE /tickets/{id}/comments/{commentId}`

### Authentication and Users
- `POST /auth/login`
- `POST /auth/login/oauth`
- `POST /auth/register`
- `GET /users/me`
- `PUT /users/role`

### Notifications
- `GET /notifications`
- `POST /notifications/{id}/read`
- `GET /notifications/preferences`
- `PUT /notifications/preferences`

## 9. Frontend Pages and Components

### Main Pages
- Home page
- Dashboard page
- Resources page
- Bookings page
- Maintenance page
- Authentication page

### Major Components
- Header
- Dashboard panels
- Resource cards and forms
- Booking queue and booking form
- Ticket cards and forms
- Notification panel
- Alert history panel
- Announcement list

## 10. UI/UX Features
- Responsive layout
- Fixed top navigation bar
- Clean dashboard cards
- Status badges and pills
- Smart search and filters
- Toast alerts with auto-dismiss
- Notification badge in the header
- Modern form panels and card grids

## 11. Advanced and Innovation Features
- Resource analytics
- Smart search suggestions
- CSV upload/import
- Booking conflict detection
- Booking expiry automation
- Ticket SLA tracking
- Comment ownership rules
- Notification preferences
- Frontend notification polling
- Technician workflow support

## 12. Security and Validation
- Request validation for all major forms
- Global exception handling
- Role-based access checks
- Session validation for protected endpoints
- Clean error messages and HTTP status handling
- Secure file and comment handling rules in the ticket module

## 13. CI/CD and Version Control
A GitHub Actions workflow is included in:
- [`.github/workflows/ci.yml`](.github/workflows/ci.yml)

It performs:
- backend build and test
- frontend production build

## 14. Testing and Build Status
- Backend tests pass successfully
- Frontend production build passes successfully

## 15. Assignment Mapping
- Member 1: Facilities Catalogue and Resource Management
- Member 2: Booking Workflow and Conflict Checking
- Member 3: Incident Tickets and Technician Workflow
- Member 4: Notifications, Role Management, and Authentication

## 16. Viva-Ready Summary
This project is a Smart Campus Operations Hub that combines resource management, booking approvals, maintenance ticket handling, notifications, and secure role-based access into a single modular system. It uses Spring Boot for scalable backend APIs and React for a responsive frontend, with clean workflow handling, validation, analytics, and reusable UI components.

## 17. Conclusion
The Smart Campus Operations Hub demonstrates a real-world application design for campus operations. It is modular, scalable, secure, and presentation-ready for the PAF assignment.
