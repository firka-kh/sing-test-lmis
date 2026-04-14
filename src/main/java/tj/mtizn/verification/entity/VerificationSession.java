package tj.mtizn.verification.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "verification_sessions")
public class VerificationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String sessionId;

    @Column(nullable = false, length = 12)
    private String inn;

    @Column(nullable = false)
    private String operatorName;

    @Column(nullable = false)
    private String visitorName;

    @Column(nullable = false)
    private boolean smsConfirmed;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime confirmedAt;

    @Column(length = 45)
    private String createdIp;

    @Column(length = 512)
    private String createdUserAgent;

    private LocalDateTime lastActivityAt;

    /** SHA-256 цепочный хэш: hash(предыдущий_хэш + данные_последнего_события) */
    @Column(length = 64)
    private String integrityHash;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("timestamp ASC")
    private List<AuditEvent> events = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("signedAt ASC")
    private List<SignedDocument> signedDocuments = new ArrayList<>();

    public VerificationSession() {}

    // --- Getters & Setters ---

    public Long getId() { return id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getInn() { return inn; }
    public void setInn(String inn) { this.inn = inn; }

    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }

    public String getVisitorName() { return visitorName; }
    public void setVisitorName(String visitorName) { this.visitorName = visitorName; }

    public boolean isSmsConfirmed() { return smsConfirmed; }
    public void setSmsConfirmed(boolean smsConfirmed) { this.smsConfirmed = smsConfirmed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }

    public String getCreatedIp() { return createdIp; }
    public void setCreatedIp(String createdIp) { this.createdIp = createdIp; }

    public String getCreatedUserAgent() { return createdUserAgent; }
    public void setCreatedUserAgent(String createdUserAgent) { this.createdUserAgent = createdUserAgent; }

    public LocalDateTime getLastActivityAt() { return lastActivityAt; }
    public void setLastActivityAt(LocalDateTime lastActivityAt) { this.lastActivityAt = lastActivityAt; }

    public String getIntegrityHash() { return integrityHash; }
    public void setIntegrityHash(String integrityHash) { this.integrityHash = integrityHash; }

    public List<AuditEvent> getEvents() { return events; }
    public List<SignedDocument> getSignedDocuments() { return signedDocuments; }

    public void addEvent(AuditEvent event) {
        events.add(event);
        event.setSession(this);
    }

    public void addSignedDocument(SignedDocument doc) {
        signedDocuments.add(doc);
        doc.setSession(this);
    }
}
