# URL Shortener

A full-stack URL shortener built with Angular and Spring Boot. The application supports JWT authentication, per-user URL history, short link generation, and public redirects from short links to their original long URLs.

## Tech Stack

- Frontend: Angular 21
- Backend: Spring Boot 3, Spring Security, JWT
- Database: PostgreSQL
- Cache: Redis
- Reverse proxy: Nginx
- Build tools: Gradle, npm
- Runtime: Docker Compose

## Features

- User registration and login
- JWT-protected API requests
- Create short links from long URLs
- View the authenticated user's saved links
- Delete one link or clear all user links
- Open short links publicly without authentication
- Redirect short links like `http://localhost/a1b2c3d4` to the original URL

## Project Structure

```text
.
├── frontend/              # Angular application
├── nginx/                 # Nginx Dockerfile and reverse proxy config
├── src/main/java/         # Spring Boot backend
├── src/main/resources/    # Spring configuration
├── docker-compose.yml     # Full application stack
├── Dockerfile             # Backend Docker build
└── build.gradle           # Backend dependencies and tasks
```

## Quick Start With Docker

From the project root:

```bash
docker compose up --build
```

The application is served by Nginx:

```text
http://localhost
```

Docker Compose starts:

- `nginx` on host port `80`
- `backend` on container port `8080`
- `postgres` on container port `5432`
- `redis` on container port `6379`

The backend is not exposed directly to the host in `docker-compose.yml`. Nginx proxies API requests to `http://backend:8080`.

## API Overview

### Authentication

Register a user:

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "user",
  "password": "password"
}
```

Login:

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "user",
  "password": "password"
}
```

Both endpoints return a JWT token:

```json
{
  "token": "jwt-token",
  "id": 1,
  "username": "user"
}
```

### URLs

URL endpoints require the JWT token:

```http
Authorization: Bearer <jwt-token>
```

Create a short URL:

```http
POST /api/url/add
Content-Type: application/json
Authorization: Bearer <jwt-token>

{
  "url": "https://example.com/very/long/link"
}
```

Response:

```json
{
  "id": 1,
  "shortUrl": "http://localhost/a1b2c3d4",
  "longUrl": "https://example.com/very/long/link"
}
```

Get the current user's URLs:

```http
GET /api/url/my
Authorization: Bearer <jwt-token>
```

Delete one URL:

```http
DELETE /api/url/{id}
Authorization: Bearer <jwt-token>
```

Delete all current user's URLs:

```http
DELETE /api/url
Authorization: Bearer <jwt-token>
```

### Redirects

Short links are public and do not require authentication:

```http
GET /{shortUrl}
```

Example:

```text
http://localhost/a1b2c3d4
```

If the short code exists, the backend returns `302 Found` and redirects to the original long URL.

## Configuration

The backend configuration is defined in `src/main/resources/application.yml`.

Important environment variables:

| Variable | Default | Description |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/urldb` | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | PostgreSQL username |
| `SPRING_DATASOURCE_PASSWORD` | `postgresql` | PostgreSQL password |
| `SPRING_REDIS_HOST` | `localhost` | Redis host |
| `SPRING_REDIS_PORT` | `6379` | Redis port |
| `JWT_SECRET` | configured in `application.yml` | JWT signing secret |
| `JWT_EXPIRATION` | `3600000` | JWT expiration time in milliseconds |

In Docker Compose, these values are configured for the `backend`, `postgres`, and `redis` services.

## Local Development

### Backend

The backend requires PostgreSQL and Redis. You can run them yourself or use Docker services.

Start the Spring Boot app:

```bash
gradle bootRun
```

The backend listens on:

```text
http://localhost:8080
```

### Frontend

Install dependencies:

```bash
cd frontend
npm install
```

Run the Angular development server:

```bash
npm start
```

Angular runs on:

```text
http://localhost:4200
```

When running the frontend separately from Docker, make sure API requests are routed to the Spring Boot backend. In the Docker setup this is handled by Nginx.

## Build

Build the backend:

```bash
gradle build
```

Build the frontend:

```bash
cd frontend
npm run build
```

## Notes

- Short codes are generated as 8-character hexadecimal strings.
- Nginx proxies `/api/` requests to the backend.
- Nginx also proxies root-level short links matching `/{8 hex characters}` to the backend redirect endpoint.
- All protected URL management endpoints require a valid JWT.
