package com.ramanhmr.audioplayer.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramanhmr.audioplayer.repositories.LyricsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class InfoViewModel(private val lyricsRepository: LyricsRepository) : ViewModel() {

    val lyricsLiveData: MutableLiveData<String> = MutableLiveData()

    fun getLyrics(title: String, artist: String) = viewModelScope.launch {
        val lyrics = async { lyricsRepository.getLyrics(title, artist) }
        lyricsLiveData.postValue(lyrics.await())
    }
}