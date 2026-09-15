package com.nutriai.modules.auth.domain;

public enum AuditEventType {
    ACCOUNT_CREATED,
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    LOGOUT,
    TOKEN_REFRESHED,
    TOKEN_REVOKED,
    TOKEN_REUSE_DETECTED,
    ACCOUNT_LOCKED
}

