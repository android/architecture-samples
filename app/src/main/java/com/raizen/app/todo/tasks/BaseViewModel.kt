package com.raizen.app.todo.tasks

import android.util.Log
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseViewModel: ViewModel() {
    private var compositeDisposable: CompositeDisposable? = null

    override fun onCleared() {
        Log.d(TAG, "onCleared")
        super.onCleared()
        dispose()
    }


    fun addDisposable(disposable: Disposable) {
        if (compositeDisposable == null) {
            compositeDisposable = CompositeDisposable()
        }
        compositeDisposable?.add(disposable)
    }

    fun dispose() {
        compositeDisposable?.dispose()
        compositeDisposable = null
    }

    companion object {
        private const val TAG = "::pos::BaseViewModel"
    }


}
