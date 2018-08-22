package amal.global.amal

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.support.media.ExifInterface
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

fun <T : View> Fragment.bind(@IdRes res: Int): T {
    @Suppress("UNCHECKED_CAST")
    return view!!.findViewById<T>(res)
}

fun <T : View> View.bind(@IdRes res: Int): T {
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

fun StorageReference.getBytesPromise(maxDownloadSizeBytes: Long): Promise<ByteArray> {
    return Promise({ fulfill, reject ->
        val task = this.getBytes(maxDownloadSizeBytes)

        task.addOnCompleteListener({ task ->
            val exception = task.exception
            if (task.isSuccessful) {
                fulfill(task.result)
            } else if (exception != null) {
                reject(Error(exception.message))
            }
        })

        task.addOnFailureListener({ exception ->
            reject(Error(exception.message))
        })

    })
}

interface OnItemClickListener {
    fun onItemClicked(position: Int, view: View)
}

fun RecyclerView.addOnItemClickListener(onClickListener: OnItemClickListener) {
    this.addOnChildAttachStateChangeListener(object: RecyclerView.OnChildAttachStateChangeListener {
        override fun onChildViewDetachedFromWindow(view: View?) {
            view?.setOnClickListener(null)
        }

        override fun onChildViewAttachedToWindow(view: View?) {
            view?.setOnClickListener({
                val holder = getChildViewHolder(view)
                onClickListener.onItemClicked(holder.adapterPosition, view)
            })
        }
    })
}

fun ExifInterface.getTimeStamp(): Date? {
    val formatter = SimpleDateFormat("yyyy:MM:dd HH:mm:ss")
    formatter.setTimeZone(TimeZone.getTimeZone("UTC"))


    val dateTimeString = this.getAttribute(ExifInterface.TAG_DATETIME)
    val nonZeroTimePattern = Pattern.compile(".*[1-9].*")

    if (dateTimeString == null || !nonZeroTimePattern.matcher(dateTimeString).matches())
        return null

    val pos = ParsePosition(0)
    try {
        // The exif field is in local time. Parsing it as if it is UTC will yield time
        // since 1/1/1970 local time
        return formatter.parse(dateTimeString, pos)
    } catch (e: IllegalArgumentException) {
        return null
    }
}

fun Call.enqueue(): Promise<Response> {
    val promise = Promise<Response>()

    this.enqueue(object: Callback {
        override fun onFailure(call: Call?, e: IOException?) {
            promise.reject(Error(e?.message ?: "The request unexpectedly failed."))
        }

        override fun onResponse(call: Call?, response: Response?) {
            try {
                if (response == null) {
                    promise.reject(Error("The response was null."))
                    return
                }

                if (!response.isSuccessful) {
                    promise.reject(Error((response.body()?.string() ?: "") + "Unexpected error code :" + response))
                    return
                }

                promise.fulfill(response)
            } catch (e: Exception) {
                promise.reject(Error(e.message))
            }
        }
    })

    return promise

}