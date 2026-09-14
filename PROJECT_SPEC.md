# PROJECT SPECIFICATION

# Nutri AI

## AI-Powered, Budget-Aware Personalized Nutrition & Wellness Platform

---

# 0. DOCUMENT PURPOSE

This document is the authoritative product, architecture, security, engineering,
and development specification for the Nutri AI project.

The coding agent MUST treat this document as the primary source of truth for
the project.

This document defines:

* What the product is
* Why it exists
* Functional requirements
* Non-functional requirements
* Technology choices
* Architecture
* Security requirements
* Data model
* API standards
* AI integration boundaries
* Testing requirements
* Development milestones
* Quality standards
* Coding-agent behavior

IMPORTANT:

This document defines the desired system and engineering rules.

It does NOT authorize the coding agent to implement the entire application
in one operation.

Implementation must happen incrementally through explicit milestones
approved by the project owner.

---

# 1. PROJECT VISION

Nutri AI is a secure, full-stack, AI-assisted personalized nutrition and
wellness platform designed to help users create practical, affordable,
and personalized nutrition plans.

The platform is designed particularly around the problem that many users
want to improve their nutrition and fitness but face practical limitations
such as:

* Limited budget
* Limited time
* Limited cooking facilities
* Lack of nutrition knowledge
* Limited access to professional guidance
* Regional food preferences
* Regional food availability
* Different lifestyles
* Different fitness goals

The platform should not simply generate generic diet plans.

Its core objective is:

> Generate personalized nutrition recommendations by combining deterministic
> health calculations, structured food data, nutritional constraints,
> affordability, food accessibility, regional preferences, lifestyle
> constraints, and AI-assisted personalization.

AI is an intelligence layer, not the sole decision-making system.

---

# 2. PROJECT OBJECTIVES

The system should:

1. Collect relevant user health and lifestyle information.
2. Calculate estimated BMI, BMR, TDEE, and nutritional targets.
3. Maintain a structured food and nutrition database.
4. Recommend foods based on nutritional requirements.
5. Consider dietary restrictions and allergies.
6. Consider user food preferences and dislikes.
7. Consider food budget.
8. Consider regional/local food availability.
9. Generate affordable daily and weekly meal plans.
10. Generate grocery lists.
11. Provide food substitutions.
12. Provide an AI nutrition assistant.
13. Track user progress.
14. Provide optional professional consultation capabilities in a later phase.
15. Maintain strong security and privacy protections.
16. Be suitable for a final-year academic project.
17. Demonstrate industry-relevant software engineering practices.
18. Be suitable for software engineering placement interviews.
19. Remain extensible for future product development.

---

# 3. CORE PRODUCT DIFFERENTIATOR

The primary differentiator is:

## Budget-Aware and Constraint-Aware Nutrition Recommendation

The platform should attempt to find practical nutrition plans that satisfy
multiple constraints simultaneously.

Example:

User:

* Goal = Muscle gain
* Vegetarian = Yes
* Budget = ₹3000/month
* Region = Tamil Nadu
* Limited cooking time

The system should try to generate a plan satisfying:

* Estimated calorie requirement
* Protein requirement
* Dietary preference
* Allergies
* Food dislikes
* Budget
* Food accessibility
* Meal frequency
* Cooking constraints

The system must not simply ask an LLM:

"Give this user a diet."

The recommendation engine should work from structured application data.

---

# 4. NON-NEGOTIABLE ENGINEERING PRINCIPLES

The following rules are mandatory.

## 4.1 Do not build an LLM wrapper

The project must contain genuine application logic.

The application must own:

* Authentication
* Authorization
* Business logic
* Nutrition calculations
* Food database
* Recommendation logic
* Budget constraints
* Validation
* APIs
* Database
* Security controls
* Progress tracking
* Auditability

## 4.2 Deterministic backend is the source of truth

The backend must be authoritative for:

* BMI
* BMR
* TDEE
* Nutrition targets
* Food nutrition data
* Budget calculations
* Dietary restrictions
* Allergy filtering
* Recommendation constraints
* Authorization
* Security decisions

The LLM must never become the authoritative source for these.

## 4.3 AI is an augmentation layer

The AI may assist with:

* Conversational interaction
* Explanation
* Personalization
* Summarization
* Meal-plan explanations
* Substitution explanations
* Natural-language interaction

## 4.4 Keep the architecture simple

This project is being developed primarily by a single student developer.

Use the simplest architecture that satisfies the requirements.

Do NOT introduce unnecessary:

* Microservices
* Kubernetes
* Message brokers
* Event-driven infrastructure
* Multiple databases
* Complex distributed systems

unless a clear requirement justifies the added complexity.

Use a modular monolith initially.

---

# 5. TECHNOLOGY BASELINE

Use the following technology stack unless a strong technical reason exists.

## 5.1 Backend

* Oracle JDK 25 LTS
* Spring Boot 4.1.1 (Current supported stable release)
* Maven Wrapper (./mvnw)
* Spring Web
* Spring Data JPA
* Hibernate
* Spring Security
* Jakarta Validation
* PostgreSQL
* Flyway
* Spring Boot Actuator

## 5.2 Frontend

* React
* TypeScript
* Vite
* Tailwind CSS
* React Router 7.1.x
* TanStack Query
* React Hook Form
* Zod
* Recharts or Chart.js

## 5.3 Authentication

* Spring Security
* JWT
* Short-lived access token
* Refresh-token mechanism
* Secure password hashing using Argon2id or BCrypt

The exact token-storage architecture must be selected deliberately based on
security implications.

Do not blindly place sensitive tokens in insecure browser storage.

## 5.4 Database

PostgreSQL.

All production schema changes must be managed with Flyway migrations.

## 5.5 AI

Use an external LLM provider such as:

* Google Gemini
* OpenAI

The provider MUST be hidden behind an internal AI provider abstraction.

The application must not depend directly on provider-specific code throughout
the business layer.

The AI API key MUST NEVER be exposed to the frontend.

## 5.6 Local Development & Optional Infrastructure

* Docker Desktop (Local development infrastructure for PostgreSQL container)
* Docker Compose (Local container orchestration for database and full-stack demo)

Use only when justified:

* Redis
* GitHub Actions
* OpenAPI / Swagger
* Postman

## 5.7 Version Governance Rule

Technology versions must not be treated as permanently frozen merely because they appeared in an earlier architecture plan.

Before beginning a major implementation milestone, verify that major runtime/framework/dependency versions remain supported, stable, secure, and mutually compatible.

Prefer LTS releases where applicable.

Do not automatically select the newest release.

Do not silently upgrade or downgrade a dependency when the change may affect APIs, security, compatibility, behavior, or architecture.

Any material version change must be identified, justified, and its impact documented before implementation.

---

# 6. HIGH-LEVEL ARCHITECTURE

Use a modular monolith.

## Frontend

React + TypeScript

```
    |
    | HTTPS / REST / JSON
    v
```

## Backend

Spring Boot

```
    |
    +-- Controller Layer
    |
    +-- Service Layer
    |
    +-- Domain / Business Logic
    |
    +-- Repository Layer
    |
    v
```

PostgreSQL

Additional integrations:

Spring Boot
|
+-- AI Provider
|
+-- Redis (optional)
|
+-- Location services
|
+-- Professional consultation services in later phases

---

# 7. BACKEND MODULE STRUCTURE

Organize code by business domain.

Suggested modules:

* auth
* user
* health
* nutrition
* food
* recommendation
* budget
* mealplan
* ai
* progress
* appointment
* admin
* audit
* common

Each module should have clear responsibilities.

Avoid giant packages containing unrelated business logic.

---

# 8. USER ROLES

Implement role-based access control.

Roles:

## USER

Can:

* Register
* Login
* Logout
* Manage own profile
* Enter health information
* Manage dietary preferences
* View nutrition targets
* Generate meal plans
* View recommendations
* View grocery lists
* Interact with AI assistant
* Track progress
* Book professional consultations

## DIETITIAN / DOCTOR

Can:

* Create/manage professional profile
* Manage availability
* View authorized user/patient data
* Review consultation requests
* Manage appointments
* Provide professional recommendations
* Maintain consultation notes

Professionals MUST NOT have unrestricted access to all users.

## ADMIN

Can:

* Manage food records
* Manage nutrition data
* Manage food prices
* Manage regional mappings
* Manage categories
* Manage appropriate system configuration
* Manage users where necessary
* Review reports
* Access security/audit information according to authorization

Use least privilege.

---

# 9. AUTHENTICATION

Implement:

* Registration
* Login
* Logout
* Access tokens
* Refresh tokens
* Token expiry
* Refresh token invalidation/rotation where practical
* Password reset
* Account verification where practical
* Login throttling/rate limiting
* Suspicious login protection

Never store plaintext passwords.

Never log:

* Passwords
* Access tokens
* Refresh tokens
* API keys

---

# 10. USER PROFILE

Collect only information required for functionality.

Possible fields:

* Name
* Age
* Sex
* Height
* Weight
* Activity level
* Goal
* Dietary preference
* Allergies
* Food dislikes
* Cuisine preference
* Budget
* Location/region
* Meal frequency
* Cooking time
* Relevant lifestyle factors

Users must be able to:

* View
* Edit
* Delete

their own profile information.

---

# 11. HEALTH INFORMATION

Health-related information is sensitive.

Treat relevant fields such as:

* Weight
* BMI
* Allergies
* Dietary restrictions
* Nutrition history
* Progress data
* Consultation information

as sensitive data.

Apply strict authorization.

Do not expose such data unnecessarily.

Do not log such data unless there is a justified requirement.

Do not send unnecessary sensitive information to external AI providers.

---

# 12. NUTRITION ENGINE

Implement deterministic backend services for:

## BMI

Input:

* Height
* Weight

Output:

* BMI
* Appropriate estimated category

## BMR

Implement a documented formula.

## TDEE

Estimate energy expenditure based on activity level.

## Nutrition Targets

Estimate:

* Calories
* Protein
* Carbohydrates
* Fat
* Fiber
* Water guidance

The calculations must be:

* deterministic
* unit tested
* documented
* reproducible

Never use the LLM to perform authoritative nutrition calculations.

Clearly state that these are estimates and not medical diagnoses.

---

# 13. PERSONALIZED RECOMMENDATION ENGINE

The recommendation engine must be independent of the LLM.

Inputs should include:

* Nutrition targets
* Goal
* Calories
* Protein
* Dietary preferences
* Allergies
* Dislikes
* Budget
* Food accessibility
* Region
* Meal count
* Cooking constraints

The engine should:

1. Filter incompatible foods.
2. Remove allergens.
3. Apply dietary restrictions.
4. Apply availability constraints.
5. Consider budget.
6. Consider nutritional requirements.
7. Rank candidate foods.
8. Build meal combinations.
9. Validate resulting meal plan.
10. Return structured recommendations.

LLM output must not bypass these rules.

---

# 14. BUDGET OPTIMIZATION

Budget awareness is one of the main project differentiators.

Support:

* Daily budget
* Weekly budget
* Monthly budget

The recommendation system should attempt to:

Minimize estimated cost

while satisfying:

* calorie target approximately
* protein target approximately
* dietary restrictions
* allergies
* availability
* user preferences
* budget

A heuristic or constraint-based optimization approach is acceptable for the
initial version.

The algorithm must be documented.

The system must explain when an exact nutritional target cannot be achieved
within the specified budget.

Do not falsely claim that a plan is optimal unless the algorithm actually
supports that claim.

---

# 15. FOOD DATABASE

Create structured food data.

Suggested fields:

* Food ID
* Name
* Category
* Serving size
* Calories
* Protein
* Carbohydrates
* Fat
* Fiber
* Estimated price
* Price unit
* Vegetarian/non-vegetarian
* Allergens
* Regional availability
* Seasonality
* Tags

Food data must be stored separately from AI-generated text.

Seed/demo data must be clearly identifiable as seed/demo data.

Do not silently fabricate authoritative nutrition data.

---

# 16. LOCATION-AWARE RECOMMENDATIONS

Allow users to select:

* Country
* State/region
* City where appropriate

Precise GPS must NOT be required for the core functionality.

Location may influence:

* Commonly available foods
* Regional cuisine
* Food accessibility
* Seasonal choices
* Estimated pricing

Regional defaults must never be forced.

The user must always be able to override them.

Do not stereotype users based solely on location.

---

# 17. FOOD PRICE MODEL

Food prices are estimates.

Store:

* Food
* Unit
* Estimated price
* Region
* Source/date where practical

Always label estimated prices appropriately.

Do not imply real-time pricing unless the application has an actual live data source.

---

# 18. MEAL PLAN GENERATOR

Generate:

* Daily meal plans
* Weekly meal plans

Meals:

* Breakfast
* Lunch
* Dinner
* Snacks

Each meal should contain:

* Food
* Serving size
* Calories
* Protein
* Carbohydrates
* Fat
* Estimated cost

Daily totals should be displayed.

Compare daily totals against estimated targets.

---

# 19. FOOD SUBSTITUTION ENGINE

Implement rule-based substitution.

Example:

Chicken unavailable

Possible alternatives:

* Eggs
* Soy
* Paneer
* Dal

depending on user's preferences and restrictions.

Substitutions must consider:

* Nutritional similarity
* Dietary compatibility
* Allergies
* Budget
* Availability

AI may explain the substitution, but the backend must validate it.

---

# 20. AI NUTRITION ASSISTANT

Implement a conversational assistant.

Example requests:

* "What can I eat for breakfast?"
* "I only have ₹100 today."
* "I don't have paneer. What else can I use?"
* "Explain today's meal plan."
* "I missed breakfast."

Architecture:

Frontend

↓

Spring Boot AI Controller

↓

AI Service

↓

Context Filter

↓

Prompt Builder

↓

AI Provider Adapter

↓

External LLM

The frontend MUST NOT communicate directly with the LLM provider.

---

# 21. AI CONTEXT MINIMIZATION

Only provide the LLM with the minimum context required for the request.

Do not unnecessarily send:

* full user profile
* unnecessary health information
* authentication information
* internal IDs
* database internals
* secrets
* audit records

User data must be minimized before transmission to the external AI service.

---

# 22. AI OUTPUT HANDLING

Treat model output as untrusted input.

Implement:

* Input validation
* Prompt templates
* Context filtering
* Output validation
* Response size limits
* Conversation history limits
* Rate limiting
* Timeout handling
* Provider failure handling
* Safe fallback

Do not render arbitrary AI-generated HTML.

Do not allow AI output to directly execute commands.

---

# 23. AI SECURITY

Protect against:

* Prompt injection
* Jailbreak attempts
* System prompt extraction
* Sensitive data leakage
* Excessive context exposure
* Prompt abuse
* Token abuse
* Malicious instructions inside model output

The model must never directly:

* execute SQL
* execute shell commands
* change authorization
* modify roles
* access arbitrary user records
* execute arbitrary application commands

Any future AI tool/function capability must operate through an explicitly
controlled allowlisted tool layer.

---

# 24. MEDICAL SAFETY BOUNDARIES

Nutri AI is a nutrition and wellness assistance platform.

It is NOT:

* a doctor
* a diagnostic system
* a replacement for medical professionals

The system must not:

* diagnose diseases
* prescribe medications
* recommend stopping medication
* recommend changing prescription dosage
* claim guaranteed medical outcomes
* provide unsafe treatment instructions

For high-risk questions, direct the user toward professional medical advice.

Clearly communicate appropriate disclaimers.

---

# 25. PROGRESS TRACKING

Allow users to track relevant wellness information such as:

* Weight
* BMI
* Activity
* Calories where available
* Protein intake
* Water intake
* Other appropriate wellness metrics

Dashboard can display:

* Weight trend
* BMI trend
* Nutrition adherence
* Budget usage

Do not encourage unhealthy rapid weight loss or unsafe behavior.

---

# 26. GROCERY LIST

Generate grocery lists from meal plans.

Include:

* Item
* Quantity
* Unit
* Estimated cost
* Category

Categories may include:

* Grains
* Protein
* Dairy
* Fruits
* Vegetables
* Other

Calculate estimated weekly/monthly cost.

---

# 27. PROFESSIONAL CONSULTATION — LATER PHASE

Consultation is NOT required for Phase 1.

Later support:

* Professional profile
* Availability
* Appointment booking
* Appointment confirmation
* Appointment status
* Consultation notes
* Role-based access
* User/professional communication

Private professional notes must never be visible to unauthorized users.

---

# 28. SECURITY AS A FIRST-CLASS REQUIREMENT

Security is not a final-stage feature.

Security must be considered during:

* Architecture
* Database design
* API design
* Authentication
* Authorization
* Frontend
* AI integration
* Logging
* File handling
* Deployment

For every feature evaluate:

1. What data does it handle?
2. Who is allowed to access it?
3. How is the user authenticated?
4. How is authorization enforced?
5. What malicious input could be supplied?
6. What information could leak?
7. What abuse could occur?
8. How is the feature tested?

---

# 29. AUTHORIZATION SECURITY

Never rely on frontend authorization.

Backend authorization is mandatory.

Prevent:

* IDOR
* Horizontal privilege escalation
* Vertical privilege escalation
* Privilege escalation
* Unauthorized data access

A user can only access their own protected resources unless a defined role
and authorization rule permits otherwise.

Object-level authorization is mandatory where applicable.

---

# 30. INPUT VALIDATION

Validate every external input.

Validation is required for:

* Request bodies
* Query parameters
* Path variables
* File uploads
* Profile fields
* AI messages
* Appointment data

Use Jakarta Validation in the backend.

Examples:

* reasonable age bounds
* reasonable height bounds
* reasonable weight bounds
* non-negative budget
* valid email
* length limits
* enum validation
* safe pagination parameters

Frontend validation is useful for UX but is NOT a security control.

---

# 31. DATABASE SECURITY

Use:

* JPA
* Parameterized queries
* Safe query construction

Never concatenate user input into SQL.

Prevent:

* SQL injection
* unsafe native queries
* mass assignment
* unauthorized data access

Use least-privileged database credentials.

Never expose the production database publicly unless specifically required.

---

# 32. DATABASE MIGRATIONS

Use Flyway for all schema evolution.

Rules:

* Every schema change must have a migration.
* Never edit an already-applied migration.
* Never delete an already-applied migration.
* Create a new migration for changes.
* Keep schema changes reproducible.

---

# 33. API DESIGN

Use:

* REST
* JSON
* `/api/v1/...`
* DTOs

Example endpoints:

POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/logout

GET /api/v1/users/me
PUT /api/v1/users/me

GET /api/v1/health/profile
PUT /api/v1/health/profile

GET /api/v1/nutrition/targets

GET /api/v1/foods

POST /api/v1/recommendations

POST /api/v1/meal-plans
GET /api/v1/meal-plans/{id}

POST /api/v1/ai/chat

GET /api/v1/progress
POST /api/v1/progress

---

# 34. API CONTRACT-FIRST DEVELOPMENT

For each feature:

1. Define endpoint.
2. Define HTTP method.
3. Define request DTO.
4. Define response DTO.
5. Define validation.
6. Define authentication requirement.
7. Define authorization requirement.
8. Define error responses.
9. Document with OpenAPI.
10. Implement backend.
11. Test backend.
12. Integrate frontend.

Frontend code must never guess backend responses.

OpenAPI is the contract between frontend and backend.

---

# 35. DTO AND SERVICE RULES

Do NOT expose JPA entities directly from controllers.

Use:

Request DTO
→ Validation
→ Controller
→ Service
→ Domain logic
→ Repository
→ Response DTO

Keep:

* Controllers thin
* Business logic in services/domain components
* Persistence in repositories
* Data transformation in mappers

Do not place business logic inside controllers.

---

# 36. CORS

Configure CORS explicitly.

Do not use:

Allow-Origin: *

for authenticated production APIs.

Allow only approved frontend origins.

Credentialed requests must only be allowed for trusted origins.

---

# 37. CSRF

Do not blindly disable CSRF.

Authentication architecture must determine the appropriate strategy.

If browser cookies are used for sensitive authentication state,
configure appropriate CSRF protection.

Document the chosen architecture and reasoning.

---

# 38. XSS

Prevent:

* Stored XSS
* Reflected XSS

Do not render arbitrary user-provided HTML.

Treat AI-generated content as untrusted.

Use safe rendering and escaping.

---

# 39. SECURITY HEADERS

Configure appropriate HTTP security headers.

Consider:

* Content-Security-Policy
* X-Content-Type-Options
* Referrer-Policy
* Strict-Transport-Security in HTTPS production
* Clickjacking/frame protection
* Permissions-Policy where appropriate

Do not introduce unsafe wildcard security policies merely to simplify development.

---

# 40. RATE LIMITING

Rate-limit sensitive/high-cost operations including:

* Login
* Registration
* Password reset
* AI requests
* Appointment operations
* Other abuse-prone endpoints

Redis may be used when distributed rate limiting becomes necessary.

Do not expose implementation details in rate-limit error responses.

---

# 41. ERROR HANDLING

Use a centralized error-handling mechanism.

Example:

{
"timestamp": "2026-01-01T10:00:00Z",
"status": 400,
"error": "VALIDATION_ERROR",
"message": "Invalid request",
"path": "/api/v1/..."
}

Never expose:

* Java stack traces
* database errors
* internal file paths
* secrets
* infrastructure details

to users.

---

# 42. SECRETS MANAGEMENT

Never hardcode:

* API keys
* JWT secrets
* database passwords
* encryption keys
* OAuth secrets

Use environment variables or appropriate secret management.

Provide:

`.env.example`

containing placeholders only.

Never commit real secrets.

Ensure `.gitignore` excludes local secret files.

---

# 43. LOGGING SECURITY

Implement structured logging.

Never log:

* passwords
* access tokens
* refresh tokens
* API keys
* sensitive health information
* unnecessary private AI conversations

Use appropriate log levels.

---

# 44. AUDIT LOGGING

Record security-sensitive events such as:

* Successful login
* Failed login
* Password changes
* Account lockout
* Role changes
* Administrative actions
* Sensitive access where justified
* Appointment changes

Normal users must not be able to modify audit records.

---

# 45. FILE UPLOAD SECURITY

If OCR/file-upload functionality is implemented later:

* Restrict MIME types
* Restrict file size
* Validate actual file type where practical
* Do not trust filename extension
* Generate server-side filenames
* Avoid executable upload locations
* Reject suspicious files
* Scan when feasible

Never allow arbitrary executable uploads.

---

# 46. DATA PRIVACY

Follow privacy-by-design principles.

Collect only information required for functionality.

Support where practical:

* Account deletion
* Data deletion
* Data export
* Privacy explanation
* AI usage explanation
* Stored-data explanation

Never expose one user's private information to another user.

---

# 47. FRONTEND SECURITY

Implement:

* Secure route guards
* Safe API handling
* Input validation
* Safe rendering
* Secure configuration
* Logout
* Proper session/token handling
* No secrets in frontend bundles
* No API keys in source code

Remember:

> Frontend restrictions are not authorization.

Backend authorization remains mandatory.

---

# 48. DATABASE MODEL

Suggested relational entities:

* users
* roles
* user_roles
* health_profiles
* user_preferences
* foods
* food_nutrition
* food_prices
* food_locations
* nutrition_targets
* meal_plans
* meal_plan_items
* progress_records
* ai_conversations
* professionals
* appointments
* audit_logs
* refresh_tokens

Use:

* Primary keys
* Foreign keys
* Unique constraints
* Check constraints where appropriate
* Indexes
* Proper relationship mapping
* Appropriate cascading rules

Do not use JSON for all data simply because it is convenient.

Use relational modelling where structured relationships exist.

---

# 49. DATA INTEGRITY

Do not silently fabricate authoritative data.

This includes:

* Nutrition values
* Prices
* Food availability
* Professional credentials
* Medical facts

Demo data must be identified appropriately.

Estimated prices must be labelled as estimates.

AI-generated content must never silently overwrite authoritative application
data.

---

# 50. PERFORMANCE

Use reasonable:

* Database indexes
* Pagination
* Lazy loading where appropriate
* Query optimization
* Caching where justified

Do not prematurely optimize.

Do not introduce Redis simply because it appears in the technology stack.

Add infrastructure when there is a real requirement.

---

# 51. OBSERVABILITY

Implement basic:

* Structured logging
* Health checks
* Error monitoring approach
* Metrics where practical

Spring Boot Actuator may be used.

Secure Actuator endpoints.

Do not expose:

* Environment variables
* Secrets
* Internal configuration
* Sensitive application information

through public management endpoints.

---

# 52. TESTING REQUIREMENTS

Testing is mandatory.

## Unit Tests

At minimum test:

* BMI
* BMR
* TDEE
* Macro calculations
* Nutrition targets
* Food filtering
* Budget calculations
* Food substitutions
* Recommendation engine

## Integration Tests

Test:

* Authentication
* Database operations
* Authorization
* REST endpoints
* Major business workflows

## Security Tests

Test:

* Unauthenticated access
* Unauthorized resource access
* IDOR attempts
* Role escalation
* Invalid tokens
* Expired tokens
* Input validation bypass
* SQL injection payloads
* XSS payloads
* CORS restrictions
* Rate limiting

Do not claim that security is complete merely because Spring Security
has been configured.

---

# 53. SECURITY THREAT MODEL

At minimum analyze:

* Authentication attacks
* Brute force
* Credential stuffing
* Broken authorization
* IDOR
* Privilege escalation
* SQL injection
* XSS
* CSRF
* CORS misconfiguration
* Token theft
* Session abuse
* Prompt injection
* AI data leakage
* Sensitive data exposure
* File upload vulnerabilities
* Rate-limit abuse
* API abuse
* Dependency vulnerabilities
* Secrets leakage
* Information disclosure

For each threat document:

Threat
→ Attack scenario
→ Impact
→ Mitigation
→ Implementation
→ Test
→ Verification status

---

# 54. DEPENDENCY SECURITY

Use supported stable dependencies.

Before adding a dependency:

1. Determine why it is required.
2. Check maintenance status.
3. Check for known vulnerabilities.
4. Avoid unnecessary libraries.

Use dependency/security scanning where practical.

---

# 55. UI/UX REQUIREMENTS

The application should have a professional modern interface.

Pages:

1. Landing page
2. Register
3. Login
4. Onboarding
5. Dashboard
6. Health profile
7. Nutrition targets
8. Meal planner
9. Food explorer
10. Budget planner
11. AI assistant
12. Progress dashboard
13. Grocery list
14. Settings
15. Admin dashboard
16. Consultation in later phases

Requirements:

* Responsive
* Mobile-friendly
* Accessible
* Consistent navigation
* Reusable components
* Clear information hierarchy
* Good loading states
* Good error states
* No unnecessary visual complexity

---

# 56. FAILURE HANDLING

Handle gracefully:

* Invalid input
* Authentication failures
* Authorization failures
* Database failure
* Network failure
* AI provider failure
* Timeout
* Rate limit
* Missing food data
* Recommendation failure

Core application functionality must remain usable when AI is unavailable.

---

# 57. AI FAILURE ISOLATION

The application must continue functioning without the AI provider.

The following must NOT depend completely on AI:

* Authentication
* User profile
* BMI
* BMR
* TDEE
* Nutrition calculations
* Food database
* Recommendation engine
* Budget calculations
* Meal planning
* Progress tracking

AI is an enhancement rather than a single point of failure.

---

# 58. DEVELOPMENT PHASES

## PHASE 1 — CORE MVP

Implement:

* Project setup
* React
* TypeScript
* Spring Boot
* PostgreSQL
* Flyway
* Secure authentication
* User profile
* Health profile
* BMI
* BMR
* TDEE
* Nutrition targets
* Food database
* Recommendation engine
* Budget-aware recommendation
* Meal planner
* Dashboard

The Phase 1 system must be stable before advanced features are introduced.

## PHASE 2 — INTELLIGENT PLATFORM

Add:

* AI nutrition assistant
* AI explanations
* Food substitutions
* Grocery planner
* Location-aware recommendations
* Seasonal recommendations
* Progress tracking
* Admin module

## PHASE 3 — ADVANCED FEATURES

Potential future features:

* OCR food-label scanning
* Barcode scanning
* Improved price estimation
* Professional consultation
* Appointment management
* Notifications
* Advanced analytics

Do not begin Phase 3 merely because the architecture exists.

Only add it after earlier phases are stable.

---

# 59. DEVELOPMENT MILESTONES

Use incremental milestones.

M01 — Repository and project foundation

M02 — Spring Boot + PostgreSQL + Flyway

M03 — Authentication + security foundation

M04 — User profile

M05 — Health profile

M06 — BMI / BMR / TDEE engine

M07 — Nutrition targets

M08 — Food database

M09 — Recommendation engine

M10 — Budget optimization

M11 — Meal planner

M12 — Dashboard

M13 — AI assistant

M14 — Grocery planner

M15 — Location-aware recommendation

M16 — Progress tracking

M17 — Admin features

M18 — Security testing

M19 — Deployment

M20 — Final documentation

The agent MUST NOT start milestone M(n+1) until the project owner explicitly
authorizes it or the development workflow clearly authorizes the next milestone.

---

# 60. DEVELOPMENT MILESTONE ACCEPTANCE

Every milestone must define:

* Objective
* Scope
* Files/modules affected
* Database changes
* API changes
* Frontend changes
* Security implications
* Tests
* Acceptance criteria

The milestone is complete only when its acceptance criteria are satisfied.

---

# 61. DEFINITION OF DONE

A feature is COMPLETE only when applicable requirements are satisfied:

* Functional implementation complete
* Backend complete
* Frontend integration complete
* Validation implemented
* Authentication verified
* Authorization verified
* Error handling implemented
* Relevant tests implemented
* Security cases tested
* Build succeeds
* Tests pass
* No known compilation errors
* No obvious runtime errors
* API documentation updated
* No secrets exposed
* Existing features still work
* Git changes reviewed

"Code generated" does not mean "feature complete".

---

# 62. CODE QUALITY

Use:

* Clean Code
* SOLID principles where appropriate
* Clear naming
* Cohesive classes/services
* Reusable components
* Clear interfaces
* Minimal useful comments
* No duplicated business logic

Avoid:

* Giant classes
* Giant controllers
* Hardcoded credentials
* Hardcoded business rules where configuration is appropriate
* Duplicate validation
* Copy-pasted functionality
* Unnecessary abstraction
* Unnecessary technology

---

# 63. GIT PRACTICES

Use Git throughout development.

Recommended:

* Small logical commits
* Meaningful commit messages
* No secrets
* No generated build artifacts
* No dependency directories
* No local environment files containing secrets

The coding agent should review changes before considering a milestone complete.

---

# 64. DOCUMENTATION REQUIREMENTS

Maintain documentation sufficient for:

* Academic review
* Viva
* Resume
* Software engineering interview
* Future development

Documentation should eventually include:

* README
* Architecture
* Database ER diagram
* API documentation
* Security architecture
* Threat model
* Testing strategy
* Deployment instructions
* Environment configuration
* Future enhancements

---

# 65. ACADEMIC PROJECT REQUIREMENTS

The project must contain genuine technical implementation.

It must not be presented merely as:

"React frontend + LLM API."

The implementation should demonstrate:

* Software architecture
* Database design
* Backend engineering
* REST API design
* Security
* Recommendation logic
* Constraint handling
* Testing
* AI integration
* Deployment

The system should support academic discussion of:

* Problem statement
* Existing limitations
* Proposed architecture
* Methodology
* Algorithms
* System design
* Security model
* Evaluation
* Results
* Limitations
* Future work

---

# 66. RESUME / INTERVIEW OBJECTIVES

The project should allow the developer to confidently discuss:

* Java
* OOP
* Spring Boot
* REST APIs
* PostgreSQL
* JPA/Hibernate
* Spring Security
* JWT
* Authentication
* Authorization
* Recommendation systems
* Constraint-based optimization
* AI integration
* Caching
* Testing
* Docker
* CI/CD
* Secure coding
* System design

The implementation must remain understandable to the student developer.

The agent must not create abstractions that the project owner cannot
reasonably understand and explain.

---

# 67. CODING-AGENT OPERATING RULES

The coding agent acts as:

* Senior Software Architect
* Senior Backend Engineer
* Senior Frontend Engineer
* Security Engineer
* Test Engineer

Before modifying code:

1. Read PROJECT_SPEC.md.
2. Inspect the current repository.
3. Understand existing architecture.
4. Identify the current milestone.
5. Identify dependencies.
6. Check whether existing code can be reused.
7. Assess security implications.

Do not blindly overwrite working code.

---

# 68. NO UNAUTHORIZED ARCHITECTURAL CHANGES

The agent must not silently change:

* Technology stack
* Database technology
* Authentication strategy
* Authorization architecture
* Project structure
* AI architecture
* Major data model

without documenting the reason and obtaining approval when the change is
architecturally significant.

---

# 69. NO FAKE IMPLEMENTATION STATUS

Never claim:

"Implemented successfully"

unless the code was actually created and tested.

Never claim:

"Security is complete"

without performing the required security verification.

Always distinguish between:

* Implemented
* Tested
* Partially implemented
* Placeholder
* Known issue
* Future work

---

# 70. ROOT-CAUSE DEBUGGING

When an error occurs:

1. Identify the root cause.
2. Explain the cause briefly.
3. Fix the underlying problem.
4. Run the relevant tests again.
5. Check for regression.
6. Only then continue.

Do not create temporary hacks merely to make an error disappear.

---

# 71. CHANGE DISCIPLINE

For each milestone:

* Avoid unrelated modifications.
* Do not refactor the entire project unnecessarily.
* Do not add dependencies without justification.
* Do not change working behavior without reason.
* Keep changes focused.

---

# 72. TEST BEFORE CLAIMING SUCCESS

Before reporting milestone completion:

1. Compile/build.
2. Run unit tests.
3. Run integration tests where applicable.
4. Verify the changed workflow manually when appropriate.
5. Check API responses.
6. Check database behavior.
7. Perform relevant security checks.
8. Review changed files.

If something could not be tested, say so explicitly.

---

# 73. SECURITY DEVELOPMENT LIFECYCLE

For every milestone perform a lightweight security review:

* Authentication
* Authorization
* Input validation
* Data exposure
* Injection
* Rate limiting
* Logging
* Secrets
* Privacy
* AI security where applicable

Security must be continuously evaluated.

---

# 74. DATA PRIVACY BY DEFAULT

Default behavior should minimize data exposure.

When deciding between two designs:

Prefer the design that:

* collects less data
* exposes less data
* stores less data
* sends less data externally
* retains less sensitive information

unless the additional data is necessary for a clearly defined feature.

---

# 75. AI PROVIDER ABSTRACTION

Use an interface/abstraction conceptually similar to:

AIProvider

with provider-specific implementations such as:

GeminiAIProvider
OpenAIProvider

The application business logic must not be tightly coupled to one provider.

---

# 76. CONFIGURATION MANAGEMENT

Environment-specific configuration should include:

* database URL
* database username
* database password
* JWT secret
* AI API key
* frontend origin
* other external service configuration

Use safe environment configuration.

Provide `.env.example`.

Never commit actual values.

---

# 77. FUTURE SCALABILITY

Design the system so that components can later be extracted or scaled
independently if the product grows.

However:

Do NOT prematurely convert the modular monolith into microservices.

Future extraction candidates may include:

* AI service
* Recommendation service
* Notification service

Only do this when justified by actual requirements.

---

# 78. SECURITY PRIORITY ORDER

Where trade-offs exist, prioritize:

1. Security
2. Correctness
3. Maintainability
4. Testability
5. Reliability
6. Performance
7. User experience
8. Development convenience

Do not sacrifice critical security for convenience.

---

# 79. FINAL QUALITY STANDARD

The objective is NOT:

"Make a demo that looks impressive."

The objective is:

> Build a secure, maintainable, testable, reliable full-stack nutrition
> platform with deterministic recommendation logic, budget awareness,
> privacy-conscious design, and AI-assisted personalization.

The final application should be something the developer can:

* Demonstrate
* Test
* Explain
* Defend in a viva
* Explain in a software interview
* Publish as a portfolio project
* Continue developing after graduation

---

# 80. SPECIFICATION AUTHORITY

PROJECT_SPEC.md is the project's master specification.

The coding agent must follow it unless the project owner explicitly changes
the specification.

When requirements conflict:

1. Security requirements take priority.
2. Data correctness takes priority.
3. Approved architecture takes priority.
4. Simplicity is preferred over unnecessary complexity.
5. The agent must document significant assumptions.

Never silently invent major requirements.

---

# 81. AGENT EXECUTION POLICY

PROJECT_SPEC.md describes the complete target system.

It does NOT instruct the agent to build the entire application at once.

Implementation must occur milestone by milestone.

The project owner controls milestone progression.

The agent must:

1. Read PROJECT_SPEC.md before implementation.
2. Inspect the current repository.
3. Identify the requested milestone.
4. Implement only the requested milestone.
5. Follow the architecture and security rules.
6. Build and test the implementation.
7. Perform the relevant security checks.
8. Review changes for regressions.
9. Report actual implementation status.
10. Clearly identify incomplete or untested functionality.

Do not start future milestones without authorization.

---

# END OF PROJECT SPECIFICATION
