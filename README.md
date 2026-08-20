# Aurora Store (BYD Variant)

A custom build of Aurora Store tailored for BYD Android-based head units.

---

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

### 🙏 Credits

Original project:
- [Aurora OSS / Aurora Store developers](https://github.com/whyorean/AuroraStore)

---
