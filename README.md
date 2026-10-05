# Employee Management System

A full-stack employee and user management application built with Vue.js and Spring Boot.

The application provides employee CRUD operations, user management, role-based permissions, authentication with secure HttpOnly cookies, password reset through email, real-time notifications, employee document management, and Dockerized deployment.

---

## Features

### Employee Management

- Create employees
- View employee details
- Update employee information
- Delete employees
- Employee profile photos
- Employee CV/document management
- Employee information including:
  - First name
  - Last name
  - Email
  - Phone number
  - Job title
  - Department
  - Hire date
  - Salary
- Employee search
- Pagination
- Responsive desktop and mobile interfaces
- Employee photo preview
- CV preview/download

### User Management

- Create users
- View users
- Update users
- Delete users
- Activate/deactivate users
- User type management
- Permission management
- Search users
- Pagination
- Responsive mobile user management

### Authentication

- User registration
- Login
- Logout
- HttpOnly access-token cookie
- HttpOnly refresh-token cookie
- Automatic access-token refresh
- Automatic handling of expired access tokens
- Session inactivity detection
- Session expiration warning
- Secure password hashing
- Protected API endpoints
- Route protection on the frontend

### Password Reset

The application implements a multi-step password reset flow:

1. User enters their email address.
2. Backend generates a six-digit verification code.
3. Code is hashed before being stored.
4. Code is sent by email.
5. User enters the verification code.
6. Code is validated.
7. User enters a new password.
8. Password is updated.
9. Reset code becomes invalid.

Security protections include:

- 10-minute code expiration
- One-time-use reset codes
- Previous reset requests are invalidated
- Maximum verification attempts
- Hashed reset codes
- No automatic login after password reset
- Generic reset-request responses to avoid revealing whether an account exists

### Notifications

The application provides real-time notifications for administrative operations.

Supported actions include:

- Employee creation
- Employee update
- Employee deletion
- User-related administrative actions

Notifications contain information such as:

- Actor
- Action
- Entity type
- Entity ID
- Message
- Details
- Read/unread state
- Creation timestamp

Notifications are delivered in real time using WebSockets and STOMP.

### File Storage

Employee photos and CVs are stored using MinIO.

The backend handles:

- File upload
- File retrieval
- File deletion
- Object management
- Employee photo streaming
- CV access

The browser does not need direct access to MinIO for employee photos. The backend retrieves the object and serves it through the application API.

### Authorization

The backend implements permission-based authorization.

Permissions include:

```text
CREATE
READ
UPDATE
DELETE
```
