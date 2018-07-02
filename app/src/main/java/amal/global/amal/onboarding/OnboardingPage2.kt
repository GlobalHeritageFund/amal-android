package amal.global.amal.onboarding

import amal.global.amal.R
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class OnboardingPage2 : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(
                R.layout.fragment_onboarding_page_2,
                container,
                false
        )

    }
}