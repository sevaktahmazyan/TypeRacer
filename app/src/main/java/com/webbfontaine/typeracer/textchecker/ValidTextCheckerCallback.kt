package com.webbfontaine.typeracer.textchecker

interface ValidTextCheckerCallback {

    fun onMistakePublished(text: String, mistakeAt: Int)

    fun onMistakeFixed(text: String, mistakeAt: Int = -1)

    fun onWordPublished(text: String)

    fun wordToCheck(text: String)
}