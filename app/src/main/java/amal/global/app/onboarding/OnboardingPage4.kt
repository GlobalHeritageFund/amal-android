package global.amal.app.onboarding

import global.amal.app.R
import global.amal.app.databinding.FragmentOnboardingPage4Binding
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class OnboardingPage4 : Fragment() {

    private var _binding: FragmentOnboardingPage4Binding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.

    private val binding get() = _binding!!
    private val tosUrl = "http://amal.global/terms-of-service/"
    private val privacyUrl = "https://globalheritagefund.org/index.php/news-resources/library/privacy-policy/"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOnboardingPage4Binding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tos.setOnClickListener { launchWebBrowser(tosUrl) }
        binding.privacy.setOnClickListener { launchWebBrowser(privacyUrl) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun launchWebBrowser(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }
}