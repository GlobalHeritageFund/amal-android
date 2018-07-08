package amal.global.amal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import com.google.firebase.storage.StorageReference

class ImageFetcher(val context: Context) {

    private val memoryCache = ImageMemoryCache(context)
    
    fun fetchImage(firebaseReference: StorageReference): Promise<Bitmap> {
        memoryCache.get(firebaseReference.toString())?.let {
            return@fetchImage Promise(it)
        }
        return firebaseReference
                .getBytesPromise(Long.MAX_VALUE)
                .flatMap { byteArray ->
                    byteArray.decodeBitmap()
                }
                .then { bitmap ->
                    memoryCache.set(firebaseReference.toString(), bitmap)
                }
    }
}

class ImageMemoryCache(context: Context) {

    private val maxMemory = Runtime.getRuntime().maxMemory().toInt()

    private val cacheSize = maxMemory / 8

    private val cache = object: LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String?, bitmap: Bitmap?): Int {
            return bitmap?.let { it.allocationByteCount } ?: 0
        }
    }

    fun get(key: String): Bitmap? {
        return cache.get(key)
    }

    fun set(key: String, bitmap: Bitmap) {
        cache.put(key, bitmap)
    }
}