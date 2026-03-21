import os
import re

paths = [
    "/home/karakadon/Документы/anti/verification-portal/src/main/resources/static/index.html",
    "/home/karakadon/Документы/anti/verification-portal/src/main/resources/static/index_en.html",
    "/home/karakadon/Документы/anti/verification-portal/index.html",
    "/home/karakadon/Документы/anti/verification-portal/index_en.html"
]

def upgrade():
    for p in paths:
        if not os.path.exists(p): continue
        with open(p, "r", encoding="utf-8") as f:
            content = f.read()

        is_en = "index_en.html" in p

        s_form_title = "Job Search Application" if is_en else "Заявление: Поиск подходящей работы"
        s_form_pos = "Desired Position" if is_en else "Желаемая должность"
        s_form_pos_v = "Civil Engineer, Architect" if is_en else "Инженер-строитель, Архитектор"
        s_form_sal = "Minimum Salary" if is_en else "Минимальная зарплата"
        s_form_sal_v = "from 3,500 somoni" if is_en else "от 3 500 сомони"
        s_form_edu = "Ready for Retraining" if is_en else "Согласие на переобучение"
        s_form_edu_v = "Yes" if is_en else "Да"
        s_form_loc = "Work Location" if is_en else "Локация работы"
        s_form_loc_v = "Dushanbe, Rudaki district" if is_en else "г. Душанбе, р-н Рудаки"
        s_req_sign = "Request Signature →" if is_en else "Запросить подпись →"
        s_doc_wait = "Awaiting request to sign" if is_en else "Ожидает отправки на подпись"

        form_block = f"""
                        <div class="docs-section" style="margin-top:0px;margin-bottom:16px;padding:16px;background:rgba(26, 86, 160, .05);border-radius:6px;border:1px solid #93C5FD;">
                            <div style="font-weight:600;font-size:14px;margin-bottom:12px;color:var(--blue);">📄 {s_form_title}</div>
                            <div style="background:#fff;border:1px solid var(--border);border-radius:4px;padding:12px;margin-bottom:12px;font-size:12px;">
                                <div style="display:flex;margin-bottom:8px;"><div style="width:160px;color:var(--text3);">{s_form_pos}:</div><div style="font-weight:600;">{s_form_pos_v}</div></div>
                                <div style="display:flex;margin-bottom:8px;"><div style="width:160px;color:var(--text3);">{s_form_sal}:</div><div style="font-weight:600;">{s_form_sal_v}</div></div>
                                <div style="display:flex;margin-bottom:8px;"><div style="width:160px;color:var(--text3);">{s_form_edu}:</div><div style="font-weight:600;">{s_form_edu_v}</div></div>
                                <div style="display:flex;"><div style="width:160px;color:var(--text3);">{s_form_loc}:</div><div style="font-weight:600;">{s_form_loc_v}</div></div>
                            </div>
                            <div class="doc-item" style="display:flex;justify-content:space-between;align-items:center;">
                                <div style="font-size:11px;color:var(--text3);" id="doc-status-2">{s_doc_wait}</div>
                                <button class="btn btn-primary" id="btn-req-sign-2" onclick="reqSign('{s_form_title}', 'btn-req-sign-2', 'doc-status-2')" style="padding:6px 12px;font-size:12px;">{s_req_sign}</button>
                            </div>
                        </div>
"""
        
        if s_form_title not in content:
            content = content.replace('<div class="docs-section"', form_block + '                        <div class="docs-section"', 1)

        if 'currentSignDocName' not in content:
            s_fn_new = """
        let currentSignDocName = '';
        let currentSignBtn = '';
        let currentSignStatus = '';
        function reqSign(docName, btnId, statusId) {
            currentSignDocName = docName;
            currentSignBtn = btnId;
            currentSignStatus = statusId;
            
            const phHdrS = document.querySelector('#ph-sign .ph-hdr-s');
            if (phHdrS) phHdrS.textContent = docName;
            
            spinVal = '';
            updSDots();
            document.getElementById('spin-ok').disabled = true;
            
            const isEn = document.querySelector('.ph-hdr-t') && document.querySelector('.ph-hdr-t').textContent.includes('Sign');
            document.getElementById(statusId).textContent = isEn ? 'Awaiting signature from visitor...' : 'Ожидание подписи от посетителя...';
            document.getElementById(statusId).style.color = 'var(--blue)';
            document.getElementById(btnId).style.display = 'none';
            switchPh('ph-sign');
        }"""
            
            content = re.sub(r'function reqSign\(\) \{.*?(?=function sp\()', s_fn_new + '\n        ', content, flags=re.DOTALL)
            
            t_order = "Allowance Assignment Order #44-A" if is_en else "Приказ о назначении пособия №44-А"
            content = content.replace('onclick="reqSign()"', f'onclick="reqSign(\'{t_order}\', \'btn-req-sign\', \'doc-status\')"')

            content = re.sub(r"document\.getElementById\('pr-sub'\)\.textContent = '.*?';", "document.getElementById('pr-sub').innerHTML = (document.querySelector('.ph-result').textContent.includes('Document Signed') ? 'You signed document:<br><br><b>' : 'Вы подписали документ:<br><br><b>') + currentSignDocName + '</b>';", content)
        
            content = re.sub(r"document\.getElementById\('doc-status'\)\.textContent = '.*?';", "document.getElementById(currentSignStatus).textContent = document.querySelector('.pr-title').textContent.includes('Document Signed') ? '✓ Signed electronically (PIN)' : '✓ Подписано электронной подписью (PIN)';", content)
            content = re.sub(r"document\.getElementById\('doc-status'\)\.style\.color = 'var\(--green\)';", "document.getElementById(currentSignStatus).style.color = 'var(--green)';", content)
            
            content = re.sub(r"newLog\.innerHTML = '<div class=\"adot g\"\></div><div class=\"atxt\"\>.*?</div><div class=\"atm\"\>' \+ fmt\(now\) \+ '\</div>';", "newLog.innerHTML = '<div class=\"adot g\"></div><div class=\"atxt\">' + (document.querySelector('.pr-title').textContent.includes('Document Signed') ? 'Document signed: ' : 'Документ подписан: ') + currentSignDocName + ' (PIN)</div><div class=\"atm\">' + fmt(now) + '</div>';", content)

            reset_new = """
            spinVal = '';
            const isEn = document.querySelector('.pr-title') && document.querySelector('.pr-title').textContent.includes('Document Signed');
            if(document.getElementById('btn-req-sign')) document.getElementById('btn-req-sign').style.display = 'block';
            if(document.getElementById('doc-status')) {
                document.getElementById('doc-status').textContent = isEn ? 'Awaiting request to sign' : 'Ожидает отправки на подпись';
                document.getElementById('doc-status').style.color = 'var(--text3)';
            }
            if(document.getElementById('btn-req-sign-2')) document.getElementById('btn-req-sign-2').style.display = 'block';
            if(document.getElementById('doc-status-2')) {
                document.getElementById('doc-status-2').textContent = isEn ? 'Awaiting request to sign' : 'Ожидает отправки на подпись';
                document.getElementById('doc-status-2').style.color = 'var(--text3)';
            }
            document.querySelectorAll('.signlog').forEach(e => e.remove());"""
            
            content = re.sub(r"spinVal = '';\s*document\.getElementById\('btn-req-sign'\)\.style\.display = 'block';\s*document\.getElementById\('doc-status'\).textContent = .*?;\s*document\.getElementById\('doc-status'\)\.style\.color = 'var\(--text3\)';\s*document\.querySelectorAll\('\.signlog'\)\.forEach\(e => e\.remove\(\)\);", reset_new, content)

        with open(p, "w", encoding="utf-8") as f:
            f.write(content)
            
    print("Done")

if __name__ == "__main__":
    upgrade()
