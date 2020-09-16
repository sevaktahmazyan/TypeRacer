package com.webbfontaine.typeracer.textchecker

import android.text.Editable
import android.text.TextWatcher
import java.util.*

open class ValidTextChecker(val textCheckerCallback: ValidTextCheckerCallback) : TextWatcher {

    private var textsQueue: ArrayDeque<String> = ArrayDeque()
    private var mistakeIndex = -1
    private var mistakePublished = false

    private fun hasMistake(): Boolean {
        return mistakeIndex != -1
    }

    private fun textToCheck(): String {
        return textsQueue.peek() ?: ""
    }

    private fun firstWordFromInput(s: Editable): String {
        val firstSpaceIndex = s.trim().indexOf(" ")
        return if (firstSpaceIndex == -1) s.toString() else s.substring(0, firstSpaceIndex + 1)
    }

    override fun afterTextChanged(s: Editable) {
        if (clearFirstWhiteSpaceIfExist(s)) {
            return
        }
        val textToCheck = textToCheck()
        val firstWord = firstWordFromInput(s)
        if (textToCheck.startsWith(firstWord)) {
            if (firstWord.isNotEmpty() && textToCheck == firstWord) {
                if (hasMistake() && mistakePublished) {
                    textCheckerCallback.onMistakeFixed(textToCheck, mistakeIndex)
                    mistakePublished = false
                    mistakeIndex = -1
                }
                if (textsQueue.isNotEmpty()) {
                    textsQueue.pop()
                }
                val stringBuilder = StringBuilder(textToCheck).append(" ")
                var loop = true
                while (textsQueue.peek() != null && loop) {
                    stringBuilder.append(textsQueue.peek())
                    if (s.startsWith(stringBuilder)) {
                        textsQueue.pop()
                        if (textsQueue.peek() != null) {
                            stringBuilder.append(" ")
                        }
                    } else {
                        val index = stringBuilder.lastIndexOf(textsQueue.peek())
                        stringBuilder.delete(index, stringBuilder.length)
                        loop = false
                    }
                }
                textCheckerCallback.onWordPublished(stringBuilder.trimEnd().toString())
                textCheckerCallback.wordToCheck(textsQueue.peek() ?: "")
            } else {
                textCheckerCallback.onMistakeFixed(textToCheck, mistakeIndex)
                mistakePublished = false
                mistakeIndex = -1
            }
        } else {
            if (!mistakePublished) {
                mistakePublished = true
                mistakeIndex = findWrongCharIndexInWord(textToCheck.toCharArray(), firstWordFromInput(s).toCharArray())
                textCheckerCallback.onMistakePublished(textToCheck, mistakeIndex)
            }
        }
    }

    private fun clearFirstWhiteSpaceIfExist(s: Editable): Boolean {
        if (s.length == 1 && s.toString() == " ") {
            s.clear()
            return true
        }
        return false
    }

    private fun findWrongCharIndexInWord(checkableTextCharArray: CharArray, firstWordCharArray: CharArray): Int {
        var index = 0
        for (i in firstWordCharArray.indices) {
            if (checkableTextCharArray[i] == firstWordCharArray[i]) {
                continue
            }
            index = i
        }
        return index
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    fun setParagraph(text: String) {
        invalidate()
        val list = text.trim().split(" ").map { "$it " }
        textsQueue.addAll(list.subList(0, list.size - 1))
        textsQueue.add(list.last().trim())
        textCheckerCallback.wordToCheck(textsQueue.peek() ?: "")
    }

    private fun invalidate() {
        textsQueue.clear()
        mistakeIndex = -1
        mistakePublished = false
    }
}