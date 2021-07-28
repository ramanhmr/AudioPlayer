package com.ramanhmr.audioplayer

import android.app.Application
import com.ramanhmr.audioplayer.repositories.ApiTagsRepository
import com.ramanhmr.audioplayer.repositories.FileRepository
import com.ramanhmr.audioplayer.repositories.MusicRepository
import com.ramanhmr.audioplayer.repositories.StatRepository
import org.koin.android.ext.koin.androidContext
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

    }

    private val repositoryModule = module {
        factory { ApiTagsRepository() }
        factory { FileRepository(get()) }
        factory { MusicRepository() }
        factory { StatRepository() }
    }

    private val dataAccessModule = module {

    }
}