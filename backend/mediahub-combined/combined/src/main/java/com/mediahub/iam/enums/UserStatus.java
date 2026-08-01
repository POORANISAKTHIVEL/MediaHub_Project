package com.mediahub.iam.enums;

// Enum listing the fixed set of states a user account can be in.
// Used by the User entity (stored as a String) to track the account's current lifecycle status.
public enum UserStatus {
    active,     // account is enabled and can be used normally
    suspended,  // account is temporarily blocked (e.g. by an admin)
    inactive    // account is deactivated / soft-deleted and no longer usable
}