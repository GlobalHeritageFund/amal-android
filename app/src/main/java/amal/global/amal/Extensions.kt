package amal.global.amal

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import java.io.File

fun <T : View> Fragment.bind(@IdRes res : Int) : T {
    @Suppress("UNCHECKED_CAST")
    return getView()!!.findViewById<T>(res)
}

fun <T : View> View.bind(@IdRes res : Int) : T {
    @Suppress("UNCHECKED_CAST")
    return findViewById<T>(res)
}

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}


fun EditText.afterTextChanged(afterTextChanged: (Editable?) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable)
        }
    })
}

fun File.decodeBitmap(): Promise<Bitmap> {
    return Promise<Bitmap>({ fulfill, reject ->
        fulfill(BitmapFactory.decodeFile(this.path))
    })
}

fun Bitmap.scale(dstWidth: Int, dstHeight: Int, filter: Boolean): Promise<Bitmap> {
    return Promise<Bitmap>({ fulfill, reject ->
        fulfill(Bitmap.createScaledBitmap(this, dstWidth, dstHeight, filter))
    })
}

fun DatabaseReference.setValuePromise(value: Any): Promise<Unit> {
    return Promise<Unit>({ fulfill, reject ->
        this.setValue(value, { databaseError, databaseReference ->
            if (databaseError != null) {
                reject(Error(databaseError.message))
            } else {
                fulfill(kotlin.Unit)
            }
        })
    })
}

fun StorageReference.putFilePromise(uri: Uri, metadata: StorageMetadata): Promise<Unit> {
    return Promise<Unit>({ fulfill, reject ->
        val uploadTask = this.putFile(uri, metadata)
        uploadTask.addOnCompleteListener({ task ->
            fulfill(Unit)
        })
        uploadTask.addOnFailureListener({ exception ->
            reject(Error(exception.message))
        })
    })
}

