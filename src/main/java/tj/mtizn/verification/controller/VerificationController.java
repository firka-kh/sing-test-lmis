package tj.mtizn.verification.controller;

import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/verification")
public class VerificationController {

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
