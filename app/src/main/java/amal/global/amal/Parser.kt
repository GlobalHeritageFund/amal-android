package amal.global.amal

import java.security.InvalidParameterException

class Parser(val map: HashMap<String, Any>) {

    fun <T> fetch(key: String): T {
        val untyped = map[key] ?: throw InvalidParameterException("Value at $key not found.")

        val typed = (untyped as? T) ?: throw InvalidParameterException("Value at key $key was type ${key.javaClass} but should have been something else.")

        return typed
    }

    fun <T> fetchOptional(key: String): T? {
        val untyped = map[key] ?: return null

        val typed = (untyped as? T) ?: throw InvalidParameterException("Value at key $key was type ${key.javaClass} but should have been something else.")

        return typed

    }
}