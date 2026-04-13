package tj.mtizn.verification.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/verification")
public class VerificationController {

    private static final String DEMO_SMS_CODE = "123456";
    private static final String DEMO_PIN_CODE = "123456";
    private static final DateTimeFormatter TS_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final Map<String, SessionRecord> sessionsById = new ConcurrentHashMap<>();
    private final Map<String, SignedDocumentRecord> documentsByToken = new ConcurrentHashMap<>();

    private static class SessionRecord {
        String sessionId;
        String inn;
        String operatorName;
        String visitorName;
        boolean smsConfirmed;
        String createdAt;
        String confirmedAt;
        String createdIp;
        String createdUserAgent;
        String lastActivityAt;
        List<String> signedTokens = new ArrayList<>();
        List<Map<String, String>> events = new ArrayList<>();
    }

    private static class SignedDocumentRecord {
        String token;
        String sessionId;
        String documentName;
        String documentVersion;
        String operatorName;
        String visitorName;
        String status;
        String signedAt;
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            String first = xff.split(",")[0].trim();
            if (!first.isEmpty()) {
                return first;
            }
        }
        String remoteAddr = request.getRemoteAddr();
        return (remoteAddr == null || remoteAddr.isBlank()) ? "unknown" : remoteAddr;
    }

    private void addSessionEvent(SessionRecord session, String type, String description, HttpServletRequest request) {
        String nowTs = LocalDateTime.now().format(TS_FORMATTER);
        Map<String, String> event = new HashMap<>();
        event.put("type", type);
        event.put("description", description);
        event.put("timestamp", nowTs);
        event.put("ip", resolveClientIp(request));
        event.put("userAgent", request != null ? request.getHeader("User-Agent") : "unknown");
        session.events.add(event);
        session.lastActivityAt = nowTs;
    }

    private long durationSeconds(String startedAt, String finishedAt) {
        if (startedAt == null || finishedAt == null) {
            return 0L;
        }
        LocalDateTime start = LocalDateTime.parse(startedAt, TS_FORMATTER);
        LocalDateTime finish = LocalDateTime.parse(finishedAt, TS_FORMATTER);
        return java.time.Duration.between(start, finish).getSeconds();
    }

    /**
     * Пример эндпоинта для проверки ИНН.
     * Реальная реализация должна проверять ИНН в базе данных.
     */
    @PostMapping("/check-inn")
    public Map<String, Object> checkInn(@RequestBody Map<String, String> payload) {
        String inn = payload.get("inn");
        Map<String, Object> response = new HashMap<>();
        
        // Мок: Для демо любой 12-значный ИНН считается валидным
        if (inn != null && inn.length() == 12) {
            response.put("found", true);
            response.put("message", "ИНН найден в системе");
        } else {
            response.put("found", false);
            response.put("message", "Введен некорректный ИНН");
        }
        
        return response;
    }

    /**
     * Создание сессии после ввода ИНН. В демо режиме код SMS всегда 123456.
     */
    @PostMapping("/create-session")
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

        SessionRecord session = new SessionRecord();
        session.sessionId = "SES-" + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        session.inn = inn;
        session.operatorName = operatorName;
        session.visitorName = visitorName;
        session.smsConfirmed = false;
        session.createdAt = LocalDateTime.now().format(TS_FORMATTER);
        session.createdIp = resolveClientIp(request);
        session.createdUserAgent = request != null ? request.getHeader("User-Agent") : "unknown";
        session.lastActivityAt = session.createdAt;
        addSessionEvent(session, "SESSION_CREATED", "Сессия создана оператором " + operatorName + " для посетителя " + visitorName, request);
        addSessionEvent(session, "SMS_SENT", "Одноразовый SMS-код отправлен на номер посетителя", request);

        sessionsById.put(session.sessionId, session);

        response.put("created", true);
        response.put("sessionId", session.sessionId);
        response.put("smsSent", true);
        response.put("maskedPhone", "+992 93 *** ** 78");
        response.put("demoSmsCode", DEMO_SMS_CODE);
        response.put("createdAt", session.createdAt);
        response.put("ip", session.createdIp);
        response.put("userAgent", session.createdUserAgent);
        return response;
    }

    /**
     * Подтверждение сессии кодом из SMS.
     */
    @PostMapping("/confirm-session")
    public Map<String, Object> confirmSession(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String sessionId = payload.get("sessionId");
        String smsCode = payload.get("smsCode");
        Map<String, Object> response = new HashMap<>();

        SessionRecord session = sessionsById.get(sessionId);
        if (session == null) {
            response.put("confirmed", false);
            response.put("error", "Сессия не найдена");
            return response;
        }

        if (smsCode == null || smsCode.length() != 6 || !DEMO_SMS_CODE.equals(smsCode)) {
            response.put("confirmed", false);
            response.put("error", "Неверный SMS-код");
            return response;
        }

        session.smsConfirmed = true;
        session.confirmedAt = LocalDateTime.now().format(TS_FORMATTER);
        addSessionEvent(session, "SMS_CONFIRMED", "Посетитель подтвердил сессию по SMS-коду", request);

        response.put("confirmed", true);
        response.put("sessionId", session.sessionId);
        response.put("confirmedAt", session.confirmedAt);
        return response;
    }

    /**
     * Подписание документа в рамках уже подтвержденной сессии.
     */
    @PostMapping("/sign-document")
    public Map<String, Object> signDocument(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String sessionId = payload.get("sessionId");
        String pin = payload.get("pin");
        String documentName = payload.getOrDefault("documentName", "Документ");
        String documentVersion = payload.getOrDefault("documentVersion", "v1.0");
        String operatorName = payload.getOrDefault("operatorName", "Не указан");
        String visitorName = payload.getOrDefault("visitorName", "Не указан");

        Map<String, Object> response = new HashMap<>();
        SessionRecord session = sessionsById.get(sessionId);
        if (session == null) {
            response.put("signed", false);
            response.put("error", "Сессия не найдена");
            return response;
        }
        if (!session.smsConfirmed) {
            response.put("signed", false);
            response.put("error", "Сессия не подтверждена по SMS");
            return response;
        }
        if (pin == null || pin.length() != 6 || !DEMO_PIN_CODE.equals(pin)) {
            response.put("signed", false);
            response.put("error", "Неверный PIN-код");
            return response;
        }

        SignedDocumentRecord doc = new SignedDocumentRecord();
        doc.token = "DOC-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        doc.sessionId = session.sessionId;
        doc.documentName = documentName;
        doc.documentVersion = documentVersion;
        doc.operatorName = operatorName;
        doc.visitorName = visitorName;
        doc.status = "Подписано и принято в обработку";
        doc.signedAt = LocalDateTime.now().format(TS_FORMATTER);

        documentsByToken.put(doc.token, doc);
        session.signedTokens.add(doc.token);
        addSessionEvent(session, "DOCUMENT_SIGNED", "Подписан документ: " + documentName + " (" + documentVersion + "), токен " + doc.token, request);

        response.put("signed", true);
        response.put("token", doc.token);
        response.put("status", doc.status);
        response.put("signedAt", doc.signedAt);
        response.put("checkUrl", "/check.html?token=" + doc.token);
        return response;
    }

    /**
     * Полная история сессии: кто открыл, кто подтвердил, какие документы подписаны.
     */
    @GetMapping("/session/{sessionId}")
    public Map<String, Object> getSession(@PathVariable("sessionId") String sessionId) {
        Map<String, Object> response = new HashMap<>();
        SessionRecord session = sessionsById.get(sessionId);
        if (session == null) {
            response.put("found", false);
            response.put("error", "Сессия не найдена");
            return response;
        }

        List<Map<String, String>> signedDocuments = new ArrayList<>();
        for (String token : session.signedTokens) {
            SignedDocumentRecord doc = documentsByToken.get(token);
            if (doc == null) {
                continue;
            }
            Map<String, String> docMap = new HashMap<>();
            docMap.put("token", doc.token);
            docMap.put("documentName", doc.documentName);
            docMap.put("documentVersion", doc.documentVersion);
            docMap.put("status", doc.status);
            docMap.put("signedAt", doc.signedAt);
            signedDocuments.add(docMap);
        }

        response.put("found", true);
        response.put("sessionId", session.sessionId);
        response.put("inn", session.inn);
        response.put("operatorName", session.operatorName);
        response.put("visitorName", session.visitorName);
        response.put("smsConfirmed", session.smsConfirmed);
        response.put("createdAt", session.createdAt);
        response.put("confirmedAt", session.confirmedAt);
        response.put("createdIp", session.createdIp);
        response.put("createdUserAgent", session.createdUserAgent);
        response.put("lastActivityAt", session.lastActivityAt);
        response.put("durationSeconds", durationSeconds(session.createdAt, LocalDateTime.now().format(TS_FORMATTER)));
        response.put("events", session.events);
        response.put("signedDocuments", signedDocuments);
        return response;
    }

    /**
     * Проверка статуса документа по токену (для check страницы).
     */
    @GetMapping("/check")
    public Map<String, Object> checkDocument(@RequestParam("token") String token) {
        Map<String, Object> response = new HashMap<>();
        SignedDocumentRecord doc = documentsByToken.get(token);
        if (doc == null) {
            response.put("found", false);
            response.put("error", "Документ не найден");
            return response;
        }

        response.put("found", true);
        response.put("token", doc.token);
        response.put("sessionId", doc.sessionId);
        response.put("documentName", doc.documentName);
        response.put("documentVersion", doc.documentVersion);
        response.put("operatorName", doc.operatorName);
        response.put("visitorName", doc.visitorName);
        response.put("status", doc.status);
        response.put("signedAt", doc.signedAt);
        return response;
    }

    /**
     * Пример эндпоинта для сверки PIN-кода и Даты Рождения.
     */
    @PostMapping("/verify-visitor")
    public Map<String, Object> verifyVisitor(@RequestBody Map<String, String> payload) {
        String pin = payload.get("pin");
        String birthDate = payload.get("birthDate");
        String inn = payload.get("inn");
        
        Map<String, Object> response = new HashMap<>();
        
        System.out.println("VERIFY: ИНН=" + inn + " PIN=" + pin + " Date=" + birthDate);
        
        // Мок проверка: для демо пропускаем, если введены любые данные нужной длины
        if (pin != null && pin.length() == 6 && inn != null && inn.length() == 12) {
            response.put("verified", true);
            response.put("fullName", "Рахимов Алишер Бахтиёрович");
        } else {
            response.put("verified", false);
            response.put("error", "Вы ввели ИНН: " + inn + ", ПИН: " + pin);
        }
        
        return response;
    }
}
