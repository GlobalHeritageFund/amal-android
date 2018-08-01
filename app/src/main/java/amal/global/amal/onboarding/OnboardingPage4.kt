package amal.global.amal.onboarding

import amal.global.amal.R
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_onboarding_page_4.*


class OnboardingPage4 : Fragment() {
    private val tosUri = Uri.parse("http://amal.global/terms-of-service/")
    private val privacyUri = Uri.parse("https://globalheritagefund.org/index.php/news-resources/library/privacy-policy/")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(
                R.layout.fragment_onboarding_page_4,
                container,
                false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tos.setOnClickListener { launchWebBrowser(tosUri) }
        privacy.setOnClickListener { launchWebBrowser(privacyUri) }
    }

    private fun launchWebBrowser(uri: Uri) {
        val browserIntent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(browserIntent)
    }
}