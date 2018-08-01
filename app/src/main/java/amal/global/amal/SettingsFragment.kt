package amal.global.amal

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import android.content.pm.PackageManager
import android.support.v7.preference.Preference


internal class SettingsFragment: PreferenceFragmentCompat() {

    private val currentUser: CurrentUser
        get() = CurrentUser(this.context!!)


    private val versionPreference: Preference
        get() = findPreference("versionPreference")

    private val authPreference: Preference
        get() = findPreference("auth")

    override fun onCreatePreferences(bundle: Bundle?, string: String?) {
        addPreferencesFromResource(R.xml.preferences)

        configureView()

        authPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            if (currentUser.isLoggedIn) {
                currentUser.signOut()
            } else {
                //call delegate and sign in
            }
            configureView()
            true
        }
    }

    fun configureView() {

        try {
            val pInfo = activity!!.packageManager.getPackageInfo(activity!!.packageName, 0)
            val version = pInfo.versionName
            versionPreference.summary = version
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        authPreference.title = if (currentUser.isLoggedIn ) "Sign Out" else "Sign In"

    }
}
