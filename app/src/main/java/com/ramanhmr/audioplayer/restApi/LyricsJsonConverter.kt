package com.ramanhmr.audioplayer.restApi

import okhttp3.ResponseBody
import retrofit2.Converter

class LyricsJsonConverter<T> : Converter<ResponseBody, T> {
    override fun convert(value: ResponseBody): T? {
        TODO("Not yet implemented")
    }
}