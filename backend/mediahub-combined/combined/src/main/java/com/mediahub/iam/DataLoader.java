package com.mediahub.iam;

import com.mediahub.iam.entity.Permission;
import com.mediahub.iam.entity.Role;
import com.mediahub.iam.entity.RolePermission;
import com.mediahub.iam.entity.User;
import com.mediahub.iam.enums.UserStatus;
import com.mediahub.iam.repository.PermissionRepository;
import com.mediahub.iam.repository.RolePermissionRepository;
import com.mediahub.iam.repository.RoleRepository;
import com.mediahub.iam.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Startup component that seeds the database with initial roles, permissions, and users.
// Implements CommandLineRunner so its run() method executes automatically once the app boots.
@Component
public class DataLoader implements CommandLineRunner {

    // Repositories for each entity, injected by Spring, used to persist the seed data.
    @Autowired private RoleRepository           roleRepository;
    @Autowired private PermissionRepository     permissionRepository;
    @Autowired private RolePermissionRepository rolePermissionRepository;
    @Autowired private UserRepository           userRepository;

    // ── Admin credentials from application.properties ─────────────────────────
    // The default admin account details, injected from external config so they aren't hardcoded here.
    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.name}")
    private String adminName;

    @Value("${app.admin.phone}")
    private String adminPhone;

    // Entry point run automatically at application startup to populate baseline data.
    @Override
    public void run(String... args) throws Exception {

        // Idempotency guard: if roles already exist, the DB has been seeded before, so skip to avoid duplicates.
        if (roleRepository.count() > 0) {
            System.out.println("✅ Seed data already exists — verifying admin permissions.");
            migrateAdminPermissions();
            migrateNotificationPermissions();
            return;
        }

        System.out.println("🌱 Seeding database...");

        // ── Roles ──────────────────────────────────────────────────────────────
        // Create the six baseline roles and keep references to them for the assignments below.
        Role subscriber    = saveRole("subscriber");
        Role creator       = saveRole("creator");
        Role editorial     = saveRole("editorial");
        Role rightsMgr     = saveRole("rightsManager");
        Role revenueAnlyst = saveRole("revenueAnalyst");
        Role admin         = saveRole("admin");

        // ── Permissions ───────────────────────────────────────────────────────
        // Create the base fine-grained permissions and hold references for the role assignments below.
        Permission contentRead      = savePerm("content:read");
        Permission contentWrite     = savePerm("content:write");
        Permission contentPublish   = savePerm("content:publish");
        Permission contentDelete    = savePerm("content:delete");
        Permission royaltyView      = savePerm("royalty:view");
        Permission royaltyApprove   = savePerm("royalty:approve");
        Permission planConfigure    = savePerm("plan:configure");
        Permission planView         = savePerm("plan:view");
        Permission subscriptionView = savePerm("subscription:view");
        Permission subscriptionManage = savePerm("subscription:manage");
        Permission roleManage       = savePerm("role:manage");
        Permission permissionManage = savePerm("permission:manage");
        Permission userSuspend      = savePerm("user:suspend");
        Permission userManage       = savePerm("user:manage");
        Permission reportView       = savePerm("report:view");
        Permission licenseManage    = savePerm("license:manage");
        Permission auditRead        = savePerm("audit:read");
        Permission notificationSend      = savePerm("notification:send");
        Permission notificationView      = savePerm("notification:view");
        Permission notificationUpdate    = savePerm("notification:update");
        Permission notificationAnalytics = savePerm("notification:analytics");

        // ── Role-Permission assignments ───────────────────────────────────────
        // Link each role to the permissions it should have, building the access-control matrix.
        // Lower roles get a few permissions; the admin role is granted all of them.
        assign(subscriber,    contentRead);
        assign(subscriber,    planView);
        assign(subscriber,    subscriptionView);
        assign(creator,       contentRead);
        assign(creator,       contentWrite);
        assign(creator,       royaltyView);
        assign(creator,       planView);
        assign(creator,       subscriptionView);
        assign(editorial,     contentRead);
        assign(editorial,     contentPublish);
        assign(editorial,     contentDelete);
        assign(editorial,     planView);
        assign(editorial,     subscriptionView);
        assign(rightsMgr,     contentRead);
        assign(rightsMgr,     licenseManage);
        assign(rightsMgr,     planView);
        assign(rightsMgr,     subscriptionView);
        assign(revenueAnlyst, royaltyView);
        assign(revenueAnlyst, royaltyApprove);
        assign(revenueAnlyst, reportView);
        assign(revenueAnlyst, planView);
        assign(revenueAnlyst, subscriptionView);
        assign(admin,         contentRead);
        assign(admin,         contentWrite);
        assign(admin,         contentPublish);
        assign(admin,         contentDelete);
        assign(admin,         royaltyView);
        assign(admin,         royaltyApprove);
        assign(admin,         planConfigure);
        assign(admin,         planView);
        assign(admin,         subscriptionView);
        assign(admin,         subscriptionManage);
        assign(admin,         roleManage);
        assign(admin,         permissionManage);
        assign(admin,         userSuspend);
        assign(admin,         userManage);
        assign(admin,         reportView);
        assign(admin,         licenseManage);
        assign(admin,         auditRead);
        assign(admin,         notificationView);
        assign(admin,         notificationSend);
        assign(admin,         notificationUpdate);
        assign(admin,         notificationAnalytics);

        // ── Admin user — reads from application.properties ────────────────────
        saveUser(adminName, admin, adminEmail, adminPhone, "IN", adminPassword);

        // ── Sample users ──────────────────────────────────────────────────────
        saveUser("Arjun Sharma",  subscriber,    "arjun@email.com",        "+91-9000000002", "IN", "$2b$12$subscriberHash002");
        saveUser("Priya Menon",   creator,       "priya.menon@email.com",  "+91-9000000003", "IN", "$2b$12$creatorHash003");
        saveUser("Ravi Kumar",    editorial,     "ravi.kumar@email.com",   "+91-9000000004", "IN", "$2b$12$editorialHash004");
        saveUser("Sneha Pillai",  rightsMgr,     "sneha.pillai@email.com", "+91-9000000005", "IN", "$2b$12$rightsHash005");
        saveUser("Karthik Nair",  revenueAnlyst, "karthik.nair@email.com", "+91-9000000006", "IN", "$2b$12$analystHash006");

        System.out.println("✅ Seeding complete — 6 roles, 18 permissions, 6 users loaded.");
    }

    // Helper that builds a Role with the given type, persists it, and returns
    // the saved entity (with its generated id) so callers can reference it.
    private Role saveRole(String roleType) {
        Role r = new Role();
        r.setRoleType(roleType);
        return roleRepository.save(r);
    }

    // Helper that builds a Permission with the given type, persists it, and
    // returns the saved entity so it can be linked to roles afterwards.
    private Permission savePerm(String permissionType) {
        Permission p = new Permission();
        p.setPermissionType(permissionType);
        return permissionRepository.save(p);
    }

    // Helper to find or create a permission by name.
    private Permission findOrCreatePermission(String permissionType) {
        return permissionRepository.findByPermissionType(permissionType)
                .orElseGet(() -> savePerm(permissionType));
    }

    // Helper to ensure a role has a permission; if missing, create the join row.
    private void assignIfMissing(Role role, String permissionType) {
        Permission permission = findOrCreatePermission(permissionType);
        if (rolePermissionRepository.findByRoleAndPermission(role, permission).isEmpty()) {
            assign(role, permission);
        }
    }

    // Backfills the admin role with the complete permission set on every startup,
    // even when the DB was seeded by an older loader that predates some permissions
    // (e.g. content:publish, content:delete). Idempotent: only missing rows are added.
    private void migrateAdminPermissions() {
        Role admin = roleRepository.findByRoleType("admin").orElse(null);
        if (admin == null) {
            System.out.println("⚠️ Admin role not found during permission migration.");
            return;
        }

        String[] adminPermissions = {
                "content:read", "content:write", "content:publish", "content:delete",
                "royalty:view", "royalty:approve",
                "plan:configure", "plan:view",
                "subscription:view", "subscription:manage",
                "role:manage", "permission:manage",
                "user:suspend", "user:manage",
                "report:view", "license:manage", "audit:read"
        };
        for (String perm : adminPermissions) {
            assignIfMissing(admin, perm);
        }
        System.out.println("✅ Admin permissions verified/backfilled (" + adminPermissions.length + " permissions).");
    }

    private void migrateNotificationPermissions() {
        Role admin = roleRepository.findByRoleType("admin").orElse(null);
        if (admin == null) {
            System.out.println("⚠️ Admin role not found during migration.");
            return;
        }

        assignIfMissing(admin, "notification:send");
        assignIfMissing(admin, "notification:view");
        assignIfMissing(admin, "notification:update");
        assignIfMissing(admin, "notification:analytics");
        System.out.println("✅ Notification permissions verified for admin.");
    }

    // Helper that creates and saves a join-row linking one role to one
    // permission, recording a single entry in the role-permission matrix.
    private void assign(Role role, Permission permission) {
        RolePermission rp = new RolePermission();
        rp.setRole(role);
        rp.setPermission(permission);
        rolePermissionRepository.save(rp);
    }

    // Helper that assembles a User from the given fields, defaults it to active
    // and non-revoked, and persists it as a seed account for the given role.
    private void saveUser(String name, Role role, String email,
                          String phone, String country, String passwordHash) {
        User user = new User();
        user.setName(name);
        user.setRole(role);
        user.setEmail(email);
        user.setPhone(phone);
        user.setCountry(country);
        user.setPasswordHash(passwordHash);
        user.setStatus(UserStatus.active);
        user.setIsRevoked(false);
        userRepository.save(user);
    }
}