package com.webbfontaine.typeracer.inputpage

import android.graphics.Color
import android.os.CountDownTimer
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.webbfontaine.App
import com.webbfontaine.typeracer.R
import com.webbfontaine.typeracer.textchecker.ValidTextCheckerCallback
import java.util.concurrent.TimeUnit

class InputViewModel : ViewModel() {

    private var gameTimer: CountDownTimer? = null
    private var timerCountDownTimer: CountDownTimer? = null
    private val repository = InputRepository()
    private var requested = false
    private var gameStartDelay = TimeUnit.SECONDS.toMillis(5)

    val gameTimeOut = TimeUnit.MINUTES.toMillis(3)
    var gameStartedAt = -1L
    var sentence: SpannableString? = null
    var enteredSymbolsCount = 0 //show the whole and correctly entered words symbols count including sentence whitespaces.
    var currentLineInText = 0
    var previousLineInText = 0
    var wordToCheck = ""
    var mistakes = 0
    var enteredWordsCount = 0

    val textCheckerCallback = object : ValidTextCheckerCallback {
        override fun onMistakePublished(text: String, mistakeAt: Int) {
            mistakes++
        }

        override fun onMistakeFixed(text: String, mistakeAt: Int) {
        }

        override fun onWordPublished(text: String) {
            sentence?.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(App.instance(), R.color.colorPrimary)),
                enteredSymbolsCount, enteredSymbolsCount + text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            enteredSymbolsCount += text.length + 1
            enteredWordsCount++
            newWordMutableLiveData.postValue(text)
            spannableSentenceMutableLiveData.postValue(sentence)
        }

        override fun wordToCheck(text: String) {
            wordToCheck = text
        }
    }

    val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {

        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (before > 0) {
                sentence?.setSpan(
                    ForegroundColorSpan(Color.BLACK),
                    enteredSymbolsCount + start, enteredSymbolsCount + start + before, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } else {
                if (wordToCheck.startsWith(s.toString())) {
                    sentence?.setSpan(
                        ForegroundColorSpan(ContextCompat.getColor(App.instance(), R.color.colorPrimary)),
                        enteredSymbolsCount, enteredSymbolsCount + (s?.length ?: (count + start)), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                } else {
                    sentence?.setSpan(
                        ForegroundColorSpan(ContextCompat.getColor(App.instance(), R.color.colorAccent)),
                        enteredSymbolsCount + start, enteredSymbolsCount + count + start, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
            spannableSentenceMutableLiveData.postValue(sentence)
        }
    }

    private val spannableSentenceMutableLiveData = MutableLiveData<SpannableString?>()
    val spannableSentenceLiveData: LiveData<SpannableString?>
        get() = spannableSentenceMutableLiveData

    private val spannableSentenceNewMutableLiveData = MutableLiveData<SpannableString?>()
    val spannableNewSentenceLiveData: LiveData<SpannableString?>
        get() = spannableSentenceNewMutableLiveData

    private val newWordMutableLiveData = MutableLiveData<String>()
    val newWordLiveData: LiveData<String>
        get() = newWordMutableLiveData

    fun getSentence(newGame: Boolean = false) {
        if (requested || spannableSentenceNewMutableLiveData.value != null && !newGame) {
            return
        }
        requested = true
        repository.getSentence(type = "meat", sentences = 10, success = this::onSentenceRetrieved, error = this::onSentenceFailed)
    }

    private fun onSentenceRetrieved(sentenceString: String) {
        requested = false
        sentence = SpannableString(sentenceString)
        spannableSentenceNewMutableLiveData.postValue(sentence)
    }

    private fun onSentenceFailed(t: Throwable) {
        requested = false
        spannableSentenceNewMutableLiveData.postValue(null)
    }

    fun startCountDownTimer(callback: (t: Long, finished: Boolean) -> Unit) {
        timerCountDownTimer = object : CountDownTimer(gameStartDelay, 1000) {
            override fun onFinish() {
                callback(-1, true)
            }

            override fun onTick(millisUntilFinished: Long) {
                callback((millisUntilFinished / 1000L) + 1, false)
            }
        }
        timerCountDownTimer?.start()
    }

    fun startGameTimer(callback: (t: Long, finished: Boolean) -> Unit) {
        gameTimer = object : CountDownTimer(gameTimeOut, 1000) {
            override fun onFinish() {
                callback(-1, true)
            }

            override fun onTick(millisUntilFinished: Long) {
                callback((millisUntilFinished / 1000L) + 1, false)
            }
        }
        gameTimer?.start()
        gameStartedAt = System.currentTimeMillis()
    }

    override fun onCleared() {
        repository.clear()
        timerCountDownTimer?.cancel()
        gameTimer?.cancel()
        super.onCleared()
    }

    fun getWPM(): Float {
        return (enteredSymbolsCount.toFloat() / 5f).times(60f).div((System.currentTimeMillis() - gameStartedAt) / 1000)
    }

    fun getCompletionPercent(): Float {
        return enteredSymbolsCount.times(100f).div(sentence.toString().length)
    }

    fun getAccuracy(): Float {
        return if (enteredWordsCount == 0) {
            0f
        } else {
            val accuracy = 100f - mistakes.times(100f).div(enteredWordsCount)
            if (accuracy < 0) {
                0f
            } else {
                accuracy
            }
        }
    }

    fun finishGame() {
        gameTimer = null
        timerCountDownTimer = null
        requested = false

        gameStartedAt = -1L
        sentence = null
        enteredSymbolsCount = 0
        currentLineInText = 0
        previousLineInText = 0
        wordToCheck = ""
        mistakes = 0
        enteredWordsCount = 0
    }
}