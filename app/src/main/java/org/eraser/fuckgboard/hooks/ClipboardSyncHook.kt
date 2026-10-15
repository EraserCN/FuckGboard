package org.eraser.fuckgboard.hooks

import android.content.ClipboardManager
import android.inputmethodservice.InputMethodService
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

object ClipboardSyncHook {
    private const val TAG = "PixelBoard-Clipboard"

    fun apply(classLoader: ClassLoader) {
        // Method 1: Hook InputMethodService.onStartInputView in Gboard
        try {
            val latinImeClass = XposedHelpers.findClassIfExists(
                "com.google.android.apps.inputmethod.latin.LatinIME",
                classLoader
            )
            if (latinImeClass != null) {
                XposedBridge.hookAllMethods(latinImeClass, "onStartInputView", object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        syncClipboard(param.thisObject)
                    }
                })
                XposedBridge.log("[$TAG] Hooked LatinIME.onStartInputView for clipboard synchronization")
            }
        } catch (t: Throwable) {
            XposedBridge.log("[$TAG] Failed to hook onStartInputView: ${t.message}")
        }

        // Method 2: Hook internal handler fml.ft if present
        try {
            val fmlClass = XposedHelpers.findClassIfExists("fml", classLoader)
            if (fmlClass != null) {
                XposedBridge.hookAllMethods(fmlClass, "ft", object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        try {
                            val handler = XposedHelpers.getObjectField(param.thisObject, "f")
                            if (handler is ClipboardManager.OnPrimaryClipChangedListener) {
                                handler.onPrimaryClipChanged()
                            }
                        } catch (t: Throwable) {
                            // ignore field lookup failure
                        }
                    }
                })
                XposedBridge.log("[$TAG] Hooked fml.ft for clipboard synchronization")
            }
        } catch (t: Throwable) {
            // Ignore if fml class is obfuscated differently in this version
        }
    }

    private fun syncClipboard(ime: Any) {
        try {
            if (ime is InputMethodService) {
                val cm = ime.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? ClipboardManager
                cm?.primaryClip // Trigger read
            }
        } catch (t: Throwable) {
            // ignore
        }
    }
}
