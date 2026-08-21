# DigitalKhata

DigitalKhata is a premium, offline Android Khata app for tracking your finances, payments, and receivables. It keeps a clear payment history for every person, features smart payment reminders, and instantly syncs your overall budget dynamically.

All data stays on the device. The app does not require an account, internet connection, or cloud service to track payments.

## Features

- **Smart Reminders:** Create reminders for upcoming payments or receivables, which automatically trigger Android notifications and can be seamlessly converted to real transactions.
- **Image Attachments:** Attach bills and receipts to your transactions via Gallery or Camera and view them securely within the app.
- **Dynamic Budgeting:** Your dashboard's Khata total dynamically updates with every payment made and amount received.
- **Premium UI:** A stunning golden and deep black Fintech user interface built with Material 3.
- **Offline First:** Fully local storage with Room database for maximum privacy and security.

## Screens

- **Home:** Interactive summary cards, budget overview, recent transactions, and pending reminders.
- **Khata:** Complete searchable ledger of all people and their total balances.
- **Person Details:** Transaction history for an individual with the ability to view attached bills.
- **Reminders:** Dedicated hub for viewing Pending, Today, and Upcoming payment reminders.
- **Settings:** Configure your total budget baseline, app preferences, and view App Information.

## Tech stack

- Kotlin
- Jetpack Compose with Material 3
- Android Gradle Plugin 9.0.1
- Gradle 9.1.0 (project wrapper)
- Room database (v4) with KSP
- Coil for asynchronous image loading
- Navigation Compose
- compileSdk 35 / targetSdk 35
- Minimum Android version: Android 6.0 (API 23)

## Requirements

To build on Linux, including Kali Linux, you need the following already installed:

- A compatible JDK. This project is configured for Java 25 with Gradle 9.1.0.
- Android SDK at `$HOME/android/sdk`, or `ANDROID_HOME` / `ANDROID_SDK_ROOT` set to your SDK location.
- Android SDK Platform 35: `$HOME/android/sdk/platforms/android-35`
- Android Build Tools 35.0.0: `$HOME/android/sdk/build-tools/35.0.0`
- Android platform-tools: `$HOME/android/sdk/platform-tools`

The project uses its own Gradle wrapper; a globally installed `gradle` command is not required.

## Build on Linux / Kali Linux

From the project folder:

```bash
chmod +x build.sh
./build.sh
```

`build.sh` detects Java, configures `ANDROID_HOME` and `ANDROID_SDK_ROOT` when needed, validates the required SDK components, and builds the debug APK through `./gradlew`.

If your SDK is in a different location, set it for the current terminal before building:

```bash
export ANDROID_SDK_ROOT="/path/to/android/sdk"
export ANDROID_HOME="$ANDROID_SDK_ROOT"
./build.sh
```

## Generate the debug APK

The normal command is:

```bash
./build.sh
```

The generated APK is located at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Project structure

```text
.
├── app/
│   ├── src/main/java/com/ben/khata/
│   │   ├── MainActivity.kt          # Compose UI, Navigation, and Features
│   │   ├── RemindersUI.kt           # Reminder-specific UI screens
│   │   ├── KhataViewModel.kt        # State Holder
│   │   ├── ReminderUtils.kt         # AlarmManager Notification integration
│   │   └── data/KhataDatabase.kt    # Room database, DAO, and singletons
│   ├── src/main/res/                # App resources and logo
│   └── build.gradle.kts             # App module configuration
├── gradle/wrapper/                  # Gradle wrapper configuration
├── build.gradle.kts                 # Root build configuration
├── settings.gradle.kts              # Project settings
└── build.sh                         # Linux build script
```
