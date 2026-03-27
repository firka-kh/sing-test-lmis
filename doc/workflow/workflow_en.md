# Interaction Workflow — EN

Documentation of the identity verification process on the portal when a visitor visits the Employment Center (EC).

## UML Sequence Diagram (TIN-Authorization)

```mermaid
sequenceDiagram
    autonumber
    participant O as Operator (Inspector)
    participant S as Verification Portal
    participant P as Visitor (Smartphone)
    participant B as Backend / API

    Note over O, P: Process Initialization
    O->>S: Enter Visitor's TIN (from passport)
    S->>O: Status: "Waiting for PIN on device"
    
    S->>P: Display "Enter PIN Code" (6 digits)
    P->>S: Visitor enters PIN (Demo: 123456)
    
    S->>P: Display "Enter Birth Date"
    P->>S: Visitor enters date (Demo: 15.03.1988)
    
    S->>B: Verification request: TIN + PIN + Birth Date
    B-->>S: Result (Data matches / Error)

    alt SCENARIO: "YES" (Success)
        S->>O: Green Indicator: "Verified"
        S->>O: Automatically open visitor card (Name, spec, allowance)
        O->>S: Request signature on document (Order/Application)
        S->>P: "Sign document on screen"
        P->>S: Clicking "Sign"
        S->>O: Document status: "Signed"
    else SCENARIO: "NO" (Failure)
        S->>O: Red status: "Access Denied"
        S->>O: Card remains locked (data hidden)
        S->>O: Message: "Data does not match"
    end
```

## Key Entities
- **TIN (ИНН)**: Primary identifier for DB lookup.
- **PIN Code**: Temporary access key (6 digits).
- **Visitor Card**: Object with full PII (available ONLY after verification).
