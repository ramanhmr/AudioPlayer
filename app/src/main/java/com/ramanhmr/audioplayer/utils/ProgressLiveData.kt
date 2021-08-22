package com.ramanhmr.audioplayer.utils

import androidx.lifecycle.MutableLiveData

class ProgressLiveData : MutableLiveData<Int>() {

    fun increase(amount: Int) {
        if (value != null) {
            val tempValue = value!!
            postValue(tempValue + amount)
        }
    }
}