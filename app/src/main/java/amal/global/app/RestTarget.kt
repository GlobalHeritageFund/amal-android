package global.amal.app

import android.util.Log

//phrase not necessary now bc enum and passphrase the same, but included in case changes in future
enum class RestTarget (val phrase: String, val url: String) {
    EAMENA("EAMENA", "https://eamena.herbridge.org/"),
    UKRAINE("UKRAINE", "https://ukraine.herbridge.org/");

    //this allows us to look up the RestTarget enum from its passphrase
    companion object {
        private val map = entries.associateBy { it.phrase }

        operator fun get(phrase: String) = map[phrase]

        fun fromPhrase(phrase: String): RestTarget? {
            Log.d("RestTarget", "Called fromPhrase")
            return entries.find { it.phrase == phrase }
        }
    }
}

