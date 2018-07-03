package amal.global.amal

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
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
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.concurrent.Semaphore

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

fun File.decodeBitmap(): Promise<Bitmap> {
    return Promise<Bitmap>({ fulfill, reject ->
        fulfill(BitmapFactory.decodeFile(this.path))
    })
}

fun ByteArray.decodeBitmap(offset: Int = 0): Promise<Bitmap> {
    return Promise({ fulfill, reject ->
        fulfill(BitmapFactory.decodeByteArray(this, offset, this.count()))
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

fun Semaphore.acquirePromise(): Promise<Unit> {
    return Promise<Unit>({ fulfill, reject ->
        this.acquire()
        fulfill(Unit)
    })
}

@Throws(JSONException::class)
fun fixJSON(json: Any?): Any? {
    return if (json === JSONObject.NULL) {
        null
    } else if (json is JSONObject) {
        toMap(json)
    } else if (json is JSONArray) {
        toList(json)
    } else {
        json
    }
}

@Throws(JSONException::class)
fun toList(array: JSONArray): List<*> {
    val list = ArrayList<Any>()
    for (i in 0 until array.length()) {
        fixJSON(array.get(i))?.let {
            list.add(it)
        }
    }
    return list
}

@Throws(JSONException::class)
fun toMap(value: JSONObject): Map<String, Any> {
    val map = HashMap<String, Any>()
    val keys = value.keys()
    while (keys.hasNext()) {
        val key = keys.next() as String
        fixJSON(value.get(key))?.let {
            map.put(key, it)
        }
    }
    return map
}
