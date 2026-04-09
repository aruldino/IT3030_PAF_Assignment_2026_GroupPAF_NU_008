# Smart Campus Operations Hub

A role-aware campus management platform for handling resources, bookings, maintenance tickets, notifications, and user access from one centralized dashboard.

## Overview

This project is designed as a practical Smart Campus Operations Hub for college and university workflows. It combines a Spring Boot REST backend with a React frontend to manage core campus operations in one place.

The system currently supports:

- Resource management for lecture halls, labs, meeting rooms, and equipment
- Booking creation, approval, rejection, and cancellation workflow
- Maintenance ticket tracking with priority, assignment, and closeout updates
- Session-based authentication and role handling
- Dashboard summaries, notifications, and announcement publishing

## Implemented Roles

- `SUPER_ADMIN` - full access, including user management
- `ADMIN` - manage campus operations and announcements
- `STAFF` - create and manage operational requests

## Core Modules

### 1. Resource Catalogue

- Create, update, delete, and search resources
- Track resource type, capacity, location, availability window, and status
- Support `ACTIVE`, `OUT_OF_SERVICE`, and `MAINTENANCE` states

### 2. Booking Workflow

- Submit booking requests
- Review bookings by status
- Approve, reject, or cancel requests
- Track booking lifecycle using:
  - `PENDING`
  - `APPROVED`
  - `REJECTED`
  - `CANCELLED`

### 3. Maintenance Module

- Raise maintenance tickets for campus resources
- Track priority and lifecycle
- Assign technicians during status updates
- Manage ticket states:
  - `OPEN`
  - `IN_PROGRESS`
- `RESOLVED`
  - `CLOSED`

### 4. Authentication and User Management

- Register and log in users
- View the current user profile
- Logout and account deletion support
- Admin-level user listing and user deletion
- Update roles with `PUT /users/role`

### 5. Announcements and Dashboard

- Publish official announcements
- Display operational statistics
- View notifications and show a role-aware dashboard for day-to-day campus monitoring

## REST API Summary

### Resources

- `GET /api/resources`
- `GET /resources`
- `GET /api/resources/{id}`
- `GET /resources/{id}/availability`
- `POST /api/resources`
- `PUT /api/resources/{id}`
- `DELETE /api/resources/{id}`

### Bookings

- `GET /api/bookings`
- `GET /bookings`
- `GET /bookings/my`
- `GET /bookings/conflicts`
- `POST /api/bookings`
- `PUT /bookings/{id}/approve`
- `PUT /bookings/{id}/reject`
- `PUT /bookings/{id}/cancel`
- `PATCH /api/bookings/{id}/status`

### Maintenance

- `GET /api/maintenance`
- `GET /tickets`
- `POST /api/maintenance`
- `POST /tickets/{id}/comments`
- `POST /tickets/{id}/attachments`
- `PUT /api/maintenance/{id}`
- `PUT /tickets/{id}/assign`
- `PUT /tickets/{id}/status`
- `DELETE /api/maintenance/{id}`

### Authentication

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /auth/login`
- `GET /api/auth/me`
- `GET /users/me`
- `POST /api/auth/logout`
- `DELETE /api/auth/me`

### Users

- `GET /api/users`
- `GET /users`
- `PUT /users/role`
- `DELETE /api/users/{id}`

### Notifications

- `GET /notifications`
- `GET /notifications/preferences`
- `PUT /notifications/preferences`
- `POST /notifications/{id}/read`

## Assignment Mapping

If you are presenting this as a 4-member project, this version fits the implemented codebase well:

- Member 1: Facilities Catalogue and Resource Management
- Member 2: Booking Workflow and Conflict Handling
- Member 3: Incident Tickets and Technician Workflow
- Member 4: Notifications, Role Management, and Authentication

## Report-Ready Project Description

> Our system is designed as a Smart Campus Operations Hub with modular architecture, implementing RESTful best practices. The platform centralizes resource management, booking approvals, maintenance ticket handling, notifications, and role-based access while supporting a clean dashboard-driven user experience.

## Tech Stack

- Backend: Spring Boot, Spring Web, Spring Data JPA, Validation
- Database: H2 for local development, MySQL support available
- Frontend: React, TypeScript, Vite
- Security: Session-based authentication with role checks

## Notes

- The current implementation is aligned with the code in this repository.
- If you want, future enhancements can include QR check-in, analytics charts, CSV import, AI-based suggestions, and OAuth-based login integration.
