# Smart Campus Operations Hub — API Endpoints

Base URL: `http://localhost:8080/api`

---

## Authentication (Module E — Member 4)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/auth/me` | Get the currently authenticated user's profile | Required |
| POST | `/auth/logout` | Log out the current user | Required |

---

## Resources — Module A (Member 1)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/resources` | List all campus resources | Public |
| GET | `/resources/{id}` | Get a resource by ID | Public |
| POST | `/resources` | Create a new resource | Admin |
| PUT | `/resources/{id}` | Update an existing resource | Admin |
| DELETE | `/resources/{id}` | Delete a resource | Admin |

**Query Parameters (GET /resources):**
- `type` — Filter by `ResourceType` (e.g. `LECTURE_HALL`, `LAB`)
- `status` — Filter by `ResourceStatus` (`ACTIVE`, `OUT_OF_SERVICE`)
- `location` — Case-insensitive location search

---

## Bookings — Module B (Member 2)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/bookings` | Submit a new booking request | User |
| GET | `/bookings/my` | Get current user's bookings | User |
| GET | `/bookings` | Get all bookings | Admin |
| GET | `/bookings/{id}` | Get a booking by ID | User/Admin |
| PUT | `/bookings/{id}/approve` | Approve a booking | Admin |
| PUT | `/bookings/{id}/reject` | Reject a booking | Admin |
| PATCH | `/bookings/{id}/cancel` | Cancel a booking | User/Admin |
| DELETE | `/bookings/{id}` | Delete a booking record | Admin |
| GET | `/bookings/{id}/qr` | Get the QR code for a booking | User |
| POST | `/bookings/check-in` | QR code check-in (`?token=...`) | User |

**Booking Status Flow:**
```
PENDING ──► APPROVED  (admin approves → QR code generated)
PENDING ──► REJECTED  (admin rejects with reason)
APPROVED ──► CANCELLED (user or admin cancels)
```

**Conflict Detection:** The system prevents double-bookings by checking whether the requested time slot overlaps with any PENDING or APPROVED booking for the same resource on the same date.

---

## Tickets — Module C (Member 3)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/tickets` | Submit a new incident ticket | User |
| GET | `/tickets` | Get all tickets | Admin/Technician |
| GET | `/tickets/my` | Get current user's tickets | User |
| GET | `/tickets/{id}` | Get a ticket by ID | User/Admin |
| PATCH | `/tickets/{id}/status` | Update ticket status | Admin/Technician |
| PUT | `/tickets/{id}/assign` | Assign ticket to a technician | Admin |
| POST | `/tickets/{id}/comments` | Add a comment to a ticket | User/Technician |
| GET | `/tickets/{id}/comments` | Get all comments for a ticket | User/Admin |
| DELETE | `/tickets/{id}` | Delete a ticket | Admin |

**Ticket Status Flow:**
```
OPEN ──► IN_PROGRESS  (technician assigned)
IN_PROGRESS ──► RESOLVED  (technician adds resolution notes)
RESOLVED ──► CLOSED  (admin closes)
OPEN / IN_PROGRESS ──► REJECTED  (admin rejects with reason)
```

**Priority Levels:** `LOW` | `MEDIUM` | `HIGH` | `CRITICAL`

---

## Notifications — Module D (Member 4)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/notifications` | Get all notifications for current user | User |
| PATCH | `/notifications/{id}/read` | Mark a notification as read | User |
| PATCH | `/notifications/read-all` | Mark all notifications as read | User |
| DELETE | `/notifications/{id}` | Delete a notification | User |

**Notification Types:**
- `BOOKING_APPROVED` — Booking was approved by admin
- `BOOKING_REJECTED` — Booking was rejected by admin
- `BOOKING_CANCELLED` — Booking was cancelled
- `TICKET_ASSIGNED` — Ticket assigned to technician
- `TICKET_STATUS_CHANGED` — Ticket status updated
- `TICKET_COMMENT_ADDED` — New comment on a ticket

---

## Error Response Format

All error responses follow this structure:

```json
{
  "timestamp": "2026-03-04T12:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Resource not found with id: 42"
}
```

**HTTP Status Codes:**
| Code | Meaning |
|------|---------|
| 200 | OK |
| 201 | Created |
| 204 | No Content |
| 400 | Bad Request / Validation Error |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 409 | Booking Conflict |
| 500 | Internal Server Error |
