package amal.global.amal

import android.graphics.Bitmap
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ImageFetcher() {

    fun fetchImage(firebaseReference: StorageReference): Promise<Bitmap> {
        return firebaseReference
                .getBytesPromise(Long.MAX_VALUE)
                .flatMap { byteArray ->
                    byteArray.decodeBitmap()
                }

    }
}