<<<<<<< HEAD
# Tandem - Multi-tenant Helpdesk

Tandem is a full-stack support ticket application for multiple organizations. It uses a React interface, a Java 17 / Spring Boot API, and MongoDB. Every ticket operation is scoped to the authenticated user's organization, and customers are further restricted to their own tickets.

## Features

- JWT login with Super Admin, Organization Admin, Support Agent, and Customer roles
- Super Admin organization directory and self-service tenant provisioning
- Initial Organization Admin account created with every new tenant
- Tenant-level data isolation at every ticket and agent lookup
- Ticket creation, editing, assignment, status changes, and closure
- Search plus status, priority, and assigned-agent filters
- Server-side pagination
- Public replies and staff-only internal notes
- Status and assignment audit history
- Request validation, consistent API errors, and appropriate status codes
- OpenAPI / Swagger UI documentation
- Seeded demo organizations and role-based accounts
- Unit tests for authorization and tenant-isolation rules
- No email integration or outbound messages

## Quick start with Docker

Requirements: Docker Desktop with Docker Compose.

```bash
docker compose up --build
```

If one of the default ports is already occupied, override it without editing the project, for example: `API_PORT=18080 WEB_PORT=13000 docker compose up --build`.

Open:

- Application: http://localhost:3000
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

To stop the services:

```bash
docker compose down
```

MongoDB data is kept in the `mongo_data` Docker volume. To intentionally reset the demo database, run `docker compose down -v`.

## Demo accounts

All demo accounts use the password `Password123!`.

| Organization | Role | Email |
|---|---|---|
| Tandem Platform | Super Admin | `superadmin@tandem.test` |
| Acme Industries | Organization Admin | `admin@acme.test` |
| Acme Industries | Support Agent | `agent@acme.test` |
| Acme Industries | Customer | `customer@acme.test` |
| Globex Corporation | Organization Admin | `admin@globex.test` |
| Globex Corporation | Support Agent | `agent@globex.test` |
| Globex Corporation | Customer | `customer@globex.test` |

Use accounts from both organizations to verify that tickets and agent lists never cross tenant boundaries.

To create a tenant, sign in as the Super Admin, open **Organizations**, and select **Create organization**. Enter the organization details, an immutable tenant key, and credentials for the initial Organization Admin. The account is available immediately; the application does not send email.

## Local development

Requirements: Java 17+, Maven 3.9+, Node.js 20.19+ or 22.12+, and MongoDB.

1. Start MongoDB on `mongodb://localhost:27017/helpdesk`.
2. Start the API:

```bash
mvn spring-boot:run
```

3. In another terminal, start the frontend:

```bash
cd frontend
npm install
npm run dev
```

4. Open http://localhost:5173. Vite proxies `/api` to the API on port 8080.

The API can be configured with these environment variables:

| Variable | Default | Purpose |
|---|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/helpdesk` | MongoDB connection string |
| `JWT_SECRET` | development-only value | HMAC signing secret; replace in production |
| `JWT_EXPIRATION_MINUTES` | `480` | Token lifetime |
| `CORS_ORIGIN` | `http://localhost:5173` | Allowed browser origin |
| `SEED_DEMO_DATA` | `true` | Creates demo users and tickets in an empty database |
| `PORT` | `8080` | API port |

## Tests and builds

Run backend unit tests and package the API:

```bash
mvn clean test
mvn package
```

Build the frontend:

```bash
cd frontend
npm ci
npm run build
```

## API overview

All routes except login require `Authorization: Bearer <token>`.

| Method | Route | Access |
|---|---|---|
| POST | `/api/auth/login` | Public |
| GET | `/api/auth/me` | Authenticated |
| GET | `/api/organizations` | Super Admin |
| POST | `/api/organizations` | Super Admin; creates tenant and initial admin |
| GET | `/api/users/agents` | Authenticated; returns current tenant only |
| GET | `/api/tickets` | Authenticated; customers see only their own |
| POST | `/api/tickets` | Authenticated |
| GET | `/api/tickets/{id}` | Tenant-scoped; customer ownership enforced |
| PUT | `/api/tickets/{id}` | Admin or agent |
| PATCH | `/api/tickets/{id}/status` | Admin or agent |
| PATCH | `/api/tickets/{id}/assignee` | Admin or agent |
| POST | `/api/tickets/{id}/comments` | Authenticated; internal notes are staff-only |

The ticket list supports `search`, `status`, `priority`, `assignedAgentId`, `page`, and `size` query parameters. `size` is capped at 100.

## Security and tenancy model

- The signed JWT identifies the user; the server reloads that user on every request.
- The trusted user's `tenantId` is always added to database reads. A tenant ID is never accepted from client input.
- Only the platform Super Admin may create organizations. Tenant keys and organization names are unique.
- The Super Admin is kept outside tenant ticket workspaces and cannot use tenant ticket-management APIs.
- Direct ticket lookups use both ticket ID and tenant ID and return 404 for inaccessible records.
- Customers are additionally restricted by requester ID.
- Assignees are validated as Support Agents belonging to the same tenant.
- Internal notes are rejected when submitted by customers and filtered from all customer responses.
- Passwords are stored as BCrypt hashes.

For production, use a long random `JWT_SECRET`, disable demo seeding, configure MongoDB authentication/TLS, and put the service behind HTTPS.

## Project structure

```text
src/main/java/com/kloudspot/helpdesk/
  config/       Security, OpenAPI, Mongo auditing, demo data
  controller/   REST endpoints
  domain/       MongoDB documents and embedded history/comment models
  dto/          Validated request and response contracts
  error/        Consistent API error handling
  repository/   Spring Data MongoDB repositories
  security/     JWT creation and authentication filter
  service/      Authentication, tenant provisioning, and ticket business rules
src/test/       JUnit 5 and Mockito business-rule tests
frontend/       React and Vite single-page application
```

## Assumptions

- Email addresses are globally unique and serve as the login identifier.
- The Super Admin provisions an organization and its first Organization Admin. Further user invitations are outside this assessment's scope.
- Admins and agents have the same ticket-management permissions, while only users with the Support Agent role can be assigned to tickets.
- Ticket deletion is intentionally unsupported to preserve audit history.
- The history records status and assignment changes, as requested. Content edits are not versioned.
=======
# helpdesk_system
>>>>>>> 25e7ad30b991b7fb14ec241789566e62ddd80f0a
