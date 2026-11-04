# Enthes 🦴

**Enthes** is a free, open-source, local-first mobile application designed specifically for people living with **psoriatic arthritis (PsA)** and spondyloarthritis.

Its primary goal is to remove friction from daily morning symptom tracking and transform raw logs into an **actionable, condensed clinical summary** for rheumatology consultations—keeping patient health data 100% private.

---

## 🌟 Core Principles

* **Zero-Backend / Absolute Privacy:** Health data never leaves your device. No remote servers, no telemetry, no analytics tracking, and no data profiling.
* **No Accounts or Sign-ups:** Install and start tracking immediately. No email, passwords, or personal identity required.
* **On-Device Biometric Lock:** Secure your sensitive health logs with your device's native fingerprint, face recognition, or system PIN/passcode. Verification is handled entirely on-device by the secure hardware chip (TEE / Secure Enclave)—no biometric data is ever captured, stored, or sent across any network.
* **Built for Stiff, Painful Hands:** Ergonomic UI featuring oversized touch targets designed specifically for morning stiffness. A complete daily check-in takes under 15 seconds without typing on a keyboard.
* **Clinically Relevant Markers:** Tracks the exact clinical indicators that matter in psoriatic arthritis management:
  * Morning stiffness duration (*in clinically relevant ranges*).
  * Peripheral joint involvement and axial/spine discomfort.
  * Enthesitis (Achilles tendon, plantar fascia) and dactylitis ("sausage digit") flare indicators.
  * Systemic inflammatory fatigue / asthenia impact.
  * Rescue medication usage (NSAIDs/analgesics).
* **Rheumatologist-Ready Reports:** Generates a structured local summary (PDF export) displaying symptom trends and flare history to support clinical decision-making during appointments.
* **True Free & Open Source:** Licensed under the GNU General Public License v3.0 (GPLv3).

---

## 🔒 Security & On-Device Authentication

Because health and symptom records are deeply personal, Enthes includes an optional local biometric lock:

* **Hardware-Backed Protection:** Integrates with native platform security frameworks (`BiometricPrompt` on Android, `LocalAuthentication` on iOS).
* **Device Credential Fallback:** If morning stiffness, swelling, or psoriasis skin plaques interfere with fingerprint recognition, the app seamlessly falls back to your standard device lock PIN, pattern, or password.
* **Multitask Privacy:** When active, the app masks its interface in the recent apps switcher to keep records private from shoulder surfers.

---

## 🛠️ Tech Stack

* **Language:** Kotlin
* **Cross-Platform UI:** Compose Multiplatform (Android / iOS)
* **Local Persistence:** Type-safe SQLite (SQLDelight / Room KMP)
* **Local Security:** Native Biometrics / Device Credential APIs (TEE / Secure Enclave)
* **Architecture:** Clean Architecture / Domain-Driven Vertical Slices (TDD-focused)
* **License:** GNU General Public License v3.0

---

## 📦 Backups & Data Portability

By design, Enthes does not rely on third-party cloud synchronization. The app includes a built-in import/export utility to backup data in an open format (`.json` / `.sqlite`) via the operating system's native file picker (Storage Access Framework on Android / Files on iOS). You retain full ownership and custody of your backup files.

---

## 📄 License & Rights

Copyright (C) 2026 Luis García Villalonga.

This program is free software: you can redistribute it and/or modify it under the terms of the **GNU General Public License (GPLv3)** as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. See the [LICENSE](LICENSE) file for complete details.

### 1. Dual Licensing (Patient Associations, Foundations & Clinics)
As the original author and sole copyright holder, this project operates under a **Dual Licensing** model:
* **Community / Open Source:** 100% free under the strict terms of the copyleft GNU GPLv3 license.
* **Commercial / Tailored Integrations:** If you represent a patient association, medical foundation, clinic, or healthcare provider seeking custom feature development (dedicated modules, branded exports, clinical workflows) or distribution without reciprocal GPLv3 obligations, contact me to negotiate a commercial license and professional services agreement.

### 2. Trademark & Visual Assets
The GPLv3 license applies solely to the source code. The project name **Enthes**, the official logo, and brand assets remain the exclusive intellectual property of the author and may not be reused in modified forks distributed across commercial application stores without explicit prior written authorization.
