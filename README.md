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

## Running & Testing Milestone M01

### 1. Frontend Build Verification
```bash
cd frontend
npm install
npm run build
```

### 2. Backend Compilation & Test Verification
```bash
cd backend
export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-25.jdk/Contents/Home"
./mvnw clean compile
./mvnw test
```

---

## Milestone Roadmap

- **M01 — Repository and Project Foundation** *(Current)*
- **M02 — Spring Boot + PostgreSQL + Flyway Baseline** *(Next)*
- **M03 — Authentication + Security Foundation**
- **M04 — User Profile**
- **M05 — Health Profile & Normalized Allergens**
- **M06 — BMI / BMR / TDEE Engine**
- **M07 — Nutrition Targets & Versioning**
- **M08 — Food Database with Provenance**
- **M09 — Recommendation Engine (Fail-Closed)**
- **M10 — Budget Optimization**
- **M11 — Meal Planner**
- **M12 — Dashboard (Phase 1 Scope)**

