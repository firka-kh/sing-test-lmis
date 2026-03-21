import os
paths = [
    "/home/karakadon/Документы/anti/verification-portal/src/main/resources/static/index_en.html",
    "/home/karakadon/Документы/anti/verification-portal/index_en.html"
]

for p in paths:
    if os.path.exists(p):
        with open(p, "r", encoding="utf-8") as f: c = f.read()
        c = c.replace("'Take the visitor's passport and enter their INN'", '"Take the visitor\'s passport and enter their INN"')
        with open(p, "w", encoding="utf-8") as f: f.write(c)

print("JS Fixed")
