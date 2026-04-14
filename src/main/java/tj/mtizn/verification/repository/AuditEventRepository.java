package tj.mtizn.verification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tj.mtizn.verification.entity.AuditEvent;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
}
