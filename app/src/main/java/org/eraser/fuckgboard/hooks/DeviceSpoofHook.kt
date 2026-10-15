package org.eraser.fuckgboard.hooks

import android.os.Build
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

object DeviceSpoofHook {
    private const val TAG = "PixelBoard-Spoof"

    fun apply() {
        try {
            setField(Build::class.java, "BRAND", "google")
            setField(Build::class.java, "MANUFACTURER", "Google")
            setField(Build::class.java, "MODEL", "Pixel 9 Pro")
            setField(Build::class.java, "DEVICE", "caiman")
            setField(Build::class.java, "PRODUCT", "caiman")
            setField(Build::class.java, "HARDWARE", "caiman")
            XposedBridge.log("[$TAG] Successfully spoofed device to Pixel 9 Pro (caiman)")
        } catch (t: Throwable) {
            XposedBridge.log("[$TAG] Failed to spoof device: ${t.message}")
        }
    }

    private fun setField(clazz: Class<*>, fieldName: String, value: Any) {
        try {
            XposedHelpers.setStaticObjectField(clazz, fieldName, value)
        } catch (t: Throwable) {
            XposedBridge.log("[$TAG] Could not set $fieldName: ${t.message}")
        }
    }
}
