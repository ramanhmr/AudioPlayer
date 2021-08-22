package com.ramanhmr.audioplayer.viewmodels

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramanhmr.audioplayer.entities.AudioItem
import com.ramanhmr.audioplayer.repositories.AudioRepository
import com.ramanhmr.audioplayer.utils.ProgressLiveData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(private val audioRepository: AudioRepository) : ViewModel() {

    val audioLiveData: MutableLiveData<List<AudioItem>> by lazy { MutableLiveData() }
    val positionLiveData: ProgressLiveData by lazy { ProgressLiveData() }

    private var progressIncrement: Job? = null

    fun updateAudio() {
        audioLiveData.postValue(audioRepository.getAllItems())
    }

    fun getAudioByUri(uri: Uri) = audioRepository.getItemByUri(uri)

    fun setProgress(progress: Int) {
        positionLiveData.postValue(progress * MILLIS_UPDATE)
    }

    fun setPosition(position: Int) {
        positionLiveData.postValue(position)
    }

    private fun startProgress() {
        progressIncrement = viewModelScope.launch {
            if (positionLiveData.value != null) {
                while (true) {
                    delay(MILLIS_UPDATE.toLong())
                    positionLiveData.increase(MILLIS_UPDATE)
                }
            }
        }
    }

    fun startProgress(progress: Int) {
        stopProgress()
        setProgress(progress)
        startProgress()
    }

    fun startProgressSetPosition(position: Int) {
        stopProgress()
        setPosition(position)
        startProgress()
    }

    private fun stopProgress() {
        progressIncrement?.cancel(null)
    }

    fun stopProgress(progress: Int) {
        stopProgress()
        setProgress(progress)
    }

    fun stopProgressSetPosition(position: Int) {
        stopProgress()
        setPosition(position)
    }

    companion object {
        const val MILLIS_UPDATE = 250
    }
}