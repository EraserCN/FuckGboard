# FuckGboard (PixelBoard LSPosed)

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Target: Gboard](https://img.shields.io/badge/Target-Gboard-green.svg)](https://play.google.com/store/apps/details?id=com.google.android.inputmethod.latin)
[![Framework: LSPosed](https://img.shields.io/badge/Xposed-LSPosed-orange.svg)](https://github.com/LSPosed/LSPosed)

**FuckGboard** is an LSPosed / Xposed module that unlocks Google Pixel-exclusive features on Gboard for rooted Android devices, including Gemini Rambler Voice Dictation, AI Writing Tools, and Material 3 Expressive styling.

Unlike traditional APK patchers (ReVanced / Apktool), this module dynamically hooks Gboard in-memory at runtime. You keep your official Gboard installation from Google Play, receive regular app updates without breaking signatures, and toggle features on the fly.

---

## 🌟 Key Features

### 🎙️ Gemini Rambler Voice Typing
- **Next Generation Assistant (NGA) Dictation**: Fast voice-to-text with auto-punctuation, sentence restructuring, and capitalization.
- **Agentic Dictation Bar**: Pixel's gradient wave glow animation and floating assistant dictation UI.
- **Dual Mic Button Interception**: Replaces standard Google Voice Typing with Rambler agentic dictation.

### ✍️ AI Writing Tools & Proofreading (Cloud-Enabled)
- **Writing Tools V2 Floating Prompt Bar**: Inline text rephrasing, tone stylization, and pre-defined prompts.
- **Multilingual Support**: Bypasses the English-only restriction for AI Writing Helper.
- **Cloud Backend Inference**: Bypasses on-device Gemini Nano/AICore hardware keymaster checks. Perfect for **bootloader unlocked (BL-unlocked) devices** or non-Pixel phones where on-device models fail attestation, preventing the dreaded *"Couldn't load suggestions"* error.

### 🎨 Pure Performance & Optional UI Tweaks
- **Dedicated Non-Functional UI Toggle**: Includes a master switch for visual/theming hooks (`pref_enable_ui_tweaks`). When disabled, all non-functional UI hooks are skipped to ensure 100% native rendering smoothness and zero typing latency.
- **Material 3 Expressive (Belka Rounded Keys)**: Optional rounded keycaps styling and pill-shaped keys.
- **Candidate Emoji Vibrancy**: Renders candidate strip emojis with 100% opacity instead of dim translucent text.

### 📱 Per-App Pixel 9 Pro Device Spoofing
- Spoofs the Gboard process to **Pixel 9 Pro (`caiman`)** internally to pass server-side eligibility checks without modifying global system properties or triggering banking app detection.

### 📋 Clipboard History Sync
- Bypasses OEM background restrictions on Samsung One UI and Vivo OriginOS, keeping clipboard history in sync.

---

## 📲 Installation

### Requirements
- Rooted Android device (KernelSU / APatch / Magisk)
- [LSPosed](https://github.com/LSPosed/LSPosed) (Zygisk release)
- [Gboard](https://play.google.com/store/apps/details?id=com.google.android.inputmethod.latin) installed

### Steps
1. Download the latest APK from the [Releases](https://github.com/EraserCN/FuckGboard/releases) page.
2. Install the APK on your device.
3. Open LSPosed Manager, enable the **FuckGboard** module, and ensure **Gboard (`com.google.android.inputmethod.latin`)** is checked in the scope.
4. Open the FuckGboard app to customize your settings.
5. Tap **"Force Stop Gboard"** at the bottom of the settings page (or force stop Gboard manually) to apply changes.

---

## ⚙️ Configuration Guide

| Setting | Recommended Value | Description |
|---|---|---|
| **Master Switch** | `Enabled` | Master switch for all Gboard modifications |
| **Pixel 9 Pro Device Spoof** | `Enabled` | Spoofs Gboard to Pixel 9 Pro (`caiman`) for feature gating |
| **Gemini Rambler Voice Typing** | `Enabled` | Activates NGA Agentic Dictation |
| **AI Writing Tools & Proofread** | `Enabled` | Enables inline proofreading and text rewriting |
| **All Languages Support** | `Enabled` | Allows Writing Tools on non-English keyboard layouts |
| **AI Backend Model** | `Astrea` | Selects cloud inference engine (bypasses BL-unlock Nano blocks) |
| **Enable UI & Styling Hooks** | `Disabled` | Turn off to keep stock UI rendering and zero typing lag |

---

## 🛠️ Building from Source

```bash
git clone https://github.com/EraserCN/FuckGboard.git
cd FuckGboard

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

To sign with your own keystore, create a `keystore.properties` in the project root:
```properties
storeFile=/path/to/your/keystore.jks
storePassword=your_store_password
keyAlias=your_key_alias
keyPassword=your_key_password
```

---

## 🙏 Acknowledgements & Credits

- **[PixelBoard](https://github.com/Akshayykadam/PixelBoard)** by [@Akshayykadam](https://github.com/Akshayykadam): Special thanks and credits to Akshay Kadam for reverse engineering Gboard's Phenotype flags and creating the original APK patcher project that inspired this LSPosed implementation.
- **[LSPosed](https://github.com/LSPosed/LSPosed)**: The legendary ART hooking framework.
- **[XposedBridge](https://github.com/rovo89/XposedBridge)** by @rovo89.

---

## 📄 License

This project is licensed under the [GNU General Public License v3.0](LICENSE).
