package org.eraser.fuckgboard.hooks

import android.graphics.Canvas
import android.widget.TextView
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

object CandidateEmojiHook {
    private const val TAG = "PixelBoard-Emoji"
    private const val TARGET_CLASS =
        "com.google.android.libraries.inputmethod.widgets.text.AutoCenterScaleTextView"

    fun apply(classLoader: ClassLoader) {
        try {
            val clazz = XposedHelpers.findClassIfExists(TARGET_CLASS, classLoader)
            if (clazz == null) {
                XposedBridge.log("[$TAG] Target class $TARGET_CLASS not found")
                return
            }

            XposedBridge.hookAllMethods(clazz, "onDraw", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val textView = param.thisObject as? TextView ?: return
                    val text = textView.text ?: return
                    if (containsEmoji(text)) {
                        val paint = textView.paint
                        paint.color = paint.color or 0xFF000000.toInt()
                        paint.alpha = 255
                    }
                }
            })
            XposedBridge.log("[$TAG] Hooked AutoCenterScaleTextView.onDraw for emoji vibrancy")
        } catch (t: Throwable) {
            XposedBridge.log("[$TAG] Failed to hook emoji vibrancy: ${t.message}")
        }
    }

    private fun containsEmoji(cs: CharSequence): Boolean {
        val len = cs.length
        for (i in 0 until len) {
            val c = cs[i]
            if (Character.isSurrogate(c)) return true
            if (c.code in 0x203C..0x3299) return true
            if (c.code == 0xFE0F) return true
        }
        return false
    }
}
