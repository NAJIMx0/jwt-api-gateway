# JWT API Gateway

A JWT-based Authentication API Gateway built with **Spring Cloud microservices architecture**. Provides user registration and JWT token generation services.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Services](#services)
- [API Endpoints](#api-endpoints)
- [JWT Token Flow](#jwt-token-flow)
- [Running the Services](#running-the-services)
- [Frontend Integration](#frontend-integration)
- [Security Notes](#security-notes)

---

## Architecture Overview

```
                    +-----------------+
                    |    Frontend     |
                    |  (React/Vue)    |
                    +-------+---------+
                            |
                            | HTTP
                            v
                +-----------------------+
                |   Auth Service        |
                |   (Port 9001)         |
                +----------+------------+
                           |
                     RestTemplate
                           |
                           v
                +-----------------------+
                |    User Service       |
                |    (External)         |
                +-----------------------+

     +-------------+         +-------------+
     |  Discovery  |         |   Config    |
     |   Service   |         |   Service   |
     |   (Eureka)  |         |             |
     | Port 8761   |         | Port 9297   |
     +-------------+         +-------------+
```

| Component | Role |
|---|---|
| Discovery Service | Service Registry (Netflix Eureka) |
| Config Service | Centralized Configuration (Spring Cloud Config) |
| Auth Service | JWT Token Generation & User Registration |
| Frontend | User Interface |

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 11 |
| Framework | Spring Boot 2.3.8.RELEASE |
| Build Tool | Maven |
| Cloud | Spring Cloud Hoxton.SR12 |
| JWT Library | JJWT 0.11.5 |
| Password Hashing | jBCrypt 0.4 |
| Service Discovery | Netflix Eureka |
| Config Server | Spring Cloud Config |

---

## Project Structure

```
jwt-api-gateway/
├── pom.xml                              # Parent Maven POM
├── authentication-service/              # JWT Auth microservice (port 9001)
│   ├── pom.xml
│   └── src/main/java/com/najim/
│       ├── AuthApplication.java         # Main Spring Boot App
│       ├── controller/
│       │   └── AuthController.java      # REST endpoint - /auth/register
│       ├── entities/
│       │   ├── AuthRequest.java         # DTO: email, password, name
│       │   ├── AuthResponse.java        # DTO: accessToken, refreshToken
│       │   └── UserVO.java              # User value object
│       └── service/
│           ├── AuthService.java         # Business logic
│           └── JwtUtil.java             # JWT generation/validation
├── config-service/                      # Spring Cloud Config Server (port 9297)
│   ├── pom.xml
│   └── src/main/java/com/najim/
│       └── ConfigService.java
└── discovery-service/                   # Netflix Eureka (port 8761)
    ├── pom.xml
    └── src/main/java/com/najim/
        └── DiscoveryService.java
```

---

## Services

### Discovery Service — Port `8761`

Netflix Eureka Server. Acts as a service registry where all microservices register themselves, enabling services to discover each other dynamically.

### Config Service — Port `9297`

Spring Cloud Config Server providing centralized configuration management. Pulls configuration from a remote Git repository:

```
https://github.com/NAJIMx0/config-server-demo-.git
```

### Authentication Service — Port `9001`

Core JWT authentication service. Exposes the `/auth/register` endpoint for user registration, generates access and refresh tokens using JJWT, and hashes passwords with BCrypt.

---

## API Endpoints

### `POST /auth/register`

Registers a new user and returns JWT tokens.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "name": "John Doe"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbG...",
  "refreshToken": "eyJhbG..."
}
```

---

## JWT Token Flow

1. Client sends `POST /auth/register` with `email`, `password`, and `name`
2. `AuthService` hashes the password using **BCrypt**
3. Calls the external `user-service` via `RestTemplate` to persist the user
4. `JwtUtil` generates:
   - **Access Token** — expires in `86400` seconds (24 hours)
   - **Refresh Token** — expires in `432000` seconds (5 days)
5. Returns both tokens in an `AuthResponse`

---

## Running the Services

> ⚠️ Start services in this exact order.

**1. Discovery Service (Eureka)**
```bash
cd discovery-service
mvn spring-boot:run
# Runs on http://localhost:8761
```

**2. Config Service**
```bash
cd config-service
mvn spring-boot:run
# Runs on http://localhost:9297
```

**3. Authentication Service**
```bash
cd authentication-service
mvn spring-boot:run
# Runs on http://localhost:9001
```

**4. Frontend (optional)**
```bash
cd frontend-service
npm start
# Runs on http://localhost:3000
```

---

## Frontend Integration

### 1. Create the Frontend

```bash
# React
npx create-react-app frontend-service

# Vue
vue create frontend-service

# Angular
ng new frontend-service
```

### 2. API Client (React + Axios)

```js
import axios from 'axios';

const API_URL = 'http://localhost:9001/auth';

export const register = async (email, password, name) => {
  const response = await axios.post(`${API_URL}/register`, { email, password, name });
  return response.data;
};
```

### 3. Token Storage & Interceptor

```js
// Store tokens after login
localStorage.setItem('accessToken', data.accessToken);
localStorage.setItem('refreshToken', data.refreshToken);

// Auto-attach token to every request
const api = axios.create({ baseURL: 'http://localhost:9001' });

api.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
```

### 4. Register Component Example

```jsx
import React, { useState } from 'react';
import { register } from './api';

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const tokens = await register(email, password, name);
      localStorage.setItem('accessToken', tokens.accessToken);
      localStorage.setItem('refreshToken', tokens.refreshToken);
      console.log('Registration successful!');
    } catch (error) {
      console.error('Registration failed:', error);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input type="text" placeholder="Name" value={name} onChange={e => setName(e.target.value)} />
      <input type="email" placeholder="Email" value={email} onChange={e => setEmail(e.target.value)} />
      <input type="password" placeholder="Password" value={password} onChange={e => setPassword(e.target.value)} />
      <button type="submit">Register</button>
    </form>
  );
}

export default Login;
```

---

## Security Notes

> ⚠️ Review these before deploying to production.

- **JWT Secret** — The default secret `"JWWNDUIDXAWI"` in `application.yml` must be replaced with a strong, randomly generated value in production.
- **Password Hashing** — Passwords are hashed with BCrypt before being stored.
- **Token Expiry** — Access tokens expire in 24 hours; refresh tokens in 5 days.
- **User Service** — Currently called externally via `RestTemplate`. Ensure it is secured appropriately.
- **CORS** — Configure CORS on the Auth Service to allow requests from your frontend origin.
