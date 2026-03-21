import os

paths = [
    "/home/karakadon/Документы/anti/verification-portal/src/main/resources/static/index.html",
    "/home/karakadon/Документы/anti/verification-portal/src/main/resources/static/index_en.html",
    "/home/karakadon/Документы/anti/verification-portal/index.html",
    "/home/karakadon/Документы/anti/verification-portal/index_en.html"
]

for p in paths:
    if os.path.exists(p):
        with open(p, "r", encoding="utf-8") as f:
            c = f.read()

        c = c.replace("Поиск по ФИО или номеру паспорта", "Поиск по ФИО или системному ID")
        c = c.replace("Введите ФИО или серию/номер паспорта...", "Введите ФИО или выданный системный ID...")
        c = c.replace("Search by Name or Passport Number", "Search by Name or System ID")
        c = c.replace("Enter name or passport series/number...", "Enter name or issued System ID...")

        with open(p, "w", encoding="utf-8") as f:
            f.write(c)

print("Search placeholders updated in all files.")
