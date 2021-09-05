package com.ramanhmr.audioplayer

import android.app.Application
import com.ramanhmr.audioplayer.daos.FileDao
import com.ramanhmr.audioplayer.interfaces.FileSource
import com.ramanhmr.audioplayer.interfaces.LyricsSource
import com.ramanhmr.audioplayer.repositories.*
import com.ramanhmr.audioplayer.restApi.LyricsApiUtil
import com.ramanhmr.audioplayer.viewmodels.InfoViewModel
import com.ramanhmr.audioplayer.viewmodels.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.bind
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
        viewModel { InfoViewModel(get()) }
    }

    private val repositoryModule = module {
        factory { ApiTagsRepository() }
        factory { AudioRepository(get(), get()) }
        factory { StatRepository() }
        single { ArtRepository }
        factory { LyricsRepository(get()) } bind LyricsSource::class
    }

    private val dataAccessModule = module {
        factory { FileDao(get()) } bind FileSource::class
        factory { LyricsApiUtil.getLyricsApi() }
    }
}