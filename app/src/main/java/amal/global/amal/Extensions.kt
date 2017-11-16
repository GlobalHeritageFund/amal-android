package amal.global.amal

import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText

fun <T : View> Fragment.bind(@IdRes res : Int) : T {
    @Suppress("UNCHECKED_CAST")
    return getView()!!.findViewById<T>(res)
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
