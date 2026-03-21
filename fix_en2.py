import os
paths = [
    "/home/karakadon/Документы/anti/verification-portal/src/main/resources/static/index_en.html",
    "/home/karakadon/Документы/anti/verification-portal/index_en.html"
]
reps = {
    'class="tnav-av">АС': 'class="tnav-av">SA',
    'ИНН: 500123456789': 'INN: 500123456789',
    'Демо:': 'Demo:',
    "PIN code принят": "PIN code accepted",
    "Date of Birth принята": "Date of Birth accepted"
}
for p in paths:
    if os.path.exists(p):
        with open(p, "r", encoding="utf-8") as f: c = f.read()
        for k, v in reps.items(): c = c.replace(k, v)
        with open(p, "w", encoding="utf-8") as f: f.write(c)
print("Done")
