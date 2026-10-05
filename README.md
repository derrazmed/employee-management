# Employee Management System

A full-stack employee and user management application built with **Vue 3** and **Spring Boot**.

The application provides employee CRUD operations, user administration, permission-based authorization, secure cookie-based authentication, password reset by email, real-time notifications, employee document management, and a Dockerized development/deployment environment.

## Features

### Employee Management

- Create, view, update, and delete employees
- Employee personal and employment information:
  - First name
  - Last name
  - Email
  - Phone number
  - Job title
  - Department
  - Hire date
  - Salary
- Employee profile photos
- CV/document upload and management
- Employee photo streaming through the backend
- Employee search
- Pagination
- Responsive desktop and mobile employee views
- Employee details modal
- Permission-controlled employee operations

### User Management

- Create users
- View users
- Update users
- Delete users
- Activate/deactivate users
- User type management
- Permission management
- User search
- Pagination
- Responsive mobile user management
- Permission-controlled administrative operations

### Authentication and Session Management

- User registration
- Login
- Logout
- Short-lived HttpOnly access-token cookie
- Long-lived HttpOnly refresh-token cookie
- Automatic access-token refresh
- Automatic handling of expired access tokens
- Inactivity detection
- Session-expiration warning
- Secure password hashing
- Protected Spring Security endpoints
- Frontend route protection
- No JWT storage in `localStorage`, `sessionStorage`, or Pinia

### Password Reset

The application implements a multi-step password-reset flow:

1. The user enters their email address.
2. The backend generates a six-digit verification code.
3. The code is hashed before being stored.
4. The code is sent by email.
5. The user enters the verification code.
6. The code is verified.
7. The user enters a new password.
8. The password is updated.
9. The reset code becomes unusable.

Security protections include:

- 10-minute code expiration
- One-time-use reset codes
- Previous active reset requests are invalidated
- Maximum verification attempts
- Hashed reset codes
- No automatic login after password reset
- Generic reset-request responses so account existence is not disclosed

### Notifications

The application provides real-time administrative notifications using **Spring WebSocket + STOMP**.

Notifications can represent operations such as:

- Employee creation
- Employee update
- Employee deletion
- User-related administrative actions

Notification data includes:

- Actor
- Action
- Entity type
- Entity ID
- Message
- Details
- Read/unread state
- Creation timestamp

The frontend subscribes to user-specific notifications and automatically attempts to reconnect when the WebSocket connection is interrupted.

### File Storage

Employee photos and CVs are stored in **MinIO** rather than PostgreSQL.

The backend handles:

- File upload
- File retrieval
- File deletion
- Object management
- Employee photo streaming
- CV access

Employee photos can be served through the backend API, which avoids requiring the browser to resolve an internal Docker hostname such as `minio`.

### Authorization

The backend uses permission-based authorization.

Current permissions are:

```text
CREATE
READ
UPDATE
DELETE
```

Authorization is enforced on the backend. Frontend permission checks are used to control the user interface, but they are not treated as a security boundary.

---

## Responsive Design

The application supports:

- Desktop
- Tablet
- Mobile

The responsive UI includes:

- Navbar
- Off-canvas mobile sidebar
- Employee management
- User management
- Authentication pages
- Modals
- Search controls
- Pagination
- Forms
- Responsive employee/user lists

Large desktop tables are not simply squeezed into small phone screens. Mobile-specific layouts are used where necessary to avoid horizontal scrolling and preserve usability.

On mobile, the sidebar opens below the navbar so the navbar remains visible.

---

# Technology Stack

## Frontend

- [Vue 3](https://vuejs.org/)
- JavaScript
- [Vite](https://vite.dev/)
- [Vue Router](https://router.vuejs.org/)
- [Pinia](https://pinia.vuejs.org/)
- [Bootstrap 5](https://getbootstrap.com/)
- Bootstrap Icons
- [Axios](https://axios-http.com/)
- STOMP / WebSocket

> TypeScript is not used in this project.

## Backend

- Java 17
- [Spring Boot](https://spring.io/projects/spring-boot)
- Spring Security
- Spring Data JPA
- Hibernate
- Spring Web
- Spring WebSocket
- Spring Mail
- Maven

## Data and Infrastructure

- [PostgreSQL](https://www.postgresql.org/)
- [MinIO](https://min.io/)
- [Docker](https://www.docker.com/)
- Docker Compose
- Nginx

---

# Architecture

The application uses a separated Vue frontend and Spring Boot backend.

```text
                           Browser
                              │
                              │ HTTP / WebSocket
                              ▼
                       ┌───────────────┐
                       │     Nginx     │
                       │               │
                       │ Vue static    │
                       │ /api → Backend│
                       │ /ws  → Backend│
                       └───────┬───────┘
                               │
                               ▼
                       ┌───────────────┐
                       │ Spring Boot   │
                       │   Backend     │
                       └───────┬───────┘
                               │
                  ┌────────────┴────────────┐
                  │                         │
                  ▼                         ▼
           ┌──────────────┐          ┌──────────────┐
           │  PostgreSQL  │          │    MinIO     │
           │              │          │              │
           │ Application  │          │ Photos / CVs │
           │     data     │          │              │
           └──────────────┘          └──────────────┘
```

In Docker, services communicate through their Compose service names.

For example, the backend connects to PostgreSQL using:

```text
jdbc:postgresql://postgres:5432/employee_management
```

and to MinIO using an internal URL such as:

```text
http://minio:9000
```

The browser does **not** use these internal Docker hostnames.

---

# Project Structure

```text
employee-management/
│
├── .env
├── .env.example
├── .gitignore
├── docker-compose.yml
├── README.md
│
├── backend/
│   ├── Dockerfile
│   ├── .dockerignore
│   ├── pom.xml
│   │
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── ...
│       │   └── resources/
│       │       └── application.properties
│       │
│       └── test/
│
└── frontend/
    └── employee-management/
        ├── Dockerfile
        ├── .dockerignore
        ├── nginx.conf
        ├── package.json
        ├── vite.config.js
        │
        └── src/
            ├── assets/
            ├── components/
            ├── router/
            ├── services/
            ├── stores/
            └── views/
```

---

# Backend Structure

The backend follows a layered Spring architecture.

```text
backend/src/main/java/
└── ...
    ├── config/
    ├── controller/
    ├── dto/
    ├── entity/
    ├── exception/
    ├── repository/
    ├── security/
    └── service/
```

### Controllers

Controllers expose REST endpoints for areas such as:

- Authentication
- Employees
- Users
- Notifications
- Password reset

### Services

Business logic is handled by service classes such as:

```text
AuthService
EmployeeService
UserService
NotificationService
PasswordResetService
MinioService
EmailService
```

### Repositories

Spring Data JPA repositories provide database access for entities such as:

```text
User
Employee
Notification
PasswordResetToken
```

---

# Frontend Structure

The frontend uses Vue 3 Composition API and `<script setup>`.

```text
src/
├── components/
│   ├── layout/
│   ├── employees/
│   ├── users/
│   ├── notifications/
│   └── ...
│
├── views/
│   ├── auth/
│   ├── employees/
│   ├── users/
│   └── ...
│
├── services/
│   ├── api.js
│   ├── authService.js
│   ├── employeeService.js
│   └── ...
│
├── stores/
│   ├── authStore.js
│   ├── notificationStore.js
│   └── ...
│
└── router/
    └── index.js
```

---

# Authentication Architecture

JWTs are stored in **HttpOnly cookies** rather than browser storage.

The application uses two token types:

```text
Access Token
Refresh Token
```

The backend distinguishes them using a token claim:

```text
type=ACCESS
type=REFRESH
```

The access token is short-lived, while the refresh token has a longer lifetime.

The frontend Axios instance uses credentials:

```javascript
const api = axios.create({
  baseURL: '/api',
  withCredentials: true,
})
```

The browser therefore sends the authentication cookies automatically with API requests.

## Session behavior

The application supports:

- Automatic access-token refresh while the user is active
- Inactivity detection
- Session-expiration warning
- Explicit "Stay logged in" action
- Explicit logout
- Automatic logout when the session expires

JWTs are intentionally not stored in:

```text
localStorage
sessionStorage
Pinia
```

The frontend may keep non-sensitive user information locally for route/UI state, but authentication tokens remain in HttpOnly cookies.

---

# Password Reset API

The password-reset endpoints are:

```text
POST /api/auth/password-reset/request
POST /api/auth/password-reset/verify
POST /api/auth/password-reset/complete
```

## Request reset

```http
POST /api/auth/password-reset/request
Content-Type: application/json

{
  "email": "user@example.com"
}
```

## Verify code

```http
POST /api/auth/password-reset/verify
Content-Type: application/json

{
  "email": "user@example.com",
  "code": "123456"
}
```

## Complete reset

```http
POST /api/auth/password-reset/complete
Content-Type: application/json

{
  "email": "user@example.com",
  "code": "123456",
  "newPassword": "NewPassword123!"
}
```

---

# Email Configuration

Password-reset emails are sent using Gmail SMTP.

Example Spring configuration:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

Use a **Gmail App Password** rather than the normal Gmail account password.

Never commit the real SMTP credentials to Git.

---

# Environment Variables

Create a `.env` file in the project root:

```env
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-gmail-app-password
JWT_SECRET=your-secure-jwt-secret
```

The actual `.env` file must remain local and must not be committed.

Commit an `.env.example` instead:

```env
MAIL_USERNAME=
MAIL_PASSWORD=
JWT_SECRET=
```

For Docker Compose, the root `.env` is used to substitute variables in `docker-compose.yml`.

---

# Docker

The entire stack can be run using Docker Compose.

The Compose stack contains:

```text
PostgreSQL
MinIO
Spring Boot Backend
Vue + Nginx Frontend
```

## Start the application

From the project root:

```bash
docker compose up -d --build
```

Check the containers:

```bash
docker compose ps
```

Expected services include:

```text
employee-management-postgres
employee-management-minio
employee-management-backend
employee-management-frontend
```

## Access the application

When running locally through the Dockerized frontend:

```text
http://localhost
```

Nginx serves the Vue application and proxies:

```text
/api/
```

to Spring Boot.

WebSocket traffic is proxied through:

```text
/ws
```

This allows the browser to communicate with the application through the same host.

---

# Docker Commands

Start the stack:

```bash
docker compose up -d
```

Build and start:

```bash
docker compose up -d --build
```

Rebuild only the backend:

```bash
docker compose up -d --build backend
```

Rebuild only the frontend:

```bash
docker compose up -d --build frontend
```

View backend logs:

```bash
docker compose logs backend --tail=100
```

Follow backend logs:

```bash
docker compose logs -f backend
```

View frontend logs:

```bash
docker compose logs frontend --tail=100
```

Stop containers:

```bash
docker compose down
```

Restart/rebuild after source changes:

```bash
docker compose up -d --build
```

### Important

Avoid:

```bash
docker compose down -v
```

unless you intentionally want to remove the Compose volumes. Removing the volumes can delete persistent PostgreSQL and MinIO data.

---

# Docker Volumes

Persistent data is stored in named Docker volumes:

```text
postgres_data
minio_data
```

PostgreSQL uses:

```text
postgres_data
```

MinIO uses:

```text
minio_data
```

Stopping/removing containers with:

```bash
docker compose down
```

does not normally remove these volumes.

---

# Frontend Docker Build

The frontend uses a multi-stage Docker build.

The build stage uses Node.js:

```text
Node.js
   ↓
npm ci
   ↓
npm run build
   ↓
dist/
```

The production stage uses Nginx:

```text
dist/
   ↓
Nginx
   ↓
Browser
```

Nginx also proxies API and WebSocket requests to the backend container.

---

# Backend Docker Build

The backend uses a multi-stage Docker build.

Build stage:

```text
Maven + Java 17
       ↓
mvn clean package
       ↓
Spring Boot JAR
```

Runtime stage:

```text
Java 17 JRE
       ↓
Spring Boot application
```

The runtime image does not need Maven or the source tree.

---

# MinIO

MinIO provides object storage for employee files.

The database stores object references such as:

```text
photoObjectName
cvObjectName
```

while the actual files are stored in MinIO.

This keeps binary files outside PostgreSQL.

For employee photos, the backend can retrieve the object from MinIO and stream it through the application API:

```text
Browser
   ↓
GET /api/employees/{id}/photo
   ↓
Spring Boot
   ↓
MinIO
```

This is preferable to exposing an internal Docker hostname directly to the browser.

---

# WebSocket Notifications

Real-time notifications use:

```text
Spring WebSocket
STOMP
```

The backend WebSocket endpoint is:

```text
/ws
```

In the Dockerized application, the browser connects through the same application host, with Nginx forwarding the WebSocket connection to the backend.

Notifications are delivered to the authenticated user's queue:

```text
/user/queue/notifications
```

The frontend attempts to reconnect when the connection is interrupted.

---

# API Overview

## Authentication

```text
POST /api/auth/login
POST /api/auth/register
POST /api/auth/logout
POST /api/auth/refresh

POST /api/auth/password-reset/request
POST /api/auth/password-reset/verify
POST /api/auth/password-reset/complete
```

## Employees

The employee API supports CRUD operations and file operations.

Examples:

```text
GET    /api/employees
GET    /api/employees/{id}
POST   /api/employees
PUT    /api/employees/{id}
DELETE /api/employees/{id}

GET    /api/employees/{id}/photo
```

Additional employee document endpoints are available in the backend implementation.

## Notifications

Notification functionality includes operations for:

```text
Get notifications
Get unread notifications
Get unread count
Mark notification as read
Mark all notifications as read
Delete notification
```

---

# Security

The application uses Spring Security for authentication and authorization.

Security measures include:

- HttpOnly authentication cookies
- Stateless API authentication
- Short-lived access tokens
- Long-lived refresh tokens
- Separate access/refresh token types
- Password hashing
- Backend authorization
- Permission-based access control
- Generic password-reset responses
- Hashed password-reset codes
- Password-reset expiration
- Password-reset attempt limits
- Protected employee operations
- Protected user-management operations
- Environment-based secrets

The following values must not be committed:

```text
JWT_SECRET
MAIL_USERNAME
MAIL_PASSWORD
```

For production, HTTPS should be used so authentication cookies and application traffic are encrypted in transit.

---

# Error Handling

The backend uses centralized exception handling for API errors.

Common HTTP statuses include:

```text
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
500 Internal Server Error
```

The application should expose user-friendly error messages to the frontend rather than raw Java stack traces or internal implementation details.

---

# Development Setup

## Requirements

For local development without Docker:

- Java 17
- Maven
- Node.js
- npm
- PostgreSQL
- MinIO

For Docker-based development:

- Docker Desktop
- Docker Compose

## Backend

Navigate to:

```bash
cd backend
```

Run:

```bash
mvn spring-boot:run
```

The backend is available at:

```text
http://localhost:8080
```

## Frontend

Navigate to:

```bash
cd frontend/employee-management
```

Install dependencies:

```bash
npm install
```

Start the Vite development server:

```bash
npm run dev
```

The development frontend is normally available at:

```text
http://localhost:5173
```

When developing frontend and backend separately, make sure the backend CORS configuration and frontend API base URL are configured for the development environment.

---

# Production Deployment

A production deployment can follow this architecture:

```text
                         Internet
                            │
                            ▼
                       HTTPS / Domain
                            │
                            ▼
                         Nginx
                    ┌───────┴───────┐
                    │               │
                Vue Frontend      /api + /ws
                                    │
                                    ▼
                              Spring Boot
                              ┌─────┴─────┐
                              │           │
                              ▼           ▼
                         PostgreSQL     MinIO
```

For production:

- Enable HTTPS.
- Use a strong, unique JWT secret.
- Keep SMTP credentials outside the repository.
- Use production database credentials rather than development defaults.
- Configure secure cookies appropriately.
- Restrict CORS to trusted origins.
- Use appropriate MinIO credentials and network exposure.
- Do not expose PostgreSQL directly to the public Internet.
- Do not expose MinIO's administrative console publicly unless required.
- Use backups for persistent data.

---

# Git and Secrets

The repository should contain:

```text
.env.example
```

but not:

```text
.env
```

A root `.gitignore` should cover environment files, build artifacts, dependencies, IDE files, and logs.

Example:

```gitignore
# Environment / secrets
.env
.env.*
!.env.example

# Maven
**/target/

# Node
**/node_modules/
**/dist/

# IDE
.idea/
.vscode/
*.iml

# Logs
*.log

# OS
.DS_Store
Thumbs.db
```

If a secret has already been committed, adding it to `.gitignore` does not remove it from Git history. The secret should be rotated and the tracked file removed appropriately.

---

# Future Improvements

The following are potential future enhancements rather than currently implemented features:

- Administrative dashboard
- Employee statistics and charts
- Department filtering
- Employee sorting
- CSV/Excel export
- Bulk employee operations
- Audit/activity log
- User profile management
- Advanced permission-management UI
- Session management for multiple devices
- Improved document preview
- Automated backend tests
- Frontend component tests
- Rate limiting
- CI/CD pipeline
- Production monitoring and logging

---

# Author

**Mohamed DERRAZ EL KABIR**

Network and Computer Engineering Student  
Moroccan School of Engineering Sciences (EMSI)

Full Stack Developer

## Main Technologies

```text
Java
Spring Boot
Spring Security
JPA / Hibernate
PostgreSQL
Vue 3
JavaScript
Pinia
Vue Router
Bootstrap
Docker
Nginx
MinIO
WebSocket / STOMP
```

---

# License

This project is currently intended as a private/professional portfolio and development project.

If the project is released publicly as open source, add an appropriate license here.
