package ke.co.skyworld.internship.util.security;

import io.undertow.util.AttachmentKey;

import java.util.Set;

/**
 * Represents the authenticated caller for the duration of one request.
 * Attached to the exchange by AuthMiddleware after resolving a bearer
 * token to a user_accounts row plus their effective permission set.
 * <p>
 * Distinct from `customers` (phase 3) — this represents an internal
 * system user (warehouse staff, supervisor, admin) operating the
 * system, not an external party placing orders.
 */
public record RequestContext(
        long userAccountId,
        Long primaryWarehouseId,
        boolean superUser,
        Set<String> permissions
) {

    public static final AttachmentKey<RequestContext> ATTACHMENT_KEY = AttachmentKey.create(RequestContext.class);

    /** RBAC superuser marker permission — bypasses all other permission checks. */
    public static final String PERMISSION_FULL_ACCESS = "system.full_access";

    public boolean hasPermission(String permission) {
        return superUser || permissions.contains(permission);
    }

    public boolean hasAnyPermission(String... candidates) {
        if (superUser) {
            return true;
        }
        for (String permission : candidates) {
            if (permissions.contains(permission)) {
                return true;
            }
        }
        return false;
    }
}