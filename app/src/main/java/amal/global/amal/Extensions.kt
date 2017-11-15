package amal.global.amal

fun String.getExtension(): String {
    val separator = System.getProperty("file.separator")
    val filename: String

    // Remove the path upto the filename.
    val lastSeparatorIndex = this.lastIndexOf(separator)
    if (lastSeparatorIndex == -1) {
        filename = this
    } else {
        filename = this.substring(lastSeparatorIndex + 1)
    }

    // Remove the extension.
    val extensionIndex = filename.lastIndexOf(".")
    return if (extensionIndex == -1) {
        filename
    } else filename.substring(extensionIndex + 1)

}

fun String.removeExtension(): String {
    val separator = System.getProperty("file.separator")
    val filename: String

    // Remove the path upto the filename.
    val lastSeparatorIndex = this.lastIndexOf(separator)
    if (lastSeparatorIndex == -1) {
        filename = this
    } else {
        filename = this.substring(lastSeparatorIndex + 1)
    }

    // Remove the extension.
    val extensionIndex = filename.lastIndexOf(".")
    return if (extensionIndex == -1) {
        filename
    } else filename.substring(0, extensionIndex)
}
