package amal.global.amal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import com.google.firebase.storage.StorageReference
import com.jakewharton.disklrucache.DiskLruCache
import java.io.File
import java.security.MessageDigest

class ImageFetcher(val context: Context) {

    private val memoryCache = ImageMemoryCache(context)
    
    private val diskCache = ImageDiskCache(context)

    fun fetchImage(firebaseReference: StorageReference): Promise<Bitmap> {
        val key = firebaseReference.toString()
        memoryCache.get(key)?.let {
            return@fetchImage Promise(it)
        }
        diskCache.get(key)?.let {
            memoryCache.set(key, it)
            return@fetchImage Promise(it)
        }
        return firebaseReference
                .getBytesPromise(Long.MAX_VALUE)
                .flatMap { byteArray ->
                    byteArray.decodeBitmap()
                }
                .then { bitmap ->
                    memoryCache.set(key, bitmap)
                    diskCache.set(key, bitmap)
                }
    }
}

class ImageDiskCache(val context: Context) {

    private val cachePath = context.cacheDir.path

    private val cacheFile = File(cachePath + File.separator + "images")

    val cache = DiskLruCache.open(cacheFile, 1, 1, 10*1024*1024 /* 10 megabytes */)

    fun filenameForKey(key: String): String {
        return MessageDigest
                .getInstance("SHA-1")
                .digest(key.toByteArray())
                .joinToString(separator = "", transform = { Integer.toHexString(0xFF and it.toInt()) })
    }

    fun get(key: String): Bitmap? {
        try {
            val filename = filenameForKey(key)
            val inputStream = cache.get(filename).getInputStream(0)
            return BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            return null
        }
    }

    fun set(key: String, bitmap: Bitmap) {
        val filename = filenameForKey(key)
        val snapshot = cache.get(filename)
        if (snapshot == null) {
            val editor = cache.edit(filename)
            val outputStream = editor.newOutputStream(0)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            editor.commit()
            outputStream.close()
        } else {
            snapshot.getInputStream(0).close()
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