package com.example.android.architecture.blueprints.todoapp.di

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

//class ViewModelFactory<T : ViewModel> @Inject constructor(
//    private val viewModel: Provider<T>
//) : ViewModelProvider.Factory {
//
//    @Suppress("UNCHECKED_CAST")
//    override fun <T : ViewModel> create(modelClass: Class<T>) = viewModel.get() as T
//}

//@Suppress("UNCHECKED_CAST")
//inline fun <reified T : ViewModel> Fragment.getViewModel(crossinline factory: () -> T) =
//    ViewModelProviders.of(this, object : ViewModelProvider.Factory {
//        override fun <T : ViewModel> create(modelClass: Class<T>) = factory() as T
//    }).get(T::class.java)

@Suppress("UNCHECKED_CAST")
inline fun <reified T : ViewModel> Fragment.viewModel(
    crossinline provider: () -> T
) = viewModels<T> {
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>) = provider() as T
    }
}
