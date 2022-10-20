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

    //changed all below from Preference to Preference? bc was getting type mismatch from inferred type
    private val versionPreference: Preference?
        get() = findPreference("versionPreference")

    private val authPreference: Preference?
        get() = findPreference("auth")

    private val passphrasePreference: Preference?
        get() = findPreference("passphrase")

    override fun onCreatePreferences(bundle: Bundle?, string: String?) {
        addPreferencesFromResource(R.xml.preferences)

        configureView()
//changed authPreference.onPref.. to safe call since changed above to Preference?
        //TODO this and above changes need to be checked and changed to preserve for functionality, but doing now to get build to work
        authPreference?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            if (currentUser.isLoggedIn) {
                delegate?.signOutTapped(this)
            } else {
                delegate?.signInTapped(this)
            }
            configureView()
            true
        }
        //changed Preference.onPref.. to safe call since changed above to Preference?
        //TODO this and above changes need to be checked and changed to preserve for functionality, but doing now to get build to work

        passphrasePreference?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            delegate?.passphraseButtonTapped(this)
            true
        }
    }

    fun configureView() {

        try {
            val pInfo = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
            val version = pInfo.versionName
            //changed Preference.summary.. to safe call since changed above to Preference?
            //TODO this and above changes need to be checked and changed to preserve for functionality, but doing now to get build to work
            versionPreference?.summary = version
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        val email = currentUser.email ?: ""
        val stringID = if (currentUser.isLoggedIn) R.string.log_out else R.string.log_in
        //changed authPref.title and summary to safe call since changed above to Preference?
        //TODO this and above changes need to be checked and changed to preserve for functionality, but doing now to get build to work

        authPreference?.title = resources.getString(stringID)
        authPreference?.summary = if (currentUser.isLoggedIn) "${resources.getString(R.string.signed_in_as)} $email" else ""
    }
}
