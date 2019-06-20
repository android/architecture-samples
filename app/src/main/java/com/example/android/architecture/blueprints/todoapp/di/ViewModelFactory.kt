package com.example.android.architecture.blueprints.todoapp.di

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.MapKey
import dagger.Module
import javax.inject.Inject
import javax.inject.Provider
import kotlin.reflect.KClass

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

/**
 * ViewModelFactory which uses Dagger to create the instances.
 */
class TodoViewModelFactory @Inject constructor(
    private val creators: @JvmSuppressWildcards Map<Class<out ViewModel>, Provider<ViewModel>>
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        var creator: Provider<out ViewModel>? = creators[modelClass]
        if (creator == null) {
            for ((key, value) in creators) {
                if (modelClass.isAssignableFrom(key)) {
                    creator = value
                    break
                }
            }
        }
        if (creator == null) {
            throw IllegalArgumentException("Unknown model class: $modelClass")
        }
        try {
            @Suppress("UNCHECKED_CAST")
            return creator.get() as T
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}

@Module
internal abstract class ViewModelBuilder {
    @Binds
    internal abstract fun bindViewModelFactory(
        factory: TodoViewModelFactory
    ): ViewModelProvider.Factory
}

@Target(
    AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)
