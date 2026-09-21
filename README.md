# SHYFT

### Privacy That Moves With You.

SHYFT is a local-first Android privacy application that adapts to the user's surroundings using the phone's camera, microphone, sensors, and on-device AI.

Instead of hiding the entire screen or requiring manual action every time, SHYFT detects privacy-risk situations and responds selectively.

## What SHYFT Does

### 👁️ Visual Privacy
- Uses the front camera with CameraX.
- Uses on-device Google ML Kit face detection.
- Estimates whether another person is oriented toward the phone.
- Uses a privacy state machine with:
  - `NO_PRIVACY_RISK`
  - `PRIVACY_CHECKING`
  - `PRIVACY_RISK`
- Applies selective masking to sensitive information such as:
  - Payment amounts
  - UPI IDs
  - Account numbers
  - OTPs
- Non-sensitive information remains visible.
- Camera frames are processed locally and are not stored or uploaded.

### 🔊 Audio Privacy
- Uses the microphone only as an ambient sound sensor.
- Does not record, transcribe, store, or upload conversations.
- Classifies the surrounding environment as:
  - QUIET
  - NORMAL
  - NOISY
- Uses EMA smoothing for stable readings.
- Generates adaptive volume recommendations of 30%, 50%, or 70%.

### 📞 Call Privacy
When privacy risk is active, the controlled call demo can selectively mask sensitive caller information and adjust speaker volume.

### 📱 Sensor Intelligence
SHYFT uses:
- Accelerometer
- Gyroscope
- Proximity sensor

These provide additional context such as movement, phone orientation, and proximity.

## Technology Stack

- **Kotlin**
- **Jetpack Compose**
- **Material 3**
- **CameraX**
- **Google ML Kit Face Detection**
- **Android AudioRecord**
- **Android Sensor APIs**
- **DataStore**
- **Gradle**
- **On-device/local processing**

## Privacy by Design

SHYFT follows a local-first approach.

- Camera processing happens on-device.
- Audio is processed in memory.
- Audio buffers are cleared after processing.
- No camera frames are saved or transmitted.
- No microphone recordings are uploaded.
- Permissions are requested only when the related privacy feature is activated.
- Protected-app selections are stored locally using DataStore.

## Demo

The project includes controlled demos for:

1. Protected Apps
2. Visual Privacy
3. Payment Privacy
4. Media / Audio Privacy
5. Call Privacy
6. Sensor Intelligence

The payment demo demonstrates selective masking: sensitive fields such as the payment amount, UPI ID, account number, and OTP are hidden when a privacy risk is detected, while non-sensitive information remains visible.

## Project Structure

```text
SHYFT/
├── app/
│   └── src/
│       ├── main/
│       └── test/
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
└── TECHNICAL_AUDIT.md
```

## Building the Project

Open the project in Android Studio and allow Gradle to sync.

Then build the debug APK with:

```bash
./gradlew assembleDebug
```

On Windows:

```bat
gradlew.bat assembleDebug
```

## Testing

Run the unit tests with:

```bash
./gradlew test
```

On Windows:

```bat
gradlew.bat test
```

## Important Scope Note

SHYFT's core privacy logic is demonstrated through controlled in-app screens. Android security and lifecycle restrictions can limit an ordinary application from directly modifying arbitrary third-party applications or system UI.

The project therefore focuses on demonstrating the privacy engine, sensing, decision logic, selective masking, and adaptive audio behavior reliably and transparently.

## Why SHYFT?

Phones are used everywhere — for payments, messages, calls, entertainment, and personal information. Privacy risks can change within seconds depending on who is nearby and what is being displayed or played.

SHYFT makes privacy contextual.

**It doesn't hide your phone. It adapts your phone to protect your privacy.**
