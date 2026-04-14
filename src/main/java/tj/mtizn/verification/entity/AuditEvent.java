package tj.mtizn.verification.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_events")
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private VerificationSession session;

    @Column(nullable = false, length = 50)
    private String eventType;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 45)
    private String ip;

    @Column(length = 512)
    private String userAgent;

    /** SHA-256 цепочный хэш: SHA256(предыдущий_checksum + тип + описание + timestamp + ip) */
    @Column(length = 64)
    private String checksum;

    /** Порядковый номер события в сессии (1, 2, 3...) */
    @Column(nullable = false)
    private int sequenceNumber;

    public AuditEvent() {}

    // --- Getters & Setters ---

    public Long getId() { return id; }

    public VerificationSession getSession() { return session; }
    public void setSession(VerificationSession session) { this.session = session; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }

    public int getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(int sequenceNumber) { this.sequenceNumber = sequenceNumber; }
}
