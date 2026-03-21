import os, re
paths = [
    "/home/karakadon/Документы/anti/verification-portal/src/main/resources/static/index.html",
    "/home/karakadon/Документы/anti/verification-portal/src/main/resources/static/index_en.html",
    "/home/karakadon/Документы/anti/verification-portal/index.html",
    "/home/karakadon/Документы/anti/verification-portal/index_en.html"
]
for p in paths:
    if not os.path.exists(p): continue
    with open(p, "r", encoding="utf-8") as f: content = f.read()
    content = re.sub(r'<body>\n<div style="position:absolute;top:20px.*?</div>', '<body>', content)
    is_en = "index_en.html" in p
    new_sw = '<div style="display:flex;align-items:center;margin-right:24px;font-size:15px;letter-spacing:1px;"><a href="index.html" style="color:var(--text3);text-decoration:none;">RU</a> <span style="color:rgba(255,255,255,0.2);margin:0 10px;">|</span> <span style="color:#fff;font-weight:700;">EN</span></div>' if is_en else '<div style="display:flex;align-items:center;margin-right:24px;font-size:15px;letter-spacing:1px;"><span style="color:#fff;font-weight:700;">RU</span> <span style="color:rgba(255,255,255,0.2);margin:0 10px;">|</span> <a href="index_en.html" style="color:var(--text3);text-decoration:none;">EN</a></div>'
    if new_sw not in content:
        content = content.replace('<div class="hdr-clock" id="clock">—</div>', new_sw + '\n                <div class="hdr-clock" id="clock">—</div>')
    with open(p, "w", encoding="utf-8") as f: f.write(content)
