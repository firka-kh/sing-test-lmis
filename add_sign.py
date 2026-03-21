import os

paths = [
    "/home/karakadon/Документы/anti/verification-portal/src/main/resources/static/index.html",
    "/home/karakadon/Документы/anti/verification-portal/src/main/resources/static/index_en.html",
    "/home/karakadon/Документы/anti/verification-portal/index.html",
    "/home/karakadon/Документы/anti/verification-portal/index_en.html"
]

def add_sign_feature():
    for p in paths:
        if not os.path.exists(p): continue
        with open(p, "r", encoding="utf-8") as f:
            content = f.read()

        # Skip if already added
        if "docs-section" in content:
            print(f"Already applied to {p}")
            continue

        is_en = "index_en.html" in p

        # 1. Operator Section Injection
        t_el_sign = "Electronic Document Signing" if is_en else "Электронное подписание документов"
        t_doc_title = "Allowance Assignment Order #44-A" if is_en else "Приказ о назначении пособия №44-А"
        t_doc_wait = "Awaiting request to sign" if is_en else "Ожидает отправки на подпись"
        t_req_btn = "Request Signature →" if is_en else "Запросить подпись →"

        op_inject = f"""
                        <div class="docs-section" style="margin-top:20px;margin-bottom:20px;padding:16px;background:var(--gray);border-radius:6px;border:1px solid var(--border);">
                            <div style="font-weight:600;font-size:14px;margin-bottom:10px;">{t_el_sign}</div>
                            <div class="doc-item" style="display:flex;justify-content:space-between;align-items:center;background:#fff;padding:12px;border:1px solid var(--border);border-radius:4px;">
                                <div>
                                    <div style="font-weight:600;font-size:13px;">{t_doc_title}</div>
                                    <div style="font-size:11px;color:var(--text3);margin-top:4px;" id="doc-status">{t_doc_wait}</div>
                                </div>
                                <button class="btn btn-primary" id="btn-req-sign" onclick="reqSign()" style="padding:6px 12px;font-size:12px;">{t_req_btn}</button>
                            </div>
                        </div>
"""
        content = content.replace('<div class="audit">', op_inject + '                        <div class="audit">')

        # 2. Phone Section Injection
        t_ph_title = "Document Signing" if is_en else "Подписание документа"
        t_ph_desc = "To confirm document signing<br>please enter your <b>PIN-code</b>" if is_en else "Для подтверждения подписания<br>введите ваш <b>PIN-код</b>"

        ph_inject = f"""
                    <!-- SIGN -->
                    <div class="ps" id="ph-sign">
                        <div class="ph-hdr">
                            <div class="ph-hdr-t">{t_ph_title}</div>
                            <div class="ph-hdr-s">{t_doc_title}</div>
                        </div>
                        <div class="ph-body">
                            <div style="font-size:13px;text-align:center;margin-bottom:16px;color:var(--text2);line-height:1.4;">
                                {t_ph_desc}
                            </div>
                            <div class="pin-area">
                                <div class="pdots">
                                    <div class="pdot" id="spd1"></div><div class="pdot" id="spd2"></div><div class="pdot" id="spd3"></div>
                                    <div class="pdot" id="spd4"></div><div class="pdot" id="spd5"></div><div class="pdot" id="spd6"></div>
                                </div>
                            </div>
                            <div class="numpad">
                                <button class="nb" onclick="sp('1')">1</button><button class="nb" onclick="sp('2')">2</button><button class="nb" onclick="sp('3')">3</button>
                                <button class="nb" onclick="sp('4')">4</button><button class="nb" onclick="sp('5')">5</button><button class="nb" onclick="sp('6')">6</button>
                                <button class="nb" onclick="sp('7')">7</button><button class="nb" onclick="sp('8')">8</button><button class="nb" onclick="sp('9')">9</button>
                                <button class="nb del" onclick="sdel()">⌫</button><button class="nb" onclick="sp('0')">0</button>
                                <button class="nb okb" id="spin-ok" onclick="signDone()" disabled>OK ✓</button>
                            </div>
                        </div>
                    </div>
"""
        content = content.replace('                    <!-- RESULT -->', ph_inject + '                    <!-- RESULT -->')

        # 3. JS State Variables
        content = content.replace("let innVal = '', pinVal = '', bdVal = '';", "let innVal = '', pinVal = '', bdVal = '', spinVal = '';")

        # 4. JS Logic Injection
        t_await_sign = "Awaiting signature from visitor..." if is_en else "Ожидание подписи от посетителя..."
        t_doc_signed = "Document Signed" if is_en else "Документ подписан"
        t_doc_signed_sub = "You have successfully signed the document" if is_en else "Вы успешно подписали документ"
        t_op_signed = "✓ Signed electronically (PIN)" if is_en else "✓ Подписано электронной подписью (PIN)"
        t_audit_log = "Document \\\"Order #44-A\\\" signed (PIN code)" if is_en else "Документ \\\"Приказ №44-А\\\" подписан (PIN-код)"

        js_inject = f"""
        // ── SIGNATURE ──
        function reqSign() {{
            spinVal = '';
            updSDots();
            document.getElementById('spin-ok').disabled = true;
            document.getElementById('doc-status').textContent = '{t_await_sign}';
            document.getElementById('doc-status').style.color = 'var(--blue)';
            document.getElementById('btn-req-sign').style.display = 'none';
            switchPh('ph-sign');
        }}
        function sp(d) {{
            if (spinVal.length >= 6) return;
            spinVal += d; updSDots();
            if (spinVal.length === 6) document.getElementById('spin-ok').disabled = false;
            playBeep();
        }}
        function sdel() {{
            spinVal = spinVal.slice(0, -1);
            document.getElementById('spin-ok').disabled = true;
            updSDots();
        }}
        function updSDots() {{
            for (let i = 1; i <= 6; i++) {{
                const el = document.getElementById('spd' + i);
                if(el) el.className = 'pdot' + (i <= spinVal.length ? ' f' : '');
            }}
        }}
        function signDone() {{
            if (spinVal.length < 6) return;
            playSuccess();
            switchPh('ph-result');
            document.getElementById('pr-ring').className = 'pr-ring ok-r'; document.getElementById('pr-ring').textContent = '✓';
            document.getElementById('pr-title').textContent = '{t_doc_signed}';
            document.getElementById('pr-sub').textContent = '{t_doc_signed_sub}';
            document.getElementById('perm-box').style.display = 'none';
            
            document.getElementById('doc-status').textContent = '{t_op_signed}';
            document.getElementById('doc-status').style.color = 'var(--green)';
            
            const auditDiv = document.querySelector('.audit');
            const newLog = document.createElement('div');
            newLog.className = 'audit-row signlog';
            const now = new Date();
            newLog.innerHTML = '<div class="adot g"></div><div class="atxt">{t_audit_log}</div><div class="atm">' + fmt(now) + '</div>';
            
            const title = auditDiv.querySelector('.audit-title');
            title.insertAdjacentElement('afterend', newLog);
        }}
"""
        content = content.replace("        // ── SOUNDS ──", js_inject + "\n        // ── SOUNDS ──")

        # 5. JS Reset Injection
        reset_inject = f"""
            spinVal = '';
            document.getElementById('btn-req-sign').style.display = 'block';
            document.getElementById('doc-status').textContent = '{t_doc_wait}';
            document.getElementById('doc-status').style.color = 'var(--text3)';
            document.querySelectorAll('.signlog').forEach(e => e.remove());
"""
        content = content.replace("bdVal = ''; document.getElementById('bd-ok').disabled = true;", "bdVal = ''; document.getElementById('bd-ok').disabled = true;" + reset_inject)

        with open(p, "w", encoding="utf-8") as f:
            f.write(content)
            
    print("Signature feature added.")

if __name__ == "__main__":
    add_sign_feature()
