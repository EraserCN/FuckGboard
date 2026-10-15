package org.eraser.fuckgboard

import android.os.Build
import org.eraser.fuckgboard.hooks.CandidateEmojiHook
import org.eraser.fuckgboard.hooks.ClipboardSyncHook
import org.eraser.fuckgboard.hooks.DeviceSpoofHook
import org.eraser.fuckgboard.hooks.FlagHook
import org.eraser.fuckgboard.hooks.RamblerHook
import org.eraser.fuckgboard.hooks.RoundedKeysHook
import org.eraser.fuckgboard.ui.MainActivity
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

class XposedInit : IXposedHookLoadPackage, IXposedHookZygoteInit {

    companion object {
        private const val TAG = "PixelBoard-LSPosed"
        const val GBOARD_PACKAGE = "com.google.android.inputmethod.latin"
        const val MODULE_PACKAGE = "org.eraser.fuckgboard"
    }

    private var prefs: XSharedPreferences? = null

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        try {
            prefs = XSharedPreferences(MODULE_PACKAGE, MainActivity.SettingsFragment.PREFS_NAME)
            prefs?.makeWorldReadable()
        } catch (t: Throwable) {
            XposedBridge.log("[$TAG] initZygote warning: ${t.message}")
        }
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != GBOARD_PACKAGE) {
            return
        }

        XposedBridge.log("[$TAG] Gboard loaded in ${lpparam.processName}")

        val activePrefs: XSharedPreferences? = try {
            val p = prefs ?: XSharedPreferences(MODULE_PACKAGE, MainActivity.SettingsFragment.PREFS_NAME)
            p.reload()
            p
        } catch (t: Throwable) {
            XposedBridge.log("[$TAG] Failed to load preferences: ${t.message}")
            null
        }

        try {
            val masterSwitch = activePrefs?.getBoolean("pref_master_switch", true) ?: true
            if (!masterSwitch) {
                XposedBridge.log("[$TAG] Master switch is disabled.")
                return
            }

            // 1. Device Spoofing (spoof to Pixel 11 Pro)
            if (activePrefs?.getBoolean("pref_spoof_pixel", true) ?: true) {
                try {
                    DeviceSpoofHook.apply()
                } catch (t: Throwable) {
                    XposedBridge.log("[$TAG] DeviceSpoof error: ${t.message}")
                }
            }

            // 2. Phenotype Flag Interception (Rambler, AI Writing, Rounded keys, etc.)
            try {
                FlagHook(activePrefs).apply(lpparam.classLoader)
            } catch (t: Throwable) {
                XposedBridge.log("[$TAG] FlagHook error: ${t.message}")
            }

            // 3. Gemini Rambler Dictation Scope & Settings Hook
            if (activePrefs?.getBoolean("pref_rambler_voice", true) ?: true) {
                try {
                    RamblerHook.apply(lpparam.classLoader)
                } catch (t: Throwable) {
                    XposedBridge.log("[$TAG] RamblerHook error: ${t.message}")
                }
            }

            val uiTweaksEnabled = activePrefs?.getBoolean("pref_enable_ui_tweaks", false) ?: false

            // 4. Candidate Emoji Vibrancy Hook (Non-functional UI tweak)
            if (uiTweaksEnabled && (activePrefs?.getBoolean("pref_vibrant_emojis", true) ?: true)) {
                try {
                    CandidateEmojiHook.apply(lpparam.classLoader)
                } catch (t: Throwable) {
                    XposedBridge.log("[$TAG] CandidateEmojiHook error: ${t.message}")
                }
            }

            // 5. Samsung / Vivo Clipboard Sync Hook (Functional)
            if (activePrefs?.getBoolean("pref_clipboard_sync", true) ?: true) {
                try {
                    ClipboardSyncHook.apply(lpparam.classLoader)
                } catch (t: Throwable) {
                    XposedBridge.log("[$TAG] ClipboardSyncHook error: ${t.message}")
                }
            }

            // 6. Material 3 Belka Rounded Keys Hook (Non-functional UI tweak)
            if (uiTweaksEnabled && (activePrefs?.getBoolean("pref_rounded_keys", true) ?: true)) {
                try {
                    RoundedKeysHook.apply(lpparam.classLoader, true)
                } catch (t: Throwable) {
                    XposedBridge.log("[$TAG] RoundedKeysHook error: ${t.message}")
                }
            }

            XposedBridge.log("[$TAG] All hooks initialized safely for Gboard!")
        } catch (t: Throwable) {
            XposedBridge.log("[$TAG] Unexpected error in handleLoadPackage: ${t.message}")
        }
    }
}
