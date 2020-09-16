package com.webbfontaine.typeracer.inputpage

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.webbfontaine.typeracer.R
import com.webbfontaine.typeracer.extensions.closeKeyboard
import com.webbfontaine.typeracer.extensions.makeGone
import com.webbfontaine.typeracer.extensions.makeVisible
import com.webbfontaine.typeracer.extensions.openKeyBoard
import kotlinx.android.synthetic.main.dialog_game_over.view.*
import kotlinx.android.synthetic.main.fragment_input.*
import java.text.DecimalFormat

class InputFragment : Fragment() {

    private lateinit var viewModel: InputViewModel
    private val decimalFormatter = DecimalFormat("#.##")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_input, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(InputViewModel::class.java)
        viewModel.spannableSentenceLiveData.observe(this, Observer<SpannableString?> { spannable ->
            tv_sentence.text = spannable
        })
        viewModel.spannableNewSentenceLiveData.observe(this, Observer<SpannableString?> { spannable ->
            spannable?.let {
                tv_sentence.text = spannable
                sentence_scroll_view.scrollY = 0
                et_checker.invalidateParams()
                et_checker.setParagraph(viewModel.sentence.toString())
                initTimer()
            } ?: kotlin.run {
                Toast.makeText(context, "Something went wrong. Pleas Try again", Toast.LENGTH_LONG).show()
                btn_try_again.makeVisible()
                loading_view.makeGone()
            }
        })
        viewModel.newWordLiveData.observe(this, Observer {
            newWordPublished(it)
        })
    }

    private fun newWordPublished(text: String) {
        val lineStart = tv_sentence.layout.getLineStart(viewModel.currentLineInText)
        val lineEnd = tv_sentence.layout.getLineEnd(viewModel.currentLineInText)
        val lineText = tv_sentence.text.substring(lineStart, lineEnd).trim()
        if (lineText.endsWith(text.trim())) {
            viewModel.currentLineInText++
            if (viewModel.currentLineInText - viewModel.previousLineInText > 1) {
                viewModel.previousLineInText = viewModel.currentLineInText - 1
                sentence_scroll_view.scrollY = sentence_scroll_view.scrollY + tv_sentence.lineHeight
            }
        }
        updateWpm()
    }

    private fun updateWpm() {
        val wpmSpannable = SpannableString("wpm ${decimalFormatter.format(viewModel.getWPM())}")
        wpmSpannable.setSpan(RelativeSizeSpan(0.6f), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        wpmSpannable.setSpan(ForegroundColorSpan(Color.GRAY), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        tv_wpm.text = wpmSpannable
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getSentence()
        et_checker.setTextCheckerCallBack(viewModel.textCheckerCallback)
        et_checker.addTextChangedListener(viewModel.textWatcher)
        btn_try_again.setOnClickListener {
            btn_try_again.makeGone()
            startNewGame()
        }
    }

    private fun initTimer() {
        main_layout.makeGone()
        loading_view.makeGone()
        timer_layout.makeVisible()
        viewModel.startCountDownTimer { t, finished ->
            tv_timer.text = "$t"
            if (finished) {
                initMainView()
            }
        }
    }

    private fun startGameTimer() {
        viewModel.startGameTimer { t, finished ->
            if (finished) {
                finishGame()
            } else {
                val minutes = t / 60
                val seconds = t % 60
                if (minutes == 0L && seconds < 15) {
                    tv_game_timer.text = "Hurry up $minutes:$seconds"
                    tv_game_timer.setTextColor(Color.RED)
                } else {
                    tv_game_timer.text = "$minutes:$seconds"
                    tv_game_timer.setTextColor(Color.BLACK)
                }
            }
        }
    }

    private fun finishGame() {
        et_checker.closeKeyboard()
        et_checker.text?.clear()
        tv_game_timer.text = ""
        tv_wpm.text = ""

        val wpm = viewModel.getWPM()
        val completed = viewModel.getCompletionPercent()
        val accuracy = viewModel.getAccuracy()

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_game_over, null)
        val dialog = AlertDialog.Builder(context).setView(view).setCancelable(false).create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        view.tv_wpm.text = "${decimalFormatter.format(wpm)}"
        view.tv_completion_percent.text = "${decimalFormatter.format(completed)}%"
        view.tv_accuracy.text = "${decimalFormatter.format(accuracy)}%"

        view.btn_new_game.setOnClickListener {
            dialog.dismiss()
            startNewGame()
        }
        dialog.show()
    }

    private fun startNewGame() {
        main_layout.makeGone()
        loading_view.makeVisible()
        viewModel.finishGame()
        viewModel.getSentence(newGame = true)
    }

    private fun initMainView() {
        timer_layout.makeGone()
        main_layout.makeVisible()
        startGameTimer()
        et_checker.openKeyBoard()
    }
}