# Smart Campus Operations Hub

**IT3030 PAF Assignment 2026 — SLIIT**

A monolithic layered architecture application using **Spring Boot REST API** + **React** client for managing campus facilities, bookings, incident tickets, and notifications.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.2.4, Spring Data MongoDB, Spring Security, OAuth 2.0 |
| Frontend | React 18, React Router v6, Axios |
| Database | MongoDB (Atlas or local MongoDB) |
| CI/CD | GitHub Actions |
| Architecture | Monolithic layered (Controller → Service → Repository) |

---

## Team Module Allocation

| Member | Module | Responsibility |
|--------|--------|---------------|
| Member 1 | Module A | Facilities catalogue + resource management |
| Member 2 | Module B | Booking workflow + conflict checking + QR check-in (innovation) |
| Member 3 | Module C | Incident tickets + attachments + technician updates |
| Member 4 | Module D & E | Notifications + role management + OAuth integration |

---

## Project Structure

```text
smart-campus-hub/
├── backend/                    # Spring Boot application
│   ├── pom.xml
│   └── src/main/java/com/smartcampus/
│       ├── SmartCampusApplication.java
│       ├── config/             # CORS, Security config
│       ├── controller/         # REST controllers
│       ├── dto/                # Request/Response DTOs
│       ├── enums/              # Shared enumerations
│       ├── exception/          # Custom exceptions & handler
│       ├── model/              # Domain models / entities
│       ├── repository/         # Spring Data repositories
│       └── service/            # Business logic
├── frontend/                   # React application
│   ├── public/
│   └── src/
│       ├── components/
│       ├── context/
│       ├── pages/
│       └── services/
├── docs/                       # API documentation
└── .github/workflows/          # CI/CD pipelines
```

---

## Setup Instructions

### Prerequisites
- Java 17+
- Maven 3.8+
- Node.js 18+
- MongoDB Atlas account (or local MongoDB)

### Environment Variables
Create a `.env` file at the project root:

```env
MONGODB_URI=mongodb+srv://<username>:<password>@<cluster-url>/smart_campus?retryWrites=true&w=majority&appName=Cluster0
```

> Do **not** commit `.env` to GitHub.

### Backend Setup
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend runs at: `http://localhost:8080`

The backend reads MongoDB from environment variable:

```properties
spring.data.mongodb.uri=${MONGODB_URI:mongodb://localhost:27017/smart_campus}
```

### Frontend Setup
```bash
cd frontend
npm install
npm start
```

Frontend runs at: `http://localhost:3000`

---

## Running Tests

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Tests
```bash
cd frontend
npm test
```

---

## API Documentation

See [`docs/api-endpoints.md`](docs/api-endpoints.md) for the full API reference.

---

## CI/CD

GitHub Actions workflows run on push/PR to `main` (and any additional configured branches).
See workflow files under `.github/workflows/`.
