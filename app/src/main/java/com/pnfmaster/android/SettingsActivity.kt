package com.pnfmaster.android

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import java.util.Locale
import kotlin.system.exitProcess

private const val TITLE_TAG = "settingsActivityTitle"

class SettingsActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        val toolbarSettings = findViewById<Toolbar>(R.id.toolbarSettings)
        setSupportActionBar(toolbarSettings)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setTitle(R.string.Settings)
        }

        // Display the fragment as the main content.
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, HeaderFragment())
                .commit()
        } else {
            title = savedInstanceState.getCharSequence(TITLE_TAG)
        }

        // Add back stack listener to update the title
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                setTitle(R.string.title_activity_settings)
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, title)
    }

    // Handle back navigation with the back button
    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.popBackStackImmediate()) {
            return true
        }
        return super.onSupportNavigateUp()
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        // Instantiate the new Fragment
        val args = pref.extras
        val fragment = pref.fragment?.let {
            supportFragmentManager.fragmentFactory.instantiate(
                classLoader,
                it
            ).apply {
                arguments = args
                setTargetFragment(caller, 0)
            }
        }
        // Replace the existing Fragment with the new Fragment
        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.settings, fragment)
                .addToBackStack(null)
                .commit()
        }
        title = pref.title
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressedDispatcher.onBackPressed()
        }
        return true
    }

    class HeaderFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.header_preferences, rootKey)
        }
    }

    class MessagesFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.basic_setings_preferences, rootKey)

            val languagePreference = findPreference<ListPreference>("language")
            if (MyApplication.language == "en") {
                languagePreference?.setDefaultValue("en")
            } else {
                languagePreference?.setDefaultValue("cn")
            }

            languagePreference?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->

                    AlertDialog.Builder(requireContext()).apply {
                        setTitle(getString(R.string.Hint))
                        setMessage("需要重新启动应用。是否继续？")
                        setNegativeButton(getString(R.string.No), null)
                        setPositiveButton(getString(R.string.Yes)) { _, _ ->
                            val language: Locale
                            when (newValue) {
                                "en" -> {
                                    language = Locale.ENGLISH
                                    MyApplication.language = "en"
                                }
                                "cn" -> {
                                    language = Locale.SIMPLIFIED_CHINESE
                                    MyApplication.language = "cn"
                                }
                                else -> language = Locale.ENGLISH
                            }
//                            (activity?.application as MyApplication).setLocale(language)
                            restartApp()
                        }
                        create()
                        show()
                    }

                    true
                }
        }

        private fun restartApp() {
            val intent = Intent(context, ControlActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            if (context is Activity) {
                (context as Activity).finish()
            }
            exitProcess(0)
        }
    }

    class SyncFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.sync_preferences, rootKey)
        }
    }
}