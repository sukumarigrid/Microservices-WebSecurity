# Microservices WebSecurity

Spring Boot API that demonstrates JWT authentication, RBAC, ownership-based authorization, and an internal-only service-to-service path for notifications.

## Tech Stack

- Java 21
- Java 17 or later
- Spring Boot 3.5
- Spring Security
- JJWT
- Maven

## What This Project Covers

- JWT-based authentication for protected endpoints
- Role-based access control for `ROLE_USER`, `ROLE_ADMIN`, and `ROLE_SERVICE_NOTIFICATION`
- Application-specific authorization rules
  - users can edit or delete only their own posts
  - admins can edit or delete all posts
- Internal service access for notification creation
  - user tokens cannot create internal notifications
  - the internal notification endpoint accepts only a service token

## Run Locally

```bash
mvn test
mvn spring-boot:run
```

The application starts on the default Spring Boot port, `8080`.

## Demo Accounts

The app seeds a few in-memory users on startup:

- `admin / admin123`
- `alice / alice123`
- `bob / bob123`

## Authentication

Get a JWT:

```bash
curl -X POST http://localhost:8080/api/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"alice123"}'
```

Use the returned token in the `Authorization` header:

```bash
Authorization: Bearer <token>
```

## API Endpoints

### Public

- `POST /api/auth/token`
- `POST /api/auth/login`

### User and Admin

- `GET /api/posts`
- `GET /api/posts/{postId}`
- `POST /api/posts`
- `PUT /api/posts/{postId}`
- `DELETE /api/posts/{postId}`
- `POST /api/posts/{postId}/likes`
- `GET /api/notifications`

### Admin Only

- `GET /api/admin/stats`

### Internal Service Only

- `POST /api/internal/notifications`

## Authorization Rules

- `ROLE_USER` can read posts, create posts, like posts, and view their own notifications.
- `ROLE_ADMIN` can do everything a user can do, plus access admin stats and modify any post.
- `ROLE_SERVICE_NOTIFICATION` can call the internal notification endpoint only.
- Post updates and deletes are restricted to the post author or an admin.

## Notes

- This project uses in-memory repositories for demo purposes.
- JWT secrets and token TTLs are configured in `src/main/resources/application.yml`.
- The internal notification flow is modeled inside one service so the security rules are visible in a small repo.

## Verification

The test suite covers:

- JWT login
- unauthorized access rejection
- post ownership checks
- admin-only access
- internal service-only notification creation
