package com.ramanhmr.audioplayer.viewmodels

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ramanhmr.audioplayer.entities.AudioItem
import com.ramanhmr.audioplayer.repositories.AudioRepository

class MainViewModel(private val audioRepository: AudioRepository) : ViewModel() {

    val audioLiveData: MutableLiveData<List<AudioItem>> = MutableLiveData()

    fun updateAudio() {
        audioLiveData.postValue(audioRepository.getAllItems())
    }

    fun getAudioByUri(uri: Uri) = audioRepository.getItemByUri(uri)
}