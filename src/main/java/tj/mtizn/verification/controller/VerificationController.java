package tj.mtizn.verification.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tj.mtizn.verification.entity.AuditEvent;
import tj.mtizn.verification.entity.SignedDocument;
import tj.mtizn.verification.entity.VerificationSession;
import tj.mtizn.verification.repository.SessionRepository;
import tj.mtizn.verification.repository.SignedDocumentRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/verification")
public class VerificationController {

    private static final String DEMO_SMS_CODE = "123456";
    private static final String DEMO_PIN_CODE = "123456";
    private static final DateTimeFormatter TS_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final SessionRepository sessionRepo;
    private final SignedDocumentRepository documentRepo;

    public VerificationController(SessionRepository sessionRepo, SignedDocumentRepository documentRepo) {
        this.sessionRepo = sessionRepo;
        this.documentRepo = documentRepo;
    }

    // ─── Утилиты ───

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) return "unknown";
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            String first = xff.split(",")[0].trim();
            if (!first.isEmpty()) return first;
        }
        String remoteAddr = request.getRemoteAddr();
        return (remoteAddr == null || remoteAddr.isBlank()) ? "unknown" : remoteAddr;
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private AuditEvent createEvent(VerificationSession session, String type, String description, HttpServletRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String ip = resolveClientIp(request);
        String ua = request != null ? request.getHeader("User-Agent") : "unknown";

        AuditEvent event = new AuditEvent();
        event.setEventType(type);
        event.setDescription(description);
        event.setTimestamp(now);
        event.setIp(ip);
        event.setUserAgent(ua);
        event.setSequenceNumber(session.getEvents().size() + 1);

        // Цепочный хэш: SHA256(предыдущий_хэш | тип | описание | timestamp | ip)
        String prevHash = session.getIntegrityHash() != null ? session.getIntegrityHash() : "GENESIS";
        String payload = prevHash + "|" + type + "|" + description + "|" + now.format(TS_FORMATTER) + "|" + ip;
        String checksum = sha256(payload);
        event.setChecksum(checksum);

        session.addEvent(event);
        session.setLastActivityAt(now);
        session.setIntegrityHash(checksum);

        return event;
    }

    // ─── Существующие эндпоинты ───

    @PostMapping("/check-inn")
    public Map<String, Object> checkInn(@RequestBody Map<String, String> payload) {
        String inn = payload.get("inn");
        Map<String, Object> response = new HashMap<>();
        if (inn != null && inn.length() == 12) {
            response.put("found", true);
            response.put("message", "ИНН найден в системе");
        } else {
            response.put("found", false);
            response.put("message", "Введен некорректный ИНН");
        }
        return response;
    }

    @PostMapping("/create-session")
    @Transactional
    public Map<String, Object> createSession(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String inn = payload.get("inn");
        String operatorName = payload.getOrDefault("operatorName", "Не указан");
        String visitorName = payload.getOrDefault("visitorName", "Не указан");

        Map<String, Object> response = new HashMap<>();
        if (inn == null || inn.length() != 12) {
            response.put("created", false);
            response.put("error", "Некорректный ИНН");
            return response;
        }

        VerificationSession session = new VerificationSession();
        session.setSessionId("SES-" + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase());
        session.setInn(inn);
        session.setOperatorName(operatorName);
        session.setVisitorName(visitorName);
        session.setSmsConfirmed(false);
        session.setCreatedAt(LocalDateTime.now());
        session.setCreatedIp(resolveClientIp(request));
        session.setCreatedUserAgent(request != null ? request.getHeader("User-Agent") : "unknown");
        session.setLastActivityAt(session.getCreatedAt());

        createEvent(session, "SESSION_CREATED", "Сессия создана оператором " + operatorName + " для посетителя " + visitorName, request);
        createEvent(session, "SMS_SENT", "Одноразовый SMS-код отправлен на номер посетителя", request);

        sessionRepo.save(session);

        response.put("created", true);
        response.put("sessionId", session.getSessionId());
        response.put("smsSent", true);
        response.put("maskedPhone", "+992 93 *** ** 78");
        response.put("demoSmsCode", DEMO_SMS_CODE);
        response.put("createdAt", session.getCreatedAt().format(TS_FORMATTER));
        response.put("ip", session.getCreatedIp());
        response.put("userAgent", session.getCreatedUserAgent());
        return response;
    }

    @PostMapping("/confirm-session")
    @Transactional
    public Map<String, Object> confirmSession(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String sessionId = payload.get("sessionId");
        String smsCode = payload.get("smsCode");
        Map<String, Object> response = new HashMap<>();

        Optional<VerificationSession> opt = sessionRepo.findBySessionId(sessionId);
        if (opt.isEmpty()) {
            response.put("confirmed", false);
            response.put("error", "Сессия не найдена");
            return response;
        }

        VerificationSession session = opt.get();

        if (smsCode == null || smsCode.length() != 6 || !DEMO_SMS_CODE.equals(smsCode)) {
            response.put("confirmed", false);
            response.put("error", "Неверный SMS-код");
            return response;
        }

        session.setSmsConfirmed(true);
        session.setConfirmedAt(LocalDateTime.now());
        createEvent(session, "SMS_CONFIRMED", "Посетитель подтвердил сессию по SMS-коду", request);
        sessionRepo.save(session);

        response.put("confirmed", true);
        response.put("sessionId", session.getSessionId());
        response.put("confirmedAt", session.getConfirmedAt().format(TS_FORMATTER));
        return response;
    }

    @PostMapping("/sign-document")
    @Transactional
    public Map<String, Object> signDocument(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String sessionId = payload.get("sessionId");
        String pin = payload.get("pin");
        String documentName = payload.getOrDefault("documentName", "Документ");
        String documentVersion = payload.getOrDefault("documentVersion", "v1.0");
        String operatorName = payload.getOrDefault("operatorName", "Не указан");
        String visitorName = payload.getOrDefault("visitorName", "Не указан");

        Map<String, Object> response = new HashMap<>();
        Optional<VerificationSession> opt = sessionRepo.findBySessionId(sessionId);
        if (opt.isEmpty()) {
            response.put("signed", false);
            response.put("error", "Сессия не найдена");
            return response;
        }

        VerificationSession session = opt.get();
        if (!session.isSmsConfirmed()) {
            response.put("signed", false);
            response.put("error", "Сессия не подтверждена по SMS");
            return response;
        }
        if (pin == null || pin.length() != 6 || !DEMO_PIN_CODE.equals(pin)) {
            response.put("signed", false);
            response.put("error", "Неверный PIN-код");
            return response;
        }

        SignedDocument doc = new SignedDocument();
        doc.setToken("DOC-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase());
        doc.setDocumentName(documentName);
        doc.setDocumentVersion(documentVersion);
        doc.setOperatorName(operatorName);
        doc.setVisitorName(visitorName);
        doc.setStatus("Подписано и принято в обработку");
        doc.setSignedAt(LocalDateTime.now());

        session.addSignedDocument(doc);
        createEvent(session, "DOCUMENT_SIGNED", "Подписан документ: " + documentName + " (" + documentVersion + "), токен " + doc.getToken(), request);
        sessionRepo.save(session);

        response.put("signed", true);
        response.put("token", doc.getToken());
        response.put("status", doc.getStatus());
        response.put("signedAt", doc.getSignedAt().format(TS_FORMATTER));
        response.put("checkUrl", "/report.html?session=" + session.getSessionId());
        return response;
    }

    @GetMapping("/session/{sessionId}")
    @Transactional(readOnly = true)
    public Map<String, Object> getSession(@PathVariable("sessionId") String sessionId) {
        Map<String, Object> response = new HashMap<>();
        Optional<VerificationSession> opt = sessionRepo.findBySessionId(sessionId);
        if (opt.isEmpty()) {
            response.put("found", false);
            response.put("error", "Сессия не найдена");
            return response;
        }

        VerificationSession session = opt.get();
        response.put("found", true);
        response.put("sessionId", session.getSessionId());
        response.put("inn", session.getInn());
        response.put("operatorName", session.getOperatorName());
        response.put("visitorName", session.getVisitorName());
        response.put("smsConfirmed", session.isSmsConfirmed());
        response.put("createdAt", session.getCreatedAt().format(TS_FORMATTER));
        response.put("confirmedAt", session.getConfirmedAt() != null ? session.getConfirmedAt().format(TS_FORMATTER) : null);
        response.put("createdIp", session.getCreatedIp());
        response.put("createdUserAgent", session.getCreatedUserAgent());
        response.put("lastActivityAt", session.getLastActivityAt() != null ? session.getLastActivityAt().format(TS_FORMATTER) : null);
        response.put("integrityHash", session.getIntegrityHash());
        response.put("durationSeconds", Duration.between(session.getCreatedAt(), LocalDateTime.now()).getSeconds());

        List<Map<String, String>> events = new ArrayList<>();
        for (AuditEvent e : session.getEvents()) {
            Map<String, String> em = new LinkedHashMap<>();
            em.put("seq", String.valueOf(e.getSequenceNumber()));
            em.put("type", e.getEventType());
            em.put("description", e.getDescription());
            em.put("timestamp", e.getTimestamp().format(TS_FORMATTER));
            em.put("ip", e.getIp());
            em.put("userAgent", e.getUserAgent());
            em.put("checksum", e.getChecksum());
            events.add(em);
        }
        response.put("events", events);

        List<Map<String, String>> signedDocuments = new ArrayList<>();
        for (SignedDocument doc : session.getSignedDocuments()) {
            Map<String, String> dm = new LinkedHashMap<>();
            dm.put("token", doc.getToken());
            dm.put("documentName", doc.getDocumentName());
            dm.put("documentVersion", doc.getDocumentVersion());
            dm.put("status", doc.getStatus());
            dm.put("signedAt", doc.getSignedAt().format(TS_FORMATTER));
            signedDocuments.add(dm);
        }
        response.put("signedDocuments", signedDocuments);
        return response;
    }

    @GetMapping("/check")
    @Transactional(readOnly = true)
    public Map<String, Object> checkDocument(@RequestParam("token") String token) {
        Map<String, Object> response = new HashMap<>();
        Optional<SignedDocument> opt = documentRepo.findByToken(token);
        if (opt.isEmpty()) {
            response.put("found", false);
            response.put("error", "Документ не найден");
            return response;
        }

        SignedDocument doc = opt.get();
        response.put("found", true);
        response.put("token", doc.getToken());
        response.put("sessionId", doc.getSession().getSessionId());
        response.put("documentName", doc.getDocumentName());
        response.put("documentVersion", doc.getDocumentVersion());
        response.put("operatorName", doc.getOperatorName());
        response.put("visitorName", doc.getVisitorName());
        response.put("status", doc.getStatus());
        response.put("signedAt", doc.getSignedAt().format(TS_FORMATTER));
        return response;
    }

    @PostMapping("/verify-visitor")
    public Map<String, Object> verifyVisitor(@RequestBody Map<String, String> payload) {
        String pin = payload.get("pin");
        String birthDate = payload.get("birthDate");
        String inn = payload.get("inn");
        Map<String, Object> response = new HashMap<>();
        if (pin != null && pin.length() == 6 && inn != null && inn.length() == 12) {
            response.put("verified", true);
            response.put("fullName", "Рахимов Алишер Бахтиёрович");
        } else {
            response.put("verified", false);
            response.put("error", "Вы ввели ИНН: " + inn + ", ПИН: " + pin);
        }
        return response;
    }

    // ─── Новые эндпоинты: поиск и отчёт ───

    /** Список всех сессий (для панели истории) */
    @GetMapping("/sessions")
    @Transactional(readOnly = true)
    public Map<String, Object> listSessions(
            @RequestParam(value = "inn", required = false) String inn,
            @RequestParam(value = "operator", required = false) String operator) {
        Map<String, Object> response = new HashMap<>();
        List<VerificationSession> sessions;

        if (inn != null && !inn.isBlank()) {
            sessions = sessionRepo.findByInnOrderByCreatedAtDesc(inn.trim());
        } else if (operator != null && !operator.isBlank()) {
            sessions = sessionRepo.findByOperatorNameContainingIgnoreCaseOrderByCreatedAtDesc(operator.trim());
        } else {
            sessions = sessionRepo.findAllByOrderByCreatedAtDesc();
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (VerificationSession s : sessions) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("sessionId", s.getSessionId());
            m.put("inn", s.getInn());
            m.put("operatorName", s.getOperatorName());
            m.put("visitorName", s.getVisitorName());
            m.put("smsConfirmed", s.isSmsConfirmed());
            m.put("createdAt", s.getCreatedAt().format(TS_FORMATTER));
            m.put("documentsCount", s.getSignedDocuments().size());
            m.put("eventsCount", s.getEvents().size());
            list.add(m);
        }
        response.put("sessions", list);
        response.put("total", list.size());
        return response;
    }

    /** Поиск сессии по токену документа */
    @GetMapping("/sessions/by-document")
    @Transactional(readOnly = true)
    public Map<String, Object> findSessionByDocument(@RequestParam("token") String token) {
        Map<String, Object> response = new HashMap<>();
        Optional<SignedDocument> opt = documentRepo.findByToken(token);
        if (opt.isEmpty()) {
            response.put("found", false);
            response.put("error", "Документ с токеном " + token + " не найден");
            return response;
        }
        response.put("found", true);
        response.put("sessionId", opt.get().getSession().getSessionId());
        return response;
    }

    /** Полный отчёт по сессии (JSON для report.html) */
    @GetMapping("/report")
    @Transactional(readOnly = true)
    public Map<String, Object> sessionReport(@RequestParam("session") String sessionId) {
        return getSession(sessionId);
    }
}
