package com.ramanhmr.audioplayer.entities

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

data class AudioFile(
    val id: Long,
    val uri: Uri,
    val title: String?,
    val album: String?,
    val artist: String?,
    val duration: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readParcelable(Uri::class.java.classLoader)!!,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeParcelable(uri, flags)
        parcel.writeString(title)
        parcel.writeString(album)
        parcel.writeString(artist)
        parcel.writeInt(duration)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AudioFile> {
        override fun createFromParcel(parcel: Parcel): AudioFile {
            return AudioFile(parcel)
        }

        override fun newArray(size: Int): Array<AudioFile?> {
            return arrayOfNulls(size)
        }
    }
}