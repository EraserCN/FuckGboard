package org.eraser.fuckgboard.hooks

import android.content.Context
import android.preference.PreferenceManager
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

object RamblerHook {
    private const val TAG = "PixelBoard-Rambler"
    private const val VOICE_SETTINGS =
        "com.google.android.apps.inputmethod.latin.preference.VoiceSettingsFragment"

    fun apply(classLoader: ClassLoader) {
        // Hook VoiceSettingsFragment to ensure enable_jetson is retained
        try {
            val fragmentClass = XposedHelpers.findClassIfExists(VOICE_SETTINGS, classLoader)
            if (fragmentClass != null) {
                XposedBridge.hookAllMethods(fragmentClass, "ac", object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        try {
                            val context = XposedHelpers.callMethod(param.thisObject, "getContext") as? Context
                            if (context != null) {
                                ensureJetsonPreference(context)
                            }
                        } catch (t: Throwable) {
                            // ignore
                        }
                    }
                })
                XposedBridge.log("[$TAG] Hooked VoiceSettingsFragment for Rambler setup")
            }
        } catch (t: Throwable) {
            XposedBridge.log("[$TAG] VoiceSettingsFragment hook error: ${t.message}")
        }
    }

    private fun ensureJetsonPreference(context: Context) {
        try {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            if (!prefs.contains("enable_jetson") || !prefs.getBoolean("enable_jetson", true)) {
                prefs.edit().putBoolean("enable_jetson", true).apply()
                XposedBridge.log("[$TAG] Ensured enable_jetson=true in Gboard preferences")
            }
        } catch (t: Throwable) {
            // ignore
        }
    }
}
