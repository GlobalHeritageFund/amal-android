package amal.global.amal

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import android.content.pm.PackageManager
import androidx.preference.Preference

interface SettingsFragmentDelegate {
    fun signOutTapped(fragment: SettingsFragment)
    fun signInTapped(fragment: SettingsFragment)
    fun passphraseButtonTapped(fragment: SettingsFragment)
}

class SettingsFragment: PreferenceFragmentCompat() {

    var delegate: SettingsFragmentDelegate? = null

    private val currentUser: CurrentUser
        get() = CurrentUser(this.requireContext())

    private val versionPreference: Preference?
        get() = findPreference("versionPreference")

    private val authPreference: Preference?
        get() = findPreference("auth")

    private val passphrasePreference: Preference?
        get() = findPreference("passphrase")

    override fun onCreatePreferences(bundle: Bundle?, string: String?) {
        addPreferencesFromResource(R.xml.preferences)

        configureView()
        authPreference?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            if (currentUser.isLoggedIn) {
                delegate?.signOutTapped(this)
            } else {
                delegate?.signInTapped(this)
            }
            configureView()
            true
        }

        passphrasePreference?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            delegate?.passphraseButtonTapped(this)
            true
        }
    }

    fun configureView() {

        try {
            val versionCode = BuildConfig.VERSION_CODE
            val versionName = BuildConfig.VERSION_NAME
            val version = "$versionName.$versionCode"
            versionPreference?.summary = version
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        val email = currentUser.email ?: ""
        val stringID = if (currentUser.isLoggedIn) R.string.log_out else R.string.log_in

        authPreference?.title = resources.getString(stringID)
        authPreference?.summary = if (currentUser.isLoggedIn) "${resources.getString(R.string.signed_in_as)} $email" else ""
    }
}
