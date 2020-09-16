package com.webbfontaine.typeracer.textchecker

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.text.InputType
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.res.ResourcesCompat
import com.webbfontaine.typeracer.R


class ValidTextEditText : AppCompatEditText {

    private var shouldShowMistakeSymbol = false
    private var mistakeSymbolColor: Int = Color.TRANSPARENT
    private var mistakeBackground = ResourcesCompat.getDrawable(resources, R.drawable.bg_checker_edittext_default, null)
    private var defaultBackground = ResourcesCompat.getDrawable(resources, R.drawable.bg_checker_edittext_default, null)
    private var textCheckerCallBack: ValidTextCheckerCallback? = null

    private val validTextCheckerCallback = object : ValidTextCheckerCallback {
        override fun onMistakePublished(text: String, mistakeAt: Int) {
            textCheckerCallBack?.onMistakePublished(text, mistakeAt)
            updateUI(true)
        }

        override fun onMistakeFixed(text: String, mistakeAt: Int) {
            textCheckerCallBack?.onMistakeFixed(text, mistakeAt)
            updateUI(false)
        }

        override fun onWordPublished(text: String) {
            this@ValidTextEditText.text?.let {
                val endIndex = if (text.length == it.length) text.length else text.length + 1
                Handler().postDelayed({ it.delete(0, endIndex) }, 100)
            }
            textCheckerCallBack?.onWordPublished(text)
        }

        override fun wordToCheck(text: String) {
            textCheckerCallBack?.wordToCheck(text)
        }
    }
    private val textWatcher = ValidTextChecker(validTextCheckerCallback)

    init {
        isSingleLine = true
        maxLines = 1
        gravity = Gravity.CENTER
        background = defaultBackground //
        inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD // disable suggestions and auto correction
        isLongClickable = false // disable copy/past actions
        addTextChangedListener(textWatcher)
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        readAttributes(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        readAttributes(context, attrs)
    }

    private fun readAttributes(context: Context, attrs: AttributeSet?) {
        context.theme?.obtainStyledAttributes(attrs, R.styleable.ValidTextEditText, 0, 0)?.apply {
            shouldShowMistakeSymbol = getBoolean(R.styleable.ValidTextEditText_showMistakeSymbol, false)
            mistakeBackground = getDrawable(R.styleable.ValidTextEditText_mistakeBackground)
            defaultBackground = getDrawable(R.styleable.ValidTextEditText_defaultBackground)
            mistakeSymbolColor = getColor(R.styleable.ValidTextEditText_mistakeSymbolColor, 0)
            recycle()
        }
    }

    fun setTextCheckerCallBack(textCheckerCallBack: ValidTextCheckerCallback) {
        this.textCheckerCallBack = textCheckerCallBack
    }

    private fun updateUI(hasError: Boolean) {
        background = if (hasError) mistakeBackground else defaultBackground
    }

    fun setParagraph(text: String) {
        textWatcher.setParagraph(text)
    }

    fun invalidateParams() {
        updateUI(false)
    }
}