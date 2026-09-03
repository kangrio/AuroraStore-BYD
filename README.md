# Aurora Store (BYD Variant)

A custom build of Aurora Store tailored for BYD Android-based head units.

---
![Stars](https://img.shields.io/github/stars/kangrio/AuroraStore-BYD) ![Forks](https://img.shields.io/github/forks/kangrio/AuroraStore-BYD) ![Downloads](https://img.shields.io/github/downloads/kangrio/AuroraStore-BYD/total) ![Release](https://img.shields.io/github/v/release/kangrio/AuroraStore-BYD)

![Open Issues](https://img.shields.io/github/issues-raw/kangrio/AuroraStore-BYD) ![Closed Issues](https://img.shields.io/github/issues-closed-raw/kangrio/AuroraStore-BYD) ![License](https://img.shields.io/github/license/kangrio/AuroraStore-BYD) ![Last Commit](https://img.shields.io/github/last-commit/kangrio/AuroraStore-BYD)

## 🚀 How to Get Started

Follow these steps carefully to set up Aurora Store on your BYD head unit:

1. **Uninstall MicroG** — If MicroG is already installed on your device, uninstall it first before proceeding.
2. **Install this app** — Install Aurora Store (BYD Variant) on your BYD head unit.
3. **Open the app & follow onboarding** — Launch the app and complete the onboarding setup (login with Google or anonymous account).
4. **Install your first app** — Search and download any app from the store.
5. **Install the MicroG bundle when prompted** — A popup will appear asking you to install the MicroG bundle. **You must install it**, otherwise no downloaded apps will be able to open.
   - Once MicroG is installed, **tap "Install"** to proceed with installing the app you just downloaded.
6. **Allow autostart for MicroG** — After your app is installed, go to your device's autostart settings and **enable autostart for both**:
   - `MicroG Service`
   - `MicroG Companion`
7. **Open your app** — Launch the app you just installed and enjoy it on your BYD head unit.
8. **Enjoy your drive! 🚗**

> [!IMPORTANT]
> Skipping step 5 (MicroG bundle installation) or step 6 (autostart permissions) will prevent apps from launching correctly.

---

## ✨ Features

* ✅ Automatic detection of MicroG
* 📦 Prompt to install MicroG if not available
* 🔧 Automatic patching of apps to support MicroG after download
* 📲 Enables apps that normally require Google Mobile Services (GMS)

---

## ✅ Tested Apps (Working)

* Google Maps
* Google Search
* Google Gemini
* Google News
* Gmail
* YouTube
* YouTube Music
* etc

✔ Basic functionality works
✔ Login may work depending on MicroG setup

---

## ❌ Tested Apps (Not Working Properly)

* Netflix

#### ⚠ Issue:
* App crash

### Possible Reasons

* Play Integrity / DRM enforcement
* Streaming protection checks
* Missing or limited Widevine support

---

## ⚠️ Compatibility Warning

> Some apps may NOT work even after patching.

Reasons include:

* Strong Play Integrity / SafetyNet checks
* Hardcoded Google Play Services dependencies
* Native (NDK) verification
* DRM / streaming restrictions

👉 You must test apps yourself. No guarantee of compatibility.

---

## 🐛 Crash Reporting

Every patched app includes optional crash reporting. **Crash reporting can be enabled or disabled from the settings.**

When crash reporting is enabled and a patched app crashes, crash information is automatically sent to the crash-reporting server and added to the existing GitHub issue for that app. This helps identify and troubleshoot crashes across different BYD devices and Android versions.

Crash reporting was added in [this commit](https://github.com/kangrio/AuroraStore-BYD/commit/1dbfe47c).

The following information is included in each report:

* **Android** — Android release version
* **Build fingerprint** — device/build fingerprint
* **Package** — application package name
* **Version code** — installed app version code
* **Version name** — installed app version name
* **Report** — crash stack trace

Example:

```json
{
  "android": "12",
  "buildfingerprint": "BYD AUTO/BYD-AUTO/BYD-AUTO:12/V417IR/320:user/release-keys",
  "package": "com.example.app",
  "versioncode": 123,
  "versionname": "1.2.3",
  "report": "java.lang.RuntimeException: ..."
}
```

Crash reports are used to help debug patched apps and improve compatibility with BYD Android head units. Users can disable crash reporting at any time from the app settings.

---

## 🚗 Tested Environment

* BYD Android Head Unit (DiLink system)

---

## 🛠 Known Limitations

* DRM-protected apps may fail
* Streaming apps may stop playback
* Push notifications may not work reliably
* Google login may fail in some apps
* Some patched apps may crash

---

## 🔐 Disclaimer

This project is for educational and personal use only.

* You are responsible for usage
* Do not violate app terms of service
* No warranty provided

---

## 📌 Notes

* Keep MicroG updated
* Reinstall apps if patching fails
* Clear app data if issues occur

---

## 🚧 Future Improvements

* WIP

---

## 🌟 Awesome Apps for BYD Head Units

A curated list of apps and tools that work great on BYD Android head units (DiLink).

### 🎙️ AI & Voice Assistants

| App                 | Description                                                                                                                                                                 | Link                                                      |
|---------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------|
| **Assistant (BYD)** | AI voice assistant launcher with offline wake-word detection ("Hey Rio", "Alexa"), one-tap launch for Google Assistant / ChatGPT / Claude, and BYD DiLink onboarding wizard | [kangrio/Assistant](https://github.com/kangrio/Assistant) |
| **Google / Gemini** | Google's AI assistant and multimodal AI — search, navigation, smart home control                                                                                            | Available via Aurora Store                                |
| **ChatGPT**         | OpenAI's conversational AI with voice mode                                                                                                                                  | Available via Aurora Store                                |

### 🗺️ Navigation

| App             | Description                                                                       | Link                       |
|-----------------|-----------------------------------------------------------------------------------|----------------------------|
| **Google Maps** | Full-featured navigation and maps                                                 | Available via Aurora Store |
| **Waze**        | Community-based traffic and navigation                                            | Available via Aurora Store |
| **Radarbot**    | Speed camera & radar detector — supported version **8.8.4 (build 189) and below** | Available via Aurora Store |

### 🎵 Media & Entertainment

| App               | Description                 | Link                       |
|-------------------|-----------------------------|----------------------------|
| **YouTube**       | Video streaming             | Available via Aurora Store |
| **YouTube Music** | Music streaming from Google | Available via Aurora Store |

### 📧 Productivity

| App             | Description            | Link                       |
|-----------------|------------------------|----------------------------|
| **Gmail**       | Google email client    | Available via Aurora Store |
| **Google News** | Personalized news feed | Available via Aurora Store |

> [!TIP]
> Use **[Assistant (BYD)](https://github.com/kangrio/Assistant)** to quickly launch any of these AI apps with a custom wake word — no screen tapping needed while driving.

---

### 🙏 Credits

- [Aurora Store (official)](https://github.com/whyorean/AuroraStore) An unofficial FOSS client to Google Play.
- [ARSCLib](https://github.com/REAndroid/ARSCLib) Android binary resources read/write library
- [AndroidHiddenApiBypass](https://github.com/LSPosed/AndroidHiddenApiBypass) LSPass: Bypass restrictions on non-SDK interfaces

---
