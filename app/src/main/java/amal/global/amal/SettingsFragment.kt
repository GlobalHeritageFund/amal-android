package amal.global.amal

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import android.view.View
import android.content.pm.PackageManager
import android.R.attr.versionName
import android.content.pm.PackageInfo



internal class SettingsFragment: PreferenceFragmentCompat() {

    override fun onCreatePreferences(bundle: Bundle?, string: String?) {
        addPreferencesFromResource(R.xml.preferences)

        val versionPreference = findPreference("versionPreference")

        try {
            val pInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
            val version = pInfo.versionName
            versionPreference.summary = version
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }


    }
}
