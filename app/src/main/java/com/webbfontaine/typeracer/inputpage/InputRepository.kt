package com.webbfontaine.typeracer.inputpage

import com.webbfontaine.typeracer.network.ApiServiceProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class InputRepository {

    private val compositeDisposable = CompositeDisposable()

    fun getSentence(type: String?, paragraphs: String? = null, sentences: Int? = null, success: (r: String) -> Unit, error: (t: Throwable) -> Unit) {
        compositeDisposable.add(
            ApiServiceProvider.apiService.getText(type, paragraphs, sentences)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    response?.joinToString(" ").toString().apply {
                        if (isNotEmpty()) {
                            if (this[0] == '"') {
                                if (this[length - 1] == '"') {
                                    success(substring(1, length - 1))
                                } else {
                                    success(substring(1, length - 1))
                                }
                            } else {
                                if (this[length - 1] == '"') {
                                    success(substring(0, length - 1))
                                }
                            }
                        }
                    }
                }, error)
        )
    }

    fun clear() {
        compositeDisposable.clear()
    }
}