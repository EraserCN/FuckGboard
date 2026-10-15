package org.eraser.fuckgboard.hooks

import android.content.Context
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

object SignatureBypassHook {
    private const val TAG = "PixelBoard-SignatureBypass"

    // Known signature check classes across Gboard versions
    private val CANDIDATE_CLASSES = listOf(
        "ryi",   // 18.4.1 Release
        "rvl",   // 18.3.1 Release
        "ajpz",  // 18.3.1 Beta
        "rpv"    // 18.0.3 Release
    )

    fun apply(classLoader: ClassLoader) {
        var hooked = 0
        for (className in CANDIDATE_CLASSES) {
            try {
                val clazz = XposedHelpers.findClassIfExists(className, classLoader) ?: continue
                XposedHelpers.findAndHookMethod(
                    clazz,
                    "a",
                    Context::class.java,
                    String::class.java,
                    XC_MethodReplacement.returnConstant(true)
                )
                hooked++
                XposedBridge.log("[$TAG] Successfully hooked signature check on $className.a(Context, String)")
            } catch (t: Throwable) {
                // Ignore classes not found in this specific version
            }
        }
        if (hooked == 0) {
            XposedBridge.log("[$TAG] Warning: No known signature check classes found in classloader")
        }
    }
}
