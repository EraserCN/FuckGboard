package org.eraser.fuckgboard.ui

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.eraser.fuckgboard.R
import java.io.DataOutputStream
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = PREFS_NAME
            preferenceManager.sharedPreferencesMode = Context.MODE_PRIVATE
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            findPreference<Preference>("action_restart_gboard")?.setOnPreferenceClickListener {
                fixPermissions(requireContext())
                restartGboard(requireContext())
                true
            }
        }

        override fun onPause() {
            super.onPause()
            fixPermissions(requireContext())
        }

        private fun fixPermissions(context: Context) {
            try {
                val dataDir = File(context.applicationInfo.dataDir)
                dataDir.setReadable(true, false)
                dataDir.setExecutable(true, false)
                val spDir = File(dataDir, "shared_prefs")
                spDir.setReadable(true, false)
                spDir.setExecutable(true, false)
                val prefFile = File(spDir, "${PREFS_NAME}.xml")
                prefFile.setReadable(true, false)
            } catch (_: Throwable) {}
        }

        private fun restartGboard(context: Context) {
            try {
                val process = Runtime.getRuntime().exec("su")
                val os = DataOutputStream(process.outputStream)
                os.writeBytes("am force-stop com.google.android.inputmethod.latin\n")
                os.writeBytes("exit\n")
                os.flush()
                process.waitFor()
                Toast.makeText(context, R.string.gboard_restarted, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, R.string.root_required, Toast.LENGTH_LONG).show()
            }
        }

        companion object {
            const val PREFS_NAME = "org.eraser.fuckgboard_preferences"
        }
    }
}
