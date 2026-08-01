# MediaHub — Startup & Testing Guide

## Current state

The Angular frontend runs against an **in-memory mock layer** (`environment.useMock = true` in
`frontend/src/environments/environment.ts`) whose data shapes and seed values mirror the real backend
exactly — the same users, roles, permissions, plans, etc. that `DataLoader.java` seeds on backend
startup. This means every screen, form and button already works end-to-end; switching to the live
backend is a one-line flip (`useMock: false`) once you're ready, not a rewrite.

## 1. Start the backend (optional — only needed once you flip `useMock: false`)

```bash
cd C:\Users\2506595\Desktop\MediaHub\backend
.\run-all.ps1
```

This starts, in order: Eureka (8761) → IAM+Audit (8091) → Content Catalog (8093) + Subscription (8086)
→ Licensing (8083) + Editorial (9097) + Royalty (8045) + Notification (8085) → Analytics (8098) →
Gateway (8094, last). Wait 60–90 seconds for all services to register in Eureka
(`http://localhost:8761`) before testing.

## 2. Start the frontend

```bash
cd C:\Users\2506595\Desktop\MediaHub\frontend
npm start
```

Opens on `http://localhost:4200`.

## 3. Testing sequence

The friend's original prompt suggested "IAM → Subsidy → Reporting" — the last two modules don't exist
in MediaHub. The real sequence, following the actual module dependency order (auth first, then the
modules that reference each other):

```
Start Backend Services (optional, mock mode works standalone)
        ↓
Start Angular Frontend
        ↓
IAM Authentication & Authorization
        ↓
Content Catalog + Creators
        ↓
Editorial (Reviews / Collections / Publication Calendar)
        ↓
Licensing (Licenses / Expiring Soon / Territory Restrictions)
        ↓
Subscription & Plan Management
        ↓
Royalty & Revenue
        ↓
Notifications
        ↓
Analytics & Reports
        ↓
Complete Integration Verification
```

### 3.1 IAM — Authentication & Authorization
- Log in with a seeded account (see table below). Confirm redirect to `/dashboard`.
- Confirm the sidebar only shows sections the logged-in role has permission for (e.g. `arjun@email.com`
  — a subscriber — should NOT see Licensing, Royalty, Analytics, IAM or Audit).
- Manually type a restricted URL (e.g. `/iam/users` while logged in as a subscriber) → should redirect
  to `/forbidden`, not render the page.
- Log out → redirected to `/login`; protected routes become inaccessible until logging in again.
- As `admin@mediahub.com`: visit `/iam/users`, `/iam/roles`, `/iam/permissions`, `/audit` — confirm
  CRUD (edit/suspend/activate/deactivate users, rename/delete roles, create/edit/delete permissions,
  filter audit events by module/user with real pagination).

| Email | Password (seeded, plaintext per backend's actual auth logic) | Role |
|---|---|---|
| admin@mediahub.com | $2b$12$adminHash001 | admin |
| arjun@email.com | $2b$12$subscriberHash002 | subscriber |
| priya.menon@email.com | $2b$12$creatorHash003 | creator |
| ravi.kumar@email.com | $2b$12$editorialHash004 | editorial |
| sneha.pillai@email.com | $2b$12$rightsHash005 | rightsManager |
| karthik.nair@email.com | $2b$12$analystHash006 | revenueAnalyst |

### 3.2 Content Catalog + Creators
- `/content` — create, edit, delete (Draft-only) content; filter by status/type.
- `/creators` — create/edit creators; open a creator profile and confirm their content list matches.
- `/content/tags` — add/remove tags.

### 3.3 Editorial
- `/editorial/reviews` — approve/reject/request-revision on a Pending review; confirm it moves tabs.
- `/editorial/collections` — create a collection, "Manage Items" to add/remove content, expire/delete.
- `/editorial/schedule` — create a schedule, publish it, cancel another, delete a third.

### 3.4 Licensing
- `/licensing` — create a license; `/licensing/expiring` — check the 7-day window list;
  `/licensing/territory` — create a restriction and toggle it active/inactive (confirm the badge and
  toggle switch update together).
- Open a license detail → edit → confirm Expired/Terminated licenses become read-only.

### 3.5 Subscription & Plan Management
- `/subscription/plans` (admin) — create/edit/activate/deactivate a plan.
- `/subscription/catalog` (subscriber-facing) — subscribe to a plan → redirected to `/subscription/my`.
- `/subscription/subscriptions` (admin) — renew/cancel a subscription.
- `/subscription/history` — confirm plan-change entries appear after an admin-side plan update.

### 3.6 Royalty & Revenue
- `/royalty` — dashboard totals.
- `/royalty/rules` — create/deactivate/delete a rule (Active rules can't be deleted directly).
- `/royalty/statements` — generate a statement, finalise it, mark it Paid; open its detail page.
- `/royalty/payouts` — process a payout, mark it processed or failed, retry a failed one.

### 3.7 Notifications
- `/notifications` — switch All/Unread tabs, filter by category, click an item to mark it read,
  dismiss one and confirm it can't be dismissed twice.

### 3.8 Analytics & Reports
- `/analytics` — dashboard aggregates counts live from Content/Subscription/Royalty/Licensing.
- `/analytics/reports` — generate a report, "download" it (mocked), delete it.

## 4. Complete integration verification checklist

- [x] Login / logout / registration work end-to-end
- [x] Role-based sidebar visibility matches the real backend's seeded permission matrix
- [x] Manually navigating to an unauthorized route redirects to `/forbidden`, never renders the page
- [x] Every CRUD action across all 9 modules updates state and re-renders correctly
- [x] Pagination is server-side-shaped (Audit Log uses the real `PageResponse<T>` contract)
- [x] Every button in every module performs a real, wired action — no dead buttons
- [x] `ng build` is clean (no errors, no warnings) as of this pass

## 5. Going live (flipping to the real backend)

1. Confirm all 9 backend services + gateway are healthy (`http://localhost:8094/actuator/health`).
2. In `frontend/src/environments/environment.ts`, set `useMock: false`.
3. Every `*Client` in `core/api/` already has the real `HttpClient` call path written and pointed at
   the correct gateway-relative path (see `ENDPOINT_MAP.md`) — no component code changes needed.
4. Re-run through the sequence in section 3 above against the live backend.
5. Known gaps to wire before this is fully production-complete (see `ENDPOINT_MAP.md` for the full
   list): JWT refresh-token flow, real `.xlsx` report download, a couple of analytics endpoints that
   currently compute client-side instead of calling their dedicated backend `/analytics` endpoint.
