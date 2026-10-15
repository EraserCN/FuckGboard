package org.eraser.fuckgboard.hooks

import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

object RoundedKeysHook {
    private const val TAG = "PixelBoard-RoundedKeys"

    fun apply(classLoader: ClassLoader, enabled: Boolean) {
        // 1. Hook Theme Key-Shape style resolver (aitv in Gboard 18.5.2)
        // aitv.b(Context, boolean) returns: 1 = Rounded Keys (M3 Expressive), 0 = Standard
        try {
            val aitvClass = XposedHelpers.findClassIfExists("aitv", classLoader)
            if (aitvClass != null) {
                val styleHook = object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = if (enabled) 1 else 0
                    }
                }
                XposedBridge.hookAllMethods(aitvClass, "a", styleHook)
                XposedBridge.hookAllMethods(aitvClass, "b", styleHook)
                XposedBridge.hookAllMethods(aitvClass, "c", styleHook)

                XposedBridge.hookAllMethods(aitvClass, "f", object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = enabled
                    }
                })
                XposedBridge.log("[$TAG] Successfully hooked aitv methods (style=${if (enabled) 1 else 0})")
            }
        } catch (t: Throwable) {
            XposedBridge.log("[$TAG] aitv hook error: ${t.message}")
        }

        // 2. Hook Gboard's theme flags class (aiug in 18.5.2)
        try {
            val aiugClass = XposedHelpers.findClassIfExists("aiug", classLoader)
            if (aiugClass != null) {
                val booleanHook = object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = enabled
                    }
                }
                for (methodName in listOf("b", "c", "d", "e", "f")) {
                    try {
                        XposedBridge.hookAllMethods(aiugClass, methodName, booleanHook)
                    } catch (_: Throwable) {}
                }

                // After aiug.a(Context) updates the flag wrappers, force fields l, m, n, o, p (.b)
                XposedBridge.hookAllMethods(aiugClass, "a", object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        for (fieldName in listOf("l", "m", "n", "o", "p")) {
                            try {
                                val wrapper = XposedHelpers.getStaticObjectField(aiugClass, fieldName) ?: continue
                                XposedHelpers.setObjectField(wrapper, "b", enabled)
                            } catch (_: Throwable) {}
                        }
                    }
                })
                XposedBridge.log("[$TAG] Successfully hooked aiug methods for Belka keys (enabled=$enabled)")
            }
        } catch (t: Throwable) {
            XposedBridge.log("[$TAG] aiug hook error: ${t.message}")
        }

        // 3. Inject enable_rounded_keys preference into Gboard's default SharedPreferences at Application startup
        try {
            XposedHelpers.findAndHookMethod(
                "android.app.Application",
                classLoader,
                "onCreate",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        try {
                            val app = param.thisObject as? Context ?: return
                            injectRoundedKeysPref(app, enabled)
                        } catch (_: Throwable) {}
                    }
                }
            )
        } catch (_: Throwable) {}
    }

    private fun injectRoundedKeysPref(context: Context, enabled: Boolean) {
        try {
            val prefs = context.getSharedPreferences("${context.packageName}_preferences", Context.MODE_PRIVATE)
            if (prefs.getBoolean("enable_rounded_keys", !enabled) != enabled) {
                prefs.edit().putBoolean("enable_rounded_keys", enabled).apply()
                XposedBridge.log("[$TAG] Synced enable_rounded_keys=$enabled to Gboard SharedPreferences")
            }
        } catch (_: Throwable) {}
    }
}
