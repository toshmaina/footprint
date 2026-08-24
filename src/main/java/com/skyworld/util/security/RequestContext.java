package com.skyworld.util.security;

import com.skyworld.domain.enums.MemberStatus;
import io.undertow.util.AttachmentKey;

import java.util.Set;


public record RequestContext(
        long userAccountId,
        Long memberId,
        boolean superUser,
        Set<String> permissions,
        MemberStatus memberStatus
) {

    public static final AttachmentKey<RequestContext> ATTACHMENT_KEY = AttachmentKey.create(RequestContext.class);

    /**
     * system.full_access is the RBAC-driven successor to the old hardcoded SACCO_ADMIN check.
     */
    public static final String PERMISSION_FULL_ACCESS = "system.full_access";

    public boolean isSelfOrSuperUser(Long targetMemberId) {
        return superUser || (memberId != null && memberId.equals(targetMemberId));
    }

    public boolean hasPermission(String permission) {
        return superUser || permissions.contains(permission);
    }

    public boolean isSelfOrHasPermission(Long targetMemberId, String permission) {
        return hasPermission(permission) || (memberId != null && memberId.equals(targetMemberId));
    }

    /**
     * True once the member's own KYC has cleared - gates loan origination, withdrawals, new accounts.
     */
    public boolean isMemberVerified() {
        return memberStatus == MemberStatus.ACTIVE;
    }
}
