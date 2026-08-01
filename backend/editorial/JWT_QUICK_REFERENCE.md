# Editorial JWT Authentication - Quick Reference

## 🔑 Get Token (Do This First)
```bash
curl -X POST http://localhost:8091/mediaHub/iam/auth/login/v1.0 \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
```
Copy the `accessToken` from response (starts with `eyJ...`)

## 🚀 Use Token in Requests
Add to every Editorial API call:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## 📋 Common Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/MediaHub/editorial/reviews` | Get all reviews |
| POST | `/MediaHub/editorial/reviews` | Submit new review |
| GET | `/MediaHub/editorial/reviews/{id}` | Get review by ID |
| POST | `/MediaHub/editorial/reviews/{id}/approve` | Approve review |
| POST | `/MediaHub/editorial/reviews/{id}/reject` | Reject review |
| POST | `/MediaHub/editorial/reviews/{id}/revise` | Request revision |

## 🔑 Token Lifecycle

1. **Login** → Get token (valid 30 minutes)
2. **Use** → Include in header
3. **Expires** → Refresh or login again
4. **Refresh** → `POST /mediaHub/iam/auth/refreshToken/v1.0?userId={userId}`

## ⚡ Example: Full Flow

```bash
# 1. Login
TOKEN_RESPONSE=$(curl -s -X POST http://localhost:8091/mediaHub/iam/auth/login/v1.0 \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}')

TOKEN=$(echo $TOKEN_RESPONSE | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

# 2. Use token immediately
curl -X GET http://localhost:9097/MediaHub/editorial/reviews \
  -H "Authorization: Bearer $TOKEN"

# 3. Get review by ID
curl -X GET http://localhost:9097/MediaHub/editorial/reviews/1 \
  -H "Authorization: Bearer $TOKEN"

# 4. Submit new review (requires authentication)
curl -X POST http://localhost:9097/MediaHub/editorial/reviews \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "contentID": 100,
    "reviewerID": 1,
    "comments": "Great content!"
  }'
```

## 🔒 Error Codes

| Status | Error | Solution |
|--------|-------|----------|
| 401 | UNAUTHORIZED | Login to get token |
| 401 | TOKEN_INVALID | Token expired - refresh or re-login |
| 500 | Internal Error | Check service logs |

## 📝 Postman Setup

1. **Get Token:**
   - POST: `http://localhost:8091/mediaHub/iam/auth/login/v1.0`
   - Body: `{"email":"user@example.com","password":"password123"}`

2. **Copy accessToken** to clipboard

3. **In Editorial Requests:**
   - Go to **Authorization** tab
   - Type: **Bearer Token**
   - Token: Paste the accessToken
   - Click **Send**

## 🔧 Service URLs

- **IAM/Token**: http://localhost:8091
- **API Gateway**: http://localhost:8094
- **Editorial**: http://localhost:9097
- **Notifications**: http://localhost:8085

## 💡 Tips

- Tokens expire in 30 minutes
- Keep secret key in `jwt.secret` property
- Never expose tokens in logs
- Use HTTPS in production
- Refresh token 5 minutes before expiry

---
**Last Updated**: July 13, 2026
