package tj.mtizn.verification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tj.mtizn.verification.entity.VerificationSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<VerificationSession, Long> {

    Optional<VerificationSession> findBySessionId(String sessionId);

    List<VerificationSession> findByInnOrderByCreatedAtDesc(String inn);

    List<VerificationSession> findByOperatorNameContainingIgnoreCaseOrderByCreatedAtDesc(String operatorName);

    List<VerificationSession> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime from, LocalDateTime to);

    List<VerificationSession> findAllByOrderByCreatedAtDesc();
}
