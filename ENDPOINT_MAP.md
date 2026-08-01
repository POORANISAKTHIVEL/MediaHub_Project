# MediaHub — Backend Endpoint → Frontend Mapping

Gateway (single entry point once wired to the real backend): `http://localhost:8094`. Each row below
shows the real backend endpoint, the Angular client method that calls it, and the screen(s) that use it.
The frontend currently runs with `environment.useMock = true` (in-memory data matching these exact
contracts) — flip it to `false` to call the endpoints below through the gateway.

## Auth (IAM — mediahub-combined, port 8091)
| Method & Path | Frontend method | Screen |
|---|---|---|
| POST `/mediaHub/iam/auth/login/v1.0` | `AuthService.login()` | Login |
| POST `/mediaHub/iam/auth/register/v1.0` | `AuthService.register()` | Register |
| POST `/mediaHub/iam/auth/logout/v1.0?userId=` | `AuthService.logout()` | Topbar / Sidebar logout |
| POST `/mediaHub/iam/auth/refreshToken/v1.0?userId=` | *not wired yet* | — (token refresh flow to add when going live) |

## IAM — Users / Roles / Permissions / Audit
| Method & Path | Frontend method | Screen |
|---|---|---|
| GET `/mediaHub/iam/users/getAllUsers/v1.0` | `IamClient.getAllUsers()` | IAM → Users |
| PUT `/mediaHub/iam/users/updateUser/v1/{id}` | `IamClient.updateUser()` | IAM → Users → Edit modal |
| POST `/mediaHub/iam/users/suspendUser/v1/{id}` | `IamClient.suspendUser()` | IAM → Users → Suspend modal |
| POST `/mediaHub/iam/users/activateUser/v1/{id}` | `IamClient.activateUser()` | IAM → Users → Activate action |
| DELETE `/mediaHub/iam/users/deleteUser/v1/{id}` | `IamClient.deactivateUser()` | IAM → Users → Deactivate modal |
| GET `/mediaHub/iam/roles/getAllRoles/v1.0` | `IamClient.getAllRoles()` | IAM → Roles |
| POST `/mediaHub/iam/roles/createRole/v1.0` | `IamClient.createRole()` | IAM → Roles → New Role modal |
| PUT `/mediaHub/iam/roles/updateRole/v1/{id}` | `IamClient.renameRole()` | IAM → Roles → Rename modal |
| DELETE `/mediaHub/iam/roles/deleteRole/v1/{id}` | `IamClient.deleteRole()` | IAM → Roles → Delete |
| GET `/mediaHub/iam/permissions/getAllPermissions/v1.0` | `IamClient.getAllPermissions()` | IAM → Permissions |
| POST `/mediaHub/iam/permissions/createPermission/v1.0` | `IamClient.createPermission()` | IAM → Permissions → New modal |
| PUT `/mediaHub/iam/permissions/updatePermission/v1/{id}` | `IamClient.updatePermission()` | IAM → Permissions → Edit modal |
| DELETE `/mediaHub/iam/permissions/deletePermission/v1/{id}` | `IamClient.deletePermission()` | IAM → Permissions → Delete |
| GET `/mediaHub/auditlog/events/getAllEvents/v1.0?page&size` | `IamClient.getAuditEvents()` | Audit Log (paginated) |
| GET `/mediaHub/auditlog/events/getByUser/v1/{id}` | folded into `getAuditEvents()` filter | Audit Log → User filter |
| GET `/mediaHub/auditlog/events/getByModule/v1/{module}` | folded into `getAuditEvents()` filter | Audit Log → Module filter |
| PermissionController `getPermissions/v1/{roleId}` | shown client-side via seeded `ROLE_PERMISSIONS` map | IAM → Roles → permission matrix |
| RoleController `assignPermission` / `revokePermission` | *not exposed yet* — matrix is currently read-only | IAM → Roles (documented gap) |
| AuditReportController / CompliancePolicyController | *not exposed yet* | Internal — no screen built for compliance policies/retention in this pass |

## Content Catalog (port 8093)
| Method & Path | Frontend method | Screen |
|---|---|---|
| GET `/mediahub/contentCatalog/contentAsset/fetchContents` | `ContentClient.fetchContents()` | Content Catalog |
| GET `.../contentAsset/fetchContentById/{id}` | `ContentClient.fetchContentById()` | Content Detail |
| POST `.../contentAsset/createContent` | `ContentClient.createContent()` | Content Catalog → Create modal |
| PUT `.../contentAsset/updateContent/{id}` | `ContentClient.updateContent()` | Content Catalog → Edit modal |
| PUT `.../contentAsset/updateContentStatus/{id}` | `ContentClient.updateContentStatus()` | Content Detail → Archive |
| DELETE `.../contentAsset/deleteContent/{id}` | `ContentClient.deleteContent()` | Content Catalog → Delete (Draft only) |
| GET `.../contentAsset/fetchByCreator/{creatorId}` | `ContentClient.contentByCreator()` | Creator Profile |
| GET `.../creator/fetchCreators` | `ContentClient.fetchCreators()` | Creators |
| GET `.../creator/fetchCreatorById/{id}` | `ContentClient.fetchCreatorById()` | Creator Profile |
| POST `.../creator/createCreator` | `ContentClient.createCreator()` | Creators → New Creator modal |
| PUT `.../creator/updateCreator/{id}` | `ContentClient.updateCreator()` | Creators → Edit modal |
| GET `.../contentTag/fetchTagsByContent/{id}` | `ContentClient.tagsByContent()` | Content Detail |
| POST `.../contentTag/addTag` | `ContentClient.addTag()` | Tag Management → New Tag modal |
| DELETE `.../contentTag/removeTag/{id}` | `ContentClient.removeTag()` | Tag Management → Delete |
| GET `.../contentAsset/accessContent/{userId}/{contentId}` | *internal service-to-service* | Not exposed to the frontend — subscription-gated playback isn't part of this build |
| GET `.../contentAsset/validateContent/{id}` , `.../creator/validateCreator/{id}` | *internal service-to-service* | Used by Licensing/Royalty backends, not called from the frontend |

## Editorial (port 9097)
| Method & Path | Frontend method | Screen |
|---|---|---|
| GET `/MediaHub/editorial/reviews` | `EditorialClient.getAllReviews()` | Editorial → Review Queue |
| POST `.../reviews/{id}/approve` | `EditorialClient.approve()` | Review Queue → Accept modal |
| POST `.../reviews/{id}/reject` | `EditorialClient.reject()` | Review Queue → Reject modal |
| POST `.../reviews/{id}/revise` | `EditorialClient.requestRevision()` | Review Queue → Revise modal |
| GET `.../collections` | `EditorialClient.getAllCollections()` | Collections |
| POST `.../collections` | `EditorialClient.createCollection()` | Collections → New Collection modal |
| PUT `.../collections/{id}` | `EditorialClient.updateCollectionItems()` | Collection Detail → Add/Remove |
| POST `.../collections/{id}/expire` | `EditorialClient.expireCollection()` | Collections → Expire |
| DELETE `.../collections/{id}` | `EditorialClient.deleteCollection()` | Collections → Delete |
| GET `.../schedules` | `EditorialClient.getAllSchedules()` | Publication Calendar |
| POST `.../schedules` | `EditorialClient.createSchedule()` | Publication Calendar → New Schedule modal |
| POST `.../schedules/{id}/publish` | `EditorialClient.publishSchedule()` | Publication Calendar → Publish |
| POST `.../schedules/{id}/cancel` | `EditorialClient.cancelSchedule()` | Publication Calendar → Cancel modal |
| DELETE `.../schedules/{id}` | `EditorialClient.deleteSchedule()` | Publication Calendar → Delete |
| GET `.../validateApproval/{contentId}` | *internal service-to-service* | Used by Royalty backend, not called from the frontend |

## Licensing (port 8083)
| Method & Path | Frontend method | Screen |
|---|---|---|
| GET `/mediaHub/contentLicensing/getAllLicenses/v1.0?status=` | `LicensingClient.getAllLicenses()` | Licensing → Licenses tab |
| POST `.../createLicense/v1.0` | `LicensingClient.createLicense()` | Licensing → New License modal |
| GET `.../getLicense/v1.0/{id}` | `LicensingClient.getLicense()` | License Detail / Edit |
| PUT `.../updateLicense/v1.0/{id}` | `LicensingClient.updateLicense()` | License Edit → Save |
| GET `.../getExpiringSoonLicenses/v1.0` | `LicensingClient.getExpiringSoon()` | Licensing → Expiring Soon tab |
| GET `.../getTerritoryRestriction/v1.0/{contentId}` | `LicensingClient.getTerritoryRestrictions()` | Territory Restrictions tab |
| POST `.../createTerritoryRestriction/v1.0` | `LicensingClient.createTerritoryRestriction()` | Territory Restrictions → New modal |
| PUT `.../updateTerritoryRestriction/v1.0/{id}` | `LicensingClient.updateTerritoryRestriction()` | Territory Restrictions → Edit modal |
| GET `.../validateLicensor/{id}` | *internal service-to-service* | Used by Royalty backend, not called from the frontend |
| GET `.../analytics/v1.0` | *not wired into a screen yet* | Documented gap — could feed the Analytics Dashboard's licensingAnalytics section directly from the real endpoint once live |

## Subscription (port 8086)
| Method & Path | Frontend method | Screen |
|---|---|---|
| GET `/mediaHub/subscriptionPlan/plans/fetchPlans` | `SubscriptionClient.fetchPlans()` | Plan Management (admin) + Plan Catalog (subscriber) |
| POST `.../plans/createPlan` | `SubscriptionClient.createPlan()` | Plan Management → New Plan modal |
| PUT `.../plans/updatePlan/{id}` | `SubscriptionClient.updatePlan()` | Plan Management → Edit / Activate / Deactivate |
| GET `.../usersubscriptions/fetchSubscriptions` | `SubscriptionClient.fetchSubscriptions()` | User Subscriptions (admin) |
| POST `.../usersubscriptions/createSubscription` | `SubscriptionClient.createSubscription()` | Plan Catalog → Subscribe |
| PUT `.../usersubscriptions/updateSubscription/{id}` | `SubscriptionClient.updateSubscription()` | (plan-change path — wired in client, not yet surfaced as its own admin modal) |
| PUT `.../usersubscriptions/renewSubscription/{id}` | `SubscriptionClient.renewSubscription()` | User Subscriptions → Renew modal |
| PUT `.../usersubscriptions/cancelSubscription/{id}` | `SubscriptionClient.cancelSubscription()` | User Subscriptions → Cancel, My Subscription → Cancel |
| GET `.../usersubscriptions/validateSubscription/{userId}` | `SubscriptionClient.fetchSubscriptionForUser()` (mock-side equivalent) | My Subscription |
| GET `.../subscriptionhistory/fetchHistories` | `SubscriptionClient.fetchHistories()` | Subscription History tab |
| GET `.../usersubscriptions/analytics` | `SubscriptionClient.analytics()` | (available on the client; not yet placed on a dashboard card) |
| UserController (`/users/*`) — subscription service's own denormalized user cache | *not called from the frontend* | The frontend treats IAM as the single source of truth for user identity |

## Royalty (port 8045)
| Method & Path | Frontend method | Screen |
|---|---|---|
| GET `/api/royalty-rules` | `RoyaltyClient.getAllRules()` | Royalty → Rules |
| POST `/api/royalty-rules` | `RoyaltyClient.createRule()` | Rules → New Rule modal |
| PUT `/api/royalty-rules/{id}/deactivate` | `RoyaltyClient.deactivateRule()` | Rules → Deactivate |
| DELETE `/api/royalty-rules/{id}` | `RoyaltyClient.deleteRule()` | Rules → Delete |
| GET `/api/royalty-statements` | `RoyaltyClient.getAllStatements()` | Royalty → Statements |
| POST `/api/royalty-statements` | `RoyaltyClient.generateStatement()` | Statements → Generate modal |
| GET `/api/royalty-statements/{id}` | `RoyaltyClient.getStatement()` | Statement Detail |
| PUT `/api/royalty-statements/{id}/finalise` | `RoyaltyClient.finaliseStatement()` | Statements / Statement Detail → Finalise |
| PUT `/api/royalty-statements/{id}/mark-paid` | `RoyaltyClient.markPaid()` | Statements → Mark Paid |
| GET `/api/royalty-payouts` | `RoyaltyClient.getAllPayouts()` | Royalty → Payouts |
| POST `/api/royalty-payouts` | `RoyaltyClient.createPayout()` | Payouts → Process modal, Statement Detail → Process Payout |
| PUT `/api/royalty-payouts/{id}/process` | `RoyaltyClient.markProcessed()` | Payouts → Mark Processed |
| PUT `/api/royalty-payouts/{id}/fail?reason=` | `RoyaltyClient.markFailed()` | Payouts → Mark Failed modal |
| DELETE `/api/royalty-payouts/{id}` | *not wired to a button yet* | Documented gap |
| GET `/api/royalty-statements/analytics` | *not wired yet* | Revenue Dashboard currently computes its own totals client-side from the statements list |

## Notification (port 8085)
| Method & Path | Frontend method | Screen |
|---|---|---|
| GET `/mediaHub/notifications/getAllNotifications/v1.0/{userId}` | `NotificationClient.getAllForUser()` | Notifications (All tab) |
| GET `.../getUnreadNotifications/v1.0/{userId}` | `NotificationClient.getUnreadForUser()` | (client method available; the list screen currently filters the "All" result client-side for the Unread tab instead of a second call) |
| POST `.../createNotification/v1.0` | `NotificationClient.create()` | *not exposed as a user-facing "compose" screen* — every other module's backend fires this server-side on real events |
| PUT `.../updateNotification/v1.0/{id}?status=` | `NotificationClient.updateStatus()` | Notifications → click to mark read, Dismiss button |
| GET `.../analytics/v1.0` | `NotificationClient.analytics()` | (available on the client; not yet placed on a dashboard card) |

## Analytics (port 8098)
| Method & Path | Frontend method | Screen |
|---|---|---|
| GET `/mediaHub/analytics/dashboard` | `AnalyticsClient.getDashboard()` | Analytics Dashboard |
| GET `/mediaHub/reports/generate` (POST) | `AnalyticsClient.generateReport()` | Reports → Generate |
| GET `/mediaHub/reports/{id}` | *not wired yet* | Reports list is read from the in-memory store directly rather than re-fetching by id |
| DELETE `/mediaHub/reports/{id}` | `AnalyticsClient.deleteReport()` | Reports → Delete |
| GET `/mediaHub/reports/download/{id}` | *not wired yet — mocked as a toast* | Real `.xlsx` byte-stream download to be wired when going live |

## Gateway / Eureka
Not called directly by the frontend — `environment.apiBaseUrl` (`http://localhost:8094`) is the base
every real HTTP call in each `*Client` would use once `environment.useMock` flips to `false`. Eureka
(8761) is infrastructure-only; the frontend never talks to it.
