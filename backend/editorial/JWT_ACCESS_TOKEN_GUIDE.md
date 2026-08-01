# Editorial Service - JWT Access Token Implementation Guide

## Overview
The Editorial service has been configured to require JWT access tokens from the IAM service (running on port 8091) for all protected endpoints. This document explains how to authenticate and use the API.

## Architecture
- **Port 8091**: IAM Service - Issues JWT tokens
- **Port 8094**: API Gateway - Routes requests through security
- **Port 9097**: Editorial Service - Protected endpoints require JWT tokens

## Step 1: Obtain an Access Token

### Option A: Login with credentials
```bash
curl -X POST http://localhost:8091/mediaHub/iam/auth/login/v1.0 \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 1800,
  "user": {
    "userId": 1,
    "name": "John Doe",
    "email": "user@example.com",
    "roleType": "admin",
    "status": "active"
  }
}
```

### Option B: Use the Editorial TokenClient service
The `TokenClient` service in Editorial can fetch tokens programmatically:

```java
@Autowired
private TokenClient tokenClient;

public void example() {
    String token = tokenClient.getAccessToken("user@example.com", "password123");
    // Use token for authenticated requests
}
```

## Step 2: Use the Access Token

### Making Authenticated Requests
Include the token in the `Authorization` header with `Bearer` prefix:

```bash
curl -X GET http://localhost:9097/MediaHub/editorial/reviews \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Supported Editorial Endpoints (All Protected)
All Editorial endpoints now require JWT authentication:

#### GET Reviews (Public List)
```bash
curl -X GET http://localhost:9097/MediaHub/editorial/reviews \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### POST Submit Review (Requires Token)
```bash
curl -X POST http://localhost:9097/MediaHub/editorial/reviews \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "contentID": 123,
    "reviewerID": 1,
    "comments": "Great content"
  }'
```

#### POST Approve Review
```bash
curl -X POST http://localhost:9097/MediaHub/editorial/reviews/1/approve \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"remarks": "Approved"}'
```

#### POST Reject Review
```bash
curl -X POST http://localhost:9097/MediaHub/editorial/reviews/1/reject \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"remarks": "Rejected"}'
```

#### POST Request Revision
```bash
curl -X POST http://localhost:9097/MediaHub/editorial/reviews/1/revise \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"remarks": "Needs revision"}'
```

#### GET Review by ID
```bash
curl -X GET http://localhost:9097/MediaHub/editorial/reviews/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Step 3: Handle Token Expiration

### Token Refresh
When a token is about to expire, refresh it:

```bash
curl -X POST http://localhost:8091/mediaHub/iam/auth/refreshToken/v1.0?userId=1 \
  -H "Authorization: Bearer YOUR_EXPIRING_TOKEN"
```

### Using TokenClient for Refresh
```java
String newToken = tokenClient.refreshAccessToken(userId);
```

## Error Responses

### No Token Provided (401)
```json
{
  "status": 401,
  "error": "UNAUTHORIZED",
  "message": "No token provided. Please login first.",
  "timestamp": "2026-07-13T12:00:00Z"
}
```

### Invalid or Expired Token (401)
```json
{
  "status": 401,
  "error": "TOKEN_INVALID",
  "message": "Token is invalid or expired. Please login again.",
  "timestamp": "2026-07-13T12:00:00Z"
}
```

## Configuration

### application.properties
```properties
# JWT token validation (must match auth server secret)
jwt.secret=mediahub_iam_secret_key_2025_must_be_32_chars_min
jwt.allowed.issuers=http://localhost:8091,http://localhost:8094

# IAM Service configuration (for token generation)
iam.service.url=http://localhost:8091

# Notification service configuration
notification.service.url=http://localhost:8085
```

## Testing with Postman

1. **Get Token:**
   - Method: POST
   - URL: `http://localhost:8091/mediaHub/iam/auth/login/v1.0`
   - Body: `{"email":"user@example.com","password":"password123"}`
   - Copy the `accessToken` from response

2. **Add to Authorization:**
   - Type: Bearer Token
   - Token: `<paste_accessToken_here>`

3. **Make Requests:**
   - All Editorial endpoints will now include the token automatically

## Gateway Access (Port 8094)
You can also access Editorial through the API Gateway:

```bash
curl -X GET http://localhost:8094/MediaHub/editorial/reviews \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Role-Based Access (Future Enhancement)
The JWT token includes `roleType` claim which can be used for role-based access control:
- Extract role from token: `jwtUtil.extractRoleType(token)`
- Implement role checks in service methods
- Example roles: admin, reviewer, editor, subscriber

## Troubleshooting

### "Connection refused" on port 8091
- Ensure IAM service is running: `http://localhost:8091`
- Check logs for IAM service startup

### "Token is invalid or expired"
- Verify token hasn't expired (expires in 30 minutes by default)
- Refresh token using the refresh endpoint
- Re-login if refresh fails

### "No token provided"
- Verify Authorization header is present
- Header format: `Authorization: Bearer <token>`
- No extra spaces or different token type prefix

## JWT Token Structure
The generated JWT token contains:
```json
{
  "userId": 1,
  "roleType": "admin",
  "email": "user@example.com",
  "iat": 1689340800,
  "exp": 1689342600
}
```

## Implementation Summary

### Files Added/Modified:
1. **pom.xml** - Added JWT and Spring Security dependencies
2. **application.properties** - Added JWT configuration
3. **security/JwtUtil.java** - JWT token validation utility
4. **security/JwtFilter.java** - Request filter for token validation
5. **config/SecurityConfig.java** - Spring Security configuration
6. **client/TokenClient.java** - Client for obtaining tokens from IAM service
7. **client/NotificationClient.java** - Updated to use async calls

### Key Components:
- **JwtUtil**: Validates JWT tokens and extracts claims
- **JwtFilter**: Intercepts requests and validates tokens
- **SecurityConfig**: Configures Spring Security with JWT filter
- **TokenClient**: Fetches tokens from IAM service for Editorial service use

## Next Steps
1. Rebuild the project: `mvn clean install`
2. Start Editorial service: `mvn spring-boot:run`
3. Test endpoints with valid JWT tokens from IAM service
4. Optionally implement role-based access control for specific endpoints
