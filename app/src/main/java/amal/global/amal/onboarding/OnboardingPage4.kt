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
    private val tosUrl = "http://amal.global/terms-of-service/"
    private val privacyUrl = "https://globalheritagefund.org/index.php/news-resources/library/privacy-policy/"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(
                R.layout.fragment_onboarding_page_4,
                container,
                false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tos.setOnClickListener { launchWebBrowser(tosUrl) }
        privacy.setOnClickListener { launchWebBrowser(privacyUrl) }
    }

    private fun launchWebBrowser(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }
}