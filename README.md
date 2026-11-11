# UoPeople App

A lightweight Android WebView wrapper for the **University of the People** student portal and **Brightspace (D2L)** learning environment.

Instead of forcing every link into Chrome, the app keeps the entire academic experience — portal login, announcements, courses, quizzes, discussions — inside a single native shell.

## Features

- **Portal login start page** — opens directly on `https://portal.uopeople.edu/login`
- **In-app navigation** — all links stay inside the WebView, including Brightspace (`*.brightspace.com`, `*.d2l.com`) and UoPeople (`*.uopeople.edu`) domains
- **Popup support** — `target="_blank"` and `window.open()` links are captured and loaded in the same WebView instead of being dropped
- **Graceful external fallback** — only if no app can handle a URL is it handed off, with a fallback to loading in-app
- **File uploads** — `<input type="file">` is supported (assignments, profile pictures)
- **Native bridge** — small `UopNative` JavaScript interface for future native integrations
- **Modern dark-mode aware theme** with branded splash colors

## Tech Stack

| Component | Version |
|-----------|---------|
| Language | Kotlin 1.9.24 |
| Build | Gradle 8.9, AGP 8.5.2 |
| SDK | compileSdk / targetSdk 34, minSdk 26 |
| UI | ViewBinding, Material Components |
| Architecture | Single-Activity WebView host, `AndroidViewModel` application class |

## Project Structure

```
app/src/main/java/edu/uopeople/app/
├── MainActivity.kt                  # WebView host, back navigation, file chooser
├── UopApplication.kt                # Application + ViewModel bootstrap
└── web/
    ├── DomainRules.kt               # Internal-domain suffix matching
    ├── BrightspaceWebViewClient.kt  # URL interception, popup capture
    ├── BrightspaceChromeClient.kt   # File chooser / UI chrome
    └── NativeJsBridge.kt            # @JavascriptInterface bridge
```

## Building

Requirements: JDK 17+, Android SDK with platform 34 and build-tools 34.0.0.

```bash
./gradlew assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

### Release build

1. Create a keystore and `keystore.properties` in the project root:

```properties
storeFile=uopeople-release.keystore
storePassword=********
keyAlias=uopeople
keyPassword=********
```

2. Build:

```bash
./gradlew assembleRelease
```

`keystore.properties`, `*.keystore`, `*.jks` and `*.apk` are git-ignored — **never commit signing material**.

## Install

```bash
adb install app-release.apk
```

Or copy either APK to the device and open it from a file manager (allow "Install unknown apps" for that source).

## Notes

- The app targets UoPeople's real hosts: `portal.uopeople.edu` (entry point) and `learn.uopeople.edu` (Brightspace, CNAME to `uopeople.brightspace.com`).
- Internal-domain rules live in `DomainRules.kt` if additional hosts ever need to be kept in-app.
- JavaScript, DOM storage and file access are enabled; no tracking or data collection is performed by the wrapper itself.

## License

Released under the [MIT License](LICENSE).

This project is an unofficial wrapper and is not affiliated with or endorsed by University of the People.
