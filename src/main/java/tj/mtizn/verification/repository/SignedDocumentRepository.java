package tj.mtizn.verification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tj.mtizn.verification.entity.SignedDocument;

import java.util.Optional;

public interface SignedDocumentRepository extends JpaRepository<SignedDocument, Long> {

    Optional<SignedDocument> findByToken(String token);
}
