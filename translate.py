import os

replacements = {
    # Header
    "Автоматизированная система верификации личности посетителей": "Automated Identity Verification System",
    "Министерство труда и занятости населения Республики Таджикистан": "Ministry of Labor, Migration and Employment of the Republic of Tajikistan",
    "Верификация личности": "Identity Verification",
    "Реестр посетителей": "Visitor Registry",
    "Выплаты пособий": "Benefit Payments",
    "Журнал аудита": "Audit Log",
    "Демо-режим": "Demo Mode",
    
    # Login & Operator
    "Рабочее место инспектора": "Inspector Workspace",
    "Единая система входа (SSO)": "Single Sign-On (SSO)",
    "Центр занятости населения г. Душанбе": "Employment Center (Dushanbe)",
    "Табельный номер": "Employee ID",
    "Пароль": "Password",
    "Единый вход для всех 4 порталов Министерства": "Single login for all 4 Ministry portals",
    "Войти в систему →": "Login →",
    "Портал регистрации безработных": "Unemployment Registration Portal",
    
    # Search
    "Инспектор I категории •": "Category I Inspector •",
    "✓ SSO активен": "✓ SSO Active",
    "Поиск по ФИО или номеру паспорта": "Search by Name or Passport Number",
    "Введите ФИО или серию/номер паспорта...": "Enter name or passport series/number...",
    "Найти": "Search",
    "💡 Нажмите «Найти» для открытия карточки посетителя": "💡 Click «Search» to open the visitor's card",
    
    # Step 1
    "Карточка посетителя — Заблокировано": "Visitor Profile — Locked",
    "Данные скрыты до завершения верификации": "Data is hidden until verification is complete",
    "Шаг 1 из 2": "Step 1 of 2",
    "Ввод ИНН оператором": "Operator enters INN",
    "с паспорта посетителя": "from visitor's passport",
    "Сверка и подтверждение": "Verification and Confirmation",
    "после ввода данных посетителем": "after visitor inputs data",
    "Шаг 1 — Введите ИНН с паспорта посетителя": "Step 1 — Enter INN from visitor's passport",
    "Демо-ИНН для проверки системы": "Demo-INN for system test",
    "Введите 12 цифр ИНН": "Enter 12-digit INN",
    "Возьмите паспорт у посетителя и введите его ИНН": "Take the visitor's passport and enter their INN",
    "PIN-код": "PIN code",
    "Дата рождения (ввод посетителя)": "Date of birth (visitor input)",
    "Дата рождения (система)": "Date of birth (system)",
    "Дата рождения": "Date of Birth",
    "Ожидание ввода...": "Waiting for input...",
    "Статус верификации": "Verification Status",
    "Прогресс верификации": "Verification Progress",
    "Перейти к подтверждению →": "Proceed to confirmation →",
    "Кнопка станет активной после ввода всех данных": "Button will activate after all data is entered",
    "Все данные получены — нажмите «Перейти к подтверждению»": "All data received — click «Proceed to confirmation»",
    "Введите корректный ИНН (12 цифр)": "Enter a valid INN (12 digits)",
    "Ожидание PIN-кода от посетителя...": "Waiting for PIN from visitor...",
    "Ожидание даты рождения от посетителя...": "Waiting for DOB from visitor...",
    
    # Step 2
    "Сверка данных с паспортом": "Verify data with passport",
    "ИНН подтверждён": "INN confirmed",
    "Сверьте с паспортом и подтвердите": "Verify with passport and confirm",
    "Верификация не пройдена — доступ запрещён": "Verification failed — access denied",
    "Проверьте совпадение данных на экране с данными в паспорте посетителя:": "Check if the data on the screen matches the visitor's passport:",
    "Номер паспорта": "Passport Number",
    "ФИО": "Full Name",
    "ИНН (введён оператором)": "INN (entered by operator)",
    "Совпадает с паспортом?": "Matches passport?",
    "✓ Все данные совпадают — Подтвердить": "✓ All data matches — Confirm",
    "✕ Не совпадает": "✕ Does not match",
    "→ Открыть карточку посетителя": "→ Open visitor profile",
    
    # Card
    "✓ Верифицирован": "✓ Verified",
    "Личность подтверждена — карточка открыта": "Identity confirmed — profile opened",
    "Телефон": "Phone",
    "Специальность": "Profession",
    "Инженер-строитель": "Civil Engineer",
    "Пособие": "Allowance",
    "320 сомони / мес": "320 somoni / month",
    "Причина увольнения": "Reason for dismissal",
    "Сокращение штата": "Staff reduction",
    "Следующая явка": "Next appointment",
    "Журнал верификации": "Verification Log",
    "Личность подтверждена: ИНН + PIN-код + Дата рождения": "Identity confirmed: INN + PIN + DOB",
    "Оператор:": "Operator:",
    "Карточка открыта • Уровень доступа: полный": "Profile opened • Access level: full",
    "← Новый посетитель": "← New visitor",
    
    # Phone
    "Телефон посетителя": "Visitor Smartphone",
    "Ожидание": "Waiting",
    "Оператор вводит<br>ваш ИНН с паспорта<br><br>Подождите...": "Operator is entering<br>your INN from passport<br><br>Please wait...",
    "Введите PIN-код": "Enter PIN Code",
    "Шаг 2 из 2": "Step 2 of 2",
    "Любые 8 цифр": "Any 8 digits",
    "6-значный PIN-код": "6-digit PIN",
    "ДД . ММ . ГГГГ": "DD . MM . YYYY",
    "Обработка...": "Processing...",
    "Ожидайте решения оператора": "Awaiting operator decision...",
    "Данные отправлены": "Data submitted",
    "Ожидайте решения оператора...": "Awaiting operator decision...",
    
    # JS specific replacements (exact matching)
    "✓ ИНН найден в системе —": "✓ INN found in system —",
    "из 12 цифр": "of 12 digits",
    "Введено": "Entered",
    "ИНН не найден в системе": "INN not found in system",
    "✓ Введён посетителем": "✓ Entered by visitor",
    "✓ Введена посетителем": "✓ Entered by visitor",
    "✓ Введён": "✓ Entered",
    "✓ Найден в базе данных": "✓ Found in database",
    "✕ Не найден": "✕ Not found",
    "✓ Совпадает": "✓ Matches",
    "ИНН не найден в системе — проверьте правильность ввода": "INN not found in system — check input",
    "✕ Отказано": "✕ Denied",
    "Доступ запрещён": "Access Denied",
    "Данные не совпали. Обратитесь к оператору.": "Data mismatch. Contact operator.",
    "Аутентификация пройдена": "Authentication successful",
    "Вы прошли аутентификацию": "You pass authentication",
    "✓ Подтверждено": "✓ Confirmed",
    "Ошибка от сервера:": "Server error:",
    "Данные не совпали": "Data mismatch",
    "Оператор вручную отклонил запрос — данные в паспорте не совпадают с данными на экране": "Operator manually rejected the request — data mismatch",
    "Отказано оператором": "Denied by operator",
    "Обратитесь к оператору за разъяснениями.": "Contact operator for clarification.",
    "Верифицировано": "Verified",
    "// РАЗРЕШЕНИЕ ВЫДАНО //": "// PERMISSION GRANTED //",
    "✓ Личность подтверждена": "✓ Identity confirmed",
    "✓ ИНН верифицирован": "✓ INN verified",
    "✓ PIN-код принят": "✓ PIN accepted",
    "✓ Дата рождения принята": "✓ DOB accepted",
    "Вы дали оператору": "You gave the operator",
    "разрешение на обработку": "permission to process",
    "вашей заявки.": "your request.",
    "Сценарий верификации": "Verification Scenario",
    "Вход оператора (SSO)": "Operator Login (SSO)",
    "Поиск карточки посетителя": "Search visitor profile",
    "Оператор вводит ИНН с паспорта": "Operator inputs INN from passport",
    "Посетитель: PIN-код + дата рождения": "Visitor: PIN + DOB",
    "Сверка данных → карточка открыта": "Data verification → Profile open",
}

paths = [
    "/home/karakadon/Документы/anti/verification-portal/src/main/resources/static/index.html", 
    "/home/karakadon/Документы/anti/verification-portal/index.html"
]

for path in paths:
    if not os.path.exists(path):
        continue
        
    en_path = path.replace("index.html", "index_en.html")
    
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()
    
    # Make EN content
    content_en = content
    for k, v in replacements.items():
        content_en = content_en.replace(k, v)
        
    switcher_en = '<div style="position:absolute;top:20px;right:30px;z-index:999;"><a href="index.html" style="color:var(--text3);font-size:14px;text-decoration:none;font-weight:bold;margin-right:15px;text-transform:uppercase;">RU</a> <span style="color:#fff;font-size:14px;font-weight:bold;text-transform:uppercase;">EN</span></div>'
    if switcher_en not in content_en:
        content_en = content_en.replace('<body>', '<body>\n' + switcher_en)
        
    with open(en_path, "w", encoding="utf-8") as f:
        f.write(content_en)
        
    # Inject switcher to RU content
    switcher_ru = '<div style="position:absolute;top:20px;right:30px;z-index:999;"><span style="color:#fff;font-size:14px;font-weight:bold;text-transform:uppercase;margin-right:15px;">RU</span> <a href="index_en.html" style="color:var(--text3);font-size:14px;text-decoration:none;font-weight:bold;text-transform:uppercase;">EN</a></div>'
    if switcher_ru not in content:
        content_ru = content.replace('<body>', '<body>\n' + switcher_ru)
        with open(path, "w", encoding="utf-8") as f:
            f.write(content_ru)

print("Translation applied successfully!")
