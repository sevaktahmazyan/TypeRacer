package com.webbfontaine.typeracer.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.webbfontaine.typeracer.R
import com.webbfontaine.typeracer.inputpage.InputViewModel
import kotlinx.android.synthetic.main.fragment_welcome.*


class WelcomeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //pre request for random text to get ready when player
        //will navigate to game page.
        activity?.let {
            ViewModelProviders.of(it).get(InputViewModel::class.java).getSentence()
        }
        btn_play.setOnClickListener { startApp() }
    }

    private fun startApp() {
        view?.let {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.welcomeFragment, true)
                .build()
            Navigation.findNavController(it).navigate(R.id.action_welcomeFragment_to_inputFragment, null, navOptions)
        }
    }
}