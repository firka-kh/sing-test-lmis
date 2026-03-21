import os
import re

paths = [
    "/home/karakadon/Документы/anti/verification-portal/src/main/resources/static/index_en.html",
    "/home/karakadon/Документы/anti/verification-portal/index_en.html"
]

reps = {
    "Портал верификации личности — МТиЗН РТ": "Identity Verification Portal - MLME RT",
    r"Портал\s*регистрации безработных": "Unemployment Registration Portal",
    r"Единый вход для\s*всех 4 порталов Министерства": "Single login for all 4 Ministry portals",
    "Войти в систему": "Login",
    "ЦЗН г. Душанбе": "Dushanbe Employment Center",
    "ЦЗН Душанбе": "Dushanbe EC",
    r"💡 Нажмите «Search» для открытия карточки\s*посетителя": "💡 Click «Search» to open the visitor's profile",
    r"с паспорта\s*посетителя": "from visitor's passport",
    r"после ввода данных\s*посетителем": "after visitor inputs data",
    "Шаг 1 — Введите ИНН from visitor's passport": "Step 1 — Enter INN from visitor's passport",
    r"Перейти к\s*подтверждению →": "Proceed to confirmation →",
    r"Сверьте с\s*паспортом и подтвердите": "Verify with passport and confirm",
    "Введён посетителем": "Entered by visitor",
    r"✓ Все данные\s*совпадают — Подтвердить": "✓ All data match — Confirm",
    r"→ Открыть карточку\s*посетителя": "→ Open visitor profile",
    "Паспорт:": "Passport:",
    "лет": "years old",
    "ИНН + PIN code + Date of Birth совпали": "INN + PIN code + Date of Birth matched",
    "Личность подтверждена: ИНН + PIN code + Date of Birth": "Identity confirmed: INN + PIN + DOB",
    "Phone посетителя": "Visitor Smartphone",
    "Введите PIN code": "Enter PIN code",
    "6-значный PIN code": "6-digit PIN code",
    "Посетитель: PIN code + дата рождения": "Visitor: PIN code + Date of birth",
    "'Waiting PIN codeа от посетителя...'": "'Waiting for PIN code from visitor...'",
    "'Waiting PIN code от посетителя...'": "'Waiting for PIN code from visitor...'",
    "Waiting PIN codeа от посетителя": "Waiting for PIN code from visitor",
    "'Верификация завершена'": "'Verification Complete'",
    "проверьте правильность ввода": "check input correctness",
    "Рахимов Алишер Бахтиёрович": "Alisher Bakhtiyorovich Rakhimov",
    "Азимов Санджар Рустамович": "Sanjar Rustamovich Azimov",
    "Оператор:": "Operator:",
    "А 4521876": "A 4521876",
    "Operator:": "Operator:",
    "ИНН не найден в системе —": "INN not found in system —"
}

for p in paths:
    if not os.path.exists(p): continue
    with open(p, "r", encoding="utf-8") as f:
        c = f.read()
    
    for k, v in reps.items():
        if r"\s*" in k:
            c = re.sub(k, v, c)
        else:
            c = c.replace(k, v)
            
    with open(p, "w", encoding="utf-8") as f:
        f.write(c)
        
print("Fixed EN translation")
