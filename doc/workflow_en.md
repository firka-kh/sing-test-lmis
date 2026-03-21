# Verification Process — Automated System (LMIS)

## Overview for Stakeholders (Business Process)
The system is designed to reliably identify visitors at Employment Centers in order to safely process benefit payments and grant access to personal unemployment records.

**Usage Scenario:**
1. The inspector logs into the system using the Ministry's Single Sign-On (SSO Keycloak).
2. A visitor arrives for an appointment. The inspector requests their passport and enters the 12-digit INN into the operator's system.
3. The system searches for the visitor in the database (search is available by Name or a unique System ID issued during the first visit).
4. An authentication request is pushed to the visitor's personal smartphone (or terminal): they must enter their secret 6-digit PIN and Date of Birth.
5. The data from the inspector's terminal and the visitor's device are synchronized and verified on the backend server.
6. If the data matches, the inspector is granted access to the visitor's full Profile Card. The Audit Log records the successful verification.
7. **Electronic Signature (New):** When a physical signature is required for a document (e.g., *Job Search Application* or *Allowance Order*), the operator clicks "Request Signature". The visitor's smartphone displays the document name and asks for their PIN code. Upon entering the PIN, the document is marked as legally signed, and the exact timestamp and document name are recorded in the Audit Log.

## Overview for Developers (Technical Workflow)

### Architecture
* **Frontend:** Single-page HTML/CSS/VanillaJS. It consists of two logical columns: the operator workspace (`col-op`) and the visitor's smartphone emulator (`col-ph`).
* **Backend:** Java Spring Boot REST API (`VerificationController.java`) which serves the static files and processes API requests.
* **SSO:** Planned integration with the existing local Keycloak instance (`192.168.3.10`).

### UI States
The interface state is managed by toggling CSS classes (`.sc.on` for operator screens and `.ps.on` for phone screens).
* `sc-login` -> `sc-search` -> `sc-step1` (INN Input) -> `sc-step2` (Data Verification) -> `sc-open` (Card Opened, Document Management).
* Phone: `ph-wait` -> `ph-pin` -> `ph-bd` -> `ph-result` -> `ph-sign` (Signing specific document).

### API Contracts (Mocks)
**1. Check INN**
* `POST /api/verification/check-inn`
* Payload: `{"inn": "500123456789"}`
* Response: `{"found": true, "message": "INN found"}`

**2. Verify Visitor Data**
* `POST /api/verification/verify-visitor`
* Payload: `{"inn": "...", "pin": "...", "birthDate": "..."}`
* Response: `{"verified": true, "fullName": "..."}`

**3. Sign Document (via PIN)**
* `POST /api/verification/sign-document`
* Payload: `{"inn": "...", "pin": "...", "documentName": "Job Search Application"}`
* Response: `{"signed": true, "timestamp": "2026-03-21T14:30:00Z"}`
