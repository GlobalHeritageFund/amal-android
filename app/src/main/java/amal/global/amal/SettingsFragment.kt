package amal.global.amal

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import android.view.View

internal class SettingsFragment: PreferenceFragmentCompat() {

    override fun onCreatePreferences(bundle: Bundle?, string: String?) {
        addPreferencesFromResource(R.xml.preferences)
    }
}
