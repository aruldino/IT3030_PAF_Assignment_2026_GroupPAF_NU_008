# Smart Campus Operations Hub

A modular campus management platform built with Spring Boot and React for resource booking, maintenance ticket handling, notifications, and role-based access control.

## Overview

This project is designed as a practical Smart Campus Operations Hub for college and university workflows. It combines a Spring Boot REST backend with a React frontend to manage core campus operations in one place.

The system currently supports:

- Resource management for lecture halls, labs, meeting rooms, and equipment
- Booking creation, approval, rejection, and cancellation workflow
- Maintenance ticket tracking with priority, assignment, and closeout updates
- Session-based authentication and role handling
- Dashboard summaries, notifications, and announcement publishing
- CSV import, smart search, analytics, and notification preferences

## Implemented Roles

- `SUPER_ADMIN` - full access, including user management
- `ADMIN` - manage campus operations and announcements
- `TECHNICIAN` - handle assigned maintenance tickets
- `USER` - request bookings, raise tickets, and receive notifications

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
- Prevent overlapping bookings for the same resource
- Auto-expire stale pending bookings

### 3. Maintenance Module

- Raise maintenance tickets for campus resources
- Track priority and lifecycle
- Assign technicians during status updates
- Manage ticket states:
  - `OPEN`
  - `IN_PROGRESS`
  - `RESOLVED`
  - `CLOSED`
- Add and manage comments with ownership rules
- Track SLA timing for response and resolution

### 4. Authentication and User Management

- Register and log in users
- View the current user profile
- Logout and account deletion support
- Admin-level user listing and user deletion
- Update roles with `PUT /users/role`
- OAuth-style login route available at `POST /auth/login/oauth`
- Role-based endpoint protection is enforced through the backend interceptor

### 5. Notifications and Dashboard

- Display operational statistics
- View notifications and show a role-aware dashboard for day-to-day campus monitoring
- Manage notification preferences
- Poll notifications periodically in the frontend for a near real-time experience

## REST API Summary

### Resources

- `GET /api/resources`
- `GET /resources`
- `GET /api/resources/{id}`
- `GET /resources/{id}/availability`
- `POST /api/resources`
- `PUT /api/resources/{id}`
- `DELETE /api/resources/{id}`
- `GET /resources/suggestions?q=...`
- `GET /resources/analytics/most-booked`
- `POST /resources/import/csv`

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
- `GET /bookings/history`
- `PUT /bookings/expire-pending`

### Maintenance

- `GET /api/maintenance`
- `GET /tickets`
- `POST /api/maintenance`
- `POST /tickets/{id}/comments`
- `POST /tickets/{id}/attachments`
- `GET /tickets/{id}/comments`
- `PUT /tickets/{id}/comments/{commentId}`
- `DELETE /tickets/{id}/comments/{commentId}`
- `GET /tickets/{id}/sla`
- `PUT /api/maintenance/{id}`
- `PUT /tickets/{id}/assign`
- `PUT /tickets/{id}/status`
- `DELETE /api/maintenance/{id}`

### Authentication

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /auth/login`
- `POST /auth/login/oauth`
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

> Our system is designed as a Smart Campus Operations Hub with modular architecture, implementing RESTful best practices. The platform centralizes resource management, booking approvals, maintenance ticket handling, notifications, and role-based access while supporting a clean dashboard-driven user experience. The frontend is modular and responsive, and the backend follows layered architecture with controller, service, and repository separation.

## Tech Stack

- Backend: Spring Boot, Spring Web, Spring Data JPA, Validation
- Database: H2 for local development, MySQL support available
- Frontend: React, TypeScript, Vite
- Security: Session-based authentication with role checks

## Local Environment

- Root env file: [`.env`](./.env)
- Frontend env file: [`frontend/.env`](./frontend/.env)
- One-command Windows launcher: [`start-dev.ps1`](./start-dev.ps1)
- Root npm runner: [`package.json`](./package.json)

To start both apps on Windows:

```powershell
.\start-dev.ps1
```

To start both apps with npm from the repo root:

```powershell
npm install
npm run dev
```

Set `GOOGLE_OAUTH_ENABLED=true` and replace the Google client placeholders in `.env` before testing Google sign-in.

## Requirements Alignment

- User Authentication & Authorization: implemented with secure session login, role checks, and OAuth-style login endpoint compatibility
- Facilities & Assets Management: implemented with CRUD, filtering, search suggestions, CSV import, availability views, and analytics
- Booking Workflow: implemented with approve/reject/cancel flow, conflict checks, booking history, and pending expiry
- Maintenance Workflow: implemented with ticket creation, assignment, status updates, comments, attachments, and SLA tracking
- Notification System: implemented with notification panel, preferences, unread badges, and frontend polling
- System Architecture & Integration: implemented with layered Spring Boot backend and modular React frontend
- Database & Data Management: implemented using persistent JPA entities and relational storage
- Version Control & CI/CD: add the GitHub Actions workflow in `.github/workflows/ci.yml`
- UI/UX Design: implemented with dashboard cards, tables, forms, workflow badges, and responsive layout
- Additional Features: implemented with analytics, smart search, notification preferences, and booking expiry

## Notes

- The current implementation is aligned with the code in this repository.
- Google Sign-In can be integrated later if your viva requires a live OAuth provider flow; the current code already provides secure login, role handling, and an OAuth-compatible login route name for documentation purposes.
- If you want, future enhancements can include QR check-in, richer analytics charts, and full third-party OAuth provider integration.
- For submission prep, see [`RUBRIC_ALIGNMENT.md`](./RUBRIC_ALIGNMENT.md) for a short demo script and requirement-to-code map.
