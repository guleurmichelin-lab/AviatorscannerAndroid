# Aviator Scanner Android

Android Java project that lets a user choose a screenshot/image and uses Google ML Kit text recognition (OCR) to detect visible multiplier-like values such as `1.24x` or `2.50x`.

## Important limitation
This app only reads text visible in an image. It does not predict the next round, access Aviator's internal systems, or guarantee outcomes. OCR can misread small or blurry text, so verify the values.

## Build an installable APK
1. Install Android Studio and Android SDK Platform 35.
2. Open this folder in Android Studio.
3. Allow Gradle sync and dependency downloads.
4. Choose **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. Find the debug APK at `app/build/outputs/apk/debug/app-debug.apk`.
6. Transfer it to your Android phone and install it. You may need to allow installation from that file manager.

## Features
- Select a screenshot or image from the phone.
- On-device OCR using ML Kit.
- Extracts visible multiplier-like text ending in `x` or `х`.
- Shows recognized text and detected values in a session history.
- Dark interface with red accents.

## Requirements
Android Studio, JDK 17, Android SDK Platform 35, and internet access during the first Gradle sync.
