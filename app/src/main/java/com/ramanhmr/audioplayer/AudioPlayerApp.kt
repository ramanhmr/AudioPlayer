package com.ramanhmr.audioplayer

import android.app.Application
import com.ramanhmr.audioplayer.daos.FileDao
import com.ramanhmr.audioplayer.repositories.ApiTagsRepository
import com.ramanhmr.audioplayer.repositories.ArtRepository
import com.ramanhmr.audioplayer.repositories.AudioRepository
import com.ramanhmr.audioplayer.repositories.StatRepository
import com.ramanhmr.audioplayer.viewmodels.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class AudioPlayerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AudioPlayerApp)
            modules(viewModelsModule, repositoryModule, dataAccessModule)
        }
    }

    private val viewModelsModule = module {
        viewModel { MainViewModel(get()) }
    }

    private val repositoryModule = module {
        factory { ApiTagsRepository() }
        factory { AudioRepository(get(), get()) }
        factory { StatRepository() }
        single { ArtRepository }
    }

    private val dataAccessModule = module {
        factory { FileDao(get()) }
    }
}