# PLAN.md: UoPeople Native Android WebView Wrapper

## 1. Project Goal & Overview
Build a lightweight, highly responsive native Android app to wrap the University of the People (UoPeople) Brightspace Learning Management System (`learning.uopeople.edu`). 

### Key Pain Points to Solve:
1. Prevent Brightspace links, peer reviews, and forums from opening outside the app into external Chrome tabs.
2. Maintain persistent SSO session cookies to avoid constant re-authentication.
3. Inject custom JavaScript & CSS to hide web header clutter and transform the desktop layout into a mobile-friendly view.
4. Provide a native local Room database for student notes and assignment reminders.

---

## 2. Technical Stack & Dependencies
- **Language:** Kotlin
- **Minimum SDK:** 26 (Android 8.0) | **Target SDK:** 34 (Android 14)
- **Core Components:** `AndroidX WebKit`, `Jetpack Compose` (or `XML Layouts`), `Room DB`, `Coroutines`
- **Permissions Required:** `INTERNET`, `ACCESS_NETWORK_STATE`

---

## 3. Implementation Roadmap & Phases

### Phase 1: Project Setup & Manifest Configuration
- [x] Initialize Android Studio project with `Empty App Activity`.
- [x] Add `android.permission.INTERNET` to `AndroidManifest.xml`.
- [x] Enable `usesCleartextTraffic="true"` (if needed for institutional test subdomains).
- [x] Configure `CookieManager` in `Application` class.

### Phase 2: WebEngine & Security Interception (Crucial)
- [x] Configure `WebViewSettings`:
  - `javaScriptEnabled = true`
  - `domStorageEnabled = true`
  - `databaseEnabled = true`
  - `allowFileAccess = true`
- [x] Implement custom `WebViewClient`:
  - Override `shouldOverrideUrlLoading`:
    - Intercept `uopeople.edu`, `brightspace.com`, and `d2l` URLs to keep them **inside** the app.
    - Route non-institutional external links to standard external browser.
  - Implement `onPageFinished`:
    - Inject custom CSS to remove redundant web headers/navbars.
    - Inject JS bridging helpers for native notifications.
- [x] Implement `WebChromeClient`:
  - Support file uploads for assignment submissions (`onShowFileChooser`).
  - Add loading progress bar updates.

### Phase 3: Cookie & SSO Persistence
- [x] Enable `CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)`.
- [x] Call `CookieManager.getInstance().flush()` on pause and page finish events to prevent login expiry.

### Phase 4: Native Android Toolbar & Floating Actions
- [ ] Add Top App Bar with `Back`, `Forward`, `Refresh`, and `Home (Dashboard)` buttons.
- [ ] Add Floating Action Button (FAB) for native quick-note overlay.

### Phase 5: Offline Notes Feature (Room DB)
- [ ] Setup `NoteEntity` (`id`, `title`, `content`, `timestamp`, `courseTag`).
- [ ] Create `NoteDao` and `AppDatabase`.
- [ ] Implement a bottom sheet modal to read/write notes while viewing coursework.

---

## 4. Specific Security & Domain Filtering Logic

```kotlin
val ALLOWED_DOMAINS = listOf(
    "uopeople.edu",
    "learning.uopeople.edu",
    "brightspace.com",
    "d2l.com",
    "my.uopeople.edu"
)
```

If a user clicks a link:
- **Matches ALLOWED_DOMAINS:** Load inside `WebView`.
- **External domain (e.g., external PDF host, Wikipedia):** Open in external system browser (`Intent.ACTION_VIEW`).
