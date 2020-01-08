package amal.global.amal

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_passphrase_form.*

interface PassphraseFormFragmentDelegate {
    fun passphraseEntered(passphrase: String, fragment: PassphraseFormFragment)
}

class PassphraseFormFragment: Fragment() {

    var delegate: PassphraseFormFragmentDelegate? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_passphrase_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        submitButton.setOnClickListener({
            delegate?.passphraseEntered(passphraseField.text.toString(), this)
        })
    }

}
