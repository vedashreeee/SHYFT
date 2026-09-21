# TECHNICAL AUDIT & ARCHITECTURAL VERIFICATION REPORT

**Product**: SHYFT — Context-Aware Local Privacy Engine for Android  
**Version**: 1.0.0 (Phase 4 Final Release)  
**Date**: September 20, 2026  
**Architecture**: 100% On-Device, Zero Cloud / Zero Remote Data Transmission  

---

## EXECUTIVE SUMMARY

SHYFT is an on-device privacy engine for Android designed to adapt sensitive UI elements, audio behavior, and application protection levels based on real-time environmental context. All camera frames, microphone audio buffers, and motion telemetry are processed strictly in local RAM and discarded immediately. No data is stored or transmitted.

---

## 1. FULLY FUNCTIONAL FEATURES (NATIVE HARDWARE & API INTEGRATIONS)

The following core modules are fully implemented using native Android APIs and run on real hardware / emulator devices:

### 1.1 Visual Privacy Engine
* **CameraX Front Camera Feed**: Utilizes `ProcessCameraProvider` bound strictly to `CameraSelector.LENS_FACING_FRONT` and `ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST`.
* **On-Demand Permission Handling**: `CAMERA` permission is requested **only** when Visual Privacy mode is explicitly toggled ON by the user.
* **ML Kit Real Face Detection**: Integrated `FaceDetection.getClient()` with `PERFORMANCE_MODE_FAST` and `CLASSIFICATION_MODE_NONE`.
* **Primary vs Secondary Face Heuristic**:
  - `0 faces` $\rightarrow$ `NO_PRIVACY_RISK`
  - `1 face` $\rightarrow$ Primary user $\rightarrow$ `NO_PRIVACY_RISK`
  - `$\ge 2$ faces` $\rightarrow$ Largest bounding box (`width * height`) identified as primary user; remaining faces evaluated as secondary viewers.
* **Head Orientation Attention Approximation**: Secondary viewers evaluated using Euler angles (`headEulerAngleY` yaw within $[-35^\circ, +35^\circ]$ and `headEulerAngleX` pitch within $[-25^\circ, +25^\circ]$).
* **3 Public Privacy States**: `NO_PRIVACY_RISK`, `PRIVACY_CHECKING`, and `PRIVACY_RISK`.
* **Hysteresis & Stability Timing**:
  - `1200 ms` risk activation stability delay before entering `PRIVACY_RISK`.
  - `400 ms` restoration debounce delay before returning to `NO_PRIVACY_RISK`.
* **One-Shot Haptic Notification**: `Vibrator` / `VibrationEffect.createOneShot(100ms)` triggered exactly once on `PRIVACY_RISK` transition.

### 1.2 Audio Privacy Engine
* **AudioRecord Ambient Sensing**: Initializes `AudioRecord` at `16000 Hz` mono 16-bit PCM.
* **On-Demand Permission Handling**: `RECORD_AUDIO` permission requested **only** when Audio Privacy mode is enabled.
* **Short-Term RMS Sound Level**: Computes Root Mean Square amplitude over $20\,\text{ms}$ buffers ($320$ samples).
* **Immediate Zeroing**: Buffer array explicitly zeroed (`fill(0)`) immediately after RMS computation. No audio recording, storage, or transmission.
* **Exponential Moving Average (EMA) Smoothing**: Smooths RMS with smoothing factor $\alpha = 0.15$.
* **Relative Ambient Level Classification**:
  - `QUIET`: RMS $< 400$
  - `NORMAL`: $400 \le \text{RMS} \le 2000$
  - `NOISY`: RMS $> 2000$
* **Adaptive Volume Recommendation**: Maps relative ambient level smoothly to recommended levels ($30\%$ for `QUIET`, $50\%$ for `NORMAL`, $70\%$ for `NOISY`).

### 1.3 Sensor Intelligence Engine
* **SensorManager Registration**: Listens to `TYPE_ACCELEROMETER`, `TYPE_GYROSCOPE`, and `TYPE_PROXIMITY`.
* **Movement State Calculation**: Evaluates magnitude $\sqrt{a_x^2 + a_y^2 + a_z^2}$; classifies as `STATIONARY`, `WALKING`, or `IN_VEHICLE`.
* **Device Orientation State**: Determines `FACE_UP`, `FACE_DOWN`, `PORTRAIT`, or `LANDSCAPE`.
* **Proximity Telemetry**: Classifies proximity distance as `NEAR_EAR` ($< 1\,\text{cm}$) or `AWAY_FROM_EAR`. Telemetry strictly used for context; does **not** perform audio output routing switching.

### 1.4 Protected Applications Engine
* **DataStore Persistence**: App protection preferences persisted via `androidx.datastore.preferences.core`.
* **User Choice Model**: Enforces requirement: *"SHYFT only activates privacy monitoring for apps you choose."*

---

## 2. CONTROLLED DEMO FEATURES (SANDBOX ENVIRONMENT)

The following features demonstrate SHYFT privacy capabilities within the controlled application sandbox:

* **Payment Demo Screen**: Selective masking of sensitive fields (`Amount`, `UPI ID`, `Account Number`, `OTP`) using `SensitiveContent` blur/mask composable when `riskState == PRIVACY_RISK`. Non-sensitive fields (`Recipient Name`, `Status`) remain unmasked. Instant restoration when risk subsides.
* **Media Demo Screen**: Simulates adaptive media player volume adjustments dynamically responding to `RelativeAmbientLevel` recommendations with manual override controls.
* **Call Privacy Demo Screen**: Demonstrates incoming call caller details masking during `PRIVACY_RISK` and proximity telemetry integration.
* **Demo Sandbox Controls**: Simulation triggers for `SHOULDER_SURFER_DETECTED` and `ENTERED_PUBLIC_ZONE` scenarios with reset options.

---

## 3. ANDROID PLATFORM LIMITATIONS & ARCHITECTURAL BOUNDARIES

To remain compliant with Android platform security model and Google Play Store policies, the following boundaries apply:

1. **Third-Party Application Window Overlay & Masking**: Android OS security prevents non-system applications from inspecting or modifying the UI windows of arbitrary third-party applications (e.g., WhatsApp, Google Pay, Banking Apps) without using Accessibility Services or System Overlay permissions. SHYFT implements selective content masking inside the controlled application sandbox and provides explicit UI messaging regarding user-managed Protected Apps.
2. **Global Third-Party App Volume Overrides**: Android `AudioManager` allows apps to adjust system stream volume (e.g., `STREAM_MUSIC`), but does not support per-app volume overrides for third-party media streams without system/root privileges. SHYFT adaptive volume operates as a relative recommendation model integrated into the media player framework.
3. **Proximity Audio Routing Constraints**: Audio routing switching between speakerphone and earpiece during active phone calls is governed strictly by `InCallService` and system audio manager call states. SHYFT uses proximity data strictly as supporting telemetry without forcing system earpiece routing.

---

## 4. VERIFICATION RESULTS SUMMARY

| Engine / Component | Automated Tests | Emulator Smoke Test | Verification Status |
| :--- | :---: | :---: | :---: |
| **Visual Privacy Engine** | `VisualPrivacyEngineTest` (PASS) | Verified on `emulator-5554` | **100% PASS** |
| **Audio Privacy Engine** | `AudioPrivacyEngineTest` (PASS) | Verified on `emulator-5554` | **100% PASS** |
| **Protected Apps DataStore** | `ProtectedAppsTest` (PASS) | Verified on `emulator-5554` | **100% PASS** |
| **Sensor Intelligence** | Built-in Unit Specs (PASS) | Verified on `emulator-5554` | **100% PASS** |
| **Payment Demo Masking** | Composition Unit Specs (PASS) | Verified on `emulator-5554` | **100% PASS** |
| **Guided Demo Mode** | Flow Integration (PASS) | Verified on `emulator-5554` | **100% PASS** |

---

**Conclusion**: SHYFT Phase 4 Final Integration is complete, fully verified, and ready for presentation.
