# Aurora Store (BYD Variant)

A custom build of Aurora Store tailored for BYD Android-based head units.

## ✨ Features

* ✅ Automatic detection of MicroG
* 📦 Prompt to install MicroG if not available
* 🔧 Automatic patching of apps to support MicroG after download
* 📲 Enables apps that normally require Google Mobile Services (GMS)
* 🚗 Android Automotive (AAOS) spoofing support (allows installing apps like Google Maps Automotive version)
    - Toggle in Settings → restart the app to apply

---

## ⚙️ How It Works

1. Open Aurora Store (BYD version)
2. Search and download any app
3. After download:

    * App is automatically patched (if required)
    * MicroG compatibility is injected
4. Install the patched APK

If MicroG is not installed:

* You will be prompted to install it first

---

## 📥 MicroG Requirement

* MicroG must be installed and configured
* Signature spoofing may be required depending on your system

---

## ✅ Tested Apps (Working)

* Google Maps
* Google Search
* Google Gemini
* Google News
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