package amal.global.amal

import amal.global.amal.databinding.FragmentPassphraseFormBinding
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

interface PassphraseFormFragmentDelegate {
    fun passphraseEntered(passphrase: String, fragment: PassphraseFormFragment)
}

class PassphraseFormFragment: Fragment() {

    private var _binding: FragmentPassphraseFormBinding? = null
    private val binding get() = _binding!!

    var delegate: PassphraseFormFragmentDelegate? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentPassphraseFormBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.submitButton.setOnClickListener({
            delegate?.passphraseEntered(binding.passphraseField.text.toString(), this)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
