package com.ramanhmr.audioplayer.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramanhmr.audioplayer.interfaces.LyricsSource
import kotlinx.coroutines.launch

class InfoViewModel(private val lyricsRepository: LyricsSource) : ViewModel() {

    val lyricsLiveData: MutableLiveData<String> = MutableLiveData()

    fun getLyrics(title: String, artist: String) = viewModelScope.launch {
        lyricsLiveData.postValue(lyricsRepository.getLyrics(title, artist))
    }
}