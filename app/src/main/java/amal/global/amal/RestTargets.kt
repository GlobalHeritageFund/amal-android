package amal.global.amal

//phrase not necessary now bc enum and passphrase the same, but included in case changes in future
enum class RestTargets (val phrase: String, val url: String) {
    EAMENA("EAMENA", "https://eamena.herbridge.org/"),
    UKRAINE("UKRAINE", "https://ukraine.herbridge.org")
}
