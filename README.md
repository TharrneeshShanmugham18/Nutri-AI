# Nutri AI

> **AI-Powered, Budget-Aware Personalized Nutrition & Wellness Platform**

Nutri AI is a full-stack personalized nutrition and wellness platform engineered as a clean **modular monolith**. The platform combines deterministic health calculations, structured food data, nutritional constraints, affordability heuristics, and regional availability, with external Large Language Models (LLMs) serving strictly as an untrusted advisory layer.

---

## High-Level Architecture

The system is built as a single-repository modular monolith:

- **Frontend (`frontend/`)**: Single-Page Application (SPA) built with React 19, TypeScript, Vite, Tailwind CSS, React Router 7, and TanStack Query v5.
- **Backend (`backend/`)**: Modular monolith REST API (`/api/v1/*`) built with Java 25 and Spring Boot 4.1.1. The backend is the sole source of truth for all calculations, budget validations, allergen exclusions, and access control.
- **Persistence Tier**: PostgreSQL 16 managed via strictly sequential Flyway migrations (`V1` through `V9` in Phase 1).
- **Local Infrastructure**: Docker Desktop for containerized local PostgreSQL (`postgres:16.8-alpine`, native ARM64).

---

## Technology Baseline

| Component | Technology | Version |
|---|---|---|
| **Operating System** | macOS (Apple Silicon ARM64) | 15.5 |
| **Java Runtime** | Oracle JDK LTS | 25.0.4.1 |
| **Backend Framework** | Spring Boot | 4.1.1 |
| **Build Tool** | Maven Wrapper (`./mvnw`) | 3.9.9 |
| **Node.js Runtime** | Node.js LTS | 24.21.0 |
| **Package Manager** | npm | 11.19.0 |
| **Frontend Framework** | React | 19.0.0 |
| **Frontend Build Tool** | Vite | 6.1.x |
| **Language** | TypeScript | 5.7+ |
| **Styling** | Tailwind CSS | 3.4.17 |
| **Client Routing** | React Router | 7.1.x |
| **Database** | PostgreSQL | 16.8 (via Docker Desktop) |

---

## Repository Structure

```
nutri-ai/
├── backend/            # Spring Boot 4.1.1 modular monolith application
│   ├── src/main/java/  # Application code organized by domain module
│   ├── src/main/resources/ # Configuration & Flyway migrations
│   ├── pom.xml         # Maven build configuration (Java 25 target)
│   └── mvnw            # Maven Wrapper executable
├── frontend/           # React 19 + TypeScript + Vite frontend
│   ├── src/            # Components, routes, features, API client
│   ├── package.json    # Frontend dependency definitions
│   └── vite.config.ts  # Vite bundler configuration
├── .env.example        # Environment variable template
├── .gitignore          # Repository exclusion definitions
├── PROJECT_SPEC.md     # Authoritative project specification
└── README.md           # Project overview and developer instructions
```

---

## Local Development Prerequisites

Ensure the following runtimes are installed on your machine:

1. **Oracle JDK 25 LTS**: Verified at `/Library/Java/JavaVirtualMachines/jdk-25.jdk/Contents/Home`.
2. **Node.js 24 LTS & npm**: Node 24.21.0+ and npm 11.19.0+.
3. **Docker Desktop**: Docker Engine running on Apple Silicon.
4. **Git**: Version 2.39+.

---

## Running & Testing the Project

### 1. Local Database Setup (Docker Desktop)
Ensure Docker Desktop is running. Start the PostgreSQL 16.8 container with a persistent volume:
```bash
# Start PostgreSQL container in detached mode
docker compose up -d

# Verify container status and health
docker compose ps

# Check PostgreSQL readiness inside container
docker compose exec postgres pg_isready -U nutri_user -d nutriai

# Stop PostgreSQL container (preserves volume nutriai_pgdata)
docker compose down
```

### 2. Environment Configuration
Configuration is managed via environment variables with safe development defaults:
```bash
# Optional: create local .env file from template
cp .env.example .env
```
Default connection properties:
- `DATABASE_HOST`: `localhost`
- `DATABASE_PORT`: `5432`
- `DATABASE_NAME`: `nutriai`
- `DATABASE_USERNAME`: `nutri_user`
- `DATABASE_PASSWORD`: `change_this_in_local_env`

### 3. Backend Compilation, Migrations & Tests
Flyway runs automatically on application startup, applying migrations from `backend/src/main/resources/db/migration/`:
```bash
cd backend
export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-25.jdk/Contents/Home"

# Compile and run test suite (includes database connectivity and Flyway migration tests)
./mvnw clean test

# Run the Spring Boot application locally
./mvnw spring-boot:run
```

### 4. Health & Connectivity Verification
With the backend running, verify application and database connectivity via Spring Boot Actuator:
```bash
curl http://localhost:8080/actuator/health
```
Expected response:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" },
    "livenessState": { "status": "UP" },
    "ping": { "status": "UP" },
    "readinessState": { "status": "UP" },
    "ssl": { "status": "UP" }
  },
  "groups": ["liveness", "readiness"]
}
```

### 5. Frontend Build Verification
```bash
cd frontend
npm install
npm run lint
npm run build
npm run dev
```

---

## Authentication & Security Architecture (Milestone M03)

Nutri AI implements a defense-in-depth, stateless authentication and session management architecture:

### 1. Token Architecture & Storage
- **Access Tokens**: Short-lived (15 minutes), signed JSON Web Tokens (HS256) holding subject (`userId`), `email`, and `roles`. Kept strictly in frontend memory (`AuthContext`); never persisted in `localStorage` or `sessionStorage` to mitigate XSS exfiltration.
- **Refresh Tokens**: Cryptographically secure random 256-bit tokens transported strictly via `HttpOnly`, `SameSite=Lax` cookies scoped to path `/api/v1/auth` (`Secure` enabled in production). Tokens are stored in PostgreSQL as SHA-256 hashes (never plaintext).
- **Token Rotation & Replay Detection**: Every refresh rotates the refresh token. If a previously replaced or revoked token is replayed, all active refresh tokens for that user are immediately revoked, and a `TOKEN_REUSE_DETECTED` audit event is logged.

### 2. Password Security & Account Protection
- **Password Hashing**: BCrypt with 12 rounds of work factor. Passwords are never logged or stored plaintext.
- **Password Policy**: Enforced minimum 8 characters, requiring at least one uppercase letter, one lowercase letter, one digit, and one special character (`@$!%*?&#`).
- **Abuse Protection & Brute Force Lockout**: In-memory rate limiting locks login attempts for an `(IP, Email)` pair after 5 consecutive failures within a 15-minute window.
- **Enumeration Defense**: Authentication failures return identical generic `401 Unauthorized` messages ("Invalid email or password") for non-existent users and wrong passwords.
- **Security Audit Logging**: Key authentication events (`ACCOUNT_CREATED`, `LOGIN_SUCCESS`, `LOGIN_FAILED`, `ACCOUNT_LOCKED`, `TOKEN_REFRESHED`, `TOKEN_REUSE_DETECTED`, `LOGOUT`) are persisted to the `audit_logs` table.

### 3. API Endpoints

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Public | Register new user account with `ROLE_USER` |
| `POST` | `/api/v1/auth/login` | Public | Authenticate user, return access token and set HttpOnly refresh cookie |
| `POST` | `/api/v1/auth/refresh` | Public (Cookie) | Rotate refresh token cookie and issue new access token |
| `POST` | `/api/v1/auth/logout` | Public (Cookie) | Revoke refresh token and clear cookie |
| `GET` | `/api/v1/auth/me` | Authenticated | Retrieve authenticated user's profile and roles |

---

## Milestone Roadmap

- **M01 — Repository and Project Foundation** *(Completed)*
- **M02 — Spring Boot + PostgreSQL + Flyway Baseline** *(Completed)*
- **M03 — Authentication + Security Foundation** *(Completed)*
- **M04 — User Profile** *(Next)*
- **M05 — Health Profile & Normalized Allergens**
- **M06 — BMI / BMR / TDEE Engine**
- **M07 — Nutrition Targets & Versioning**
- **M08 — Food Database with Provenance**
- **M09 — Recommendation Engine (Fail-Closed)**
- **M10 — Budget Optimization**
- **M11 — Meal Planner**
- **M12 — Dashboard (Phase 1 Scope)**


