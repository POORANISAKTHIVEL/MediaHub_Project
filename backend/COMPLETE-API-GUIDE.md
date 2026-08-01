# MediaHub Integrated Project - Complete Setup & API Guide

This document provides a complete overview of the integrated MediaHub project with gateway, microservices, and JWT-based security.

---

## 1. Architecture Overview

```
┌─────────────────────────┐
│   Client / Postman      │
└────────────┬────────────┘
             │
             ▼
┌──────────────────────────────────┐
│  Spring Cloud Gateway (8080)      │
│  - Routes /content/** → 8093      │
│  - Routes /media/** → 8091        │
│  - Single entry point for clients │
└────┬──────────────────┬───────────┘
     │                  │
     ▼                  ▼
┌────────────────────┐  ┌──────────────────────┐
│  Content Catalog   │  │  MediaHub IAM+Audit  │
│  Service (8093)    │  │  Service (8091)      │
│  - Create content  │  │  - User registration │
│  - Manage creators │  │  - JWT auth          │
│  - Track assets    │  │  - Audit logging     │
│                    │  │  - Role management   │
└────────┬───────────┘  └──────────┬───────────┘
         │                         │
         └────────────┬────────────┘
                      ▼
            ┌──────────────────┐
            │  MySQL Database  │
            │  (mediahub DB)   │
            └──────────────────┘
```

---

## 2. Starting the Application

### Local Maven (Recommended)
Start each service in a separate terminal. All services use Maven wrappers (`mvnw.cmd`):

**Quick start script:**
```bash
cd C:\MediaHub-Integrated
.\run-all.ps1
```
This opens 3 console windows for each service.

**Manual startup:**

Terminal 1 — Content Catalog (runs on port 8093):
```bash
cd contentcatalog_git_individual
.\mvnw.cmd spring-boot:run
```

Terminal 2 — MediaHub IAM/Audit (runs on port 8091):
```bash
cd mediahub-combined/combined
.\mvnw.cmd spring-boot:run
```

Terminal 3 — Gateway (runs on port 8080):
```bash
cd gateway
.\mvnw.cmd spring-boot:run
```

All services are now running and connected to the shared MySQL database at `localhost:3306/mediahub`.

---

## 3. Authentication Flow

### Step 1: Register a New User

```bash
curl -X POST http://localhost:8080/media/mediaHub/iam/auth/register/v1.0 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Alice Creator",
    "email": "alice@mediahub.com",
    "password": "SecurePass123",
    "phone": "+919876543210",
    "country": "IN"
  }'
```

Response:
```json
{
  "message": "Account created successfully"
}
```

### Step 2: Login to Get JWT Token

```bash
curl -X POST http://localhost:8080/media/mediaHub/iam/auth/login/v1.0 \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@mediahub.com",
    "password": "SecurePass123"
  }'
```

Response:
```json
{
  "userId": 1,
  "email": "alice@mediahub.com",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhbGljZUBtZWRpYWh1Yi5jb20iLCJ1c2VySWQiOjEsInJvbGUiOiJDUkVBVE9SIiwiaWF0IjoxNjI0MDAwMDAwLCJleHAiOjE2MjQwMTgwMDB9.signature",
  "expiresIn": 1800000
}
```

Save the `token` value — you'll use it in subsequent requests.

### Step 3: Use Token for Protected Requests

All requests to `/mediahub/contentCatalog/**` require the token in the `Authorization` header:

```bash
curl -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  http://localhost:8080/content/mediahub/contentCatalog/contentAsset/fetchContents
```

---

## 4. Complete API Reference

### Gateway Routes

All client requests go through the gateway at **http://localhost:8080**:

- `/content/**` → Content Catalog Service (8093)
- `/media/**` → MediaHub IAM/Audit Service (8091)

---

## 5. Content Catalog APIs (requires JWT token)

### Base Path: `/content/mediahub/contentCatalog`

#### Content Asset Management

**Create Content** (POST)
```
POST /content/mediahub/contentCatalog/contentAsset/createContent
Authorization: Bearer <token>
Content-Type: application/json

{
  "creatorId": 1,
  "title": "Quantum Physics 101",
  "type": "Video",
  "genre": "Education",
  "language": "en",
  "durationSeconds": 7200,
  "synopsis": "Complete course on quantum mechanics",
  "filePath": "/videos/quantum-101.mp4",
  "thumbnailPath": "/thumbs/quantum-101.jpg",
  "status": "Draft"
}
```

**List All Content** (GET)
```
GET /content/mediahub/contentCatalog/contentAsset/fetchContents
Authorization: Bearer <token>
```

**Get Content by ID** (GET)
```
GET /content/mediahub/contentCatalog/contentAsset/fetchContentById/1
Authorization: Bearer <token>
```

**Update Content** (PUT)
```
PUT /content/mediahub/contentCatalog/contentAsset/updateContent/1
Authorization: Bearer <token>
Content-Type: application/json

{
  "creatorId": 1,
  "title": "Updated Title",
  "type": "Video",
  ...
}
```

**Update Content Status** (PUT)
```
PUT /content/mediahub/contentCatalog/contentAsset/updateContentStatus/1
Authorization: Bearer <token>
Content-Type: application/json

{ "status": "Published" }
```

**Delete Content** (DELETE)
```
DELETE /content/mediahub/contentCatalog/contentAsset/deleteContent/1
Authorization: Bearer <token>
```

#### Creator Management

**Create Creator** (POST)
```
POST /content/mediahub/contentCatalog/creator/createCreator
Authorization: Bearer <token>
Content-Type: application/json

{
  "userId": 1,
  "displayName": "Alice Creator",
  "genre": "Documentary",
  "country": "IN",
  "royaltyTier": "Tier1",
  "bankAccountRef": "ACC-12345",
  "status": "PendingReview"
}
```

**List Creators** (GET)
```
GET /content/mediahub/contentCatalog/creator/fetchCreators
Authorization: Bearer <token>
```

**Get Creator by ID** (GET)
```
GET /content/mediahub/contentCatalog/creator/fetchCreatorById/1
Authorization: Bearer <token>
```

**Update Creator** (PUT)
```
PUT /content/mediahub/contentCatalog/creator/updateCreator/1
Authorization: Bearer <token>
```

**Update Creator Status** (PUT)
```
PUT /content/mediahub/contentCatalog/creator/updateCreatorStatus/1
Authorization: Bearer <token>
Content-Type: application/json

{ "status": "Active" }
```

#### Content Tags

**Add Tag** (POST)
```
POST /content/mediahub/contentCatalog/contentTag/addTag
Authorization: Bearer <token>
Content-Type: application/json

{
  "contentId": 1,
  "tagName": "Action",
  "tagCategory": "Genre"
}
```

**Fetch Tags for Content** (GET)
```
GET /content/mediahub/contentCatalog/contentTag/fetchTagsByContent/1
Authorization: Bearer <token>
```

**Remove Tag** (DELETE)
```
DELETE /content/mediahub/contentCatalog/contentTag/removeTag/5
Authorization: Bearer <token>
```

---

## 6. MediaHub IAM & Audit APIs

### Base Path: `/media/mediaHub`

#### Authentication (Public)

**Register** (POST)
```
POST /media/mediaHub/iam/auth/register/v1.0
Content-Type: application/json

{
  "name": "Admin User",
  "email": "admin@mediahub.com",
  "password": "AdminSecure123",
  "phone": "+919000000001",
  "country": "IN"
}
```

**Login** (POST)
```
POST /media/mediaHub/iam/auth/login/v1.0
Content-Type: application/json

{
  "email": "admin@mediahub.com",
  "password": "AdminSecure123"
}
```

**Logout** (POST)
```
POST /media/mediaHub/iam/auth/logout/v1.0?userId=1
Authorization: Bearer <token>
```

**Refresh Token** (POST)
```
POST /media/mediaHub/iam/auth/refreshToken/v1.0?userId=1
Authorization: Bearer <token>
```

#### User Management

**List Users** (GET)
```
GET /media/mediaHub/iam/users/getAllUsers/v1.0
Authorization: Bearer <admin-token>
```

**Get User by ID** (GET)
```
GET /media/mediaHub/iam/users/getUser/v1/1
Authorization: Bearer <token>
```

**Suspend User** (POST)
```
POST /media/mediaHub/iam/users/suspendUser/v1/1
Authorization: Bearer <admin-token>
```

**Activate User** (POST)
```
POST /media/mediaHub/iam/users/activateUser/v1/1
Authorization: Bearer <admin-token>
```

#### Role Management

**Create Role** (POST)
```
POST /media/mediaHub/iam/roles/createRole/v1.0
Authorization: Bearer <admin-token>
Content-Type: application/json

{ "roleType": "ContentReviewer" }
```

**Get All Roles** (GET)
```
GET /media/mediaHub/iam/roles/getAllRoles/v1.0
Authorization: Bearer <token>
```

**Assign Permission to Role** (POST)
```
POST /media/mediaHub/iam/roles/assignPermission/v1/1
Authorization: Bearer <admin-token>
Content-Type: application/json

{ "permissionId": 5 }
```

#### Permission Management

**Create Permission** (POST)
```
POST /media/mediaHub/iam/permissions/createPermission/v1.0
Authorization: Bearer <admin-token>
Content-Type: application/json

{ "permissionType": "CONTENT_EDIT" }
```

**List Permissions** (GET)
```
GET /media/mediaHub/iam/permissions/getAllPermissions/v1.0
Authorization: Bearer <token>
```

#### Audit Events

**Log Event** (POST)
```
POST /media/mediaHub/auditlog/events/logEvent/v1.0
Authorization: Bearer <token>
Content-Type: application/json

{
  "eventType": "CONTENT_CREATED",
  "moduleSource": "CONTENTCATALOG",
  "performedBy": 1,
  "performedByRole": "Creator",
  "targetEntityType": "ContentAsset",
  "targetEntityId": "123",
  "oldValue": null,
  "newValue": "{\"title\": \"New Video\"}",
  "ipAddress": "192.168.1.1",
  "severity": "INFO",
  "status": "SUCCESS",
  "description": "Creator uploaded new video content"
}
```

**Get All Events** (GET)
```
GET /media/mediaHub/auditlog/events/getAllEvents/v1.0?page=0&size=20
Authorization: Bearer <token>
```

**Generate Report** (POST)
```
POST /media/mediaHub/auditlog/reports/generateReport/v1.0
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "reportName": "July 2026 Audit",
  "reportType": "USAGE",
  "fromDate": "2026-07-01T00:00:00",
  "toDate": "2026-07-07T23:59:59",
  "generatedBy": 1
}
```

---

## 7. Testing in Postman

### Create Postman Environment Variable

1. **Environment** → **Create new**
2. Add variables:
   - `baseUrl` = `http://localhost:8080`
   - `token` = (empty, populate after login)

### Workflow

1. **Register**: Call `/media/mediaHub/iam/auth/register/v1.0`
2. **Login**: Call `/media/mediaHub/iam/auth/login/v1.0` → copy `token`
3. **Set {{token}}**: In Postman, Authorization tab → Bearer Token → `{{token}}`
4. **Create Content**: POST to `/content/mediahub/contentCatalog/contentAsset/createContent`
5. **List Content**: GET `/content/mediahub/contentCatalog/contentAsset/fetchContents`

---

## 8. Security Details

### JWT Token Structure

```
Header: { "alg": "HS256", "typ": "JWT" }
Payload: {
  "sub": "user@email.com",
  "userId": 1,
  "role": "Creator",
  "iat": 1624000000,
  "exp": 1624001800
}
Signature: HMAC-SHA256(secret)
```

### Security Configuration

- **Content Catalog Security**: All `/mediahub/contentCatalog/**` endpoints require JWT
- **Public Endpoints**: `/swagger-ui/**`, `/actuator/**`, `/v3/api-docs/**`
- **Gateway**: Acts as reverse proxy, validates token forwarding
- **Secret Key**: `mediahub_iam_secret_key_2025_must_be_32_chars_min` (shared)
- **Token Expiry**: 30 minutes (1800000 ms)

### Error Responses

**401 Unauthorized** (missing/invalid token):
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication failed: Invalid or expired JWT token"
}
```

**403 Forbidden** (insufficient permissions):
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied"
}
```

---

## 9. Database

All services share a single MySQL database (`mediahub`):

```sql
-- Content Catalog tables
CREATE TABLE creator (creatorId, userId, displayName, genre, country, ...);
CREATE TABLE content_asset (contentId, creatorId, title, type, status, ...);
CREATE TABLE content_tag (tagId, contentId, tagName, tagCategory);

-- IAM tables
CREATE TABLE user (userId, name, email, passwordHash, role, status, ...);
CREATE TABLE role (roleId, roleName, ...);
CREATE TABLE permission (permissionId, permissionType, ...);
CREATE TABLE role_permission (roleId, permissionId);

-- Audit tables
CREATE TABLE audit_event (eventId, eventType, moduleSource, userId, ...);
CREATE TABLE audit_report (reportId, reportName, reportType, ...);
```

---

## 10. Troubleshooting

| Issue | Cause | Solution |
|-------|-------|----------|
| 401 Unauthorized on content API | Missing JWT token | Login first via `/media/mediaHub/iam/auth/login/v1.0` |
| 502 Bad Gateway | Service not running | Check `docker ps` or console output |
| Connection refused (8093/8091) | Database offline | Ensure MySQL is running |
| Invalid JWT signature | Wrong secret key | Verify `jwt.secret` in both services' `application.properties` |
| Token expired | 30 min has passed | Call `/media/mediaHub/iam/auth/refreshToken/v1.0` |

---

## 11. Next Steps

- Deploy to Kubernetes or cloud (AKS, ECS, etc.)
- Add additional modules (Licensing, Royalties, Analytics)
- Implement rate limiting & API throttling
- Add CI/CD pipeline (GitHub Actions, Jenkins)
- Set up monitoring (Prometheus + Grafana)
- Configure centralized logging (ELK stack)
