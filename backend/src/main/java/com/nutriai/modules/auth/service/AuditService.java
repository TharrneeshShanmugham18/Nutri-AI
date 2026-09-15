package com.nutriai.modules.auth.service;

import com.nutriai.modules.auth.domain.AuditEventType;
import com.nutriai.modules.auth.domain.AuditLog;
import com.nutriai.modules.auth.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void recordEvent(AuditEventType eventType, UUID userId, String email, String ipAddress, String userAgent, String details) {
        try {
            AuditLog auditLog = new AuditLog(eventType, userId, email, ipAddress, userAgent, details);
            auditLogRepository.save(auditLog);
            log.info("SECURITY_AUDIT: event={} userId={} email={} ip={}", eventType, userId, email, ipAddress);
        } catch (Exception e) {
            log.error("Failed to persist security audit event: {}", eventType, e);
        }
    }
}
